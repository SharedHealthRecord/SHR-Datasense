package org.sharedhealth.datasense.handler.mappers;

import org.hl7.fhir.dstu3.model.*;
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
        StringType value = new StringType();
        value.setValue("hello");
        assertEquals("hello", observationValueMapper.getObservationValue(value));
    }

    @Test
    public void shouldMapDecimalValues() throws Exception {
        DecimalType value = new DecimalType();
        BigDecimal bigDecimal = new BigDecimal(210.3);
        value.setValue(bigDecimal);
        assertEquals(bigDecimal.toString(), observationValueMapper.getObservationValue(value));
    }

    @Test
    public void shouldMapDateAndDateTimeValues() throws Exception {
        java.util.Date date = new java.util.Date();
        DateType fhirDate = new DateType(date);
        String formatedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        assertEquals(formatedDate, observationValueMapper.getObservationValue(fhirDate));

        DateTimeType fhirDateTime = new DateTimeType();
        fhirDateTime.setValue(date);
        assertEquals(formatedDate, observationValueMapper.getObservationValue(fhirDateTime));
    }

    @Test
    public void shouldMapBooleanValues() throws Exception {
        BooleanType value = new BooleanType();
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
        codingForConcept.setSystem("http://tr.com/tr/concepts/concept123");
        codingForConcept.setCode("concept123");
        assertEquals("concept123", observationValueMapper.getObservationValue(codeableConcept));

        Coding codingForRefTerm = codeableConcept.addCoding();
        codingForRefTerm.setSystem("http://tr.com/tr/referenceterms/refTermabc");
        codingForRefTerm.setCode("refTermabc");
        assertEquals("refTermabc", observationValueMapper.getObservationValue(codeableConcept));
    }
}