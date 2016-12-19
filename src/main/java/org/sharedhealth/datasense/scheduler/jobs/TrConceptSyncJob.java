package org.sharedhealth.datasense.scheduler.jobs;

import org.apache.log4j.Logger;
import org.ict4h.atomfeed.client.repository.jdbc.AllFailedEventsJdbcImpl;
import org.ict4h.atomfeed.client.repository.jdbc.AllMarkersJdbcImpl;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.feeds.tr.ConceptEventWorker;
import org.sharedhealth.datasense.feeds.tr.TRFeedProcessor;
import org.sharedhealth.datasense.feeds.transaction.AtomFeedSpringTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.Scheduled;
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

    Logger log = Logger.getLogger(TrConceptSyncJob.class);

    @Scheduled(fixedDelayString = "${TR_SYNC_JOB_INTERVAL}", initialDelay = 120000)
    public void start() {
        String trConceptAtomfeedUrl = properties.getTrConceptAtomfeedUrl();
        log.info("Crawling feed:" + trConceptAtomfeedUrl);

        AtomFeedSpringTransactionManager transactionManager = new AtomFeedSpringTransactionManager(txMgr);
        TRFeedProcessor feedProcessor =
                new TRFeedProcessor(
                        conceptEventWorker, trConceptAtomfeedUrl,
                        new AllMarkersJdbcImpl(transactionManager),
                        new AllFailedEventsJdbcImpl(transactionManager),
                        transactionManager, properties);
        try {
            feedProcessor.process();
        } catch (URISyntaxException e) {
            String message = String.format("Unable to process concept feed [%s]", trConceptAtomfeedUrl);
            log.error(message, e);
            throw new RuntimeException(message, e);
        }
    }
}
