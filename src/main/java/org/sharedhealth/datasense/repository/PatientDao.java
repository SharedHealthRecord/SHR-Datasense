package org.sharedhealth.datasense.repository;

import org.apache.log4j.Logger;
import org.sharedhealth.datasense.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class PatientDao {
    @Autowired
    JdbcTemplate jdbcTemplate;

    Logger log = Logger.getLogger(PatientDao.class);

    public Patient getPatientById(String healthId) {
        List<Patient> patients = jdbcTemplate.query(
                "select patient_hid, dob, gender, present_location_id from patient where patient_hid=?", new Object[]{healthId},
                new RowMapper<Patient>() {
                    @Override
                    public Patient mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Patient patient = new Patient();
                        patient.setHid(rs.getString("patient_hid"));
                        patient.setDateOfBirth(rs.getDate("dob"));
                        patient.setGender(rs.getString("gender"));
                        patient.setPresentAddressCode(rs.getString("present_location_id"));
                        return patient;
                    }
                });
        return patients.isEmpty() ? null : patients.get(0);
    }

    public void save(Patient patient) {
        jdbcTemplate.update("insert into patient (patient_hid, dob, gender, present_location_id) values(?, ? ,? ,?)",
                patient.getHid(), patient.getDateOfBirth(), patient.getGender(), patient.getPresentLocationCode());
    }
}
