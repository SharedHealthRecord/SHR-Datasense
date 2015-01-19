package org.sharedhealth.datasense.processor;

import org.hl7.fhir.instance.formats.ResourceOrFeed;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.helpers.DatabaseHelper;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.sharedhealth.datasense.model.Encounter;
import org.sharedhealth.datasense.model.Facility;
import org.sharedhealth.datasense.model.Patient;
import org.sharedhealth.datasense.model.fhir.BundleContext;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.repository.EncounterDao;
import org.sharedhealth.datasense.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class ClinicalEncounterProcessorIT {
    @Autowired
    private EncounterDao encounterDao;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    private ClinicalEncounterProcessor processor;

    @Before
    public void setUp() throws Exception {
        processor = new ClinicalEncounterProcessor(null, encounterDao);
    }

    @After
    public void tearDown() {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }

    @Test
    public void shouldSaveEncounter() throws Exception {
        ResourceOrFeed resourceOrFeed = loadFromXmlFile("xmls/sampleEncounter.xml");
        String shrEncounterId = "shrEncounterId";
        BundleContext context = new BundleContext(resourceOrFeed.getFeed(), shrEncounterId);
        EncounterComposition composition = context.getEncounterCompositions().get(0);
        Patient patient = new Patient();
        Date dob = new SimpleDateFormat("yyyy-MM-dd").parse("1999-10-22");
        patient.setDateOfBirth(dob);
        String hid = "5942395046400622593";
        patient.setHid(hid);
        composition.getPatientReference().setValue(patient);
        String facilityId = "10000059";
        String facilityLocation = "3013";
        Facility facility = new Facility();
        facility.setFacilityId(facilityId);
        facility.setFacilityLocationCode(facilityLocation);
        composition.getServiceProviderReference().setValue(facility);
        processor.process(composition);
        Encounter encounter = encounterDao.findEncounterById(shrEncounterId);
        assertNotNull(encounter);
        assertEquals(hid, encounter.getPatient().getHid());
        assertEquals(facilityId, encounter.getFacility().getFacilityId());
        assertEquals(facilityLocation, encounter.getLocationCode());
        assertEquals(DateUtil.parseDate("2014-12-09T10:59:27.000+0530"), encounter.getEncounterDateTime());
        assertEquals("Consultation", encounter.getEncounterType());
        assertEquals("outpatient", encounter.getEncounterVisitType());
    }
}