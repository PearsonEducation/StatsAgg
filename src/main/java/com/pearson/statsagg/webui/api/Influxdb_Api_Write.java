package com.pearson.statsagg.webui.api;

import com.google.common.io.CharStreams;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.influxdb.InfluxdbMetric_v1;
import com.pearson.statsagg.utilities.StackTrace;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.codec.binary.Base64;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name="Influxdb_Api_Write", urlPatterns={"/db/*"})
public class Influxdb_Api_Write extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(Influxdb_Api_Write.class.getName());

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
            String httpAuth = request.getHeader("Authorization");
            boolean isUsingHttpBasicAuth = false;
            
            if ((httpAuth != null) && (username == null) && (password == null) && httpAuth.startsWith("Basic ")) {
                String base64EncodedCredentials = httpAuth.substring(6);
                
                try {
                    byte[] credentialsBytes = Base64.decodeBase64(base64EncodedCredentials);
                    String credentialsString = new String(credentialsBytes);
                    
                    int colonIndex = credentialsString.indexOf(':');
                    if ((colonIndex > -1) && (colonIndex < (credentialsString.length() - 1))) {
                        username = credentialsString.substring(0, colonIndex);
                        password = credentialsString.substring(colonIndex + 1, credentialsString.length());
                        
                        isUsingHttpBasicAuth = true;
                    }
                }
                catch (Exception e) {
                    logger.warn("Error decoding HTTP Basic Auth base64 credentials.");
                }
            }
            
            String json = CharStreams.toString(request.getReader());
            
            String requestUri = request.getRequestURI();
            String database = (requestUri == null) ? null : StringUtils.substringBetween(requestUri, "/db/", "/series");
            
            parseMetrics(database, json, username, password, isUsingHttpBasicAuth, GlobalVariables.influxdbPrefix, metricsReceivedTimestampInMilliseconds);

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

    public static void parseMetrics(String database, String inputJson, String username, String password, boolean isUsingHttpBasicAuth, 
            String metricPrefix, long metricsReceivedTimestampInMilliseconds) {
                
        List<InfluxdbMetric_v1> influxdbMetrics = InfluxdbMetric_v1.parseInfluxdbMetricJson(database, inputJson, username, password, 
                isUsingHttpBasicAuth, metricPrefix, metricsReceivedTimestampInMilliseconds);

        for (InfluxdbMetric_v1 influxdbMetric : influxdbMetrics) {
            long hashKey = GlobalVariables.metricHashKeyGenerator.incrementAndGet();
            influxdbMetric.setHashKey(hashKey);
            GlobalVariables.influxdbMetrics.put(influxdbMetric.getHashKey(), influxdbMetric);
            GlobalVariables.incomingMetricsCount.incrementAndGet();

            if (ApplicationConfiguration.isDebugModeEnabled()) {
                logger.info("Database=\"" + database + "\", HTTP_InfluxDB_String=\"" + inputJson + "\"");
            }
        }

    }

}
