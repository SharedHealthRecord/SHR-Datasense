package org.sharedhealth.datasense.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.security.IdentityToken;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.datasense.util.HeaderUtil.*;

public class ShrWebClientTest {
    @Mock
    private IdentityServiceClient identityServiceClient;
    @Mock
    private DatasenseProperties properties;

    private ShrWebClient shrWebClient;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        shrWebClient = new ShrWebClient(identityServiceClient, properties);
    }

    @Test
    public void shouldGetShrFeedsWithHeadersAndValidToken() throws Exception {
        String clientId = "1234";
        String facilityID = "10000001";
        String email = "email@gmail.com";
        String uuid = UUID.randomUUID().toString();
        IdentityToken identityToken = new IdentityToken(uuid);
        String url = "http://localhost:9997/shr";
        URI uri = URI.create(url);
        String response = "Response";

        when(properties.getIdpClientId()).thenReturn(clientId);
        when(properties.getIdpClientEmail()).thenReturn(email);
        when(properties.getDatasenseFacilityId()).thenReturn(facilityID);
        when(identityServiceClient.getOrCreateToken()).thenReturn(identityToken);

        givenThat(get(urlEqualTo("/shr"))
                .withHeader(CLIENT_ID_KEY, equalTo(clientId))
                .withHeader(AUTH_TOKEN_KEY, equalTo(identityToken.toString()))
                .withHeader(FROM_KEY, equalTo(email))
                .withHeader("Accept", equalTo("application/atom+xml"))
                .withHeader("facilityId", equalTo(facilityID))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withBody(response)));

        String content = shrWebClient.getEncounterFeedContent(uri);

        assertEquals(response, content);
    }

    @Test(expected = IOException.class)
    public void shouldClearTheExpiredToken() throws Exception {
        String clientId = "1234";
        String facilityID = "10000001";
        String email = "email@gmail.com";
        String uuid = UUID.randomUUID().toString();
        IdentityToken identityToken = new IdentityToken(uuid);
        String url = "http://localhost:9997/shr";
        URI uri = URI.create(url);
        String response = "Response";

        when(properties.getIdpClientId()).thenReturn(clientId);
        when(properties.getIdpClientEmail()).thenReturn(email);
        when(properties.getDatasenseFacilityId()).thenReturn(facilityID);
        when(identityServiceClient.getOrCreateToken()).thenReturn(identityToken);

        givenThat(get(urlEqualTo("/shr"))
                .withHeader(CLIENT_ID_KEY, equalTo(clientId))
                .withHeader(AUTH_TOKEN_KEY, equalTo(identityToken.toString()))
                .withHeader(FROM_KEY, equalTo(email))
                .withHeader("Accept", equalTo("application/atom+xml"))
                .withHeader("facilityId", equalTo(facilityID))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.UNAUTHORIZED.value())));

        String content = shrWebClient.getEncounterFeedContent(uri);

        assertEquals(null, content);
        verify(identityServiceClient, times(1)).clearToken();
    }
}