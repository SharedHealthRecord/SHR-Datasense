package org.sharedhealth.datasense.feeds.encounters;


import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.BaseIntegrationTest;
import org.sharedhealth.datasense.helpers.DatabaseHelper;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.sharedhealth.datasense.model.EncounterBundle;
import org.sharedhealth.datasense.model.Facility;
import org.sharedhealth.datasense.repository.FacilityDao;
import org.sharedhealth.datasense.repository.PatientDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.datasense.helpers.ResourceHelper.asString;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;
import static org.sharedhealth.datasense.util.HeaderUtil.AUTH_TOKEN_KEY;
import static org.sharedhealth.datasense.util.HeaderUtil.CLIENT_ID_KEY;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class DefaultShrEncounterEventWorkerIntegrationTest extends BaseIntegrationTest {

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

    private static String VALID_HEALTH_ID = "98001046534";
    private static String VALID_FACILITY_ID = "10000069";

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        super.loadConfigParameters();
        String authToken = "b7aa1f4001ac4b922dabd6a02a0dabc44cf5af74a0d1b68003ce7ccdb897a1d2";
        UUID token = UUID.randomUUID();

        String response = "{\"access_token\" : \"" + token.toString() + "\"}";
        
        givenThat(post(urlEqualTo("/signin"))
                .withHeader(CLIENT_ID_KEY, equalTo("18552"))
                .withHeader(AUTH_TOKEN_KEY, equalTo(authToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(response)));
        
        givenThat(get(urlEqualTo("/api/default/patients/" + VALID_HEALTH_ID))
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
        bundle.addContent(loadFromXmlFile("dstu2/xmls/p98001046534_encounter_with_all_resources.xml"));
        String shrEncounterId = "shrEncounterId";
        bundle.setEncounterId(shrEncounterId);
        encounterEventWorker.process(bundle);
        assertNotNull(encounterEventWorker);
        assertNotNull(patientDao.findPatientById(VALID_HEALTH_ID));
        Facility facility = facilityDao.findFacilityById(VALID_FACILITY_ID);
        assertNotNull(facility);
        assertEquals("Dohar Upazila Health Complex", facility.getFacilityName());

        assertEquals(1, getEncounterIdList(shrEncounterId).size());

        assertEquals(1, getDiagnosisIdList(shrEncounterId).size());
        assertEquals(5, getObservationIdList(shrEncounterId).size());
        assertEquals(1, getPatientDeathList(shrEncounterId).size());
        assertEquals(1, getProcedureIdList(shrEncounterId).size());
        assertEquals(1, getDiagnosisIdList(shrEncounterId).size());
        assertEquals(1, getMedicationIdList(shrEncounterId).size());
    }

    @Test
    public void shouldUpdateAnExistingEncounter() throws Exception {
        EncounterBundle bundle = new EncounterBundle();
        givenThat(get(urlEqualTo("/api/default/patients/" + VALID_HEALTH_ID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/P" + VALID_HEALTH_ID + ".json"))));


        givenThat(get(urlEqualTo("/api/1.0/facilities/10019841.json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/F10019841.json"))));

        bundle.setHealthId(VALID_HEALTH_ID);
        bundle.addContent(loadFromXmlFile("dstu2/xmls/p98001046534_encounter_with_vitals.xml"));
        String shrEncounterId = "shrEncounterId";
        bundle.setEncounterId(shrEncounterId);
        encounterEventWorker.process(bundle);

        assertEquals(1, getEncounterIdList(shrEncounterId).size());
        assertEquals(5, getObservationIdList(shrEncounterId).size());
        assertEquals(0, getDiagnosisIdList(shrEncounterId).size());


        //update encounter with procedure
        bundle.addContent(loadFromXmlFile("dstu2/xmls/p98001046534_encounter_with_procedure.xml"));
        encounterEventWorker.process(bundle);
        assertEquals(1, getEncounterIdList(shrEncounterId).size());
        assertEquals(1, getProcedureIdList(shrEncounterId).size());
        assertEquals(0, getObservationIdList(shrEncounterId).size());
        assertEquals(0, getDiagnosisIdList(shrEncounterId).size());

        //update encounter with diagnosis
        bundle.addContent(loadFromXmlFile("dstu2/xmls/p98001046534_encounter_with_diagnoses.xml"));
        encounterEventWorker.process(bundle);
        assertEquals(1, getEncounterIdList(shrEncounterId).size());
        assertEquals(1, getDiagnosisIdList(shrEncounterId).size());
        assertEquals(0, getObservationIdList(shrEncounterId).size());
        assertEquals(0, getProcedureIdList(shrEncounterId).size());


        //update encounter with immunization
        bundle.addContent(loadFromXmlFile("dstu2/xmls/p98001046534_encounter_with_immunization.xml"));
        encounterEventWorker.process(bundle);
        assertEquals(1, getEncounterIdList(shrEncounterId).size());
        assertEquals(1, getMedicationIdList(shrEncounterId).size());
        assertEquals(0, getDiagnosisIdList(shrEncounterId).size());
        assertEquals(0, getObservationIdList(shrEncounterId).size());
        assertEquals(0, getProcedureIdList(shrEncounterId).size());
        assertEquals(0, getPatientDeathList(shrEncounterId).size());

        //update encounter with death note
        bundle.addContent(loadFromXmlFile("dstu2/xmls/p98001046534_encounter_with_deathNote.xml"));
        encounterEventWorker.process(bundle);
        assertEquals(1, getEncounterIdList(shrEncounterId).size());
        assertEquals(0, getDiagnosisIdList(shrEncounterId).size());
        assertEquals(0, getObservationIdList(shrEncounterId).size());
        assertEquals(0, getProcedureIdList(shrEncounterId).size());
        assertEquals(1, getPatientDeathList(shrEncounterId).size());
    }

    @Test
    public void shouldProcessEncounterWithObservationNested() throws Exception {
        EncounterBundle bundle = new EncounterBundle();
        givenThat(get(urlEqualTo("/api/default/patients/" + VALID_HEALTH_ID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/P" + VALID_HEALTH_ID + ".json"))));

        givenThat(get(urlEqualTo("api/1.0/facilities/10000069.json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/F10000069.json"))));



        bundle.setHealthId(VALID_HEALTH_ID);
        bundle.addContent(loadFromXmlFile("dstu2/xmls/p98001046534_encounter_with_vitals.xml"));
        String shrEncounterId = "shrEncounterId";
        bundle.setEncounterId(shrEncounterId);
        encounterEventWorker.process(bundle);

        assertEquals(1, getEncounterIdList(shrEncounterId).size());
        assertEquals(5, getObservationIdList(shrEncounterId).size());



    }

    private List<Integer> getProcedureIdList(String shrEncounterId) {
        return jdbcTemplate.queryForList(
                "select procedure_id from procedures where encounter_id= :encounter_id",
                Collections.singletonMap("encounter_id", shrEncounterId), Integer.class);

    }

    private List<Integer> getMedicationIdList(String shrEncounterId) {
        return jdbcTemplate.queryForList(
                "select medication_id from medication where encounter_id= :encounter_id",
                Collections.singletonMap("encounter_id", shrEncounterId), Integer.class);

    }

    private List<Integer> getPatientDeathList(String shrEncounterId) {
        return jdbcTemplate.queryForList(
                "select id from patient_death_details where encounter_id= :encounter_id",
                Collections.singletonMap("encounter_id", shrEncounterId), Integer.class);

    }

    private List<Integer> getDiagnosisIdList(String shrEncounterId) {
        return jdbcTemplate.queryForList(
                "select diagnosis_id from diagnosis where encounter_id= :encounter_id",
                Collections.singletonMap("encounter_id", shrEncounterId), Integer.class);
    }

    private List<String> getEncounterIdList(String shrEncounterId) {
        return jdbcTemplate.queryForList(
                "select encounter_id from encounter where encounter_id= :encounter_id",
                Collections.singletonMap("encounter_id", shrEncounterId), String.class);
    }

    private List<String> getObservationIdList(String shrEncounterId){
        return jdbcTemplate.queryForList("select observation_id from observation where encounter_id = :encounter_id",
                Collections.singletonMap("encounter_id", shrEncounterId), String.class);
    }

    private void showObsDetails(String shrEncounterId, String table, String ... fields) {
        List<Map<String, Object>> maps = jdbcTemplate.queryForList(
                String.format("select * from %s where encounter_id = '%s'", table, shrEncounterId), new EmptySqlParameterSource());

        for (Map<String, Object> map : maps) {
            StringBuilder sb = new StringBuilder();
            for (String field : fields) {
                sb.append(String.format("%s=%s, ", field, map.get(field)));
            }
            System.out.printf(sb.toString());
        }
    }

    @After
    public void tearDown() {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }
}