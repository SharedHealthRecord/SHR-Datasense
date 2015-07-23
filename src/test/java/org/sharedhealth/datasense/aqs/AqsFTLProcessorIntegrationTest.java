package org.sharedhealth.datasense.aqs;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.client.DHIS2Client;
import org.sharedhealth.datasense.dhis2.model.DHISResponse;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class AqsFTLProcessorIntegrationTest {

    @Autowired
    AqsFTLProcessor processor;

    @Autowired
    DHIS2Client dhis2Client;

    @Test
    public void shouldProcessResultsUsingTemplate() throws IOException {
        Map<String, Object> params = new HashMap<String, Object>() {{
            put("paramFacilityId", 10005);
            put("paramStartDate", "2015-07-01");
            put("paramDatasetId", "iUz0yoVeeiZ");
            put("paramReportingPeriod", "20150701");
            put("paramOrgUnitId", "nRm6mKjJsaE");
        }};
        //AqsExecutor executor = new AqsExecutor(jdbcTemplate);
//        AqsFTLProcessor processor = new AqsFTLProcessor(executor);
        String content = processor.process("test_opd_ipd_report.json", params);
        System.out.println(content);
//        DHISResponse dhisResponse = dhis2Client.post(content);
//        System.out.println(dhisResponse.getValue());

    }

}