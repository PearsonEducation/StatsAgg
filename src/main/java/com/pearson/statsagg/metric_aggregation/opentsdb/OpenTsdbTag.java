package com.pearson.statsagg.metric_aggregation.opentsdb;

import java.util.ArrayList;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class OpenTsdbTag {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenTsdbTag.class.getName());

    private final String tag_;
    
    public OpenTsdbTag(String tag) {
        this.tag_ = tag;
    }

    public static ArrayList<OpenTsdbTag> parseRawTags(String unparsedTags) {
        
        if ((unparsedTags == null) || unparsedTags.isEmpty()) {
            ArrayList<OpenTsdbTag> openTsdbTags = new ArrayList<>();
            openTsdbTags.trimToSize();
            return openTsdbTags;
        }
        
        ArrayList<OpenTsdbTag> openTsdbTags = new ArrayList<>();
        String[] unparsedTags_Split = StringUtils.split(unparsedTags.trim(), ' ');
        
        if ((unparsedTags_Split != null) && (unparsedTags_Split.length > 0)) {
            for (String metricTag : unparsedTags_Split) {
                int equalsIndex = metricTag.indexOf('=');
                
                if (equalsIndex > -1) {
                    String tag = metricTag.substring(0, metricTag.length()).trim();
                    OpenTsdbTag openTsdbTag = new OpenTsdbTag(tag);
                    openTsdbTags.add(openTsdbTag);
                }
            }
        }
        
        openTsdbTags.trimToSize();
        
        return openTsdbTags;
    }
    
    public static ArrayList<OpenTsdbTag> parseRawTags(String unparsedOpenTsdbMetric, int startPosition) {
        
        if ((unparsedOpenTsdbMetric == null) || unparsedOpenTsdbMetric.isEmpty()) {
            ArrayList<OpenTsdbTag> openTsdbTags = new ArrayList<>();
            openTsdbTags.trimToSize();
            return openTsdbTags;
        }
        
        ArrayList<OpenTsdbTag> openTsdbTags = new ArrayList<>();
        
        int offset = startPosition;
        
        while (true) {
            int metricTagIndexRange = unparsedOpenTsdbMetric.indexOf(' ', offset + 1);
            String unparsedTag;
            
            if (metricTagIndexRange > 0) {
                unparsedTag = unparsedOpenTsdbMetric.substring(offset + 1, metricTagIndexRange);
                int equalsIndex = unparsedTag.indexOf('=');
                
                if (equalsIndex > -1) {
                    String tag = unparsedTag.substring(0, unparsedTag.length()).trim();
                    OpenTsdbTag openTsdbTag = new OpenTsdbTag(tag);
                    openTsdbTags.add(openTsdbTag);
                }

                offset = metricTagIndexRange;
            }
            else {
                unparsedTag = unparsedOpenTsdbMetric.substring(offset + 1, unparsedOpenTsdbMetric.length());
                int equalsIndex = unparsedTag.indexOf('=');
                
                if (equalsIndex > -1) {
                    String tag = unparsedTag.substring(0, unparsedTag.length()).trim();
                    OpenTsdbTag openTsdbTag = new OpenTsdbTag(tag);
                    openTsdbTags.add(openTsdbTag);
                }
                
                break;
            }
        }

        openTsdbTags.trimToSize();
        
        return openTsdbTags;
    }
    
    public String getTag() {
        return tag_;
    }
    
    public String getTagKey() {
        if (tag_ == null) {
            return null;
        }
        
        int equalsIndex = tag_.indexOf('=');
        if (equalsIndex >= 0) return tag_.substring(0, equalsIndex);
        
        return null;
    }
    
    public String getTagValue() {
        if (tag_ == null) {
            return null;
        }
        
        int equalsIndex = tag_.indexOf('=');
        if ((equalsIndex >= 0) && (tag_.length() > (equalsIndex + 1))) return tag_.substring(equalsIndex + 1, tag_.length());
        
        return null;
    }

}
