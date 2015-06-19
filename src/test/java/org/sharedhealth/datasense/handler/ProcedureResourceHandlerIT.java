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
import org.sharedhealth.datasense.model.Procedure;
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
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class ProcedureResourceHandlerIT {

    @Autowired
    private ProcedureResourceHandler procedureResourceHandler;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private EncounterComposition composition;
    private Resource procedureResource;

    private static final String SHR_ENCOUNTER_ID = "shrEncounterId";
    private static final String PATIENT_HID = "patientHid";

    @Before
    public void setUp() throws Exception {
        ParserBase.ResourceOrFeed resourceOrFeed = loadFromXmlFile("xmls/encounterWithProcedure.xml");
        BundleContext bundleContext = new BundleContext(resourceOrFeed.getFeed(), SHR_ENCOUNTER_ID);
        composition = bundleContext.getEncounterCompositions().get(0);

        Patient patient = new Patient();
        patient.setHid(PATIENT_HID);
        Encounter encounter = new Encounter();
        encounter.setEncounterId(SHR_ENCOUNTER_ID);
        encounter.setEncounterDateTime(DateUtil.parseDate("2015-02-02T00:00:00+05:30"));
        composition.getEncounterReference().setValue(encounter);
        composition.getPatientReference().setValue(patient);

        ResourceReference resourceReference = new ResourceReference().setReferenceSimple("urn:9f582d08-1245-4c5a-a4b0-8d021298ac61");
        procedureResource = bundleContext.getResourceByReferenceFromFeed(resourceReference);

    }

    @After
    public void tearDown() throws Exception {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }

    @Test
    public void shouldHandleProcedureResources() throws Exception {
        assertTrue(procedureResourceHandler.canHandle(procedureResource));
    }

    @Test
    public void shouldProcessProcedures() throws Exception {
        procedureResourceHandler.process(procedureResource, composition);
        List<Procedure> procedures = findByEncounterId(SHR_ENCOUNTER_ID);
        assertEquals(1, procedures.size());
        Procedure procedure = procedures.get(0);
        assertEquals(PATIENT_HID, procedure.getPatientHid());
        shouldProcessProcedureDates(procedure);
        shouldProcessProcedureType(procedure);
        shouldProcessDiagnosis(procedure);
    }

    private void shouldProcessProcedureDates(Procedure procedure) {
        assertEquals(DateUtil.parseDate("2015-02-02T00:00:00+05:30"), procedure.getEncounterDate());
        assertEquals(DateUtil.parseDate("2015-02-03T00:00:00+05:30"), procedure.getStartDate());
        assertEquals(DateUtil.parseDate("2015-02-04T00:00:00+05:30"), procedure.getEndDate());
    }


    private void shouldProcessProcedureType(Procedure procedure) throws Exception {
        assertEquals("b1b46bb3-c956-4e1c-84df-12f03f33b7f5", procedure.getProcedureUuid());
        assertEquals("7W00X0Z", procedure.getProcedureCode());
    }

    private void shouldProcessDiagnosis(Procedure procedure) throws Exception {
        assertEquals("maleria", procedure.getDiagnosisCode());
        assertEquals("e8816d09-9735-4f08-ad05-471c1f7b4b88", procedure.getDiagnosisUuid());
    }


    private List<Procedure> findByEncounterId(String shrEncounterId) {
        String sql = "select patient_hid, encounter_id, datetime, start_date, end_date, procedure_uuid, procedure_code, " +
                "diagnosis_uuid, diagnosis_code from procedures where encounter_id = :encounter_id";
        HashMap<String, Object> map = new HashMap<>();
        map.put("encounter_id", shrEncounterId);
        List<Procedure> procedures = jdbcTemplate.query(sql, map, new RowMapper<Procedure>() {
            @Override
            public Procedure mapRow(ResultSet rs, int rowNum) throws SQLException {
                Procedure procedure = new Procedure();

                procedure.setPatientHid(rs.getString("patient_hid"));
                procedure.setEncounterId(rs.getString("encounter_id"));
                procedure.setEncounterDate(rs.getTimestamp("datetime"));
                procedure.setStartDate(rs.getTimestamp("start_date"));
                procedure.setEndDate(rs.getTimestamp("end_date"));
                procedure.setProcedureUuid(rs.getString("procedure_uuid"));
                procedure.setProcedureCode(rs.getString("procedure_code"));
                procedure.setDiagnosisUuid(rs.getString("diagnosis_uuid"));
                procedure.setDiagnosisCode(rs.getString("diagnosis_code"));
                return procedure;
            }
        });
        return procedures.isEmpty() ? null : procedures;
    }
}