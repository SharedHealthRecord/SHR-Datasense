package org.sharedhealth.datasense.service;


import org.sharedhealth.datasense.model.Parameter;
import org.sharedhealth.datasense.repository.ConfigurationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConfigurationService {
    public static final String EMPTY_STR = "";
    @Autowired
    ConfigurationDao configurationDao;
    public static final String CIRCUMSTANCES_OF_DEATH_UUID = "CIRCUMSTANCES_OF_DEATH_UUID";

    public static final String CAUSE_OF_DEATH_UUID = "CAUSE_OF_DEATH_UUID";
    public static final String DEATH_CODES = "DEATH_CODES";

    public static final String DATE_OF_DEATH_UUID = "DATE_OF_DEATH_UUID";
    public static final String PLACE_OF_DEATH_UUID = "PLACE_OF_DEATH_UUID";


    public List<Parameter> allParameters() {
        return configurationDao.allParameters();
    }

    public Parameter parameterById(int paramId) {
        return configurationDao.parameterById(paramId);
    }

    private ConcurrentHashMap<String, Parameter> parameterCache = new ConcurrentHashMap<>();

    private List<String> parametersInCache = Arrays.asList(CIRCUMSTANCES_OF_DEATH_UUID, CAUSE_OF_DEATH_UUID, DEATH_CODES, DATE_OF_DEATH_UUID, PLACE_OF_DEATH_UUID);

    public void save(Parameter parameter) {
        configurationDao.save(parameter);
        if (parametersInCache.contains(parameter.getParamName())) {
            parameterCache.remove(parameter.getParamName());
        }
    }

    public Parameter parameterByName(String paramName) {
        return configurationDao.parameterByName(paramName);
    }

    private Parameter findParameter(String paramName) {
        String nameOfParameter = paramName.trim();
        if (parametersInCache.contains(nameOfParameter)) {
            Parameter parameter = parameterCache.get(nameOfParameter);
            if (parameter == null) {
                parameter = parameterByName(nameOfParameter);
                parameterCache.put(nameOfParameter, parameter);
            }
            return parameter;
        } else {
            return parameterByName(nameOfParameter);
        }
    }

    public List<String> getDeathCodes() {
        Parameter deathCodes = findParameter(DEATH_CODES);
        return Arrays.asList(deathCodes.getParamValue().split(","));
    }

    public String getDateOfDeathUuid() {
        Parameter param = findParameter(DATE_OF_DEATH_UUID);
        return (param != null) ? param.getParamValue() : EMPTY_STR;
    }

    public String getCircumstancesOfDeathUuid() {
        Parameter param = findParameter(CIRCUMSTANCES_OF_DEATH_UUID);
        return (param != null) ? param.getParamValue() : EMPTY_STR;
    }

    public String getCauseOfDeath() {
        Parameter param = findParameter(CAUSE_OF_DEATH_UUID);
        return (param != null) ? param.getParamValue() : EMPTY_STR;
    }

    public String getPlaceOfDeathConceptUuid() {
        Parameter param = findParameter(PLACE_OF_DEATH_UUID);
        return (param != null) ? param.getParamValue() : EMPTY_STR;
    }
}
