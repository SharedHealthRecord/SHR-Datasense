package org.sharedhealth.datasense.feeds.tr;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.ict4h.atomfeed.client.repository.memory.AllFailedEventsInMemoryImpl;
import org.ict4h.atomfeed.client.repository.memory.AllMarkersInMemoryImpl;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.feeds.transaction.AtomFeedSpringTransactionManager;
import org.sharedhealth.datasense.helpers.DatabaseHelper;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.sharedhealth.datasense.repository.ConceptDao;
import org.sharedhealth.datasense.repository.ReferenceTermDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertNotNull;
import static org.sharedhealth.datasense.helpers.ResourceHelper.asString;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {TestConfig.class, DatabaseConfig.class})
public class FeedProcessorIT {
    private final String trConceptAtomfeedUrl = "/openmrs/ws/atomfeed/concept/";
    private final static String CONCEPT_UUID = "575ee049-bc0d-459c-9e19-07f1151fe0d6";
    @Autowired
    private ConceptEventWorker conceptEventWorker;

    private final String trReferenceTermAtomfeedUrl = "/openmrs/ws/atomfeed/conceptreferenceterm/";
    private final static String REFERENCE_TERM_UUID = "66b81088-adce-4432-83b7-46fbc14ffa85";
    @Autowired
    private ReferenceTermEventWorker referenceTermEventWorker;

    @Autowired
    private DataSourceTransactionManager txMgr;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    @Autowired
    private ConceptDao conceptDao;
    @Autowired
    private ReferenceTermDao referenceTermDao;
    @Autowired
    private DatasenseProperties properties;


    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Test
    public void shouldProcessConceptAtomFeedUrl() throws Exception {
        givenThat(get(urlMatching(trConceptAtomfeedUrl + "[0-9]+"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(asString("xmls/conceptAtomfeed.xml"))));
        givenThat(get(urlMatching("/openmrs/ws/rest/v1/tr/concepts/" + CONCEPT_UUID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(asString("jsons/C" + CONCEPT_UUID + ".json"))));

        String trBasePath = "http://localhost:9997";
        new TRFeedProcessor(conceptEventWorker,
                trBasePath + trConceptAtomfeedUrl + "1",
                new AllMarkersInMemoryImpl(),
                new AllFailedEventsInMemoryImpl(),
                new AtomFeedSpringTransactionManager(txMgr), properties).process();
        assertNotNull(conceptDao.findByConceptUuid(CONCEPT_UUID));
    }

    @Test
    public void shouldProcessReferenceTermAtomFeedUrl() throws Exception {
        givenThat(get(urlMatching(trReferenceTermAtomfeedUrl + "[0-9]+"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(asString("xmls/referenceTermAtomfeed.xml"))));
        givenThat(get(urlMatching("/openmrs/ws/rest/v1/tr/referenceterms/" + REFERENCE_TERM_UUID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(asString("jsons/R" + REFERENCE_TERM_UUID + ".json"))));



        String trBasePath = "http://localhost:9997";
        new TRFeedProcessor(referenceTermEventWorker,
                trBasePath + trReferenceTermAtomfeedUrl + "1",
                new AllMarkersInMemoryImpl(),
                new AllFailedEventsInMemoryImpl(),
                new AtomFeedSpringTransactionManager(txMgr), properties).process();
        assertNotNull(referenceTermDao.findByReferenceTermUuid(REFERENCE_TERM_UUID));
    }

    @After
    public void tearDown() throws Exception {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }
}