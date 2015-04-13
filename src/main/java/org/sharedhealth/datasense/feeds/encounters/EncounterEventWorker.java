package org.sharedhealth.datasense.feeds.encounters;

import org.sharedhealth.datasense.model.EncounterBundle;

public interface EncounterEventWorker {
    public void process(EncounterBundle encounterBundle);
}
