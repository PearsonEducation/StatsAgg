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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 * 
 * This class can be used to make arbitrary HTTP requests. 
 * This class is NOT thread-safe, so you can't use it to make multiple concurrent requests. However, you can use it to make sequential requests.
 */
public class HttpRequest {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class.getName());

    private final String url_;
    private final Map<String,String> headerProperties_;
    private final byte[] httpBody_;
    private final String httpRequestMethod_;
    private final int connectTimeoutInMs_;
    private final int readTimeoutInMs_;
    private final int numRetries_;
    private final boolean logErrorResponse_;
    
    private int retryAttemptCounter_ = -1;
    private HttpURLConnection httpUrlConnection_ = null;
    private DataOutputStream dataOutputStream_ = null;
    private InputStreamReader inputStreamReader_ = null;
    private BufferedReader bufferedReader_ = null;
    private String httpResponse_ = null;
    private boolean didEncounterConnectionError_ = false;
    private boolean isHttpRequestSuccess_ = false;
    
    private boolean continueRetrying_ = true;
    
    public HttpRequest(String url, Map<String,String> headerProperties, byte[] httpBody, String httpRequestMethod, 
            int connectTimeoutInMs, int readTimeoutInMs, int numRetries, boolean logErrorResponse){
        this.url_ = url;
        this.headerProperties_ = headerProperties;
        this.httpBody_ = httpBody;
        this.httpRequestMethod_ = httpRequestMethod;
        this.connectTimeoutInMs_ = connectTimeoutInMs;
        this.readTimeoutInMs_ = readTimeoutInMs;
        this.numRetries_ = numRetries;
        this.logErrorResponse_ = logErrorResponse;
    }
    
    public HttpRequest(String url, Map<String,String> headerProperties, String httpBody, String httpBodyCharset, String httpRequestMethod, 
            int connectTimeoutInMs, int readTimeoutInMs, int numRetries, boolean logErrorResponse) {
        
        Charset charsetToUse = StringUtilities.getCharsetFromString(httpBodyCharset);
        byte[] httpBodyBytes = null;
        if ((httpBody != null) && (charsetToUse != null)) httpBodyBytes = httpBody.getBytes(charsetToUse);
        
        this.url_ = url;
        this.headerProperties_ = headerProperties;
        this.httpBody_ = httpBodyBytes;
        this.httpRequestMethod_ = httpRequestMethod;
        this.connectTimeoutInMs_ = connectTimeoutInMs;
        this.readTimeoutInMs_ = readTimeoutInMs;
        this.numRetries_ = numRetries;
        this.logErrorResponse_ = logErrorResponse;
    }
            
    // httpRequestMethod can be "GET", "POST", "PUT", etc
    // returns the body of the http response (if there is one). if there was no response, or there was an error, this returns null
    public String makeRequest() {
        
        if (url_ == null) {
            logger.warn("Url cannot be null");
            return null;
        }
        
        if (httpRequestMethod_ == null) {
            logger.warn("HTTP request method cannot be null. Use \"GET\", \"POST\", etc.");
            return null;
        }
        
        if (connectTimeoutInMs_ < 0) {
            logger.warn("The connection timeout cannot be less than 0");
            return null;
        }
        
        if (readTimeoutInMs_ < 0) {
            logger.warn("The read timeout cannot be less than 0");
            return null;
        }
        
        if (numRetries_ < 0) {
            logger.warn("The number of HTTP request retry attempts cannot be less than 0");
            return null;
        }
        
        httpResponse_ = null;
        didEncounterConnectionError_ = false;
        isHttpRequestSuccess_ = false;
        
        for (retryAttemptCounter_ = -1; (retryAttemptCounter_ < numRetries_) && !isHttpRequestSuccess_ && continueRetrying_; retryAttemptCounter_++) {
            try {
                boolean didEncounterError = false;
                
                URL connectionUrl = new URL(url_);
                httpUrlConnection_ = (HttpURLConnection) connectionUrl.openConnection();
                httpUrlConnection_.setConnectTimeout(connectTimeoutInMs_);
                httpUrlConnection_.setReadTimeout(readTimeoutInMs_);
                httpUrlConnection_.setRequestMethod(httpRequestMethod_);
                
                if (httpRequestMethod_.equalsIgnoreCase("POST") || httpRequestMethod_.equalsIgnoreCase("PUT")) {
                    if (httpBody_ != null) httpUrlConnection_.setRequestProperty("Content-Length", Integer.toString(httpBody_.length));
                    else httpUrlConnection_.setRequestProperty("Content-Length", "0");

                    if (headerProperties_ != null) {
                        for (String headerKey : headerProperties_.keySet()) {
                            String headerValue = headerProperties_.get(headerKey);
                            httpUrlConnection_.setRequestProperty(headerKey, headerValue);
                        }
                    }
                    
                    httpUrlConnection_.setDoOutput(true);
                }
                
                try {
                    httpUrlConnection_.connect();
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                    didEncounterConnectionError_ = true;
                    continue;
                }
                
                if (httpRequestMethod_.equalsIgnoreCase("POST") || httpRequestMethod_.equalsIgnoreCase("PUT")) {
                    dataOutputStream_ = new DataOutputStream(httpUrlConnection_.getOutputStream());
                    dataOutputStream_.write(httpBody_);
                    dataOutputStream_.flush();
                }

                try {
                    inputStreamReader_ = new InputStreamReader(httpUrlConnection_.getInputStream());
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                    
                    didEncounterError = true;
                    
                    if (inputStreamReader_ != null) {
                        try {inputStreamReader_.close();} 
                        catch (Exception e2) {logger.error(e2.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e2));}
                    }
                
                    inputStreamReader_ = new InputStreamReader(httpUrlConnection_.getErrorStream());
                    if (logErrorResponse_) logger.error("HTTP_error_response_body:\"" + StringUtilities.removeNewlinesFromString(httpResponse_) + "\"");
                }
                
                bufferedReader_ = new BufferedReader(inputStreamReader_);
                httpResponse_ = CharStreams.toString(bufferedReader_);
                if (!didEncounterError) isHttpRequestSuccess_ = true;
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
            finally {
                closeResources();
            }
        }
        
        return httpResponse_;
    }
    
    public void closeResources() {
        if (dataOutputStream_ != null) {
            try {
                dataOutputStream_.close();
                dataOutputStream_ = null;
            } 
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }

        if (bufferedReader_ != null) {
            try {
                bufferedReader_.close();
                bufferedReader_ = null;
            } 
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }

        if (inputStreamReader_ != null) {
            try {
                inputStreamReader_.close();
                inputStreamReader_ = null;
            } 
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }

        if (httpUrlConnection_ != null) {
            try {
                httpUrlConnection_.disconnect();
                httpUrlConnection_ = null;
            } 
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
    }
    
    public static boolean httpRequestBatch(String url, Map<String,String> headerProperties, List<String> httpBodies, String httpBodyCharset, 
            String httpRequestMethod, int connectTimeoutInMs, int readTimeoutInMs, int numRetries, boolean logErrorResponse) {
        
        if ((httpBodies == null) || httpBodies.isEmpty() || (url == null) || (numRetries < 0) || (connectTimeoutInMs < 0) || (readTimeoutInMs < 0))  {
            return false;
        }
        
        boolean isAllRequestsSuccess = true;

        for (String httpBody : httpBodies) {
            HttpRequest httpRequest = new HttpRequest(url, headerProperties, httpBody, httpBodyCharset, httpRequestMethod, connectTimeoutInMs, readTimeoutInMs, numRetries, logErrorResponse);
            String response = httpRequest.makeRequest();
            if (response == null) isAllRequestsSuccess = false;
        }

        return isAllRequestsSuccess;
    }
    
    public static boolean httpRequestBatch(String url, Map<String,String> headerProperties, List<byte[]> httpBodies, 
            String httpRequestMethod, int connectTimeoutInMs, int readTimeoutInMs, int numRetries, boolean logErrorResponse) {
        
        if ((httpBodies == null) || httpBodies.isEmpty() || (url == null) || (numRetries < 0) || (connectTimeoutInMs < 0) || (readTimeoutInMs < 0))  {
            return false;
        }
        
        boolean isAllRequestsSuccess = true;

        for (byte[] httpBody : httpBodies) {
            HttpRequest httpRequest = new HttpRequest(url, headerProperties, httpBody, httpRequestMethod, connectTimeoutInMs, readTimeoutInMs, numRetries, logErrorResponse);
            String response = httpRequest.makeRequest();
            if (response == null) isAllRequestsSuccess = false;
        }

        return isAllRequestsSuccess;
    }
    
    public boolean didHitRetryAttemptLimit() {
        return retryAttemptCounter_ == numRetries_;
    }
    
    public int getRetryAttemptCounter() {
        return retryAttemptCounter_;
    }
    
    public HttpURLConnection getHttpUrlConnection() {
        return httpUrlConnection_;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream_;
    }

    public InputStreamReader getInputStreamReader() {
        return inputStreamReader_;
    }

    public BufferedReader getBufferedReader() {
        return bufferedReader_;
    }

    public String getHttpResponse() {
        return httpResponse_;
    }

    public boolean didEncounterConnectionError() {
        return didEncounterConnectionError_;
    }

    public boolean isHttpRequestSuccess() {
        return isHttpRequestSuccess_;
    }

    public boolean isContinueRetrying() {
        return continueRetrying_;
    }

    public void setContinueRetrying(boolean continueRetrying) {
        this.continueRetrying_ = continueRetrying;
    }

}
