package org.sharedhealth.datasense.feeds.encounters;

import org.hl7.fhir.instance.formats.ResourceOrFeed;
import org.hl7.fhir.instance.formats.XmlParser;
import org.ict4h.atomfeed.client.AtomFeedProperties;
import org.ict4h.atomfeed.client.domain.Event;
import org.ict4h.atomfeed.client.repository.memory.AllFailedEventsInMemoryImpl;
import org.ict4h.atomfeed.client.repository.memory.AllMarkersInMemoryImpl;
import org.ict4h.atomfeed.client.service.AtomFeedClient;
import org.ict4h.atomfeed.client.service.EventWorker;
import org.sharedhealth.datasense.feeds.transaction.AtomFeedSpringTransactionManager;
import org.sharedhealth.datasense.freeshr.EncounterBundle;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class ShrEncounterFeedProcessor {

    private DataSourceTransactionManager dsTxManager;
    private ShrEventWorker shrEventWorker;

    public ShrEncounterFeedProcessor(DataSourceTransactionManager dsTxManager, ShrEventWorker shrEventWorker) {
        this.dsTxManager = dsTxManager;
        this.shrEventWorker = shrEventWorker;
    }

    public void process() throws URISyntaxException {
        atomFeedClient("http://172.18.46.57:8081/catchments/3026/encounters",
                new FeedEventWorker(shrEventWorker),
                new AtomFeedProperties()).processEvents();
    }

    private AtomFeedClient atomFeedClient(String url, EventWorker worker, AtomFeedProperties properties) throws URISyntaxException {
        AtomFeedSpringTransactionManager txManager = new AtomFeedSpringTransactionManager(dsTxManager);
        return new AtomFeedClient(
                new AllEncounterFeeds(),
                new AllMarkersInMemoryImpl(),
                new AllFailedEventsInMemoryImpl(),
                properties,
                txManager,
                new URI(url),
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
