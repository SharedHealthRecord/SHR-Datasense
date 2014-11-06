package org.sharedhealth.datasense.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class Patient {
    @JsonProperty("hid")
    private String hid;
    @JsonProperty("date_of_birth")
    private Date dateOfBirth;
    @JsonProperty("gender")
    private String gender;
    @JsonProperty("present_address")
    private Address presentAddress;

    private String presentAddressCode;

    public void setHid(String hid) {
        this.hid = hid;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setPresentAddress(Address presentAddress) {
        this.presentAddress = presentAddress;
    }

    public String getHid() {
        return hid;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public Address getPresentAddress() {
        return presentAddress;
    }

    public void setPresentAddressCode(String presentAddressCode) {
        this.presentAddressCode = presentAddressCode;
    }

    public String getPresentLocationCode() {
        if (presentAddress != null) {
            return presentAddress.getLocationCode();
        } else {
            return presentAddressCode;
        }
    }

    public String getGender() {
        return gender;
    }
}
