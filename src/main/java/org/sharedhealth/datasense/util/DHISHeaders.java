package org.sharedhealth.datasense.util;

import org.apache.commons.codec.binary.Base64;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class DHISHeaders {

    @Autowired
    private DatasenseProperties datasenseProperties;

    public static String getBase64Authentication(String userName, String password) {
        String credentials = userName + ":" + password;
        byte[] credentialBytes = Base64.encodeBase64(credentials.getBytes());
        return "Basic " + new String(credentialBytes);
    }

    public HashMap<String, String> get() {
        HashMap<String, String> postHeaders = new HashMap<>();
        String authentication = getBase64Authentication(datasenseProperties.getDhisUserName(), datasenseProperties
                .getDhisPassword());
        postHeaders.put("Authorization", authentication);
        postHeaders.put("Content-Type", "application/json");
        return postHeaders;
    }
}
