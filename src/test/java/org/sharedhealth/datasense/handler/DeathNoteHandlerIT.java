package org.sharedhealth.datasense.handler;

import org.hl7.fhir.instance.formats.ParserBase;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.sharedhealth.datasense.model.fhir.BundleContext;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class DeathNoteHandlerIT {

    @Autowired
    private DeathNoteHandler deathNoteHandler;

    private EncounterComposition composition;
    private Resource deathNoteResource;

    @Before
    public void setUp() throws Exception {
        ParserBase.ResourceOrFeed resourceOrFeed = loadFromXmlFile("xmls/encounterWithDeathNote.xml");
        String shrEncounterId = "shrEncounterId";
        BundleContext bundleContext = new BundleContext(resourceOrFeed.getFeed(), shrEncounterId);
        composition = bundleContext.getEncounterCompositions().get(0);
        ResourceReference resourceReference = new ResourceReference().setReferenceSimple("urn:9d3f2b4e-2f83-4d60-930c-5a7cfafbcaf2");
        deathNoteResource = bundleContext.getResourceByReferenceFromFeed(resourceReference);

    }

    @Test
    public void canHandleDeathNoteResource() throws Exception {
        assertTrue(deathNoteHandler.canHandle(deathNoteResource));

    }
}