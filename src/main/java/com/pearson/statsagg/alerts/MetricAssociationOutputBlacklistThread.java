package com.pearson.statsagg.alerts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricAssociationOutputBlacklistThread implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricAssociationOutputBlacklistThread.class.getName());
    
    protected final Long threadStartTimestampInMilliseconds_;
    protected final String threadId_;
    
    public MetricAssociationOutputBlacklistThread(Long threadStartTimestampInMilliseconds) {
        this.threadStartTimestampInMilliseconds_ = threadStartTimestampInMilliseconds;
        this.threadId_ = "MA-" + threadStartTimestampInMilliseconds_.toString();
    }
    
    @Override
    public void run() {
        long metricAssociationStartTime = System.currentTimeMillis();
        MetricAssociation.associateMetricKeysWithMetricGroups_OutputBlacklistMetricGroup(threadId_);
        long metricAssociationTimeElasped = System.currentTimeMillis() - metricAssociationStartTime; 

        String outputMessage = "ThreadId=" + threadId_
                + ", Routine=MetricAssociation"
                + ", MetricAssociationTime=" + metricAssociationTimeElasped
                ;
        
        logger.info(outputMessage);
    }
    
}

