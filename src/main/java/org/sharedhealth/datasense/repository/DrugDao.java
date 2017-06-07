package org.sharedhealth.datasense.repository;

import org.sharedhealth.datasense.model.tr.TrMedication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DrugDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void saveOrUpdate(final TrMedication drug) {

        String sql = null;
        if (getDrugCountByUuid(drug.getUuid()) == 0) {
            sql = "insert into drug(drug_uuid, concept_uuid, name, reference_term_uuid) values (:drug_uuid, :concept_uuid, :name, :reference_term_uuid)";
        } else {
            sql = "update drug set concept_uuid= :concept_uuid, name=:name, reference_term_uuid=:reference_term_uuid, updated_at=:updated_at where drug_uuid= :drug_uuid";
        }
        jdbcTemplate.update(sql, getParameterMap(drug));
    }

    private long getDrugCountByUuid(final String drugUuid) {
        List<Map<String, Object>> records = jdbcTemplate.query("select count(*) as drug_count from drug where drug_uuid=:drug_uuid", Collections.singletonMap("drug_uuid", drugUuid), new ColumnMapRowMapper());
        return (long) records.get(0).get("drug_count");

    }


    private HashMap<String, Object> getParameterMap(TrMedication drug) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("drug_uuid", drug.getUuid());
        map.put("concept_uuid", drug.getAssociatedConceptId());
        map.put("name", drug.getName());
        map.put("reference_term_uuid", drug.getReferenceTermId());
        map.put("updated_at", new Date());
        return map;
    }
}

