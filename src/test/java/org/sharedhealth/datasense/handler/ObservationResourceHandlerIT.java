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

import static junit.framework.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class ObservationResourceHandlerIT {
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
        String shrEncounterId = "shrEncounterId";
        bundleContext = new BundleContext(resourceOrFeed.getFeed(), shrEncounterId);
        EncounterComposition composition = bundleContext.getEncounterCompositions().get(0);
        Patient patient = new Patient();
        patient.setHid("5957279291845640193");
        composition.getPatientReference().setValue(patient);
        Encounter encounter = new Encounter();
        encounter.setEncounterId(shrEncounterId);
        encounter.setEncounterDateTime(DateUtil.parseDate("2015-01-20T11:10:53+05:30"));
        composition.getEncounterReference().setValue(encounter);

    }

    @After
    public void tearDown() throws Exception {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }

    @Test
    public void shouldHandleFHIRObservations() throws Exception {
        ResourceReference deathReference = new ResourceReference().setReferenceSimple("urn:9d3f2b4e-2f83-4d60-930c-5a7cfafbcaf2");
        org.hl7.fhir.instance.model.Observation fhirObservation = (org.hl7.fhir.instance.model.Observation) bundleContext.getResourceByReference(deathReference);
        assertTrue(observationResourceHandler.canHandle(fhirObservation));
    }

    @Test
    public void shouldSaveDeathNoteObservation() throws Exception {
        ResourceReference deathNoteReference = new ResourceReference().setReferenceSimple("urn:9d3f2b4e-2f83-4d60-930c-5a7cfafbcaf2");
        org.hl7.fhir.instance.model.Observation fhirObservation = (org.hl7.fhir.instance.model.Observation) bundleContext.getResourceByReference(deathNoteReference);
        observationResourceHandler.process(fhirObservation, bundleContext.getEncounterCompositions().get(0));
        List<Observation> observations = observationDao.findByEncounterId(bundleContext.getShrEncounterId());
        assertFalse(observations.isEmpty());
        assertEquals(1, observations.size());
        Observation observation = observations.get(0);
        assertEquals(DateUtil.parseDate("2015-01-20T11:10:53+05:30"), observation.getDateTime());
        assertEquals("shrEncounterId", observation.getEncounter().getEncounterId());
        assertEquals("5957279291845640193", observation.getPatient().getHid());
        assertNotNull(observation.getUuid());
        assertEquals("22473b09-9dfb-47ce-b117-32a10e5b9a5f", observation.getConceptId());
        assertEquals("419620001", observation.getReferenceCode());
        assertNull(observation.getValue());
        assertNull(observation.getParentId());
    }

    @Test
    public void shouldSaveDateOfDeathObservation() throws Exception {
        ResourceReference deathNoteReference = new ResourceReference().setReferenceSimple("urn:dcd89f72-8e07-4b6d-af52-8575c1b9c72b");
        org.hl7.fhir.instance.model.Observation fhirObservation = (org.hl7.fhir.instance.model.Observation) bundleContext.getResourceByReference(deathNoteReference);
        observationResourceHandler.process(fhirObservation, bundleContext.getEncounterCompositions().get(0));
        List<Observation> observations = observationDao.findByEncounterId(bundleContext.getShrEncounterId());
        assertFalse(observations.isEmpty());
        assertEquals(1, observations.size());
        Observation observation = observations.get(0);
        assertEquals(DateUtil.parseDate("2014-12-28T00:00:00+05:30").toString(), observation.getValue());
    }
}
