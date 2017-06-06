package org.sharedhealth.datasense.processor;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.hl7.fhir.dstu3.model.Bundle;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.sharedhealth.datasense.client.FacilityWebClient;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.helpers.DatabaseHelper;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.sharedhealth.datasense.model.Facility;
import org.sharedhealth.datasense.model.fhir.BundleContext;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.repository.FacilityDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.datasense.helpers.ResourceHelper.asString;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class ServiceProviderProcessorIT {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    @Autowired
    private FacilityDao facilityDao;
    @Autowired
    private FacilityWebClient webClient;
    @Autowired
    @Mock
    private ProviderProcessor providerProcessor;
    @Mock
    private DatasenseProperties datasenseProperties;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);
    private static String VALID_FACILITY_ID = "10019841";
    private ServiceProviderProcessor processor;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        processor = new ServiceProviderProcessor(null, facilityDao, webClient, providerProcessor, datasenseProperties);
    }

    @After
    public void tearDown() {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }

    @Test
    public void shouldDownloadAndSaveFacility() throws Exception {
        Bundle bundle = loadFromXmlFile("stu3/p98001046534_encounter_with_registration.xml");
        BundleContext context = new BundleContext(bundle, "shrEncounterId");

        givenThat(get(urlEqualTo("/api/1.0/facilities/" + VALID_FACILITY_ID + ".json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/F" + VALID_FACILITY_ID + ".json"))));

        when(datasenseProperties.getCloudHostedFacilityIds()).thenReturn(asList("12345"));

        processor.process(context.getEncounterCompositions().get(0));

        Facility facility = facilityDao.findFacilityById(VALID_FACILITY_ID);
        assertNotNull(facility);
        assertEquals("Dohar Upazila Health Complex", facility.getFacilityName());
        assertEquals("Upazila Health Complex", facility.getFacilityType());
        assertEquals("302618", facility.getFacilityLocationCode());
    }

    @Test
    public void shouldNotDownloadFacilityIfAlreadyPresent() throws Exception {
        Bundle bundle = loadFromXmlFile("stu3/p98001046534_encounter_with_registration.xml");
        BundleContext context = new BundleContext(bundle, "shrEncounterId");

        jdbcTemplate.update("insert into facility (facility_id, name, type, location_id) " +
                "values ('" + VALID_FACILITY_ID + "', 'Test Facility', 'Test Facility Type', '302618');", new EmptySqlParameterSource());

        when(datasenseProperties.getCloudHostedFacilityIds()).thenReturn(asList("12345"));

        processor.process(context.getEncounterCompositions().get(0));

        Facility facility = facilityDao.findFacilityById(VALID_FACILITY_ID);
        assertNotNull(facility);
        assertEquals("Test Facility", facility.getFacilityName());
        assertEquals("Test Facility Type", facility.getFacilityType());
        assertEquals("302618", facility.getFacilityLocationCode());
    }

    @Test
    public void shouldFetchFacilityFromProviderIfNotPresentInEncounter() throws Exception {
        Bundle bundle = loadFromXmlFile("stu3/p98001046534_encounter_without_serviceProvider.xml");
        BundleContext context = new BundleContext(bundle, "shrEncounterId");
        jdbcTemplate.update("insert into facility (facility_id, name, type, location_id) " +
                "values ('" + VALID_FACILITY_ID + "', 'Test Facility', 'Test Facility Type', '302618');", new EmptySqlParameterSource());

        when(datasenseProperties.getCloudHostedFacilityIds()).thenReturn(asList("12345"));
        when(providerProcessor.process(any(EncounterComposition.class))).thenReturn(VALID_FACILITY_ID);

        processor.process(context.getEncounterCompositions().get(0));

        Facility facility = facilityDao.findFacilityById(VALID_FACILITY_ID);
        assertNotNull(facility);
        assertEquals("Test Facility", facility.getFacilityName());
        assertEquals("Test Facility Type", facility.getFacilityType());
        assertEquals("302618", facility.getFacilityLocationCode());
    }

    @Test
    public void shouldDownloadProviderFacilityIfNotPresent() throws Exception {
        Bundle bundle = loadFromXmlFile("stu3/p98001046534_encounter_without_serviceProvider.xml");
        BundleContext context = new BundleContext(bundle, "shrEncounterId");

        givenThat(get(urlEqualTo("/api/1.0/facilities/" + VALID_FACILITY_ID + ".json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/F" + VALID_FACILITY_ID + ".json"))));

        when(providerProcessor.process(any(EncounterComposition.class))).thenReturn(VALID_FACILITY_ID);

        processor.process(context.getEncounterCompositions().get(0));

        Facility facility = facilityDao.findFacilityById(VALID_FACILITY_ID);
        assertNotNull(facility);
        assertEquals("Dohar Upazila Health Complex", facility.getFacilityName());
        assertEquals("Upazila Health Complex", facility.getFacilityType());
        assertEquals("302618", facility.getFacilityLocationCode());
    }

    @Test
    @Ignore
    public void shouldFetchFacilityOfProviderWhenServiceProviderIsBahmniOnCloud() throws Exception {
        String facilityIdOfProvider = "10000059";
        Bundle bundle = loadFromXmlFile("stu3/p98001046534_encounter_without_serviceProvider.xml");
        BundleContext context = new BundleContext(bundle, "shrEncounterId");

        givenThat(get(urlEqualTo("/api/1.0/facilities/" + facilityIdOfProvider + ".json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/F" + facilityIdOfProvider + ".json"))));

        givenThat(get(urlEqualTo("/api/1.0/facilities/" + VALID_FACILITY_ID + ".json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/F" + VALID_FACILITY_ID + ".json"))));

        when(datasenseProperties.getCloudHostedFacilityIds()).thenReturn(asList(VALID_FACILITY_ID));
        when(providerProcessor.process(any(EncounterComposition.class))).thenReturn(facilityIdOfProvider);

        processor.process(context.getEncounterCompositions().get(0));

        Facility facility = facilityDao.findFacilityById(facilityIdOfProvider);
        assertNotNull(facility);
        assertEquals("Test:Amta Union Sub Center", facility.getFacilityName());
        assertEquals("Union Sub-center", facility.getFacilityType());
        assertEquals("302614", facility.getFacilityLocationCode());
    }
}