package org.sharedhealth.datasense.model.tr;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import static org.sharedhealth.datasense.util.TrUrl.isConceptUrl;
import static org.sharedhealth.datasense.util.TrUrl.isReferenceTermUrl;


public class TrMedication {
    private String uuid;
    @JsonProperty("name")
    private String name;
    @JsonProperty("code")
    private CodeableConcept code;
    @JsonProperty("extension")
    private List<ResourceExtension> extension;

    public String getName() {
        return name;
    }

    public String getUuid(){ return uuid; }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(CodeableConcept code) {
        this.code = code;
    }

    public void setExtension(List<ResourceExtension> extension) {
        this.extension = extension;
    }

    public String getReferenceCode() {
        for (org.sharedhealth.datasense.model.tr.Coding coding : code.getCoding()) {
            if (isReferenceTermUrl(coding.getSystem()))
                return coding.getCode();
        }
        return null;
    }

    public String getConceptId() {
        for (org.sharedhealth.datasense.model.tr.Coding coding : code.getCoding()) {
            if (isConceptUrl(coding.getSystem()))
                return coding.getCode();
        }
        return null;
    }

    public Boolean getRetired(){
        if(extension != null){
            for (ResourceExtension resourceExtension : extension) {
                if (resourceExtension.getUrl().endsWith("retired"))
                    return Boolean.parseBoolean(resourceExtension.getValueString());
            }
        }
        return false;
    }
}