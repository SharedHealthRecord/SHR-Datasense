package org.sharedhealth.datasense.feeds.patients;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ict4h.atomfeed.client.domain.Event;
import org.ict4h.atomfeed.client.service.EventWorker;
import org.sharedhealth.datasense.repository.EncounterDao;
import org.sharedhealth.datasense.repository.PatientDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PatientUpdateEventWorker implements EventWorker {
    private static final Logger logger = LoggerFactory.getLogger(PatientUpdateEventWorker.class);
    private PatientDao patientDao;
    private EncounterDao encounterDao;

    @Autowired
    public PatientUpdateEventWorker(PatientDao patientDao, EncounterDao encounterDao) {
        this.patientDao = patientDao;
        this.encounterDao = encounterDao;
    }

    @Override
    public void process(Event event) {
        try {
            final PatientUpdate patientUpdate = readFrom(extractContent(event.getContent()), PatientUpdate.class);
            if (patientUpdate.hasMergedWithChanges()) {
                String healthId = patientUpdate.getHealthId();
                if (encounterDao.getEncounterCountForPatient(healthId) > 0) {
                    String message = String.format("Cannot delete merged patient %s because there are encounters associated with it", healthId);
                    logger.error(message);
                    throw new RuntimeException(message);
                }
                logger.info(String.format("Deleting inactive patient %s because it is merged.", healthId));
                patientDao.deletePatient(healthId);
                return;
            }
            patientDao.update(patientUpdate);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void cleanUp(Event event) {

    }

    public static <T> T readFrom(String content, Class<T> returnType) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setVisibilityChecker(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        return mapper.readValue(content, returnType);

    }

    private static String extractContent(String content) {
        return content.replaceFirst(
                "^<!\\[CDATA\\[", "").replaceFirst("\\]\\]>$", "");
    }


}
