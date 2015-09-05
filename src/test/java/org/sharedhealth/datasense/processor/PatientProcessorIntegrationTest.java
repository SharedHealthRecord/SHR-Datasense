package org.sharedhealth.datasense.processor;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.client.MciWebClient;
import org.sharedhealth.datasense.helpers.DatabaseHelper;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.sharedhealth.datasense.model.Patient;
import org.sharedhealth.datasense.model.fhir.BundleContext;
import org.sharedhealth.datasense.repository.PatientDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.sharedhealth.datasense.helpers.ResourceHelper.asString;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;
import static org.sharedhealth.datasense.util.HeaderUtil.AUTH_TOKEN_KEY;
import static org.sharedhealth.datasense.util.HeaderUtil.CLIENT_ID_KEY;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class PatientProcessorIntegrationTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    @Autowired
    private MciWebClient webClient;
    @Autowired
    private PatientDao patientDao;
    private PatientProcessor processor;

    private final String VALID_HEALTH_ID = "98001046534";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Before
    public void setUp() throws Exception {
        processor = new PatientProcessor(null, webClient, patientDao);
    }

    @After
    public void tearDown() {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }

    @Test
    public void shouldDownloadAndSavePatientIfNotPresent() throws Exception {
        String authToken = "b7aa1f4001ac4b922dabd6a02a0dabc44cf5af74a0d1b68003ce7ccdb897a1d2";
        UUID token = UUID.randomUUID();

        String response = "{\"access_token\" : \"" + token.toString() + "\"}";

        givenThat(post(urlEqualTo("/signin"))
                .withHeader(CLIENT_ID_KEY, equalTo("18552"))
                .withHeader(AUTH_TOKEN_KEY, equalTo(authToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(response)));

        givenThat(get(urlEqualTo("/api/default/patients/" + VALID_HEALTH_ID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/P" + VALID_HEALTH_ID + ".json"))));
        Bundle bundle = loadFromXmlFile("dstu2/xmls/p98001046534_encounter_with_registration.xml");
        BundleContext context = new BundleContext(bundle, "shrEncounterId");
        processor.process(context.getEncounterCompositions().get(0));
        Patient patient = patientDao.findPatientById(VALID_HEALTH_ID);
        assertEquals(VALID_HEALTH_ID, patient.getHid());
        Date dateOfBirth = patient.getDateOfBirth();
        assertEquals("1970-09-18", new SimpleDateFormat("yyyy-MM-dd").format(dateOfBirth));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowRuntimeExceptionIfIncorrectUrl() throws Exception {
        givenThat(get(urlEqualTo("/api/default/patients/" + VALID_HEALTH_ID))
                .willReturn(aResponse()
                        .withStatus(404)));
        Bundle bundle = loadFromXmlFile("dstu1/xmls/sampleEncounter.xml");
        BundleContext context = new BundleContext(bundle, "shrEncounterId");
        processor.process(context.getEncounterCompositions().get(0));
    }
}