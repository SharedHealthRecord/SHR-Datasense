package org.sharedhealth.datasense.repository;

import org.sharedhealth.datasense.model.tr.TrConcept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


@Component
public class ConceptDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void saveOrUpdate(final TrConcept trConcept) {
        HashMap<String, Object> map1 = new HashMap<>();
        map1.put("concept_uuid", trConcept.getConceptUuid());
        map1.put("name", trConcept.getName());
        map1.put("class", trConcept.getConceptClass());
        HashMap<String, Object> map = map1;
        String sql;
        if (findByConceptUuid(trConcept.getConceptUuid()) == null) {
            sql = "insert into concept(concept_uuid, name, class) values " +
                    "(:concept_uuid, :name, :class)";
        } else {
            sql = "update concept set name = :name, class = :class where concept_uuid = :concept_uuid";
        }

        jdbcTemplate.update(sql, map);
    }

    public void saveReferenceTermMap(String referenceTermUuid, String conceptUuid, String relationshipType) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("concept_uuid", conceptUuid);
        map.put("reference_term_uuid", referenceTermUuid);
        map.put("relationship_type", relationshipType);
        String sql;
        sql = "insert into reference_term_map (concept_uuid, reference_term_uuid, relationship_type) " +
                "values(:concept_uuid, :reference_term_uuid, :relationship_type)";
        jdbcTemplate.update(sql, map);
    }

    public void deleteConceptReferenceTermMap(String conceptUuid) {
        jdbcTemplate.update("delete from reference_term_map where concept_uuid = :concept_uuid", Collections.singletonMap("concept_uuid", conceptUuid));
    }

    public TrConcept findByConceptUuid(String conceptUuid) {
        List<TrConcept> trConcepts = jdbcTemplate.query(
                "select concept_uuid, name, class from concept where concept_uuid = :concept_uuid",
                Collections.singletonMap("concept_uuid", conceptUuid),
                new RowMapper<TrConcept>() {
                    @Override
                    public TrConcept mapRow(ResultSet rs, int rowNum) throws SQLException {
                        TrConcept trConcept = new TrConcept();
                        trConcept.setConceptUuid(rs.getString("concept_uuid"));
                        trConcept.setName(rs.getString("name"));
                        trConcept.setConceptClass(rs.getString("class"));
                        return trConcept;
                    }
                });
        return trConcepts.isEmpty() ? null : trConcepts.get(0);
    }
}
