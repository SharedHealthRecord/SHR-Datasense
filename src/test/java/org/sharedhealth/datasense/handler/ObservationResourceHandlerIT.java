package org.sharedhealth.datasense.handler;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.Type;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.BaseIntegrationTest;
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

import static org.junit.Assert.*;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class ObservationResourceHandlerIT extends BaseIntegrationTest {
    private final String SHR_ENCOUNTER_ID = "shrEncounterId";
    private final String HEALTH_ID = "98001046534";
    private final String VITALS_RESOURCE_REFERENCE = "urn:uuid:a4708fe7-43c5-4b32-86ec-76924cf1f0e1";
    private final String PULSE_RESOURCE_REFERENCE = "urn:uuid:f95dcc54-702b-47b7-ad37-fbff3dcbf336";
    @Autowired
    private ObservationResourceHandler observationResourceHandler;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    private BundleContext bundleContext;

    @Before
    public void setUp() throws Exception {
        Bundle bundle = loadFromXmlFile("stu3/p98001046534_encounter_with_vitals.xml");
        bundleContext = new BundleContext(bundle, SHR_ENCOUNTER_ID);
        setEncounterReference(bundleContext, HEALTH_ID, SHR_ENCOUNTER_ID);
    }

    @After
    public void tearDown() throws Exception {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }

    @Test
    public void shouldHandleTSObservations() throws Exception {
        Bundle bundle = loadFromXmlFile("stu3/p98001046534_encounter_with_vitals_with_somelocalconcepts.xml");
        bundleContext = new BundleContext(bundle, SHR_ENCOUNTER_ID);
        setEncounterReference(bundleContext, HEALTH_ID, SHR_ENCOUNTER_ID);
        String topLevelVitalsObs = "urn:uuid:a4708fe7-43c5-4b32-86ec-76924cf1f0e1";
        Reference obsReference = new Reference().setReference(topLevelVitalsObs);
        EncounterComposition composition = bundleContext.getEncounterCompositions().get(0);
        Resource obsResource = bundleContext.getResourceForReference(obsReference);
        observationResourceHandler.process(obsResource, composition);
        List<Observation> observations = findObsByEncounterId(bundleContext.getShrEncounterId());
        assertEquals(4, observations.size()); //should find Systolic, Diastolic, Pulse and Temperature
    }


    @Test
    public void shouldNotHandleDeathNoteObservations() throws Exception {
        Bundle bundle = loadFromXmlFile("stu3/p98001046534_encounter_with_deathNote.xml");
        BundleContext deathNoteBundleContext = new BundleContext(bundle, SHR_ENCOUNTER_ID);
        String deathNoteObsResId = "urn:uuid:7e53fe65-c5b8-49e1-8248-eecb35e5e87c";
        Reference deathReference = new Reference().setReference(deathNoteObsResId);
        org.hl7.fhir.dstu3.model.Observation fhirObservation = (org.hl7.fhir.dstu3.model.Observation) deathNoteBundleContext.getResourceForReference(deathReference);
        assertFalse(observationResourceHandler.canHandle(fhirObservation));
    }

    @Test
    public void shouldHandleVitalsObservations() throws Exception {
        Reference vitalsReference = new Reference().setReference(VITALS_RESOURCE_REFERENCE);
        org.hl7.fhir.dstu3.model.Observation fhirObservation = (org.hl7.fhir.dstu3.model.Observation) bundleContext.getResourceForReference(vitalsReference);
        assertTrue(observationResourceHandler.canHandle(fhirObservation));
    }

    @Test
    public void shouldSaveSimpleObservation() throws Exception {
        Reference pulseResourceReference = new Reference().setReference(PULSE_RESOURCE_REFERENCE);
        EncounterComposition composition = bundleContext.getEncounterCompositions().get(0);
        observationResourceHandler.process(bundleContext.getResourceForReference(pulseResourceReference), composition);
        List<Observation> observations = findObsByEncounterId(bundleContext.getShrEncounterId());
        assertFalse(observations.isEmpty());
        assertEquals(1, observations.size());
        Observation observation = observations.get(0);
        assertEquals("70.0", observation.getValue());
        assertEquals(DateUtil.parseDate("2015-01-20T11:10:53+05:30"), observation.getDateTime());
        assertEquals(SHR_ENCOUNTER_ID, observation.getEncounter().getEncounterId());
        assertEquals(HEALTH_ID, observation.getPatient().getHid());
        assertNotNull(observation.getUuid());
        assertEquals("07b4c2f5-5206-11e5-ae6d-0050568225ca", observation.getConceptId());
        assertEquals("78564009", observation.getReferenceCode());
    }

    @Test
    public void shouldSaveNestedObservationAlongWithRelatedObservations() throws Exception {
        Reference vitalReference = new Reference().setReference(VITALS_RESOURCE_REFERENCE);
        EncounterComposition composition = bundleContext.getEncounterCompositions().get(0);
        observationResourceHandler.process(bundleContext.getResourceForReference(vitalReference),
                composition);
        List<Observation> observations = findObsByEncounterId(bundleContext.getShrEncounterId());
        assertFalse(observations.isEmpty());
        assertEquals(5, observations.size());

        Observation systolicObservation = findObservationByConceptId(observations, "07b01ede-5206-11e5-ae6d-0050568225ca");
        Observation pulseObservation = findObservationByConceptId(observations, "07b4c2f5-5206-11e5-ae6d-0050568225ca");
        Observation vitalsObservation = findObservationByConceptId(observations, "XYZ38225-5206-11e5-ae6d-005056822123");

        assertEquals(vitalsObservation.getUuid(), pulseObservation.getParentId());
        assertNotNull(systolicObservation.getUuid());
    }

    @Test
    public void shouldSaveObservationIfParentObsIsNonCoded() throws Exception {
        String bloodPressureObsRef = "urn:uuid:cb144010-c38c-4fcc-975e-ab831ab991ad";
        Reference bpReference = new Reference().setReference(bloodPressureObsRef);
        EncounterComposition composition = bundleContext.getEncounterCompositions().get(0);
        observationResourceHandler.process(bundleContext.getResourceForReference(bpReference),
                composition);
        List<Observation> observations = findObsByEncounterId(bundleContext.getShrEncounterId());
        assertFalse(observations.isEmpty());
        assertEquals(2, observations.size());

        Observation systolicBpObs = findObservationByConceptId(observations, "07b01ede-5206-11e5-ae6d-0050568225ca");
        Observation diastolicBpObs = findObservationByConceptId(observations, "07b25cc6-5206-11e5-ae6d-0050568225ca");

        assertNull(systolicBpObs.getParentId());
        assertNull(diastolicBpObs.getParentId());
    }

    @Test
    public void shouldGetValueBooleanFromObservation() throws Exception {
        Bundle bundle = loadFromXmlFile("stu3/p98001046534_encounter_with_observations.xml");
        bundleContext = new BundleContext(bundle, SHR_ENCOUNTER_ID);
        setEncounterReference(bundleContext, HEALTH_ID, SHR_ENCOUNTER_ID);
        String obsRef4PncWithin48HoursAfterBirth = "urn:uuid:68b6c450-4dec-41d3-aa05-464c2bdaeb98";
        //String obsRef4PncWithin48HoursAfterBirth = "PNC Within 48 Hours After Birth";
        Reference obsReference = new Reference().setReference(obsRef4PncWithin48HoursAfterBirth);
        EncounterComposition composition = bundleContext.getEncounterCompositions().get(0);
        Resource obsResource = bundleContext.getResourceForReference(obsReference);
        Type obsValue = ((org.hl7.fhir.dstu3.model.Observation) obsResource).getValue();
        assertTrue(obsValue instanceof org.hl7.fhir.dstu3.model.CodeableConcept);
        observationResourceHandler.process(obsResource, composition);
        List<Observation> observations = findObsByEncounterId(bundleContext.getShrEncounterId());
        assertEquals(1, observations.size());
        assertEquals("true", observations.get(0).getValue());


    }

    private void setEncounterReference(BundleContext bundleContext, String healthId, String encounterId) {
        EncounterComposition composition = bundleContext.getEncounterCompositions().get(0);
        Patient patient = new Patient();
        patient.setHid(healthId);
        composition.getPatientReference().setValue(patient);
        Encounter encounter = new Encounter();
        encounter.setEncounterId(encounterId);
        encounter.setEncounterDateTime(DateUtil.parseDate("2015-01-20T11:10:53+05:30"));
        composition.getEncounterReference().setValue(encounter);
    }

    private Observation findObservationByConceptId(List<Observation> observations, final String conceptId) {
        for (Observation observation : observations) {
            if (observation.getConceptId().equals(conceptId)) {
                return observation;
            }
        }
        return null;
    }

    private List<Observation> findObsByEncounterId(String shrEncounterId) {
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
