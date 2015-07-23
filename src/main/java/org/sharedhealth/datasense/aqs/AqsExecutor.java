package org.sharedhealth.datasense.aqs;

import org.apache.commons.io.IOUtils;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@Component
public class AqsExecutor {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private DatasenseProperties datasenseProperties;

    @Autowired
    public AqsExecutor(NamedParameterJdbcTemplate jdbcTemplate, DatasenseProperties datasenseProperties) {
        this.jdbcTemplate = jdbcTemplate;
        this.datasenseProperties = datasenseProperties;
    }

    public AqsConfig loadAqsConfig(String configFilename) throws IOException {
        String configContent = loadFileContents(datasenseProperties.getAqsConfigLocationPath() + configFilename);
        AqsConfig aqsConfig = new AqsConfigurationReader().getAqsConfig(configContent);
        List<QueryDefinition> queryDefinitions = loadAqsQueries(aqsConfig.getQuerySrc());
        String[] configuredQueries = aqsConfig.getQueryNames();
        List<QueryDefinition> applicableQueries = new ArrayList<QueryDefinition>();
        for (String queryName : configuredQueries) {
            for (QueryDefinition query : queryDefinitions) {
                if (queryName.equalsIgnoreCase(query.getQueryName())) {
                    applicableQueries.add(query);
                }
            }
        }
        aqsConfig.setApplicableQueries(applicableQueries);
        return aqsConfig;
    }

    private List<QueryDefinition> loadAqsQueries(String queryFileName) throws IOException {
        String querySrcContent = loadFileContents(datasenseProperties.getAqsQueryLocationPath() + queryFileName + ".json");
        return new AqsConfigurationReader().getQueryDefns(querySrcContent);
    }

    public HashMap<String, Object> execute(String configFilename, Map<String, Object> params) throws IOException, ExecutionException, InterruptedException {
        AqsConfig aqsConfig = loadAqsConfig(configFilename);
        HashMap<String, Object> results = fetchResults(aqsConfig, params);
        return results;
    }

    public HashMap<String, Object> fetchResults(AqsConfig config, Map<String, Object> params) throws InterruptedException, ExecutionException {
        List<QueryDefinition> queryDefinitionList = config.getApplicableQueries();
        ExecutorService executorService = Executors.newFixedThreadPool(queryDefinitionList.size());
        Set<CallableAqsQuery> aqsQueries = new HashSet<CallableAqsQuery>();
        for (QueryDefinition definition : queryDefinitionList) {
            aqsQueries.add(new CallableAqsQuery(jdbcTemplate, definition, params));
        }

        List<Future<HashMap<String, List<Map<String, Object>>>>> futures = executorService.invokeAll(aqsQueries);

        HashMap<String, Object> resultsMap = new HashMap<>();
        try {
            for (Future<HashMap<String, List<Map<String, Object>>>> future : futures) {
                HashMap<String, List<Map<String, Object>>> queryResult = future.get();
                resultsMap.putAll(queryResult);
            }
            return resultsMap;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            executorService.shutdown();
            try {
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String loadFileContents(String src) throws IOException {
        File file = new File(src);
        try (FileInputStream fis = new FileInputStream(file)) {
            return IOUtils.toString(fis, "UTF-8");
        }
    }
}
