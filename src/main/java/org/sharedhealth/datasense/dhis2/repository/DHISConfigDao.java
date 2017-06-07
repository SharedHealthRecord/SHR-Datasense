package org.sharedhealth.datasense.dhis2.repository;

import org.sharedhealth.datasense.dhis2.model.DHISOrgUnitConfig;
import org.sharedhealth.datasense.dhis2.model.DHISReportConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Repository
public class DHISConfigDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    private String DATASET_CFG_ALL_FIELDS = "id, name, config_file, dataset_name, dataset_id, period_type";

    private final String qryOrgUnitInclusive = "SELECT f.facility_id, f.name AS facility_name, ou.org_unit_id, ou.org_unit_name FROM facility f " +
            "LEFT OUTER JOIN dhis_orgunit_map ou ON (f.facility_id=ou.facility_id AND ou.voided = 0)";

    private final String qryOrgUnitExclusive = "SELECT f.facility_id, f.name AS facility_name, ou.org_unit_id, ou.org_unit_name FROM facility f " +
            "INNER JOIN dhis_orgunit_map ou ON f.facility_id=ou.facility_id WHERE ou.voided = 0";

    private final String qryOrgUnitByFacilityId = "SELECT f.facility_id, f.name AS facility_name, ou.org_unit_id, ou.org_unit_name FROM facility f " +
            "INNER JOIN dhis_orgunit_map ou ON f.facility_id=ou.facility_id WHERE f.facility_id = :facility_id AND ou.voided = 0";


    public java.util.List<DHISReportConfig> findAllMappedDatasets() {
        return jdbcTemplate.query(
                String.format("SELECT %s FROM dhis_dataset_map WHERE voided = 0", DATASET_CFG_ALL_FIELDS),
                rowMapperForDataset());
    }

    public void save(DHISReportConfig config) {
        List<DHISReportConfig> configs = jdbcTemplate.query(
                String.format("SELECT %s FROM dhis_dataset_map WHERE name=:name AND voided = 0", DATASET_CFG_ALL_FIELDS),
                Collections.singletonMap("name", config.getName()),
                rowMapperForDataset());
        DHISReportConfig dhisReportConfig = configs.isEmpty() ? null : configs.get(0);

        if (dhisReportConfig != null) {
            voidReportConfig(dhisReportConfig.getId());
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("name", config.getName());
        map.put("config_file", config.getConfigFile());
        map.put("dataset_name", config.getDatasetName());
        map.put("dataset_id", config.getDatasetId());
        map.put("period_type", config.getPeriodType());
        String query = "INSERT INTO dhis_dataset_map (name, config_file, dataset_name, dataset_id, period_type) " +
                "VALUES (:name, :config_file, :dataset_name, :dataset_id, :period_type)";
        jdbcTemplate.update(query, map);
    }

    public void voidReportConfig(Integer reportId) {
        String SQL = "UPDATE dhis_dataset_map SET voided = 1, date_voided = now() WHERE id = :id";
        SqlParameterSource namedParameters = new MapSqlParameterSource("id", Integer.valueOf(reportId));
        jdbcTemplate.update(SQL, namedParameters);
    }

    public DHISReportConfig getMappedConfigForDataset(String datasetId) {
        List<DHISReportConfig> configs = jdbcTemplate.query(
                String.format("SELECT %s FROM dhis_dataset_map WHERE dataset_id=:dataset_id AND voided = 0", DATASET_CFG_ALL_FIELDS),
                Collections.singletonMap("dataset_id", datasetId),
                rowMapperForDataset());
        return configs.isEmpty() ? null : configs.get(0);
    }

    public DHISReportConfig getReportConfig(Integer configId) {
        List<DHISReportConfig> configs = jdbcTemplate.query(
                String.format("SELECT %s FROM dhis_dataset_map WHERE id=:configId AND voided = 0", DATASET_CFG_ALL_FIELDS),
                Collections.singletonMap("configId", configId),
                rowMapperForDataset());
        return configs.isEmpty() ? null : configs.get(0);
    }

    public List<DHISOrgUnitConfig> findAllOrgUnits(boolean includeNotConfigured) {
        String queryStr = includeNotConfigured ? qryOrgUnitInclusive : qryOrgUnitExclusive;
        return jdbcTemplate.query(queryStr, rowMapperForOrgUnit());

    }

    public void save(DHISOrgUnitConfig config) {
        if (config.getFacilityId() != null) {
            voidOrgUnitMap(config.getFacilityId());
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("facility_id", config.getFacilityId());
        map.put("org_unit_id", config.getOrgUnitId());
        map.put("org_unit_name", config.getOrgUnitName());
        String query = "INSERT INTO dhis_orgunit_map (facility_id, org_unit_id, org_unit_name) " +
                "VALUES (:facility_id, :org_unit_id, :org_unit_name)";
        jdbcTemplate.update(query, map);
    }

    public DHISOrgUnitConfig findOrgUnitConfigFor(String facilityId) {
        List<DHISOrgUnitConfig> orgUnitConfigs = jdbcTemplate.query(qryOrgUnitByFacilityId,
                Collections.singletonMap("facility_id", facilityId),
                rowMapperForOrgUnit());

        return orgUnitConfigs.isEmpty() ? null : orgUnitConfigs.get(0);
    }

    public void voidOrgUnitMap(String facilityId) {
        String SQL = "UPDATE dhis_orgunit_map SET voided = 1, date_voided = now() WHERE facility_id = :facilityId";
        SqlParameterSource namedParameters = new MapSqlParameterSource("facilityId", Integer.valueOf(facilityId));
        jdbcTemplate.update(SQL, namedParameters);
    }

    private RowMapper<DHISReportConfig> rowMapperForDataset() {
        return new RowMapper<DHISReportConfig>() {
            @Override
            public DHISReportConfig mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new DHISReportConfig(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("config_file"),
                        rs.getString("dataset_name"),
                        rs.getString("dataset_id"),
                        rs.getString("period_type"));
            }
        };
    }

    private RowMapper<DHISOrgUnitConfig> rowMapperForOrgUnit() {
        return new RowMapper<DHISOrgUnitConfig>() {
            @Override
            public DHISOrgUnitConfig mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new DHISOrgUnitConfig(
                        rs.getString("facility_id"),
                        rs.getString("facility_name"),
                        rs.getString("org_unit_id"),
                        rs.getString("org_unit_name"));
            }
        };
    }
}
