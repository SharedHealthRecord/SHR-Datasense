package org.sharedhealth.datasense.feeds.tr;

import org.ict4h.atomfeed.client.AtomFeedProperties;
import org.ict4h.atomfeed.client.repository.AllFailedEvents;
import org.ict4h.atomfeed.client.repository.AllFeeds;
import org.ict4h.atomfeed.client.repository.AllMarkers;
import org.ict4h.atomfeed.client.service.AtomFeedClient;
import org.ict4h.atomfeed.client.service.EventWorker;
import org.sharedhealth.datasense.feeds.transaction.AtomFeedSpringTransactionManager;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

public class TrConceptFeedProcessor {
    private final ConceptEventWorker conceptEventWorker;
    private final String trConceptAtomfeedUrl;
    private final AllMarkers markers;
    private final AllFailedEvents failedEvents;
    private final AtomFeedSpringTransactionManager transactionManager;

    public TrConceptFeedProcessor(ConceptEventWorker conceptEventWorker,
                                  String trConceptAtomfeedUrl,
                                  AllMarkers allMarkers,
                                  AllFailedEvents allFailedEvents,
                                  AtomFeedSpringTransactionManager transactionManager) {

        this.conceptEventWorker = conceptEventWorker;
        this.trConceptAtomfeedUrl = trConceptAtomfeedUrl;
        this.markers = allMarkers;
        this.failedEvents = allFailedEvents;
        this.transactionManager = transactionManager;
    }

    public void process() throws URISyntaxException {
        AtomFeedProperties atomProperties = new AtomFeedProperties();
        atomProperties.setMaxFailedEvents(20);
        AtomFeedClient atomFeedClient = atomFeedClient(new URI(this.trConceptAtomfeedUrl),
                conceptEventWorker,
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
