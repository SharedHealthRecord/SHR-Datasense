package org.sharedhealth.datasense.processor.tr;

import org.sharedhealth.datasense.model.tr.TrConcept;
import org.sharedhealth.datasense.model.tr.TrReferenceTerm;
import org.sharedhealth.datasense.repository.ConceptDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConceptProcessor {
    private final ReferenceTermProcessor referenceTermProcessor;
    private final ConceptDao conceptDao;

    @Autowired
    public ConceptProcessor(ReferenceTermProcessor referenceTermProcessor, ConceptDao conceptDao) {
        this.referenceTermProcessor = referenceTermProcessor;
        this.conceptDao = conceptDao;
    }

    public void process(TrConcept trConcept) {
        processReferenceTerms(trConcept);
        conceptDao.saveOrUpdate(trConcept);
        saveOrUpdateReferenceTermMaps(trConcept);
    }

    private void saveOrUpdateReferenceTermMaps(TrConcept trConcept) {
        conceptDao.deleteConceptReferenceTermMap(trConcept.getConceptUuid());
        if (trConcept.getReferenceTermMaps() != null) {
            for (TrReferenceTerm trReferenceTerm : trConcept.getReferenceTermMaps()) {
                conceptDao.saveReferenceTermMap(trReferenceTerm.getReferenceTermUuid(), trConcept.getConceptUuid(), trReferenceTerm.getRelationshipType());
            }
        }
    }

    private void processReferenceTerms(TrConcept trConcept) {
        if (trConcept.getReferenceTermMaps() != null) {
            for (TrReferenceTerm trReferenceTerm : trConcept.getReferenceTermMaps()) {
                referenceTermProcessor.process(trReferenceTerm);
            }
        }
    }
}
