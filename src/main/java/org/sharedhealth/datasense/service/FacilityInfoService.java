package org.sharedhealth.datasense.service;

import org.sharedhealth.datasense.model.Facility;
import org.sharedhealth.datasense.repository.EncounterDao;
import org.sharedhealth.datasense.repository.FacilityDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FacilityInfoService {
    @Autowired
    private EncounterDao encounterDao;

    @Autowired
    private FacilityDao facilityConfigDao;

    @Transactional
    public Object getLastEncounterDateTime(String facilityId) {
        return encounterDao.getLastSyncedEncounterDateTime(facilityId);
    }

    @Transactional
    public Facility getAvailableFacilitiesById(String facility_Id) {
        return facilityConfigDao.findFacilityById(facility_Id);
    }

    @Transactional
    public List<Facility> getAvailableFacilitiesBYName(String facility_name) {
        return facilityConfigDao.findFacilityByName(facility_name);
    }
}
