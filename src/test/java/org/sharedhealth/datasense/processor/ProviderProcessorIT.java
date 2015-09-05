package org.sharedhealth.datasense.processor;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.helpers.DatabaseHelper;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.sharedhealth.datasense.model.Provider;
import org.sharedhealth.datasense.model.fhir.BundleContext;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.repository.ProviderDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.sharedhealth.datasense.helpers.ResourceHelper.asString;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class ProviderProcessorIT {
    private static final String VALID_PROVIDER_ID = "19";
    @Autowired
    private ProviderProcessor providerProcessor;
    @Autowired
    private ProviderDao providerDao;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);
    private EncounterComposition encounterComposition;

    @Before
    public void setUp() throws Exception {
        Bundle bundle = loadFromXmlFile("dstu2/xmls/p98001046534_encounter_with_registration.xml");
        BundleContext context = new BundleContext(bundle, "shrEncounterId");
        encounterComposition = context.getEncounterCompositions().get(0);
    }

    @After
    public void tearDown() throws Exception {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }

    @Test
    public void shouldProcessAndSaveProvider() throws Exception {
        givenThat(get(urlEqualTo("/api/1.0/providers/" + VALID_PROVIDER_ID + ".json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(asString("jsons/P" + VALID_PROVIDER_ID + ".json"))));
        String facilityId = providerProcessor.process(encounterComposition);
        assertEquals("1302", facilityId);
        Provider provider = providerDao.findProviderById(VALID_PROVIDER_ID);
        assertEquals("Test", provider.getName());
        assertEquals("1302", provider.getFacilityId());
    }

    @Test
    public void shouldFetchFromDatabaseIfProviderAlreadyPresent() throws Exception {
        jdbcTemplate.update("insert into provider (id, name, facility_id) values ('19', 'Some Test Name', 'Some Facility');", new EmptySqlParameterSource());
        String facilityId = providerProcessor.process(encounterComposition);
        assertEquals("Some Facility", facilityId);
        Provider provider = providerDao.findProviderById(VALID_PROVIDER_ID);
        assertEquals("Some Test Name", provider.getName());
        assertEquals("Some Facility", provider.getFacilityId());
    }

    @Test
    public void shouldReturnNullIfNoProviderFound() throws Exception {
        givenThat(get(urlEqualTo("/api/1.0/providers/19.json"))
                .willReturn(aResponse()
                        .withStatus(404)));
        String facilityId = providerProcessor.process(encounterComposition);
        assertNull(facilityId);
    }
}