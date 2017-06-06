package org.sharedhealth.datasense.helpers;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ResourceHelper {
    private static FhirContext context = FhirContext.forDstu2();

    public static String asString(String fileName) throws IOException {
        InputStream resourceAsStream = ResourceHelper.class.getClassLoader().getResourceAsStream(fileName);
        if (resourceAsStream != null) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream));
            StringBuilder response = new StringBuilder();
            String line;
            while ( (line = bufferedReader.readLine()) != null ) {
                response.append(line);
            }
            bufferedReader.close();
            return response.toString();
        }
        return null;
    }

    public static Bundle loadFromXmlFile(String fileName) throws IOException {
        return (Bundle) loadResourceFromXmlFile(fileName);
    }

    public static IBaseResource loadResourceFromXmlFile(String filename) throws IOException {
        String content = asString(filename);
        return context.newXmlParser().parseResource(content);
    }

    public static FhirContext getFhirContext() {
        return context;
    }
}
