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

    private final String key_;
    private final String value_;
    
    public OpenTsdbTag(String key, String value) {
        this.key_ = key;
        this.value_ = value;
    }

    public static ArrayList<OpenTsdbTag> parseRawTags(String unparsedTags) {
        
        if ((unparsedTags == null) || unparsedTags.isEmpty()) {
            return new ArrayList<>();
        }
        
        ArrayList<OpenTsdbTag> openTsdbTags = new ArrayList<>();
        String[] unparsedTags_Split = StringUtils.split(unparsedTags.trim(), ' ');
        
        if ((unparsedTags_Split != null) && (unparsedTags_Split.length > 0)) {
            for (String metricTag : unparsedTags_Split) {
                int equalsIndex = metricTag.indexOf('=');
                
                if (equalsIndex > -1) {
                    String key = metricTag.substring(0, equalsIndex);
                    String value = metricTag.substring(equalsIndex + 1, metricTag.length());
                    OpenTsdbTag openTsdbTag = new OpenTsdbTag(key, value);
                    openTsdbTags.add(openTsdbTag);
                }
            }
        }
        
        openTsdbTags.trimToSize();
        
        return openTsdbTags;
    }
    
    public static ArrayList<OpenTsdbTag> parseRawTags(String unparsedOpenTsdbMetric, int startPosition) {
        
        if ((unparsedOpenTsdbMetric == null) || unparsedOpenTsdbMetric.isEmpty()) {
            return new ArrayList<>();
        }
        
        ArrayList<OpenTsdbTag> openTsdbTags = new ArrayList<>();
        
        int offset = startPosition;
        
        while (true) {
            int metricTagIndexRange = unparsedOpenTsdbMetric.indexOf(' ', offset + 1);
            String unparsedTag = null;
            
            if (metricTagIndexRange > 0) {
                unparsedTag = unparsedOpenTsdbMetric.substring(offset + 1, metricTagIndexRange);
                int equalsIndex = unparsedTag.indexOf('=');
                
                if (equalsIndex > -1) {
                    String key = unparsedTag.substring(0, equalsIndex);
                    String value = unparsedTag.substring(equalsIndex + 1, unparsedTag.length());
                    OpenTsdbTag openTsdbTag = new OpenTsdbTag(key, value);
                    openTsdbTags.add(openTsdbTag);
                }

                offset = metricTagIndexRange;
            }
            else {
                unparsedTag = unparsedOpenTsdbMetric.substring(offset + 1, unparsedOpenTsdbMetric.length());
                int equalsIndex = unparsedTag.indexOf('=');
                
                if (equalsIndex > -1) {
                    String key = unparsedTag.substring(0, equalsIndex);
                    String value = unparsedTag.substring(equalsIndex + 1, unparsedTag.length());
                    OpenTsdbTag openTsdbTag = new OpenTsdbTag(key, value);
                    openTsdbTags.add(openTsdbTag);
                }
                
                break;
            }
        }

        openTsdbTags.trimToSize();
        
        return openTsdbTags;
    }
    
    public String getUnparsedTag() {
        return key_ + "=" + value_;
    }
    
    public String getKey() {
        return key_;
    }

    public String getValue() {
        return value_;
    }

}
