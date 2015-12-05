package org.sharedhealth.datasense.dhis2.service;

import liquibase.util.file.FilenameUtils;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.dhis2.model.DHISOrgUnitConfig;
import org.sharedhealth.datasense.dhis2.model.DHISReportConfig;
import org.sharedhealth.datasense.dhis2.repository.DHISConfigDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DHISMetaDataService {

    @Autowired
    ApplicationContext context;

    @Autowired
    DatasenseProperties properties;

    @Autowired
    DHISConfigDao dhisConfigDao;

    public List<DHISReportConfig> getConfiguredReports() {
        return getReportMappings();
    }

    private List<DHISReportConfig> getReportMappings() {
        List<DHISReportConfig> mappedDatasets = dhisConfigDao.findAllMappedDatasets();
        List<DHISReportConfig> allReports = mergeWithConfiguredReports(mappedDatasets);
        for (DHISReportConfig configuredReport : allReports) {

        }

        return allReports;
    }

    private List<DHISReportConfig> mergeWithConfiguredReports(final List<DHISReportConfig> mappedDatasets) {
        List<DHISReportConfig> dataSets = new ArrayList<>();
        try {
            Resource[] resources = context.getResources("file://"+properties.getAqsConfigLocationPath() + "*.json");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                DHISReportConfig mds = findInMapped(mappedDatasets, filename);
                if (mds == null) {
                    dataSets.add(new DHISReportConfig(FilenameUtils.getBaseName(filename), filename));
                    continue;
                }
                dataSets.add(mds);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataSets;
    }

    private DHISReportConfig findInMapped(List<DHISReportConfig> mappedDatasets, String filename) {
        for (DHISReportConfig mds : mappedDatasets) {
            if (mds.getConfigFile().equals(filename)) {
                return mds;
            }
        }
        return null;
    }

    @Transactional
    public void save(DHISReportConfig config) {
        dhisConfigDao.save(config);
    }

    public List<DHISOrgUnitConfig> getAvailableOrgUnits(boolean includeNotConfigured) {
        return dhisConfigDao.findAllOrgUnits(includeNotConfigured);
    }

    public void save(DHISOrgUnitConfig config) {
        dhisConfigDao.save(config);
    }

    public DHISReportConfig getReportConfigForDataset(String datasetId) {
        return dhisConfigDao.getMappedConfigForDataset(datasetId);
    }

    public DHISReportConfig getReportConfig(Integer configId) {
        return dhisConfigDao.getReportConfig(configId);
    }
}
