package org.sharedhealth.datasense.config;

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
    private String mciServerPatientUrl;
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
    private List<String> deathCodes;

    private String dateOfDeathUuid;
    private String circumstancesOfDeathUuid;
    private String causeOfDeath;
    private String cloudHostedFacilityIds;


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
        this.deathCodes = asList(env.getProperty("DEATH_CODES").split(","));
        this.dateOfDeathUuid = env.getProperty("DATE_OF_DEATH_UUID");
        this.circumstancesOfDeathUuid = env.getProperty("CIRCUMSTANCES_OF_DEATH_UUID");
        this.causeOfDeath = env.getProperty("CAUSE_OF_DEATH_UUID");
        this.cloudHostedFacilityIds = env.getProperty("CLOUD_HOSTED_FACILITY_IDs");
        this.shrServerUrl = env.getProperty("SHR_SERVER_URL");
        this.mciServerPatientUrl = env.getProperty("MCI_SERVER_PATIENT_URL");
        this.trServerUrl = env.getProperty("TR_SERVER_URL");
        this.facilityRegistryUrl = env.getProperty("FACILITY_REGISTRY_URL");
        this.providerRegistryUrl = env.getProperty("PROVIDER_REGISTRY_URL");
        this.idpServerLoginUrl = env.getProperty("IDP_SERVER_LOGIN_URL");
    }

    public String getShrBaseUrl() {
        return shrServerUrl.trim();
    }


    public String getMciPatientUrl() {
        return mciServerPatientUrl.trim();
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

    public String getDhisAqsConfigPath() {
        return dhisAqsConfigPath;
    }

    public List<String> getDeathCodes() {
        return deathCodes;
    }

    public String getDateOfDeathUuid() {
        return dateOfDeathUuid;
    }

    public String getCircumstancesOfDeathUuid() {
        return circumstancesOfDeathUuid;
    }

    public String getCauseOfDeath() {
        return causeOfDeath;
    }

    public String getPrProviderUrl() {
        return providerRegistryUrl.trim();
    }

    public String getFacilityRegistryUrl() {
        return facilityRegistryUrl.trim();
    }

    private String getBaseUrl(String scheme, String host, String port) {
        String path = scheme + SCHEME_SEPARATOR + host;
        return port != null ? path + PORT_SEPARATOR + port : path;
    }

    public List<String> getCloudHostedFacilityIds() {
        if (StringUtils.isBlank(cloudHostedFacilityIds)) return new ArrayList<>();
        return asList(cloudHostedFacilityIds.trim().split(","));
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
