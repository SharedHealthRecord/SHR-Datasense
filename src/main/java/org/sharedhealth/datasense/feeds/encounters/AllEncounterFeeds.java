package org.sharedhealth.datasense.feeds.encounters;

import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.io.WireFeedInput;
import org.apache.log4j.Logger;
import org.ict4h.atomfeed.client.repository.AllFeeds;
import org.sharedhealth.datasense.client.ShrWebClient;

import java.io.StringReader;
import java.net.URI;
import java.util.Map;

public class AllEncounterFeeds extends AllFeeds {

    private Map<String, Object> feedProperties;
    private ShrWebClient shrWebClient;
    private static final Logger logger = Logger.getLogger(AllEncounterFeeds.class);

    public AllEncounterFeeds(ShrWebClient shrWebClient) {
        //pass facility auth info
        //aditional properties - timeout etc
        this.shrWebClient = shrWebClient;
    }

    @Override
    public Feed getFor(URI uri) {
        try {
            String response = shrWebClient.getEncounterFeedContent(uri);
            WireFeedInput input = new WireFeedInput();
            return (Feed) input.build(new StringReader(response));
        } catch (Exception e) {
            logger.error(String.format("Error occurred while processing feed for uri %s", uri.toString()), e);
            throw new RuntimeException(e);
        }
    }

}
