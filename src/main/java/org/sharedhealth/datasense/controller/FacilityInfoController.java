package org.sharedhealth.datasense.controller;

import org.apache.log4j.Logger;
import org.sharedhealth.datasense.client.IdentityServiceClient;
import org.sharedhealth.datasense.security.UserInfo;
import org.sharedhealth.datasense.service.FacilityInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/facility")
public class FacilityInfoController {

    Logger log = Logger.getLogger(FacilityInfoController.class);
    @Autowired
    private FacilityInfoService facilityDataService;
    @Autowired
    private IdentityServiceClient identityServiceClient;

    @RequestMapping(value = {"/", ""}, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public ModelAndView getFacilityInfo() {
        ModelAndView modelAndView = new ModelAndView("facilityInfo");
        return modelAndView;
    }

    @RequestMapping(value = "/{facilityId}/visitTypes/forDate", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public
    @ResponseBody
    List<Map<String, Object>> getVisitType(@PathVariable String facilityId,
                                           @RequestParam(value = "date", required = true) String date) {
        logAccessDetails("Getting visit type with count.");
        return facilityDataService.getAllVisitTypes(facilityId, date);
    }

    @RequestMapping(value = "/{facilityId}/diagnosis/withinDates", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public
    @ResponseBody
    List<Map<String, Object>> getDiagnosisNameWithCount(@PathVariable String facilityId,
                                                        @RequestParam(value = "startDate", required = true) String startDate,
                                                        @RequestParam(value = "endDate", required = true) String endDate) {
        logAccessDetails("Getting diagnosis name with count.");
        return facilityDataService.getDiagnosisNameWithCount(facilityId, startDate, endDate);
    }

    @RequestMapping(value = "/{facilityId}/encounterTypes/withinDates", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public
    @ResponseBody
    List<Map<String, Object>> getEncounterTypesWithCount(@PathVariable String facilityId,
                                                         @RequestParam(value = "startDate", required = true) String startDate,
                                                         @RequestParam(value = "endDate", required = true) String endDate) {
        logAccessDetails("Getting encounter types with count");
        return facilityDataService.getEncounterTypesWithCount(facilityId, startDate, endDate);
    }

    @RequestMapping(value = "/{facilityId}/prescribedDrugs/withinDates", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public
    @ResponseBody
    HashMap<String, List<Map<String, Object>>> getPrescribedDrugsWithCount(@PathVariable String facilityId,
                                                                           @RequestParam(value = "startDate", required = true) String startDate,
                                                                           @RequestParam(value = "endDate", required = true) String endDate) {
        logAccessDetails("Getting prescribed drugs with count");
        HashMap<String, List<Map<String, Object>>> prescribedDrugs = new HashMap<>();
        prescribedDrugs.put("freetextCount", facilityDataService.getFreeTextCount(facilityId, startDate, endDate));
        prescribedDrugs.put("nonCodedDrugsWithCount", facilityDataService.getnonCodedDrugsWithCount(facilityId, startDate, endDate));
        prescribedDrugs.put("codedDrugCount", facilityDataService.getCodedDrugCount(facilityId, startDate, endDate));
        prescribedDrugs.put("codedDrugWithCount", facilityDataService.getCodedDrugWithCount(facilityId, startDate, endDate));
        return prescribedDrugs;
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public
    @ResponseBody
    Object searchFacility(@RequestParam(value = "name", required = false) String facilityName,
                          @RequestParam(value = "id", required = false) String facilityId) throws IOException, URISyntaxException {
        if (facilityId != null) {
            logAccessDetails(String.format("Searching facility %s", facilityId));
            return facilityDataService.getAvailableFacilitiesById(facilityId);
        } else if (facilityName != null) {
            logAccessDetails(String.format("Searching facility %s", facilityName));
            return facilityDataService.getAvailableFacilitiesBYName(facilityName);
        }
        return null;
    }

    @RequestMapping(value = "/{facilityId}/dashboard", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public ModelAndView getFacilityDashboard(@PathVariable(value = "facilityId") String facilityId) throws IOException, URISyntaxException {
        ModelAndView dashboard = new ModelAndView("facilityDashboard");
        dashboard.addObject("facility", facilityDataService.getAvailableFacilitiesById(facilityId));
        dashboard.addObject("lastEncounterDate", facilityDataService.getLastEncounterDateTime(facilityId));
        return dashboard;
    }

    private UserInfo getUserInfo() {
        return (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void logAccessDetails(String action) {
        UserInfo userInfo = getUserInfo();
        log.info(String.format("ACCESS: EMAIL=%s ACTION=%s", userInfo.getProperties().getEmail(), action));
    }
}
