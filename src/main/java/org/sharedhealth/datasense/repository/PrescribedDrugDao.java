package org.sharedhealth.datasense.repository;

import org.sharedhealth.datasense.model.PrescribedDrug;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class PrescribedDrugDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void save(PrescribedDrug prescribedDrug) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("patient_hid", prescribedDrug.getPatientHid());
        map.put("encounter_id", prescribedDrug.getEncounterId());
        map.put("prescription_datetime", prescribedDrug.getPrescriptionDateTime());
        map.put("drug_uuid",prescribedDrug.getDrugUuid());
        map.put("drug_name",prescribedDrug.getDrugName());
        map.put("prescriber",prescribedDrug.getPrescriber());
        map.put("status",prescribedDrug.getStatus());
        map.put("shr_medication_order_uuid", prescribedDrug.getShrMedicationOrderUuid());
        map.put("prior_shr_medication_order_uuid", prescribedDrug.getPriorShrMedicationOrderUuid());
        map.put("uuid",prescribedDrug.getUuid());

        String sql = "insert into prescribed_drugs (patient_hid, encounter_id, prescription_datetime, drug_uuid, drug_name, prescriber, status, shr_medication_order_uuid, prior_shr_medication_order_uuid, uuid) " +
                                              "values(:patient_hid, :encounter_id, :prescription_datetime, :drug_uuid, :drug_name, :prescriber, :status, :shr_medication_order_uuid, :prior_shr_medication_order_uuid, :uuid )";
        jdbcTemplate.update(sql, map);
    }

    public void deleteExisting(String encounterId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("encounter_id", encounterId);

        jdbcTemplate.update("delete from prescribed_drugs where encounter_id = :encounter_id", map);

    }
}