package org.sharedhealth.datasense.feeds.tr;

import org.ict4h.atomfeed.client.AtomFeedProperties;
import org.ict4h.atomfeed.client.repository.AllFailedEvents;
import org.ict4h.atomfeed.client.repository.AllFeeds;
import org.ict4h.atomfeed.client.repository.AllMarkers;
import org.ict4h.atomfeed.client.service.AtomFeedClient;
import org.ict4h.atomfeed.client.service.EventWorker;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.feeds.transaction.AtomFeedSpringTransactionManager;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

public class TRFeedProcessor {
    private final EventWorker eventWorker;
    private final String trConceptAtomfeedUrl;
    private final AllMarkers markers;
    private final AllFailedEvents failedEvents;
    private final AtomFeedSpringTransactionManager transactionManager;
    private DatasenseProperties properties;

    public TRFeedProcessor(EventWorker eventWorker,
                           String trConceptAtomfeedUrl,
                           AllMarkers allMarkers,
                           AllFailedEvents allFailedEvents,
                           AtomFeedSpringTransactionManager transactionManager, DatasenseProperties properties) {

        this.eventWorker = eventWorker;
        this.trConceptAtomfeedUrl = trConceptAtomfeedUrl;
        this.markers = allMarkers;
        this.failedEvents = allFailedEvents;
        this.transactionManager = transactionManager;
        this.properties = properties;
    }

    public void process() throws URISyntaxException {
        AtomFeedProperties atomProperties = new AtomFeedProperties();
        atomProperties.setMaxFailedEvents(properties.getMaxFailedEvents());
        AtomFeedClient atomFeedClient = atomFeedClient(new URI(this.trConceptAtomfeedUrl),
                eventWorker,
                atomProperties);
        atomFeedClient.processEvents();
        atomFeedClient.processFailedEvents();
    }

    private AtomFeedClient atomFeedClient(URI feedUri, EventWorker worker, AtomFeedProperties atomProperties) {
        return new AtomFeedClient(
                new AllFeeds(atomProperties, new HashMap<String, String>()),
                markers,
                failedEvents,
                atomProperties,
                transactionManager,
                feedUri,
                worker);
    }
}
