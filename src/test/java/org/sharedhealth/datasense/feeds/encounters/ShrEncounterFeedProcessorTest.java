package org.sharedhealth.datasense.feeds.encounters;

import org.apache.commons.codec.binary.Base64;
import org.hl7.fhir.instance.formats.ResourceOrFeed;
import org.ict4h.atomfeed.client.repository.memory.AllFailedEventsInMemoryImpl;
import org.ict4h.atomfeed.client.repository.memory.AllMarkersInMemoryImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.freeshr.EncounterBundle;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = DatabaseConfig.class)
public class ShrEncounterFeedProcessorTest {

    @Autowired
    DataSourceTransactionManager txMgr;

    @Test
    public void shouldFetchEncountersForCatchment() throws URISyntaxException, IOException {
        ShrEventWorker shrEventWorker = new ShrEventWorker() {
            @Override
            public void process(EncounterBundle encounterBundle) {
                System.out.println(encounterBundle.getEncounterId());
                ResourceOrFeed resourceOrFeed = encounterBundle.getResourceOrFeed();
            }
        };
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("shr.properties");
        Properties properties = new Properties();
        properties.load(inputStream);
        String feedUrl = getFeedUrl(properties);
        ShrEncounterFeedProcessor feedCrawler =
            new ShrEncounterFeedProcessor(
                txMgr, shrEventWorker, feedUrl,
                new AllMarkersInMemoryImpl(),
                new AllFailedEventsInMemoryImpl(),
                getFeedProperties());
        feedCrawler.process();
    }

    private String getFeedUrl(Properties properties) {
        return properties.getProperty("shr.scheme")
                +  "://" + properties.getProperty("shr.host")
                + ":" + properties.getProperty("shr.port")
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