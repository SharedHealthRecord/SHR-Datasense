package org.sharedhealth.datasense.client;

import org.apache.log4j.Logger;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.model.Patient;
import org.sharedhealth.datasense.util.MapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static org.sharedhealth.datasense.util.HeaderUtil.getHrmAccessTokenHeaders;

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
        Map<String, String> headers = getHrmAccessTokenHeaders(identityServiceClient.getOrCreateToken(), properties);
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

    private URI getMciURI(String healthId) throws URISyntaxException {
        return new URI(properties.getMciPatientUrl() + "/" + healthId);
    }
}
