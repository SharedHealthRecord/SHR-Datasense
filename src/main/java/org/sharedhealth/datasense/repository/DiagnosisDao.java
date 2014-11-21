package org.sharedhealth.datasense.repository;

import org.sharedhealth.datasense.model.Diagnosis;
import org.sharedhealth.datasense.model.Encounter;
import org.sharedhealth.datasense.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class DiagnosisDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void save(final Diagnosis diagnosis) {
        jdbcTemplate.update("insert into diagnosis(patient_hid, encounter_id, diagnosis_datetime, " +
                        "diagnosis_code, diagnosis_concept_id, diagnosis_status) values (?,?,?,?,?,?)",
                diagnosis.getPatient().getHid(), diagnosis.getEncounter().getEncounterId(), diagnosis.getDiagnosisDateTime(),
                diagnosis.getDiagnosisCode(), diagnosis.getDiagnosisConcept(), diagnosis.getDiagnosisStatus());

    }

    public List<Diagnosis> findByEncounterId(String encounterId) {

        return jdbcTemplate.query(
                "select diagnosis_id, patient_hid, encounter_id, diagnosis_datetime, diagnosis_code, diagnosis_concept_id, " +
                "diagnosis_status from diagnosis where encounter_id=?", new Object[]{encounterId},
                new RowMapper<Diagnosis>() {
                    @Override
                    public Diagnosis mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Diagnosis diagnosis = new Diagnosis();
                        diagnosis.setDiagnosisId(rs.getInt("diagnosis_id"));
                        diagnosis.setDiagnosisDateTime(new java.util.Date(rs.getTimestamp("diagnosis_datetime").getTime()));
                        diagnosis.setDiagnosisCode(rs.getString("diagnosis_code"));
                        diagnosis.setDiagnosisConceptId(rs.getString("diagnosis_concept_id"));
                        diagnosis.setDiagnosisStatus(rs.getString("diagnosis_status"));
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
