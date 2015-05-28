package org.sharedhealth.datasense.dhis2.model;

public class DHISOrgUnit {
    private final String name;
    private final String uuid;

    public DHISOrgUnit(String name, String uuid) {
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
