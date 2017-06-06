package org.sharedhealth.datasense.handler;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.BaseIntegrationTest;
import org.sharedhealth.datasense.helpers.DatabaseHelper;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.sharedhealth.datasense.model.Encounter;
import org.sharedhealth.datasense.model.Patient;
import org.sharedhealth.datasense.model.PatientDeathDetails;
import org.sharedhealth.datasense.model.fhir.BundleContext;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class DeathNoteHandlerIT extends BaseIntegrationTest {

    @Autowired
    private DeathNoteHandler deathNoteHandler;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private EncounterComposition composition;
    private Resource deathNoteResource;

    private static final String PATIENT_HID = "98001046534";
    private static final String SHR_ENCOUNTER_ID = "shrEncounterId";

    @Before
    public void setUp() throws Exception {
        super.loadConfigParameters();
        Bundle bundle = loadFromXmlFile("stu3/p98001046534_encounter_with_deathNote.xml");
        BundleContext bundleContext = new BundleContext(bundle, SHR_ENCOUNTER_ID);
        composition = bundleContext.getEncounterCompositions().get(0);
        Patient patient = new Patient();
        patient.setDateOfBirth(new SimpleDateFormat("yyyy-MM-dd").parse("2013-12-28"));
        patient.setHid(PATIENT_HID);
        Encounter encounter = new Encounter();
        encounter.setEncounterId(SHR_ENCOUNTER_ID);
        composition.getEncounterReference().setValue(encounter);
        composition.getPatientReference().setValue(patient);
        Reference resourceReference = new Reference().setReference("urn:uuid:83504ff9-96bf-46b7-838a-7d042004b4ff");
        deathNoteResource = bundleContext.getResourceForReference(resourceReference);
    }

    @After
    public void tearDown() throws Exception {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }

    @Test
    public void canHandleDeathNoteResource() throws Exception {
        assertTrue(deathNoteHandler.canHandle(deathNoteResource));
    }

    @Test
    public void shouldSavePatientAndEncounterIdForDeathNoteObservation() {
        deathNoteHandler.process(deathNoteResource, composition);
        PatientDeathDetails patientDeathDetails = findByEncounterId("shrEncounterId");
        assertEquals(PATIENT_HID, patientDeathDetails.getPatient().getHid());
        assertEquals(SHR_ENCOUNTER_ID, patientDeathDetails.getEncounter().getEncounterId());
        assertNotNull(patientDeathDetails.getUuid());
    }

    @Test
    public void shouldSaveDateOfDeathFromDateOfDeathObservationResource() {
        deathNoteHandler.process(deathNoteResource, composition);
        PatientDeathDetails patientDeathDetails = findByEncounterId("shrEncounterId");
        assertEquals(DateUtil.parseDate("2015-11-02T12:12:00.000+05:30"), patientDeathDetails.getDateOfDeath());
    }

    @Test
    public void shouldSaveCircumstancesOfDeathObservationResource() {
        deathNoteHandler.process(deathNoteResource, composition);
        PatientDeathDetails patientDeathDetails = findByEncounterId("shrEncounterId");
        assertEquals("63238001", patientDeathDetails.getCircumstancesOfDeathCode());
    }

    @Test
    public void shouldSaveCauseOfDeath() {
        deathNoteHandler.process(deathNoteResource, composition);
        PatientDeathDetails patientDeathDetails = findByEncounterId("shrEncounterId");
        assertEquals("A00", patientDeathDetails.getCauseOfDeathCode());
        assertEquals("c4060895-798e-11e5-b243-0050568276cf", patientDeathDetails.getCauseOfDeathConceptUuid());
    }

    private PatientDeathDetails findByEncounterId(String shrEncounterId) {
        String sql = "select patient_hid, encounter_id, date_of_death, " +
                "circumstance_concept_uuid, circumstance_code, cause_concept_uuid, cause_code, uuid, pod_concept_uuid, pod_code " +
                "from patient_death_details where encounter_id= :encounter_id";
        HashMap<String, Object> map = new HashMap<>();
        map.put("encounter_id", shrEncounterId);
        List<PatientDeathDetails> patientDeathDetailsList = jdbcTemplate.query(sql, map, new RowMapper<PatientDeathDetails>() {
            @Override
            public PatientDeathDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
                PatientDeathDetails patientDeathDetails = new PatientDeathDetails();
                Patient patient = new Patient();
                patient.setHid(rs.getString("patient_hid"));
                patientDeathDetails.setPatient(patient);

                Encounter encounter = new Encounter();
                encounter.setEncounterId(rs.getString("encounter_id"));
                patientDeathDetails.setEncounter(encounter);

                patientDeathDetails.setDateOfDeath(rs.getTimestamp("date_of_death"));
                patientDeathDetails.setCircumstancesOfDeathUuid(rs.getString("circumstance_concept_uuid"));
                patientDeathDetails.setCircumstancesOfDeathCode(rs.getString("circumstance_code"));
                patientDeathDetails.setCauseOfDeathConceptUuid(rs.getString("cause_concept_uuid"));
                patientDeathDetails.setCauseOfDeathCode(rs.getString("cause_code"));
                patientDeathDetails.setPlaceOfDeathUuid(rs.getString("pod_concept_uuid"));
                patientDeathDetails.setPlaceOfDeathCode(rs.getString("pod_code"));
                patientDeathDetails.setUuid(rs.getString("uuid"));
                return patientDeathDetails;
            }
        });
        assertFalse(patientDeathDetailsList.isEmpty());
        assertEquals(1, patientDeathDetailsList.size());

        return patientDeathDetailsList.get(0);
    }
}