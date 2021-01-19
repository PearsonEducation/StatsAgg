package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.threads.alert_related.MetricAssociation;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database_objects.suspensions.Suspension;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class SuspensionAssociationsPreview extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(SuspensionAssociationsPreview.class.getName());
    
    public static final String PAGE_NAME = "Suspension Associations";
    
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
    
        Suspension suspension = getSuspensionFromParameters(request);
        String suspensionAssociationsBody = getSuspension_ResponseHtml(suspension);
        
        try {  
            StringBuilder htmlBuilder = new StringBuilder();

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            
            String htmlBody =
            "<body>" +
            "  <div id=\"page-content-wrapper\">\n" +
            "    <!-- Keep all page content within the page-content inset div! -->\n" +
            "    <div class=\"page-content inset statsagg_page_content_font\">\n" +
            "      <div class=\"statsagg_force_word_wrap\">" +
            suspensionAssociationsBody +
            "      </div>\n" +
            "    </div>\n" +
            "  </div>\n" +
            "</body>";
            
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

    private Suspension getSuspensionFromParameters(HttpServletRequest request) {
        
        if (request == null) {
            return null;
        }
        
        String suspensionName = "Suspension Preview";
        
        Suspension suspension = new Suspension(
                -1, suspensionName, suspensionName.toUpperCase(), true, null, 1, null, null, "", false, true, 
                true, true, true, true, true, true, true, 
                new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), 
                60000l, DatabaseObjectCommon.TIME_UNIT_MINUTES, null);
        
        try {
            String parameter;
            
            parameter = request.getParameter("SuspendBy");
            if ((parameter != null) && parameter.contains("AlertName")) suspension.setSuspendBy(Suspension.SUSPEND_BY_ALERT_ID);
            else if ((parameter != null) && parameter.contains("Tags")) suspension.setSuspendBy(Suspension.SUSPEND_BY_METRIC_GROUP_TAGS);
            else if ((parameter != null) && parameter.contains("Everything")) suspension.setSuspendBy(Suspension.SUSPEND_BY_EVERYTHING);
            else if ((parameter != null) && parameter.contains("Metrics")) suspension.setSuspendBy(Suspension.SUSPEND_BY_METRICS);
            
            parameter = request.getParameter("AlertName");
            if (parameter != null) parameter = parameter.trim();
            Alert alert = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, parameter);
            if (alert != null) suspension.setAlertId(alert.getId());

            parameter = request.getParameter("MetricGroupTagsInclusive");
            if (parameter != null) {
                String trimmedTags = Suspension.trimNewLineDelimitedTags(parameter);
                suspension.setMetricGroupTagsInclusive(trimmedTags);
            }
            
            parameter = request.getParameter("MetricGroupTagsExclusive");
            if (parameter != null) {
                String trimmedTags = Suspension.trimNewLineDelimitedTags(parameter);
                suspension.setMetricGroupTagsExclusive(trimmedTags);
            }
            
            parameter = request.getParameter("MetricSuspensionRegexes");
            if (parameter != null) {
                String trimmedRegexes = Suspension.trimNewLineDelimitedTags(parameter);
                suspension.setMetricSuspensionRegexes(trimmedRegexes);
            }
        }
        catch (Exception e) {
            suspension = null;
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
            
        if (Suspension.isValid_CheckSuspendBy(suspension).isValid()) return suspension;
        else return null;
    }
   
    protected String getSuspension_ResponseHtml(Suspension suspension) {
        
        if (suspension == null) {
            return "<b>Invalid suspension</b>";
        }
        
        if (suspension.getSuspendBy() == Suspension.SUSPEND_BY_ALERT_ID) return getAlertSuspension_AlertAssociations_ResponseHtml(suspension);
        if (suspension.getSuspendBy() == Suspension.SUSPEND_BY_METRIC_GROUP_TAGS) return getAlertSuspension_AlertAssociations_ResponseHtml(suspension);
        if (suspension.getSuspendBy() == Suspension.SUSPEND_BY_EVERYTHING) return getAlertSuspension_AlertAssociations_ResponseHtml(suspension);
        if (suspension.getSuspendBy() == Suspension.SUSPEND_BY_METRICS) return getSuspension_MetricSuspensions(suspension);
        else return "<b>Invalid suspension</b>";
    }

    private String getAlertSuspension_AlertAssociations_ResponseHtml(Suspension suspension) {
        List<String> alertNames = new ArrayList<>();
        
        List<Alert> alerts = AlertsDao.getAlerts(DatabaseConnections.getConnection(), true);
        if (alerts == null) alerts = new ArrayList<>();

        for (Alert alert : alerts) {
            if ((alert.getName() == null) || alert.getName().isEmpty()) continue;
            
            boolean outputAlert = false;
            
            if (suspension.getSuspendBy() == Suspension.SUSPEND_BY_ALERT_ID) {
                outputAlert = com.pearson.statsagg.threads.alert_related.Suspensions.isSuspensionCriteriaMet_SuspendByAlertName(alert, suspension);
            }
            else if (suspension.getSuspendBy() == Suspension.SUSPEND_BY_METRIC_GROUP_TAGS) {
                outputAlert = com.pearson.statsagg.threads.alert_related.Suspensions.isSuspensionCriteriaMet_SuspendedByMetricGroupTags(alert, suspension);
            }
            else if (suspension.getSuspendBy() == Suspension.SUSPEND_BY_EVERYTHING) {
                outputAlert = com.pearson.statsagg.threads.alert_related.Suspensions.isSuspensionCriteriaMet_SuspendedByEverything(alert, suspension);
            }

            if (outputAlert) {
                alertNames.add(alert.getName());
            }
        }
        
        Collections.sort(alertNames);
        
        StringBuilder outputString = new StringBuilder();
        outputString.append("<b>Total Alert Associations</b> = ").append(alertNames.size()).append("<br><br>");
        
        if (alertNames.size() > 0) {
            outputString.append("<b>Alert Associations...</b>").append("<br>");

            outputString.append("<ul>");
            for (String alertName : alertNames) {
                String alertDetailsUrl = "<a href=\"AlertDetails?Name=" + StatsAggHtmlFramework.urlEncode(alertName) + "\">" + StatsAggHtmlFramework.htmlEncode(alertName) + "</a>";
                outputString.append("<li>").append(alertDetailsUrl).append("</li>");
            }
        }
        
        outputString.append("</ul>");

        return outputString.toString();
    }
    
    protected String getSuspension_MetricSuspensions(Suspension suspension) {
        
        List<String> matchRegexes = StringUtilities.getListOfStringsFromDelimitedString(suspension.getMetricSuspensionRegexes(), '\n');
        String mergedMatchRegex = StringUtilities.createMergedRegex(matchRegexes);
        
        Set<String> matchMetricKeys = MetricAssociation.getRegexMatches(GlobalVariables.metricKeysLastSeenTimestamp.keySet(), mergedMatchRegex, null, 1001);
        String regexMatchesHtml = RegexTester.getRegexMatchesHtml(matchMetricKeys, 1000, false);
        
        return regexMatchesHtml;
    }
    
}
