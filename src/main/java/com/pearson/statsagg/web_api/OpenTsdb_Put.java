package com.pearson.statsagg.web_api;

import com.google.common.io.CharStreams;
import com.pearson.statsagg.configuration.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_formats.opentsdb.OpenTsdbMetric;
import com.pearson.statsagg.utilities.compress_utils.Compression;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class OpenTsdb_Put extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenTsdb_Put.class.getName());

    public static final String PAGE_NAME = "OpenTSDB 2.x Put API";
    
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        processGetRequest(request, response);
    }
    
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
   
    protected void processGetRequest(HttpServletRequest request, HttpServletResponse response) {
        
        if ((request == null) || (response == null)) {
            return;
        }
                
        PrintWriter out = null;
        
        try {
            response.setContentType("text/html"); 
            out = response.getWriter();
            out.println("<head>" + "</head>" + "<body><h1>" + PAGE_NAME + "</h1></body>");
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {            
            if (out != null) {
                out.close();
            }
        }
    }
   
    protected void processPostRequest(HttpServletRequest request, HttpServletResponse response) {
        
        if ((request == null) || (response == null)) {
            return;
        }
        
        long metricsReceivedTimestampInMilliseconds = System.currentTimeMillis();
        
        PrintWriter out = null;
        
        try {
            response.setContentType("text/json"); 
            
            boolean doesRequestSummary = false, doesRequestDetails = false;
            if ((request.getParameterMap() != null) && (request.getParameterMap().keySet() != null) && !request.getParameterMap().keySet().isEmpty()) {
                if (request.getParameterMap().keySet().contains("summary")) doesRequestSummary = true;
                if (request.getParameterMap().keySet().contains("details")) doesRequestDetails = true;
            }

            String contentEncoding = request.getHeader("Content-Encoding");
            String json;
            if ((contentEncoding != null) && contentEncoding.equalsIgnoreCase("gzip")) json = Compression.decompressGzipToString(request.getInputStream(), "UTF-8");
            else if ((contentEncoding != null) && contentEncoding.equalsIgnoreCase("deflate")) json = Compression.decompressDeflateToString(request.getInputStream(), "UTF-8");
            else json = CharStreams.toString(request.getReader());
            
            String responseMessage = parseMetrics(json, GlobalVariables.openTsdbPrefix, metricsReceivedTimestampInMilliseconds, doesRequestSummary, doesRequestDetails);
                            
            if (doesRequestSummary) response.setStatus(200);
            else if (doesRequestDetails) response.setStatus(200);
            else response.setStatus(204);
            
            out = response.getWriter();
            if (doesRequestSummary || doesRequestDetails) out.println(responseMessage);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {            
            if (out != null) {
                out.close();
            }
        }
    }

    public static String parseMetrics(String inputJson, String metricPrefix, long metricsReceivedTimestampInMilliseconds, 
            boolean doesRequestSummary, boolean doesRequestDetails) {
        
        List<Integer> successCountAndFailCount = new ArrayList<>();
        
        List<OpenTsdbMetric> openTsdbMetrics = OpenTsdbMetric.parseOpenTsdbJson(inputJson, metricPrefix, 
                metricsReceivedTimestampInMilliseconds, successCountAndFailCount);
        
        if (successCountAndFailCount.isEmpty()) {
            successCountAndFailCount.add(0);
            successCountAndFailCount.add(0);
        }
        
        for (OpenTsdbMetric openTsdbMetric : openTsdbMetrics) {
            long hashKey = GlobalVariables.metricHashKeyGenerator.incrementAndGet();
            openTsdbMetric.setHashKey(hashKey);
            GlobalVariables.openTsdbMetrics.put(openTsdbMetric.getHashKey(), openTsdbMetric);
            GlobalVariables.incomingMetricsCount.incrementAndGet();
        }
        
        if (ApplicationConfiguration.isDebugModeEnabled()) {
            logger.info("HTTP_OpenTSDB_Num_Received_Metrics=" + openTsdbMetrics.size());
            logger.info("HTTP_OpenTSDB_String=\"" + inputJson + "\"");
        }
        
        if (doesRequestSummary) {
            StringBuilder summaryResponse = new StringBuilder();
            summaryResponse.append("{\"failed\":").append(successCountAndFailCount.get(1)).append(",\"success\":").append(successCountAndFailCount.get(0)).append("}");
            return summaryResponse.toString();
        }
        
        if (doesRequestDetails) {
            StringBuilder detailsResponse = new StringBuilder();
            detailsResponse.append("{\"failed\":").append(successCountAndFailCount.get(1)).append(",\"success\":")
                    .append(successCountAndFailCount.get(0)).append(",\"errors\":[]").append("}");
            return detailsResponse.toString();
        }
    
        return "";
    }

}
