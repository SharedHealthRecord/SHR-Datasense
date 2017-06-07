package org.sharedhealth.datasense.repository;

import org.sharedhealth.datasense.model.PrescribedDrug;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PrescribedDrugDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void save(PrescribedDrug prescribedDrug) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("patient_hid", prescribedDrug.getPatientHid());
        map.put("encounter_id", prescribedDrug.getEncounterId());
        map.put("prescription_datetime", prescribedDrug.getPrescriptionDateTime());
        map.put("drug_code", prescribedDrug.getDrugCode());
        map.put("non_coded_name", prescribedDrug.getNonCodedName());
        map.put("prescriber", prescribedDrug.getPrescriber());
        map.put("status", prescribedDrug.getStatus());
        map.put("shr_medication_order_uuid", prescribedDrug.getShrMedicationOrderUuid());
        map.put("prior_shr_medication_order_uuid", prescribedDrug.getPriorShrMedicationOrderUuid());
        map.put("uuid", prescribedDrug.getUuid());

        String sql = "insert into prescribed_drug (patient_hid, encounter_id, prescription_datetime, drug_code, non_coded_name, prescriber, status, shr_medication_order_uuid, prior_shr_medication_order_uuid, uuid) " +
                "values(:patient_hid, :encounter_id, :prescription_datetime, :drug_code, :non_coded_name, :prescriber, :status, :shr_medication_order_uuid, :prior_shr_medication_order_uuid, :uuid )";
        jdbcTemplate.update(sql, map);
    }

    public void deleteExisting(String encounterId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("encounter_id", encounterId);

        jdbcTemplate.update("delete from prescribed_drug where encounter_id = :encounter_id", map);
    }

    public List<Map<String, Object>> getTotalFreeTextCount(String facilityId, String startDate, String endDate) {
        HashMap<String, Object> map = getGroupedArguments(facilityId, startDate, endDate);
        String query = "SELECT count(d.uuid) AS count FROM prescribed_drug d WHERE d.encounter_id IN" +
                "(SELECT encounter_id FROM encounter WHERE facility_id = :facility_id AND " +
                "encounter_datetime >= STR_TO_DATE(:start_date, '%d/%m/%Y %H:%i:%s') AND " +
                "encounter_datetime <= STR_TO_DATE(:end_date, '%d/%m/%Y %H:%i:%s')) AND " +
                "d.non_coded_name IS NOT NULL";
        List<Map<String, Object>> maps = jdbcTemplate.query(query, map, new ColumnMapRowMapper());
        return maps;
    }

    public List<Map<String, Object>> getNonCodedDrugs(String facilityId, String startDate, String endDate) {
        HashMap<String, Object> map = getGroupedArguments(facilityId, startDate, endDate);
        String query = "SELECT d.non_coded_name AS drug,count(d.non_coded_name) AS count FROM prescribed_drug d WHERE d.encounter_id IN" +
                "(SELECT encounter_id FROM encounter WHERE facility_id = :facility_id AND " +
                "encounter_datetime >= STR_TO_DATE(:start_date, '%d/%m/%Y %H:%i:%s')" +
                "AND encounter_datetime <= STR_TO_DATE(:end_date, '%d/%m/%Y %H:%i:%s')) " +
                "AND d.non_coded_name IS NOT NULL GROUP BY d.non_coded_name";
        List<Map<String, Object>> maps = jdbcTemplate.query(query, map, new ColumnMapRowMapper());
        return maps;
    }

    public List<Map<String, Object>> getCodedDrugCount(String facilityId, String startDate, String endDate) {
        HashMap<String, Object> map = getGroupedArguments(facilityId, startDate, endDate);
        String query = "SELECT count(d.uuid)AS count FROM prescribed_drug d WHERE d.encounter_id IN" +
                "(SELECT encounter_id FROM encounter WHERE facility_id = :facility_id AND " +
                "encounter_datetime >= STR_TO_DATE(:start_date, '%d/%m/%Y %H:%i:%s')" +
                "AND encounter_datetime <= STR_TO_DATE(:end_date, '%d/%m/%Y %H:%i:%s')) " +
                "AND d.drug_code IS NOT NULL";
        List<Map<String, Object>> maps = jdbcTemplate.query(query, map, new ColumnMapRowMapper());
        return maps;
    }

    public List<Map<String, Object>> getCodedDrugs(String facilityId, String startDate, String endDate) {
        HashMap<String, Object> map = getGroupedArguments(facilityId, startDate, endDate);
        String query = "SELECT d.name AS drug_name, c.name AS concept_name," +
                "r.name AS reference_term_name, r.code AS reference_term_code, count(pd.drug_code) AS count " +
                "FROM prescribed_drug pd " +
                "JOIN drug d ON pd.drug_code = d.drug_uuid " +
                "JOIN concept c ON d.concept_uuid = c.concept_uuid " +
                "LEFT JOIN reference_term r ON d.reference_term_uuid = r.reference_term_uuid " +
                "WHERE pd.encounter_id IN " +
                "(SELECT encounter_id FROM encounter WHERE facility_id = :facility_id AND " +
                "encounter_datetime >= STR_TO_DATE(:start_date, '%d/%m/%Y %H:%i:%s') " +
                "AND encounter_datetime <= STR_TO_DATE(:end_date, '%d/%m/%Y %H:%i:%s')) " +
                "AND pd.drug_code IS NOT NULL GROUP BY pd.drug_code";
        List<Map<String, Object>> maps = jdbcTemplate.query(query, map, new ColumnMapRowMapper());
        return maps;
    }

    private HashMap<String, Object> getGroupedArguments(String facilityId, String startDate, String endDate) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("facility_id", facilityId);
        map.put("start_date", startDate);
        map.put("end_date", endDate);
        return map;
    }
}