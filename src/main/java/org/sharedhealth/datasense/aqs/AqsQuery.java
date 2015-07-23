package org.sharedhealth.datasense.aqs;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AqsQuery {
    @JsonProperty("queryGroupname")
    private String queryGroupname;
    @JsonProperty("queries")
    private List<QueryDefinition> queryDefinitions;

    public String getQueryGroupname() {
        return queryGroupname;
    }

    public void setQueryGroupname(String queryGroupname) {
        this.queryGroupname = queryGroupname;
    }

    public List<QueryDefinition> getQueryDefinitions() {
        return queryDefinitions;
    }

    public void setQueryDefinitions(List<QueryDefinition> queryDefinitions) {
        this.queryDefinitions = queryDefinitions;
    }
}
