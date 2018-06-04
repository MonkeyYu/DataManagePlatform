package com.etone.universe.dmp.task;

import org.joda.time.DateTime;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

/**
 * Created by Lanny on 2016-9-6.
 */
public class StatefulRunner extends CronRunner implements StatefulJob {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            super.initialize(jobExecutionContext);
            logger.info("initialize stateful job {} : delay={}, task count={}", this.getName(), this.getDelay(), this.getTaskList().size());

            super.run(jobExecutionContext);
            logger.info("finish stateful job id : {}, name : {}, spent: {} s", getId(), getName(), jobExecutionContext.getJobRunTime()/1000);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("error when execute job id : {}, name : {}, error : {} ", getId(), getName(), e);
        }
    }
}
