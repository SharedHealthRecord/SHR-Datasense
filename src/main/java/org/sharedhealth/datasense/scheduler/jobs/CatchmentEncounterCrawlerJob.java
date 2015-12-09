package org.sharedhealth.datasense.scheduler.jobs;

import org.apache.log4j.Logger;
import org.ict4h.atomfeed.client.repository.jdbc.AllFailedEventsJdbcImpl;
import org.ict4h.atomfeed.client.repository.jdbc.AllMarkersJdbcImpl;
import org.sharedhealth.datasense.client.ShrWebClient;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.feeds.encounters.EncounterEventWorker;
import org.sharedhealth.datasense.feeds.encounters.FhirBundleUtil;
import org.sharedhealth.datasense.feeds.encounters.ShrEncounterFeedProcessor;
import org.sharedhealth.datasense.feeds.transaction.AtomFeedSpringTransactionManager;
import org.sharedhealth.datasense.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CatchmentEncounterCrawlerJob {
    @Autowired
    private DataSourceTransactionManager txMgr;
    @Autowired
    private DatasenseProperties properties;
    @Autowired
    private ShrWebClient shrWebClient;
    @Autowired
    private EncounterEventWorker encounterEventWorker;
    @Autowired
    FhirBundleUtil bundleUtil;

    Logger log = Logger.getLogger(CatchmentEncounterCrawlerJob.class);


    @Scheduled(fixedDelayString = "${ENCOUNTER_SYNC_JOB_INTERVAL}", initialDelay = 10000)
    public void start() {
        for (String catchment : properties.getDatasenseCatchmentList()) {
            String feedUrl = StringUtil.ensureSuffix(properties.getShrBaseUrl(), "/") + "catchments/" + catchment + "/encounters";
            AtomFeedSpringTransactionManager transactionManager = new AtomFeedSpringTransactionManager(txMgr);
            ShrEncounterFeedProcessor feedCrawler =
                    new ShrEncounterFeedProcessor(
                            encounterEventWorker, feedUrl,
                            new AllMarkersJdbcImpl(transactionManager),
                            new AllFailedEventsJdbcImpl(transactionManager),
                            transactionManager, shrWebClient, properties, bundleUtil);
            try {
                log.info("Crawling feed:" + feedUrl);
                feedCrawler.process();
            } catch (Exception e) {
                String errorMessage = String.format("Unable to process encounter catchment feed [%s]", feedUrl);
                log.error(errorMessage, e);
                throw new RuntimeException(errorMessage, e);
            }
        }
    }
}
