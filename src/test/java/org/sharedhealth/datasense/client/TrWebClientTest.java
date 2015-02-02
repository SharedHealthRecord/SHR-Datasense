package org.sharedhealth.datasense.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.model.tr.TrConcept;
import org.sharedhealth.datasense.model.tr.TrMedication;
import org.sharedhealth.datasense.model.tr.TrReferenceTerm;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.datasense.helpers.ResourceHelper.asString;

public class TrWebClientTest {
    private final static String CONCEPT_UUID = "575ee049-bc0d-459c-9e19-07f1151fe0d6";
    private final static String REFERENCE_TERM_UUID = "66b81088-adce-4432-83b7-46fbc14ffa85";
    private final static String DRUG_UUID = "28c3c784-c0bf-4cae-bd26-ca76a384085a";
    private final static String TR_CONCEPT_URL = "/openmrs/ws/rest/v1/tr/concepts/";
    private final static String TR_REFERENCE_TERM_URL = "/openmrs/ws/rest/v1/tr/referenceterms/";
    private final static String TR_MEDICATION_URL = "/openmrs/ws/rest/v1/tr/drugs/";
    @Mock
    private DatasenseProperties datasenseProperties;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);
    private TrWebClient trWebClient;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        trWebClient = new TrWebClient(datasenseProperties);

        when(datasenseProperties.getTrUser()).thenReturn("admin");
        when(datasenseProperties.getTrPassword()).thenReturn("password");
    }

    @Test
    public void shouldGetTrConceptFromFeed() throws Exception {
        givenThat(get(urlMatching(TR_CONCEPT_URL + CONCEPT_UUID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(asString("jsons/C" + CONCEPT_UUID + ".json"))));

        TrConcept trConcept = trWebClient.getTrConcept("http://localhost:9997"+ TR_CONCEPT_URL + CONCEPT_UUID);
        assertNotNull(trConcept);
        assertEquals(CONCEPT_UUID, trConcept.getConceptUuid());
        assertEquals("C Set03", trConcept.getName());
        assertEquals("Symptom", trConcept.getConceptClass());
        assertEquals(1, trConcept.getReferenceTermMaps().size());

        TrReferenceTerm trReferenceTerm = trConcept.getReferenceTermMaps().get(0);
        assertEquals("Back pain 186289", trReferenceTerm.getName());
        assertEquals("M54.186289", trReferenceTerm.getCode());
        assertEquals(REFERENCE_TERM_UUID, trReferenceTerm.getReferenceTermUuid());
        assertEquals("ICD10-BD", trReferenceTerm.getSource());
        assertEquals("SAME-AS", trReferenceTerm.getRelationshipType());
    }

    @Test
    public void shouldGetTrReferenceTermFromFeed() throws Exception {
        givenThat(get(urlMatching(TR_REFERENCE_TERM_URL + REFERENCE_TERM_UUID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(asString("jsons/R" + REFERENCE_TERM_UUID + ".json"))));

        TrReferenceTerm trReferenceTerm = trWebClient.getTrReferenceTerm("http://localhost:9997" + TR_REFERENCE_TERM_URL + REFERENCE_TERM_UUID);
        assertEquals("Back pain 186289", trReferenceTerm.getName());
        assertEquals("M54.186289", trReferenceTerm.getCode());
        assertEquals(REFERENCE_TERM_UUID, trReferenceTerm.getReferenceTermUuid());
        assertEquals("ICD10-BD", trReferenceTerm.getSource());
    }

    @Test
    public void shouldGetTrMedicationFromFeed() throws Exception {

        givenThat(get(urlMatching(TR_MEDICATION_URL + DRUG_UUID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(asString("jsons/M" + DRUG_UUID + ".json"))));

        TrMedication trMedication = trWebClient.getTrMedication("http://localhost:9997" + TR_MEDICATION_URL + DRUG_UUID);

        assertEquals("OPV 1",trMedication.getName());
        assertEquals(DRUG_UUID,trMedication.getUuid());
        assertEquals("J07BF01",trMedication.getReferenceCode());
        assertEquals("9d770880-fd65-43f5-a7b7-2fb7b6a4037a",trMedication.getConceptId());
        assertTrue(trMedication.getRetired());

    }
}