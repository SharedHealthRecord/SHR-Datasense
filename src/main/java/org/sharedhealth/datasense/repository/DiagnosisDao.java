package org.sharedhealth.datasense.repository;

import org.sharedhealth.datasense.model.Diagnosis;
import org.sharedhealth.datasense.model.Encounter;
import org.sharedhealth.datasense.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Repository
public class DiagnosisDao {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void save(final Diagnosis diagnosis) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("patient_hid", diagnosis.getPatient().getHid());
        map.put("encounter_id", diagnosis.getEncounter().getEncounterId());
        map.put("diagnosis_datetime", diagnosis.getDiagnosisDateTime());
        map.put("code", diagnosis.getDiagnosisCode());
        map.put("concept_id", diagnosis.getDiagnosisConcept());
        map.put("status", diagnosis.getDiagnosisStatus());
        map.put("uuid", diagnosis.getUuid());
        jdbcTemplate.update("insert into diagnosis(patient_hid, encounter_id, diagnosis_datetime, " +
                "diagnosis_code, diagnosis_concept_id, diagnosis_status, uuid) values " +
                "(:patient_hid, :encounter_id, :diagnosis_datetime, :code, :concept_id, :status, :uuid)", map);

    }

    public List<Diagnosis> findByEncounterId(String encounterId) {
        return jdbcTemplate.query(
                "select diagnosis_id, patient_hid, encounter_id, diagnosis_datetime, diagnosis_code, " +
                        "diagnosis_concept_id, " +
                        "diagnosis_status, uuid from diagnosis where encounter_id= :encounter_id", Collections
                        .singletonMap("encounter_id", encounterId),
                new RowMapper<Diagnosis>() {
                    @Override
                    public Diagnosis mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Diagnosis diagnosis = new Diagnosis();
                        diagnosis.setDiagnosisId(rs.getInt("diagnosis_id"));
                        diagnosis.setDiagnosisDateTime(new java.util.Date(rs.getTimestamp("diagnosis_datetime")
                                .getTime()));
                        diagnosis.setDiagnosisCode(rs.getString("diagnosis_code"));
                        diagnosis.setDiagnosisConceptId(rs.getString("diagnosis_concept_id"));
                        diagnosis.setDiagnosisStatus(rs.getString("diagnosis_status"));
                        diagnosis.setUuid(rs.getString("uuid"));

                        Encounter encounter = new Encounter();
                        encounter.setEncounterId(rs.getString("encounter_id"));
                        diagnosis.setEncounter(encounter);

                        Patient patient = new Patient();
                        patient.setHid(rs.getString("patient_hid"));
                        diagnosis.setPatient(patient);
                        return diagnosis;
                    }
                });
    }
}
