package org.sharedhealth.datasense.repository;

import org.sharedhealth.datasense.model.Encounter;
import org.sharedhealth.datasense.model.Facility;
import org.sharedhealth.datasense.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@Repository
public class EncounterDao {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public void save(Encounter encounter) {
        jdbcTemplate.update("insert into encounter (encounter_id, encounter_datetime, encounter_type, visit_type, patient_hid, " +
                        "patient_age_years, patient_age_months, patient_age_days, encounter_location_id, facility_id) " +
                        "values(?, ?, ? ,? ,?, ?, ?, ? ,?, ?)",
                encounter.getEncounterId(), encounter.getEncounterDateTime(), encounter.getEncounterType(), encounter.getEncounterVisitType(),
                encounter.getPatient().getHid(), encounter.getPatientAgeInYears(), encounter.getPatientAgeInMonths(),
                encounter.getPatientAgeInDays(), encounter.getLocationCode(), encounter.getFacility().getFacilityId());
    }

    public Encounter findEncounterById(String encounterId){
        List<Encounter> encounters = jdbcTemplate.query(
                "select encounter_id ,encounter_datetime, encounter_type, visit_type, patient_hid, " +
                        "patient_age_years, patient_age_months, patient_age_days,encounter_location_id, facility_id " +
                        "from encounter where encounter_id=?", new Object[]{encounterId},
                new RowMapper<Encounter>() {
                    @Override
                    public Encounter mapRow(ResultSet rs, int rowNum) throws SQLException {
                       Encounter encounter = new Encounter();
                        encounter.setEncounterId(rs.getString("encounter_id"));
                        encounter.setEncounterDateTime(new Date(rs.getTimestamp("encounter_datetime").getTime()));
                        encounter.setEncounterType(rs.getString("encounter_type"));
                        encounter.setEncounterVisitType(rs.getString("visit_type"));
                        encounter.setPatientAgeInYears(rs.getInt("patient_age_years"));
                        encounter.setPatientAgeInMonths(rs.getInt("patient_age_months"));
                        encounter.setPatientAgeInDays(rs.getInt("patient_age_days"));
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
