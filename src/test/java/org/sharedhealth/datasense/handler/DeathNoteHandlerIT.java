package org.sharedhealth.datasense.handler;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public class DeathNoteHandlerIT {

    @Autowired
    private DeathNoteHandler deathNoteHandler;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private EncounterComposition composition;
    private IResource deathNoteResource;

    private static final String PATIENT_HID = "98001046534";
    private static final String SHR_ENCOUNTER_ID = "shrEncounterId";

    @Before
    public void setUp() throws Exception {
        Bundle bundle = loadFromXmlFile("dstu2/xmls/p98001046534_encounter_with_deathNote.xml");
        BundleContext bundleContext = new BundleContext(bundle, SHR_ENCOUNTER_ID);
        composition = bundleContext.getEncounterCompositions().get(0);
        Patient patient = new Patient();
        patient.setDateOfBirth(new SimpleDateFormat("yyyy-MM-dd").parse("2013-12-28"));
        patient.setHid(PATIENT_HID);
        Encounter encounter = new Encounter();
        encounter.setEncounterId(SHR_ENCOUNTER_ID);
        composition.getEncounterReference().setValue(encounter);
        composition.getPatientReference().setValue(patient);
        ResourceReferenceDt resourceReference = new ResourceReferenceDt().setReference("urn:uuid:7e53fe65-c5b8-49e1-8248-eecb35e5e87c");
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
        assertEquals(DateUtil.parseDate("2015-09-04 02:02:00"), patientDeathDetails.getDateOfDeath());
    }

    @Test
    public void shouldSaveCircumstancesOfDeathObservationResource() {
        deathNoteHandler.process(deathNoteResource, composition);
        PatientDeathDetails patientDeathDetails = findByEncounterId("shrEncounterId");
        assertEquals("died in hospital", patientDeathDetails.getCircumstancesOfDeath());
    }

    @Test
    public void shouldSaveCauseOfDeath() {
        deathNoteHandler.process(deathNoteResource, composition);
        PatientDeathDetails patientDeathDetails = findByEncounterId("shrEncounterId");
        assertEquals("A90", patientDeathDetails.getCauseOfDeathCode());
        assertEquals("07952dc2-5206-11e5-ae6d-0050568225ca", patientDeathDetails.getCauseOfDeathConceptUuid());
    }

    private PatientDeathDetails findByEncounterId(String shrEncounterId) {
        String sql = "select patient_hid, encounter_id, date_of_death, " +
                "circumstances_of_death, cause_concept_uuid, cause_code, uuid " +
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
                patientDeathDetails.setCircumstancesOfDeath(rs.getString("circumstances_of_death"));
                patientDeathDetails.setCauseOfDeathConceptUuid(rs.getString("cause_concept_uuid"));
                patientDeathDetails.setCauseOfDeathCode(rs.getString("cause_code"));
                patientDeathDetails.setUuid(rs.getString("uuid"));
                return patientDeathDetails;
            }
        });
        assertFalse(patientDeathDetailsList.isEmpty());
        assertEquals(1, patientDeathDetailsList.size());

        return patientDeathDetailsList.get(0);
    }
}