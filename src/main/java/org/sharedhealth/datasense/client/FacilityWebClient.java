package org.sharedhealth.datasense.client;


import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.model.Address;
import org.sharedhealth.datasense.model.Facility;
import org.sharedhealth.datasense.util.HeaderUtil;
import org.sharedhealth.datasense.util.MapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.sharedhealth.datasense.util.HeaderUtil.getHrmAuthTokenHeaders;

@Component
public class FacilityWebClient {

    private DatasenseProperties properties;

    private Logger log = Logger.getLogger(FacilityWebClient.class);

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
        address.setUpazilaId((String) locations.get("upazila_code"));
        address.setCityCorporationId((String) locations.get("paurasava_code"));
        address.setUnionId((String) locations.get("union_code"));
        address.setUnionOrUrbanWardId((String) locations.get("ward_code"));
        return address;
    }

    private String getResponse(String facilityId) throws URISyntaxException, IOException {
        URI facilityUrl = getFacilityUrl(facilityId);
        log.info("Reading from " + facilityUrl);
        Map<String, String> headers = getHrmAuthTokenHeaders(properties);
        headers.put("Accept", "application/json");
        return new WebClient().get(facilityUrl, headers);
    }

    private URI getFacilityUrl(String facilityId) throws URISyntaxException {
        return new URI(properties.getFacilityRegistryUrl() + "/" + facilityId + ".json");
    }
}
