package org.sharedhealth.datasense.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DatasenseProperties implements EnvironmentAware {

    //MCI Server Properties
    private String mciScheme;
    private String mciHost;
    private String mciPort;
    private String mciContextPath;
    private String mciUser;
    private String mciPassword;
    //SHR Server Properties
    private String shrScheme;
    private String shrHost;
    private String shrPort;
    private String shrUser;
    private String shrPassword;
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
    private String identityScheme;
    private String identityHost;
    private String identityPort;
    private String identityUser;
    private String identityPassword;
    //DHIS Server Properties
    private String dhisPostUrl;
    private String dhisUserName;
    private String dhisPassword;
    private String dhisAqsConfigPath;
    //FR Server Properties
    private String facilityRegistryUrl;
    private String facilityAuthToken;
    //Datasense Properties
    private String datasenseFacilityId;
    private String datasenseCatchmentList;
    //Tr Reference Code
    private List<String> deathCodes;
    private String dateOfDeathUuid;
    private String circumstancesOfDeathUuid;
    private String causeOfDeath;


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
        this.mciContextPath = env.getProperty("MCI_CONTEXT_PATH");
        this.mciUser = env.getProperty("MCI_USER");
        this.mciPassword = env.getProperty("MCI_PASSWORD");
        this.datasenseFacilityId = env.getProperty("DATASENSE_FACILITY_ID");
        this.datasenseCatchmentList = env.getProperty("DATASENSE_CATCHMENT_LIST");
        this.facilityRegistryUrl = env.getProperty("FACILITY_URL");
        this.facilityAuthToken = env.getProperty("FACILITY_AUTH_TOKEN");
        this.dhisPostUrl = env.getProperty("DHIS_POST_URL");
        this.dhisUserName = env.getProperty("DHIS_USER_NAME");
        this.dhisPassword = env.getProperty("DHIS_PASSWORD");
        this.dhisAqsConfigPath = env.getProperty("DHIS_AQS_CONFIG_PATH");
        this.identityScheme = env.getProperty("IDENTITY_SCHEME");
        this.identityHost = env.getProperty("IDENTITY_HOST");
        this.identityPort = env.getProperty("IDENTITY_PORT");
        this.identityUser = env.getProperty("IDENTITY_USER");
        this.identityPassword = env.getProperty("IDENTITY_PASSWORD");
        this.trScheme = env.getProperty("TR_SCHEME");
        this.trHost = env.getProperty("TR_HOST");
        this.trPort = env.getProperty("TR_PORT");
        this.trConceptAtomfeedPath = env.getProperty("TR_CONCEPT_ATOMFEED_PATH");
        this.trReferenceTermAtomfeedPath = env.getProperty("TR_REFERENCE_TERM_ATOMFEED_PATH");
        this.trMedicationAtomFeedPath=env.getProperty("TR_MEDICATION_ATOMFEED_PATH");
        this.trUser = env.getProperty("TR_USER");
        this.trPassword = env.getProperty("TR_PASSWORD");
        this.deathCodes = Arrays.asList(env.getProperty("DEATH_CODES").split(","));
        this.dateOfDeathUuid = env.getProperty("DATE_OF_DEATH_UUID");
        this.circumstancesOfDeathUuid = env.getProperty("CIRCUMSTANCES_OF_DEATH_UUID");
        this.causeOfDeath = env.getProperty("CAUSE_OF_DEATH_UUID");
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
        return shrScheme + "://" + shrHost + ":" + shrPort;
    }

    public String getMciScheme() {
        return mciScheme;
    }

    public String getMciContextPath() {
        return mciContextPath;
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
        return mciScheme + "://" + mciHost + ":" + mciPort + "/" + mciContextPath;
    }

    public String getIdentityServerBaseUrl() {
        return identityScheme + "://" + identityHost + ":" + identityPort;
    }

    public String getDatasenseFacilityId() {
        return datasenseFacilityId;
    }

    public String[] getDatasenseCatchmentList() {
        return StringUtils.split(datasenseCatchmentList, ",");
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

    public String getTrHost() {
        return trHost;
    }

    public String getTrPort() {
        return trPort;
    }

    public String getTrUser() {
        return trUser;
    }

    public String getTrPassword() {
        return trPassword;
    }

    public String getTrConceptAtomfeedUrl() {
        return getTrBasePath() + "/" + trConceptAtomfeedPath;
    }

    public String getTrReferenceTermAtomfeedUrl() {
        return getTrBasePath() + "/" + trReferenceTermAtomfeedPath;
    }

    public String getTrMedicationfeedUrl() {
        return getTrBasePath() + "/" + trMedicationAtomFeedPath;
    }

    public String getTrBasePath() {
        return trScheme + "://" + trHost + ":" + trPort;
    }

    public String getTrScheme() {
        return trScheme;
    }

    public String getTrConceptAtomfeedPath() {
        return trConceptAtomfeedPath;
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
}
