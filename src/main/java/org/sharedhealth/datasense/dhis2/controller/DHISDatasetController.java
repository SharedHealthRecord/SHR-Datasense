package org.sharedhealth.datasense.dhis2.controller;


import org.sharedhealth.datasense.client.DHIS2Client;
import org.sharedhealth.datasense.dhis2.model.DHISResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = "/dhis2/dataSets")
public class DHISDatasetController {

    private static final String DHIS_DATASET_DATA_ELEMENTS_FORMAT = "/api/dataSets/%s?fields=id,name,dataElements,categoryCombo";
    private static final String DHIS_DATA_ELEMENTS_FORMAT = "/api/dataElements/%s";
    private static final String DHIS_CAT_COMBO_FORMAT = "/api/categoryCombos/%s";
    private static final String DHIS_CAT_OPT_COMBO_FORMAT = "/api/categoryOptionCombos/%s";

    @Autowired
    DHIS2Client dhis2Client;

    @RequestMapping(value = {"/templates", ""}, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public ModelAndView templates() {
        ModelAndView modelAndView = new ModelAndView("dhis.datasetTemplate");
        return modelAndView;
    }

    @RequestMapping(value = "/{datasetId}/dataElements", method = RequestMethod.GET, produces= MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public @ResponseBody
    DHISResponse getDataElementsForDataSet(@PathVariable String datasetId) {
        String searchUri = String.format(DHIS_DATASET_DATA_ELEMENTS_FORMAT, datasetId);
        return dhis2Client.get(searchUri);
    }

    @RequestMapping(value = "/dataElements/{dataElementId}", method = RequestMethod.GET, produces= MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public @ResponseBody
    DHISResponse getDataElementDetails(@PathVariable String dataElementId) {
        String searchUri = String.format(DHIS_DATA_ELEMENTS_FORMAT, dataElementId);
        return dhis2Client.get(searchUri);
    }

    @RequestMapping(value = "/categoryCombos/{categoryComboId}", method = RequestMethod.GET, produces= MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public @ResponseBody
    DHISResponse getCategoryComboDetails(@PathVariable String categoryComboId) {
        String searchUri = String.format(DHIS_CAT_COMBO_FORMAT, categoryComboId);
        return dhis2Client.get(searchUri);
    }

    @RequestMapping(value = "/categoryOptionCombos/{categoryOptionComboId}", method = RequestMethod.GET, produces= MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_SHR System Admin')")
    public @ResponseBody
    DHISResponse getCategoryOptionComboDetails(@PathVariable String categoryOptionComboId) {
        String searchUri = String.format(DHIS_CAT_OPT_COMBO_FORMAT, categoryOptionComboId);
        return dhis2Client.get(searchUri);
    }
}
