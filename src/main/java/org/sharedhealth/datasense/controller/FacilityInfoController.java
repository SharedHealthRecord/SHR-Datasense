package org.sharedhealth.datasense.controller;

import org.sharedhealth.datasense.service.FacilityInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/facility")
public class FacilityInfoController {
    @Autowired
    private FacilityInfoService facilityDataService;

    @RequestMapping(value = "/{facilityId}/lastEncounterDate", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public
    @ResponseBody
    Date showLastEncounter(@PathVariable String facilityId) {
        return facilityDataService.getLastEncounterDateTime(facilityId);
    }

    @RequestMapping(value = "/{facilityId}/visitTypes/forDate", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public
    @ResponseBody
    List<Map<String, Object>> showVisitType(@PathVariable String facilityId,
                                            @RequestParam(value = "date", required = true) String date) {
        return facilityDataService.getAllVisitTypes(facilityId, date);
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public
    @ResponseBody
    Object searchFacility(@RequestParam(value = "name", required = false) String name,
                          @RequestParam(value = "id", required = false) String id) throws IOException, URISyntaxException {
        if (id != null) {
            return facilityDataService.getAvailableFacilitiesById(id);
        } else if (name != null) {
            return facilityDataService.getAvailableFacilitiesBYName(name);
        }
        return null;
    }


}
