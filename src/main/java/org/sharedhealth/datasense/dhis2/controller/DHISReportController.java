package org.sharedhealth.datasense.dhis2.controller;

import org.quartz.SchedulerException;
import org.sharedhealth.datasense.dhis2.model.DHISDataset;
import org.sharedhealth.datasense.dhis2.model.DHISOrgUnit;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "/dhis2/reports")
public class DHISReportController {

    private List<DHISDataset> datasetList = new ArrayList<>();
    private List<DHISOrgUnit> orgUnitList = new ArrayList<>();

    public DHISReportController() {
        datasetList.add(new DHISDataset("Daily Opd Ipd Report", "opdreport"));
        datasetList.add(new DHISDataset("Monthly Colposcopy Report", "colreport"));

        orgUnitList.add(new DHISOrgUnit("Dohar UHC", "DUHC"));
        orgUnitList.add(new DHISOrgUnit("Amtali UHC", "AUHC"));
    }

    @RequestMapping(value = {"/", ""}, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public ModelAndView showReportsList() throws SchedulerException {
        ModelAndView modelAndView = new ModelAndView("dhis.reports");
        modelAndView.addObject("availableReports", datasetList);
        return modelAndView;
    }

    @RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public ModelAndView showOrganizationUnits(@PathVariable final String uuid) throws SchedulerException {
        ModelAndView modelAndView = new ModelAndView("dhis.reportingUnits");
        modelAndView.addObject("orgUnits", orgUnitList);
        return modelAndView;
    }



}
