package com.pearson.statsagg.webui;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.MetricTimestampAndValue;
import com.pearson.statsagg.utilities.StackTrace;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "MetricRecentValues", urlPatterns = {"/MetricRecentValues"})
public class MetricRecentValues extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(MetricRecentValues.class.getName());
    
    public static final String PAGE_NAME = "Metric Recent Values";
    
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
     * Handles the HTTP <code>POST</code> method.
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
        
        processPostRequest(request, response);
    }
    
    protected void processPostRequest(HttpServletRequest request, HttpServletResponse response) {
        
        if ((request == null) || (response == null)) {
            return;
        }
        
        response.setContentType("text/html");
        PrintWriter out = null;
    
        String metricKey = request.getParameter("MetricKey");
                
        try {  
            StringBuilder htmlBuilder = new StringBuilder("");

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            
            String metricTimestampsAndValues = getRecentMetricTimestampsAndValues(metricKey);
                    
            String htmlBody = statsAggHtmlFramework.createHtmlBody(
            "<div id=\"page-content-wrapper\">\n" +
            "  <!-- Keep all page content within the page-content inset div! -->\n" +
            "  <div class=\"page-content inset\">\n" +
            "    <div class=\"content-header\"> \n" +
            "      <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "    </div>" +   
            metricTimestampsAndValues +
            "  </div>\n" +
            "</div>\n");

            htmlBuilder.append("<!DOCTYPE html>\n<html>\n").append(htmlHeader).append(htmlBody).append("</html>");
            
            Document htmlDocument = Jsoup.parse(htmlBuilder.toString());
            String htmlFormatted  = htmlDocument.toString();
            out = response.getWriter();
            if (ApplicationConfiguration.isDebugModeEnabled()) out.println(htmlBuilder);
            else out.println(htmlFormatted);
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

    private String getRecentMetricTimestampsAndValues(String metricKey) {
        
        if (metricKey == null) {
            return "";
        }
        
        StringBuilder outputString = new StringBuilder("");
        SimpleDateFormat dateAndTimeFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");

        List<MetricTimestampAndValue> metricTimestampsAndValuesLocal = new ArrayList<>();

        synchronized(GlobalVariables.recentMetricTimestampsAndValuesByMetricKey) {
            Set<MetricTimestampAndValue> metricTimestampsAndValues = GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.get(metricKey);

            if (metricTimestampsAndValues != null) {
                synchronized(metricTimestampsAndValues) {
                    metricTimestampsAndValuesLocal = new ArrayList<>(metricTimestampsAndValues);
                }
            }
        }
        
        java.util.Collections.sort(metricTimestampsAndValuesLocal, MetricTimestampAndValue.COMPARE_BY_TIMESTAMP);

        outputString.append("<b>").append(StatsAggHtmlFramework.htmlEncode(metricKey)).append("</b>").append("<br>").append("<br>");

        for (int i = (metricTimestampsAndValuesLocal.size() - 1); i >= 0; i--) {
            
            MetricTimestampAndValue metricTimestampAndValue = metricTimestampsAndValuesLocal.get(i);
            
            if ((metricTimestampAndValue.getTimestamp() != null) && (metricTimestampAndValue.getMetricValue() != null)) {
                String timestamp = dateAndTimeFormat.format(metricTimestampAndValue.getTimestamp());
                String metricValue = metricTimestampAndValue.getMetricValue().stripTrailingZeros().toPlainString();
                
                outputString.append(timestamp).append(" : ").append(metricValue).append("<br>");
            }
            
        }

        return outputString.toString();
    }

}
