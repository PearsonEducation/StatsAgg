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
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class HttpUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class.getName());
    
    // httpRequestMethod can be "GET", "POST", "PUT", etc
    // if writing data & httpBodyCharset is not defined, then the default charset will be used
    public static String httpRequest(String url, Map<String,String> headerProperties, String httpBody, String httpBodyCharset, String httpRequestMethod, int numRetries) {
        Charset charsetToUse = StringUtilities.getCharsetFromString(httpBodyCharset);
        
        byte[] httpBodyBytes = null;
        if ((httpBody != null) && (charsetToUse != null)) httpBodyBytes = httpBody.getBytes(charsetToUse);
        return httpRequest(url, headerProperties, httpBodyBytes, httpRequestMethod, numRetries);
    }
    
    // httpRequestMethod can be "GET", "POST", "PUT", etc
    public static String httpRequest(String url, Map<String,String> headerProperties, byte[] httpBody, String httpRequestMethod, int numRetries) {
        
        if (url == null) {
            logger.warn(("Url cannot be null"));
            return null;
        }
        
        if (httpRequestMethod == null) {
            logger.warn(("HTTP request method cannot be null. Use \"GET\", \"POST\", etc."));
            return null;
        }
        
        if (numRetries < 0) {
            logger.warn(("The number of HTTP request retry attempts cannot be less than 0"));
            return null;
        }
        
        HttpURLConnection httpUrlConnection = null;
        DataOutputStream dataOutputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        String httpResponse = null;
        boolean isHttpRequestSuccess = false;
        
        for (int i = -1; (i < numRetries) && !isHttpRequestSuccess; i++) {
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

                inputStreamReader = new InputStreamReader(httpUrlConnection.getInputStream());
                bufferedReader = new BufferedReader(inputStreamReader);
                httpResponse = CharStreams.toString(bufferedReader);
                isHttpRequestSuccess = true;
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
            finally {
                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                        dataOutputStream = null;
                    } 
                    catch (Exception e) {
                        logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                    }
                }
                
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                        bufferedReader = null;
                    } 
                    catch (Exception e) {
                        logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                    }
                }
                
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                        inputStreamReader = null;
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
        }
        
        return httpResponse;
    }
    
    public static boolean httpRequestBatch(String url, Map<String,String> headerProperties, List<String> httpBodies, String httpBodyCharset, String httpRequestMethod, int numRetries) {
        
        if ((httpBodies == null) || httpBodies.isEmpty() || (url == null) || (numRetries < 0))  {
            return false;
        }
        
        boolean isAllRequestsSuccess = true;

        for (String httpBody : httpBodies) {
            String httpResponse = httpRequest(url, headerProperties, httpBody, httpBodyCharset, httpRequestMethod, numRetries);
            if (httpResponse == null) isAllRequestsSuccess = false;
        }

        return isAllRequestsSuccess;
    }
    
    public static boolean httpRequestBatch(String url, Map<String,String> headerProperties, List<byte[]> httpBodies, String httpRequestMethod, int numRetries) {
        
        if ((httpBodies == null) || httpBodies.isEmpty() || (url == null) || (numRetries < 0))  {
            return false;
        }
        
        boolean isAllRequestsSuccess = true;

        for (byte[] httpBody : httpBodies) {
            String httpResponse = httpRequest(url, headerProperties, httpBody, httpRequestMethod, numRetries);
            if (httpResponse == null) isAllRequestsSuccess = false;
        }

        return isAllRequestsSuccess;
    }
    
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

}
