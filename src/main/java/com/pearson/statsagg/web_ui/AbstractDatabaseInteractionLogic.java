package com.pearson.statsagg.web_ui;

/**
 * @author Jeffrey Schmidt
 */
public abstract class AbstractDatabaseInteractionLogic {

    public final static int STATUS_CODE_FAILURE = -1;
    public final static int STATUS_CODE_UNKNOWN = 0;
    public final static int STATUS_CODE_SUCCESS = 1;
    
    protected int lastAlterRecordStatus_ = 0;
    protected int lastDeleteRecordStatus_ = 0;
    
    public int getLastAlterRecordStatus() {
        return lastAlterRecordStatus_;
    }

    public int getLastDeleteRecordStatus() {
        return lastDeleteRecordStatus_;
    }
    
}
