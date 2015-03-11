package org.sharedhealth.datasense.util;

import org.apache.commons.codec.binary.Base64;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.security.IdentityToken;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class HeaderUtil {

    public static final String AUTH_TOKEN_KEY = "X-Auth-Token";
    public static final String CLIENT_ID_KEY = "client_id";
    public static final String FROM_KEY = "From";
    public static final String URL_SEPARATOR_FOR_CONTEXT_PATH = "/";

    public static String getBase64Authentication(String userName, String password) {
        String credentials = userName + ":" + password;
        byte[] credentialBytes = Base64.encodeBase64(credentials.getBytes());
        return "Basic " + new String(credentialBytes);
    }

    public static HashMap<String, String> getDhisHeaders(DatasenseProperties datasenseProperties) {
        HashMap<String, String> postHeaders = new HashMap<>();
        String authentication = getBase64Authentication(datasenseProperties.getDhisUserName(), datasenseProperties
                .getDhisPassword());
        postHeaders.put("Authorization", authentication);
        postHeaders.put("Content-Type", "application/json");
        return postHeaders;
    }

    public static Map<String, String> getHrmAccessTokenHeaders(IdentityToken accessToken, DatasenseProperties datasenseProperties) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(CLIENT_ID_KEY, datasenseProperties.getIdpClientId());
        headers.put(AUTH_TOKEN_KEY, accessToken.toString());
        headers.put(FROM_KEY, datasenseProperties.getIdpClientEmail());
        return headers;
    }

    public static Map<String, String> getHrmAuthTokenHeaders(DatasenseProperties datasenseProperties) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(CLIENT_ID_KEY, datasenseProperties.getIdpClientId());
        headers.put(AUTH_TOKEN_KEY, datasenseProperties.getIdpAuthToken());
        return headers;
    }
}
