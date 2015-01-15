package org.sharedhealth.datasense.model.tr;


import com.fasterxml.jackson.annotation.JsonProperty;

public class Coding {
    @JsonProperty("system")
    private String system;
    @JsonProperty("code")
    private String code;
    @JsonProperty("display")
    private String display;

    public String getSystem() {
        return system;
    }

    public String getCode() {
        return code;
    }

    public String getDisplay() {
        return display;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDisplay(String display) {
        this.display = display;
    }
}
