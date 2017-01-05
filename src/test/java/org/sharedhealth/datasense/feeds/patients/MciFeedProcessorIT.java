package org.sharedhealth.datasense.feeds.patients;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.client.MciWebClient;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.feeds.transaction.AtomFeedSpringTransactionManager;
import org.sharedhealth.datasense.helpers.DatabaseHelper;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.sharedhealth.datasense.model.Address;
import org.sharedhealth.datasense.model.Encounter;
import org.sharedhealth.datasense.model.Facility;
import org.sharedhealth.datasense.model.Patient;
import org.sharedhealth.datasense.repository.EncounterDao;
import org.sharedhealth.datasense.repository.PatientDao;
import org.sharedhealth.datasense.security.IdentityStore;
import org.sharedhealth.datasense.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;
import static org.sharedhealth.datasense.helpers.ResourceHelper.asString;
import static org.sharedhealth.datasense.util.HeaderUtil.*;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class MciFeedProcessorIT {
    private MciFeedProcessor mciFeedProcessor;
    @Autowired
    private DataSourceTransactionManager txMgr;
    @Autowired
    private MciWebClient mciWebClient;
    @Autowired
    private PatientUpdateEventWorker patientUpdateEventWorker;
    @Autowired
    private DatasenseProperties datasenseProperties;

    @Autowired
    private PatientDao patientDao;
    @Autowired
    private EncounterDao encounterDao;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    @Autowired
    private IdentityStore identityStore;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);
    private String hid = "HID";
    private final UUID token = UUID.randomUUID();

    @Before
    public void setUp() throws Exception {
        mciFeedProcessor = new MciFeedProcessor("http://localhost:9997/api/v1/feed/patients", new AtomFeedSpringTransactionManager(txMgr),
                mciWebClient, patientUpdateEventWorker, datasenseProperties);

        String response = "{\"access_token\" : \"" + token.toString() + "\"}";

        givenThat(post(urlEqualTo("/signin"))
                .withHeader(CLIENT_ID_KEY, equalTo(datasenseProperties.getIdpClientId()))
                .withHeader(AUTH_TOKEN_KEY, equalTo(datasenseProperties.getIdpAuthToken()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(response)));

        givenThat(get(urlEqualTo("/api/default/feed/patients?last_marker=caed6a71-acaf-11e6-8bea-0050568276cf"))
                .withHeader(CLIENT_ID_KEY, equalTo(datasenseProperties.getIdpClientId()))
                .withHeader(FROM_KEY, equalTo(datasenseProperties.getIdpClientEmail()))
                .withHeader(AUTH_TOKEN_KEY, equalTo(token.toString()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(asString("feeds/blank_patient_feed.xml"))));
        identityStore.clearToken();
    }

    @After
    public void tearDown() throws Exception {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
        identityStore.clearToken();
    }

    @Test
    public void shouldUpdateAPatientWhenChangedInMCI() throws Exception {
        patientDao.save(createPatient());

        givenThat(get(urlEqualTo("/api/v1/feed/patients"))
                .withHeader(CLIENT_ID_KEY, equalTo(datasenseProperties.getIdpClientId()))
                .withHeader(FROM_KEY, equalTo(datasenseProperties.getIdpClientEmail()))
                .withHeader(AUTH_TOKEN_KEY, equalTo(token.toString()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(asString("feeds/mci_update_gender_address_dob_chnage.xml"))));

        mciFeedProcessor.process();

        Patient patient = patientDao.findPatientById(hid);
        assertEquals("F", patient.getGender());
        assertEquals(DateUtil.parseDate("1976-01-13T16:50:00.000+05:30"), patient.getDateOfBirth());
        assertEquals("303334", patient.getPresentLocationCode());
        assertEquals("ISSUED", patient.getHidCardStatus());
    }

    @Test
    public void shouldDeleteAMergedPatient() throws Exception {
        patientDao.save(createPatient());

        givenThat(get(urlEqualTo("/api/v1/feed/patients"))
                .withHeader(CLIENT_ID_KEY, equalTo(datasenseProperties.getIdpClientId()))
                .withHeader(FROM_KEY, equalTo(datasenseProperties.getIdpClientEmail()))
                .withHeader(AUTH_TOKEN_KEY, equalTo(token.toString()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(asString("feeds/mci_update_merged_patient.xml"))));

        mciFeedProcessor.process();

        Patient patient = patientDao.findPatientById(hid);
        assertNull(patient);
    }

    @Test
    public void shouldPutMergeEventAsFailedIfPatientStillHasEncounters() throws Exception {
        Patient createdPatient = createPatient();
        patientDao.save(createdPatient);
        Encounter encounter = new Encounter();
        encounter.setEncounterDateTime(new Date());
        encounter.setEncounterId("encId");
        encounter.setEncounterType("Consultation");
        encounter.setEncounterVisitType("OPD");
        encounter.setPatient(createdPatient);
        encounter.setLocationCode("302618");
        Facility facility = new Facility();
        facility.setFacilityId("1000041");
        encounter.setFacility(facility);
        encounterDao.save(encounter);

        givenThat(get(urlEqualTo("/api/v1/feed/patients"))
                .withHeader(CLIENT_ID_KEY, equalTo(datasenseProperties.getIdpClientId()))
                .withHeader(FROM_KEY, equalTo(datasenseProperties.getIdpClientEmail()))
                .withHeader(AUTH_TOKEN_KEY, equalTo(token.toString()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(asString("feeds/mci_update_merged_patient.xml"))));

        mciFeedProcessor.process();

        List<LoggedRequest> all = findAll(getRequestedFor(urlMatching("/")));
        System.out.println(all.size());

        Patient patient = patientDao.findPatientById(hid);
        assertNotNull(patient);

        jdbcTemplate.query("select * from failed_events", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                assertEquals("http://localhost:9997/api/v1/feed/patients", rs.getString("feed_uri"));
                assertEquals("caed6a71-acaf-11e6-8bea-0050568276cf", rs.getString("event_id"));
                assertEquals("patient updated", rs.getString("title"));
            }
        });
    }

    private Patient createPatient() {
        Patient patient = new Patient();
        patient.setHid(hid);
        patient.setDateOfBirth(DateUtil.parseDate("1976-01-12T16:50:00.000+05:30"));
        patient.setGender("M");
        patient.setHidCardStatus("REGISTERED");
        Address address = createAddress("1st lane", "30", "26", "07");
        patient.setPresentAddress(address);
        patient.setPresentAddressCode("302607");
        return patient;
    }

    private Address createAddress(String addressLine, String divisionId, String districtId, String upazilaId) {
        Address address = new Address();
        address.setAddressLine(addressLine);
        address.setDivisionId(divisionId);
        address.setDistrictId(districtId);
        address.setUpazilaId(upazilaId);
        return address;
    }

}