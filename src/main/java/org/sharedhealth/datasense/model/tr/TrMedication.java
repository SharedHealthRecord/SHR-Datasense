package org.sharedhealth.datasense.model.tr;


import com.fasterxml.jackson.annotation.JsonProperty;

public class TrMedication {
    @JsonProperty("name")
    private String name;
    @JsonProperty("code")
    private CodeableConcept code;


    public String getName() {
        return name;
    }

    public CodeableConcept getCode() {
        return code;
    }
}