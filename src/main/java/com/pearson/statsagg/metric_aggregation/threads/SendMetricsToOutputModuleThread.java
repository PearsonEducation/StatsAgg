package com.pearson.statsagg.metric_aggregation.threads;

/**
 * @author Jeffrey Schmidt
 */
public abstract class SendMetricsToOutputModuleThread implements Runnable {
    
    protected String threadId_;
    protected String outputEndpoint_ = null;
    protected boolean isShuttingDown_ = false;
    protected boolean isFinished_ = false;
    
    public abstract void shutdown();
    
    public abstract boolean isFinished();
    
    public String getOutputEndpoint() {
        return outputEndpoint_;
    }

    public String getThreadId() {
        return threadId_;
    }
    
    public boolean isShuttingDown() {
        return isShuttingDown_;
    }
    
}
