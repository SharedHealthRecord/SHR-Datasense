package org.sharedhealth.datasense.scheduler.jobs;

import org.ict4h.atomfeed.client.repository.jdbc.AllFailedEventsJdbcImpl;
import org.ict4h.atomfeed.client.repository.jdbc.AllMarkersJdbcImpl;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.feeds.encounters.EncounterEventWorker;
import org.sharedhealth.datasense.feeds.encounters.ShrEncounterFeedProcessor;
import org.sharedhealth.datasense.feeds.transaction.AtomFeedSpringTransactionManager;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class CatchmentEncounterCrawlerJob extends QuartzJobBean {

    public static final String AUTH_HEADER = "X-Auth-Token";
    DataSourceTransactionManager transactionManager;
    private DatasenseProperties properties;

    private EncounterEventWorker encounterEventWorker;

    public void setTransactionManager(DataSourceTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void setProperties(DatasenseProperties properties) {
        this.properties = properties;
    }

    public void setEncounterEventWorker(EncounterEventWorker encounterEventWorker) {
        this.encounterEventWorker = encounterEventWorker;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        for (String catchment : properties.getDatasenseCatchmentList()) {
            String feedUrl = properties.getShrBaseUrl() + "/catchments/" + catchment + "/encounters";
            AtomFeedSpringTransactionManager transactionManager = new AtomFeedSpringTransactionManager(this
                    .transactionManager);
            ShrEncounterFeedProcessor feedCrawler =
                    new ShrEncounterFeedProcessor(
                            encounterEventWorker, feedUrl,
                            new AllMarkersJdbcImpl(transactionManager),
                            new AllFailedEventsJdbcImpl(transactionManager),
                            getFeedProperties(properties),
                            transactionManager);
            try {
                feedCrawler.process();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<String, Object> getFeedProperties(DatasenseProperties properties) {
        Map<String, Object> feedProps = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/atom+xml");
        headers.put("facilityId", properties.getDatasenseFacilityId());
        headers.put(AUTH_HEADER, UUID.randomUUID().toString());
        feedProps.put("headers", headers);
        return feedProps;
    }

}
