package com.pearson.statsagg.utilities;

import com.google.common.io.CharStreams;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class HttpUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class.getName());
    
    private static final Map<String,Charset> CharsetCache = new ConcurrentHashMap<>();

    // httpRequestMethod can be "GET", "POST", "PUT", etc
    // if writing data & httpBodyCharset is not defined, then the default charset will be used
    public static String httpRequest(String url, Map<String,String> headerProperties, String httpBody, String httpBodyCharset, String httpRequestMethod) {
        Charset charsetToUse = StringUtilities.getCharsetFromString(httpBodyCharset);
        
        byte[] httpBodyBytes = null;
        if ((httpBody != null) && (charsetToUse != null)) httpBodyBytes = httpBody.getBytes(charsetToUse);
        return httpRequest(url, headerProperties, httpBodyBytes, httpRequestMethod);
    }
    
    // httpRequestMethod can be "GET", "POST", "PUT", etc
    public static String httpRequest(String url, Map<String,String> headerProperties, byte[] httpBody, String httpRequestMethod) {
        
        if (url == null) {
            logger.warn(("Url cannot be null"));
            return null;
        }
        
        if (httpRequestMethod == null) {
            logger.warn(("Http request method cannot be null. Use \"GET\", \"POST\", etc."));
            return null;
        }
        
        HttpURLConnection httpUrlConnection = null;
        DataOutputStream dataOutputStream = null;
        BufferedReader bufferedReader = null;
        String httpResponse = null;
        
        try {
            URL connectionUrl = new URL(url);
            httpUrlConnection = (HttpURLConnection) connectionUrl.openConnection();
            httpUrlConnection.setRequestMethod(httpRequestMethod);
            
            if (httpRequestMethod.equalsIgnoreCase("POST") || httpRequestMethod.equalsIgnoreCase("PUT")) {
                if (httpBody != null) httpUrlConnection.setRequestProperty("Content-Length", Integer.toString(httpBody.length));
                else httpUrlConnection.setRequestProperty("Content-Length", "0");
                
                if (headerProperties != null) {
                    for (String headerKey : headerProperties.keySet()) {
                        String headerValue = headerProperties.get(headerKey);
                        httpUrlConnection.setRequestProperty(headerKey, headerValue);
                    }
                }
                
                httpUrlConnection.setDoOutput(true);
                dataOutputStream = new DataOutputStream(httpUrlConnection.getOutputStream());
                dataOutputStream.write(httpBody);
            }

            bufferedReader = new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream()));
            httpResponse = CharStreams.toString(bufferedReader);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                    bufferedReader = null;
                } 
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
            }

            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close();
                    dataOutputStream = null;
                } 
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
            }

            if (httpUrlConnection != null) {
                try {
                    httpUrlConnection.disconnect();
                    httpUrlConnection = null;
                } 
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
            }
        }
        
        return httpResponse;
    }
    
    public static boolean httpRequestBatch(String url, Map<String,String> headerProperties, List<String> httpBodies, String httpBodyCharset, String httpRequestMethod) {
        
        if ((httpBodies == null) || (httpBodies.isEmpty() || (url == null)))  {
            return false;
        }
        
        boolean isAllRequestsSuccess = true;

        for (String httpBody : httpBodies) {
            String httpResponse = httpRequest(url, headerProperties, httpBody, httpBodyCharset, httpRequestMethod);
            if (httpResponse == null) isAllRequestsSuccess = false;
        }

        return isAllRequestsSuccess;
    }
    
    public static boolean httpRequestBatch(String url, Map<String,String> headerProperties, List<byte[]> httpBodies, String httpRequestMethod) {
        
        if ((httpBodies == null) || (httpBodies.isEmpty() || (url == null)))  {
            return false;
        }
        
        boolean isAllRequestsSuccess = true;

        for (byte[] httpBody : httpBodies) {
            String httpResponse = httpRequest(url, headerProperties, httpBody, httpRequestMethod);
            if (httpResponse == null) isAllRequestsSuccess = false;
        }

        return isAllRequestsSuccess;
    }
    
}
