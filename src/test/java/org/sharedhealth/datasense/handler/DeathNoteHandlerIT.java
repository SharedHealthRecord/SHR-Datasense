package org.sharedhealth.datasense.handler;

import org.hl7.fhir.instance.formats.ParserBase;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceReference;
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
    private Resource deathNoteResource;

    private static final String PATIENT_HID = "patientHid";
    private static final String SHR_ENCOUNTER_ID = "shrEncounterId";

    @Before
    public void setUp() throws Exception {
        ParserBase.ResourceOrFeed resourceOrFeed = loadFromXmlFile("xmls/encounterWithDeathNote.xml");
        BundleContext bundleContext = new BundleContext(resourceOrFeed.getFeed(), SHR_ENCOUNTER_ID);
        composition = bundleContext.getEncounterCompositions().get(0);
        Patient patient = new Patient();
        patient.setDateOfBirth(new SimpleDateFormat("yyyy-MM-dd").parse("2013-12-28"));
        patient.setHid(PATIENT_HID);
        Encounter encounter = new Encounter();
        encounter.setEncounterId(SHR_ENCOUNTER_ID);
        composition.getEncounterReference().setValue(encounter);
        composition.getPatientReference().setValue(patient);
        ResourceReference resourceReference = new ResourceReference().setReferenceSimple("urn:9d3f2b4e-2f83-4d60-930c-5a7cfafbcaf2");
        deathNoteResource = bundleContext.getResourceByReferenceFromFeed(resourceReference);
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
        assertEquals(DateUtil.parseDate("2014-12-28T00:00:00+05:30"), patientDeathDetails.getDateOfDeath());
    }

    @Test
    public void shouldSavePatientAgeFromDateOfDeathObservationResource() {
        deathNoteHandler.process(deathNoteResource, composition);
        PatientDeathDetails patientDeathDetails = findByEncounterId("shrEncounterId");
        assertEquals(1, patientDeathDetails.getPatientAgeInYears());
        assertEquals(12, patientDeathDetails.getPatientAgeInMonths());
        assertEquals(365, patientDeathDetails.getPatientAgeInDays());
    }

    @Test
    public void shouldSaveCircumstancesOfDeathObservationResource() {
        deathNoteHandler.process(deathNoteResource, composition);
        PatientDeathDetails patientDeathDetails = findByEncounterId("shrEncounterId");
        assertEquals("killed.", patientDeathDetails.getCircumstancesOfDeath());
    }

    @Test
    public void shouldSaveCauseOfDeath() {
        deathNoteHandler.process(deathNoteResource, composition);
        PatientDeathDetails patientDeathDetails = findByEncounterId("shrEncounterId");
        assertEquals("J19", patientDeathDetails.getCauseOfDeathCode());
        assertEquals("03d43153-4d18-4195-a033-6a32792a00e7", patientDeathDetails.getCauseOfDeathConceptUuid());
    }

    private PatientDeathDetails findByEncounterId(String shrEncounterId) {
        String sql = "select patient_hid, encounter_id, date_of_death, patient_age_years, patient_age_months, patient_age_days, " +
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
                patientDeathDetails.setPatientAgeInYears(rs.getInt("patient_age_years"));
                patientDeathDetails.setPatientAgeInMonths(rs.getInt("patient_age_months"));
                patientDeathDetails.setPatientAgeInDays(rs.getInt("patient_age_days"));
                patientDeathDetails.setCircumstancesOfDeath(rs.getString("circumstances_of_death"));
                patientDeathDetails.setCauseOfDeathConceptUuid(rs.getString("cause_concept_uuid"));
                patientDeathDetails.setCauseOfDeathCode(rs.getString("cause_code"));
                patientDeathDetails.setUuid(rs.getString("uuid"));
                return patientDeathDetails;
            }
        });
        return patientDeathDetailsList.isEmpty() ? null : patientDeathDetailsList.get(0);
    }
}