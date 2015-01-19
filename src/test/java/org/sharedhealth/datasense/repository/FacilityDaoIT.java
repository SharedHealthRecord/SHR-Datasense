package org.sharedhealth.datasense.repository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.helpers.DatabaseHelper;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.sharedhealth.datasense.model.Facility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class FacilityDaoIT {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    @Autowired
    FacilityDao facilityDao;

    @Before
    public void setUp() throws Exception {
        jdbcTemplate.update("Insert into facility select * from CSVREAD('classpath:/csv/facility.csv')", new EmptySqlParameterSource());
    }

    @Test
    public void shouldFindFacilityByType() throws Exception {
        assertEquals(2, facilityDao.findFacilitiesByType(asList("Upazila Health Complex")).size());
        assertEquals(1, facilityDao.findFacilitiesByType(asList("Upazila Level Office")).size());
        assertEquals(3, facilityDao.findFacilitiesByType(asList("Upazila Level Office", "Upazila Health Complex")).size());
    }

    @Test
    public void shouldFindFacilityById() throws Exception {
        assertEquals("Dohar Upazila Health Complex 1", facilityDao.findFacilityById("1000").getFacilityName());
        assertEquals("Dohar Upazila Level Office", facilityDao.findFacilityById("1001").getFacilityName());
    }

    @Test
    public void shouldSaveFacility() throws Exception {
        Facility facility = new Facility();
        facility.setFacilityId("2000");
        facility.setFacilityLocationCode("302610");
        facility.setFacilityName("Test Facility");
        facility.setDhisOrgUnitUid("dhis org uid");
        facility.setFacilityType("Upazila Health Complex");

        facilityDao.save(facility);

        Facility savedFacility = facilityDao.findFacilityById("2000");
        assertEquals(facility.getFacilityName(), savedFacility.getFacilityName());
        assertEquals(facility.getFacilityType(), savedFacility.getFacilityType());
        assertEquals(facility.getFacilityLocationCode(), savedFacility.getFacilityLocationCode());
        assertEquals(facility.getDhisOrgUnitUid(), savedFacility.getDhisOrgUnitUid());
    }

    @After
    public void tearDown() throws Exception {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);

    }
}