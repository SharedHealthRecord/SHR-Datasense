package org.sharedhealth.datasense.aqs;

import java.util.Map;

public interface AqsTemplateProcessor {
    String process(String aqsConfigFile, Map<String, Object> params);
}
