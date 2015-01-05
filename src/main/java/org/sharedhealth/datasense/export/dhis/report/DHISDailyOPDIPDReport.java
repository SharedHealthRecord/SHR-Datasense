package org.sharedhealth.datasense.export.dhis.report;

import org.sharedhealth.datasense.model.Facility;
import org.sharedhealth.datasense.repository.FacilityDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Component
public class DHISDailyOPDIPDReport implements DHISReport {
    private FacilityDao facilityDao;
    private static final String facilityType = "Upazila Health Complex";

    @Autowired
    public DHISDailyOPDIPDReport(FacilityDao facilityDao) {
        this.facilityDao = facilityDao;
    }

    @Override
    public void process() {
        List<Facility> facilitiesByType = facilityDao.findFacilitiesByType(facilityType);
        System.out.println("**************************");
        System.out.println(facilitiesByType.size());
        for (Facility facility : facilitiesByType) {
            System.out.println(facility.getFacilityName());
            processReportForFacility(facility);
        }
        System.out.println("**************************");
    }

    private void processReportForFacility(Facility facility) {

        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -1);

        String today = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
        String period = new SimpleDateFormat("yyyyMMdd").format(c.getTime());
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("FACILITY", facility.getFacilityId());
        queryParams.put("ENC_DATE", today);

        HashMap<String, String> extraParams = new HashMap<>();
        extraParams.put("period", period);
        extraParams.put("orgUnit", facility.getDhisOrgUnitUid());

        HashMap<String, String> postHeaders = new HashMap<>();
        postHeaders.put("Authorization", "Basic YWRtaW46ZGlzdHJpY3Q=");
        postHeaders.put("Content-Type", "application/json");

        System.out.println("************************************************");
        for (String s : queryParams.keySet()) {
            System.out.println(queryParams.get(s));
        }
        for (String s : extraParams.keySet()) {
            System.out.println(extraParams.get(s));
        }
        System.out.println("************************************************");
    }


}
