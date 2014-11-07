package org.sharedhealth.datasense.model.fhir;

import org.hl7.fhir.instance.formats.ResourceOrFeed;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;

public class BundleContextTest {

    @Test
    public void shouldParseFeedAndIdentifyEncounter() throws Exception {
        ResourceOrFeed encounterResource = loadFromXmlFile("xmls/sampleEncounter.xml");
        BundleContext context = new BundleContext(encounterResource.getFeed(), "shrEncounterId");
        List<EncounterComposition> encounterCompositions = context.getEncounterCompositions();
        assertEquals(1, encounterCompositions.size());
        EncounterComposition encounterComposition = encounterCompositions.get(0);
        assertNotNull(encounterComposition.getEncounter());
        assertEquals(6, encounterComposition.getResources().size());
    }

}