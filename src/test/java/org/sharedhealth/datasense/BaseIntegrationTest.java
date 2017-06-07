package org.sharedhealth.datasense;


import org.sharedhealth.datasense.model.Parameter;
import org.sharedhealth.datasense.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

public class BaseIntegrationTest {
    @Autowired
    ConfigurationService configurationService;

    public void loadConfigParameters() {
        Properties allParams = loadProperties("configParams.properties");
        Enumeration e = allParams.propertyNames();
        while (e.hasMoreElements()) {
            String paramName = (String) e.nextElement();
            String paramValue = allParams.getProperty(paramName);
            Parameter param = createStringParam(paramName, paramValue);
            configurationService.save(param);
        }
    }

    private Parameter createStringParam(String paramName, String paramValue) {
        Parameter parameter = new Parameter();
        parameter.setDataType("String");
        parameter.setParamType(Parameter.ParameterType.SYSTEM.toString());
        parameter.setParamName(paramName);
        parameter.setParamValue(paramValue);
        return parameter;
    }

    public Properties loadProperties(String filename) {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = BaseIntegrationTest.class.getClassLoader().getResourceAsStream(filename);
            if (input == null) {
                return prop;
            }
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return prop;
    }
}
