package com.pearson.statsagg.metric_aggregation.opentsdb;

import java.util.ArrayList;
import java.util.List;
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
    private final String unparsedTag_;
    
    public OpenTsdbTag(String key, String value, String unparsedTag) {
        this.key_ = key;
        this.value_ = value;
        this.unparsedTag_ = unparsedTag;
    }

    public static List<OpenTsdbTag> parseRawTags(String unparsedTags) {
        
        if ((unparsedTags == null) || unparsedTags.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<OpenTsdbTag> openTsdbTags = new ArrayList<>();
        
        String[] unparsedTags_Split = StringUtils.split(unparsedTags.trim(), ' ');
        
        if ((unparsedTags_Split != null) && (unparsedTags_Split.length > 0)) {
            for (String metricTag : unparsedTags_Split) {
                int equalsIndex = metricTag.indexOf('=');
                
                if (equalsIndex > -1) {
                    String key = metricTag.substring(0, equalsIndex);
                    String value = metricTag.substring(equalsIndex + 1, metricTag.length());
                    OpenTsdbTag openTsdbTag = new OpenTsdbTag(key, value, metricTag);
                    openTsdbTags.add(openTsdbTag);
                }
            }
        }
        
        return openTsdbTags;
    }
    
    public String getKey() {
        return key_;
    }

    public String getValue() {
        return value_;
    }

    public String getUnparsedTag() {
        return unparsedTag_;
    }
    
}
