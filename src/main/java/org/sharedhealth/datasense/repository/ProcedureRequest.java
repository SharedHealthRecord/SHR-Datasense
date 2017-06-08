package org.sharedhealth.datasense.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

@Component
public class ProcedureRequest {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void save(org.sharedhealth.datasense.model.ProcedureRequest procedureRequest) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("patient_hid", procedureRequest.getPatientHid());
        map.put("encounter_id", procedureRequest.getEncounterId());
        map.put("order_datetime", procedureRequest.getOrderDate());
        map.put("order_category", procedureRequest.getOrderCategory());
        map.put("code", procedureRequest.getCode());
        map.put("orderer", procedureRequest.getOrderer());
        map.put("order_concept", procedureRequest.getOrderConcept());
        map.put("order_status", procedureRequest.getOrderStatus());
        map.put("shr_order_uuid", procedureRequest.getShrOrderUuid());
        map.put("uuid", procedureRequest.getUuid());

        String sql = "insert into procedure_request (patient_hid, encounter_id, order_datetime, order_category," +
                " code , orderer, order_concept, order_status, shr_order_uuid, uuid) values(:patient_hid, :encounter_id, " +
                ":order_datetime, :order_category, :code, :orderer, :order_concept, :order_status, :shr_order_uuid, :uuid)";

        jdbcTemplate.update(sql, map);
    }

    public Integer getOrderId(String encounterId, String conceptUuid) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("encounter_id", encounterId);
        map.put("order_concept", conceptUuid);

        String sql = "select order_id from procedure_request where encounter_id =:encounter_id " +
                "and  order_concept =:order_concept";

        List<Integer> order_id = jdbcTemplate.query(sql, map, new RowMapper<Integer>() {
            @Override
            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getInt("order_id");
            }
        });
        if (order_id.isEmpty()) return null;
        return order_id.get(0);
    }

    public Integer getOrderId(String shrOrderUuid) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("shr_order_uuid", shrOrderUuid);

        String sql = "select order_id from procedure_request where shr_order_uuid = :shr_order_uuid";

        List<Integer> order_id = jdbcTemplate.query(sql, map, new RowMapper<Integer>() {
            @Override
            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getInt("order_id");
            }
        });
        if (order_id.isEmpty()) return null;
        return order_id.get(0);
    }

    public void deleteExisting(String encounterId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("encounter_id", encounterId);

        jdbcTemplate.update("delete from procedure_request where encounter_id = :encounter_id", map);

    }
}