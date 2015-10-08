package org.sharedhealth.datasense.repository;

import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.datasense.model.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Component
public class ConfigurationDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<Parameter> allParameters() {
        List<Parameter> parameters = jdbcTemplate.query(
                "select id, param_name, param_value, param_type, data_type from param_config",
                getParamRowMapper());
        return parameters.isEmpty() ? new ArrayList<Parameter>() : parameters;

    }

    public Parameter parameterById(int paramId) {
        List<Parameter> parameters = jdbcTemplate.query(
                "select id, param_name, param_value, param_type, data_type from param_config where id = :paramId",
                Collections.singletonMap("paramId", paramId),
                getParamRowMapper());
        return parameters.isEmpty() ? null : parameters.get(0);
    }

    public Parameter parameterByName(String paramName) {
        if (StringUtils.isBlank(paramName)) return null;
        List<Parameter> parameters = jdbcTemplate.query(
                "select id, param_name, param_value, param_type, data_type from param_config where param_name = :paramName",
                Collections.singletonMap("paramName", paramName.trim()),
                getParamRowMapper());
        return parameters.isEmpty() ? null : parameters.get(0);
    }

    public void save(Parameter parameter) {
        Parameter existingParam = parameterByName(parameter.getParamName());
        HashMap<String, Object> map = new HashMap<>();
        String queryString;
        if (existingParam != null) {
            queryString = "update param_config set param_value = :paramValue, param_type = :paramType, data_type = :dataType where param_name = :paramName";
        } else {
            queryString = "insert into param_config (param_name, param_value, param_type, data_type) values (:paramName, :paramValue, :paramType, :dataType)";
        }

        map.put("paramType", parameter.getParamType());
        String dataType = StringUtils.isBlank(parameter.getDataType()) ? "String" : parameter.getDataType();
        map.put("dataType", dataType);
        map.put("paramName", parameter.getParamName());
        map.put("paramValue", parameter.getParamValue());

        jdbcTemplate.update(queryString, new MapSqlParameterSource(map));
        return;
    }

    private RowMapper<Parameter> getParamRowMapper() {
        return new RowMapper<Parameter>() {
            @Override
            public Parameter mapRow(ResultSet rs, int rowNum) throws SQLException {
                Parameter param = new Parameter();
                param.setParamId(rs.getInt("id"));
                param.setParamName(rs.getString("param_name"));
                param.setParamValue(rs.getString("param_value"));
                param.setParamType(rs.getString("param_type"));
                param.setDataType(rs.getString("data_type"));
                return param;
            }
        };
    }
}
