package org.sharedhealth.datasense.handler;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.hl7.fhir.instance.formats.ParserBase;
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
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static junit.framework.Assert.*;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;
import static org.sharedhealth.datasense.util.ResourceLookup.getDatasenseResourceReference;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class DiagnosisProcessorIT {
    @Autowired
    DiagnosisResourceHandler processor;

    @Autowired
    DiagnosisDao diagnosisDao;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8081);

    @Before
    public void setUp() throws Exception {
        processor = new DiagnosisResourceHandler(diagnosisDao);
    }

    @After
    public void tearDown() {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }

    @Test
    public void shouldSaveDiagnosis() throws Exception {
        ParserBase.ResourceOrFeed resourceOrFeed = loadFromXmlFile("xmls/sampleEncounter.xml");
        String shrEncounterId = "shrEncounterId";
        BundleContext context = new BundleContext(resourceOrFeed.getFeed(), shrEncounterId);
        EncounterComposition composition = context.getEncounterCompositions().get(0);
        Encounter encounter = new Encounter();
        encounter.setEncounterId(shrEncounterId);
        composition.getEncounterReference().setValue(encounter);
        Patient patient = new Patient();
        String hid = "5942395046400622593";
        patient.setHid(hid);
        composition.getPatientReference().setValue(patient);
        ResourceReference resourceReference = new ResourceReference();
        resourceReference.setReferenceSimple("urn:2801e2b9-3886-4bf5-919f-ce9268fdc317");
        processor.process(getDatasenseResourceReference(resourceReference, composition), composition);
        List<Diagnosis> diagnosises = diagnosisDao.findByEncounterId(shrEncounterId);
        assertEquals(1, diagnosises.size());
        Diagnosis diagnosis = diagnosises.get(0);
        assertNotNull(diagnosis.getUuid());
        assertEquals(shrEncounterId, diagnosis.getEncounter().getEncounterId());
        assertEquals("J19.513891", diagnosis.getDiagnosisCode());
        assertEquals("12722059-401d-4ef1-83c7-ebc3fb32bf80", diagnosis.getDiagnosisConcept());
        assertEquals("confirmed", diagnosis.getDiagnosisStatus());
        assertTrue(DateUtil.parseDate("2014-12-09T10:59:28+05:30").equals(diagnosis.getDiagnosisDateTime()));
        assertEquals(hid, diagnosis.getPatient().getHid());
    }
}