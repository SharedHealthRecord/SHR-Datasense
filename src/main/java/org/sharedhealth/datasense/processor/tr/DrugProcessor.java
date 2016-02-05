package org.sharedhealth.datasense.processor.tr;

import org.sharedhealth.datasense.model.tr.TrConcept;
import org.sharedhealth.datasense.model.tr.TrMedication;
import org.sharedhealth.datasense.repository.ConceptDao;
import org.sharedhealth.datasense.repository.DrugDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DrugProcessor {
    @Autowired
    private DrugDao drugDao;
    @Autowired
    private ConceptDao conceptDao;

    private static final Logger logger = LoggerFactory.getLogger(DrugProcessor.class);

    public void process(TrMedication trMedication) {
        final String associatedConceptId = trMedication.getAssociatedConceptId();
        TrConcept concept = conceptDao.findByConceptUuid(associatedConceptId);
        if(concept == null){
            final String message = String.format("Concept [%s] associated with Drug [%s] not downloaded", associatedConceptId, trMedication.getUuid());
            logger.error(message);
            throw new RuntimeException(message);
        }
        drugDao.saveOrUpdate(trMedication);
    }
}
