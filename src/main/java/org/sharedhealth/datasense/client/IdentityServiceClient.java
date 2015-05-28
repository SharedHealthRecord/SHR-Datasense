package org.sharedhealth.datasense.client;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.security.IdentityStore;
import org.sharedhealth.datasense.security.IdentityToken;
import org.sharedhealth.datasense.security.TokenAuthentication;
import org.sharedhealth.datasense.security.UserInfo;
import org.sharedhealth.datasense.util.MapperUtil;
import org.sharedhealth.datasense.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
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

    public IdentityToken authenticateUser(String username, String password) throws IOException {
        ArrayList<BasicNameValuePair> valuePairs = new ArrayList<>();
        valuePairs.add(new BasicNameValuePair(EMAIL_KEY, username));
        valuePairs.add(new BasicNameValuePair(PASSWORD_KEY, password));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(valuePairs);
        Map<String, String> headers = getHrmAuthTokenHeaders(properties);
        headers.put("accept", "application/json");
        String loginUrl = StringUtil.removeSuffix(properties.getIdentityServerLoginUrl(), URL_SEPARATOR);
        try {
            String response = new WebClient().post(loginUrl, headers, entity);
            if (!StringUtils.isBlank(response)) {
                return MapperUtil.readFrom(response, IdentityToken.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void clearToken() {
        identityStore.clearToken();
    }

    public UserInfo getUserInfo(IdentityToken tokenForUser) {
        try {
            String userInfoUrl = StringUtil.ensureSuffix(properties.getIdpServerUserInfoUrl(), URL_SEPARATOR) + tokenForUser.toString();
            String response = new WebClient().get(new URI(userInfoUrl),getHrmAuthTokenHeaders(properties));
            return MapperUtil.readFrom(response, UserInfo.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new AuthenticationServiceException("Unable to authenticate user.");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new AuthenticationServiceException("Unable to authenticate user.");
        } catch (Exception e) {
            System.out.println(String.format("Error while validating client %s", tokenForUser.toString()));
            throw new AuthenticationServiceException("Unable to authenticate user.");
        }

    }
}
