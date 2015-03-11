package org.sharedhealth.datasense.client;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.model.Provider;
import org.sharedhealth.datasense.model.fhir.ProviderReference;
import org.sharedhealth.datasense.util.HeaderUtil;
import org.sharedhealth.datasense.util.MapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.sharedhealth.datasense.util.HeaderUtil.getHrmAuthTokenHeaders;

@Component
public class ProviderWebClient {
    private DatasenseProperties properties;

    private Logger log = Logger.getLogger(ProviderWebClient.class);

    @Autowired
    public ProviderWebClient(DatasenseProperties datasenseProperties) {
        this.properties = datasenseProperties;
    }

    public Provider findById(String providerId) throws IOException, URISyntaxException {
        String response = getResponse(providerId);
        if (StringUtils.isBlank(response)) return null;
        Map providerMap = MapperUtil.readFrom(response, Map.class);
        return toProvider(providerMap);
    }

    private Provider toProvider(Map providerMap) {
        Provider provider = new Provider();
        provider.setId(providerMap.get("id").toString());
        provider.setName((String) providerMap.get("name"));
        Map organization = (Map) providerMap.get("organization");
        if(organization != null) {
            String reference = (String) organization.get("reference");
            provider.setFacilityId(ProviderReference.parseUrl(reference));
        }
        return provider;
    }

    private String getResponse(String facilityId) throws URISyntaxException, IOException {
        URI providerUrl = getProviderUrl(facilityId);
        log.info("Reading from " + providerUrl);
        Map<String, String> headers = getHrmAuthTokenHeaders(properties);
        headers.put("Accept", "application/json");
        return new WebClient().get(providerUrl, headers);
    }

    private URI getProviderUrl(String providerId) throws URISyntaxException {
        return new URI(properties.getPrProviderUrl() + "/" + providerId + ".json");
    }
}
