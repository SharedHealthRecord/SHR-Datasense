package org.sharedhealth.datasense.processor;

import org.hl7.fhir.dstu3.model.Reference;
import org.sharedhealth.datasense.client.ProviderWebClient;
import org.sharedhealth.datasense.model.Provider;
import org.sharedhealth.datasense.model.fhir.EncounterComposition;
import org.sharedhealth.datasense.model.fhir.ProviderReference;
import org.sharedhealth.datasense.repository.ProviderDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProviderProcessor {
    private ProviderDao providerDao;
    private ProviderWebClient providerWebClient;

    private static final Logger logger = LoggerFactory.getLogger(ProviderProcessor.class);

    @Autowired
    public ProviderProcessor(ProviderDao providerDao, ProviderWebClient providerWebClient) {
        this.providerDao = providerDao;
        this.providerWebClient = providerWebClient;
    }

    public String process(EncounterComposition composition) {
        ProviderReference providerReference = composition.getProviderReference();
        if (providerReference != null && !providerReference.getReferences().isEmpty()) {
            for (Reference resourceReference : providerReference.getReferences()) {
                String providerId = providerReference.getProviderId(resourceReference);
                if (providerId != null) {
                    Provider provider = providerDao.findProviderById(providerId);
                    if (provider == null) {
                        provider = downloadAndSaveProvider(providerId);
                    }
                    if (provider != null && provider.getFacilityId() != null) {
                        return provider.getFacilityId();
                    }
                }
            }
        }
        return null;
    }

    private Provider downloadAndSaveProvider(String providerId) {
        Provider provider = null;
        logger.info("Finding provider with Id :" + providerId);
        try {
            provider = providerWebClient.findById(providerId);
        } catch (Exception e) {
            logger.error("Can not find provider with Id:" + providerId, e);
        }
        if (provider != null) {
            providerDao.save(provider);
        }
        return provider;
    }

    public void saveEncounterProviders(String encounterId, String providerId) {
        providerDao.saveEncounterProvider(encounterId, providerId);
    }

    public void deleteEncounterProviders(String encounter_id) {
        providerDao.deleteEncounterProvider(encounter_id);
    }
}
