package org.sharedhealth.datasense.export.dhis;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class DHISDatasetExportIntegrationTest {

    private Configuration cfg;

    @Autowired
    private DataSource dataSource;

    @Before
    public void setup() throws IOException {
        cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
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
    @Ignore
    public void shouldPostValues() {
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("FACILITY", "1000");
        queryParams.put("MIN_YEAR", "5");
        queryParams.put("MAX_YEAR", "14");
        queryParams.put("GENDER", "M");
        queryParams.put("VISIT_TYPE", "inpatient");
        queryParams.put("ENC_DATE", "2014-12-23");
        queryParams.put("GENDER2", "F");

        HashMap<String, String> extraParams = new HashMap<>();
        extraParams.put("dataset", "iUz0yoVeeiZ");
        extraParams.put("period", "20141223");
        extraParams.put("orgUnit", "qNNm09QC9O8");

        HashMap<String, String> postHeaders = new HashMap<>();
        postHeaders.put("Authorization", "Basic YWRtaW46ZGlzdHJpY3Q=");
        postHeaders.put("Content-Type", "application/json");
    }


}
