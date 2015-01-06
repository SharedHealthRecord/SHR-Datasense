package org.sharedhealth.datasense.export.dhis.report;

import java.util.Map;

public interface DHISReport {
    public void process(Map<String, Object> dataMap);
}
