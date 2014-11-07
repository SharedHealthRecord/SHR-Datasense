package org.sharedhealth.datasense.client;


import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.model.Address;
import org.sharedhealth.datasense.model.Facility;
import org.sharedhealth.datasense.util.MapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class FacilityWebClient {

    private DatasenseProperties properties;

    @Autowired
    public FacilityWebClient(DatasenseProperties properties) {
        this.properties = properties;
    }

    public Facility findById(String facilityId) throws IOException, URISyntaxException {
        String response = getResponse(facilityId);
        if (StringUtils.isBlank(response)) return null;
        Map facilityMap = MapperUtil.readFrom(response, Map.class);
        return toFacility(facilityMap);
    }

    private Facility toFacility(Map facilityMap) {
        Facility facility = new Facility();
        facility.setFacilityId((String) facilityMap.get("id"));
        facility.setFacilityName((String) facilityMap.get("name"));
        LinkedHashMap properties = (LinkedHashMap) facilityMap.get("properties");
        facility.setFacilityType((String) properties.get("org_type"));
        LinkedHashMap locations = (LinkedHashMap) properties.get("locations");
        facility.setFacilityLocation(getLocationAddress(locations));
        return facility;
    }

    private Address getLocationAddress(LinkedHashMap locations) {
        Address address = new Address();
        address.setDistrictId((String) locations.get("district_code"));
        address.setDivisionId((String) locations.get("division_code"));
        address.setUpazillaId((String) locations.get("upazila_code"));
        address.setCityCorporationId((String) locations.get("paurasava_code"));
        address.setUnionId((String) locations.get("union_code"));
        address.setWardId((String) locations.get("ward_code"));
        return address;
    }

    private String getResponse(String facilityId) throws URISyntaxException, IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(getFacilityUrl(facilityId));
        request.addHeader("X-Auth-Token", properties.getFacilityAuthToken());
        request.addHeader("Accept", "application/json");
        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
            public String handleResponse(final HttpResponse response) throws IOException {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else if (status == 404) {
                    return null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            }
        };
        try {
            return httpClient.execute(request, responseHandler);
        } finally {
            httpClient.close();
        }
    }

    private String getFacilityUrl(String facilityId) {
        return properties.getFacilityRegistryUrl() + "/" + facilityId + ".json";
    }

}
