package org.sharedhealth.datasense.client;

import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.dhis2.model.DHISResponse;
import org.sharedhealth.datasense.util.HeaderUtil;
import org.sharedhealth.datasense.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

@Component
public class DHIS2Client {

    @Autowired
    DatasenseProperties properties;

    public DHISResponse get(String resourceUrl) {
        String uri = StringUtil.ensureSuffix(properties.getDhisBaseUrl(), "/") + StringUtil.removePrefix(resourceUrl,"/");
        HashMap<String, String> dhisHeaders = HeaderUtil.getDhisHeaders(properties);
        try {
            String remoteResponse = new WebClient().get(new URI(uri), dhisHeaders);
            return new DHISResponse(remoteResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }
}
