package org.sharedhealth.datasense.scheduler.jobs;

import org.apache.log4j.Logger;
import org.ict4h.atomfeed.client.repository.jdbc.AllFailedEventsJdbcImpl;
import org.ict4h.atomfeed.client.repository.jdbc.AllMarkersJdbcImpl;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.feeds.tr.TRFeedProcessor;
import org.sharedhealth.datasense.feeds.tr.ReferenceTermEventWorker;
import org.sharedhealth.datasense.feeds.transaction.AtomFeedSpringTransactionManager;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.net.URISyntaxException;

public class TrReferenceTermSyncJob extends QuartzJobBean{

    private DatasenseProperties properties;
    private ReferenceTermEventWorker referenceTermEventWorker;
    private DataSourceTransactionManager txMgr;

    public void setProperties(DatasenseProperties properties) {
        this.properties = properties;
    }

    public void setReferenceTermEventWorker(ReferenceTermEventWorker referenceTermEventWorker) {
        this.referenceTermEventWorker = referenceTermEventWorker;
    }

    public void setTxMgr(DataSourceTransactionManager txMgr) {
        this.txMgr = txMgr;
    }

    Logger log = Logger.getLogger(TrReferenceTermSyncJob.class);

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        String trReferenceTermAtomfeedUrl = properties.getTrReferenceTermAtomfeedUrl();
        AtomFeedSpringTransactionManager transactionManager = new AtomFeedSpringTransactionManager(txMgr);
        TRFeedProcessor TRFeedProcessor =
                new TRFeedProcessor(
                        referenceTermEventWorker,
                        trReferenceTermAtomfeedUrl,
                        new AllMarkersJdbcImpl(transactionManager),
                        new AllFailedEventsJdbcImpl(transactionManager),
                        transactionManager);
        try {
            TRFeedProcessor.process();
        } catch (URISyntaxException e) {
            log.error(String.format("Unable to process reference term feed [%s]", trReferenceTermAtomfeedUrl));
            e.printStackTrace();
        }
    }
}
