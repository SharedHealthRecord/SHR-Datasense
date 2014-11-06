package org.sharedhealth.datasense.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class DatasenseProperties implements EnvironmentAware {

    private String mciScheme;
    private String mciHost;
    private String mciPort;
    private String mciUser;
    private String mciPassword;
    private String shrScheme;
    private String shrHost;
    private String shrPort;
    private String shrUser;
    private String shrPassword;
    private String datasenseFacilityId;
    private String datasenseCatchmentList;


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

    public String getMciScheme() {
        return mciScheme;
    }

    public String getMciHost() {
        return mciHost;
    }

    public String getMciPort() {
        return mciPort;
    }

    public String getMciUser() {
        return mciUser;
    }

    public String getMciPassword() {
        return mciPassword;
    }

    public String getMciBaseUrl() {
        return mciScheme +  "://" + mciHost + ":" + mciPort + "/api/v1";
    }

    public String getDatasenseFacilityId() {
        return datasenseFacilityId;
    }

    public String[] getDatasenseCatchmentList() {
        String[] catchments = StringUtils.split(datasenseCatchmentList, ",");
        return catchments;
    }

    @Override
    public void setEnvironment(Environment env) {
        this.shrScheme = env.getProperty("SHR_SCHEME");
        this.shrHost = env.getProperty("SHR_HOST");
        this.shrPort = env.getProperty("SHR_PORT");
        this.shrUser = env.getProperty("SHR_USER");
        this.shrPassword = env.getProperty("SHR_PASSWORD");
        this.mciScheme = env.getProperty("MCI_SCHEME");
        this.mciHost = env.getProperty("MCI_HOST");
        this.mciPort = env.getProperty("MCI_PORT");
        this.mciUser = env.getProperty("MCI_USER");
        this.mciPassword = env.getProperty("MCI_PASSWORD");
        this.datasenseFacilityId = env.getProperty("DATASENSE_FACILITY_ID");
        this.datasenseCatchmentList = env.getProperty("DATASENSE_CATCHMENT_LIST");
    }
}
