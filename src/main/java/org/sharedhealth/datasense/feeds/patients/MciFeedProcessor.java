package org.sharedhealth.datasense.feeds.patients;

import org.ict4h.atomfeed.client.AtomFeedProperties;
import org.ict4h.atomfeed.client.repository.jdbc.AllFailedEventsJdbcImpl;
import org.ict4h.atomfeed.client.repository.jdbc.AllMarkersJdbcImpl;
import org.ict4h.atomfeed.client.service.AtomFeedClient;
import org.ict4h.atomfeed.client.service.EventWorker;
import org.sharedhealth.datasense.client.MciWebClient;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.feeds.transaction.AtomFeedSpringTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.net.URISyntaxException;

public class MciFeedProcessor {
    private final String feedUrl;
    private final AtomFeedSpringTransactionManager transactionManager;
    @Autowired
    private final MciWebClient mciWebClient;
    @Autowired
    private final PatientUpdateEventWorker patientUpdateEventWorker;
    private DatasenseProperties datasenseProperties;

    public MciFeedProcessor(String feedUrl,
                            AtomFeedSpringTransactionManager transactionManager,
                            MciWebClient mciWebClient,
                            PatientUpdateEventWorker patientUpdateEventWorker, DatasenseProperties datasenseProperties) {
        this.feedUrl = feedUrl;
        this.transactionManager = transactionManager;
        this.mciWebClient = mciWebClient;
        this.patientUpdateEventWorker = patientUpdateEventWorker;
        this.datasenseProperties = datasenseProperties;
    }

    public void process() throws URISyntaxException {
        AtomFeedProperties atomProperties = new AtomFeedProperties();
        atomProperties.setMaxFailedEvents(datasenseProperties.getMaxFailedEvents());
        AtomFeedClient atomFeedClient = atomFeedClient(new URI(this.feedUrl),
                patientUpdateEventWorker,
                atomProperties);
        atomFeedClient.processEvents();
        atomFeedClient.processFailedEvents();
    }

    private AtomFeedClient atomFeedClient(URI feedUri, EventWorker worker, AtomFeedProperties atomProperties) {
        return new AtomFeedClient(
                new MciPatientUpdateFeeds(mciWebClient),
                new AllMarkersJdbcImpl(transactionManager),
                new AllFailedEventsJdbcImpl(transactionManager),
                atomProperties,
                transactionManager,
                feedUri,
                worker);
    }
}
