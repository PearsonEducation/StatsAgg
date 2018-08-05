package com.pearson.statsagg.utilities.web_utils;

import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.core_utils.Threads;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class NetIo {
    
    private static final Logger logger = LoggerFactory.getLogger(NetIo.class.getName());
    
    public static String downloadUrl(String urlString, int nRetries, double nSecondsBetweenRetries) {
        return downloadUrl(urlString, nRetries, nSecondsBetweenRetries, false);
    }
    
    public static String downloadUrl(String urlString, int nRetries, double nSecondsBetweenRetries, boolean useDebugLevelLogging) {
        
        if ((urlString == null) || (nRetries < 0) || (nSecondsBetweenRetries < 0)) {
            return null;
        }
        
        boolean isDownloadSuccess = false;
        String webpageString = null;
        
        for (int i = 0; (i <= nRetries) && !isDownloadSuccess; i++) {
            
            if (i > 0) {
                Threads.sleepSeconds(nSecondsBetweenRetries);
                logger.info("Download Request Retry #" + i + " for url=\"" + urlString + "\"");
            }

            webpageString = NetIo.downloadUrl(urlString, useDebugLevelLogging);

            if (webpageString != null) {
                isDownloadSuccess = true;
            }
            else {
                isDownloadSuccess = false;
            }
        }

        return webpageString;
    }
    
    public static String downloadUrl(String urlString) {
        return downloadUrl(urlString, false);
    }
    
    public static String downloadUrl(String urlString, boolean useDebugLevelLogging) {
        
        if ((urlString == null)) {
            return null;
        }
     
        BufferedReader reader = null;
        InputStreamReader urlStream = null;
        
        try {
            StringBuilder webpageString = new StringBuilder();
            URL url = new URL(urlString);

            urlStream = new InputStreamReader(url.openStream());
            reader = new BufferedReader(urlStream);

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                webpageString.append(currentLine);
                webpageString.append(System.lineSeparator());
            }
            
            return webpageString.toString();
        }
        catch (Exception e) {
            if (useDebugLevelLogging) logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            else logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            
            return null;
        }
        finally {
            try {
                if (urlStream != null) {
                    urlStream.close();
                }
            }       
            catch (Exception e){
                if (useDebugLevelLogging) logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                else logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
            
            try {
                if (reader != null) {
                    reader.close();
                }
            }
            catch (Exception e){
                if (useDebugLevelLogging) logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                else logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
    }

}