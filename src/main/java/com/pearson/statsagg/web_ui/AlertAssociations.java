package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.database_objects.alerts.AlertsDaoWrapper;
import com.pearson.statsagg.globals.DatabaseConnections;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.time_utils.DateAndTime;
import com.pearson.statsagg.utilities.core_utils.KeyValue;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class AlertAssociations extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AlertAssociations.class.getName());
    
    public static final String PAGE_NAME = "Alert Associations";
    
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
    
        String name = request.getParameter("Name");
        String level = request.getParameter("Level");
        String acknowledgeLevel = request.getParameter("AcknowledgeLevel");
        String acknowledgeChange = request.getParameter("AcknowledgeChange");
        boolean excludeNavbar = StringUtilities.isStringValueBooleanTrue(request.getParameter("ExcludeNavbar"));

        Alert alert = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, name);
        
        // if a user clicked on the 'acknowledge' button, change the acknowledgement status in the database
        boolean wasAcknowledgementUpdated = acknowledgeAlert(alert, acknowledgeChange, acknowledgeLevel);
        
        if (wasAcknowledgementUpdated) {
            alert = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, name);
        }
        
        String alertAssociations = "";
        
        if (level != null) {
            if (level.equalsIgnoreCase("Triggered")) alertAssociations = getTriggeredAlertAssociations(name, level, excludeNavbar);
            else if (level.equalsIgnoreCase("Caution")) alertAssociations = getCautionAlertAssociations(name, level, excludeNavbar);
            else if (level.equalsIgnoreCase("Danger")) alertAssociations = getDangerAlertAssociations(name, level, excludeNavbar);
        }
        
        try {  
            StringBuilder htmlBuilder = new StringBuilder();

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            
            StringBuilder htmlBodyBuilder = new StringBuilder();
            htmlBodyBuilder.append(
                "<div id=\"page-content-wrapper\">\n" +
                "<!-- Keep all page content within the page-content inset div! -->\n" +
                "  <div class=\"page-content inset statsagg_page_content_font\">\n" +
                "    <div class=\"content-header\"> \n" +
                "      <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
                "      <div class=\"pull-right \">\n");
            
            String acknowledgeButtonHtml = getAcknowledgeButtonHtml(alert, level, excludeNavbar);
            String clearAllButtonHtml = getClearAllButtonHtml(alert, level, excludeNavbar);
            htmlBodyBuilder.append(acknowledgeButtonHtml);
            if (!acknowledgeButtonHtml.isEmpty() && !clearAllButtonHtml.isEmpty()) htmlBodyBuilder.append("&nbsp;");
            htmlBodyBuilder.append(clearAllButtonHtml);
            
            htmlBodyBuilder.append(
                "      </div>\n" + 
                "    </div>\n " +
                "    <div class=\"statsagg_force_word_wrap\">" +
                alertAssociations +
                "    </div>\n" +
                "  </div>\n" +
                "</div>\n");
            
            String htmlBody = statsAggHtmlFramework.createHtmlBody(htmlBodyBuilder.toString(), excludeNavbar);
 
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
        
        String name = request.getParameter("Name");
        String level = request.getParameter("Level");
        String forgetMetric = request.getParameter("ForgetMetric");
        String clearAll = request.getParameter("ClearAll");
        boolean excludeNavbar = StringUtilities.isStringValueBooleanTrue(request.getParameter("ExcludeNavbar"));
        
        Alert alert = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, name);
        
        if ((forgetMetric != null) && !forgetMetric.isEmpty()) { // if the user clicked on the X button to remove a metric, perform that action & reload the page
            boolean forgetSuccess = forgetMetricAndReloadPage(forgetMetric, name, level, excludeNavbar, response);
            if (!forgetSuccess) processGetRequest(request, response);
        }
        else if ((clearAll != null) && !clearAll.isEmpty()) { // if the user clicked on the 'clear all' button, perform that action & reload the page
            boolean clearSuccess = clearAllAndReloadPage(alert, level, excludeNavbar, response);
            if (!clearSuccess) processGetRequest(request, response);
        }
        else processGetRequest(request, response);
    }
        
    private boolean forgetMetricAndReloadPage(String metricToForget, String alertName, String level, boolean excludeNavbar, HttpServletResponse response) {
        
        if ((metricToForget == null) || metricToForget.isEmpty() || (alertName == null) || (alertName.isEmpty()) || (level == null) || (level.isEmpty()) || (response == null)) {
            return false;
        }
        
        String trimmedMetricToForget = metricToForget.trim();
        GlobalVariables.immediateCleanupMetrics.put(trimmedMetricToForget, trimmedMetricToForget);
        String cleanMetricKey = StringUtilities.removeNewlinesFromString(trimmedMetricToForget);
        logger.info("Action=AlertAssociations_ForgetMetric, " + "MetricKey=\"" + cleanMetricKey + "\"");
        
        if (GlobalVariables.cleanupInvokerThread != null) GlobalVariables.cleanupInvokerThread.runCleanupThread();

        StatsAggHtmlFramework.redirectAndGet(response, 303, "AlertAssociations?" + "ExcludeNavbar=" + excludeNavbar + "&Name=" + StatsAggHtmlFramework.urlEncode(alertName) + "&" + "Level=" + StatsAggHtmlFramework.urlEncode(level));
        
        return true;
    }
            
    private boolean clearAllAndReloadPage(Alert alert, String level, boolean excludeNavbar, HttpServletResponse response) {
        
        if ((alert == null) || (alert.getName() == null) || (level == null) || (level.isEmpty()) || (response == null) || (alert.getAlertType() != Alert.TYPE_AVAILABILITY)) {
            return false;
        }
        
        HashSet<String> metricKeysToForget = new HashSet<>();
        if (level.equalsIgnoreCase("Triggered")) {
            metricKeysToForget.addAll(getActiveAlertMetricKeys(alert, GlobalVariables.activeCautionAlertMetricKeysByAlertId));
            metricKeysToForget.addAll(getActiveAlertMetricKeys(alert, GlobalVariables.activeDangerAlertMetricKeysByAlertId));
        }
        else if (level.equalsIgnoreCase("Caution")) {
            metricKeysToForget.addAll(getActiveAlertMetricKeys(alert, GlobalVariables.activeCautionAlertMetricKeysByAlertId));
        }
        else if (level.equalsIgnoreCase("Danger")) {
            metricKeysToForget.addAll(getActiveAlertMetricKeys(alert, GlobalVariables.activeDangerAlertMetricKeysByAlertId));
        }
        
        for (String metricKeyToForget : metricKeysToForget) GlobalVariables.immediateCleanupMetrics.put(metricKeyToForget, metricKeyToForget);
        
        logger.info("Action=AlertAssociations_ClearAllMetrics, " + "MetricKeyCount=\"" + metricKeysToForget.size() + "\"");

        if (GlobalVariables.cleanupInvokerThread != null) GlobalVariables.cleanupInvokerThread.runCleanupThread();

        StatsAggHtmlFramework.redirectAndGet(response, 303, "AlertAssociations?" + "ExcludeNavbar=" + excludeNavbar + "&Name=" + StatsAggHtmlFramework.urlEncode(alert.getName()) + "&" + "Level=" + StatsAggHtmlFramework.urlEncode(level));
        
        return true;
    }
    
    /*
    Returns false if the alert acknowledgement was not updated in the database. 
    Returns true if the alert acknowledgement was updated in the database. 
    */
    private boolean acknowledgeAlert(Alert alert, String acknowledgeChange, String acknowledgeLevel) {
        
        if ((alert == null) || (alert.getName() == null) || (acknowledgeChange == null) || (acknowledgeLevel == null)) {
            return false;
        }
        
        boolean didSetAlertAcknowledgement = false;
        
        Boolean acknowledgeChange_Boolean = null;
        try {
            acknowledgeChange_Boolean = Boolean.parseBoolean(acknowledgeChange);
        }
        catch (Exception e){}
        
        if ((alert.isCautionAlertActive() != null) && alert.isCautionAlertActive() && (acknowledgeLevel.equalsIgnoreCase("Caution") || acknowledgeLevel.equalsIgnoreCase("Triggered"))) {
            AlertsDaoWrapper alertsDaoWrapper = AlertsDaoWrapper.changeAlertCautionAcknowledge(alert.getName(), acknowledgeChange_Boolean);
            didSetAlertAcknowledgement = (alertsDaoWrapper.getLastAlterRecordStatus() == AlertsDaoWrapper.STATUS_CODE_SUCCESS);
        }     

        if ((alert.isDangerAlertActive() != null) && alert.isDangerAlertActive() && (acknowledgeLevel.equalsIgnoreCase("Danger") || acknowledgeLevel.equalsIgnoreCase("Triggered"))) {
            AlertsDaoWrapper alertsDaoWrapper = AlertsDaoWrapper.changeAlertDangerAcknowledge(alert.getName(), acknowledgeChange_Boolean);
            didSetAlertAcknowledgement = (alertsDaoWrapper.getLastAlterRecordStatus() == AlertsDaoWrapper.STATUS_CODE_SUCCESS);
        }    
        
        if (didSetAlertAcknowledgement) Alerts.sendPagerDutyAcknowledgeRequest(alert.getId());
        
        return didSetAlertAcknowledgement;
    }
    
    private String getAcknowledgeButtonHtml(Alert alert, String level, boolean excludeNavbar) {
        
        if ((level == null) || (alert == null) || (alert.getName() == null)) {
            return "";
        }
        
        StringBuilder htmlBodyBuilder = new StringBuilder();
        
        if (level.equalsIgnoreCase("Triggered")) {
            if (                // caution & danger both acknowledged
                ((alert.isCautionAlertActive() && (alert.isCautionAlertAcknowledged() != null) && alert.isCautionAlertAcknowledged()) &&
                (alert.isDangerAlertActive() && (alert.isDangerAlertAcknowledged() != null) && alert.isDangerAlertAcknowledged())) 
                ||              // danger acknowledged, caution not active (therefore not acknowledged)
                (!alert.isCautionAlertActive() && (alert.isDangerAlertActive() && (alert.isDangerAlertAcknowledged() != null) && alert.isDangerAlertAcknowledged()))
                ||              // caution acknowledged, danger not active (therefore not acknowledged)
                (!alert.isDangerAlertActive() && (alert.isCautionAlertActive() && (alert.isCautionAlertAcknowledged() != null) && alert.isCautionAlertAcknowledged()))
               )              
            {
                htmlBodyBuilder.append("<a href=\"AlertAssociations?AcknowledgeLevel=Triggered&amp;AcknowledgeChange=False&amp;Level=Triggered&amp;ExcludeNavbar=").append(excludeNavbar).append("&amp;Name=").
                        append(StatsAggHtmlFramework.urlEncode(alert.getName())).append("\" class=\"btn btn-primary statsagg_page_content_font\">Unacknowledge Triggered Alert</a>\n");
            }
            else if ((alert.isCautionAlertActive() && ((alert.isCautionAlertAcknowledged() == null) || ((alert.isCautionAlertAcknowledged() != null) && !alert.isCautionAlertAcknowledged()))) || 
                    (alert.isDangerAlertActive() && ((alert.isDangerAlertAcknowledged() == null) || ((alert.isDangerAlertAcknowledged() != null) && !alert.isDangerAlertAcknowledged())))) {
                htmlBodyBuilder.append("<a href=\"AlertAssociations?AcknowledgeLevel=Triggered&amp;AcknowledgeChange=True&amp;Level=Triggered&amp;ExcludeNavbar=").append(excludeNavbar).append("&amp;Name=").
                        append(StatsAggHtmlFramework.urlEncode(alert.getName())).append("\" class=\"btn btn-primary statsagg_page_content_font\">Acknowledge Triggered Alert</a>\n");
            }
        }
        else if (level.equalsIgnoreCase("Caution")) {
            if ((alert.isCautionAlertActive() != null) && alert.isCautionAlertActive()) {
                if ((alert.isCautionAlertAcknowledged() == null) || ((alert.isCautionAlertAcknowledged() != null) && !alert.isCautionAlertAcknowledged())) {
                    htmlBodyBuilder.append("<a href=\"AlertAssociations?AcknowledgeLevel=Caution&amp;AcknowledgeChange=True&amp;Level=Caution&amp;ExcludeNavbar=").append(excludeNavbar).append("&amp;Name=").
                            append(StatsAggHtmlFramework.urlEncode(alert.getName())).append("\" class=\"btn btn-primary statsagg_page_content_font\">Acknowledge Caution Alert</a>\n");
                }
                else if (((alert.isCautionAlertAcknowledged() != null) && alert.isCautionAlertAcknowledged())) {
                    htmlBodyBuilder.append("<a href=\"AlertAssociations?AcknowledgeLevel=Caution&amp;AcknowledgeChange=False&amp;Level=Caution&amp;ExcludeNavbar=").append(excludeNavbar).append("&amp;Name=").
                            append(StatsAggHtmlFramework.urlEncode(alert.getName())).append("\" class=\"btn btn-primary statsagg_page_content_font\">Unacknowledge Caution Alert</a>\n");
                }
            }
        }
        else if (level.equalsIgnoreCase("Danger")) {
            if ((alert.isDangerAlertActive() != null) && alert.isDangerAlertActive()) {
                if ((alert.isDangerAlertAcknowledged() == null) || ((alert.isDangerAlertAcknowledged() != null) && !alert.isDangerAlertAcknowledged())) {
                    htmlBodyBuilder.append("<a href=\"AlertAssociations?AcknowledgeLevel=Danger&amp;AcknowledgeChange=True&amp;Level=Danger&amp;ExcludeNavbar=").append(excludeNavbar).append("&amp;Name=").
                            append(StatsAggHtmlFramework.urlEncode(alert.getName())).append("\" class=\"btn btn-primary statsagg_page_content_font\">Acknowledge Danger Alert</a>\n");
                }
                else if (((alert.isDangerAlertAcknowledged() != null) && alert.isDangerAlertAcknowledged())) {
                    htmlBodyBuilder.append("<a href=\"AlertAssociations?AcknowledgeLevel=Danger&amp;AcknowledgeChange=False&amp;Level=Danger&amp;ExcludeNavbar=").append(excludeNavbar).append("&amp;Name=").
                            append(StatsAggHtmlFramework.urlEncode(alert.getName())).append("\" class=\"btn btn-primary statsagg_page_content_font\">Unacknowledge Danger Alert</a>\n");
                }
            }
        }
        
        return htmlBodyBuilder.toString();
    }

    private String getClearAllButtonHtml(Alert alert, String level, boolean excludeNavbar) {
        
        if ((level == null) || (alert == null) || (alert.getName() == null) || (alert.getAlertType() != Alert.TYPE_AVAILABILITY)) {
            return "";
        }
        
        if (level.equalsIgnoreCase("Triggered") && !(alert.isCautionAlertActive() || alert.isDangerAlertActive())) return "";
        else if (level.equalsIgnoreCase("Caution") && !alert.isCautionAlertActive()) return "";
        else if (level.equalsIgnoreCase("Danger") && !alert.isDangerAlertActive()) return "";
        
        StringBuilder htmlBodyBuilder = new StringBuilder();
        
        htmlBodyBuilder.append("<form style=\"display: inline;\" action=\"AlertAssociations\" method=\"POST\" id=\"AlertAssociations_ClearAll\" name=\"AlertAssociations_ClearAll\"> ");
        htmlBodyBuilder.append("<input type=\"hidden\" name=\"Name\" value=\"").append(StatsAggHtmlFramework.htmlEncode(alert.getName(), true)).append("\">");
        htmlBodyBuilder.append("<input type=\"hidden\" name=\"Level\" value=\"").append(level).append("\">");
        htmlBodyBuilder.append("<input type=\"hidden\" name=\"ExcludeNavbar\" value=\"").append(excludeNavbar).append("\">");
        htmlBodyBuilder.append("<input type=\"hidden\" name=\"ClearAll\" value=\"").append("True").append("\">");         
        htmlBodyBuilder.append("<button type=\"submit\" onclick=\"return confirmAction('AlertAssociations_ClearAll', 'Are you sure you want to remove these metrics? This will completely erase the triggered metrics from StatsAgg. Other alerts may be affected.')\" class=\"btn btn-primary statsagg_page_content_font\">Clear All</button>");
        htmlBodyBuilder.append("</form>");
        
        return htmlBodyBuilder.toString();
    }
    
    private String getTriggeredAlertAssociations(String alertName, String level, boolean excludeNavbar) {
        
        if ((alertName == null) || (level == null)) {
            return "<b>No alert specified</b>";
        }
        
        Alert alert = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, alertName);
        
        if (alert == null) {
            return "<b>Alert not found</b>";
        }
        else {
            StringBuilder outputString = new StringBuilder();

            outputString.append("<b>Alert Name</b> = ").append(StatsAggHtmlFramework.htmlEncode(alert.getName())).append("<br>");
            
            outputString.append("<b>Caution Acknowledged</b> = ");
            if (alert.isCautionAlertAcknowledged() == null) outputString.append("N/A");
            else if (alert.isCautionAlertAcknowledged()) outputString.append("Yes");   
            else if (!alert.isCautionAlertAcknowledged()) outputString.append("No");    
            outputString.append("<br>");
            
            outputString.append("<b>Caution First Triggered At</b> = ");
            if (alert.getCautionFirstActiveAt() == null) outputString.append("N/A");
            else outputString.append(DateAndTime.getFormattedDateAndTime(alert.getCautionFirstActiveAt(), "yyyy-MM-dd, h:mm:ss a"));   
            outputString.append("<br>");
            
            outputString.append("<b>Danger Acknowledged</b> = ");
            if (alert.isDangerAlertAcknowledged() == null) outputString.append("N/A");
            else if (alert.isDangerAlertAcknowledged()) outputString.append("Yes");   
            else if (!alert.isDangerAlertAcknowledged()) outputString.append("No");    
            outputString.append("<br>");
            
            outputString.append("<b>Danger First Triggered At</b> = ");
            if (alert.getDangerFirstActiveAt() == null) outputString.append("N/A");
            else outputString.append(DateAndTime.getFormattedDateAndTime(alert.getDangerFirstActiveAt(), "yyyy-MM-dd, h:mm:ss a"));   
            outputString.append("<br>");
            
            outputString.append("<hr>");

            String cautionBody = getCautionAlertAssociations_Body(alert, level, excludeNavbar);
            outputString.append(cautionBody);
            
            outputString.append("<hr>");
            
            String dangerBody = getDangerAlertAssociations_Body(alert, level, excludeNavbar);
            outputString.append(dangerBody);
            
            return outputString.toString();
        }
    }
    
    private String getCautionAlertAssociations(String alertName, String level, boolean excludeNavbar) {
        
        if ((alertName == null) || (level == null)) {
            return "<b>No alert specified</b>";
        }
        
        Alert alert = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, alertName);
        
        if (alert == null) {
            return "<b>Alert not found</b>";
        }
        else {
            StringBuilder outputString = new StringBuilder();

            outputString.append("<b>Alert Name</b> = ").append(StatsAggHtmlFramework.htmlEncode(alert.getName())).append("<br>");
            
            outputString.append("<b>Caution Acknowledged</b> = ");
            if (alert.isCautionAlertAcknowledged() == null) outputString.append("N/A");
            else if (alert.isCautionAlertAcknowledged()) outputString.append("Yes");   
            else if (!alert.isCautionAlertAcknowledged()) outputString.append("No");    
            outputString.append("<br>");
            
            outputString.append("<b>Caution First Triggered At</b> = ");
            if (alert.getCautionFirstActiveAt() == null) outputString.append("N/A");
            else outputString.append(DateAndTime.getFormattedDateAndTime(alert.getCautionFirstActiveAt(), "yyyy-MM-dd, h:mm:ss a"));   
            outputString.append("<br>");
            
            String body = getCautionAlertAssociations_Body(alert, level, excludeNavbar);
            outputString.append(body);
            
            return outputString.toString();
        }
    }
    
    private String getCautionAlertAssociations_Body(Alert alert, String level, boolean excludeNavbar) {
        
        if ((alert == null) || (alert.getId() == null) || (level == null)) {
            return "";
        }
        
        StringBuilder outputString = new StringBuilder();
        
        // gets all active caution metric-keys for this alert
        HashSet<String> activeCautionAlertMetricKeys = getActiveAlertMetricKeys(alert, GlobalVariables.activeCautionAlertMetricKeysByAlertId);
        
        // limit the number of metrics displayed
        int i = 1;
        List<String> activeCautionAlertMetricKeysSorted_Reduced = new ArrayList<>();
        for (String activeCautionAlertMetricKey : activeCautionAlertMetricKeys) {
            activeCautionAlertMetricKeysSorted_Reduced.add(activeCautionAlertMetricKey);
            i++;
            if (i > MAX_METRIC_KEYS_TO_DISPLAY) break;
        }
        
        // sort the metrics that are going to be displayed alphabetically
        Collections.sort(activeCautionAlertMetricKeysSorted_Reduced);

        // get a local copy of the metric values that are associated triggered metric-keys
        Map<String,BigDecimal> activeCautionAlertMetricValuesLocal;
        synchronized(GlobalVariables.activeCautionAlertMetricValues) {
            activeCautionAlertMetricValuesLocal = new HashMap<>(GlobalVariables.activeCautionAlertMetricValues);
        }

        if (activeCautionAlertMetricKeys == null) {
            outputString.append("<b>Total Triggered Caution Metrics</b> = ").append("0");
        }
        else {
            outputString.append("<b>Total Triggered Caution Metrics</b> = ").append(activeCautionAlertMetricKeys.size()).append("<br><br>");

            if (activeCautionAlertMetricKeys.size() > 0) {
                outputString.append("<b>Triggered Metrics...</b>").append("<br>");

                int associationOutputCounter = 0;
                outputString.append("<ul>");

                for (String activeCautionAlertMetricKey : activeCautionAlertMetricKeysSorted_Reduced) {
                    String metricValueString = null;

                    BigDecimal alertMetricValue = activeCautionAlertMetricValuesLocal.get(activeCautionAlertMetricKey + "-" + alert.getId());
                    if (alertMetricValue != null) metricValueString = Alert.getMetricValueString_WithLabel(Alert.CAUTION, alert, alertMetricValue);

                    String forgetMetric = "";

                    if ((alert.getAlertType() != null) && (alert.getAlertType() == Alert.TYPE_AVAILABILITY)) {
                        List<KeyValue<String,String>> keysAndValues = new ArrayList<>();
                        keysAndValues.add(new KeyValue("ForgetMetric", StatsAggHtmlFramework.htmlEncode(activeCautionAlertMetricKey, true)));
                        keysAndValues.add(new KeyValue("Name", StatsAggHtmlFramework.htmlEncode(alert.getName(), true)));
                        keysAndValues.add(new KeyValue("Level", level));
                        keysAndValues.add(new KeyValue("ExcludeNavbar", Boolean.toString(excludeNavbar)));
                        forgetMetric = StatsAggHtmlFramework.buildJavaScriptPostLink("ForgetMetric_" + (activeCautionAlertMetricKey + "Caution"), "AlertAssociations", 
                                "<i class=\"fa fa-times\"></i>", keysAndValues, true, 
                                "Are you sure you want to remove this metric? " +
                                "This will completely erase the metric from StatsAgg. Other alerts may be affected.");
                    }

                    outputString.append("<li>");
                    outputString.append("<a class=\"iframe cboxElement\" href=\"MetricRecentValues?ExcludeNavbar=").append(excludeNavbar).append("&amp;MetricKey=").append(StatsAggHtmlFramework.urlEncode(activeCautionAlertMetricKey)).append("\">");
                    outputString.append(StatsAggHtmlFramework.htmlEncode(activeCautionAlertMetricKey)).append("</a>");
                    outputString.append("&nbsp;=&nbsp;").append(metricValueString);
                    if (!forgetMetric.isEmpty()) outputString.append("&nbsp;&nbsp;").append(forgetMetric);
                    outputString.append("</li>");

                    associationOutputCounter++;
                }

                int numAssociationsNotOutputted = activeCautionAlertMetricKeys.size() - associationOutputCounter;
                if (numAssociationsNotOutputted > 0) outputString.append("<li>").append(numAssociationsNotOutputted).append(" more...").append("</li>");

                outputString.append("</ul>");
            }
        }
        
        return outputString.toString();
    }

    private String getDangerAlertAssociations(String alertName, String level, boolean excludeNavbar) {
        
        if ((alertName == null) || (level == null)) {
            return "<b>No alert specified</b>";
        }
        
        Alert alert = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, alertName);
        
        if (alert == null) {
            return "<b>Alert not found</b>";
        }
        else {
            StringBuilder outputString = new StringBuilder();
            outputString.append("<b>Name</b> = ").append(StatsAggHtmlFramework.htmlEncode(alert.getName())).append("<br>");
            
            outputString.append("<b>Danger Acknowledged</b> = ");
            if (alert.isDangerAlertAcknowledged() == null) outputString.append("N/A");
            else if (alert.isDangerAlertAcknowledged()) outputString.append("Yes");   
            else if (!alert.isDangerAlertAcknowledged()) outputString.append("No");    
            outputString.append("<br>");
            
            outputString.append("<b>Danger First Triggered At</b> = ");
            if (alert.getDangerFirstActiveAt() == null) outputString.append("N/A");
            else outputString.append(DateAndTime.getFormattedDateAndTime(alert.getDangerFirstActiveAt(), "yyyy-MM-dd, h:mm:ss a"));   
            outputString.append("<br>");
            
            String body = getDangerAlertAssociations_Body(alert, level, excludeNavbar);
            outputString.append(body);
            
            return outputString.toString();
        }
    }
    
    private String getDangerAlertAssociations_Body(Alert alert, String level, boolean excludeNavbar) {
        
        if ((alert == null) || (alert.getId() == null) || (level == null)) {
            return "";
        }
        
        StringBuilder outputString = new StringBuilder();
        
        // gets all active danger metric-keys for this alert
        HashSet<String> activeDangerAlertMetricKeys = getActiveAlertMetricKeys(alert, GlobalVariables.activeDangerAlertMetricKeysByAlertId);
        
        // limit the number of metrics displayed
        int i = 1;
        List<String> activeDangerAlertMetricKeysSorted_Reduced = new ArrayList<>();
        for (String activeDangerAlertMetricKey : activeDangerAlertMetricKeys) {
            activeDangerAlertMetricKeysSorted_Reduced.add(activeDangerAlertMetricKey);
            i++;
            if (i > MAX_METRIC_KEYS_TO_DISPLAY) break;
        }
        
        // sort the metrics that are going to be displayed alphabetically
        Collections.sort(activeDangerAlertMetricKeysSorted_Reduced);

        // get a local copy of the metric values that are associated triggered metric-keys
        Map<String,BigDecimal> activeDangerAlertMetricValuesLocal;
        synchronized(GlobalVariables.activeDangerAlertMetricValues) {
            activeDangerAlertMetricValuesLocal = new HashMap<>(GlobalVariables.activeDangerAlertMetricValues);
        }

        if (activeDangerAlertMetricKeys == null) {
            outputString.append("<b>Total Triggered Danger Metrics</b> = ").append("0");
        }
        else {
            outputString.append("<b>Total Triggered Danger Metrics</b> = ").append(activeDangerAlertMetricKeys.size()).append("<br><br>");

            if (activeDangerAlertMetricKeys.size() > 0) {
                outputString.append("<b>Triggered Metrics...</b>").append("<br>");

                int associationOutputCounter = 0;
                outputString.append("<ul>");

                for (String activeDangerAlertMetricKey : activeDangerAlertMetricKeysSorted_Reduced) {
                    String metricValueString = null;

                    BigDecimal alertMetricValue = activeDangerAlertMetricValuesLocal.get(activeDangerAlertMetricKey + "-" + alert.getId());
                    if (alertMetricValue != null) metricValueString = Alert.getMetricValueString_WithLabel(Alert.DANGER, alert, alertMetricValue);

                    String forgetMetric = "";

                    if ((alert.getAlertType() != null) && (alert.getAlertType() == Alert.TYPE_AVAILABILITY)) {
                        List<KeyValue<String,String>> keysAndValues = new ArrayList<>();
                        keysAndValues.add(new KeyValue("ForgetMetric", StatsAggHtmlFramework.htmlEncode(activeDangerAlertMetricKey, true)));
                        keysAndValues.add(new KeyValue("Name", StatsAggHtmlFramework.htmlEncode(alert.getName(), true)));
                        keysAndValues.add(new KeyValue("Level", level));
                        keysAndValues.add(new KeyValue("ExcludeNavbar", Boolean.toString(excludeNavbar)));
                        forgetMetric = StatsAggHtmlFramework.buildJavaScriptPostLink("ForgetMetric_" + (activeDangerAlertMetricKey + "Danger"), "AlertAssociations", 
                                "<i class=\"fa fa-times\"></i>", keysAndValues, true, 
                                "Are you sure you want to remove this metric? " +
                                "This will completely erase the metric from StatsAgg. Other alerts may be affected.");
                    }

                    outputString.append("<li>");
                    outputString.append("<a class=\"iframe cboxElement\" href=\"MetricRecentValues?ExcludeNavbar=").append(excludeNavbar).append("&amp;MetricKey=").append(StatsAggHtmlFramework.urlEncode(activeDangerAlertMetricKey)).append("\">");
                    outputString.append(StatsAggHtmlFramework.htmlEncode(activeDangerAlertMetricKey)).append("</a>");
                    outputString.append("&nbsp;=&nbsp;").append(metricValueString);
                    if (!forgetMetric.isEmpty()) outputString.append("&nbsp;&nbsp;").append(forgetMetric);
                    outputString.append("</li>");

                    associationOutputCounter++;
                }

                int numAssociationsNotOutputted = activeDangerAlertMetricKeys.size() - associationOutputCounter;
                if (numAssociationsNotOutputted > 0) outputString.append("<li>").append(numAssociationsNotOutputted).append(" more...").append("</li>");

                outputString.append("</ul>");
            }
        }
        
        return outputString.toString();
    }
    
    private static HashSet<String> getActiveAlertMetricKeys(Alert alert, ConcurrentHashMap<Integer,List<String>> alertMetricKeysByAlertId) {
        
        if ((alert == null) || (alertMetricKeysByAlertId == null)) {
            return new HashSet<>();
        }
        
        // make a local copy of the relevant 'active alerts' list
        List<String> activeAlertMetricKeys;
        HashSet<String> activeAlertMetricKeysLocal = new HashSet<>();
        synchronized(alertMetricKeysByAlertId) {
            activeAlertMetricKeys = alertMetricKeysByAlertId.get(alert.getId());
            if (activeAlertMetricKeys != null) activeAlertMetricKeysLocal = new HashSet<>(activeAlertMetricKeys);
        }
        
        // don't display metrics that have been 'forgotten', but haven't gone through the alert-routine yet
        List<String> keysToRemove = new ArrayList<>();
        for (String activeAlertMetricKey : activeAlertMetricKeysLocal) {
            if (!GlobalVariables.metricKeysLastSeenTimestamp.containsKey(activeAlertMetricKey)) keysToRemove.add(activeAlertMetricKey);
        }
        activeAlertMetricKeysLocal.removeAll(GlobalVariables.immediateCleanupMetrics.keySet());
        activeAlertMetricKeysLocal.removeAll(keysToRemove);
        
        return activeAlertMetricKeysLocal;
    }

}
