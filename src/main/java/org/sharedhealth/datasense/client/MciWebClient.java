package org.sharedhealth.datasense.client;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.model.Patient;
import org.sharedhealth.datasense.util.MapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

@Component
public class MciWebClient {

    private DatasenseProperties properties;

    @Autowired
    public MciWebClient(DatasenseProperties properties) {
        this.properties = properties;
    }

    public Patient identifyPatient(String healthId) throws URISyntaxException, IOException {
        return MapperUtil.readFrom(getResponse(healthId), Patient.class);
    }

    private String getResponse(String healthId) throws URISyntaxException, IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        URI mciURI = getMciURI(healthId);
        HttpGet request = new HttpGet(mciURI);
        request.addHeader("Authorization", getAuthHeader());
        request.addHeader("Accept", "application/json");
        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
            public String handleResponse(final HttpResponse response) throws IOException {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else if (status == 404) {
                    return null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            }
        };
        try {
            return httpClient.execute(request, responseHandler);
        } catch (IOException e) {
            throw new RuntimeException("Unable to connect to MCI server. [" + mciURI +"]", e);
        } finally {
            httpClient.close();
        }
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
