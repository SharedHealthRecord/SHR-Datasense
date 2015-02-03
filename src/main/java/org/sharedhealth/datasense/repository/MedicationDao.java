package org.sharedhealth.datasense.repository;

import org.sharedhealth.datasense.model.Encounter;
import org.sharedhealth.datasense.model.Medication;
import org.sharedhealth.datasense.model.MedicationStatus;
import org.sharedhealth.datasense.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Component
public class MedicationDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<Medication> findByEncounterId(String shrEncounterId) {
        String sql = "select datetime, encounter_id, patient_hid, status, drug_id, uuid from " +
                "medication where encounter_id= :encounter_id";
        return jdbcTemplate.query(sql, Collections.singletonMap("encounter_id", shrEncounterId), new
                RowMapper<Medication>() {
            @Override
            public Medication mapRow(ResultSet rs, int rowNum) throws SQLException {
                Medication medication = new Medication();

                Date medicationDatetime = new Date(rs.getTimestamp("datetime").getTime());
                medication.setDateTime(medicationDatetime);

                Encounter encounter = new Encounter();
                encounter.setEncounterId(rs.getString("encounter_id"));
                medication.setEncounter(encounter);

                Patient patient = new Patient();
                patient.setHid(rs.getString("patient_hid"));
                medication.setPatient(patient);

                medication.setDrugId(rs.getString("drug_id"));
                medication.setStatus(MedicationStatus.getMedicationStatus(rs.getString("status")));
                medication.setUuid(rs.getString("uuid"));
                return medication;
            }
        });
    }

    public void save(Medication medication) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("patient_hid", medication.getPatient().getHid());
        map.put("encounter_id", medication.getEncounter().getEncounterId());
        map.put("datetime", medication.getDateTime());
        map.put("status", medication.getStatus().getValue());
        map.put("drug_id", medication.getDrugId());
        map.put("uuid", medication.getUuid());
        String sql = "insert into medication (patient_hid, encounter_id, datetime, status,  drug_id, uuid) " +
                "values(:patient_hid, :encounter_id, :datetime, :status,  :drug_id, :uuid)";
        jdbcTemplate.update(sql, map);
    }
}
