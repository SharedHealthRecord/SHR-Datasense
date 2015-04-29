package org.sharedhealth.datasense.repository;

import org.sharedhealth.datasense.model.Medication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class MedicationDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    public void save(Medication medication) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("patient_hid", medication.getPatient().getHid());
        map.put("encounter_id", medication.getEncounter().getEncounterId());
        map.put("datetime", medication.getDateTime());
        map.put("status", medication.getStatus().getValue());
        map.put("drug_id", medication.getDrugId());
        map.put("uuid", medication.getUuid());
        String sql = "insert into medication (patient_hid, encounter_id, datetime, status,  drug_id, uuid) " +
                "values(:patient_hid, :encounter_id, :datetime, :status,  :drug_id, :uuid)";
        jdbcTemplate.update(sql, map);
    }

    public void deleteExisting(String healthId, String encounterId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("patient_hid", healthId);
        map.put("encounter_id", encounterId);

        String sql = "delete from medication where patient_hid = :patient_hid and encounter_id = :encounter_id";
        jdbcTemplate.update(sql, map);
    }
}
