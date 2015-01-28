package org.sharedhealth.datasense.processor.tr;

import org.sharedhealth.datasense.model.tr.TrReferenceTerm;
import org.sharedhealth.datasense.repository.ReferenceTermDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReferenceTermProcessor {
    private ReferenceTermDao referenceTermDao;

    @Autowired
    public ReferenceTermProcessor(ReferenceTermDao referenceTermDao) {
        this.referenceTermDao = referenceTermDao;
    }

    public void process(TrReferenceTerm trReferenceTerm) {
        referenceTermDao.saveOrUpdate(trReferenceTerm);
    }
}
