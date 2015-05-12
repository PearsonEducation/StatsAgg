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

    public static ArrayList<OpenTsdbTag> parseTags(String unparsedTags) {
        
        if ((unparsedTags == null) || unparsedTags.isEmpty()) {
            ArrayList<OpenTsdbTag> openTsdbTags = new ArrayList<>();
            openTsdbTags.trimToSize();
            return openTsdbTags;
        }
        
        ArrayList<OpenTsdbTag> openTsdbTags = new ArrayList<>();
        ArrayList<String> openTsdbTagKeys = new ArrayList<>();
        
        String[] unparsedTags_Split = StringUtils.split(unparsedTags.trim(), ' ');
        
        if ((unparsedTags_Split != null) && (unparsedTags_Split.length > 0)) {
            for (String unparsedTag : unparsedTags_Split) {
                int equalsIndex = unparsedTag.indexOf('=');
                
                if (equalsIndex > -1) {
                    String tagKey = unparsedTag.substring(0, equalsIndex);
                    
                    if (!openTsdbTagKeys.contains(tagKey)) {
                        openTsdbTagKeys.add(tagKey);
                        String tag = unparsedTag.substring(0, unparsedTag.length()).trim();
                        OpenTsdbTag openTsdbTag = new OpenTsdbTag(tag);
                        openTsdbTags.add(openTsdbTag);
                    }
                    else {
                        logger.info("Duplicate Tag-Key Detected:" + tagKey);
                        return new ArrayList<>();
                    }
                }
            }
        }
        
        openTsdbTags.trimToSize();
        
        return openTsdbTags;
    }
    
    public static ArrayList<OpenTsdbTag> parseTags(String unparsedOpenTsdbMetric, int startPosition) {
        
        if ((unparsedOpenTsdbMetric == null) || unparsedOpenTsdbMetric.isEmpty()) {
            ArrayList<OpenTsdbTag> openTsdbTags = new ArrayList<>();
            openTsdbTags.trimToSize();
            return openTsdbTags;
        }
        
        ArrayList<OpenTsdbTag> openTsdbTags = new ArrayList<>();
        ArrayList<String> openTsdbTagKeys = new ArrayList<>();
        
        int offset = startPosition;
        
        while (true) {
            int metricTagIndexRange = unparsedOpenTsdbMetric.indexOf(' ', offset + 1);
            String unparsedTag;
            
            if (metricTagIndexRange > 0) {
                unparsedTag = unparsedOpenTsdbMetric.substring(offset + 1, metricTagIndexRange);
                int equalsIndex = unparsedTag.indexOf('=');
                
                if (equalsIndex > -1) {
                    String tagKey = unparsedTag.substring(0, equalsIndex);
                    
                    if (!openTsdbTagKeys.contains(tagKey)) {
                        openTsdbTagKeys.add(tagKey);
                        String tag = unparsedTag.substring(0, unparsedTag.length()).trim();
                        OpenTsdbTag openTsdbTag = new OpenTsdbTag(tag);
                        openTsdbTags.add(openTsdbTag);
                    }
                    else {
                        logger.info("Duplicate Tag-Key Detected:" + tagKey);
                        return new ArrayList<>();
                    }
                }

                offset = metricTagIndexRange;
            }
            else {
                unparsedTag = unparsedOpenTsdbMetric.substring(offset + 1, unparsedOpenTsdbMetric.length());
                int equalsIndex = unparsedTag.indexOf('=');
                
                if (equalsIndex > -1) {
                    String tagKey = unparsedTag.substring(0, equalsIndex);
                    
                    if (!openTsdbTagKeys.contains(tagKey)) {
                        openTsdbTagKeys.add(tagKey);
                        String tag = unparsedTag.substring(0, unparsedTag.length()).trim();
                        OpenTsdbTag openTsdbTag = new OpenTsdbTag(tag);
                        openTsdbTags.add(openTsdbTag);
                    }
                    else {
                        logger.info("Duplicate Tag-Key Detected: " + tagKey);
                        return new ArrayList<>();
                    }
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
