package org.sharedhealth.datasense.scheduler.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.transaction.PlatformTransactionManager;

public class SimpleJob extends QuartzJobBean {

    PlatformTransactionManager txMgr;

    public void setTxMgr(PlatformTransactionManager txMgr) {
        this.txMgr = txMgr;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        System.out.println("********************* scheduler invoked ************** ");;
        System.out.println(txMgr);
    }
}
