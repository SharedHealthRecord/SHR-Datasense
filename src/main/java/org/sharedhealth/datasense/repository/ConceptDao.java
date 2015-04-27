package org.sharedhealth.datasense.repository;

import org.sharedhealth.datasense.model.tr.TrConcept;
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
public class ConceptDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void saveOrUpdate(final TrConcept trConcept) {
        HashMap<String, Object> map1 = new HashMap<>();
        map1.put("concept_uuid", trConcept.getConceptUuid());
        map1.put("name", trConcept.getName());
        map1.put("class", trConcept.getConceptClass());
        map1.put("updated_at", new Date());
        HashMap<String, Object> map = map1;
        String sql;
        if (findByConceptUuid(trConcept.getConceptUuid()) == null) {
            sql = "insert into concept(concept_uuid, name, class) values " +
                    "(:concept_uuid, :name, :class)";
        } else {
            sql = "update concept set name = :name, class = :class, updated_at=:updated_at where concept_uuid = :concept_uuid";
        }

        jdbcTemplate.update(sql, map);
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
