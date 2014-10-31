package org.sharedhealth.datasense.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.lang.System.getenv;

@Component
public class DatasenseProperties {

    private String shrScheme;
    private String shrHost;
    private String shrPort;
    private String shrUser;
    private String shrPassword;
    private String datasenseFacilityId;
    private String datasenseCatchmentList;

    public DatasenseProperties() {
        Map<String, String> env = getenv();
        this.shrScheme = env.get("SHR_SCHEME");
        this.shrHost = env.get("SHR_HOST");
        this.shrPort = env.get("SHR_PORT");
        this.shrUser = env.get("SHR_USER");
        this.shrPassword = env.get("SHR_PASSWORD");
        this.datasenseFacilityId = env.get("DATASENSE_FACILITY_ID");
        this.datasenseCatchmentList = env.get("DATASENSE_CATCHMENT_LIST");
    }

    public String getShrScheme() {
        return shrScheme;
    }

    public String getShrHost() {
        return shrHost;
    }

    public String getShrPort() {
        return shrPort;
    }

    public String getShrUser() {
        return shrUser;
    }

    public String getShrPassword() {
        return shrPassword;
    }

    public String getShrBaseUrl() {
        return shrScheme +  "://" + shrHost + ":" + shrPort;
    }

    public String getDatasenseFacilityId() {
        return datasenseFacilityId;
    }

    public String[] getDatasenseCatchmentList() {
        String[] catchments = StringUtils.split(datasenseCatchmentList, ",");
        return catchments;
    }
}
