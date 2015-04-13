package org.sharedhealth.datasense.scheduler.jobs;

import org.apache.log4j.Logger;
import org.ict4h.atomfeed.client.repository.jdbc.AllFailedEventsJdbcImpl;
import org.ict4h.atomfeed.client.repository.jdbc.AllMarkersJdbcImpl;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.feeds.tr.ConceptEventWorker;
import org.sharedhealth.datasense.feeds.tr.TRFeedProcessor;
import org.sharedhealth.datasense.feeds.transaction.AtomFeedSpringTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;

@Component
public class TrConceptSyncJob {
    @Autowired
    private DatasenseProperties properties;
    @Autowired
    private ConceptEventWorker conceptEventWorker;
    @Autowired
    private DataSourceTransactionManager txMgr;

//    public void setProperties(DatasenseProperties properties) {
//        this.properties = properties;
//    }
//
//    public void setConceptEventWorker(ConceptEventWorker conceptEventWorker) {
//        this.conceptEventWorker = conceptEventWorker;
//    }
//
//    public void setTxMgr(DataSourceTransactionManager txMgr) {
//        this.txMgr = txMgr;
//    }

    Logger log = Logger.getLogger(TrConceptSyncJob.class);

    @Scheduled(fixedDelay = 2000, initialDelay = 2000)
    public void executeInternal() {
        String trConceptAtomfeedUrl = properties.getTrConceptAtomfeedUrl();
        AtomFeedSpringTransactionManager transactionManager = new AtomFeedSpringTransactionManager(txMgr);
        TRFeedProcessor feedProcessor =
                new TRFeedProcessor(
                        conceptEventWorker, trConceptAtomfeedUrl,
                        new AllMarkersJdbcImpl(transactionManager),
                        new AllFailedEventsJdbcImpl(transactionManager),
                        transactionManager);
        try {
            feedProcessor.process();
        } catch (URISyntaxException e) {
            String message = String.format("Unable to process concept feed [%s]", trConceptAtomfeedUrl);
            log.error(message);
            throw new RuntimeException(message, e);
        }
    }
}
