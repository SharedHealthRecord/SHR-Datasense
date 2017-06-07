package org.sharedhealth.datasense.scheduler.jobs;

import org.apache.log4j.Logger;
import org.ict4h.atomfeed.client.repository.jdbc.AllFailedEventsJdbcImpl;
import org.ict4h.atomfeed.client.repository.jdbc.AllMarkersJdbcImpl;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.feeds.tr.ReferenceTermEventWorker;
import org.sharedhealth.datasense.feeds.tr.TRFeedProcessor;
import org.sharedhealth.datasense.feeds.transaction.AtomFeedSpringTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TrReferenceTermSyncJob {
    @Autowired
    private DatasenseProperties properties;
    @Autowired
    private ReferenceTermEventWorker referenceTermEventWorker;
    @Autowired
    private DataSourceTransactionManager txMgr;

    Logger log = Logger.getLogger(TrReferenceTermSyncJob.class);

    @Scheduled(fixedDelayString = "${TR_SYNC_JOB_INTERVAL}", initialDelay = 10000)
    public void start() {
        String trReferenceTermAtomfeedUrl = properties.getTrReferenceTermAtomfeedUrl();
        log.info("Crawling feed:" + trReferenceTermAtomfeedUrl);

        AtomFeedSpringTransactionManager transactionManager = new AtomFeedSpringTransactionManager(txMgr);
        TRFeedProcessor feedProcessor =
                new TRFeedProcessor(
                        referenceTermEventWorker,
                        trReferenceTermAtomfeedUrl,
                        new AllMarkersJdbcImpl(transactionManager),
                        new AllFailedEventsJdbcImpl(transactionManager),
                        transactionManager, properties);
        try {
            feedProcessor.process();
        } catch (Exception e) {
            String message = String.format("Unable to process reference term feed [%s]", trReferenceTermAtomfeedUrl);
            log.error(message, e);
            throw new RuntimeException(message, e);
        }
    }
}
