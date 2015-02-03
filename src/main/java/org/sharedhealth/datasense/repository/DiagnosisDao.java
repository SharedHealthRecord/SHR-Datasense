package org.sharedhealth.datasense.repository;

import org.sharedhealth.datasense.model.Diagnosis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

@Repository
public class DiagnosisDao {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void save(final Diagnosis diagnosis) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("patient_hid", diagnosis.getPatient().getHid());
        map.put("encounter_id", diagnosis.getEncounter().getEncounterId());
        map.put("diagnosis_datetime", diagnosis.getDiagnosisDateTime());
        map.put("code", diagnosis.getDiagnosisCode());
        map.put("concept_id", diagnosis.getDiagnosisConcept());
        map.put("status", diagnosis.getDiagnosisStatus());
        map.put("uuid", diagnosis.getUuid());
        jdbcTemplate.update("insert into diagnosis(patient_hid, encounter_id, diagnosis_datetime, " +
                "diagnosis_code, diagnosis_concept_id, diagnosis_status, uuid) values " +
                "(:patient_hid, :encounter_id, :diagnosis_datetime, :code, :concept_id, :status, :uuid)", map);
    }
}
