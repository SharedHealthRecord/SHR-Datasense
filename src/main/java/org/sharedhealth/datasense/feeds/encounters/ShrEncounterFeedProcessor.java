package org.sharedhealth.datasense.feeds.encounters;

import org.hl7.fhir.instance.formats.ParserBase;
import org.hl7.fhir.instance.formats.XmlParser;
import org.ict4h.atomfeed.client.AtomFeedProperties;
import org.ict4h.atomfeed.client.domain.Event;
import org.ict4h.atomfeed.client.repository.AllFailedEvents;
import org.ict4h.atomfeed.client.repository.AllMarkers;
import org.ict4h.atomfeed.client.service.AtomFeedClient;
import org.ict4h.atomfeed.client.service.EventWorker;
import org.sharedhealth.datasense.client.ShrWebClient;
import org.sharedhealth.datasense.feeds.transaction.AtomFeedSpringTransactionManager;
import org.sharedhealth.datasense.model.EncounterBundle;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class ShrEncounterFeedProcessor {

    private EncounterEventWorker encounterEventWorker;
    private String feedUrl;
    private AllMarkers markers;
    private AllFailedEvents failedEvents;
    private Map<String, Object> feedProperties;
    private AtomFeedSpringTransactionManager transactionManager;
    private ShrWebClient shrWebClient;

    public ShrEncounterFeedProcessor(EncounterEventWorker encounterEventWorker,
                                     String feedUrl,
                                     AllMarkers markers,
                                     AllFailedEvents failedEvents,
                                     AtomFeedSpringTransactionManager transactionManager, ShrWebClient shrWebClient) {
        this.encounterEventWorker = encounterEventWorker;
        this.feedUrl = feedUrl;
        this.markers = markers;
        this.failedEvents = failedEvents;
        this.transactionManager = transactionManager;
        this.shrWebClient = shrWebClient;
    }

    public void process() throws URISyntaxException {
        AtomFeedProperties atomProperties = new AtomFeedProperties();
        atomProperties.setMaxFailedEvents(20);
        atomFeedClient(new URI(this.feedUrl),
                new FeedEventWorker(encounterEventWorker),
                atomProperties).processEvents();
    }

    private AtomFeedClient atomFeedClient(URI feedUri, EventWorker worker, AtomFeedProperties atomProperties)  {
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
            ParserBase.ResourceOrFeed resource;
            try {
                resource = new XmlParser(true).parseGeneral(new ByteArrayInputStream(content.getBytes()));
            } catch (Exception e) {
                throw new RuntimeException("Unable to parse XML", e);
            }
            EncounterBundle encounterBundle = new EncounterBundle();
            encounterBundle.setEncounterId(event.getId());
            encounterBundle.setTitle(event.getTitle());
            encounterBundle.addContent(resource);
            encounterEventWorker.process(encounterBundle);
        }

        @Override
        public void cleanUp(Event event) {
        }
    }
}
