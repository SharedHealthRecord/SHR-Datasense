package org.sharedhealth.datasense.handler;

import org.hl7.fhir.instance.formats.ParserBase;
import org.hl7.fhir.instance.model.ResourceReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.helpers.DatabaseHelper;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.sharedhealth.datasense.model.Encounter;
import org.sharedhealth.datasense.model.Observation;
import org.sharedhealth.datasense.model.Patient;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.*;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class ObservationResourceHandlerIT {
    private final String SHR_ENCOUNTER_ID = "shrEncounterId";
    private final String HEALTH_ID = "5960610240356417537";
    private final String VITALS_RESOURCE_REFERENCE = "urn:10b4fdd0-0507-4063-8259-ee50fff7ad6e";
    private final String PULSE_RESOURCE_REFERENCE = "urn:2bfe946c-95cd-4543-8122-ec8619e56296";
    @Autowired
    private ObservationResourceHandler observationResourceHandler;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    private BundleContext vitalsBundleContext;

    @Before
    public void setUp() throws Exception {
        ParserBase.ResourceOrFeed resourceOrFeed = loadFromXmlFile("xmls/encounterWithVitalsObservation.xml");
        vitalsBundleContext = new BundleContext(resourceOrFeed.getFeed(), SHR_ENCOUNTER_ID);
        EncounterComposition composition = vitalsBundleContext.getEncounterCompositions().get(0);
        Patient patient = new Patient();
        patient.setHid(HEALTH_ID);
        composition.getPatientReference().setValue(patient);
        Encounter encounter = new Encounter();
        encounter.setEncounterId(SHR_ENCOUNTER_ID);
        encounter.setEncounterDateTime(DateUtil.parseDate("2015-01-20T11:10:53+05:30"));
        composition.getEncounterReference().setValue(encounter);
    }

    @After
    public void tearDown() throws Exception {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }

    @Test
    public void shouldNotHandleDeathNoteObservations() throws Exception {
        ParserBase.ResourceOrFeed resourceOrFeed = loadFromXmlFile("xmls/encounterWithDeathNote.xml");
        BundleContext deathNoteBundleContext = new BundleContext(resourceOrFeed.getFeed(), SHR_ENCOUNTER_ID);
        ResourceReference deathReference = new ResourceReference().setReferenceSimple("urn:9d3f2b4e-2f83-4d60-930c-5a7cfafbcaf2");
        org.hl7.fhir.instance.model.Observation fhirObservation = (org.hl7.fhir.instance.model.Observation) deathNoteBundleContext.getResourceByReferenceFromFeed(deathReference);
        assertFalse(observationResourceHandler.canHandle(fhirObservation));
    }

    @Test
    public void shouldHandleFHIRObservations() throws Exception {
        ResourceReference vitalsReference = new ResourceReference().setReferenceSimple(VITALS_RESOURCE_REFERENCE);
        org.hl7.fhir.instance.model.Observation fhirObservation = (org.hl7.fhir.instance.model.Observation) vitalsBundleContext.getResourceByReferenceFromFeed(vitalsReference);
        assertTrue(observationResourceHandler.canHandle(fhirObservation));
    }

    @Test
    public void shouldSaveObservationWithoutRelatedComponents() throws Exception {
        ResourceReference dateOfDeathReference = new ResourceReference().setReferenceSimple(PULSE_RESOURCE_REFERENCE);
        EncounterComposition composition = vitalsBundleContext.getEncounterCompositions().get(0);
        observationResourceHandler.process(vitalsBundleContext.getResourceByReferenceFromFeed(dateOfDeathReference), composition);
        List<Observation> observations = findByEncounterId(vitalsBundleContext.getShrEncounterId());
        assertFalse(observations.isEmpty());
        assertEquals(1, observations.size());
        Observation observation = observations.get(0);
        assertEquals("72.0", observation.getValue());
        assertEquals(DateUtil.parseDate("2015-01-20T11:10:53+05:30"), observation.getDateTime());
        assertEquals(SHR_ENCOUNTER_ID, observation.getEncounter().getEncounterId());
        assertEquals(HEALTH_ID, observation.getPatient().getHid());
        assertNotNull(observation.getUuid());
        assertEquals("22a952b6-cc36-45e8-8b52-ff5a90fa7c4f", observation.getConceptId());
        assertNull(observation.getReferenceCode());
    }

    @Test
    public void shouldSaveObservationAlongWithRelatedObservations() throws Exception {
        ResourceReference deathNoteReference = new ResourceReference().setReferenceSimple(VITALS_RESOURCE_REFERENCE);
        EncounterComposition composition = vitalsBundleContext.getEncounterCompositions().get(0);
        observationResourceHandler.process(vitalsBundleContext.getResourceByReferenceFromFeed(deathNoteReference),
                composition);
        List<Observation> observations = findByEncounterId(vitalsBundleContext.getShrEncounterId());
        assertFalse(observations.isEmpty());
        assertEquals(4, observations.size());

        Observation bloodPressureObservation = findObservationByConceptId(observations, "e69ef886-6914-4ed7-93a8-7b951dbf7139");
        Observation diastolicObservation = findObservationByConceptId(observations, "af747d2f-8946-4ca2-93ec-5eb76986aff8");
        Observation pulseObservation = findObservationByConceptId(observations, "22a952b6-cc36-45e8-8b52-ff5a90fa7c4f");
        Observation vitalsObservation = findObservationByConceptId(observations, "44c245dd-d234-4991-a8b2-3c4a54d5092b");

        assertEquals(vitalsObservation.getUuid(), bloodPressureObservation.getParentId());
        assertEquals(vitalsObservation.getUuid(), pulseObservation.getParentId());
        assertEquals(bloodPressureObservation.getUuid(), diastolicObservation.getParentId());
    }

    private Observation findObservationByConceptId(List<Observation> observations, final String conceptId) {
        for (Observation observation : observations) {
            if (observation.getConceptId().equals(conceptId)) {
                return observation;
            }
        }
        return null;
    }

    private List<Observation> findByEncounterId(String shrEncounterId) {
        String sql = "select observation_id, patient_hid, encounter_id, concept_id, code, datetime, parent_id, value," +
                " uuid from observation where encounter_id= :encounter_id";
        return jdbcTemplate.query(sql, Collections.singletonMap("encounter_id", shrEncounterId), new
                RowMapper<Observation>() {

                    @Override
                    public Observation mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Observation observation = new Observation();
                        observation.setObservationId(rs.getInt("observation_id"));

                        Encounter encounter = new Encounter();
                        encounter.setEncounterId(rs.getString("encounter_id"));
                        observation.setEncounter(encounter);

                        Patient patient = new Patient();
                        patient.setHid(rs.getString("patient_hid"));
                        observation.setPatient(patient);

                        observation.setConceptId(rs.getString("concept_id"));
                        observation.setReferenceCode(rs.getString("code"));
                        observation.setDatetime(new Date(rs.getTimestamp("datetime").getTime()));
                        observation.setParentId(rs.getString("parent_id"));
                        observation.setValue(rs.getString("value"));
                        observation.setUuid(rs.getString("uuid"));
                        return observation;
                    }
                });
    }


}
