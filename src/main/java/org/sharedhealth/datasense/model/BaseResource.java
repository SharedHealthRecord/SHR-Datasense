package org.sharedhealth.datasense.model;

import java.util.UUID;

public class BaseResource {
    private String uuid;

    public BaseResource() {
        this.uuid = UUID.randomUUID().toString();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
