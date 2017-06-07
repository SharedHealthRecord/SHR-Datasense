package org.sharedhealth.datasense.handler;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.*;
import org.sharedhealth.datasense.model.PrescribedDrug;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.model.fhir.ProviderReference;
import org.sharedhealth.datasense.repository.PrescribedDrugDao;
import org.sharedhealth.datasense.util.ResourceReferenceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PrescribedDrugResourceHandler implements FhirResourceHandler {
    private static final String FHIR_MEDICATION_ORDER_STATUS_EXTENSION_URL = "https://sharedhealth.atlassian.net/wiki/display/docs/fhir-extensions#MedicationRequestAction";
    @Autowired
    PrescribedDrugDao prescribedDrugDao;

    @Override
    public boolean canHandle(Resource resource) {
        return resource instanceof MedicationRequest;
    }

    @Override
    public void process(Resource resource, EncounterComposition composition) {
        MedicationRequest medicationOrder = (MedicationRequest) resource;
        PrescribedDrug prescribedDrug = new PrescribedDrug();

        String healthId = composition.getPatientReference().getHealthId();
        String encounterId = composition.getEncounterReference().getEncounterId();

        prescribedDrug.setPatientHid(healthId);
        prescribedDrug.setEncounterId(encounterId);
        prescribedDrug.setPrescriptionDateTime(medicationOrder.getAuthoredOn());
        setDrugNameAndUuid(medicationOrder, prescribedDrug);
        prescribedDrug.setPrescriber(getMedicationPrescriber(medicationOrder));
        prescribedDrug.setStatus(medicationOrder.getStatus().toCode());
        setShrMedicationId(prescribedDrug, encounterId, medicationOrder);
        setPriorMedicationId(prescribedDrug, medicationOrder);

        prescribedDrugDao.save(prescribedDrug);
    }

    private void setShrMedicationId(PrescribedDrug prescribedDrug, String encounterId, MedicationRequest medicationOrder) {
        String medicationOrderIdPart = StringUtils.substringAfter(medicationOrder.getId(), "urn:uuid:");
        prescribedDrug.setShrMedicationOrderUuid(getConcatenatedShrMedicationRequestUuid(encounterId, medicationOrderIdPart));
    }

    private void setPriorMedicationId(PrescribedDrug prescribedDrug, MedicationRequest medicationOrder) {
        Reference priorPrescription = medicationOrder.getPriorPrescription();
        if (priorPrescription.isEmpty()) return;

        String referenceUrl = ResourceReferenceUtils.getReferenceUrlFromResourceReference(priorPrescription);
        if (!referenceUrl.contains("#MedicationRequest/")) return;

        String priorEncounterId = ResourceReferenceUtils.getEncounterUuidFromReferenceUrl(referenceUrl);
        String priorOrderUuidFromPrescription = ResourceReferenceUtils.getOrderUuidFromReferenceUrl(referenceUrl);
        prescribedDrug.setPriorShrMedicationOrderUuid(getConcatenatedShrMedicationRequestUuid(priorEncounterId, priorOrderUuidFromPrescription));
    }

    private String getConcatenatedShrMedicationRequestUuid(String encounterId, String medicationOrderUuid) {
        return encounterId + ":" + medicationOrderUuid;
    }


    @Override
    public void deleteExisting(EncounterComposition composition) {
        String encounterId = composition.getEncounterReference().getValue().getEncounterId();
        prescribedDrugDao.deleteExisting(encounterId);

    }

    private void setDrugNameAndUuid(MedicationRequest medicationOrder, PrescribedDrug prescribedDrug) {
        CodeableConcept medication = (CodeableConcept) medicationOrder.getMedication();
        Coding codingFirstRep = medication.getCodingFirstRep();
        if (StringUtils.isNotBlank(codingFirstRep.getCode())) {
            prescribedDrug.setDrugCode(codingFirstRep.getCode());
        } else {
            prescribedDrug.setNonCodedName(codingFirstRep.getDisplay());
        }

    }

    private String getMedicationPrescriber(MedicationRequest medicationOrder) {
        MedicationRequest.MedicationRequestRequesterComponent requester = medicationOrder.getRequester();
        return ProviderReference.parseUrl(requester.getAgent().getReference());
    }
}
