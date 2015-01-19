package org.sharedhealth.datasense.export.dhis.report;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.helpers.DatabaseHelper;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class DHISDailyOPDIPDReportTest {
    @Autowired
    private DHISDailyOPDIPDReport dailyOPDIPDReport;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    public void shouldPostDataForEachFacility() {
        jdbcTemplate.update("Insert into facility select * from CSVREAD('classpath:/csv/facility.csv')", new EmptySqlParameterSource());
        jdbcTemplate.update("Insert into patient select * from CSVREAD('classpath:/csv/patients.csv')", new EmptySqlParameterSource());
        jdbcTemplate.update("Insert into encounter select * from CSVREAD('classpath:/csv/encounters.csv')", new EmptySqlParameterSource());
        Map<String, Object> map = new HashMap<>();
        map.put("reportingDate", "2014-12-23");
//        dailyOPDIPDReport.process(map);
    }

    @After
    public void tearDown() {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }

}