package com.pearson.statsagg.metric_aggregation.graphite;

import org.apache.commons.lang.StringUtils;

/**
 * @author Jeffrey Schmidt
 */
public class Common {

    public static String getGraphiteFormattedMetricPath(String metricPath) {

        if (metricPath == null) {
            return null;
        }

        String formattedGraphiteMetricPath = metricPath;
 
        formattedGraphiteMetricPath = StringUtils.replace(formattedGraphiteMetricPath, "%", "Pct");
        formattedGraphiteMetricPath = StringUtils.replace(formattedGraphiteMetricPath, " ", "_");
        formattedGraphiteMetricPath = StringUtils.replace(formattedGraphiteMetricPath, "\"", "");
        formattedGraphiteMetricPath = StringUtils.replace(formattedGraphiteMetricPath, "/", "|");
        formattedGraphiteMetricPath = StringUtils.replace(formattedGraphiteMetricPath, "\\", "|");
        formattedGraphiteMetricPath = StringUtils.replace(formattedGraphiteMetricPath, "[", "|");
        formattedGraphiteMetricPath = StringUtils.replace(formattedGraphiteMetricPath, "]", "|");
        formattedGraphiteMetricPath = StringUtils.replace(formattedGraphiteMetricPath, "{", "|");
        formattedGraphiteMetricPath = StringUtils.replace(formattedGraphiteMetricPath, "}", "|");
        formattedGraphiteMetricPath = StringUtils.replace(formattedGraphiteMetricPath, "(", "|");
        formattedGraphiteMetricPath = StringUtils.replace(formattedGraphiteMetricPath, ")", "|");
        
        while (formattedGraphiteMetricPath.contains("..")) {
            formattedGraphiteMetricPath = StringUtils.replace(formattedGraphiteMetricPath, "..", ".");
        }
        
        return formattedGraphiteMetricPath;
    }
    
}
