package org.sharedhealth.datasense.model.fhir;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.ResourceReference;
import org.sharedhealth.datasense.model.Facility;


public class ServiceProviderReference {
    private ResourceReference serviceProvider;
    private Facility value;

    public String getFacilityId() {
        return parseUrl();
    }

    private String parseUrl() {
        String s = StringUtils.substringAfterLast(serviceProvider.getReferenceSimple(), "/");
        return StringUtils.substringBefore(s, ".json");
    }

    public void setValue(Facility value) {
        this.value = value;
    }

    public Facility getValue() {
        return value;
    }

    public void setServiceProvider(ResourceReference serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public ResourceReference getServiceProvider() {
        return serviceProvider;
    }
}