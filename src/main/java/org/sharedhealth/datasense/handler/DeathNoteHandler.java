package org.sharedhealth.datasense.handler;

import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Resource;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeathNoteHandler implements FhirResourceHandler {

    @Autowired
    private DatasenseProperties datasenseProperties;

    @Override
    public boolean canHandle(Resource resource) {
        if (resource instanceof org.hl7.fhir.instance.model.Observation) {
            List<Coding> codings = ((Observation) resource).getName().getCoding();
            for (Coding coding : codings) {
                if (datasenseProperties.getDeathCodes().contains(coding.getCodeSimple())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void process(Resource resource, EncounterComposition composition) {

    }
}
