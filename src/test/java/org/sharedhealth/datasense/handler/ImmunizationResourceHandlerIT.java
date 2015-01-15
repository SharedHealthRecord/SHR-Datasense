package org.sharedhealth.datasense.handler;

import org.hl7.fhir.instance.formats.ResourceOrFeed;
import org.hl7.fhir.instance.model.Immunization;
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
import org.sharedhealth.datasense.model.Medication;
import org.sharedhealth.datasense.model.fhir.BundleContext;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.repository.MedicationDao;
import org.sharedhealth.datasense.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class ImmunizationResourceHandlerIT {

    @Autowired
    private MedicationDao medicationDao;
    @Autowired
    private ImmunizationResourceHandler immunizationResourceHandler;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private BundleContext bundleContext;
    private Resource immunizationResource;

    @Before
    public void setUp() throws Exception {
        ResourceOrFeed resourceOrFeed = loadFromXmlFile("xmls/encounterWithImmunization.xml");
        String shrEncounterId = "shrEncounterId";
        bundleContext = new BundleContext(resourceOrFeed.getFeed(), shrEncounterId);
        EncounterComposition composition = bundleContext.getEncounterCompositions().get(0);
        Encounter encounter = new Encounter();
        encounter.setEncounterId(shrEncounterId);
        encounter.setEncounterDateTime(DateUtil.parseDate("2015-01-14T15:04:57+05:30"));
        composition.getEncounterReference().setValue(encounter);
        immunizationResource = bundleContext.getResourceByReference(new ResourceReference().setReferenceSimple("urn:9b6bd490-f9d5-4d8f-9d08-ac0083ff9d35"));
    }

    @Test
    public void shouldHandleImmunizationResources() throws Exception {
        ImmunizationResourceHandler immunizationResourceHandler = new ImmunizationResourceHandler();
        assertTrue(immunizationResourceHandler.canHandle(immunizationResource));
    }

    @Test
    public void shouldSaveImmunizationDateTimeAndEncounter() throws Exception {
        immunizationResourceHandler.process(immunizationResource, bundleContext.getEncounterCompositions().get(0));
        Medication medication = getMedication();
        assertEquals(DateUtil.parseDate("2015-01-06T11:00:00+05:30"), medication.getDateTime());
        assertEquals("shrEncounterId", medication.getEncounter().getEncounterId());
    }

    @Test
    public void shouldSaveEncounterDateTimeIfImmunizationDateNotGiven() throws Exception {
        Immunization immunization = ((Immunization) immunizationResource).setDate(null);
        immunizationResourceHandler.process(immunization, bundleContext.getEncounterCompositions().get(0));
        Medication medication = getMedication();
        assertEquals(DateUtil.parseDate("2015-01-14T15:04:57+05:30"), medication.getDateTime());
    }

    private Medication getMedication() {
        List<Medication> medications = medicationDao.findByEncounterId(bundleContext.getShrEncounterId());
        assertFalse(medications.isEmpty());
        return medications.get(0);
    }

    @Test
    public void shouldSaveImmunizationStatus() throws Exception {
        immunizationResourceHandler.process(immunizationResource, bundleContext.getEncounterCompositions().get(0));
        Medication medication = getMedication();
        assertEquals("A", medication.getStatus().getValue());
    }

    @After
    public void tearDown() throws Exception {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }
}