package com.pearson.statsagg.web_api;

import com.google.common.io.CharStreams;
import com.pearson.statsagg.configuration.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_formats.influxdb.InfluxdbMetric_v1;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class InfluxdbV1_Write extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(InfluxdbV1_Write.class.getName());

    public static final String PAGE_NAME = "InfluxDB Write API (v0.6, v0.7, v0.8)";
    
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
            
            String username = request.getParameter("u");
            String password = request.getParameter("p");
            String timePrecision = request.getParameter("time_precision");
            String httpAuth = request.getHeader("Authorization");

            String json = CharStreams.toString(request.getReader());
            
            String requestUri = request.getRequestURI();
            String database = (requestUri == null) ? null : StringUtils.substringBetween(requestUri, "/db/", "/series");

            parseMetrics(database, json, username, password, httpAuth, timePrecision, GlobalVariables.influxdbPrefix, metricsReceivedTimestampInMilliseconds);

            out = response.getWriter();
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

    public static void parseMetrics(String database, String inputJson, String username, String password, 
            String httpAuth, String timePrecision, String namePrefix, long metricsReceivedTimestampInMilliseconds) {
                
        List<InfluxdbMetric_v1> influxdbMetrics = InfluxdbMetric_v1.parseInfluxdbMetricJson(database, inputJson, username, password, httpAuth, 
                timePrecision, namePrefix, metricsReceivedTimestampInMilliseconds);

        for (InfluxdbMetric_v1 influxdbMetric : influxdbMetrics) {
            long hashKey = GlobalVariables.metricHashKeyGenerator.incrementAndGet();
            influxdbMetric.setHashKey(hashKey);
            influxdbMetric.setIncludeDatabaseInNonNativeInfluxdbStandardizedMetricsOutput(ApplicationConfiguration.isInfluxdbIncludeDatabaseNameInNonNativeOutput());
            GlobalVariables.influxdbV1Metrics.put(influxdbMetric.getHashKey(), influxdbMetric);
            if (influxdbMetric.getInfluxdbStandardizedMetrics() != null) GlobalVariables.incomingMetricsCount.addAndGet(influxdbMetric.getInfluxdbStandardizedMetrics().size());

            if (ApplicationConfiguration.isDebugModeEnabled()) {
                logger.info("Database=\"" + database + "\", HTTP_InfluxDB_String=\"" + inputJson + "\"");
            }
        }

    }

}
