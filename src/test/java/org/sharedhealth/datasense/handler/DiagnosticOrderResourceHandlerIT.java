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
import org.sharedhealth.datasense.model.DiagnosticOrder;
import org.sharedhealth.datasense.model.Encounter;
import org.sharedhealth.datasense.model.Patient;
import org.sharedhealth.datasense.model.fhir.BundleContext;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class DiagnosticOrderResourceHandlerIT extends BaseIntegrationTest {
    @Autowired
    private DiagnosticOrderResourceHandler diagnosticOrderResourceHandler;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private EncounterComposition composition;
    private IResource diagnosticOrder;

    private static final String PATIENT_HID = "98001046534";
    private static final String SHR_ENCOUNTER_ID = "shrEncounterId";

    @Before
    public void setUp() throws Exception {
        super.loadConfigParameters();
        Bundle bundle = loadFromXmlFile("dstu2/xmls/p98001046534_encounter_with_diagnostic_order_requested.xml");
        BundleContext bundleContext = new BundleContext(bundle, SHR_ENCOUNTER_ID);
        composition = bundleContext.getEncounterCompositions().get(0);
        Patient patient = new Patient();
        patient.setDateOfBirth(new SimpleDateFormat("yyyy-MM-dd").parse("2013-12-28"));
        patient.setHid(PATIENT_HID);
        Encounter encounter = new Encounter();
        encounter.setEncounterId(SHR_ENCOUNTER_ID);
        composition.getEncounterReference().setValue(encounter);
        composition.getPatientReference().setValue(patient);
        ResourceReferenceDt resourceReference = new ResourceReferenceDt().setReference("urn:uuid:e8436e26-a011-48e7-a4e8-a41465dfae34");
        diagnosticOrder = bundleContext.getResourceForReference(resourceReference);
    }

    @After
    public void tearDown() throws Exception {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }

    @Test
    public void canHandleDeathNoteResource() throws Exception {
        assertTrue(diagnosticOrderResourceHandler.canHandle(diagnosticOrder));
    }

    @Test
    public void shouldSavePatientAndEncounterIdForDiagnosticOrder() {
        diagnosticOrderResourceHandler.process(diagnosticOrder, composition);
        DiagnosticOrder savedDiagnosticOrder = findByEncounterId("shrEncounterId");
        assertEquals(PATIENT_HID, savedDiagnosticOrder.getPatientHid());
        assertEquals(SHR_ENCOUNTER_ID, savedDiagnosticOrder.getEncounterId());
        assertEquals("RAD", savedDiagnosticOrder.getOrderCategory());
        assertEquals("requested", savedDiagnosticOrder.getOrderStatus());
        assertEquals("http://172.18.46.199:8084/api/1.0/providers/24.json",savedDiagnosticOrder.getOrderer());
        assertNotNull(savedDiagnosticOrder.getUuid());
    }

    private DiagnosticOrder findByEncounterId(String shrEncounterId) {
        String sql = "select patient_hid,encounter_id,order_datetime,order_category,order_code,orderer," +
                "order_concept,order_status from diagnostic_order where encounter_id= :encounter_id";
        HashMap<String, Object> map = new HashMap<>();
        map.put("encounter_id", shrEncounterId);
        return jdbcTemplate.query(sql, map, new RowMapper<DiagnosticOrder>() {
            @Override
            public DiagnosticOrder mapRow(ResultSet rs, int rowNum) throws SQLException {
                DiagnosticOrder order = new DiagnosticOrder();
                order.setPatientHid(rs.getString("patient_hid"));
                order.setEncounterId(rs.getString("encounter_id"));
                order.setOrderCategory(rs.getString("order_category"));
                order.setOrderCode(rs.getString("order_code"));
                order.setOrderer(rs.getString("orderer"));
                order.setOrderConcept(rs.getString("order_concept"));
                order.setOrderStatus(rs.getString("order_status"));
                return order;
            }
        }).get(0);
    }
}