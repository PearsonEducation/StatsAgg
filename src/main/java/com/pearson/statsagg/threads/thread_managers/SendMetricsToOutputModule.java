package com.pearson.statsagg.threads.thread_managers;

import com.pearson.statsagg.threads.output.SendMetricsToOutputModuleThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class SendMetricsToOutputModule {
    
    private static final Logger logger = LoggerFactory.getLogger(SendMetricsToOutputModule.class.getName());

    private final SendMetricsToOutputModuleThread SendMetricsToOutputModuleThread_;
    private final Thread sendMetricsToOutputModuleThread_Thread_;
    
    public SendMetricsToOutputModule(SendMetricsToOutputModuleThread SendMetricsToOutputModuleThread, Thread sendMetricsToOutputModuleThread_Thread) {
        this.SendMetricsToOutputModuleThread_ = SendMetricsToOutputModuleThread;
        this.sendMetricsToOutputModuleThread_Thread_ = sendMetricsToOutputModuleThread_Thread;
    }

    public SendMetricsToOutputModuleThread getSendMetricsToOutputModuleThread() {
        return SendMetricsToOutputModuleThread_;
    }

    public Thread getSendMetricsToOutputModuleThread_Thread() {
        return sendMetricsToOutputModuleThread_Thread_;
    }
    
}
