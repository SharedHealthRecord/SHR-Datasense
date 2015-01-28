package org.sharedhealth.datasense.feeds.tr;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.ict4h.atomfeed.client.repository.memory.AllFailedEventsInMemoryImpl;
import org.ict4h.atomfeed.client.repository.memory.AllMarkersInMemoryImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.feeds.transaction.AtomFeedSpringTransactionManager;
import org.sharedhealth.datasense.helpers.DatabaseHelper;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.sharedhealth.datasense.model.tr.TrConcept;
import org.sharedhealth.datasense.repository.ConceptDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.sharedhealth.datasense.helpers.ResourceHelper.asString;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {TestConfig.class, DatabaseConfig.class})
public class TrConceptFeedProcessorIT {
    private final String trConceptAtomfeedUrl = "/openmrs/ws/atomfeed/concept/";
    private final static String CONCEPT_UUID = "575ee049-bc0d-459c-9e19-07f1151fe0d6";
    @Autowired
    private ConceptEventWorker conceptEventWorker;
    @Autowired
    private DataSourceTransactionManager txMgr;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    @Autowired
    private ConceptDao conceptDao;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Before
    public void setUp() throws Exception {
        givenThat(get(urlMatching(trConceptAtomfeedUrl + "[0-9]+"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(asString("xmls/conceptAtomfeed.xml"))));
        givenThat(get(urlMatching("/openmrs/ws/rest/v1/tr/concepts/" + CONCEPT_UUID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(asString("jsons/C" + CONCEPT_UUID + ".json"))));
    }

    @Test
    public void shouldProcessConceptAtomFeedUrl() throws Exception {
        String trBasePath = "http://localhost:9997";
        new TrConceptFeedProcessor(conceptEventWorker,
                trBasePath + trConceptAtomfeedUrl + "1",
                new AllMarkersInMemoryImpl(),
                new AllFailedEventsInMemoryImpl(),
                new AtomFeedSpringTransactionManager(txMgr)).process();
        assertNotNull(conceptDao.findByConceptUuid(CONCEPT_UUID));
    }

    @After
    public void tearDown() throws Exception {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }
}