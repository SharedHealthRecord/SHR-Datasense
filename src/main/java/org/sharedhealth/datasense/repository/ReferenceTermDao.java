package org.sharedhealth.datasense.repository;

import org.sharedhealth.datasense.model.tr.TrReferenceTerm;
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
public class ReferenceTermDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void saveOrUpdate(final TrReferenceTerm trReferenceTerm) {
        HashMap<String, Object> map = getParameterMap(trReferenceTerm);
        String sql;
        if (findByReferenceTermUuid(trReferenceTerm.getReferenceTermUuid()) == null) {
            sql = "insert into reference_term (reference_term_uuid, name, code, source) values " +
                    "(:reference_term_uuid, :name, :code, :source)";
        } else {
            sql = "update reference_term set name = :name, code = :code, source = :source, updated_at =:updated_at where " +
                    "reference_term_uuid = :reference_term_uuid";
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

    public TrReferenceTerm findByReferenceTermUuid(String referenceTermUuid) {
        List<TrReferenceTerm> trReferenceTerms = jdbcTemplate.query(
                "select reference_term_uuid, name, code, source from reference_term where reference_term_uuid = :reference_term_uuid",
                Collections.singletonMap("reference_term_uuid", referenceTermUuid),
                new RowMapper<TrReferenceTerm>() {
                    @Override
                    public TrReferenceTerm mapRow(ResultSet rs, int rowNum) throws SQLException {
                        TrReferenceTerm trReferenceTerm = new TrReferenceTerm();
                        trReferenceTerm.setReferenceTermUuid(rs.getString("reference_term_uuid"));
                        trReferenceTerm.setName(rs.getString("name"));
                        trReferenceTerm.setCode(rs.getString("code"));
                        trReferenceTerm.setSource(rs.getString("source"));
                        return trReferenceTerm;
                    }
                });
        return trReferenceTerms.isEmpty() ? null : trReferenceTerms.get(0);
    }

    private HashMap<String, Object> getParameterMap(TrReferenceTerm trReferenceTerm) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("reference_term_uuid", trReferenceTerm.getReferenceTermUuid());
        map.put("name", trReferenceTerm.getName());
        map.put("code", trReferenceTerm.getCode());
        map.put("source", trReferenceTerm.getSource());
        map.put("updated_at", new Date());
        return map;
    }
}
