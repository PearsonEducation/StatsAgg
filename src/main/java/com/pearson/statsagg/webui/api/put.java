package com.pearson.statsagg.webui.api;

import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.opentsdb.OpenTsdbMetricRaw;
import com.pearson.statsagg.utilities.StackTrace;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
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
            out.println("<head>" + PAGE_NAME + "</head>" + "<body><h1>" + PAGE_NAME + "</h1></body>");
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

            String json = getJsonPayloadFromRequest(request);
            
            String responseMessage = parseMetrics(json, metricsReceivedTimestampInMilliseconds, doesRequestSummary, doesRequestDetails);
                            
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
    
    protected String getJsonPayloadFromRequest(HttpServletRequest request) {
        
        if (request == null) {
            return null;
        }
        
        BufferedReader requestBodyReader = null;

        try {
            StringBuilder json = new StringBuilder("");
            requestBodyReader = request.getReader();

            if (requestBodyReader != null) {
                String requestLine = "";

                while (requestLine != null) {
                    requestLine = requestBodyReader.readLine();
                    if (requestLine != null) json.append(requestLine);
                }
            }
            
            return json.toString();
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        finally {            
            if (requestBodyReader != null) {
                try {
                    requestBodyReader.close();
                }
                catch (Exception e){}
            }
        }
        
    }
    
    public static String parseMetrics(String inputJson, long metricsReceivedTimestampInMilliseconds, boolean doesRequestSummary, boolean doesRequestDetails) {
        
        List<Integer> successCountAndFailCount = new ArrayList<>();
        
        List<OpenTsdbMetricRaw> openTsdbMetricsRaw = OpenTsdbMetricRaw.parseOpenTsdbJson(inputJson, metricsReceivedTimestampInMilliseconds, successCountAndFailCount);
        
        if (successCountAndFailCount.isEmpty()) {
            successCountAndFailCount.add(0);
            successCountAndFailCount.add(0);
        }
        
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
            logger.info("HTTP_TCP_OpenTsdb_String=\"" + inputJson + "\"");
        }
        
        if (doesRequestSummary) {
            StringBuilder summaryResponse = new StringBuilder("");
            summaryResponse.append("{\"failed\":").append(successCountAndFailCount.get(1)).append(",\"success\":").append(successCountAndFailCount.get(0)).append("}");
            return summaryResponse.toString();
        }
        
        if (doesRequestDetails) {
            StringBuilder detailsResponse = new StringBuilder("");
            detailsResponse.append("{\"failed\":").append(successCountAndFailCount.get(1)).append(",\"success\":")
                    .append(successCountAndFailCount.get(0)).append(",\"errors\":[]").append("}");
            return detailsResponse.toString();
        }
    
        return "";
    }

}
