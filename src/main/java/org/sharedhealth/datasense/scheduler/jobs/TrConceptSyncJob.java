package org.sharedhealth.datasense.scheduler.jobs;

import org.ict4h.atomfeed.client.repository.jdbc.AllFailedEventsJdbcImpl;
import org.ict4h.atomfeed.client.repository.jdbc.AllMarkersJdbcImpl;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.apache.log4j.Logger;
import org.sharedhealth.datasense.config.DatasenseProperties;
import org.sharedhealth.datasense.feeds.tr.ConceptEventWorker;
import org.sharedhealth.datasense.feeds.tr.TrConceptFeedProcessor;
import org.sharedhealth.datasense.feeds.transaction.AtomFeedSpringTransactionManager;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.net.URISyntaxException;

public class TrConceptSyncJob extends QuartzJobBean {

    private DatasenseProperties properties;
    private ConceptEventWorker conceptEventWorker;
    private DataSourceTransactionManager txMgr;

    public void setProperties(DatasenseProperties properties) {
        this.properties = properties;
    }

    public void setConceptEventWorker(ConceptEventWorker conceptEventWorker) {
        this.conceptEventWorker = conceptEventWorker;
    }

    public void setTxMgr(DataSourceTransactionManager txMgr) {
        this.txMgr = txMgr;
    }

    Logger log = Logger.getLogger(TrConceptSyncJob.class);

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        String trConceptAtomfeedUrl = properties.getTrConceptAtomfeedUrl();
        AtomFeedSpringTransactionManager transactionManager = new AtomFeedSpringTransactionManager(txMgr);
        TrConceptFeedProcessor feedCrawler =
                new TrConceptFeedProcessor(
                        conceptEventWorker, trConceptAtomfeedUrl,
                        new AllMarkersJdbcImpl(transactionManager),
                        new AllFailedEventsJdbcImpl(transactionManager),
                        transactionManager);
        try {
            feedCrawler.process();
        } catch (URISyntaxException e) {
            log.error(String.format("Unable to process concept feed [%s]", trConceptAtomfeedUrl));
            e.printStackTrace();
        }
    }
}
