package org.sharedhealth.datasense.dhis2.controller;

import org.sharedhealth.datasense.client.DHIS2Client;
import org.sharedhealth.datasense.dhis2.model.DHISOrgUnitConfig;
import org.sharedhealth.datasense.dhis2.model.DHISReportConfig;
import org.sharedhealth.datasense.dhis2.model.DHISResponse;
import org.sharedhealth.datasense.dhis2.service.DHISMetaDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "/dhis2/reports")
public class DHISReportController {



    @Autowired
    DHISMetaDataService metaDataService;

    @Autowired
    DHIS2Client dhis2Client;

    private List<DHISReportConfig> datasetList = new ArrayList<>();
    private List<DHISOrgUnitConfig> orgUnitList = new ArrayList<>();

    public DHISReportController() {
        datasetList.add(new DHISReportConfig("Daily Opd Ipd Report", "opdreport"));
        datasetList.add(new DHISReportConfig("Monthly Colposcopy Report", "colreport"));

        orgUnitList.add(new DHISOrgUnitConfig("Dohar UHC", "DUHC"));
        orgUnitList.add(new DHISOrgUnitConfig("Amtali UHC", "AUHC"));
    }

    @RequestMapping(value = {"/", ""}, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public ModelAndView showReportsList() {
        ModelAndView modelAndView = new ModelAndView("dhis.reports");
        modelAndView.addObject("availableReports", metaDataService.getConfiguredReports());
        return modelAndView;
    }

    @RequestMapping(value = "/configure", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public ModelAndView configure(@RequestParam(value = "reportName") String reportName) {
        ModelAndView modelAndView = new ModelAndView("dhis.datasetConfig");
        modelAndView.addObject("reportName", reportName);
        //TODO
        modelAndView.addObject("configFile", reportName + ".json") ;
        return modelAndView;
    }

    @RequestMapping(value = "/configure", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public @ResponseBody
    String configure(@RequestBody DHISReportConfig config) {
        metaDataService.save(config);
        return "{}";
    }

    @RequestMapping(value = "/schedule", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public ModelAndView showOrganizationUnits(@RequestParam(value = "reportName") String reportName) {
        ModelAndView modelAndView = new ModelAndView("dhis.scheduleReports");
        modelAndView.addObject("orgUnits", metaDataService.getAvailableOrgUnits(false));
        return modelAndView;
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET, produces= MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public @ResponseBody
    DHISResponse searchDHISDataset(@RequestParam(value = "name") String name) {
        String searchUri =
                String.format("/api/dataSets?filter=name:like:%s&fields=id,name,href", name);
        return dhis2Client.get(searchUri);
    }


}
