package org.sharedhealth.datasense.model.tr;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.List;
import java.util.Map;

public class TrConcept {
    @JsonProperty("uuid")
    private String conceptUuid;
    private String name;
    @JsonProperty("conceptClass")
    private String conceptClass;
    @JsonProperty("referenceTerms")
    private List<TrReferenceTerm> referenceTermMaps;

    @JsonSetter("fullySpecifiedName")
    public void setName(Map name) {
        this.name = (String) name.get("conceptName");
    }

    public String getConceptUuid() {
        return conceptUuid;
    }

    public void setConceptUuid(String conceptUuid) {
        this.conceptUuid = conceptUuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConceptClass() {
        return conceptClass;
    }

    public void setConceptClass(String conceptClass) {
        this.conceptClass = conceptClass;
    }

    public List<TrReferenceTerm> getReferenceTermMaps() {
        return referenceTermMaps;
    }

    public void setReferenceTermMaps(List<TrReferenceTerm> referenceTermMaps) {
        this.referenceTermMaps = referenceTermMaps;
    }
}
