package org.sharedhealth.datasense.repository;

import org.sharedhealth.datasense.model.ImmunizationReason;
import org.sharedhealth.datasense.model.Immunization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
public class ImmunizationDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    public void save(Immunization immunization) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("patient_hid", immunization.getPatient().getHid());
        map.put("encounter_id", immunization.getEncounter().getEncounterId());
        map.put("datetime", immunization.getDateTime());
        map.put("status", immunization.getStatus().getValue());
        map.put("drug_id", immunization.getDrugId());
        map.put("uuid", immunization.getUuid());
        String sql = "insert into immunizations (patient_hid, encounter_id, datetime, status,  drug_id, uuid) " +
                "values(:patient_hid, :encounter_id, :datetime, :status,  :drug_id, :uuid)";
        jdbcTemplate.update(sql, map);
    }

    public void deleteExisting(String healthId, String encounterId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("patient_hid", healthId);
        map.put("encounter_id", encounterId);

        String medicationSql = "delete from immunizations where patient_hid = :patient_hid and encounter_id = :encounter_id";
        jdbcTemplate.update(medicationSql, map);
    }

    public void save(List<ImmunizationReason> immunizationReasons) {
        String insertStatement = "insert into immunization_reason (patient_hid, encounter_id, descr, code, uuid, incident_uuid) " +
                "values(:patient_hid, :encounter_id, :descr, :code, :uuid, :incident_uuid)";
        for (ImmunizationReason reason : immunizationReasons) {
            HashMap<String, Object> params = new HashMap<>();
            params.put("patient_hid", reason.getHid());
            params.put("encounter_id", reason.getEncounterId());
            params.put("descr", reason.getDescr());
            params.put("code", reason.getCode());
            params.put("uuid", reason.getUuid());
            params.put("incident_uuid", reason.getIncidentUuid());
            jdbcTemplate.update(insertStatement, params);
        }
    }

    public void deleteExistingImmunizationReasons(String healthId, String encounterId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("patient_hid", healthId);
        map.put("encounter_id", encounterId);

        String immunizationSql = "delete from immunization_reason where patient_hid = :patient_hid and encounter_id = :encounter_id";
        jdbcTemplate.update(immunizationSql, map);

    }
}
