package org.sharedhealth.datasense.dhis2.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HomeController {
    @RequestMapping(value = {"/dhis2/home", "/dhis2", "/", ""}, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public String home() {
        return "home";
    }
}
