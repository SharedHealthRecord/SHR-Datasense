package org.sharedhealth.datasense.model.fhir;

import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.datasense.model.Facility;


public class ServiceProviderReference {
    public static final String EXTENSION = ".json";
    private ResourceReferenceDt serviceProvider;
    private Facility value;

    public String getFacilityId() {
        return parseUrl();
    }

    private String parseUrl() {
        String s = StringUtils.substringAfterLast(serviceProvider.getReference().getValue(), "/");
        return StringUtils.substringBefore(s, EXTENSION);
    }

    public void setValue(Facility value) {
        this.value = value;
    }

    public Facility getValue() {
        return value;
    }

    public void setServiceProvider(ResourceReferenceDt serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public ResourceReferenceDt getServiceProvider() {
        return serviceProvider;
    }
}