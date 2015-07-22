package org.sharedhealth.datasense.scheduler.jobs;

import org.apache.log4j.Logger;
import org.sharedhealth.datasense.client.MciWebClient;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.feeds.patients.MciFeedProcessor;
import org.sharedhealth.datasense.feeds.patients.PatientUpdateEventWorker;
import org.sharedhealth.datasense.feeds.transaction.AtomFeedSpringTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;

@Component
public class PatientUpdateCrawlerJob {
    @Autowired
    private DataSourceTransactionManager txMgr;
    @Autowired
    private DatasenseProperties properties;
    @Autowired
    private MciWebClient mciWebClient;
    @Autowired
    private PatientUpdateEventWorker patientUpdateEventWorker;

    Logger log = Logger.getLogger(PatientUpdateCrawlerJob.class);


    @Scheduled(fixedDelayString = "${PATIENT_UPDATE_SYNC_JOB_INTERVAL}", initialDelay = 10000)
    public void start() {
        String feedUrl = properties.getMciPatientUpdateFeedUrl();
        AtomFeedSpringTransactionManager transactionManager = new AtomFeedSpringTransactionManager(txMgr);

        MciFeedProcessor feedProcessor = new MciFeedProcessor(feedUrl, transactionManager, mciWebClient, patientUpdateEventWorker, properties);
        try {
            feedProcessor.process();
        } catch (URISyntaxException e) {
            String errorMessage = String.format("Unable to process patient update feed [%s]", feedUrl);
            log.error(errorMessage);
            throw new RuntimeException(errorMessage, e);
        }
    }
}
