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
        map.put("circumstance_concept_uuid", patientDeathDetails.getCircumstancesOfDeathUuid());
        map.put("circumstance_code", patientDeathDetails.getCircumstancesOfDeathCode());
        map.put("cause_concept_uuid", patientDeathDetails.getCauseOfDeathConceptUuid());
        map.put("cause_code", patientDeathDetails.getCauseOfDeathCode());
        map.put("uuid", patientDeathDetails.getUuid());
        map.put("pod_concept_uuid", patientDeathDetails.getPlaceOfDeathUuid());
        map.put("pod_code", patientDeathDetails.getPlaceOfDeathCode());


        String sql = "insert into patient_death_details (patient_hid, encounter_id, date_of_death, " +
                "circumstance_concept_uuid, circumstance_code, cause_concept_uuid, cause_code, uuid, pod_concept_uuid, pod_code) " +
                "values(:patient_hid, :encounter_id, :date_of_death, " +
                ":circumstance_concept_uuid, :circumstance_code, :cause_concept_uuid, :cause_code, :uuid, :pod_concept_uuid, :pod_code)";

        jdbcTemplate.update(sql, map);
    }

    public void deleteExisting(String encounterId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("encounter_id", encounterId);

        jdbcTemplate.update("delete from patient_death_details where encounter_id = :encounter_id", map);
    }
}
