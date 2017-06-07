package org.sharedhealth.datasense.feeds.patients;

import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.io.WireFeedInput;
import org.apache.log4j.Logger;
import org.ict4h.atomfeed.client.repository.AllFeeds;
import org.sharedhealth.datasense.client.MciWebClient;

import java.io.StringReader;
import java.net.URI;

public class MciPatientUpdateFeeds extends AllFeeds {
    private MciWebClient mciWebClient;
    private Logger log = Logger.getLogger(MciWebClient.class);

    public MciPatientUpdateFeeds(MciWebClient mciWebClient) {
        this.mciWebClient = mciWebClient;
    }

    @Override
    public Feed getFor(URI uri) {
        try {
            String response = mciWebClient.get(uri);
            WireFeedInput input = new WireFeedInput();
            return (Feed) input.build(new StringReader(response));
        } catch (Exception e) {
            log.error("Unable to get for uri " + uri.toString(), e);
            throw new RuntimeException(e);
        }
    }

}
