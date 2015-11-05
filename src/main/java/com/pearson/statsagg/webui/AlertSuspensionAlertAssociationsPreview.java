package com.pearson.statsagg.webui;

import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database_objects.alert_suspensions.AlertSuspension;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.StackTrace;
import com.pearson.statsagg.utilities.StringUtilities;
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
@WebServlet(name = "AlertSuspensionAlertAssociationsPreview", urlPatterns = {"/AlertSuspensionAlertAssociationsPreview"})
public class AlertSuspensionAlertAssociationsPreview extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AlertSuspensionAlertAssociationsPreview.class.getName());
    
    public static final String PAGE_NAME = "Alert Associations";
    
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
        
        response.setContentType("text/html");
        PrintWriter out = null;
    
        AlertSuspension suspension = getSuspensionFromParameters(request);
        String alertAssociationsBody = getSuspension_ResponseHtml(suspension);
        
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
            alertAssociationsBody +
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

    private AlertSuspension getSuspensionFromParameters(HttpServletRequest request) {
        
        if (request == null) {
            return null;
        }
        
        String suspensionName = "Suspension Preview";
        
        AlertSuspension suspension = new AlertSuspension(
                -1, suspensionName, suspensionName.toUpperCase(), true, null, 1, null, null, "", false, true, 
                true, true, true, true, true, true, true, 
                new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), 
                60000l, DatabaseObjectCommon.TIME_UNIT_MINUTES, null);
        
        try {
            String parameter;
            
            parameter = request.getParameter("CreateSuspension_SuspendBy");
            if ((parameter != null) && parameter.contains("AlertName")) suspension.setSuspendBy(AlertSuspension.SUSPEND_BY_ALERT_ID);
            else if ((parameter != null) && parameter.contains("Tags")) suspension.setSuspendBy(AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS);
            else if ((parameter != null) && parameter.contains("Everything")) suspension.setSuspendBy(AlertSuspension.SUSPEND_BY_EVERYTHING);
            else if ((parameter != null) && parameter.contains("Metrics")) suspension.setSuspendBy(AlertSuspension.SUSPEND_BY_METRICS);
            
            parameter = request.getParameter("AlertName");
            AlertsDao alertsDao = new AlertsDao();
            Alert alert = alertsDao.getAlertByName(parameter);
            if (alert != null) suspension.setAlertId(alert.getId());

            parameter = request.getParameter("MetricGroupTagsInclusive");
            if (parameter != null) {
                String trimmedTags = AlertSuspension.trimNewLineDelimitedTags(parameter);
                suspension.setMetricGroupTagsInclusive(trimmedTags);
            }
            
            parameter = request.getParameter("MetricGroupTagsExclusive");
            if (parameter != null) {
                String trimmedTags = AlertSuspension.trimNewLineDelimitedTags(parameter);
                suspension.setMetricGroupTagsExclusive(trimmedTags);
            }
            
            parameter = request.getParameter("MetricSuspensionRegexes");
            if (parameter != null) {
                String trimmedRegexes = AlertSuspension.trimNewLineDelimitedTags(parameter);
                suspension.setMetricSuspensionRegexes(trimmedRegexes);
            }
        }
        catch (Exception e) {
            suspension = null;
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
            
        if (AlertSuspension.isValid_CheckSuspendBy(suspension)) return suspension;
        else return null;
    }
   
    protected String getSuspension_ResponseHtml(AlertSuspension suspension) {
        
        if (suspension == null) {
            return "<b>Invalid suspension</b>";
        }
        
        if (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_ALERT_ID) return getAlertSuspension_AlertAssociations_ResponseHtml(suspension);
        if (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS) return getAlertSuspension_AlertAssociations_ResponseHtml(suspension);
        if (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_EVERYTHING) return getAlertSuspension_AlertAssociations_ResponseHtml(suspension);
        if (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_METRICS) return getAlertSuspension_MetricSuspensions(suspension);
        else return "<b>Invalid suspension</b>";
    }

    private String getAlertSuspension_AlertAssociations_ResponseHtml(AlertSuspension suspension) {
        List<String> alertNames = new ArrayList<>();
        
        AlertsDao alertsDao = new AlertsDao();
        List<Alert> alerts = alertsDao.getAllDatabaseObjectsInTable();
        
        for (Alert alert : alerts) {
            if ((alert.getName() == null) || alert.getName().isEmpty()) continue;
            
            boolean outputAlert = false;
            
            if (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_ALERT_ID) {
                outputAlert = com.pearson.statsagg.alerts.AlertSuspensions.isSuspensionCriteriaMet_SuspendByAlertName(alert, suspension);
            }
            else if (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS) {
                outputAlert = com.pearson.statsagg.alerts.AlertSuspensions.isSuspensionCriteriaMet_SuspendedByMetricGroupTags(alert, suspension);
            }
            else if (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_EVERYTHING) {
                outputAlert = com.pearson.statsagg.alerts.AlertSuspensions.isSuspensionCriteriaMet_SuspendedByEverything(alert, suspension);
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
                String alertSuspensionDetailsUrl = "<a href=\"AlertDetails?Name=" + StatsAggHtmlFramework.urlEncode(alertName) + "\">" + StatsAggHtmlFramework.htmlEncode(alertName) + "</a>";
                outputString.append("<li>").append(alertSuspensionDetailsUrl).append("</li>");
            }
        }
        
        outputString.append("</ul>");

        return outputString.toString();
    }
    
    protected String getAlertSuspension_MetricSuspensions(AlertSuspension suspension) {
        
        List<String> matchRegexes = StringUtilities.getListOfStringsFromDelimitedString(suspension.getMetricSuspensionRegexes(), '\n');
        String mergedMatchRegex = StringUtilities.createMergedRegex(matchRegexes);
        
        Set<String> matchMetricKeys = RegexTester.getRegexMatches(GlobalVariables.metricKeysLastSeenTimestamp.keySet(), mergedMatchRegex, null, 1001);
        String regexMatchesHtml = RegexTester.getRegexMatchesHtml(matchMetricKeys, 1000);
        
        return regexMatchesHtml;
    }
    
}
