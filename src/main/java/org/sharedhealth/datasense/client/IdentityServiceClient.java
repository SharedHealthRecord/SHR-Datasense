package org.sharedhealth.datasense.client;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.security.IdentityStore;
import org.sharedhealth.datasense.security.IdentityToken;
import org.sharedhealth.datasense.util.MapperUtil;
import org.sharedhealth.datasense.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.sharedhealth.datasense.util.HeaderUtil.URL_SEPARATOR;
import static org.sharedhealth.datasense.util.HeaderUtil.getHrmAuthTokenHeaders;

@Component
public class IdentityServiceClient {
    private static final String EMAIL_KEY = "email";
    private static final String PASSWORD_KEY = "password";
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
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(getNameValuePairs());
            Map<String, String> headers = getHrmAuthTokenHeaders(properties);
            headers.put("accept", "application/json");
            String loginUrl = StringUtil.removeSuffix(properties.getIdentityServerLoginUrl(), URL_SEPARATOR);
            String response = new WebClient().post(loginUrl, headers, entity);
            token = MapperUtil.readFrom(response, IdentityToken.class);
            identityStore.setToken(token);
        }
        return token;
    }

    private List<? extends NameValuePair> getNameValuePairs() {
        ArrayList<BasicNameValuePair> valuePairs = new ArrayList<>();
        valuePairs.add(new BasicNameValuePair(EMAIL_KEY, properties.getIdpClientEmail()));
        valuePairs.add(new BasicNameValuePair(PASSWORD_KEY, properties.getIdpClientPassword()));
        return valuePairs;
    }

    public void clearToken() {
        identityStore.clearToken();
    }

}
