package com.pearson.statsagg.alerts;

import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricAssociationOutputBlacklistThread implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricAssociationOutputBlacklistThread.class.getName());
    
    protected final Long threadStartTimestampInMilliseconds_;
    protected final String threadId_;
    protected final ThreadPoolExecutor threadPoolExecutor_;
    
    public MetricAssociationOutputBlacklistThread(Long threadStartTimestampInMilliseconds, ThreadPoolExecutor threadPoolExecutor) {
        this.threadStartTimestampInMilliseconds_ = threadStartTimestampInMilliseconds;
        this.threadId_ = "MAOB-" + threadStartTimestampInMilliseconds_.toString();
        this.threadPoolExecutor_ = threadPoolExecutor;
    }
    
    @Override
    public void run() {
        long metricAssociationStartTime = System.currentTimeMillis();
        MetricAssociation.associateMetricKeysWithMetricGroups_OutputBlacklistMetricGroup(threadId_, threadPoolExecutor_);
        long metricAssociationTimeElasped = System.currentTimeMillis() - metricAssociationStartTime; 

        String outputMessage = "ThreadId=" + threadId_
                + ", Routine=MetricAssociation_OutputBlacklist"
                + ", MetricAssociationTime=" + metricAssociationTimeElasped
                ;
        
        logger.info(outputMessage);
    }
    
}

