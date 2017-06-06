package org.sharedhealth.datasense.processor;

import org.hl7.fhir.dstu3.model.Bundle;
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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
        Bundle bundle = loadFromXmlFile("dstu2/xmls/p98001046534_encounter_with_registration.xml");
        String shrEncounterId = "shrEncounterId";
        BundleContext context = new BundleContext(bundle, shrEncounterId);
        EncounterComposition composition = context.getEncounterCompositions().get(0);
        Patient patient = new Patient();
        Date dob = new SimpleDateFormat("yyyy-MM-dd").parse("1999-10-22");
        patient.setDateOfBirth(dob);
        String hid = "98001046534";
        patient.setHid(hid);
        composition.getPatientReference().setValue(patient);
        String facilityId = "10019841";
        String facilityLocation = "3013";
        Facility facility = new Facility();
        facility.setFacilityId(facilityId);
        facility.setFacilityLocationCode(facilityLocation);
        composition.getServiceProviderReference().setValue(facility);
        processor.process(composition);
        Encounter encounter = findEncounterById(shrEncounterId);
        assertNotNull(encounter);
        assertEquals(hid, encounter.getPatient().getHid());
        assertEquals(facilityId, encounter.getFacility().getFacilityId());
        assertEquals(facilityLocation, encounter.getLocationCode());
        assertEquals(DateUtil.parseDate("2015-09-04T12:34:46.000+05:30"), encounter.getEncounterDateTime());
        assertEquals("REG", encounter.getEncounterType());
        assertEquals("outpatient", encounter.getEncounterVisitType());
    }

    private Encounter findEncounterById(String encounterId) {
        List<Encounter> encounters = jdbcTemplate.query(
                "select encounter_id ,encounter_datetime, encounter_type, visit_type, patient_hid, " +
                        "encounter_location_id, facility_id " +
                        "from encounter where encounter_id= :encounter_id", Collections.singletonMap("encounter_id",
                        encounterId),
                new RowMapper<Encounter>() {
                    @Override
                    public Encounter mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Encounter encounter = new Encounter();
                        encounter.setEncounterId(rs.getString("encounter_id"));
                        encounter.setEncounterDateTime(new Date(rs.getTimestamp("encounter_datetime").getTime()));
                        encounter.setEncounterType(rs.getString("encounter_type"));
                        encounter.setEncounterVisitType(rs.getString("visit_type"));
                        encounter.setLocationCode(rs.getString("encounter_location_id"));

                        Patient patient = new Patient();
                        patient.setHid(rs.getString("patient_hid"));
                        encounter.setPatient(patient);

                        Facility facility = new Facility();
                        facility.setFacilityId(rs.getString("facility_id"));
                        encounter.setFacility(facility);

                        return encounter;
                    }
                });
        return encounters.isEmpty() ? null : encounters.get(0);
    }
}