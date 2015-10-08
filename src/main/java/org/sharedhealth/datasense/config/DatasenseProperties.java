package org.sharedhealth.datasense.config;

import liquibase.util.file.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.datasense.util.StringUtil;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

@Component
public class DatasenseProperties implements EnvironmentAware {

    private final static String URL_SEPARATOR = "/";
    private final static String PORT_SEPARATOR = ":";
    private final static String SCHEME_SEPARATOR = "://";

    //MCI Server Properties
    private String mciServerUrl;
    private String mciServerPatientUri;
    private String mciServerPatientUpdateFeedUri;
    //SHR Server Properties
    private String shrServerUrl;
    //TR Server Properties
    private String trServerUrl;
    private String trConceptAtomfeedPath;
    private String trReferenceTermAtomfeedPath;
    private String trMedicationAtomFeedPath;
    private String trUser;
    private String trPassword;
    //Identity Server Properties
    private String idpClientId;
    private String idpAuthToken;
    private String idpClientEmail;
    private String idpClientPassword;
    private String idpServerLoginUrl;
    //DHIS Server Properties
    private String dhisPostUrl;
    private String dhisUserName;
    private String dhisPassword;
    private String dhisAqsConfigPath;
    //PR Server Properties
    private String providerRegistryUrl;
    //FR Server Properties
    private String facilityRegistryUrl;
    //Datasense Properties
    private String datasenseFacilityId;
    private String datasenseCatchmentList;
    //Tr Reference Code
    private String cloudHostedFacilityIds;

    private String maxFailedEvents;

    private String idpServerUserInfoUrl;
    private String dhisBaseUrl;
    private String pentaThreeDrugUuid;

    @Override
    public void setEnvironment(Environment env) {
        this.datasenseFacilityId = env.getProperty("DATASENSE_FACILITY_ID");
        this.datasenseCatchmentList = env.getProperty("DATASENSE_CATCHMENT_LIST");
        this.dhisUserName = env.getProperty("DHIS_USER_NAME");
        this.dhisPassword = env.getProperty("DHIS_PASSWORD");
        this.dhisAqsConfigPath = env.getProperty("DHIS_AQS_CONFIG_PATH");
        this.idpClientId = env.getProperty("IDP_CLIENT_ID");
        this.idpAuthToken = env.getProperty("IDP_AUTH_TOKEN");
        this.idpClientEmail = env.getProperty("IDP_CLIENT_EMAIL");
        this.idpClientPassword = env.getProperty("IDP_CLIENT_PASSWORD");
        this.trConceptAtomfeedPath = env.getProperty("TR_CONCEPT_ATOMFEED_PATH");
        this.trReferenceTermAtomfeedPath = env.getProperty("TR_REFERENCE_TERM_ATOMFEED_PATH");
        this.trMedicationAtomFeedPath = env.getProperty("TR_MEDICATION_ATOMFEED_PATH");
        this.trUser = env.getProperty("TR_USER");
        this.trPassword = env.getProperty("TR_PASSWORD");
        this.cloudHostedFacilityIds = env.getProperty("CLOUD_HOSTED_FACILITY_IDs");
        this.shrServerUrl = env.getProperty("SHR_SERVER_URL");
        this.mciServerUrl = env.getProperty("MCI_SERVER_URL");
        this.mciServerPatientUri = env.getProperty("MCI_SERVER_PATIENT_URI");
        this.mciServerPatientUpdateFeedUri = env.getProperty("MCI_SERVER_PATIENT_UPDATE_FEED_URI");
        this.trServerUrl = env.getProperty("TR_SERVER_URL");
        this.facilityRegistryUrl = env.getProperty("FACILITY_REGISTRY_URL");
        this.providerRegistryUrl = env.getProperty("PROVIDER_REGISTRY_URL");
        this.idpServerLoginUrl = env.getProperty("IDP_SERVER_LOGIN_URL");
        this.idpServerUserInfoUrl = env.getProperty("IDP_SERVER_USERINFO_URL");
        this.maxFailedEvents = env.getProperty("MAX_FAILED_EVENTS");
        this.dhisBaseUrl = env.getProperty("DHIS_BASE_URL");


    }


