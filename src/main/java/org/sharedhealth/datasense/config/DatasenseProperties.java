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
    private String facilityRegistryUrl;
    private String facilityAuthToken;
    private String dhisPostUrl;
    private String dhisUserName;
    private String dhisPassword;

    //Identity Server Properties
    private String identityScheme;
    private String identityHost;
    private String identityPort;
    private String identityUser;
    private String identityPassword;


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
        return shrScheme + "://" + shrHost + ":" + shrPort;
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
        return mciScheme + "://" + mciHost + ":" + mciPort + "/api/v1";
    }

    public String getIdentityServerBaseUrl() {
        return identityScheme + "://" + identityHost + ":" + identityPort;
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
        this.facilityRegistryUrl = env.getProperty("FACILITY_URL");
        this.facilityAuthToken = env.getProperty("FACILITY_AUTH_TOKEN");
        this.dhisPostUrl = env.getProperty("DHIS_POST_URL");
        this.dhisUserName = env.getProperty("DHIS_USER_NAME");
        this.dhisPassword = env.getProperty("DHIS_PASSWORD");
        this.identityScheme = env.getProperty("IDENTITY_SCHEME");
        this.identityHost = env.getProperty("IDENTITY_HOST");
        this.identityPort = env.getProperty("IDENTITY_PORT");
        this.identityUser = env.getProperty("IDENTITY_USER");
        this.identityPassword = env.getProperty("IDENTITY_PASSWORD");
    }

    public String getFacilityRegistryUrl() {
        return facilityRegistryUrl;
    }

    public String getFacilityAuthToken() {
        return facilityAuthToken;
    }

    public String getDhisPostUrl() {
        return dhisPostUrl;
    }

    public String getDhisUserName() {
        return dhisUserName;
    }

    public String getDhisPassword() {
        return dhisPassword;
    }

    public String getIdentityScheme() {
        return identityScheme;
    }

    public String getIdentityHost() {
        return identityHost;
    }

    public String getIdentityPort() {
        return identityPort;
    }

    public String getIdentityUser() {
        return identityUser;
    }

    public String getIdentityPassword() {
        return identityPassword;
    }
}
