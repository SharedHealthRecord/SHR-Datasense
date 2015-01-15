package org.sharedhealth.datasense.util;

import org.apache.commons.codec.binary.Base64;

public class HttpUtil {
    public static String getBase64Authentication(String userName, String password) {
        String credentials = userName + ":" + password;
        byte[] credentialBytes = Base64.encodeBase64(credentials.getBytes());
        return  "Basic " + new String(credentialBytes);
    }
}
