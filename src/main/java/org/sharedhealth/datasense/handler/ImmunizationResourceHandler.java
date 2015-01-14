package org.sharedhealth.datasense.handler;

import org.hl7.fhir.instance.model.Immunization;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.sharedhealth.datasense.model.Medication;
import org.sharedhealth.datasense.model.MedicationStatus;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.repository.MedicationDao;
import org.sharedhealth.datasense.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ImmunizationResourceHandler implements FhirResourceHandler {
    @Autowired
    private MedicationDao medicationDao;

    @Override
    public boolean canHandle(Resource resource) {
        return resource.getResourceType().equals(ResourceType.Immunization);
    }

    @Override
    public void process(Resource resource, EncounterComposition composition) {
        Immunization immunization = (Immunization) resource;
        Medication medication = new Medication();
        medication.setStatus(MedicationStatus.Administered);
        medication.setEncounter(composition.getEncounterReference().getValue());
        medication.setMedicationDate(DateUtil.parseDate(immunization.getDateSimple().toString()));
        medicationDao.save(medication);
    }
}
