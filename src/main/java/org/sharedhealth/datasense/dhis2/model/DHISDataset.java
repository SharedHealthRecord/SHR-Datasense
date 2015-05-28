package org.sharedhealth.datasense.dhis2.model;

public class DHISDataset {
    private String name;
    private String uuid;

    public DHISDataset(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }
}
