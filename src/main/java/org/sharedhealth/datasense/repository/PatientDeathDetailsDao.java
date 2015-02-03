package org.sharedhealth.datasense.repository;

import org.sharedhealth.datasense.model.PatientDeathDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class PatientDeathDetailsDao {


    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public PatientDeathDetailsDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
