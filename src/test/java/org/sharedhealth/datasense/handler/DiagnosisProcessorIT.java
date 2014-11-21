package org.sharedhealth.datasense.handler;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.hl7.fhir.instance.formats.ResourceOrFeed;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.helpers.DatabaseHelper;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.sharedhealth.datasense.model.Diagnosis;
import org.sharedhealth.datasense.model.Encounter;
import org.sharedhealth.datasense.model.Patient;
import org.sharedhealth.datasense.model.fhir.BundleContext;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.repository.DiagnosisDao;
import org.sharedhealth.datasense.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class DiagnosisProcessorIT {
    @Autowired
    DiagnosisProcessor processor;

    @Autowired
    DiagnosisDao diagnosisDao;

    @Autowired
    JdbcTemplate jdbcTemplate;
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8081);

    @Before
    public void setUp() throws Exception {
        processor = new DiagnosisProcessor(diagnosisDao);
    }

    @After
    public void tearDown() {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }

    @Test
    public void shouldSaveDiagnosis() throws Exception {
        ResourceOrFeed resourceOrFeed = loadFromXmlFile("xmls/sampleEncounter.xml");
        String shrEncounterId = "shrEncounterId";
        BundleContext context = new BundleContext(resourceOrFeed.getFeed(), shrEncounterId);
        EncounterComposition composition = context.getEncounterCompositions().get(0);
        Encounter encounter = new Encounter();
        encounter.setEncounterId(shrEncounterId);
        composition.getEncounterReference().setValue(encounter);
        Patient patient = new Patient();
        String hid = "5927558688825933825";
        patient.setHid(hid);
        composition.getPatientReference().setValue(patient);
        ResourceReference resourceReference = new ResourceReference();
        resourceReference.setReferenceSimple("c74f59f4-81f0-4d6c-b550-ac4f98587b6d");
        Resource resource = context.getResourceByReference(resourceReference);
        processor.process(resource, composition);
        List<Diagnosis> diagnosises = diagnosisDao.findByEncounterId(shrEncounterId);
        assertEquals(1, diagnosises.size());
        Diagnosis diagnosis = diagnosises.get(0);
        assertEquals(shrEncounterId, diagnosis.getEncounter().getEncounterId());
        assertEquals("J19.513891", diagnosis.getDiagnosisCode());
        assertEquals("12722059-401d-4ef1-83c7-ebc3fb32bf80", diagnosis.getDiagnosisConcept());
        assertEquals("provisional", diagnosis.getDiagnosisStatus());
        assertTrue(DateUtil.parseDate("2014-10-29T12:28:41+05:30").equals(diagnosis.getDiagnosisDateTime()));
        assertEquals(hid, diagnosis.getPatient().getHid());
    }
}