package org.sharedhealth.datasense.handler;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
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
import org.sharedhealth.datasense.model.*;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;
import static org.sharedhealth.datasense.helpers.ResourceHelper.asString;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class ImmunizationResourceHandlerIT {

    public static final String HEALTH_ID = "98001046534";
    private final String TR_DRUG_UUID = "28c3c784-c0bf-4cae-bd26-ca76a384085a";
    @Autowired
    private ImmunizationResourceHandler immunizationResourceHandler;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private BundleContext bundleContext;
    private IResource immunizationResource;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Before
    public void setUp() throws Exception {
        givenThat(get(urlEqualTo("/openmrs/ws/rest/v1/tr/drugs/" + TR_DRUG_UUID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/M" + TR_DRUG_UUID + ".json"))));
        loadBundleContext();
    }

    private void loadBundleContext() throws IOException {
        Bundle bundle = loadFromXmlFile("dstu2/xmls/p98001046534_encounter_with_immunization.xml");
        String shrEncounterId = "shrEncounterId";
        bundleContext = new BundleContext(bundle, shrEncounterId);
        EncounterComposition composition = bundleContext.getEncounterCompositions().get(0);
        Patient patient = new Patient();
        patient.setHid(HEALTH_ID);
        composition.getPatientReference().setValue(patient);
        Encounter encounter = new Encounter();
        encounter.setEncounterId(shrEncounterId);
        encounter.setEncounterDateTime(DateUtil.parseDate("2015-01-14T15:04:57+05:30"));
        composition.getEncounterReference().setValue(encounter);
        ResourceReferenceDt resourceReference = new ResourceReferenceDt().setReference("urn:uuid:554e13d9-25f9-4802-8f21-669249bf51be");
        immunizationResource = bundleContext.getResourceForReference(resourceReference);
    }

    @Test
    public void shouldHandleImmunizationResources() throws Exception {
        ImmunizationResourceHandler immunizationResourceHandler = new ImmunizationResourceHandler();
        assertTrue(immunizationResourceHandler.canHandle(immunizationResource));
    }

    @Test
    public void shouldSaveImmunizationDateTimeAndEncounterAndPatient() throws Exception {
        immunizationResourceHandler.process(immunizationResource,
                bundleContext.getEncounterCompositions().get(0));
        Immunization immunization = getImmunization();
        assertEquals(DateUtil.parseDate("2015-09-03T00:00:00.000+05:30"), immunization.getDateTime());
        assertEquals("shrEncounterId", immunization.getEncounter().getEncounterId());
        assertEquals(HEALTH_ID, immunization.getPatient().getHid());
        List<ImmunizationReason> reasons = findImmunizationReasonsByEncounterId("shrEncounterId");
        assertTrue(reasons.size() > 0);
    }

    @Test
    public void shouldSaveEncounterDateTimeIfImmunizationDateNotGiven() throws Exception {
        ((ca.uhn.fhir.model.dstu2.resource.Immunization) immunizationResource).setDate(null);
        immunizationResourceHandler.process(immunizationResource, bundleContext.getEncounterCompositions().get(0));
        Immunization immunization = getImmunization();
        assertEquals(DateUtil.parseDate("2015-01-14T15:04:57+05:30"), immunization.getDateTime());
    }

    @Test
    public void shouldSaveImmunizationStatusAndUuid() throws Exception {
        immunizationResourceHandler.process(immunizationResource, bundleContext.getEncounterCompositions().get(0));
        Immunization immunization = getImmunization();
        assertEquals("IA", immunization.getStatus().getValue());
        assertNotNull(immunization.getUuid());
    }

    @Test
    public void shouldSaveDrugId() throws Exception {
        immunizationResourceHandler.process(immunizationResource, bundleContext.getEncounterCompositions().get(0));
        Immunization immunization = getImmunization();
        assertEquals(TR_DRUG_UUID, immunization.getDrugId());
    }

    @Test
    public void shouldNotSaveNonCodedImmunization() throws Exception {
        CodeableConceptDt vaccineType = ((ca.uhn.fhir.model.dstu2.resource.Immunization) immunizationResource).getVaccineCode();
        CodingDt codingDt = vaccineType.getCoding().get(0);
        codingDt.setSystem((String) null);
        immunizationResourceHandler.process(immunizationResource, bundleContext.getEncounterCompositions().get(0));
        List<Immunization> immunizations = findImmunizationsFor(bundleContext.getShrEncounterId());
        assertTrue(immunizations.isEmpty());
    }

    private Immunization getImmunization() {
        List<Immunization> immunizations = findImmunizationsFor(bundleContext.getShrEncounterId());
        assertFalse(immunizations.isEmpty());
        assertEquals(1, immunizations.size());
        return immunizations.get(0);
    }

    @After
    public void tearDown() throws Exception {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }

    private List<Immunization> findImmunizationsFor(String shrEncounterId) {
        String sql = "select datetime, encounter_id, patient_hid, status, drug_id, uuid from " +
                "immunizations where encounter_id= :encounter_id";
        return jdbcTemplate.query(sql, Collections.singletonMap("encounter_id", shrEncounterId), new
                RowMapper<Immunization>() {
                    @Override
                    public Immunization mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Immunization immunization = new Immunization();

                        Date medicationDatetime = new Date(rs.getTimestamp("datetime").getTime());
                        immunization.setDateTime(medicationDatetime);

                        Encounter encounter = new Encounter();
                        encounter.setEncounterId(rs.getString("encounter_id"));
                        immunization.setEncounter(encounter);

                        Patient patient = new Patient();
                        patient.setHid(rs.getString("patient_hid"));
                        String status = rs.getString("status");
                        immunization.setStatus(MedicationStatus.getMedicationStatus(status));
                        immunization.setPatient(patient);
                        immunization.setDrugId(rs.getString("drug_id"));
                        immunization.setStatus(MedicationStatus.getMedicationStatus(rs.getString("status")));
                        immunization.setUuid(rs.getString("uuid"));
                        return immunization;
                    }
                });
    }

    private List<ImmunizationReason> findImmunizationReasonsByEncounterId(String shrEncounterId) {
        String sql = "select patient_hid, encounter_id, descr, code, uuid, incident_uuid from immunization_reason where encounter_id= :encounter_id";
        return jdbcTemplate.query(sql, Collections.singletonMap("encounter_id", shrEncounterId), new
                RowMapper<ImmunizationReason>() {
                    @Override
                    public ImmunizationReason mapRow(ResultSet rs, int rowNum) throws SQLException {
                        ImmunizationReason reason = new ImmunizationReason();
                        reason.setHid(rs.getString("patient_hid"));
                        reason.setEncounterId(rs.getString("encounter_id"));
                        reason.setDescr(rs.getString("descr"));
                        reason.setCode(rs.getString("code"));
                        reason.setUuid(rs.getString("uuid"));
                        reason.setIncidentUuid(rs.getString("incident_uuid"));
                        return reason;
                    }
                });
    }
}
