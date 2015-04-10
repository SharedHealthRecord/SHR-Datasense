package org.sharedhealth.datasense.feeds.encounters;

import org.sharedhealth.datasense.model.EncounterBundle;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface EncounterEventWorker {
    @Transactional(propagation = Propagation.REQUIRED)
    public void process(EncounterBundle encounterBundle);
}
