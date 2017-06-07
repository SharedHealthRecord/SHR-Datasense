package org.sharedhealth.datasense.repository;

import org.sharedhealth.datasense.model.Provider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Component
public class ProviderDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public Provider findProviderById(String providerId) {
        List<Provider> providers = jdbcTemplate.query(
                "select  id, name, facility_id  from provider where id= :provider_id",
                Collections.singletonMap("provider_id", providerId),
                new RowMapper<Provider>() {
                    @Override
                    public Provider mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Provider provider = new Provider();
                        provider.setId(rs.getString("id"));
                        provider.setName(rs.getString("name"));
                        provider.setFacilityId(rs.getString("facility_id"));
                        return provider;
                    }
                });
        return providers.isEmpty() ? null : providers.get(0);
    }

    public void save(Provider provider) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", provider.getId());
        map.put("name", provider.getName());
        map.put("facility_id", provider.getFacilityId());
        String query = "insert into provider (id, name, facility_id) values " +
                "(:id, :name, :facility_id)";
        jdbcTemplate.update(query, map);
    }

    public void deleteEncounterProvider(String encounterId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("encounter_id", encounterId);
        jdbcTemplate.update("delete from encounter_provider where encounter_id = :encounter_id", map);
    }

    public void saveEncounterProvider(String encounterId, String providerId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("encounter_id", encounterId);
        map.put("provider_id", providerId);
        String query = "insert into encounter_provider (encounter_id, provider_id) values " +
                "(:encounter_id, :provider_id)";
        jdbcTemplate.update(query, map);
    }

    public List<?> findEncounterProviderIds(String encounterId) {
        List<String> providerIds = jdbcTemplate.query(
                "select provider_id from encounter_provider where encounter_id = :encounter_id",
                Collections.singletonMap("encounter_id", encounterId),
                new RowMapper<String>() {
                    @Override
                    public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getString("provider_id");
                    }
                });
        return providerIds.isEmpty() ? new ArrayList<>() : providerIds;
    }
}
