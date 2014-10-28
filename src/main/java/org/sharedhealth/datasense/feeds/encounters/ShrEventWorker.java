package org.sharedhealth.datasense.feeds.encounters;

import org.sharedhealth.datasense.freeshr.EncounterBundle;

public interface ShrEventWorker {
    public void process(EncounterBundle encounterBundle);
}
