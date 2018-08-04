package com.pearson.statsagg.webui;

import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class ForgetMetrics_RegexThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ForgetMetrics_RegexThread.class.getName());

    private final String regex_;
    
    public ForgetMetrics_RegexThread(String regex) {
        this.regex_ = regex;
    }
    
    @Override
    public void run() {
        
        if (regex_ == null) {
            return;
        }
        
        long startTimestamp = System.currentTimeMillis();
        
        List<String> metricKeyesToCleanup = getRegexMetricKeyMatches(regex_, GlobalVariables.metricKeysLastSeenTimestamp.keySet());
        
        for (String metricKey : metricKeyesToCleanup) {
            GlobalVariables.immediateCleanupMetrics.put(metricKey, metricKey);
        }
                
        if (GlobalVariables.cleanupInvokerThread != null) GlobalVariables.cleanupInvokerThread.runCleanupThread();

        long timeElasped = System.currentTimeMillis() - startTimestamp;
        
        logger.info("Thread=ForgetMetrics_RegexThread, Regex=\"" + regex_ + "\", TimeElasped=" + timeElasped);
    }
    
    public static List<String> getRegexMetricKeyMatches(String regex, Set<String> metricKeys) {
        
        if ((regex == null) || (metricKeys == null)) {
            return new ArrayList<>();
        }
        
        Pattern pattern = null;
        
        try {
            pattern = Pattern.compile(regex.trim());
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        List<String> matchingMetricKeys = new ArrayList<>();

        if (pattern != null) {            
            for (String metricKey : metricKeys) {
                Matcher matcher = pattern.matcher(metricKey);
                if (matcher.matches()) matchingMetricKeys.add(metricKey);
            }
        }
        
        return matchingMetricKeys;
    }
    
}