package org.sharedhealth.datasense.client;

import org.apache.http.entity.StringEntity;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.dhis2.model.DHISResponse;
import org.sharedhealth.datasense.util.HeaderUtil;
import org.sharedhealth.datasense.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

    public DHISResponse post(String content) throws UnsupportedEncodingException {
        String dataValueSetsUri = StringUtil.removeSuffix(properties.getDhisBaseUrl(), "/") + "/api/dataValueSets";
        HashMap<String, String> dhisHeaders = HeaderUtil.getDhisHeaders(properties);
        String response = new WebClient().post(dataValueSetsUri, dhisHeaders, new StringEntity(content));
        return new DHISResponse(response);
    }
}
