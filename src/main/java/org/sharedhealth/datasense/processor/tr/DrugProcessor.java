package org.sharedhealth.datasense.processor.tr;

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
        drugDao.saveOrUpdate(trMedication);
    }
}
