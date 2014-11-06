package org.sharedhealth.datasense.model;

import org.hl7.fhir.instance.formats.ResourceOrFeed;
import org.junit.Test;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.model.fhir.FHIRBundle;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;

public class FHIRBundleTest {
    @Test
    public void shouldParseFeedAndIdentifyEncounter() throws Exception {
        ResourceOrFeed encounterResource = loadFromXmlFile("xmls/sampleEncounter.xml");
        FHIRBundle fhirBundle = new FHIRBundle(encounterResource.getFeed());
        List<EncounterComposition> encounterCompositions = fhirBundle.getEncounterCompositions();
        assertEquals(1, encounterCompositions.size());
        assertNotNull(encounterCompositions.get(0).getEncounter());
    }
}