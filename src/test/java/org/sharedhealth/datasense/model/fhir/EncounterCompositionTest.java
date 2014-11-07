package org.sharedhealth.datasense.model.fhir;

import org.hl7.fhir.instance.formats.ResourceOrFeed;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;

public class EncounterCompositionTest {

    @Test
    public void shouldParseFeedAndIdentifyPatient() throws Exception {
        ResourceOrFeed encounterResource = loadFromXmlFile("xmls/sampleEncounter.xml");
        BundleContext context = new BundleContext(encounterResource.getFeed(), "shrEncounterId");
        List<EncounterComposition> encounterCompositions = context.getEncounterCompositions();
        assertEquals(1, encounterCompositions.size());
        assertEquals("5927558688825933825", encounterCompositions.get(0).getPatientReference().getHealthId());
    }

}