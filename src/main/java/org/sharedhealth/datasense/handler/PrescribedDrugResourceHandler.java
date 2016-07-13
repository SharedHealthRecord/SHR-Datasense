package org.sharedhealth.datasense.handler;

import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.MedicationOrder;
import org.apache.commons.lang3.StringUtils;
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
    private static final String FHIR_MEDICATION_ORDER_STATUS_EXTENSION_URL = "https://sharedhealth.atlassian.net/wiki/display/docs/fhir-extensions#MedicationOrderAction";
    @Autowired
    PrescribedDrugDao prescribedDrugDao;

    @Override
    public boolean canHandle(IResource resource) {
        return resource instanceof MedicationOrder;
    }

    @Override
    public void process(IResource resource, EncounterComposition composition) {
        MedicationOrder medicationOrder = (MedicationOrder) resource;
        PrescribedDrug prescribedDrug = new PrescribedDrug();

        String healthId = composition.getPatientReference().getHealthId();
        String encounterId = composition.getEncounterReference().getEncounterId();

        prescribedDrug.setPatientHid(healthId);
        prescribedDrug.setEncounterId(encounterId);
        prescribedDrug.setPrescriptionDateTime(medicationOrder.getDateWritten());
        setDrugNameAndUuid(medicationOrder, prescribedDrug);
        prescribedDrug.setPrescriber(getMedicationPrescriber(medicationOrder));
        setStatus(prescribedDrug, medicationOrder);

        setShrMedicationId(prescribedDrug, encounterId, medicationOrder);
        setPriorMedicationId(prescribedDrug, medicationOrder);

        prescribedDrugDao.save(prescribedDrug);
    }

    private void setShrMedicationId(PrescribedDrug prescribedDrug, String encounterId, MedicationOrder medicationOrder) {
        String medicationOrderUuid = medicationOrder.getId().getIdPart();
        prescribedDrug.setShrMedicationOrderUuid(getConcatenatedShrMedicationOrderUuid(encounterId, medicationOrderUuid));
    }

    private void setPriorMedicationId(PrescribedDrug prescribedDrug, MedicationOrder medicationOrder) {
        ResourceReferenceDt priorPrescription = medicationOrder.getPriorPrescription();
        if (priorPrescription.isEmpty()) return;

        String referenceUrl = ResourceReferenceUtils.getReferenceUrlFromResourceReference(priorPrescription);
        if (!referenceUrl.contains("#MedicationOrder/")) return;

        String priorEncounterId = ResourceReferenceUtils.getEncounterUuidFromReferenceUrl(referenceUrl);
        String priorOrderUuidFromPrescription = ResourceReferenceUtils.getOrderUuidFromReferenceUrl(referenceUrl);
        prescribedDrug.setPriorShrMedicationOrderUuid(getConcatenatedShrMedicationOrderUuid(priorEncounterId, priorOrderUuidFromPrescription));
    }

    private String getConcatenatedShrMedicationOrderUuid(String encounterId, String medicationOrderUuid) {
        return encounterId + ":" + medicationOrderUuid;
    }


    @Override
    public void deleteExisting(EncounterComposition composition) {
        String encounterId = composition.getEncounterReference().getValue().getEncounterId();
        prescribedDrugDao.deleteExisting(encounterId);

    }

    private void setStatus(PrescribedDrug prescribedDrug, MedicationOrder medicationOrder) {
        List<ExtensionDt> undeclaredExtensionsByUrl = medicationOrder.getUndeclaredExtensionsByUrl(FHIR_MEDICATION_ORDER_STATUS_EXTENSION_URL);
        if (!CollectionUtils.isEmpty(undeclaredExtensionsByUrl)) {
            prescribedDrug.setStatus(undeclaredExtensionsByUrl.get(0).getValue().toString());
            return;
        }
    }

    private void setDrugNameAndUuid(MedicationOrder medicationOrder, PrescribedDrug prescribedDrug) {
        CodeableConceptDt medication = (CodeableConceptDt) medicationOrder.getMedication();
        CodingDt codingFirstRep = medication.getCodingFirstRep();
        if (StringUtils.isNotBlank(codingFirstRep.getCode())) {
            prescribedDrug.setDrugCode(codingFirstRep.getCode());
        } else {
            prescribedDrug.setNonCodedName(codingFirstRep.getDisplay());
        }

    }

    private String getMedicationPrescriber(MedicationOrder medicationOrder) {
        return ProviderReference.parseUrl(medicationOrder.getPrescriber().getReference().getValue());
    }
}
