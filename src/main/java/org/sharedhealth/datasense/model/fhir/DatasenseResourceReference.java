package org.sharedhealth.datasense.model.fhir;

import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceReference;
import org.sharedhealth.datasense.model.BaseResource;

public class DatasenseResourceReference {
    BaseResource value;
    org.hl7.fhir.instance.model.ResourceReference resourceReference;
    Resource resourceValue;

    public DatasenseResourceReference(ResourceReference resourceReference, Resource resourceValue) {
        this.resourceReference = resourceReference;
        this.resourceValue = resourceValue;
    }

    public BaseResource getValue() {
        return value;
    }

    public void setValue(BaseResource value) {
        this.value = value;
    }

    public ResourceReference getResourceReference() {
        return resourceReference;
    }

    public void setResourceReference(ResourceReference resourceReference) {
        this.resourceReference = resourceReference;
    }

    public Resource getResourceValue() {
        return resourceValue;
    }

    public void setResourceValue(Resource resourceValue) {
        this.resourceValue = resourceValue;
    }
}
