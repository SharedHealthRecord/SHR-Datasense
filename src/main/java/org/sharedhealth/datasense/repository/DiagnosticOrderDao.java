package org.sharedhealth.datasense.repository;

import org.sharedhealth.datasense.model.DiagnosticOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

@Component
public class DiagnosticOrderDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void save(DiagnosticOrder diagnosticOrder) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("patient_hid", diagnosticOrder.getPatientHid());
        map.put("encounter_id", diagnosticOrder.getEncounterId());
        map.put("order_datetime", diagnosticOrder.getOrderDate());
        map.put("order_category", diagnosticOrder.getOrderCategory());
        map.put("code", diagnosticOrder.getCode());
        map.put("orderer", diagnosticOrder.getOrderer());
        map.put("order_concept", diagnosticOrder.getOrderConcept());
        map.put("order_status", diagnosticOrder.getOrderStatus());
        map.put("uuid", diagnosticOrder.getUuid());

        String sql = "insert into diagnostic_order (patient_hid, encounter_id, order_datetime, order_category," +
                " code , orderer, order_concept, order_status, uuid) values(:patient_hid, :encounter_id, " +
                ":order_datetime, :order_category, :code, :orderer, :order_concept, :order_status, :uuid)";

        jdbcTemplate.update(sql, map);
    }

    public List<DiagnosticOrder> getOrderId(String encounterId, String conceptUuid) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("encounter_id", encounterId);
        map.put("order_concept", conceptUuid);

        String sql = "select order_id from diagnostic_order where encounter_id =:encounter_id " +
                "and  order_concept =:order_concept";

        return jdbcTemplate.query(sql, map, new RowMapper<DiagnosticOrder>() {
            @Override
            public DiagnosticOrder mapRow(ResultSet rs, int rowNum) throws SQLException {
                DiagnosticOrder order = new DiagnosticOrder();
                order.setId(rs.getInt("order_id"));
                return order;
            }
        });


    }

    public void deleteExisting(String healthId, String encounterId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("patient_hid", healthId);
        map.put("encounter_id", encounterId);

        jdbcTemplate.update("delete from diagnostic_order where patient_hid = :patient_hid and encounter_id = :encounter_id", map);

    }
}