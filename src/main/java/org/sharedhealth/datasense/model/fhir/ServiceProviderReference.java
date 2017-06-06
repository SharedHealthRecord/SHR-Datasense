package org.sharedhealth.datasense.model.fhir;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Reference;
import org.sharedhealth.datasense.model.Facility;


public class ServiceProviderReference {
    public static final String EXTENSION = ".json";
    private Reference referenceDt;
    private Facility value;

    public String getFacilityId() {
        return parseUrl();
    }

    private String parseUrl() {
        if (referenceDt == null) return null;
        String s = StringUtils.substringAfterLast(referenceDt.getReference(), "/");
        return StringUtils.substringBefore(s, EXTENSION);
    }

    public void setValue(Facility value) {
        this.value = value;
    }

    public Facility getValue() {
        return value;
    }

    public void setReference(Reference serviceProvider) {
        this.referenceDt = serviceProvider;
    }

    public Reference getReference() {
        return referenceDt;
    }
}