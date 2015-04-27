package org.sharedhealth.datasense.processor.tr;

import org.sharedhealth.datasense.model.tr.TrConcept;
import org.sharedhealth.datasense.model.tr.TrReferenceTerm;
import org.sharedhealth.datasense.repository.ConceptDao;
import org.sharedhealth.datasense.repository.ReferenceTermDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConceptProcessor {
    private final ReferenceTermProcessor referenceTermProcessor;
    private final ConceptDao conceptDao;
    private final ReferenceTermDao referenceTermDao;

    @Autowired
    public ConceptProcessor(ReferenceTermProcessor referenceTermProcessor, ConceptDao conceptDao, ReferenceTermDao referenceTermDao) {
        this.referenceTermProcessor = referenceTermProcessor;
        this.conceptDao = conceptDao;
        this.referenceTermDao = referenceTermDao;
    }

    public void process(TrConcept trConcept) {
        processReferenceTerms(trConcept);
        conceptDao.saveOrUpdate(trConcept);
        saveOrUpdateReferenceTermMaps(trConcept);
    }

    private void saveOrUpdateReferenceTermMaps(TrConcept trConcept) {
        referenceTermDao.deleteConceptReferenceTermMap(trConcept.getConceptUuid());
        if (trConcept.getReferenceTermMaps() != null) {
            for (TrReferenceTerm trReferenceTerm : trConcept.getReferenceTermMaps()) {
                referenceTermDao.saveReferenceTermMap(trReferenceTerm.getReferenceTermUuid(), trConcept.getConceptUuid(), trReferenceTerm.getRelationshipType());
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
