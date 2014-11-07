package org.sharedhealth.datasense.processors;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.hl7.fhir.instance.formats.ResourceOrFeed;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.helpers.DatabaseHelper;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.sharedhealth.datasense.model.Patient;
import org.sharedhealth.datasense.model.fhir.FHIRBundle;
import org.sharedhealth.datasense.repository.PatientDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.sharedhealth.datasense.helpers.ResourceHelper.asString;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class PatientProcessorIntegrationTest {

    @Autowired
    private PatientProcessor processor;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private PatientDao patientDao;

    private final String VALID_HEALTH_ID = "5927558688825933825";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8081);

    @Before
    public void setUp() throws Exception {
        givenThat(get(urlEqualTo("/api/v1/patients/" + VALID_HEALTH_ID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/P" + VALID_HEALTH_ID + ".json"))));
    }

    @After
    public void tearDown() {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }

    @Test
    public void shouldDownloadAndSavePatientIfNotPresent() throws Exception {
        ResourceOrFeed resourceOrFeed = loadFromXmlFile("xmls/sampleEncounter.xml");
        FHIRBundle fhirBundle = new FHIRBundle(resourceOrFeed.getFeed());
        processor.setNext(null);
        processor.process(fhirBundle.getEncounterCompositions().get(0));
        Patient patient = patientDao.getPatientById(VALID_HEALTH_ID);
        assertEquals(VALID_HEALTH_ID, patient.getHid());
        Date dateOfBirth = patient.getDateOfBirth();
        assertEquals("2000-03-01", new SimpleDateFormat("yyyy-MM-dd").format(dateOfBirth));
    }
}