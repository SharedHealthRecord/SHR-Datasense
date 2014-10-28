package org.sharedhealth.datasense.feeds.encounters;

import org.hl7.fhir.instance.formats.ResourceOrFeed;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.freeshr.EncounterBundle;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URISyntaxException;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = DatabaseConfig.class)
public class ShrEncounterFeedProcessorTest {

    @Autowired
    DataSourceTransactionManager txMgr;

    @Test
    public void shouldFetchEncountersForCatchment() throws URISyntaxException {
        ShrEventWorker shrEventWorker = new ShrEventWorker() {
            @Override
            public void process(EncounterBundle encounterBundle) {
                System.out.println(encounterBundle.getEncounterId());
                ResourceOrFeed resourceOrFeed = encounterBundle.getResourceOrFeed();
            }
        };
        ShrEncounterFeedProcessor feedCrawler = new ShrEncounterFeedProcessor(txMgr, shrEventWorker);
        feedCrawler.process();

    }


}