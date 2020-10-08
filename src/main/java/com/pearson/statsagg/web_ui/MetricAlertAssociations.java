package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.alerts.MetricAssociation;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricAlertAssociations extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(MetricAlertAssociations.class.getName());
    
    public static final String PAGE_NAME = "Metric - Alert Associations";
    
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
        
        try {  
            request.setCharacterEncoding("UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html");
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        } 
        
        PrintWriter out = null;
    
        try {  
            StringBuilder htmlBuilder = new StringBuilder();

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");

            String htmlBodyContents = buildRegexTesterHtml("", "");
            String htmlBody = statsAggHtmlFramework.createHtmlBody(htmlBodyContents);
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
    
    protected void processPostRequest(HttpServletRequest request, HttpServletResponse response) {
        
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
        
        try {
            String regex = request.getParameter("Regex");
            boolean excludeNavbar = StringUtilities.isStringValueBooleanTrue(request.getParameter("ExcludeNavbar"));
            
            Set<String> metricKeys = MetricAssociation.getRegexMatches(GlobalVariables.metricKeysLastSeenTimestamp.keySet(), regex, null, -1);
            Set<Alert> alertsAssociatedWithMetrics = getAlertsAssociatedWithMetrics(metricKeys);
            String alertMatchesHtml = getAlertMatchesHtml(alertsAssociatedWithMetrics, excludeNavbar);
            
            StringBuilder htmlBuilder = new StringBuilder();

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");

            String htmlBodyContents = buildRegexTesterHtml(regex, alertMatchesHtml);
            String htmlBody = statsAggHtmlFramework.createHtmlBody(htmlBodyContents);
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
    
    private String buildRegexTesterHtml(String regex, String regexMatches) {

        StringBuilder htmlBody = new StringBuilder();

        htmlBody.append(
            "<div id=\"page-content-wrapper\">\n" +
            "<!-- Keep all page content within the page-content inset div! -->\n" +
            "<div class=\"page-content inset statsagg_page_content_font\">\n" +
            "  <div class=\"content-header\"> \n" +
            "    <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "  </div> " +
            "  <form action=\"MetricAlertAssociations\" method=\"POST\">\n");
        
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Regex to test</label>\n" +
            "  <input class=\"form-control-statsagg\" placeholder=\"Enter a regex to match against metrics. Metrics that are associated with alerts will be displayed.\" name=\"Regex\" ");
        
        if ((regex != null) && (!regex.isEmpty())) {
            htmlBody.append(" value=\"").append(StatsAggHtmlFramework.htmlEncode(regex, true)).append("\"");
        }
        
        htmlBody.append(">\n</div>\n");
       
        htmlBody.append(
            "  <button type=\"submit\" class=\"btn btn-default statsagg_page_content_font\">Submit</button>\n" +
            "</form>\n");
        
        if (!regexMatches.equals("")) {
            htmlBody.append("<br><hr><br>");
        }
        
        htmlBody.append("<div class=\"statsagg_force_word_wrap\">");
        htmlBody.append(regexMatches);
        htmlBody.append("</div>");
        
        htmlBody.append("</div>\n" + "</div>\n");
            
        return htmlBody.toString();
    }
    
    private static Set<Alert> getAlertsAssociatedWithMetrics(Set<String> regexMatchedMetricKeys) {
        
        if (regexMatchedMetricKeys == null) {
            return null;
        }

        Set<Integer> metricGroupIdsAssociatedWithMetrics = new HashSet<>();
        for (Integer metricGroupId : GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.keySet()) {
            Set<String> matchingMetricKeysAssociatedWithMetricGroup = GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.get(metricGroupId);
            if ((matchingMetricKeysAssociatedWithMetricGroup == null) || matchingMetricKeysAssociatedWithMetricGroup.isEmpty()) continue;
            
            for (String regexMatchedMetricKey : regexMatchedMetricKeys) {
                if (matchingMetricKeysAssociatedWithMetricGroup.contains(regexMatchedMetricKey)) metricGroupIdsAssociatedWithMetrics.add(metricGroupId);
            }
        }
                
        List<Alert> alerts = AlertsDao.getAlerts(DatabaseConnections.getConnection(), true);
        if ((alerts == null) || alerts.isEmpty()) return null;
        
        Set<Alert> alertsAssociatedWithMetrics = new HashSet<>();
        for (Integer metricGroupIdAssociatedWithMetrics : metricGroupIdsAssociatedWithMetrics) {
            for (Alert alert : alerts) {
                if ((alert != null) && (alert.getMetricGroupId() != null) && (alert.getMetricGroupId().intValue() == metricGroupIdAssociatedWithMetrics.intValue())) {
                    alertsAssociatedWithMetrics.add(alert);
                }
            }
        }
        
        return alertsAssociatedWithMetrics;
    }
    
    private static String getAlertMatchesHtml(Set<Alert> alerts, boolean excludeNavbar) {
        
        if (alerts == null) {
            return "<b>Alert Match Count</b> = 0";
        }
        
        List<String> alertNames = new ArrayList<>();
        for (Alert alert : alerts) if ((alert != null) && (alert.getName() != null)) alertNames.add(alert.getName());
        Collections.sort(alertNames);
        
        StringBuilder outputString = new StringBuilder();
                
        String metricKeyCountString = Integer.toString(alerts.size());
        outputString.append("<b>Alert Match Count</b> = ").append(metricKeyCountString).append("<br><br>");

        if (alerts.size() > 0) {
            outputString.append("<b>Matching Alerts...</b>").append("<br>");

            outputString.append("<ul>");

            for (String alertName : alertNames) {
                String alertDetailsUrl = "<a href=\"AlertDetails?ExcludeNavbar=" + excludeNavbar + "&amp;Name=" + StatsAggHtmlFramework.urlEncode(alertName) + "\">" + StatsAggHtmlFramework.htmlEncode(alertName) + "</a>";
                outputString.append("<li>").append(alertDetailsUrl).append("</li>");
            }
            
            outputString.append("</ul>");
        }
        
        return outputString.toString();
    }
    
}
