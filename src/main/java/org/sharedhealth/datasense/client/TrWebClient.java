package org.sharedhealth.datasense.client;

import org.apache.log4j.Logger;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.model.tr.TrMedication;
import org.sharedhealth.datasense.util.MapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.sharedhealth.datasense.util.HttpUtil.getBase64Authentication;

@Component
public class TrWebClient {
    @Autowired
    private DatasenseProperties datasenseProperties;
    private Logger log = Logger.getLogger(TrWebClient.class);

    public TrMedication getTrMedication(String uri) throws IOException, URISyntaxException {
        String response = getResponse(uri);
        return response != null ? MapperUtil.readFrom(response, TrMedication.class) : null;
    }

    public String getResponse(String uri) throws IOException, URISyntaxException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", getBase64Authentication(datasenseProperties.getTrUser(), datasenseProperties.getTrPassword()));
        headers.put("Accept", "application/json");
        String response;
        try {
            response = new WebClient().get(new URI(uri), headers);
        } catch (ConnectionException e) {
            log.error(String.format("Could not fetch feed for URI [%s]", uri.toString()), e);
            throw new IOException(e);
        }
        return response;
    }
}
