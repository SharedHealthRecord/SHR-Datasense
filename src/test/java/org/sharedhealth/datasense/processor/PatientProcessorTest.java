package org.sharedhealth.datasense.processor;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sharedhealth.datasense.client.MciWebClient;
import org.sharedhealth.datasense.model.EncounterBundle;
import org.sharedhealth.datasense.model.Patient;
import org.sharedhealth.datasense.model.fhir.BundleContext;
import org.sharedhealth.datasense.repository.PatientDao;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.sharedhealth.datasense.helpers.ResourceHelper.loadFromXmlFile;

public class PatientProcessorTest {

    @Mock
    PatientDao patientDao;

    @Mock
    MciWebClient webClient;

    @Mock
    Logger log;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void shouldDownloadAndSavePatientIfNotPresent() throws Exception {

        String healthId = "5942395046400622593";
        EncounterBundle bundle = new EncounterBundle();
        bundle.addContent(loadFromXmlFile("xmls/sampleEncounter.xml"));
        BundleContext context = new BundleContext(bundle.getBundle().getFeed(), "shrEncounterId");
        Patient patient = new Patient();
        when(webClient.identifyPatient(healthId)).thenReturn(patient);
        PatientProcessor processor = new PatientProcessor(null, webClient, patientDao);
        processor.process(context.getEncounterCompositions().get(0));
        verify(patientDao).findPatientById(healthId);
        verify(patientDao).save(patient);
    }
}