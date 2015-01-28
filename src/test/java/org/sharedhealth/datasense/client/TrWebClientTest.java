package org.sharedhealth.datasense.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.model.tr.TrConcept;
import org.sharedhealth.datasense.model.tr.TrReferenceTerm;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.datasense.helpers.ResourceHelper.asString;

public class TrWebClientTest {
    private final static String CONCEPT_UUID = "575ee049-bc0d-459c-9e19-07f1151fe0d6";
    private final static String TR_CONCEPT_URL = "/openmrs/ws/rest/v1/tr/concepts/";
    @Mock
    private DatasenseProperties datasenseProperties;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);
    private TrWebClient trWebClient;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        trWebClient = new TrWebClient(datasenseProperties);
        givenThat(get(urlMatching(TR_CONCEPT_URL + CONCEPT_UUID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(asString("jsons/C" + CONCEPT_UUID + ".json"))));
        when(datasenseProperties.getTrUser()).thenReturn("admin");
        when(datasenseProperties.getTrPassword()).thenReturn("password");
    }

    @Test
    public void shouldGetTrConceptFromFeed() throws Exception {
        TrConcept trConcept = trWebClient.getTrConcept("http://localhost:9997"+ TR_CONCEPT_URL + CONCEPT_UUID);
        assertNotNull(trConcept);
        assertEquals(CONCEPT_UUID, trConcept.getConceptUuid());
        assertEquals("C Set03", trConcept.getName());
        assertEquals("Symptom", trConcept.getConceptClass());
        assertEquals(1, trConcept.getReferenceTermMaps().size());

        TrReferenceTerm trReferenceTerm = trConcept.getReferenceTermMaps().get(0);
        assertEquals("Back pain 186289", trReferenceTerm.getName());
        assertEquals("M54.186289", trReferenceTerm.getCode());
        assertEquals("66b81088-adce-4432-83b7-46fbc14ffa85", trReferenceTerm.getReferenceTermUuid());
        assertEquals("ICD10-BD", trReferenceTerm.getSource());
        assertEquals("SAME-AS", trReferenceTerm.getRelationshipType());
    }
}