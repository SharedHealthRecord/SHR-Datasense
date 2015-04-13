package org.sharedhealth.datasense.scheduler.jobs;

import org.apache.log4j.Logger;
import org.ict4h.atomfeed.client.repository.jdbc.AllFailedEventsJdbcImpl;
import org.ict4h.atomfeed.client.repository.jdbc.AllMarkersJdbcImpl;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.feeds.tr.DrugEventWorker;
import org.sharedhealth.datasense.feeds.tr.TRFeedProcessor;
import org.sharedhealth.datasense.feeds.transaction.AtomFeedSpringTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;

@Component
public class TrDrugSyncJob {
    @Autowired
    private DatasenseProperties properties;
    @Autowired
    private DrugEventWorker drugEventWorker;
    @Autowired
    private DataSourceTransactionManager txMgr;

//    public void setProperties(DatasenseProperties properties) {
//        this.properties = properties;
//    }
//
//    public void setTxMgr(DataSourceTransactionManager txMgr) {
//        this.txMgr = txMgr;
//    }
//
//    public void setDrugEventWorker(DrugEventWorker drugEventWorker) {
//        this.drugEventWorker = drugEventWorker;
//    }

    Logger log = Logger.getLogger(TrDrugSyncJob.class);
    
    @Scheduled(fixedDelay = 2000, initialDelay = 2000)
    public void executeInternal() {
        String trMedicationAtomfeedUrl = properties.getTrMedicationfeedUrl();
        AtomFeedSpringTransactionManager transactionManager = new AtomFeedSpringTransactionManager(txMgr);
        TRFeedProcessor feedProcessor =
                new TRFeedProcessor(drugEventWorker,
                        trMedicationAtomfeedUrl,
                        new AllMarkersJdbcImpl(transactionManager),
                        new AllFailedEventsJdbcImpl(transactionManager),
                        transactionManager);
        try {
            feedProcessor.process();
        } catch (URISyntaxException e) {
            String message = String.format("Unable to process drug feed [%s]", trMedicationAtomfeedUrl);
            log.error(message);
            throw new RuntimeException(message, e);
        }
    }
}
