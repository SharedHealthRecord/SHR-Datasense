package org.sharedhealth.datasense.handler;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.hl7.fhir.instance.formats.ParserBase;
import org.hl7.fhir.instance.model.Immunization;
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

    private final String TR_DRUG_UUID = "28c3c784-c0bf-4cae-bd26-ca76a384085a";
    @Autowired
    private ImmunizationResourceHandler immunizationResourceHandler;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private BundleContext bundleContext;
    private Resource immunizationResource;

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
        ParserBase.ResourceOrFeed resourceOrFeed = loadFromXmlFile("xmls/encounterWithImmunization.xml");
        String shrEncounterId = "shrEncounterId";
        bundleContext = new BundleContext(resourceOrFeed.getFeed(), shrEncounterId);
        EncounterComposition composition = bundleContext.getEncounterCompositions().get(0);
        Patient patient = new Patient();
        patient.setHid("5960610240356417537");
        composition.getPatientReference().setValue(patient);
        Encounter encounter = new Encounter();
        encounter.setEncounterId(shrEncounterId);
        encounter.setEncounterDateTime(DateUtil.parseDate("2015-01-14T15:04:57+05:30"));
        composition.getEncounterReference().setValue(encounter);
        ResourceReference resourceReference = new ResourceReference().setReferenceSimple("urn:9b6bd490-f9d5-4d8f-9d08-ac0083ff9d35");
        immunizationResource = bundleContext.getResourceByReferenceFromFeed(resourceReference);
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
        Medication medication = getMedication();
        assertEquals(DateUtil.parseDate("2015-01-06T11:00:00+05:30"), medication.getDateTime());
        assertEquals("shrEncounterId", medication.getEncounter().getEncounterId());
        assertEquals("5960610240356417537", medication.getPatient().getHid());
        List<ImmunizationReason> reasons = findImmunizationReasonsByEncounterId("shrEncounterId");
        assertTrue(reasons.size() > 0);
    }

    @Test
    public void shouldSaveEncounterDateTimeIfImmunizationDateNotGiven() throws Exception {
        ((Immunization) immunizationResource).setDate(null);
        immunizationResourceHandler.process(immunizationResource, bundleContext.getEncounterCompositions().get(0));
        Medication medication = getMedication();
        assertEquals(DateUtil.parseDate("2015-01-14T15:04:57+05:30"), medication.getDateTime());
    }

    @Test
    public void shouldSaveImmunizationStatusAndUuid() throws Exception {
        immunizationResourceHandler.process(immunizationResource, bundleContext.getEncounterCompositions().get(0));
        Medication medication = getMedication();
        assertEquals("IA", medication.getStatus().getValue());
        assertNotNull(medication.getUuid());
    }

    @Test
    public void shouldSaveDrugId() throws Exception {
        immunizationResourceHandler.process(immunizationResource, bundleContext.getEncounterCompositions().get(0));
        Medication medication = getMedication();
        assertEquals(TR_DRUG_UUID, medication.getDrugId());
    }

    @Test
    public void shouldNotSaveNonCodedImmunization() throws Exception {
        ((Immunization) immunizationResource).getVaccineType().getCoding().get(0).setSystemSimple(null);
        immunizationResourceHandler.process(immunizationResource, bundleContext.getEncounterCompositions().get(0));
        List<Medication> medications = findByEncounterId(bundleContext.getShrEncounterId());
        assertTrue(medications.isEmpty());
    }

    private Medication getMedication() {
        List<Medication> medications = findByEncounterId(bundleContext.getShrEncounterId());
        assertFalse(medications.isEmpty());
        assertEquals(1, medications.size());
        return medications.get(0);
    }

    @After
    public void tearDown() throws Exception {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }

    private List<Medication> findByEncounterId(String shrEncounterId) {
        String sql = "select datetime, encounter_id, patient_hid, status, drug_id, uuid from " +
                "medication where encounter_id= :encounter_id";
        return jdbcTemplate.query(sql, Collections.singletonMap("encounter_id", shrEncounterId), new
                RowMapper<Medication>() {
                    @Override
                    public Medication mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Medication medication = new Medication();

                        Date medicationDatetime = new Date(rs.getTimestamp("datetime").getTime());
                        medication.setDateTime(medicationDatetime);

                        Encounter encounter = new Encounter();
                        encounter.setEncounterId(rs.getString("encounter_id"));
                        medication.setEncounter(encounter);

                        Patient patient = new Patient();
                        patient.setHid(rs.getString("patient_hid"));
                        String status = rs.getString("status");
                        medication.setStatus(MedicationStatus.getMedicationStatus(status));
                        medication.setPatient(patient);
                        medication.setDrugId(rs.getString("drug_id"));
                        medication.setStatus(MedicationStatus.getMedicationStatus(rs.getString("status")));
                        medication.setUuid(rs.getString("uuid"));
                        return medication;
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
