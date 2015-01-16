package org.sharedhealth.datasense.util;

import org.sharedhealth.datasense.config.DatasenseProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

import static org.sharedhealth.datasense.util.HttpUtil.getBase64Authentication;

@Component
public class DHISHeaderUtil {

    @Autowired
    private DatasenseProperties datasenseProperties;

    public HashMap<String, String> getDhisHeaders() {
        HashMap<String, String> postHeaders = new HashMap<>();
        String authentication = getBase64Authentication(datasenseProperties.getDhisUserName(), datasenseProperties.getDhisPassword());
        postHeaders.put("Authorization", authentication);
        postHeaders.put("Content-Type", "application/json");
        return postHeaders;
    }
}
