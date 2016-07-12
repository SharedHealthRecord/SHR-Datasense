package org.sharedhealth.datasense.service;

import org.sharedhealth.datasense.model.Facility;
import org.sharedhealth.datasense.repository.DiagnosisDao;
import org.sharedhealth.datasense.repository.EncounterDao;
import org.sharedhealth.datasense.repository.FacilityDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class FacilityInfoService {
    @Autowired
    private EncounterDao encounterDao;

    @Autowired
    private FacilityDao facilityConfigDao;

    @Autowired
    private DiagnosisDao diagnosisDao;

    @Transactional
    public Date getLastEncounterDateTime(String facilityId) {
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

    @Transactional
    public List<Map<String, Object>> getAllVisitTypes(String facilityId, String date) {
        return encounterDao.getVisitTypesWithCount(facilityId, date);
    }

    @Transactional
    public List<Map<String, Object>> getDiagnosisNameWithCount(String facilityId, String startDate, String endDate) {
        endDate = changeEndDateToEndOfDay(endDate);
        return diagnosisDao.getDiagosisWithCount(facilityId, startDate, endDate);
    }

    private String changeEndDateToEndOfDay(String endDate) {
        endDate = endDate + " 23:59:59";
        return endDate;
    }


    public List<Map<String, Object>> getEncounterTypesWithCount(String facilityId, String startDate, String endDate) {
        endDate = changeEndDateToEndOfDay(endDate);
        return encounterDao.getEncounterTypesWithCount(facilityId,startDate,endDate);
    }
}
