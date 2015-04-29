package org.sharedhealth.datasense.repository;

import org.sharedhealth.datasense.model.Encounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

@Repository
public class EncounterDao {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void save(Encounter encounter) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("encounter_id", encounter.getEncounterId());
        map.put("encounter_datetime", encounter.getEncounterDateTime());
        map.put("encounter_type", encounter.getEncounterType());
        map.put("visit_type", encounter.getEncounterVisitType());
        map.put("patient_hid", encounter.getPatient().getHid());
        map.put("patient_age_years", encounter.getPatientAgeInYears());
        map.put("patient_age_months", encounter.getPatientAgeInMonths());
        map.put("patient_age_days", encounter.getPatientAgeInDays());
        map.put("location_id", encounter.getLocationCode());
        map.put("facility_id", encounter.getFacility().getFacilityId());
        jdbcTemplate.update("insert into encounter (encounter_id, encounter_datetime, encounter_type, visit_type, " +
                "patient_hid, " +
                "patient_age_years, patient_age_months, patient_age_days, encounter_location_id, facility_id) " +
                "values(:encounter_id, :encounter_datetime, :encounter_type , :visit_type , :patient_hid, " +
                ":patient_age_years, :patient_age_months," +
                " :patient_age_days , :location_id, :facility_id)", map);
    }

    public void deleteExisting(String healthId, String encounterId){
        HashMap<String, Object> map = new HashMap<>();
        map.put("encounter_id", encounterId);
        map.put("patient_hid", healthId);

        jdbcTemplate.update("delete from encounter where patient_hid = :patient_hid and encounter_id = :encounter_id", map);

    }
}
