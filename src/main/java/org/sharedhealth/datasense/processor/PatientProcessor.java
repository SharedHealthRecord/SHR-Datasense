package org.sharedhealth.datasense.processor;

import org.apache.log4j.Logger;
import org.sharedhealth.datasense.client.MciWebClient;
import org.sharedhealth.datasense.model.Patient;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.repository.PatientDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;


@Component("patientProcessor")
public class PatientProcessor implements ResourceProcessor {

    private ResourceProcessor nextProcessor;
    private MciWebClient mciWebClient;
    private PatientDao patientDao;

    private Logger log = Logger.getLogger(PatientProcessor.class);

    @Autowired
    public PatientProcessor(@Qualifier("serviceProviderProcessor") ResourceProcessor nextProcessor,
                            MciWebClient mciWebClient,
                            PatientDao patientDao) {
        this.nextProcessor = nextProcessor;
        this.mciWebClient = mciWebClient;
        this.patientDao = patientDao;
    }

    @Override
    public void process(EncounterComposition composition) {
        String healthId = composition.getPatientReference().getHealthId();
        log.debug("Processing Patient : " + healthId);
        Patient patient = patientDao.findPatientById(healthId);
        if (patient == null) {
            patient = downloadPatientAndSave(healthId);
        }
        composition.getPatientReference().setValue(patient);
        callNextIfGiven(composition);
    }

    private Patient downloadPatientAndSave(String healthId) {
        Patient patient;
        String message = "Could not identify patient by health Id:" + healthId;
        try {
            log.debug("Couldn't identify patient locally, downloading patient: " + healthId);
            patient = mciWebClient.identifyPatient(healthId);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unable to identify patient in MCI", e);
        } catch (Exception e) {
            log.error(message, e);
            throw new RuntimeException(message, e);
        }
        if (patient == null) {
            throw new RuntimeException(message);
        }
        log.debug("Saving downloaded patient: " + healthId);
        patientDao.save(patient);
        return patient;
    }


    @Override
    public void setNext(ResourceProcessor nextProcessor) {
        this.nextProcessor = nextProcessor;
    }

    private void callNextIfGiven(EncounterComposition bundle) {
        if (nextProcessor != null) {
            log.debug("Invoking next processor:" + nextProcessor.getClass().getName());
            nextProcessor.process(bundle);
        }
    }
}
