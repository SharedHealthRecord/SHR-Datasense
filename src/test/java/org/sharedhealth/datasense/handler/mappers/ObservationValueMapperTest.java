package org.sharedhealth.datasense.handler.mappers;

import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.primitive.*;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import static org.junit.Assert.assertEquals;

public class ObservationValueMapperTest {
    private ObservationValueMapper observationValueMapper;

    @Before
    public void setUp() throws Exception {
        observationValueMapper = new ObservationValueMapper();
    }

    @Test
    public void shouldMapStringValues() throws Exception {
        StringDt value = new StringDt();
        value.setValue("hello");
        assertEquals("hello", observationValueMapper.getObservationValue(value));
    }

    @Test
    public void shouldMapDecimalValues() throws Exception {
        DecimalDt value = new DecimalDt();
        BigDecimal bigDecimal = new BigDecimal(210.3);
        value.setValue(bigDecimal);
        assertEquals(bigDecimal.toString(), observationValueMapper.getObservationValue(value));
    }

    @Test
    public void shouldMapDateAndDateTimeValues() throws Exception {
        java.util.Date date = new java.util.Date();
        DateDt fhirDate = new DateDt(date);
        String formatedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        assertEquals(formatedDate, observationValueMapper.getObservationValue(fhirDate));

        DateTimeDt fhirDateTime = new DateTimeDt();
        fhirDateTime.setValue(date);
        assertEquals(formatedDate, observationValueMapper.getObservationValue(fhirDateTime));
    }

    @Test
    public void shouldMapBooleanValues() throws Exception {
        BooleanDt value = new BooleanDt();
        value.setValue(java.lang.Boolean.TRUE);
        assertEquals("true", observationValueMapper.getObservationValue(value));
        value.setValue(java.lang.Boolean.FALSE);
        assertEquals("false", observationValueMapper.getObservationValue(value));
    }

    @Test
    public void shouldMapCodeableConceptValues() throws Exception {
        //First preference must be given for reference codes.
        CodeableConceptDt codeableConcept = new CodeableConceptDt();
        CodingDt codingForConcept = codeableConcept.addCoding();
        codingForConcept.setSystem("http://tr.com/tr/concepts/concept123");
        codingForConcept.setCode("concept123");
        assertEquals("concept123", observationValueMapper.getObservationValue(codeableConcept));

        CodingDt codingForRefTerm = codeableConcept.addCoding();
        codingForRefTerm.setSystem("http://tr.com/tr/referenceterms/refTermabc");
        codingForRefTerm.setCode("refTermabc");
        assertEquals("refTermabc", observationValueMapper.getObservationValue(codeableConcept));
    }
}