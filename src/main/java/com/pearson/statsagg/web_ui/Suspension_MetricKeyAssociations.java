package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.database_objects.suspensions.Suspension;
import com.pearson.statsagg.database_objects.suspensions.SuspensionsDao;
import java.io.PrintWriter;
import java.util.Set;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.MetricTimestampAndValue;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class Suspension_MetricKeyAssociations extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(Suspension_MetricKeyAssociations.class.getName());
    
    public static final String PAGE_NAME = "Suspension - Metric Key Associations";
    
    private static final int MAX_METRIC_KEYS_TO_DISPLAY = 1000;
    
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
        processGetRequest(request, response);
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
        
        try {  
            request.setCharacterEncoding("UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html");
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        } 
        
        PrintWriter out = null;
    
        String name = request.getParameter("Name");
        boolean excludeNavbar = StringUtilities.isStringValueBooleanTrue(request.getParameter("ExcludeNavbar"));
        String suspensionMetricKeyAssociations = getSuspensionMetricKeyAssociations(name, excludeNavbar);
                
        try {  
            StringBuilder htmlBuilder = new StringBuilder();

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            
            String htmlBody = statsAggHtmlFramework.createHtmlBody(
            "<div id=\"page-content-wrapper\">\n" +
            "<!-- Keep all page content within the page-content inset div! -->\n" +
            "  <div class=\"page-content inset statsagg_page_content_font\">\n" +
            "    <div class=\"content-header\"> \n" +
            "      <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "    </div> " +
            "    <div class=\"statsagg_force_word_wrap\">" +
            suspensionMetricKeyAssociations +
            "    </div>\n" +
            "  </div>\n" +
            "</div>\n",
            excludeNavbar);
            
            htmlBuilder.append("<!DOCTYPE html>\n<html>\n").append(htmlHeader).append(htmlBody).append("</html>");
            
            Document htmlDocument = Jsoup.parse(htmlBuilder.toString());
            String htmlFormatted  = htmlDocument.toString();
            out = response.getWriter();
            out.println(htmlFormatted);
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

    private String getSuspensionMetricKeyAssociations(String suspensionName, boolean excludeNavbar) {
        
        if (suspensionName == null) {
            return "";
        }
        
        StringBuilder outputString = new StringBuilder();
        
        Suspension suspension = SuspensionsDao.getSuspension(DatabaseConnections.getConnection(), true, suspensionName);
        
        if ((suspension != null) && (suspension.getId() != null) && (suspension.getName() != null)) {
            outputString.append("<b>Name</b> = ").append(StatsAggHtmlFramework.htmlEncode(suspension.getName())).append("<br>");

            int associationCount = 0;
            Set<String> matchingMetricKeysAssociatedWithSuspension = GlobalVariables.matchingMetricKeysAssociatedWithSuspension.get(suspension.getId());
            
            if (matchingMetricKeysAssociatedWithSuspension == null) {
                outputString.append("<b>Total Suspension - Metric Key Associations (over the last 24 hours)</b> = ").append("0");
            }
            else {
                List<String> matchingMetricKeysAssociatedWithSuspensionSorted = new ArrayList<>();
                synchronized(matchingMetricKeysAssociatedWithSuspension) {
                    associationCount = matchingMetricKeysAssociatedWithSuspension.size();
                    
                    int i = 1;
                    for (String matchingMetricKeyAssociatedWithSuspension : matchingMetricKeysAssociatedWithSuspension) {
                        matchingMetricKeysAssociatedWithSuspensionSorted.add(matchingMetricKeyAssociatedWithSuspension);
                        i++;
                        if (i > MAX_METRIC_KEYS_TO_DISPLAY) break;
                    }
                }
                
                Collections.sort(matchingMetricKeysAssociatedWithSuspensionSorted);
                
                outputString.append("<b>Total Suspension - Metric Key Associations (over the last 24 hours)</b> = ").append(associationCount).append("<br><br>");
                
                if (associationCount > 0) {
                    outputString.append("<b>Suspension - Metric Key Associations (over the last 24 hours)...</b>").append("<br>");
                    
                    int associationOutputCounter = 0;
                    outputString.append("<ul>");

                    for (String metricKey : matchingMetricKeysAssociatedWithSuspensionSorted) {
                        List<MetricTimestampAndValue> metricTimestampsAndValues = getSortedMetricTimestampsAndValues(metricKey);
                        BigDecimal mostRecentValue = null;
                        if ((metricTimestampsAndValues != null) && !metricTimestampsAndValues.isEmpty()) mostRecentValue = metricTimestampsAndValues.get(metricTimestampsAndValues.size() - 1).getMetricValue();
                        
                        outputString.append("<li>");
                        outputString.append("<a class=\"iframe cboxElement\" href=\"MetricRecentValues?ExcludeNavbar=").append(excludeNavbar).append("&amp;MetricKey=").append(StatsAggHtmlFramework.urlEncode(metricKey)).append("\">");
                        outputString.append(StatsAggHtmlFramework.htmlEncode(metricKey)).append("</a>");
                        if (mostRecentValue != null) outputString.append(" = ").append(mostRecentValue.stripTrailingZeros().toPlainString()).append(" (most recent value)");
                        outputString.append("</li>");

                        associationOutputCounter++;
                    }

                    int numAssociationsNotOutputted = associationCount - associationOutputCounter;
                    if (numAssociationsNotOutputted > 0) outputString.append("<li>").append(numAssociationsNotOutputted).append(" more...").append("</li>");
                    
                    outputString.append("</ul>");
                }
            }
            
        }
        
        return outputString.toString();
    }

    private List<MetricTimestampAndValue> getSortedMetricTimestampsAndValues(String metricKey) {
        
        if (metricKey == null) {
            return new ArrayList<>();
        }
        
        List<MetricTimestampAndValue> metricTimestampsAndValuesLocal = new ArrayList<>();

        synchronized(GlobalVariables.recentMetricTimestampsAndValuesByMetricKey) {
            List<MetricTimestampAndValue> metricTimestampsAndValues = GlobalVariables.recentMetricTimestampsAndValuesByMetricKey.get(metricKey);

            if (metricTimestampsAndValues != null) {
                synchronized(metricTimestampsAndValues) {
                    metricTimestampsAndValuesLocal = new ArrayList<>(metricTimestampsAndValues);
                }
            }
        }

        java.util.Collections.sort(metricTimestampsAndValuesLocal, MetricTimestampAndValue.COMPARE_BY_TIMESTAMP);

        return metricTimestampsAndValuesLocal;
    }
    
}
