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
import org.springframework.util.CollectionUtils;

import java.util.List;

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
        //todo:  prescribedDrug.setPrescriptionDateTime(medicationOrder.getDateWritten());
        setDrugNameAndUuid(medicationOrder, prescribedDrug);
        prescribedDrug.setPrescriber(getMedicationPrescriber(medicationOrder));
        setStatus(prescribedDrug, medicationOrder);

        setShrMedicationId(prescribedDrug, encounterId, medicationOrder);
        setPriorMedicationId(prescribedDrug, medicationOrder);

        prescribedDrugDao.save(prescribedDrug);
    }

    private void setShrMedicationId(PrescribedDrug prescribedDrug, String encounterId, MedicationRequest medicationOrder) {
        String medicationOrderUuid = medicationOrder.getId();
        prescribedDrug.setShrMedicationOrderUuid(getConcatenatedShrMedicationRequestUuid(encounterId, medicationOrderUuid));
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

    private void setStatus(PrescribedDrug prescribedDrug, MedicationRequest medicationOrder) {
        List<Extension> undeclaredExtensionsByUrl = medicationOrder.getExtensionsByUrl(FHIR_MEDICATION_ORDER_STATUS_EXTENSION_URL);
        if (!CollectionUtils.isEmpty(undeclaredExtensionsByUrl)) {
            prescribedDrug.setStatus(undeclaredExtensionsByUrl.get(0).getValue().toString());
            return;
        }
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
        return null;
        //todo: return ProviderReference.parseUrl(medicationOrder.getPrescriber().getReference().getValue());
    }
}
