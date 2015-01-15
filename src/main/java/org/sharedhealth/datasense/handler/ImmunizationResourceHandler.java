package org.sharedhealth.datasense.handler;

import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.Immunization;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.sharedhealth.datasense.client.TrWebClient;
import org.sharedhealth.datasense.model.Encounter;
import org.sharedhealth.datasense.model.Medication;
import org.sharedhealth.datasense.model.MedicationStatus;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.model.tr.TrMedication;
import org.sharedhealth.datasense.repository.MedicationDao;
import org.sharedhealth.datasense.util.DateUtil;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import static org.sharedhealth.datasense.util.TrUrlMatcher.isConceptUrl;
import static org.sharedhealth.datasense.util.TrUrlMatcher.isReferenceTermUrl;
import static org.sharedhealth.datasense.util.TrUrlMatcher.isTrMedicationUrl;

@Component
public class ImmunizationResourceHandler implements FhirResourceHandler {

    @Autowired
    private MedicationDao medicationDao;
    @Autowired
    private TrWebClient trWebClient;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ImmunizationResourceHandler.class);

    @Override
    public boolean canHandle(Resource resource) {
        return resource.getResourceType().equals(ResourceType.Immunization);
    }

    @Override
    public void process(Resource resource, EncounterComposition composition) {
        Immunization immunization = (Immunization) resource;
        Medication medication = new Medication();
        medication.setStatus(MedicationStatus.Administered);
        Encounter encounter = composition.getEncounterReference().getValue();
        medication.setEncounter(encounter);
        medication.setPatient(composition.getPatientReference().getValue());
        medication.setDateTime(getDateTime(immunization, encounter));
        setMedicationCodes(immunization, medication);
        if (medication.getDrugId() == null) {
            logger.warn("Cannot save non-coded immnunizations.");
            return;
        }
        medicationDao.save(medication);
    }

    private void setMedicationCodes(Immunization immunization, Medication medication) {
        List<Coding> codings = immunization.getVaccineType().getCoding();
        for (Coding coding : codings) {
            String system = coding.getSystemSimple();
            if (system != null && isTrMedicationUrl(system)) {
                TrMedication trMedication = getTrMedication(system);
                medication.setDrugId(coding.getCodeSimple());
                medication.setConceptId(getConceptId(trMedication.getCode().getCoding()));
                medication.setReferenceCode(getReferenceCode(trMedication.getCode().getCoding()));
            }
        }
    }

    private String getReferenceCode(List<org.sharedhealth.datasense.model.tr.Coding> codings) {
        for (org.sharedhealth.datasense.model.tr.Coding coding : codings) {
            if(isReferenceTermUrl(coding.getSystem()))
                return coding.getCode();
        }
        return null;
    }

    private String getConceptId(List<org.sharedhealth.datasense.model.tr.Coding> codings) {
        for (org.sharedhealth.datasense.model.tr.Coding coding : codings) {
            if(isConceptUrl(coding.getSystem()))
                return coding.getCode();
        }
        return null;
    }

    private TrMedication getTrMedication(String system) {
        String errorMessage = String.format("Could not connect to [ %s ]", system);
        try {
            return trWebClient.getTrMedicaion(system);
        } catch (IOException e) {
            logger.error("IO exception", e);
            throw new RuntimeException("IO exception", e);
        } catch (URISyntaxException e) {
            logger.error("uri syntax", e);
            throw new RuntimeException("uri syntax", e);
        }
    }

    private Date getDateTime(Immunization immunization, Encounter encounter) {
        DateAndTime dateSimple = immunization.getDateSimple();
        return dateSimple != null ? DateUtil.parseDate(dateSimple.toString()) : encounter.getEncounterDateTime();
    }
}
