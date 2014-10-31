package org.sharedhealth.datasense.scheduler.jobs;

import org.apache.commons.codec.binary.Base64;
import org.hl7.fhir.instance.formats.ResourceOrFeed;
import org.ict4h.atomfeed.client.repository.jdbc.AllFailedEventsJdbcImpl;
import org.ict4h.atomfeed.client.repository.jdbc.AllMarkersJdbcImpl;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.feeds.encounters.ShrEncounterFeedProcessor;
import org.sharedhealth.datasense.feeds.encounters.ShrEventWorker;
import org.sharedhealth.datasense.feeds.transaction.AtomFeedSpringTransactionManager;
import org.sharedhealth.datasense.freeshr.EncounterBundle;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


public class CatchmentEncounterCrawlerJob extends QuartzJobBean {

    private int catchment = 3026;
    DataSourceTransactionManager txMgr;
    private DatasenseProperties properties;

    public void setTxMgr(DataSourceTransactionManager txMgr) {
        this.txMgr = txMgr;
    }

    public void setProperties(DatasenseProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        ShrEventWorker shrEventWorker = new ShrEventWorker() {
            @Override
            public void process(EncounterBundle encounterBundle) {
                System.out.println(encounterBundle.getEncounterId());
                ResourceOrFeed resourceOrFeed = encounterBundle.getResourceOrFeed();
            }
        };
        String feedUrl = getFeedUrl(properties);
        AtomFeedSpringTransactionManager transactionManager = new AtomFeedSpringTransactionManager(txMgr);
        ShrEncounterFeedProcessor feedCrawler =
                new ShrEncounterFeedProcessor(
                        shrEventWorker, feedUrl,
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

    private String getFeedUrl(DatasenseProperties properties) {
        //TODO : for all patients
        for (String catchment : properties.getDatasenseCatchmentList()) {
            return properties.getShrBaseUrl() + "/catchments/" + catchment + "/encounters";
        }
        return null;
    }

    public Map<String, Object> getFeedProperties(DatasenseProperties properties) {
        Map<String, Object> feedProps = new HashMap<String, Object>();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "application/atom+xml");
        headers.put("facilityId", properties.getDatasenseFacilityId());
        headers.put("Authorization", getAuthHeader(properties));
        feedProps.put("headers", headers);
        return feedProps;
    }

    public String getAuthHeader(DatasenseProperties properties) {
        String auth = properties.getShrHost() + ":" + properties.getShrPassword();
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("UTF-8")));
        return "Basic " + new String(encodedAuth);
    }
}
