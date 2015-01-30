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
import org.sharedhealth.datasense.repository.ObservationDao;
import org.sharedhealth.datasense.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class ObservationResourceHandlerIT {
    private final String DATE_OF_DEATH_REFERENCE_ID = "urn:dcd89f72-8e07-4b6d-af52-8575c1b9c72b";
    private final String SHR_ENCOUNTER_ID = "shrEncounterId";
    private final String HEALTH_ID = "5957279291845640193";
    private final String DATE_OF_DEATH_CONCEPT_ID = "a6e20fe1-4044-4ce7-8440-577f7f814765";
    private final String DEATH_NOTE_REFERENCE_ID = "urn:9d3f2b4e-2f83-4d60-930c-5a7cfafbcaf2";
    private final String DEATH_NOTE_CONCEPT_ID = "22473b09-9dfb-47ce-b117-32a10e5b9a5f";
    @Autowired
    private ObservationDao observationDao;
    @Autowired
    private ObservationResourceHandler observationResourceHandler;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    private BundleContext bundleContext;

    @Before
    public void setUp() throws Exception {
        ParserBase.ResourceOrFeed resourceOrFeed = loadFromXmlFile("xmls/encounterWithDeathNote.xml");
        bundleContext = new BundleContext(resourceOrFeed.getFeed(), SHR_ENCOUNTER_ID);
        EncounterComposition composition = bundleContext.getEncounterCompositions().get(0);
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
    public void shouldHandleFHIRObservations() throws Exception {
        ResourceReference deathReference = new ResourceReference().setReferenceSimple(DEATH_NOTE_REFERENCE_ID);
        org.hl7.fhir.instance.model.Observation fhirObservation = (org.hl7.fhir.instance.model.Observation) bundleContext.getResourceByReferenceFromFeed(deathReference);
        assertTrue(observationResourceHandler.canHandle(fhirObservation));
    }

    @Test
    public void shouldSaveObservationWithoutRelatedComponents() throws Exception {
        ResourceReference dateOfDeathReference = new ResourceReference().setReferenceSimple(DATE_OF_DEATH_REFERENCE_ID);
        EncounterComposition composition = bundleContext.getEncounterCompositions().get(0);
        observationResourceHandler.process(bundleContext.getResourceByReferenceFromFeed(dateOfDeathReference),
                composition);
        List<Observation> observations = observationDao.findByEncounterId(bundleContext.getShrEncounterId());
        assertFalse(observations.isEmpty());
        assertEquals(1, observations.size());
        Observation observation = observations.get(0);
        assertEquals("2014-12-28 00:00:00", observation.getValue());
        assertEquals(DateUtil.parseDate("2015-01-20T11:10:53+05:30"), observation.getDateTime());
        assertEquals(SHR_ENCOUNTER_ID, observation.getEncounter().getEncounterId());
        assertEquals(HEALTH_ID, observation.getPatient().getHid());
        assertNotNull(observation.getUuid());
        assertEquals(DATE_OF_DEATH_CONCEPT_ID, observation.getConceptId());
        assertNull(observation.getReferenceCode());
    }

    @Test
    public void shouldSaveObservationAlongWithRelatedObservations() throws Exception {
        ResourceReference deathNoteReference = new ResourceReference().setReferenceSimple(DEATH_NOTE_REFERENCE_ID);
        EncounterComposition composition = bundleContext.getEncounterCompositions().get(0);
        observationResourceHandler.process(bundleContext.getResourceByReferenceFromFeed(deathNoteReference),
                composition);
        List<Observation> observations = observationDao.findByEncounterId(bundleContext.getShrEncounterId());
        assertFalse(observations.isEmpty());
        assertEquals(2, observations.size());


        String dateOfDeathConceptId = DATE_OF_DEATH_CONCEPT_ID;
        Observation dateOfDeathObservation = findObservationByConceptId(observations, dateOfDeathConceptId);

        String deathNoteConceptId = DEATH_NOTE_CONCEPT_ID;
        Observation deathNoteObservation = findObservationByConceptId(observations, deathNoteConceptId);
        assertNull(deathNoteObservation.getParentId());

        assertEquals(deathNoteObservation.getUuid(), dateOfDeathObservation.getParentId());
    }

    private Observation findObservationByConceptId(List<Observation> observations, final String conceptId) {
        for (Observation observation : observations) {
            if (observation.getConceptId().equals(conceptId)) {
                return observation;
            }
        }
        return null;
    }
}
