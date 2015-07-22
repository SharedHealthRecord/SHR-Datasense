package org.sharedhealth.datasense.feeds.patients;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.datasense.model.Address;
import org.sharedhealth.datasense.util.DateUtil;

import java.util.HashMap;
import java.util.Map;

import static org.sharedhealth.datasense.DatabaseKeyConstants.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientData {

    @JsonProperty("gender")
    private Change genderChange = new Change();

    @JsonProperty("date_of_birth")
    private Change dobChange = new Change();

    @JsonProperty("present_address")
    private AddressChange addressChange = new AddressChange();

    public Address getAddressChange() {
        return addressChange.getNewValue();
    }

    public void setAddressChange(AddressChange addressChange) {
        this.addressChange = addressChange;
    }

    public void setGenderChange(Change genderChange) {
        this.genderChange = genderChange;
    }

    public String getGenderChange() {
        return (String) genderChange.getNewValue();
    }

    public String getDobChange() {
        return (String) dobChange.getNewValue();
    }

    public void setDobChange(Change dobChange){
        this.dobChange = dobChange;
    }

    public boolean hasChanges() {
        return getChanges().size() > 0;
    }

    public Map<String, String> getChanges() {
        HashMap<String, String> changes = new HashMap<>();
        changes.put(GENDER, getGenderChange());
        changes.put(PRESENT_LOCATION_ID, getLocationCodeChange());
        changes.put(DOB, getFormattedDobChange());

        return Maps.filterValues(changes, new Predicate<Object>() {
            @Override
            public boolean apply(Object input) {
                return null != input;
            }
        });
    }

    private String getLocationCodeChange() {
        return getAddressChange()!= null ? getAddressChange().getLocationCode() : null;
    }

    private String getFormattedDobChange(){
        String dobChange = getDobChange();
        return StringUtils.isEmpty(dobChange) ? null : DateUtil.format(DateUtil.parseDate(dobChange));
    }
}
