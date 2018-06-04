package com.etone.universe.dmp.task;

import org.joda.time.DateTime;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by Lanny on 2016-9-6.
 */
public class StatelessRunner extends CronRunner implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            super.initialize(jobExecutionContext);
            logger.info("initialize stateless job {} : delay={}, task count={}", this.getName(), this.getDelay(), this.getTaskList().size());

            super.run(jobExecutionContext);
            logger.info("finish stateless job id : {}, name : {}, spent: {} s", getId(), getName(), jobExecutionContext.getJobRunTime()/1000);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("error when execute job id : {}, name : {}, error : {} ", getId(), getName(), e);
        }
    }
}
