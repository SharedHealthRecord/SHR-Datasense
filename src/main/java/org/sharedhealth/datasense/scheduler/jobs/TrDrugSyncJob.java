package org.sharedhealth.datasense.scheduler.jobs;

import org.apache.log4j.Logger;
import org.ict4h.atomfeed.client.repository.jdbc.AllFailedEventsJdbcImpl;
import org.ict4h.atomfeed.client.repository.jdbc.AllMarkersJdbcImpl;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.feeds.tr.DrugEventWorker;
import org.sharedhealth.datasense.feeds.tr.TRFeedProcessor;
import org.sharedhealth.datasense.feeds.transaction.AtomFeedSpringTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.Scheduled;
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

    Logger log = Logger.getLogger(TrDrugSyncJob.class);

    @Scheduled(fixedDelayString = "${TR_SYNC_JOB_INTERVAL}", initialDelay = 120000)
    public void start() {
        String trMedicationAtomfeedUrl = properties.getTrMedicationfeedUrl();
        log.info("Crawling feed:" + trMedicationAtomfeedUrl);

        AtomFeedSpringTransactionManager transactionManager = new AtomFeedSpringTransactionManager(txMgr);
        TRFeedProcessor feedProcessor =
                new TRFeedProcessor(drugEventWorker,
                        trMedicationAtomfeedUrl,
                        new AllMarkersJdbcImpl(transactionManager),
                        new AllFailedEventsJdbcImpl(transactionManager),
                        transactionManager, properties);
        try {
            feedProcessor.process();
        } catch (Exception e) {
            String message = String.format("Unable to process drug feed [%s]", trMedicationAtomfeedUrl);
            log.error(message, e);
            throw new RuntimeException(message, e);
        }
    }
}
