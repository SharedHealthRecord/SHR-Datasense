package org.sharedhealth.datasense.helpers;

import org.hl7.fhir.instance.formats.ResourceOrFeed;
import org.hl7.fhir.instance.formats.XmlParser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ResourceHelper {
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

    public static ResourceOrFeed loadFromXmlFile(String fileName) throws IOException {
        String content = asString(fileName);
        ResourceOrFeed resource;
        try {
             resource = new XmlParser(true).parseGeneral(new ByteArrayInputStream(content.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse XML", e);
        }
        return resource;
    }
}
