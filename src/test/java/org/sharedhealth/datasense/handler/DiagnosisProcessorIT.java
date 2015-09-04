package org.sharedhealth.datasense.handler;

import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.*;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;

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
    public WireMockRule wireMockRule = new WireMockRule(9997);

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
        Bundle bundle = loadFromXmlFile("xmls/sampleEncounter.xml");
        String shrEncounterId = "shrEncounterId";
        BundleContext context = new BundleContext(bundle, shrEncounterId);
        EncounterComposition composition = context.getEncounterCompositions().get(0);
        Encounter encounter = new Encounter();
        encounter.setEncounterId(shrEncounterId);
        composition.getEncounterReference().setValue(encounter);
        Patient patient = new Patient();
        String hid = "5942395046400622593";
        patient.setHid(hid);
        composition.getPatientReference().setValue(patient);
        ResourceReferenceDt resourceReference = new ResourceReferenceDt();
        resourceReference.setReference("urn:uuid:2801e2b9-3886-4bf5-919f-ce9268fdc317");
        processor.process(context.getResourceForReference(resourceReference), composition);
        List<Diagnosis> diagnosises = findByEncounterId(shrEncounterId);
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

    private List<Diagnosis> findByEncounterId(String encounterId) {
        return jdbcTemplate.query(
                "select diagnosis_id, patient_hid, encounter_id, diagnosis_datetime, diagnosis_code, " +
                        "diagnosis_concept_id, " +
                        "diagnosis_status, uuid from diagnosis where encounter_id= :encounter_id", Collections
                        .singletonMap("encounter_id", encounterId),
                new RowMapper<Diagnosis>() {
                    @Override
                    public Diagnosis mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Diagnosis diagnosis = new Diagnosis();
                        diagnosis.setDiagnosisId(rs.getInt("diagnosis_id"));
                        diagnosis.setDiagnosisDateTime(new java.util.Date(rs.getTimestamp("diagnosis_datetime")
                                .getTime()));
                        diagnosis.setDiagnosisCode(rs.getString("diagnosis_code"));
                        diagnosis.setDiagnosisConceptId(rs.getString("diagnosis_concept_id"));
                        diagnosis.setDiagnosisStatus(rs.getString("diagnosis_status"));
                        diagnosis.setUuid(rs.getString("uuid"));

                        Encounter encounter = new Encounter();
                        encounter.setEncounterId(rs.getString("encounter_id"));
                        diagnosis.setEncounter(encounter);

                        Patient patient = new Patient();
                        patient.setHid(rs.getString("patient_hid"));
                        diagnosis.setPatient(patient);
                        return diagnosis;
                    }
                });
    }
}