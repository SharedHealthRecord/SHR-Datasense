package org.sharedhealth.datasense.service;


import org.sharedhealth.datasense.model.Parameter;
import org.sharedhealth.datasense.repository.ConfigurationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ConfigurationService {
    @Autowired
    ConfigurationDao configurationDao;

    public static final String CIRCUMSTANCES_OF_DEATH_UUID = "CIRCUMSTANCES_OF_DEATH_UUID";
    public static final String CAUSE_OF_DEATH_UUID = "CAUSE_OF_DEATH_UUID";

    public static final String DEATH_CODES = "DEATH_CODES";
    public static final String DATE_OF_DEATH_UUID = "DATE_OF_DEATH_UUID";


    public List<Parameter> allParameters() {
        return configurationDao.allParameters();
    }

    public Parameter parameterById(int paramId) {
        return configurationDao.parameterById(paramId);
    }

    /**
     * TODO
     * Should cache parameters. Invalidate on Save
     * @param parameter
     */
    public void save(Parameter parameter) {
        configurationDao.save(parameter);
    }

    public Parameter parameterByName(String paramName) {
        return configurationDao.parameterByName(paramName);
    }

    /**
     * Should cache parameter. Invalidate during save param
     * @return
     */
    public List<String> getDeathCodes() {
        Parameter deathCodes = parameterByName(DEATH_CODES);
        return Arrays.asList(deathCodes.getParamValue().split(","));
    }

    /**
     * Should cache parameter. Invalidate during save param
     * @return
     */
    public String getDateOfDeathUuid() {
        Parameter param = parameterByName(DATE_OF_DEATH_UUID);
        return param.getParamValue();
    }

    /**
     * Should cache parameter. Invalidate during save param
     * @return
     */
    public String getCircumstancesOfDeathUuid() {
        Parameter param = parameterByName(CIRCUMSTANCES_OF_DEATH_UUID);
        return param.getParamValue();
    }

    /**
     * Should cache parameter. Invalidate during save param
     * @return
     */
    public String getCauseOfDeath() {
        Parameter param = parameterByName(CAUSE_OF_DEATH_UUID);
        return param.getParamValue();
    }
}
