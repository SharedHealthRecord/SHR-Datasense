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
        if (trConcept.getReferenceTermMaps() != null) {
            for (TrReferenceTerm trReferenceTerm : trConcept.getReferenceTermMaps()) {
                referenceTermProcessor.process(trReferenceTerm);
            }
        }
        conceptDao.saveOrUpdate(trConcept);
        if (trConcept.getReferenceTermMaps() != null) {
            for (TrReferenceTerm trReferenceTerm : trConcept.getReferenceTermMaps()) {
                conceptDao.saveOrUpdateReferenceTermMap(trReferenceTerm.getReferenceTermUuid(), trConcept.getConceptUuid(), trReferenceTerm.getRelationshipType());
            }
        }
    }
}
