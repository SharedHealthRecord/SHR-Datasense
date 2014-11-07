package org.sharedhealth.datasense.feeds.encounters;


import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.helpers.DatabaseHelper;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.sharedhealth.datasense.model.EncounterBundle;
import org.sharedhealth.datasense.repository.FacilityDao;
import org.sharedhealth.datasense.repository.PatientDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.sharedhealth.datasense.helpers.ResourceHelper.asString;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class DefaultShrEncounterEventWorkerIntegrationTest {

    @Autowired
    private EncounterEventWorker encounterEventWorker;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PatientDao patientDao;

    @Autowired
    private FacilityDao facilityDao;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8081);

    private static String VALID_HEALTH_ID = "5927558688825933825";
    private static String VALID_FACILITY_ID = "10000059";

    @Before
    public void setUp() throws Exception {
        givenThat(get(urlEqualTo("/api/v1/patients/" + VALID_HEALTH_ID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/P" + VALID_HEALTH_ID + ".json"))));
        givenThat(get(urlEqualTo("/~stagedgh/dghshrml4/public/api/1.0/facilities/" + VALID_FACILITY_ID + ".json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/F" + VALID_FACILITY_ID + ".json"))));
    }


    @Test
    public void shouldParseAndStoreEncounters() throws IOException {
        EncounterBundle bundle = new EncounterBundle();
        bundle.setHealthId(VALID_HEALTH_ID);
        bundle.addContent(loadFromXmlFile("xmls/sampleEncounter.xml"));
        encounterEventWorker.process(bundle);
        assertNotNull(encounterEventWorker);
        assertNotNull(patientDao.getPatientById(VALID_HEALTH_ID));
        assertEquals("Test:Amta Union Sub Center", facilityDao.findFacilityById(VALID_FACILITY_ID).getFacilityName());
    }

    @After
    public void tearDown() {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }

}