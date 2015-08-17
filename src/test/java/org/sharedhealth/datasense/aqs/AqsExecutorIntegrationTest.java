package org.sharedhealth.datasense.aqs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class AqsExecutorIntegrationTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    @Autowired
    DatasenseProperties datasenseProperties;

    @Test
    public void shouldExecuteMultipleQueriesAndReturnResults() throws ExecutionException, InterruptedException {
        QueryDefinition admissionsMale = new QueryDefinition();
        admissionsMale.setQueryName("ipd_male_0_to_4");
        admissionsMale.setQueryString("select count(distinct e.patient_hid) as v1 from encounter e, patient p where facility_id=':paramFacilityId:' and e.patient_hid=p.patient_hid and TIMESTAMPDIFF(YEAR, p.dob, e.encounter_datetime) between 0 and 5 and p.gender='M' and LOWER(e.visit_type)='inpatient' and e.encounter_datetime =':paramStartDate:';");

        QueryDefinition emergencyVisitsFemale = new QueryDefinition();
        emergencyVisitsFemale.setQueryName("emergency_female_15_to_24");
        emergencyVisitsFemale.setQueryString("select count(distinct e.patient_hid) as v1 from encounter e, patient p where facility_id=':paramFacilityId:' and e.patient_hid=p.patient_hid and TIMESTAMPDIFF(YEAR, p.dob, e.encounter_datetime) between 15 and 25 and p.gender='F' and LOWER(e.visit_type)='emergency' and e.encounter_datetime=':paramStartDate:';");


        Map<String, Object> params = new HashMap<String, Object>() {{
            put("paramFacilityId", 10005);
            put("paramStartDate", "2015-07-01");
        }};
        AqsConfig cfg = new AqsConfig();
        cfg.setApplicableQueries(Arrays.asList(admissionsMale, emergencyVisitsFemale));

        AqsExecutor executor = new AqsExecutor(jdbcTemplate, datasenseProperties);
        HashMap<String, Object> results = executor.fetchResults(cfg, params);
        Object count = ((List<Map<String, Object>>) results.get("ipd_male_0_to_4")).get(0).get("V1");
        assertEquals(0, Integer.valueOf(count.toString()).intValue());

    }

}