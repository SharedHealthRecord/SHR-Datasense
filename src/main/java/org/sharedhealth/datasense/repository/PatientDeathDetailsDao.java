package org.sharedhealth.datasense.repository;

import org.sharedhealth.datasense.model.Encounter;
import org.sharedhealth.datasense.model.Patient;
import org.sharedhealth.datasense.model.PatientDeathDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

@Component
public class PatientDeathDetailsDao {


    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public PatientDeathDetailsDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public PatientDeathDetails findByEncounterId(String shrEncounterId) {
        String sql= "select patient_hid, encounter_id, date_of_death, patient_age_years, patient_age_months, patient_age_days, " +
                "circumstances_of_death, cause_concept_uuid, cause_code, uuid " +
                "from patient_death_details where encounter_id= :encounter_id";
        HashMap<String, Object> map= new HashMap<>();
        map.put("encounter_id",shrEncounterId);
        List<PatientDeathDetails> patientDeathDetailsList = jdbcTemplate.query(sql, map, new RowMapper<PatientDeathDetails>() {
            @Override
            public PatientDeathDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
                PatientDeathDetails patientDeathDetails = new PatientDeathDetails();
                Patient patient = new Patient();
                patient.setHid(rs.getString("patient_hid"));
                patientDeathDetails.setPatient(patient);

                Encounter encounter = new Encounter();
                encounter.setEncounterId(rs.getString("encounter_id"));
                patientDeathDetails.setEncounter(encounter);

                patientDeathDetails.setDateOfDeath(rs.getTimestamp("date_of_death"));
                patientDeathDetails.setPatientAgeInYears(rs.getInt("patient_age_years"));
                patientDeathDetails.setPatientAgeInMonths(rs.getInt("patient_age_months"));
                patientDeathDetails.setPatientAgeInDays(rs.getInt("patient_age_days"));
                patientDeathDetails.setCircumstancesOfDeath(rs.getString("circumstances_of_death"));
                patientDeathDetails.setCauseOfDeathConceptUuid(rs.getString("cause_concept_uuid"));
                patientDeathDetails.setCauseOfDeathCode(rs.getString("cause_code"));
                patientDeathDetails.setUuid(rs.getString("uuid"));
                return patientDeathDetails;
            }
        });
        return patientDeathDetailsList.isEmpty() ? null : patientDeathDetailsList.get(0);
    }

    public void save(PatientDeathDetails patientDeathDetails) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("patient_hid", patientDeathDetails.getPatient().getHid());
        map.put("encounter_id", patientDeathDetails.getEncounter().getEncounterId());
        map.put("date_of_death", patientDeathDetails.getDateOfDeath());
        map.put("patient_age_years", patientDeathDetails.getPatientAgeInYears());
        map.put("patient_age_months", patientDeathDetails.getPatientAgeInMonths());
        map.put("patient_age_days", patientDeathDetails.getPatientAgeInDays());
        map.put("circumstances_of_death", patientDeathDetails.getCircumstancesOfDeath());
        map.put("cause_concept_uuid", patientDeathDetails.getCauseOfDeathConceptUuid());
        map.put("cause_code", patientDeathDetails.getCauseOfDeathCode());
        map.put("uuid", patientDeathDetails.getUuid());


        String sql = "insert into patient_death_details (patient_hid, encounter_id, date_of_death, patient_age_years, patient_age_months, patient_age_days," +
                "circumstances_of_death, cause_concept_uuid, cause_code, uuid) " +
                "values(:patient_hid, :encounter_id, :date_of_death, :patient_age_years, :patient_age_months, :patient_age_days, " +
                " :circumstances_of_death, :cause_concept_uuid, :cause_code, :uuid)";

        jdbcTemplate.update(sql, map);
    }
}
