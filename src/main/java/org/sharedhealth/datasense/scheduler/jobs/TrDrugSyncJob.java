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
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.net.URISyntaxException;

public class TrDrugSyncJob extends QuartzJobBean {

    private DatasenseProperties properties;
    private DrugEventWorker drugEventWorker;
    private DataSourceTransactionManager txMgr;

    public void setProperties(DatasenseProperties properties) {
        this.properties = properties;
    }

    public void setTxMgr(DataSourceTransactionManager txMgr) {
        this.txMgr = txMgr;
    }

    public void setDrugEventWorker(DrugEventWorker drugEventWorker) {
        this.drugEventWorker = drugEventWorker;
    }

    Logger log = Logger.getLogger(TrDrugSyncJob.class);

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        String trMedicationAtomfeedUrl = properties.getTrMedicationfeedUrl();
        AtomFeedSpringTransactionManager transactionManager = new AtomFeedSpringTransactionManager(txMgr);
        TRFeedProcessor TRFeedProcessor =
                new TRFeedProcessor(drugEventWorker,
                        trMedicationAtomfeedUrl,
                        new AllMarkersJdbcImpl(transactionManager),
                        new AllFailedEventsJdbcImpl(transactionManager),
                        transactionManager);
        try {
            TRFeedProcessor.process();
        } catch (URISyntaxException e) {
            String message = String.format("Unable to process drug feed [%s]", trMedicationAtomfeedUrl);
            log.error(message);
            throw new RuntimeException(message, e);
        }
    }
}
