package org.sharedhealth.datasense.controller;

import org.quartz.SchedulerException;
import org.sharedhealth.datasense.service.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping(value = "/scheduler")
public class SchedulerController {
    private SchedulerService schedulerService;

    @Autowired
    public SchedulerController(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @RequestMapping(value = "/manage", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public ModelAndView manageScheduler() throws SchedulerException {
        ModelAndView modelAndView = new ModelAndView("index");
        modelAndView.addObject("runningJobs", schedulerService.getRunningJobs());
        modelAndView.addObject("stoppedJobs", schedulerService.getStoppedJobs());
        return modelAndView;
    }

    @RequestMapping(value = "/start", method = RequestMethod.POST)
    @ResponseBody
    public void startScheduler(
            @RequestParam(value = "reportName") String reportName,
            @RequestParam(value = "expression") String cronExpression,
            @RequestParam(value = "paramKey") String reportParamKey,
            @RequestParam(value = "paramValue") String reportParamValue,
            HttpServletResponse response)
            throws SchedulerException, IOException {
        schedulerService.startJob(reportName, cronExpression, reportParamKey, reportParamValue);
        response.sendRedirect("/scheduler/manage");
    }

    @RequestMapping(value = "/stop", method = RequestMethod.POST)
    @ResponseBody
    public void stopScheduler(@RequestParam(value = "reportName") String reportName, HttpServletResponse response) throws SchedulerException, IOException {
        schedulerService.stopJob(reportName);
        response.sendRedirect("/scheduler/manage");
    }


}
