package org.sharedhealth.datasense.export.dhis.report;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.helpers.DatabaseHelper;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class DHISDailyOPDIPDReportTest {
    @Autowired
    private DHISDailyOPDIPDReport dailyOPDIPDReport;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void shouldPostDataForEachFacility() {
        jdbcTemplate.execute("Insert into facility select * from CSVREAD('classpath:/csv/facility.csv')");
        dailyOPDIPDReport.process();
    }

    @After
    public void tearDown() {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }

}