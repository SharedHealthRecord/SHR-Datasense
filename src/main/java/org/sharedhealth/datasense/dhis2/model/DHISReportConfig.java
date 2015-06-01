package org.sharedhealth.datasense.dhis2.model;

public class DHISReportConfig {

    private int id;
    private String name;
    private String configFile;
    private String datasetName;
    private String datasetId;

    public DHISReportConfig() {
    }

    public DHISReportConfig(String name, String configFile) {
        this.name = name;
        this.configFile = configFile;
    }

    public DHISReportConfig(int id, String name, String configFile, String datasetName, String datasetId) {
        this.id = id;
        this.name = name;
        this.configFile = configFile;
        this.datasetName = datasetName;
        this.datasetId = datasetId;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getConfigFile() {
        return configFile;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }
}
