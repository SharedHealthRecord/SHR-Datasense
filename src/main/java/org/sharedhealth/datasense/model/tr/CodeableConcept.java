package org.sharedhealth.datasense.model.tr;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class CodeableConcept {
    @JsonProperty("coding")
    private List<Coding> coding = new ArrayList<>();

    @JsonProperty("text")
    private String text;

    public void addCoding(Coding code) {
        this.coding.add(code);
    }

    public void setCoding(List<Coding> coding) {
        this.coding = coding;
    }

    public List<Coding> getCoding() {
        return coding;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