    public String getShrBaseUrl() {
        return shrServerUrl.trim();
    }
    public String getMciBaseUrl() {
        return mciServerUrl.trim();
    }

    public String getMciPatientUrl() {
        return StringUtil.ensureSuffix(getMciBaseUrl(), URL_SEPARATOR) + StringUtil.removePrefix(mciServerPatientUri, URL_SEPARATOR);
    }

    public String getMciPatientUpdateFeedUrl() {
        return StringUtil.ensureSuffix(getMciBaseUrl(), URL_SEPARATOR) + StringUtil.removePrefix(mciServerPatientUpdateFeedUri, URL_SEPARATOR);
    }

    public String getIdentityServerLoginUrl() {
        return idpServerLoginUrl.trim();
    }

    public String getDatasenseFacilityId() {
        return datasenseFacilityId;
    }

    public String[] getDatasenseCatchmentList() {
        return StringUtils.split(datasenseCatchmentList, ",");
    }

    public String getDhisUserName() {
        return dhisUserName;
    }

    public String getDhisPassword() {
        return dhisPassword;
    }

    public String getIdpAuthToken() {
        return idpAuthToken;
    }

    public String getIdpClientId() {
        return idpClientId;
    }

    public String getIdpClientEmail() {
        return idpClientEmail;
    }

    public String getIdpClientPassword() {
        return idpClientPassword;
    }

    public String getTrUser() {
        return trUser;
    }

    public String getTrPassword() {
        return trPassword;
    }

    public String getTrConceptAtomfeedUrl() {
        return StringUtil.ensureSuffix(getTrBasePath(), URL_SEPARATOR) + StringUtil.removePrefix(trConceptAtomfeedPath, URL_SEPARATOR);
    }

    public String getTrReferenceTermAtomfeedUrl() {
        return StringUtil.ensureSuffix(getTrBasePath(), URL_SEPARATOR) + StringUtil.removePrefix(trReferenceTermAtomfeedPath, URL_SEPARATOR);
    }

    public String getTrMedicationfeedUrl() {
        return StringUtil.ensureSuffix(getTrBasePath(), URL_SEPARATOR) + StringUtil.removePrefix(trMedicationAtomFeedPath, URL_SEPARATOR);
    }

    public String getTrBasePath() {
        return trServerUrl.trim();
    }

    public String getAqsConfigLocationPath() {
        return dhisAqsConfigPath;
    }

    public String getPrProviderUrl() {
        return providerRegistryUrl.trim();
    }

    public String getFacilityRegistryUrl() {
        return facilityRegistryUrl.trim();
    }

    public String getIdpServerUserInfoUrl() {
        return idpServerUserInfoUrl.trim();
    }

    public List<String> getCloudHostedFacilityIds() {
        if (StringUtils.isBlank(cloudHostedFacilityIds)) return new ArrayList<>();
        return asList(cloudHostedFacilityIds.trim().split(","));
    }

    public int getMaxFailedEvents() {
        return Integer.parseInt(maxFailedEvents);
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    public String getDhisBaseUrl() {
        return dhisBaseUrl.trim();
    }

    public String getDhisDataValueSetsUrl() {
        return StringUtil.ensureSuffix(dhisBaseUrl.trim(), "/") + "api/dataValueSets";
    }

    public String getAqsQueryLocationPath() {
        return getAqsConfigBasePath() + "/aqs_query/";
    }

    private String getAqsConfigBasePath() {
        String configLocation = StringUtil.removeSuffix(getAqsConfigLocationPath(), "/");
        return configLocation.substring(0,configLocation.lastIndexOf("/"));
    }


    public String getAqsTemplateLocationPath() {
        return getAqsConfigBasePath() + "/templates/";
    }
}
