package org.sharedhealth.datasense.repository;


import org.sharedhealth.datasense.model.Facility;
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
public class FacilityDao {

    private static final String ALL_FIELDS = "facility_id, name, type, location_id, dhis_org_unit_uid";
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public Facility findFacilityById(String facilityId) {
        List<Facility> facilities = jdbcTemplate.query("select " + ALL_FIELDS + " from facility where facility_id= " +
                        ":facility_id",
                Collections.singletonMap("facility_id", facilityId),
                getRowMapperForFacility());
        return facilities.isEmpty() ? null : facilities.get(0);
    }

    public void save(Facility facility) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("facility_id", facility.getFacilityId());
        map.put("facility_name", facility.getFacilityName());
        map.put("facility_type", facility.getFacilityType());
        map.put("facility_location", facility.getFacilityLocationCode());
        map.put("dhis_org_unit_uid", facility.getDhisOrgUnitUid());
        String query = "insert into facility (" + ALL_FIELDS + ") values " +
                "(:facility_id, :facility_name, :facility_type, :facility_location, :dhis_org_unit_uid)";
        jdbcTemplate.update(query, map);
    }

    public List<Facility> findFacilitiesByTypes(List<String> facilityTypes) {
        return jdbcTemplate.query("select " + ALL_FIELDS + " from facility where type in (:ids)", Collections
                .singletonMap("ids", facilityTypes), getRowMapperForFacility());
    }

    private RowMapper<Facility> getRowMapperForFacility() {
        return new RowMapper<Facility>() {
            @Override
            public Facility mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new Facility(
                        rs.getString("facility_id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getString("location_id"),
                        rs.getString("dhis_org_unit_uid"));
            }
        };
    }
}
