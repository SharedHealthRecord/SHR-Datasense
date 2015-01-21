package org.sharedhealth.datasense.handler.mappers;

import org.hl7.fhir.instance.model.*;
import org.hl7.fhir.instance.model.Boolean;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class ObservationValueMapperTest {
    private ObservationValueMapper observationValueMapper;

    @Before
    public void setUp() throws Exception {
        observationValueMapper = new ObservationValueMapper();
    }

    @Test
    public void shouldMapStringValues() throws Exception {
        String_ value = new String_();
        value.setValue("hello");
        assertEquals("hello", observationValueMapper.getObservationValue(value));
    }

    @Test
    public void shouldMapDecimalValues() throws Exception {
        Decimal value = new Decimal();
        BigDecimal bigDecimal = new BigDecimal(210.3);
        value.setValue(bigDecimal);
        assertEquals(bigDecimal.toString(), observationValueMapper.getObservationValue(value));
    }

    @Test
    public void shouldMapDateAndDateTimeValues() throws Exception {
        java.util.Date date = new java.util.Date();
        DateAndTime dateAndTime = new DateAndTime(date);
        Date fhirDate = new Date();
        fhirDate.setValue(dateAndTime);
        assertEquals(date.toString(), observationValueMapper.getObservationValue(fhirDate));
        DateTime fhirDateTime = new DateTime();
        fhirDateTime.setValue(dateAndTime);
        assertEquals(date.toString(), observationValueMapper.getObservationValue(fhirDateTime));
    }

    @Test
    public void shouldMapBooleanValues() throws Exception {
        Boolean value = new Boolean();
        value.setValue(java.lang.Boolean.TRUE);
        assertEquals("true", observationValueMapper.getObservationValue(value));
        value.setValue(java.lang.Boolean.FALSE);
        assertEquals("false", observationValueMapper.getObservationValue(value));
    }

    @Test
    public void shouldMapCodeableConceptValues() throws Exception {
        //First preference must be given for reference codes.
        CodeableConcept codeableConcept = new CodeableConcept();
        Coding codingForConcept = codeableConcept.addCoding();
        codingForConcept.setSystemSimple("http://tr.com/tr/concepts/concept123");
        codingForConcept.setCodeSimple("concept123");
        assertEquals("concept123", observationValueMapper.getObservationValue(codeableConcept));

        Coding codingForRefTerm = codeableConcept.addCoding();
        codingForRefTerm.setSystemSimple("http://tr.com/tr/referenceterms/refTermabc");
        codingForRefTerm.setCodeSimple("refTermabc");
        assertEquals("refTermabc", observationValueMapper.getObservationValue(codeableConcept));
    }
}