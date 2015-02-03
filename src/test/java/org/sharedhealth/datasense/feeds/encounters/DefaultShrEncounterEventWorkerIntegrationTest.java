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
import org.sharedhealth.datasense.model.Facility;
import org.sharedhealth.datasense.repository.FacilityDao;
import org.sharedhealth.datasense.repository.PatientDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.datasense.helpers.ResourceHelper.asString;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class DefaultShrEncounterEventWorkerIntegrationTest {

    @Autowired
    private EncounterEventWorker encounterEventWorker;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private PatientDao patientDao;

    @Autowired
    private FacilityDao facilityDao;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    private static String VALID_HEALTH_ID = "5942395046400622593";
    private static String VALID_FACILITY_ID = "10000069";

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        givenThat(get(urlEqualTo("/api/v1/patients/" + VALID_HEALTH_ID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/P" + VALID_HEALTH_ID + ".json"))));
        givenThat(get(urlEqualTo("/api/1.0/facilities/" + VALID_FACILITY_ID + ".json"))
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
        String shrEncounterId = "shrEncounterId";
        bundle.setEncounterId(shrEncounterId);
        encounterEventWorker.process(bundle);
        assertNotNull(encounterEventWorker);
        assertNotNull(patientDao.findPatientById(VALID_HEALTH_ID));
        Facility facility = facilityDao.findFacilityById(VALID_FACILITY_ID);
        assertNotNull(facility);
        assertEquals("Dohar Upazila Health Complex", facility.getFacilityName());

        assertEquals(1, getEncounterList(shrEncounterId).size());

        assertEquals(1, getDiagnosisList(shrEncounterId).size());
    }

    private List<Integer> getDiagnosisList(String shrEncounterId) {
        return jdbcTemplate.queryForList(
                "select diagnosis_id from diagnosis where encounter_id= :encounter_id",
                Collections.singletonMap("encounter_id", shrEncounterId), Integer.class);
    }

    private List<String> getEncounterList(String shrEncounterId) {
        return jdbcTemplate.queryForList(
                "select encounter_id from encounter where encounter_id= :encounter_id",
                Collections.singletonMap("encounter_id", shrEncounterId), String.class);
    }

    @After
    public void tearDown() {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }

}