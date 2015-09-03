package org.sharedhealth.datasense.model;

import ca.uhn.fhir.model.dstu2.resource.Bundle;

public class EncounterBundle {


    private String encounterId;
    private String healthId;
    private String publishedDate;

    private String title;

    private String link;

    private String[] categories;

    private Bundle bundle;

    public String getEncounterId() {
        return encounterId;
    }

    public String getHealthId() {
        return healthId;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setEncounterId(String encounterId) {
        this.encounterId = encounterId;
    }

    public void setHealthId(String healthId) {
        this.healthId = healthId;
    }

    public void setPublishedDate(String date) {
        this.publishedDate = date;
    }

    public void addContent(Bundle resourceOrFeed) {
        this.bundle = resourceOrFeed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String[] getCategories() {
        return categories;
    }

    public void setCategories(String[] categories) {
        this.categories = categories;
    }
}
