package org.sharedhealth.datasense.repository;

import org.sharedhealth.datasense.model.DiagnosticReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class DiagnosticReportDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public int save(DiagnosticReport diagnosticreport) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("patient_hid", diagnosticreport.getPatientHid());
        map.put("encounter_id", diagnosticreport.getEncounterId());
        map.put("report_datetime", diagnosticreport.getReportDate());
        map.put("report_category", diagnosticreport.getReportCategory());
        map.put("order_id", diagnosticreport.getOrderId());
        map.put("code", diagnosticreport.getCode());
        map.put("fulfiller", diagnosticreport.getFulfiller());
        map.put("report_concept", diagnosticreport.getReportConcept());
        map.put("uuid", diagnosticreport.getUuid());

        String sql = "insert into diagnostic_report (patient_hid, encounter_id, report_datetime, report_category," +
                " code , fulfiller, report_concept,order_id, uuid) values(:patient_hid, :encounter_id, " +
                ":report_datetime, :report_category, :code, :fulfiller, :report_concept, :order_id, :uuid)";
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, new MapSqlParameterSource(map), generatedKeyHolder);
        return generatedKeyHolder.getKey().intValue();
    }

    public void deleteExisting(String encounterId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("encounter_id", encounterId);

        jdbcTemplate.update("delete from diagnostic_report where encounter_id = :encounter_id", map);

    }
}