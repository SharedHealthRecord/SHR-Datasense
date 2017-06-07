package org.sharedhealth.datasense.handler.mappers;

import org.hl7.fhir.dstu3.model.*;
import org.sharedhealth.datasense.util.DateUtil;

import java.util.List;

import static org.sharedhealth.datasense.util.FhirCodeLookup.getConceptId;
import static org.sharedhealth.datasense.util.FhirCodeLookup.getReferenceCode;

public class ObservationValueMapper {

    public static final String HL7_FHIR_VS_URL_FOR_BOOLEAN = "http://hl7.org/fhir/v2/0136";
    public static final String HL7_FHIR_VS_SYSTEM_FOR_BOOLEAN = "http://hl7.org/fhir/v2/vs/0136";

    /**
     * Use this only for observation values
     *
     * @param value
     * @return
     */
    public String getObservationValue(Type value) {
        if (value instanceof StringType) {
            return ((StringType) value).getValue();
        } else if (value instanceof DecimalType) {
            return ((DecimalType) value).getValue().toString();
        } else if (value instanceof DateType) {
            return DateUtil.parseToString(((DateType) value).getValue());
        } else if (value instanceof DateTimeType) {
            return DateUtil.parseToString(((DateTimeType) value).getValue());
        } else if (value instanceof BooleanType) {
            return ((BooleanType) value).getValue().toString();
        } else if (value instanceof Quantity) {
            return ((Quantity) value).getValue().toString();
        }
        //TODO : Codeable concept should point to concept synced from TR (Can be done after we sync all concepts from
        // TR).
        else if (value instanceof CodeableConcept) {
            List<Coding> codings = ((CodeableConcept) value).getCoding();
            if (isValueBoolean(codings)) {
                return getValueBoolean(codings);
            }
            String referenceCode = getReferenceCode(codings);
            if (referenceCode != null) {
                return referenceCode;
            } else {
                return getConceptId(codings);
            }
        }
        return null;
    }

    private boolean isValueBoolean(List<Coding> codings) {
        if (!codings.isEmpty()) {
            String system = codings.get(0).getSystem();
            return HL7_FHIR_VS_URL_FOR_BOOLEAN.equalsIgnoreCase(system) || HL7_FHIR_VS_SYSTEM_FOR_BOOLEAN.equalsIgnoreCase(system);
        }
        return false;
    }

    private String getValueBoolean(List<Coding> codings) {
        if (!codings.isEmpty()) {
            Coding coding = codings.get(0);
            String system = coding.getSystem();
            if (system.equalsIgnoreCase(HL7_FHIR_VS_URL_FOR_BOOLEAN) || system.equalsIgnoreCase(HL7_FHIR_VS_SYSTEM_FOR_BOOLEAN)) {
                return String.valueOf(coding.getCode().equalsIgnoreCase("Y"));
            }
        }
        return "false";
    }

}
