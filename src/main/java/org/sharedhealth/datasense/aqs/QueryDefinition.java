package org.sharedhealth.datasense.aqs;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryDefinition {
    @JsonProperty("queryName")
    private String queryName;
    @JsonProperty("query")
    private String queryString;

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }
}
