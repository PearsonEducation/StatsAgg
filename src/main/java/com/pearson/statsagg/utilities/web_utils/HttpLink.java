package com.pearson.statsagg.utilities.web_utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class HttpLink {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpLink.class.getName());

    private final String url_;
    private final String linkText_;
    
    public HttpLink(String url, String linkText) {
        this.url_ = url;
        this.linkText_ = linkText;
    }

    public String getUrl() {
        return url_;
    }

    public String getLinkText() {
        return linkText_;
    }
    
}
