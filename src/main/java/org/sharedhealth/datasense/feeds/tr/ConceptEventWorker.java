package org.sharedhealth.datasense.feeds.tr;

import org.apache.log4j.Logger;
import org.ict4h.atomfeed.client.domain.Event;
import org.ict4h.atomfeed.client.service.EventWorker;
import org.sharedhealth.datasense.client.TrWebClient;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.model.tr.TrConcept;
import org.sharedhealth.datasense.processor.tr.ConceptProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;

@Component
public class ConceptEventWorker implements EventWorker {
    private TrWebClient trWebClient;
    private ConceptProcessor conceptProcessor;
    private DatasenseProperties datasenseProperties;
    private Logger log = Logger.getLogger(ConceptEventWorker.class);

    @Autowired
    public ConceptEventWorker(TrWebClient trWebClient, ConceptProcessor conceptProcessor, DatasenseProperties datasenseProperties) {
        this.trWebClient = trWebClient;
        this.conceptProcessor = conceptProcessor;
        this.datasenseProperties = datasenseProperties;
    }

    @Override
    public void process(Event event) {
        String conceptUri = datasenseProperties.getTrBasePath() + event.getContent();
        String errorMessage = String.format("Could not connect to [ %s ]", conceptUri);
        try {
            TrConcept trConcept = trWebClient.getTrConcept(conceptUri);
            conceptProcessor.process(trConcept);
        } catch (URISyntaxException e) {
            log.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        } catch (IOException e) {
            log.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    @Override
    public void cleanUp(Event event) {

    }
}
