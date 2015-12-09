package org.sharedhealth.datasense.feeds.encounters;

import ca.uhn.fhir.model.dstu2.resource.Bundle;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.ict4h.atomfeed.client.AtomFeedProperties;
import org.ict4h.atomfeed.client.domain.Event;
import org.ict4h.atomfeed.client.repository.AllFailedEvents;
import org.ict4h.atomfeed.client.repository.AllMarkers;
import org.ict4h.atomfeed.client.service.AtomFeedClient;
import org.ict4h.atomfeed.client.service.EventWorker;
import org.sharedhealth.datasense.client.ShrWebClient;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.feeds.transaction.AtomFeedSpringTransactionManager;
import org.sharedhealth.datasense.model.EncounterBundle;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class ShrEncounterFeedProcessor {

    private EncounterEventWorker encounterEventWorker;
    private String feedUrl;
    private AllMarkers markers;
    private AllFailedEvents failedEvents;
    private AtomFeedSpringTransactionManager transactionManager;
    private ShrWebClient shrWebClient;
    private DatasenseProperties properties;
    private FhirBundleUtil fhirBundleUtil;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ShrEncounterFeedProcessor.class);

    public ShrEncounterFeedProcessor(EncounterEventWorker encounterEventWorker,
                                     String feedUrl,
                                     AllMarkers markers,
                                     AllFailedEvents failedEvents,
                                     AtomFeedSpringTransactionManager transactionManager,
                                     ShrWebClient shrWebClient,
                                     DatasenseProperties properties, FhirBundleUtil fhirBundleUtil) {
        this.encounterEventWorker = encounterEventWorker;
        this.feedUrl = feedUrl;
        this.markers = markers;
        this.failedEvents = failedEvents;
        this.transactionManager = transactionManager;
        this.shrWebClient = shrWebClient;
        this.properties = properties;
        this.fhirBundleUtil = fhirBundleUtil;
    }

    public void process() throws URISyntaxException {
        AtomFeedProperties atomProperties = new AtomFeedProperties();
        atomProperties.setMaxFailedEvents(properties.getMaxFailedEvents());
        AtomFeedClient atomFeedClient = atomFeedClient(new URI(this.feedUrl),
                new FeedEventWorker(encounterEventWorker),
                atomProperties);
        logger.debug("Crawling feed:" + this.feedUrl);
        atomFeedClient.processEvents();
        atomFeedClient.processFailedEvents();
    }

    private AtomFeedClient atomFeedClient(URI feedUri, EventWorker worker, AtomFeedProperties atomProperties) {
        return new AtomFeedClient(
                new AllEncounterFeeds(shrWebClient),
                markers,
                failedEvents,
                atomProperties,
                transactionManager,
                feedUri,
                worker);
    }

    private class FeedEventWorker implements EventWorker {
        private EncounterEventWorker encounterEventWorker;

        FeedEventWorker(EncounterEventWorker encounterEventWorker) {
            this.encounterEventWorker = encounterEventWorker;
        }

        @Override
        public void process(Event event) {
            String content = event.getContent();
            Bundle bundle;
            try {
                bundle = fhirBundleUtil.parseBundle(content, "xml");
            } catch (Exception e) {
                throw new RuntimeException("Unable to parse XML", e);
            }
            EncounterBundle encounterBundle = new EncounterBundle();
            encounterBundle.setEncounterId(getSHREncounterId(event.getTitle()));
            encounterBundle.setTitle(event.getTitle());
            encounterBundle.addContent(bundle);
            encounterEventWorker.process(encounterBundle);
        }

        @Override
        public void cleanUp(Event event) {
        }
    }

    //Title -> Encounter:shrEncounterId
    private String getSHREncounterId(String eventTitle) {
        return StringUtils.substringAfter(eventTitle, "Encounter:");
    }
}
