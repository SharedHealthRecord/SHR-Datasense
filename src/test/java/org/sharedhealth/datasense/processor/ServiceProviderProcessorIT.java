package org.sharedhealth.datasense.processor;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.hl7.fhir.instance.formats.ParserBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.client.FacilityWebClient;
import org.sharedhealth.datasense.helpers.DatabaseHelper;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.sharedhealth.datasense.model.Facility;
import org.sharedhealth.datasense.model.fhir.BundleContext;
import org.sharedhealth.datasense.repository.FacilityDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
    @Qualifier("dhisFacilitiesMap")
    private PropertiesFactoryBean dhisFacilitiesMap;
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    private static String VALID_FACILITY_ID = "10000069";
    private ServiceProviderProcessor processor;

    @Before
    public void setUp() throws Exception {
        processor = new ServiceProviderProcessor(null, facilityDao, webClient, dhisFacilitiesMap);
    }

    @After
    public void tearDown() {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }

    @Test
    public void shouldDownloadAndSaveFacility() throws Exception {
        givenThat(get(urlEqualTo("/api/1.0/facilities/" + VALID_FACILITY_ID + ".json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/F" + VALID_FACILITY_ID + ".json"))));
        ParserBase.ResourceOrFeed resourceOrFeed = loadFromXmlFile("xmls/sampleEncounter.xml");
        BundleContext context = new BundleContext(resourceOrFeed.getFeed(), "shrEncounterId");
        processor.process(context.getEncounterCompositions().get(0));
        Facility facility = facilityDao.findFacilityById(VALID_FACILITY_ID);
        assertNotNull(facility);
        assertEquals("Dohar Upazila Health Complex", facility.getFacilityName());
        assertEquals("Upazila Health Complex", facility.getFacilityType());
        assertEquals("302618", facility.getFacilityLocationCode());
        assertEquals("nRm6mKjJsaE", facility.getDhisOrgUnitUid());
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionIfIncorrectUrl() throws Exception {
        givenThat(get(urlEqualTo("/api/1.0/facilities/" + VALID_FACILITY_ID + ".json"))
                .willReturn(aResponse()
                        .withStatus(500)));
        ParserBase.ResourceOrFeed resourceOrFeed = loadFromXmlFile("xmls/sampleEncounter.xml");
        BundleContext context = new BundleContext(resourceOrFeed.getFeed(), "shrEncounterId");
        processor.process(context.getEncounterCompositions().get(0));
    }
}