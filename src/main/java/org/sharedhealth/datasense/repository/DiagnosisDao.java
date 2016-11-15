package org.sharedhealth.datasense.repository;

import org.sharedhealth.datasense.model.Diagnosis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DiagnosisDao {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void save(Diagnosis diagnosis) {
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

    public void delete(String encounterId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("encounter_id", encounterId);

        jdbcTemplate.update("delete from diagnosis where encounter_id=:encounter_id", map);
    }

    public List<Map<String, Object>> getDiagosisWithCount(String facilityId, String startDate, String endDate) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("facility_id", facilityId);
        map.put("start_date", startDate);
        map.put("end_date", endDate);
        String query = "SELECT c.name AS diagnosis_name ,d.diagnosis_code AS code, count(d.diagnosis_concept_id) AS count FROM diagnosis d  JOIN concept c WHERE d.encounter_id IN" +
                "(SELECT encounter_id FROM encounter WHERE facility_id = :facility_id AND encounter_datetime >= STR_TO_DATE(:start_date, '%d/%m/%Y %H:%i:%s')" +
                " AND encounter_datetime <= STR_TO_DATE(:end_date, '%d/%m/%Y %H:%i:%s'))AND " +
                " c.concept_uuid=d.diagnosis_concept_id GROUP BY d.diagnosis_concept_id;";
        List<Map<String, Object>> maps = jdbcTemplate.query(query, map, new ColumnMapRowMapper());
        return maps;
    }
}
