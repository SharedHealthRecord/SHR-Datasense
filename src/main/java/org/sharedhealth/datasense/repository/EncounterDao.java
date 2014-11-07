package org.sharedhealth.datasense.repository;

import org.apache.log4j.Logger;
import org.sharedhealth.datasense.model.Encounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class EncounterDao {
    @Autowired
    JdbcTemplate jdbcTemplate;

    Logger log = Logger.getLogger(PatientDao.class);

    public void save(Encounter encounter) {
        jdbcTemplate.update("insert into encounter (encounter_id, encounter_datetime, encounter_type, visit_type, patient_hid, " +
                        "patient_age_years, patient_age_months, patient_age_days, encounter_location_id, facility_id) " +
                        "values(?, ?, ? ,? ,?, ?, ?, ? ,?, ?)",
                encounter.getEncounterId(), encounter.getEncounterDateTime(), encounter.getEncounterType(), encounter.getEncounterVisitType(),
                encounter.getPatient().getHid(), encounter.getPatientAgeInYears(), encounter.getPatientAgeInMonths(),
                encounter.getPatientAgeInDays(), encounter.getLocationCode(), encounter.getFacility().getFacilityId());
    }
}
