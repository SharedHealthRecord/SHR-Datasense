package org.sharedhealth.datasense.feeds.encounters;

import org.hl7.fhir.instance.formats.ResourceOrFeed;
import org.hl7.fhir.instance.formats.XmlParser;
import org.ict4h.atomfeed.client.AtomFeedProperties;
import org.ict4h.atomfeed.client.domain.Event;
import org.ict4h.atomfeed.client.repository.AllFailedEvents;
import org.ict4h.atomfeed.client.repository.AllMarkers;
import org.ict4h.atomfeed.client.service.AtomFeedClient;
import org.ict4h.atomfeed.client.service.EventWorker;
import org.sharedhealth.datasense.feeds.transaction.AtomFeedSpringTransactionManager;
import org.sharedhealth.datasense.freeshr.EncounterBundle;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class ShrEncounterFeedProcessor {

    private DataSourceTransactionManager dsTxManager;
    private ShrEventWorker shrEventWorker;
    private String feedUrl;
    private AllMarkers markers;
    private AllFailedEvents failedEvents;
    private Map<String, Object> feedProperties;

    public ShrEncounterFeedProcessor(DataSourceTransactionManager dsTxManager,
                                     ShrEventWorker shrEventWorker,
                                     String feedUrl,
                                     AllMarkers markers,
                                     AllFailedEvents failedEvents,
                                     Map<String, Object> feedProperties) {
        this.dsTxManager = dsTxManager;
        this.shrEventWorker = shrEventWorker;
        this.feedUrl = feedUrl;
        this.markers = markers;
        this.failedEvents = failedEvents;
        this.feedProperties = feedProperties;
    }

    public void process() throws URISyntaxException {
        AtomFeedProperties atomProperties = new AtomFeedProperties();
        atomProperties.setMaxFailedEvents(20);
        atomFeedClient(new URI(this.feedUrl),
                new FeedEventWorker(shrEventWorker),
                atomProperties).processEvents();
    }

    private AtomFeedClient atomFeedClient(URI feedUri, EventWorker worker, AtomFeedProperties atomProperties)  {
        AtomFeedSpringTransactionManager txManager = new AtomFeedSpringTransactionManager(dsTxManager);
        return new AtomFeedClient(
                new AllEncounterFeeds(feedProperties),
                markers,
                failedEvents,
                atomProperties,
                txManager,
                feedUri,
                worker);
    }

    private class FeedEventWorker implements EventWorker {
        private ShrEventWorker shrEventWorker;
        FeedEventWorker(ShrEventWorker shrEventWorker) {
            this.shrEventWorker = shrEventWorker;
        }

        @Override
        public void process(Event event) {
            String content = event.getContent();
            ResourceOrFeed resource;
            try {
                resource = new XmlParser(true).parseGeneral(new ByteArrayInputStream(content.getBytes()));
            } catch (Exception e) {
                throw new RuntimeException("Unable to parse XML", e);
            }
            EncounterBundle encounterBundle = new EncounterBundle();
            encounterBundle.setEncounterId(event.getId());
            encounterBundle.setTitle(event.getTitle());
            encounterBundle.addContent(resource);
            shrEventWorker.process(encounterBundle);
        }

        @Override
        public void cleanUp(Event event) {
        }
    }
}
