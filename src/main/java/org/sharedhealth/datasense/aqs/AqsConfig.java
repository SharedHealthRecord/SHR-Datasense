package org.sharedhealth.datasense.aqs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AqsConfig {
    @JsonProperty("query_json_path")
    private String querySrc;

    @JsonProperty("template_name")
    private String templateName;

    @JsonProperty("query_names")
    private String[] queryNames;
    private List<QueryDefinition> applicableQueries;

    public String getQuerySrc() {
        return querySrc;
    }

    public void setQuerySrc(String querySrc) {
        this.querySrc = querySrc;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String[] getQueryNames() {
        return queryNames;
    }

    public void setQueryNames(String[] queryNames) {
        this.queryNames = queryNames;
    }

    public void setApplicableQueries(List<QueryDefinition> applicableQueries) {
        this.applicableQueries = applicableQueries;
    }

    public List<QueryDefinition> getApplicableQueries() {
        return this.applicableQueries;
    }
}
