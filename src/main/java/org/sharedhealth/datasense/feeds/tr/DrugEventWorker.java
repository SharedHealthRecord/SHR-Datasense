package org.sharedhealth.datasense.feeds.tr;

import org.apache.log4j.Logger;
import org.ict4h.atomfeed.client.domain.Event;
import org.ict4h.atomfeed.client.service.EventWorker;
import org.sharedhealth.datasense.client.TrWebClient;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.model.tr.TrMedication;
import org.sharedhealth.datasense.processor.tr.DrugProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;

@Component
public class DrugEventWorker implements EventWorker{

    private final TrWebClient trWebClient;
    private DrugProcessor drugProcessor;
    private final DatasenseProperties datasenseProperties;
    private Logger log = Logger.getLogger(DrugEventWorker.class);


    @Autowired
    public DrugEventWorker(TrWebClient trWebClient, DrugProcessor drugProcessor, DatasenseProperties datasenseProperties) {
        this.trWebClient = trWebClient;
        this.drugProcessor = drugProcessor;
        this.datasenseProperties = datasenseProperties;
    }

    @Override
    public void process(Event event) {
        String drugUri = datasenseProperties.getTrBasePath() + event.getContent();
        String errorMessage = String.format("Could not connect to [ %s ]", drugUri);
        try {
            TrMedication trDrug = trWebClient.getTrMedication(drugUri);
            drugProcessor.process(trDrug);
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
