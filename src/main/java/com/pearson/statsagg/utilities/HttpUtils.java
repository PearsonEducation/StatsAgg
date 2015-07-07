package com.pearson.statsagg.utilities;

import java.net.URLEncoder;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class HttpUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class.getName());
    
    // The returned value at [0] is the username
    // The returned value at [1] is the password
    public static String[] getUsernameAndPasswordFromBasicAuthoricationHeaderValue(String httpAuthorizationHeaderValue) {
        
        String[] usernameAndPassword = new String[2];
        usernameAndPassword[0] = null;
        usernameAndPassword[1] = null;
        
        if ((httpAuthorizationHeaderValue != null) && httpAuthorizationHeaderValue.startsWith("Basic ")) {
            String base64EncodedCredentials = httpAuthorizationHeaderValue.substring(6);

            try {
                byte[] credentialsBytes = Base64.decodeBase64(base64EncodedCredentials);
                String credentialsString = new String(credentialsBytes);

                int colonIndex = credentialsString.indexOf(':');
                if ((colonIndex > -1) && (colonIndex < (credentialsString.length() - 1))) {
                    String username = credentialsString.substring(0, colonIndex);
                    String password = credentialsString.substring(colonIndex + 1, credentialsString.length());

                    usernameAndPassword[0] = username;
                    usernameAndPassword[1] = password;
                }
            }
            catch (Exception e) {
                logger.warn("Error decoding HTTP Basic Auth base64 credentials.");
            }
        }
        
        return usernameAndPassword;
    }

    public static String urlEncode(String urlSnippet, String characterEncoding) {
        
        if (urlSnippet == null) {
            return null;
        }
        
        String encodedUrlSnippet = "";
        
        try {
            encodedUrlSnippet = URLEncoder.encode(urlSnippet, characterEncoding);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return encodedUrlSnippet;
    }
    
}
