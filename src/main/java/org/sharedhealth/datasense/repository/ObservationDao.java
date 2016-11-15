package org.sharedhealth.datasense.repository;

import org.sharedhealth.datasense.model.Observation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class ObservationDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public int save(Observation observation) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("patient_hid", observation.getPatient().getHid());
        map.put("encounter_id", observation.getEncounter().getEncounterId());
        map.put("datetime", observation.getDateTime());
        map.put("concept_id", observation.getConceptId());
        map.put("code", observation.getReferenceCode());
        map.put("parent_id", observation.getParentId());
        map.put("value", observation.getValue());
        map.put("uuid", observation.getUuid());
        map.put("report_id", observation.getReportId());
        String sql = "insert into observation (patient_hid, encounter_id, datetime, concept_id, code, value, uuid, report_id) " +
                "values(:patient_hid, :encounter_id, :datetime, :concept_id, :code, :value, :uuid, :report_id)";
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, new MapSqlParameterSource(map), generatedKeyHolder);
        return generatedKeyHolder.getKey().intValue();
    }

    public void updateParentId(Observation observation) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("parent_id", observation.getParentId());
        map.put("observation_id", observation.getObservationId());
        String sql = "update observation set parent_id = :parent_id where observation_id = :observation_id";
        jdbcTemplate.update(sql, new MapSqlParameterSource(map));
    }

    public void deleteExisting(String encounterId){
        HashMap<String, Object> map = new HashMap<>();
        map.put("encounter_id", encounterId);

        jdbcTemplate.update("delete from observation where encounter_id = :encounter_id", map);

    }
}
