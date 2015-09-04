package org.sharedhealth.datasense.model.fhir;


import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;

public class EncounterCompositionTest {

    @Test
    public void shouldParseFeedAndIdentifyPatient() throws Exception {
        Bundle bundle = loadFromXmlFile("dstu2/xmls/encounter_with_condition.xml");
        BundleContext context = new BundleContext(bundle, "shrEncounterId");
        List<EncounterComposition> encounterCompositions = context.getEncounterCompositions();
        assertEquals(1, encounterCompositions.size());
        assertEquals("5893922485019082753", encounterCompositions.get(0).getPatientReference().getHealthId());
    }


    @Test
    public void shouldGetAllCompositionResourcesOtherThanEncounter() throws Exception {
        Bundle bundle = loadFromXmlFile("dstu2/xmls/encounter_with_condition.xml");
        BundleContext context = new BundleContext(bundle, "shrEncounterId");
        EncounterComposition composition = context.getEncounterCompositions().get(0);
        ArrayList<IResource> resources = composition.loadResourcesFromComposition();
        for (IResource resource : resources) {
            if (resource instanceof Encounter) {
                fail("Fetching section resources from composition should not return Encounter as it is fetched by composition.encounter");
            }
        }

    }

    @Test
    public void shouldLoadParentResources() throws Exception {
        Bundle bundle = loadFromXmlFile("dstu2/xmls/encounter_with_condition.xml");
        BundleContext context = new BundleContext(bundle, "shrEncounterId");
        EncounterComposition composition = context.getEncounterCompositions().get(0);
        ArrayList<IResource> resources = composition.loadResourcesFromComposition();
        assertTrue("Composition resource barring Encounter should be 1", resources.size() == 1);
        ca.uhn.fhir.model.dstu2.resource.Condition condition = null;
        for (IResource resource : resources) {
            if (resource instanceof ca.uhn.fhir.model.dstu2.resource.Condition) {
                condition = (ca.uhn.fhir.model.dstu2.resource.Condition) resource;
             }
        }
        assertNotNull("Should have found a condition resource", condition);
    }
}