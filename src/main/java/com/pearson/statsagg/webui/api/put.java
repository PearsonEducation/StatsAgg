package com.pearson.statsagg.webui.api;

import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.opentsdb.OpenTsdbMetricRaw;
import com.pearson.statsagg.utilities.StackTrace;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name="put", urlPatterns={"/api/put"})
public class put extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(put.class.getName());

    public static final String PAGE_NAME = "OpenTSDB Put API";

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        processPostRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return PAGE_NAME;
    }
    
    protected void processPostRequest(HttpServletRequest request, HttpServletResponse response) {
        
        if ((request == null) || (response == null)) {
            return;
        }
        
        long metricsReceivedTimestampInMilliseconds = System.currentTimeMillis();
        BufferedReader requestBodyReader = null;
        PrintWriter out = null;
        
        try {
            response.setContentType("text/json");     
            StringBuilder inputJson = new StringBuilder("");
            
            requestBodyReader = request.getReader();
            
            if (requestBodyReader != null) {
                String requestLine = "";
                
                while (requestLine != null) {
                    requestLine = requestBodyReader.readLine();
                    if (requestLine != null) inputJson.append(requestLine);
                }
            }
            
            List<OpenTsdbMetricRaw> openTsdbMetricsRaw = OpenTsdbMetricRaw.parseOpenTsdbJson(inputJson.toString(), metricsReceivedTimestampInMilliseconds);

            for (OpenTsdbMetricRaw openTsdbMetricRaw : openTsdbMetricsRaw) {
                Long hashKey = GlobalVariables.rawMetricHashKeyGenerator.incrementAndGet();
                openTsdbMetricRaw.setHashKey(hashKey);

                if (ApplicationConfiguration.isOpenTsdbSendPreviousValue()) {
                    openTsdbMetricRaw.createAndGetMetricTimestampInMilliseconds();
                }

                GlobalVariables.openTsdbMetricsRaw.put(openTsdbMetricRaw.getHashKey(), openTsdbMetricRaw);
                GlobalVariables.incomingMetricsCount.incrementAndGet();
            }
            
            if (ApplicationConfiguration.isDebugModeEnabled()) {
                logger.info("HTTP_TCP_OpenTsdb_Received_Metrics=" + openTsdbMetricsRaw.size());
                logger.info("HTTP_TCP_OpenTsdb_String=\"" + inputJson.toString() + "\"");
            }
                
            response.setStatus(204);
            out = response.getWriter();
            out.println("");
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {            
            if (requestBodyReader != null) {
                try {
                    requestBodyReader.close();
                }
                catch (Exception e){}
            }
            
            if (out != null) {
                out.close();
            }
        }
    }
  
}
