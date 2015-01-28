package org.sharedhealth.datasense.model.tr;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.Map;

public class TrReferenceTerm {
    @JsonProperty("uuid")
    private String referenceTermUuid;
    @JsonProperty("name")
    private String name;
    @JsonProperty("code")
    private String code;
    private String source;
    @JsonProperty("mapType")
    private String relationshipType;

    @JsonSetter("conceptSource")
    public void setSource(Map source) {
        this.source = (String) source.get("hl7Code");
    }

    public String getReferenceTermUuid() {
        return referenceTermUuid;
    }

    public void setReferenceTermUuid(String referenceTermUuid) {
        this.referenceTermUuid = referenceTermUuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String conceptSource) {
        this.source = conceptSource;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }
}
