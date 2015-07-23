package org.sharedhealth.datasense.aqs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AqsConfigurationReader {

    public List<QueryDefinition> getQueryDefns(String content) throws IOException {
        JsonNode rootNode = new ObjectMapper().readTree(content);
        JsonNode queryNode = ((ArrayNode) rootNode).get(0);
        ArrayNode queries = (ArrayNode) queryNode.get("queries");
        ArrayList<QueryDefinition> queryDefinitions = new ArrayList<>();


        for (JsonNode node : queries) {
            QueryDefinition queryDefinition = new QueryDefinition();
            queryDefinition.setQueryName(node.get("queryName").textValue());
            queryDefinition.setQueryString(node.get("query").textValue());
            queryDefinitions.add(queryDefinition);
        }

        return queryDefinitions;
    }

    public AqsConfig getAqsConfig(String content) throws IOException {
        JsonNode rootNode = new ObjectMapper().readTree(content);
        String querySource = ((TextNode) rootNode.get("query_json_path")).asText();
        AqsConfig aqsConfig = new AqsConfig();
        aqsConfig.setQuerySrc(extractFileName(querySource));

        ArrayNode queryTemplates = (ArrayNode) rootNode.get("template_query_map");
        if (queryTemplates.size() > 0) {
            ObjectNode queryObj = (ObjectNode) queryTemplates.get(0);
            String template_path = ((TextNode) queryObj.get("template_path")).asText();
            aqsConfig.setTemplateName(extractFileName(template_path));
            ArrayNode query_list = (ArrayNode) queryObj.get("query_list");

            ArrayList<String> queryNames = new ArrayList<>();
            for (JsonNode jsonNode : query_list) {
                queryNames.add(jsonNode.textValue());
            }
            aqsConfig.setQueryNames(queryNames.toArray(new String[queryNames.size()]));
        }

        return aqsConfig;
    }

    private String extractFileName(String querySource) {
        return FilenameUtils.getBaseName(querySource);
    }
}
