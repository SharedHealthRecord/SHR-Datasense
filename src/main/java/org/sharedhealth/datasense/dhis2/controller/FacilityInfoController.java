package org.sharedhealth.datasense.dhis2.controller;

import org.sharedhealth.datasense.dhis2.model.MetadataConfig;
import org.sharedhealth.datasense.dhis2.service.FacilityInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;

@Controller
@RequestMapping(value = "/facilityInfo")
public class FacilityInfoController {
    @Autowired
    FacilityInfoService metaDataService;

    @Autowired
    FacilityInfoService facilityDataService;

    @RequestMapping(value = "/encounter", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public
    @ResponseBody
    Object showLastEncounter(@RequestBody MetadataConfig config) {
        return metaDataService.getLastEncounter(config);
    }

    @RequestMapping(value = "/searchByName", method = RequestMethod.GET, produces= MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public @ResponseBody
    Object searchFacilityByName(@RequestParam(value = "name") String name) throws IOException, URISyntaxException {
        return facilityDataService.getAvailableFacilitiesBYName(name);
    }

    @RequestMapping(value = "/searchById", method = RequestMethod.GET, produces= MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public @ResponseBody
    Object searchFacilityById(@RequestParam(value = "id") String id) throws IOException, URISyntaxException {
        return facilityDataService.getAvailableFacilitiesById(id);
    }
}
