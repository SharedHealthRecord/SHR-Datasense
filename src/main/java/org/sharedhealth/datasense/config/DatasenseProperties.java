package org.sharedhealth.datasense.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DatasenseProperties implements EnvironmentAware {

    private final static String URL_SEPARATOR = "/";
    private final static String PORT_SEPARATOR = ":";
    private final static String SCHEME_SEPARATOR = "://";

    //MCI Server Properties
    private String mciScheme;
    private String mciHost;
    private String mciPort;
    private String mciContextPath;
    //SHR Server Properties
    private String shrScheme;
    private String shrHost;
    private String shrPort;
    private String shrVersion;
    //TR Server Properties
    private String trScheme;
    private String trHost;
    private String trConceptAtomfeedPath;
    private String trReferenceTermAtomfeedPath;
    private String trMedicationAtomFeedPath;
    private String trPort;
    private String trUser;
    private String trPassword;
    //Identity Server Properties
    private String idpScheme;
    private String idpHost;
    private String idpPort;
    private String idpClientId;
    private String idpAuthToken;
    private String idpClientEmail;
    private String idpClientPassword;
    private String idpServerSigninPath;

    //DHIS Server Properties
    private String dhisPostUrl;
    private String dhisUserName;
    private String dhisPassword;
    private String dhisAqsConfigPath;
    //PR Server Properties
    private String prScheme;
    private String prHost;
    private String prContextPath;
    //FR Server Properties
    private String frScheme;
    private String frHost;
    private String frContextPath;
    //Datasense Properties
    private String datasenseFacilityId;
    private String datasenseCatchmentList;
    //Tr Reference Code
    private List<String> deathCodes;
    private String dateOfDeathUuid;
    private String circumstancesOfDeathUuid;
    private String causeOfDeath;
    
    private String bahmniOnCloudFacilityId;

    @Override
    public void setEnvironment(Environment env) {
        this.shrScheme = env.getProperty("SHR_SCHEME");
        this.shrHost = env.getProperty("SHR_HOST");
        this.shrPort = env.getProperty("SHR_PORT");
        this.shrVersion = env.getProperty("SHR_VERSION");
        this.mciScheme = env.getProperty("MCI_SCHEME");
        this.mciHost = env.getProperty("MCI_HOST");
        this.mciPort = env.getProperty("MCI_PORT");
        this.mciContextPath = env.getProperty("MCI_CONTEXT_PATH");
        this.datasenseFacilityId = env.getProperty("DATASENSE_FACILITY_ID");
        this.datasenseCatchmentList = env.getProperty("DATASENSE_CATCHMENT_LIST");
        this.prScheme = env.getProperty("PR_SCHEME");
        this.prHost = env.getProperty("PR_HOST");
        this.prContextPath = env.getProperty("PR_CONTEXT_PATH");
        this.frScheme = env.getProperty("FR_SCHEME");
        this.frHost = env.getProperty("FR_HOST");
        this.frContextPath = env.getProperty("FR_CONTEXT_PATH");
        this.dhisUserName = env.getProperty("DHIS_USER_NAME");
        this.dhisPassword = env.getProperty("DHIS_PASSWORD");
        this.dhisAqsConfigPath = env.getProperty("DHIS_AQS_CONFIG_PATH");
        this.idpScheme = env.getProperty("IDP_SCHEME");
        this.idpHost = env.getProperty("IDP_HOST");
        this.idpPort = env.getProperty("IDP_PORT");
        this.idpClientId = env.getProperty("IDP_CLIENT_ID");
        this.idpAuthToken = env.getProperty("IDP_AUTH_TOKEN");
        this.idpServerSigninPath = env.getProperty("IDP_SIGNIN_PATH");
        this.idpClientEmail = env.getProperty("IDP_CLIENT_EMAIL");
        this.idpClientPassword = env.getProperty("IDP_CLIENT_PASSWORD");
        this.trScheme = env.getProperty("TR_SCHEME");
        this.trHost = env.getProperty("TR_HOST");
        this.trPort = env.getProperty("TR_PORT");
        this.trConceptAtomfeedPath = env.getProperty("TR_CONCEPT_ATOMFEED_PATH");
        this.trReferenceTermAtomfeedPath = env.getProperty("TR_REFERENCE_TERM_ATOMFEED_PATH");
        this.trMedicationAtomFeedPath = env.getProperty("TR_MEDICATION_ATOMFEED_PATH");
        this.trUser = env.getProperty("TR_USER");
        this.trPassword = env.getProperty("TR_PASSWORD");
        this.deathCodes = Arrays.asList(env.getProperty("DEATH_CODES").split(","));
        this.dateOfDeathUuid = env.getProperty("DATE_OF_DEATH_UUID");
        this.circumstancesOfDeathUuid = env.getProperty("CIRCUMSTANCES_OF_DEATH_UUID");
        this.causeOfDeath = env.getProperty("CAUSE_OF_DEATH_UUID");
        this.bahmniOnCloudFacilityId = env.getProperty("BAHMNI_ON_CLOUD_FACILITY_ID");
    }

    public String getShrBaseUrl() {
        return String.format("%s/%s", getBaseUrl(shrScheme, shrHost, shrPort), this.shrVersion);
    }

    public String getMciBaseUrl() {
        return getBaseUrl(mciScheme, mciHost, mciPort);
    }

    public String getMciPatientUrl() {
        return getMciBaseUrl() + URL_SEPARATOR + mciContextPath;
    }

    public String getIdentityServerBaseUrl() {
        return getBaseUrl(idpScheme, idpHost, idpPort);
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
        return getTrBasePath() + URL_SEPARATOR + trConceptAtomfeedPath;
    }

    public String getTrReferenceTermAtomfeedUrl() {
        return getTrBasePath() + URL_SEPARATOR + trReferenceTermAtomfeedPath;
    }

    public String getTrMedicationfeedUrl() {
        return getTrBasePath() + URL_SEPARATOR + trMedicationAtomFeedPath;
    }

    public String getTrBasePath() {
        return getBaseUrl(trScheme, trHost, trPort);
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

    public String getPrBaseUrl() {
        return getBaseUrl(prScheme, prHost, null);
    }

    public String getPrProviderUrl() {
        return getPrBaseUrl() + URL_SEPARATOR + prContextPath;
    }

    private String getFrBaseUrl() {
        return getBaseUrl(frScheme, frHost, null);
    }

    public String getFacilityRegistryUrl() {
        return getFrBaseUrl() + URL_SEPARATOR + frContextPath;
    }

    private String getBaseUrl(String scheme, String host, String port) {
        String path = scheme + SCHEME_SEPARATOR + host;
        return port != null ? path + PORT_SEPARATOR + port : path;
    }

    public String getIdpServerSigninPath() {
        return idpServerSigninPath;
    }

    public String getBahmniOnCloudFacilityId() {
        return bahmniOnCloudFacilityId;
    }
}
