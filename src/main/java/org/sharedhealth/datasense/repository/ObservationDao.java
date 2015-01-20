package org.sharedhealth.datasense.repository;

import org.sharedhealth.datasense.model.Encounter;
import org.sharedhealth.datasense.model.Observation;
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
public class ObservationDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<Observation> findByEncounterId(String shrEncounterId) {
        String sql = "select observation_id, patient_hid, encounter_id, concept_id, code, datetime, parent_id, value, uuid from observation where encounter_id= :encounter_id";
        return jdbcTemplate.query(sql, Collections.singletonMap("encounter_id", shrEncounterId), new RowMapper<Observation>() {

            @Override
            public Observation mapRow(ResultSet rs, int rowNum) throws SQLException {
                Observation observation = new Observation();
                observation.setObservationId(rs.getInt("observation_id"));

                Encounter encounter = new Encounter();
                encounter.setEncounterId(rs.getString("encounter_id"));
                observation.setEncounter(encounter);

                Patient patient = new Patient();
                patient.setHid(rs.getString("patient_hid"));
                observation.setPatient(patient);

                observation.setConceptId(rs.getString("concept_id"));
                observation.setReferenceCode(rs.getString("code"));
                observation.setDatetime(new Date(rs.getTimestamp("datetime").getTime()));
                observation.setParentId(rs.getString("parent_id"));
                observation.setValue(rs.getString("value"));
                observation.setUuid(rs.getString("uuid"));
                return observation;
            }
        });
    }

    public void save(Observation observation) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("patient_hid", observation.getPatient().getHid());
        map.put("encounter_id", observation.getEncounter().getEncounterId());
        map.put("datetime", observation.getDateTime());
        map.put("concept_id", observation.getConceptId());
        map.put("code", observation.getReferenceCode());
        map.put("parent_id", observation.getParentId());
        map.put("value", observation.getValue());
        map.put("uuid", observation.getUuid());
        String sql = "insert into observation (patient_hid, encounter_id, datetime, concept_id, code, parent_id, value, uuid) " +
                "values(:patient_hid, :encounter_id, :datetime, :concept_id, :code, :parent_id, :value, :uuid)";
        jdbcTemplate.update(sql, map);
    }
}
