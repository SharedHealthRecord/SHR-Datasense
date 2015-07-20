package org.sharedhealth.datasense.repository;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.feeds.patients.AddressChange;
import org.sharedhealth.datasense.feeds.patients.Change;
import org.sharedhealth.datasense.feeds.patients.PatientData;
import org.sharedhealth.datasense.feeds.patients.PatientUpdate;
import org.sharedhealth.datasense.helpers.DatabaseHelper;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.sharedhealth.datasense.model.Address;
import org.sharedhealth.datasense.model.Patient;
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
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})

public class PatientDaoIT {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private PatientDao patientDao;

    @Test
    public void shouldUpdatePatientDetailsBasedOnChangeSet() throws Exception {
        final Patient patient = new Patient();
        String hid = "1234567";
        patient.setHid(hid);

        Map<String, Object> fields = new HashMap<String, Object>(){{
            put("dob", "2015-01-01");
            put("gender", "M");
            put("address", new Address("","30","26","15",null,null, null,null));
        }};

        setFields(patient, fields);
        patientDao.save(patient);

        Map<String, Object> updatedFields = new HashMap<>(fields);
        updatedFields.put("dob", "2014-11-10");
        updatedFields.put("address", new Address("","20","22","",null,null, null,null));

        PatientUpdate patientUpdate = getPatientUpdate(fields, updatedFields);
        patientUpdate.setHealthId(hid);
        patientDao.update(patientUpdate);

        // gender and address updated
        setFields(patient, updatedFields);
        assertPatient(patient, hid);

    }

    private PatientUpdate getPatientUpdate(Map<String, Object> fields, Map<String, Object> updatedFields){
        PatientUpdate patientUpdate= new PatientUpdate();
        PatientData patientData = new PatientData();
        if(updatedFields.get("gender") != null){
            patientData.setGenderChange(new Change(fields.get("gender"), updatedFields.get("gender")));
        }
        if(updatedFields.get("dob") != null){
            patientData.setDobChange(new Change(fields.get("dob"), updatedFields.get("dob")));
        }
        if(updatedFields.get("address") != null){
            patientData.setAddressChange(new AddressChange(((Address) fields.get("address")), ((Address) updatedFields.get("address"))));
        }

        patientUpdate.setChangeSetMap(patientData);
        return patientUpdate;
    }

    private void setFields(Patient patient, Map<String, Object> fields) {
        patient.setDateOfBirth(DateUtil.parseDate((String)fields.get("dob")));
        patient.setGender((String)fields.get("gender"));
        patient.setPresentAddress((Address)fields.get("address"));
    }

    private void assertPatient(final Patient patient, String healthId) {
        jdbcTemplate.query(String.format("select * from patient where patient_hid='%s'", healthId), new RowMapper<ResultSet>() {
            @Override
            public ResultSet mapRow(ResultSet rs, int rowNum) throws SQLException {
                assertEquals(patient.getDateOfBirth(), rs.getDate("dob"));
                assertEquals(patient.getGender(), rs.getString("gender"));
                assertEquals(patient.getPresentLocationCode(), rs.getString("present_location_id"));
                return rs;
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }
}