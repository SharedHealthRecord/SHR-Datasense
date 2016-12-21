package org.sharedhealth.datasense.controller;

import org.apache.log4j.Logger;
import org.sharedhealth.datasense.security.UserInfo;
import org.springframework.security.core.context.SecurityContextHolder;

public class DatasenseController {
    Logger log = Logger.getLogger(FacilityInfoController.class);


    private UserInfo getUserInfo() {
        return (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    protected void logAccessDetails(String action) {
        UserInfo userInfo = getUserInfo();
        log.info(String.format("ACCESS: EMAIL=%s ACTION=%s", userInfo.getProperties().getEmail(), action));
    }
}
