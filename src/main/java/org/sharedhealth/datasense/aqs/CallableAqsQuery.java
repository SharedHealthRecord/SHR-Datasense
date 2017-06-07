package org.sharedhealth.datasense.aqs;


import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CallableAqsQuery implements Callable<HashMap<String, List<Map<String, Object>>>> {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private QueryDefinition queryDefinition;
    private Map<String, Object> params;

    private static final Logger logger = Logger.getLogger(CallableAqsQuery.class);

    public CallableAqsQuery(NamedParameterJdbcTemplate jdbcTemplate, QueryDefinition queryDefinition, Map<String, Object> params) {
        this.jdbcTemplate = jdbcTemplate;
        this.queryDefinition = queryDefinition;
        this.params = params;
    }

    @Override
    public HashMap<String, List<Map<String, Object>>> call() throws Exception {
        String queryString = queryDefinition.getQueryString();
        Pattern p = Pattern.compile("\\:(.*?)\\:");
        Matcher m = p.matcher(queryDefinition.getQueryString());
        while (m.find()) {
            String paramName = m.group(1);
            Object paramValue = params.get(paramName);
            if (paramValue == null) {
                String message = "Could not find value for query parameter:" + paramName;
                logger.error(message);
                throw new RuntimeException(message);
            }
            queryString = queryString.replace(String.format(":%s:", paramName), String.format("%s", paramValue));
        }
        logger.info("Executing query");
        logger.debug(queryString);
        List<Map<String, Object>> queryResults = jdbcTemplate.query(queryString, new RowMapper<Map<String, Object>>() {
            @Override
            public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                Map<String, Object> rowMap = new HashMap<String, Object>();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int colIdx = 1; colIdx <= columnCount; colIdx++) {
                    String columnName = metaData.getColumnName(colIdx);
                    Object columnValue = rs.getObject(columnName);
                    rowMap.put(columnName, columnValue);
                }
                return rowMap;
            }
        });

        HashMap<String, List<Map<String, Object>>> results = new HashMap<>();
        results.put(queryDefinition.getQueryName(), queryResults);
        return results;
    }

}
