package org.sharedhealth.datasense.model.fhir;

import org.hl7.fhir.dstu3.model.Bundle;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;

public class BundleContextTest {

    @Test
    public void shouldParseFeedAndIdentifyEncounter() throws Exception {
        Bundle bundle = loadFromXmlFile("dstu2/xmls/p98001046534_encounter_with_immunization.xml");
        BundleContext context = new BundleContext(bundle, "shrEncounterId");
        List<EncounterComposition> encounterCompositions = context.getEncounterCompositions();
        assertEquals(1, encounterCompositions.size());
        EncounterComposition encounterComposition = encounterCompositions.get(0);
        assertNotNull(encounterComposition.getEncounterReference().getResource());
        assertEquals(1, encounterComposition.getCompositionRefResources().size());
    }
}