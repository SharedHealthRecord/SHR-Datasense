package org.sharedhealth.datasense.controller;

import org.apache.log4j.Logger;
import org.sharedhealth.datasense.model.Parameter;
import org.sharedhealth.datasense.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping(value = "/config/parameters")
public class ParameterController {

    private static final Logger logger = Logger.getLogger(ParameterController.class);
    private static final String[] supportedParamDataTypes = new String[] { "String" };

    @Autowired
    ConfigurationService configurationService;

    @RequestMapping(value = {"/", ""}, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView("config.parameters");
        List<Parameter> parameters = configurationService.allParameters();
        modelAndView.addObject("paramList", parameters);
        return modelAndView;
    }

    @RequestMapping(value = "/{paramId}/edit", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public ModelAndView editParam(@PathVariable int paramId) {
        ModelAndView modelAndView = new ModelAndView("config.defineParameter");
        Parameter param = configurationService.parameterById(paramId);
        modelAndView.addObject("configParameter", param);
        modelAndView.addObject("supportedParameterTypes", new String[] {Parameter.ParameterType.SYSTEM.toString(), Parameter.ParameterType.USER_DEFINED.toString()});
        modelAndView.addObject("supportedParamDataTypes", supportedParamDataTypes);
        return modelAndView;
    }

    @RequestMapping(value = "/define", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public ModelAndView defineParam() {
        ModelAndView modelAndView = new ModelAndView("config.defineParameter");
        Parameter parameter = new Parameter();
        parameter.setDataType("String");
        parameter.setParamType(Parameter.ParameterType.USER_DEFINED.toString());
        modelAndView.addObject("configParameter", parameter);
        modelAndView.addObject("supportedParameterTypes", new String[] {Parameter.ParameterType.SYSTEM.toString(), Parameter.ParameterType.USER_DEFINED.toString()});
        modelAndView.addObject("supportedParamDataTypes", supportedParamDataTypes);
        return modelAndView;
    }



    @RequestMapping(value = "/define", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public ModelAndView saveParam(Parameter parameter) {
        String message = "Error occurred while trying to save parameter.";
        try {
            configurationService.save(parameter);
            return new ModelAndView("redirect:/config/parameters");
        } catch (Exception e) {
            logger.error(message, e);
            String[] formErrors = new String[] {message + e.getMessage()};
            ModelAndView modelAndView = new ModelAndView("config.defineParameter");
            modelAndView.addObject("configParameter", parameter);
            modelAndView.addObject("supportedParameterTypes", new String[] {Parameter.ParameterType.SYSTEM.toString(), Parameter.ParameterType.USER_DEFINED.toString()});
            modelAndView.addObject("supportedParamDataTypes", supportedParamDataTypes);
            modelAndView.addObject("formErrors", formErrors);
            return modelAndView;

        }
    }


}
