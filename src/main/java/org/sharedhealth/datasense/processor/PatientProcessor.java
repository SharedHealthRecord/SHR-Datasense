package org.sharedhealth.datasense.processor;

import org.apache.log4j.Logger;
import org.sharedhealth.datasense.client.MciWebClient;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.model.Patient;
import org.sharedhealth.datasense.repository.PatientDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;


@Component("patientProcessor")
public class PatientProcessor implements ResourceProcessor {

    private ResourceProcessor nextProcessor;
    private MciWebClient webClient;
    private PatientDao patientDao;

    private Logger log = Logger.getLogger(PatientProcessor.class);

    @Autowired
    public PatientProcessor(@Qualifier("serviceProviderProcessor") ResourceProcessor nextProcessor,
                            MciWebClient mciWebClient,
                            PatientDao patientDao) {
        this.nextProcessor = nextProcessor;
        webClient = mciWebClient;
        this.patientDao = patientDao;
    }

    @Override
    public void process(EncounterComposition composition) {
        String healthId = composition.getPatientReference().getHealthId();
        log.info("Processing Patient : " + healthId);
        Patient patient = patientDao.getPatientById(healthId);
        if (patient == null) {
            patient = downloadPatientAndSave(healthId);
        }
        composition.getPatientReference().setValue(patient);
        callNextIfGiven(composition);
    }

    private Patient downloadPatientAndSave(String healthId) {
        Patient patient;
        try {
            patient = webClient.identifyPatient(healthId);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unable to identify patient in MCI", e);
        } catch (IOException e) {
            throw new RuntimeException("Unable to identify patient in MCI", e);
        }

        if (patient == null) {
            throw new RuntimeException("Could not identify patient by health Id:" + healthId);
        }
        patientDao.save(patient);
        return patient;
    }


    @Override
    public void setNext(ResourceProcessor nextProcessor) {
        this.nextProcessor = nextProcessor;
    }

    private void callNextIfGiven(EncounterComposition bundle) {
        if (nextProcessor != null) {
            nextProcessor.process(bundle);
        }
    }
}
