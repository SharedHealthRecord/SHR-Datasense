package org.sharedhealth.datasense.scheduler.jobs;

import org.ict4h.atomfeed.client.repository.jdbc.AllFailedEventsJdbcImpl;
import org.ict4h.atomfeed.client.repository.jdbc.AllMarkersJdbcImpl;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sharedhealth.datasense.client.ShrWebClient;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.feeds.encounters.EncounterEventWorker;
import org.sharedhealth.datasense.feeds.encounters.ShrEncounterFeedProcessor;
import org.sharedhealth.datasense.feeds.transaction.AtomFeedSpringTransactionManager;
import org.sharedhealth.datasense.security.IdentityStore;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.net.URISyntaxException;


public class CatchmentEncounterCrawlerJob extends QuartzJobBean {
    private DataSourceTransactionManager txMgr;
    private DatasenseProperties properties;

    private EncounterEventWorker encounterEventWorker;

    private IdentityStore identityStore;

    private ShrWebClient shrWebClient;

    public void setShrWebClient(ShrWebClient shrWebClient) {
        this.shrWebClient = shrWebClient;
    }

    public void setTxMgr(DataSourceTransactionManager txMgr) {
        this.txMgr = txMgr;
    }

    public void setProperties(DatasenseProperties properties) {
        this.properties = properties;
    }

    public void setEncounterEventWorker(EncounterEventWorker encounterEventWorker) {
        this.encounterEventWorker = encounterEventWorker;
    }

    public void setIdentityStore(IdentityStore identityStore) {
        this.identityStore = identityStore;
    }


    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        for (String catchment : properties.getDatasenseCatchmentList()) {
            String feedUrl = properties.getShrBaseUrl() + "/catchments/" + catchment + "/encounters";
            AtomFeedSpringTransactionManager transactionManager = new AtomFeedSpringTransactionManager(txMgr);
            ShrEncounterFeedProcessor feedCrawler =
                    new ShrEncounterFeedProcessor(
                            encounterEventWorker, feedUrl,
                            new AllMarkersJdbcImpl(transactionManager),
                            new AllFailedEventsJdbcImpl(transactionManager),
                            transactionManager, shrWebClient);
            try {
                feedCrawler.process();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }
}
