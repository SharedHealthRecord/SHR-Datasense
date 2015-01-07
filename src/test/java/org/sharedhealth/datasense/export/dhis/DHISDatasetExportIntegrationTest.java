package org.sharedhealth.datasense.export.dhis;

import aggregatequeryservice.postservice;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestListener;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.junit.*;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.helpers.DatabaseHelper;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.sharedhealth.datasense.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class DHISDatasetExportIntegrationTest {

    private Configuration cfg;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Before
    public void setup() throws IOException {
        cfg = new Configuration();
        cfg.setClassForTemplateLoading(this.getClass(), "/dhis/templates/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }


    @Test
    public void shouldGenerateContentForDHIS() throws IOException, TemplateException {
        Template temp = cfg.getTemplate("DTFTest.ftl");
        Map model = new HashMap();
        model.put("dataset", "KvrdDT4bwn9");
        model.put("period", "201410");
        Writer out = new OutputStreamWriter(System.out);
        temp.process(model, out);
    }

    @Test
    public void shouldInsertDataFromCSV() throws IOException{
        jdbcTemplate.execute("INSERT INTO patient SELECT * FROM CSVREAD('classpath:/csv/patients.csv') ");

        List<Patient> patients = jdbcTemplate.query("select patient_hid,dob,gender,present_location_id from patient where patient_hid=123", new RowMapper<Patient>() {
            @Override
            public Patient mapRow(ResultSet rs, int rowNum) throws SQLException {
                Patient patient = new Patient();
                patient.setDateOfBirth(rs.getDate("dob"));
                patient.setGender(rs.getString("gender"));
                patient.setHid(rs.getString("patient_hid"));
                patient.setPresentAddressCode(rs.getString("present_location_id"));
                return patient;
            }
        });

        Patient patient = patients.get(0);
        assertEquals("123", patient.getHid());
        assertEquals("M", patient.getGender());
        assertEquals("1000", patient.getPresentLocationCode());
        SimpleDateFormat actual = new SimpleDateFormat("YYYY-MM-dd");

        assertEquals("2013-10-10", actual.format(patient.getDateOfBirth()));
    }

    @Test
    @Ignore
    public void shouldPostValues() {
        jdbcTemplate.execute("Insert into facility select * from CSVREAD('classpath:/csv/facility.csv')");
        jdbcTemplate.execute("Insert into patient select * from CSVREAD('classpath:/csv/patients.csv')");
        jdbcTemplate.execute("Insert into encounter select * from CSVREAD('classpath:/csv/encounters.csv')");

        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("FACILITY", "1000");
        queryParams.put("MIN_YEAR", "5");
        queryParams.put("MAX_YEAR", "14");
        queryParams.put("GENDER", "M");
        queryParams.put("VISIT_TYPE", "inpatient");
        queryParams.put("ENC_DATE", "2014-12-23");
        queryParams.put("GENDER2","F");

        HashMap<String, String> extraParams = new HashMap<>();
        extraParams.put("dataset","iUz0yoVeeiZ");
        extraParams.put("period","20141223");
        extraParams.put("orgUnit","qNNm09QC9O8");

        HashMap<String, String> postHeaders = new HashMap<>();
        postHeaders.put("Authorization", "Basic YWRtaW46ZGlzdHJpY3Q=");
        postHeaders.put("Content-Type", "application/json");

        Object o = postservice.executeQueriesAndPostResultsSync("dhis/config/post-config.json", dataSource, queryParams, extraParams, postHeaders);


        String postData = "{\n  \"dataSet\": \"iUz0yoVeeiZ\",\n  \"period\": \"20141223\",\n  \"orgUnit\": \"qNNm09QC9O8\",\n  \"dataValues\": [\n    { \"dataElement\": \"AiPqHCbJQJ1\", \"categoryOptionCombo\": \"UBdaznQ8DlT\",\n      \"value\": \"0\"\n    },\n    { \"dataElement\": \"AiPqHCbJQJ1\", \"categoryOptionCombo\": \"tSwmrlTW11V\",\n      \"value\": \"1\"\n    }\n  ]\n}";
        verify(
                postRequestedFor(urlEqualTo("/dhis/api/dataValueSets"))
                        .withHeader("Content-Type", matching("application/json"))
                        .withHeader("Authorization", matching("Basic YWRtaW46ZGlzdHJpY3Q="))
                        .withRequestBody(equalToJson(postData))

        );
    }

    @Test
    public void shouldPrepareDatabase() {
        jdbcTemplate.execute("Insert into facility select * from CSVREAD('classpath:/csv/facility.csv')");
        jdbcTemplate.execute("Insert into patient select * from CSVREAD('classpath:/csv/patients.csv')");
        jdbcTemplate.execute("Insert into encounter select * from CSVREAD('classpath:/csv/encounters.csv')");

        String sql_for_M_10 = "select count(distinct e.patient_hid) from encounter e, patient p where facility_id=1000 and e.patient_hid=p.patient_hid and e.patient_age_years >= 5 and e.patient_age_years < 14 and p.gender='M' and e.visit_type='inpatient' and encounter_datetime='2014-12-23';";
        String sql_for_M_11 = "select count(distinct e.patient_hid) from encounter e, patient p where facility_id=1000 and e.patient_hid=p.patient_hid and e.patient_age_years >= 5 and e.patient_age_years < 14 and p.gender='F' and e.visit_type='inpatient' and encounter_datetime='2014-12-23';";
        String sql_for_F_11 = "select count(distinct e.patient_hid) from encounter e, patient p where facility_id=1000 and e.patient_hid=p.patient_hid and e.patient_age_years >= 15 and e.patient_age_years < 50 and p.gender='F' and e.visit_type='outpatient' and encounter_datetime='2014-10-11';";
        Integer dataValue_M_10 = executeQuery(sql_for_M_10);
        Integer dataValue_M_11 = executeQuery(sql_for_M_11);
        Integer dataValue_F_11 = executeQuery(sql_for_F_11);
        System.out.println("******************************");
        System.out.println(dataValue_M_10);
        System.out.println(dataValue_M_11);
        System.out.println(dataValue_F_11);
        System.out.println("******************************");
    }

    private Integer executeQuery(String sql) {
        return jdbcTemplate.query(sql, new RowMapper<Integer>() {
            @Override
            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getInt(1);
            }
        }).get(0);
    }

    @After
    public void tearDown() {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }

}
