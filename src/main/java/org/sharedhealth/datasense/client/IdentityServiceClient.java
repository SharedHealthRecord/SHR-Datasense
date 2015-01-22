package org.sharedhealth.datasense.client;

import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.security.Identity;
import org.sharedhealth.datasense.security.IdentityStore;
import org.sharedhealth.datasense.security.IdentityToken;
import org.sharedhealth.datasense.util.MapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class IdentityServiceClient {
    private DatasenseProperties properties;
    private IdentityStore identityStore;

    @Autowired
    public IdentityServiceClient(DatasenseProperties properties, IdentityStore identityStore) {
        this.properties = properties;
        this.identityStore = identityStore;
    }

    public IdentityToken getOrCreateToken() throws IOException {
        IdentityToken token = identityStore.getToken();
        if (token == null) {
            Identity identity = new Identity(properties.getIdentityUser(), properties.getIdentityPassword());
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("accept", "application/json");
            String response = new WebClient().post(getIdentityServerUrl(), identity, headers);
            token = MapperUtil.readFrom(response, IdentityToken.class);
            identityStore.setToken(token);
        }
        return token;
    }

    public void clearToken() {
        identityStore.clearToken();
    }

    private String getIdentityServerUrl() {
        return properties.getIdentityServerBaseUrl() + "/login";
    }
}
