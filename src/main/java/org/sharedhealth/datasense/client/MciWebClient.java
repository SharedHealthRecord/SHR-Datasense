package org.sharedhealth.datasense.client;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.model.Patient;
import org.sharedhealth.datasense.util.MapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@Component
public class MciWebClient {

    private DatasenseProperties properties;
    private IdentityServiceClient identityServiceClient;
    private Logger log = Logger.getLogger(MciWebClient.class);

    @Autowired
    public MciWebClient(DatasenseProperties properties, IdentityServiceClient identityServiceClient) {
        this.properties = properties;
        this.identityServiceClient = identityServiceClient;
    }

    public Patient identifyPatient(String healthId) throws URISyntaxException, IOException {
        String response = getResponse(healthId);
        if (response != null) {
            return MapperUtil.readFrom(response, Patient.class);
        }
        return null;
    }

    private String getResponse(final String healthId) throws URISyntaxException, IOException {
        URI mciURI = getMciURI(healthId);
        log.info("Reading from " + mciURI);
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", getAuthHeader());
        headers.put("Accept", "application/json");
        String response = null;
        try {
            response = new WebClient().get(mciURI, headers);
        } catch (ConnectionException e) {
            log.error(String.format("Could not identify patient with healthId [%s]", healthId), e);
            if (e.getErrorCode() == 401) {
                identityServiceClient.clearToken();
            }
        }
        return response;
    }

    private String getAuthHeader() {
        String auth = properties.getMciUser() + ":" + properties.getMciPassword();
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("UTF-8")));
        return "Basic " + new String(encodedAuth);
    }

    private URI getMciURI(String healthId) throws URISyntaxException {
        return new URI(properties.getMciBaseUrl() + "/patients/" + healthId);
    }
}
