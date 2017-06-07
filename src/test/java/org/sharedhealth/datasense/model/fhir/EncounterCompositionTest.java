package org.sharedhealth.datasense.model.fhir;


import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Resource;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;

public class EncounterCompositionTest {

    @Test
    public void shouldParseFeedAndIdentifyPatient() throws Exception {
        Bundle bundle = loadFromXmlFile("stu3/encounter_with_condition.xml");
        BundleContext context = new BundleContext(bundle, "shrEncounterId");
        List<EncounterComposition> encounterCompositions = context.getEncounterCompositions();
        assertEquals(1, encounterCompositions.size());
        assertEquals("5893922485019082753", encounterCompositions.get(0).getPatientReference().getHealthId());
    }


    @Test
    public void shouldGetAllCompositionResourcesOtherThanEncounter() throws Exception {
        Bundle bundle = loadFromXmlFile("stu3/encounter_with_condition.xml");
        BundleContext context = new BundleContext(bundle, "shrEncounterId");
        EncounterComposition composition = context.getEncounterCompositions().get(0);
        ArrayList<Resource> resources = composition.getCompositionRefResources();
        for (Resource resource : resources) {
            if (resource instanceof Encounter) {
                fail("Fetching section resources from composition should not return Encounter as it is fetched by composition.encounter");
            }
        }

    }

    @Test
    public void shouldFetchVitalsOnly() throws Exception {
        Bundle bundle = loadFromXmlFile("stu3/p98001046534_encounter_with_vitals.xml");
        BundleContext context = new BundleContext(bundle, "shrEncounterId");
        EncounterComposition composition = context.getEncounterCompositions().get(0);
        List<Resource> topLevelResources = composition.getTopLevelResources();
        assertEquals(1, topLevelResources.size());
        assertEquals("urn:uuid:a4708fe7-43c5-4b32-86ec-76924cf1f0e1", topLevelResources.get(0).getId());
    }

    @Test
    public void shouldFetchTopLevelResourcesForDiagnositicReport() throws Exception {
        Bundle bundle = loadFromXmlFile("stu3/p98001046534_encounter_with_diagnosticReport.xml");
        BundleContext context = new BundleContext(bundle, "shrEncounterId");
        EncounterComposition composition = context.getEncounterCompositions().get(0);
        List<Resource> topLevelResources = composition.getTopLevelResources();
        assertEquals(4, topLevelResources.size());
    }

}