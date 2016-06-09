package org.sharedhealth.datasense.dhis2.controller;

import org.sharedhealth.datasense.client.DHIS2Client;
import org.sharedhealth.datasense.dhis2.model.DHISOrgUnitConfig;
import org.sharedhealth.datasense.dhis2.model.DHISResponse;
import org.sharedhealth.datasense.dhis2.service.DHISMetaDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.net.URISyntaxException;

@Controller
@RequestMapping(value = "/dhis2/orgUnits")
public class DHISOrgUnitController {

    public static final String DHIS_ORGUNIT_SEARCH_FORMAT = "/api/organisationUnits?filter=name:like:%s&fields=id,name,href&pageSize=500";
    @Autowired
    DHISMetaDataService metaDataService;

    @Autowired
    DHIS2Client dhis2Client;

    @RequestMapping(value = {"/", ""}, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public ModelAndView showReportsList() {
        ModelAndView modelAndView = new ModelAndView("dhis.orgUnits");
        modelAndView.addObject("allOrgUnits", metaDataService.getAvailableOrgUnits(true));
        return modelAndView;
    }

    @RequestMapping(value = "/configure", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public ModelAndView configure(@RequestParam(value = "facilityId") String facilityId) {
        ModelAndView modelAndView = new ModelAndView("dhis.orgunitConfig");
        modelAndView.addObject("facilityId", facilityId);
        return modelAndView;
    }

    @RequestMapping(value = "/configure", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public @ResponseBody
    String configure(@RequestBody DHISOrgUnitConfig config) {
        metaDataService.save(config);
        return "{}";
    }

    @RequestMapping(value = "/config", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public ModelAndView showFacilityInfo() {
        ModelAndView modelAndView = new ModelAndView("facilityInfo");
        return modelAndView;
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET, produces= MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public @ResponseBody
    DHISResponse searchDHISDataset(@RequestParam(value = "name") String name) throws IOException, URISyntaxException {
        String searchString = name.replaceAll("  ", " ").replaceAll(" ", "%20");
        String searchUri =
                String.format(DHIS_ORGUNIT_SEARCH_FORMAT, searchString);
        return dhis2Client.get(searchUri);
    }
}
