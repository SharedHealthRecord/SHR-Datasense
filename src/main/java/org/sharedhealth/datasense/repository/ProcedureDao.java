package org.sharedhealth.datasense.repository;

import org.sharedhealth.datasense.model.Procedure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class ProcedureDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void save(Procedure procedure) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("patient_hid", procedure.getPatientHid());
        map.put("encounter_id", procedure.getEncounterId());
        map.put("datetime", procedure.getEncounterDate());
        map.put("start_date", procedure.getStartDate());
        map.put("end_date", procedure.getEndDate());
        map.put("procedure_uuid", procedure.getProcedureUuid());
        map.put("procedure_code", procedure.getProcedureCode());
        map.put("diagnosis_uuid", procedure.getDiagnosisUuid());
        map.put("diagnosis_code", procedure.getDiagnosisCode());

        String sql = "insert into procedures (patient_hid, encounter_id, datetime, start_date, end_date, procedure_uuid, " +
                "procedure_code, diagnosis_uuid, diagnosis_code) " +
                "values(:patient_hid, :encounter_id, :datetime, :start_date, :end_date, :procedure_uuid, :procedure_code, " +
                ":diagnosis_uuid, :diagnosis_code)";
        jdbcTemplate.update(sql, map);
    }

    public void deleteExisting(String encounterId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("encounter_id", encounterId);

        jdbcTemplate.update("delete from procedures where encounter_id = :encounter_id", map);

    }
}