package org.sharedhealth.datasense.repository;

import org.sharedhealth.datasense.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Repository
public class PatientDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public Patient findPatientById(String healthId) {
        List<Patient> patients = jdbcTemplate.query(
                "select patient_hid, dob, gender, present_location_id from patient where patient_hid= :patient_hid",
                Collections.singletonMap("patient_hid", healthId),
                new RowMapper<Patient>() {
                    @Override
                    public Patient mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Patient patient = new Patient();
                        patient.setHid(rs.getString("patient_hid"));
                        patient.setDateOfBirth(new Date(rs.getTimestamp("dob").getTime()));
                        patient.setGender(rs.getString("gender"));
                        patient.setPresentAddressCode(rs.getString("present_location_id"));
                        return patient;
                    }
                });
        return patients.isEmpty() ? null : patients.get(0);
    }

    public void save(Patient patient) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("patient_hid", patient.getHid());
        map.put("dob", patient.getDateOfBirth());
        map.put("gender", patient.getGender());
        map.put("present_location_id", patient.getPresentLocationCode());
        jdbcTemplate.update("insert into patient (patient_hid, dob, gender, present_location_id) values" +
                "(:patient_hid, :dob, :gender, :present_location_id)", map);
    }
}
