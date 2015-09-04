package org.sharedhealth.datasense.feeds.encounters;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import org.apache.commons.codec.binary.Base64;
import org.ict4h.atomfeed.client.repository.memory.AllFailedEventsInMemoryImpl;
import org.ict4h.atomfeed.client.repository.memory.AllMarkersInMemoryImpl;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.client.ShrWebClient;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.feeds.transaction.AtomFeedSpringTransactionManager;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.sharedhealth.datasense.model.EncounterBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.getenv;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = DatabaseConfig.class)
public class ShrEncounterFeedProcessorTest {

    @Autowired
    DataSourceTransactionManager txMgr;

    @Autowired
    private ShrWebClient shrWebClient;

    @Autowired
    private DatasenseProperties properties;

    @Test
    @Ignore
    public void shouldFetchEncountersForCatchment() throws URISyntaxException, IOException {
        EncounterEventWorker encounterEventWorker = new EncounterEventWorker() {
            @Override
            public void process(EncounterBundle encounterBundle) {
                Bundle bundle = encounterBundle.getBundle();
            }
        };
        String feedUrl = getFeedUrl();
        ShrEncounterFeedProcessor feedCrawler =
                new ShrEncounterFeedProcessor(encounterEventWorker,
                        feedUrl,
                        new AllMarkersInMemoryImpl(),
                        new AllFailedEventsInMemoryImpl(),
                        new AtomFeedSpringTransactionManager(txMgr), shrWebClient, properties, null);
        feedCrawler.process();
    }

    private String getFeedUrl() {
        Map<String, String> env = getenv();
        return env.get("SHR_SCHEME")
                + "://" + env.get("SHR_HOST")
                + ":" + env.get("SHR_PORT")
                + "/catchments/3026/encounters";
    }

    public Map<String, Object> getFeedProperties() {
        Map<String, Object> feedProps = new HashMap<String, Object>();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "application/atom+xml");
        headers.put("facilityId", getFacilityId());
        headers.put("Authorization", getAuthHeader());
        feedProps.put("headers", headers);
        return feedProps;
    }

    private String getFacilityId() {
        return "10000069";
    }

    public String getAuthHeader() {
        String auth = "shr" + ":" + "password";
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("UTF-8")));
        return "Basic " + new String(encodedAuth);
    }
}