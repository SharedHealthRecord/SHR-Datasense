package org.sharedhealth.datasense.handler;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.BaseIntegrationTest;
import org.sharedhealth.datasense.helpers.DatabaseHelper;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.sharedhealth.datasense.model.Encounter;
import org.sharedhealth.datasense.model.Patient;
import org.sharedhealth.datasense.model.PrescribedDrug;
import org.sharedhealth.datasense.model.fhir.BundleContext;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;


@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class PrescribedDrugResourceHandlerIT extends BaseIntegrationTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    @Autowired
    private PrescribedDrugResourceHandler prescribedDrugResourceHandler;

    private EncounterComposition composition;
    private IResource resource;

    private static final String SHR_ENCOUNTER_ID = "shrEncounterId";
    private static final String PATIENT_HID = "98001046534";

    @Before
    public void setUp() throws Exception {
        super.loadConfigParameters();
    }

    @After
    public void tearDown() throws Exception {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }

    private void setUpData(String fileName, String resourceUuid, String shrEncounterId) throws IOException, ParseException {
        Bundle bundle = loadFromXmlFile(fileName);
        BundleContext bundleContext = new BundleContext(bundle, shrEncounterId);
        composition = bundleContext.getEncounterCompositions().get(0);
        Patient patient = new Patient();
        patient.setDateOfBirth(new SimpleDateFormat("yyyy-MM-dd").parse("2013-12-28"));
        patient.setHid(PATIENT_HID);
        Encounter encounter = new Encounter();
        encounter.setEncounterId(shrEncounterId);
        composition.getEncounterReference().setValue(encounter);
        composition.getPatientReference().setValue(patient);
        ResourceReferenceDt resourceReference = new ResourceReferenceDt().setReference(resourceUuid);
        resource = bundleContext.getResourceForReference(resourceReference);

    }

    @Test
    public void canHandleMedicationOrder() throws Exception {
        setUpData("dstu2/xmls/p98001046534_encounter_with_presciption.xml", "urn:uuid:97711fe6-d025-4324-96a0-a480a49bb893", SHR_ENCOUNTER_ID);
        assertTrue(prescribedDrugResourceHandler.canHandle(resource));
    }


    @Test
    public void shouldNotHandleDiagnosticOrderResource() throws Exception {
        setUpData("dstu2/xmls/p98001046534_encounter_with_diagnostic_order_requested.xml", "urn:uuid:e8436e26-a011-48e7-a4e8-a41465dfae34", SHR_ENCOUNTER_ID);
        assertFalse(prescribedDrugResourceHandler.canHandle(resource));
    }

    @Test
    public void shouldProcessMedicationOrderWithTRDrug() throws Exception {
        setUpData("dstu2/xmls/p98001046534_encounter_with_presciption.xml", "urn:uuid:97711fe6-d025-4324-96a0-a480a49bb893", SHR_ENCOUNTER_ID);
        prescribedDrugResourceHandler.process(resource, composition);

        List<PrescribedDrug> byEncounterId = findByEncounterId(SHR_ENCOUNTER_ID);
        assertEquals(1, byEncounterId.size());


        PrescribedDrug prescribedDrug = byEncounterId.get(0);
        Date expectedDate = DateUtil.parseDate("23/06/2016", DateUtil.DATE_FMT_DD_MM_YYYY);
        String expectedDrugUuid = "cd89844d-8211-11e5-aa01-0050568276cf";
        String expectedDrugName = "Paracetamol Tablet 500 mg";
        String expectedPrescriberId = "20";
        String expectedStatus = "NEW";
        String expectedShrMedicationOrderUuid = "97711fe6-d025-4324-96a0-a480a49bb893";

        assertPrescription(prescribedDrug, SHR_ENCOUNTER_ID, expectedDate, expectedDrugUuid, expectedDrugName,
                expectedPrescriberId, expectedStatus, expectedShrMedicationOrderUuid, null);
    }

    @Test
    public void shouldProcessMedicationOrderWithPriorPrescription() throws Exception {
        //create new prescribed drug
        setUpData("dstu2/xmls/p98001046534_encounter_with_new_presciption.xml", "urn:uuid:acaccc8b-a011-488d-bea3-d2c88a0b07c1", SHR_ENCOUNTER_ID);
        prescribedDrugResourceHandler.process(resource, composition);

        List<PrescribedDrug> byEncounterId = findByEncounterId(SHR_ENCOUNTER_ID);
        assertEquals(1, byEncounterId.size());

        //discontinuing prescribed drug

        String discontinuingEncounterId = "DiscontinuingEncounterId";
        setUpData("dstu2/xmls/p98001046534_encounter_with_discontinued_presciption.xml", "urn:uuid:2af6380b-466f-447a-9bfa-b1d5f816e09c", discontinuingEncounterId);
        prescribedDrugResourceHandler.process(resource, composition);

        List<PrescribedDrug> byEncounterIdForDiscontinuing = findByEncounterId(discontinuingEncounterId);
        assertEquals(1, byEncounterIdForDiscontinuing.size());
        PrescribedDrug prescribedDrug = byEncounterIdForDiscontinuing.get(0);
        Date expectedDate = DateUtil.parseDate("24/06/2016", DateUtil.DATE_FMT_DD_MM_YYYY);
        String expectedDrugUuid = "cd74d25f-8211-11e5-aa01-0050568276cf";
        String expectedDrugName = "Diazepam Tablet 5 mg";
        String expectedPrescriberId = "20";
        String expectedStatus = "DISCONTINUE";
        String expectedShrMedicationOrderUuid = "2af6380b-466f-447a-9bfa-b1d5f816e09c";
        String expectedPriorShrMedicationUuid = "acaccc8b-a011-488d-bea3-d2c88a0b07c1";

        assertPrescription(prescribedDrug, discontinuingEncounterId, expectedDate,expectedDrugUuid, expectedDrugName,
                expectedPrescriberId,expectedStatus,expectedShrMedicationOrderUuid, expectedPriorShrMedicationUuid);



    }

    @Test
    public void shouldProcessMedicationOrderWithLocalDrug() throws Exception {
        setUpData("dstu2/xmls/p98001046534_encounter_with_presciption.xml", "urn:uuid:2d161c05-64be-4de4-a488-7ed499d2ccca", SHR_ENCOUNTER_ID);
        prescribedDrugResourceHandler.process(resource, composition);

        List<PrescribedDrug> byEncounterId = findByEncounterId(SHR_ENCOUNTER_ID);
        assertEquals(1, byEncounterId.size());

        PrescribedDrug prescribedDrug = byEncounterId.get(0);
        Date expectedDate = DateUtil.parseDate("24/06/2016", DateUtil.DATE_FMT_DD_MM_YYYY);
        String expectedDrugName = "Asthalin";
        String expectedPrescriberId = "20";
        String expectedStatus = "NEW";
        String expectedShrMedicationOrderUuid = "2d161c05-64be-4de4-a488-7ed499d2ccca";

        assertPrescription(prescribedDrug, SHR_ENCOUNTER_ID, expectedDate, null, expectedDrugName,
                expectedPrescriberId, expectedStatus, expectedShrMedicationOrderUuid, null);
    }

    @Test
    public void shouldDeleteExistingPrescribedDrug() throws Exception {
        setUpData("dstu2/xmls/p98001046534_encounter_with_presciption.xml", "urn:uuid:97711fe6-d025-4324-96a0-a480a49bb893", SHR_ENCOUNTER_ID);
        prescribedDrugResourceHandler.process(resource, composition);
        List<PrescribedDrug> byEncounterId = findByEncounterId(SHR_ENCOUNTER_ID);

        assertEquals(1, byEncounterId.size());

        prescribedDrugResourceHandler.deleteExisting(composition);
        byEncounterId = findByEncounterId(SHR_ENCOUNTER_ID);
        assertEquals(0, byEncounterId.size());

    }

    private void assertPrescription(PrescribedDrug prescribedDrug, String shrEncounterId, Date expectedDate, String expectedDrugUuid, String expectedDrugName, String expectedPrescriber, String expectedStatus, String expectedShrMedicationOrderUuid, String expectedPriorShrMedicationOrderUuid) throws ParseException {

        assertEquals(PATIENT_HID, prescribedDrug.getPatientHid());
        assertEquals(shrEncounterId, prescribedDrug.getEncounterId());
        assertEquals(expectedDate, prescribedDrug.getPrescriptionDateTime());
        assertEquals(expectedDrugUuid, prescribedDrug.getDrugUuid());
        assertEquals(expectedDrugName, prescribedDrug.getDrugName());
        assertEquals(expectedPrescriber, prescribedDrug.getPrescriber());
        assertEquals(expectedStatus, prescribedDrug.getStatus());
        assertEquals(expectedShrMedicationOrderUuid, prescribedDrug.getShrMedicationOrderUuid());
        assertEquals(expectedPriorShrMedicationOrderUuid, prescribedDrug.getPriorShrMedicationOrderUuid());

        assertNotNull(prescribedDrug.getUuid());
    }

    private List<PrescribedDrug> findByEncounterId(String shrEncounterId) {
        String sql = "select patient_hid, encounter_id, prescription_datetime, drug_uuid, drug_name, prescriber, status, shr_medication_order_uuid, prior_shr_medication_order_uuid, uuid from prescribed_drugs where encounter_id= :encounter_id";
        HashMap<String, Object> map = new HashMap<>();
        map.put("encounter_id", shrEncounterId);
        return jdbcTemplate.query(sql, map, new RowMapper<PrescribedDrug>() {
            @Override
            public PrescribedDrug mapRow(ResultSet rs, int rowNum) throws SQLException {

                PrescribedDrug prescribedDrug = new PrescribedDrug();
                prescribedDrug.setPatientHid(rs.getString("patient_hid"));
                prescribedDrug.setEncounterId(rs.getString("encounter_id"));
                prescribedDrug.setPrescriptionDateTime(rs.getDate("prescription_datetime"));
                prescribedDrug.setDrugUuid(rs.getString("drug_uuid"));
                prescribedDrug.setDrugName(rs.getString("drug_name"));
                prescribedDrug.setPrescriber(rs.getString("prescriber"));
                prescribedDrug.setStatus(rs.getString("status"));
                prescribedDrug.setShrMedicationOrderUuid(rs.getString("shr_medication_order_uuid"));
                prescribedDrug.setPriorShrMedicationOrderUuid(rs.getString("prior_shr_medication_order_uuid"));
                prescribedDrug.setUuid(rs.getString("uuid"));

                return prescribedDrug;
            }
        });
    }
}