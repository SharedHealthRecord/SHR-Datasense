package org.sharedhealth.datasense.dhis2.service;

import org.sharedhealth.datasense.dhis2.model.MetadataConfig;
import org.sharedhealth.datasense.dhis2.repository.DHISConfigDao;
import org.sharedhealth.datasense.model.Facility;
import org.sharedhealth.datasense.repository.FacilityDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FacilityInfoService {
    @Autowired
    DHISConfigDao dhisConfigDao;

    @Autowired
    FacilityDao facilityCongigDao;

    @Transactional
    public Object getLastEncounter(MetadataConfig config) {
        return dhisConfigDao.getLastEncounter(config);
    }

    @Transactional
    public Facility getAvailableFacilitiesById(String facility_Id) {
        return facilityCongigDao.findFacilityById(facility_Id);
    }

    @Transactional
    public List<Facility> getAvailableFacilitiesBYName(String facility_name) {
        return facilityCongigDao.findFacilityByName(facility_name);
    }
}
