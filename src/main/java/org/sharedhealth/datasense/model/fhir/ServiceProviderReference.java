package org.sharedhealth.datasense.model.fhir;

import org.hl7.fhir.instance.model.ResourceReference;
import org.sharedhealth.datasense.model.Facility;


public class ServiceProviderReference {
    private ResourceReference serviceProvider;
    private Facility value;

    public ServiceProviderReference(ResourceReference serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public String getFacilityId() {
        return serviceProvider.getReferenceSimple();
    }

    public void setValue(Facility value) {
        this.value = value;
    }

    public Facility getValue() {
        return value;
    }
}