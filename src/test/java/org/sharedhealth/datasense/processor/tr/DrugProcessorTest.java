package org.sharedhealth.datasense.processor.tr;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.sharedhealth.datasense.model.tr.CodeableConcept;
import org.sharedhealth.datasense.model.tr.Coding;
import org.sharedhealth.datasense.model.tr.TrConcept;
import org.sharedhealth.datasense.model.tr.TrMedication;
import org.sharedhealth.datasense.repository.ConceptDao;
import org.sharedhealth.datasense.repository.DrugDao;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(JUnit4.class)
public class DrugProcessorTest {

    @Mock
    private DrugDao drugDao;

    @Mock
    private ConceptDao conceptDao;

    @InjectMocks
    private DrugProcessor drugProcessor = new DrugProcessor();

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }


    @Test
    public void shouldProcessADrugIfAssociatedConceptIsDownloaded() throws Exception {
        TrMedication drug = new TrMedication();
        drug.setCode(getCodeableConcept("CODE", "ConceptId"));
        when(conceptDao.findByConceptUuid("ConceptId")).thenReturn(new TrConcept());

        drugProcessor.process(drug);

        verify(drugDao, times(1)).saveOrUpdate(drug);

    }

    @Test
    public void shouldProcessADrugIfAssociatedConceptIsNotDownloaded() throws Exception {
        TrMedication drug = new TrMedication();
        drug.setCode(getCodeableConcept("CODE", "ConceptId"));

        drugProcessor.process(drug);

        verify(drugDao, times(1)).saveOrUpdate(drug);
    }

    private CodeableConcept getCodeableConcept(String code, String conceptId) {
        CodeableConcept drugCode = new CodeableConcept();
        drugCode.addCoding(getCoding("http://tr.com/openmrs/ws/rest/v1/tr/referenceterms/ref-term-uuid", code));
        drugCode.addCoding(getCoding("http://tr.com/openmrs/ws/rest/v1/tr/concepts/concept-uuid", conceptId));
        return drugCode;
    }

    private Coding getCoding(String system, String code) {
        Coding coding = new Coding();
        coding.setSystem(system);
        coding.setCode(code);
        return coding;
    }

}