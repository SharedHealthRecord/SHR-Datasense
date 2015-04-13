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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;

@Component
public class TrReferenceTermSyncJob{
    @Autowired
    private DatasenseProperties properties;
    @Autowired
    private ReferenceTermEventWorker referenceTermEventWorker;
    @Autowired
    private DataSourceTransactionManager txMgr;

    Logger log = Logger.getLogger(TrReferenceTermSyncJob.class);

    @Scheduled(fixedDelay = 2000, initialDelay = 2000)
    public void executeInternal() {
        String trReferenceTermAtomfeedUrl = properties.getTrReferenceTermAtomfeedUrl();
        AtomFeedSpringTransactionManager transactionManager = new AtomFeedSpringTransactionManager(txMgr);
        TRFeedProcessor feedProcessor =
                new TRFeedProcessor(
                        referenceTermEventWorker,
                        trReferenceTermAtomfeedUrl,
                        new AllMarkersJdbcImpl(transactionManager),
                        new AllFailedEventsJdbcImpl(transactionManager),
                        transactionManager);
        try {
            feedProcessor.process();
        } catch (URISyntaxException e) {
            String message = String.format("Unable to process reference term feed [%s]", trReferenceTermAtomfeedUrl);
            log.error(message);
            throw new RuntimeException(message, e);
        }
    }
}
