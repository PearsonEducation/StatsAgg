package com.pearson.statsagg.webui;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database.alerts.Alert;
import com.pearson.statsagg.database.alerts.AlertsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.DateAndTime;
import com.pearson.statsagg.utilities.KeyValue;
import com.pearson.statsagg.utilities.StackTrace;
import com.pearson.statsagg.utilities.StringUtilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "AlertAssociations", urlPatterns = {"/AlertAssociations"})
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
        
        response.setContentType("text/html");
        PrintWriter out = null;
    
        String name = request.getParameter("Name");
        String level = request.getParameter("Level");
        String acknowledgeLevel = request.getParameter("AcknowledgeLevel");
        String acknowledgeChange = request.getParameter("AcknowledgeChange");

        // if a user clicked on the 'acknowledge' button, change the acknowledgement status in the database
        acknowledgeAlert(name, acknowledgeChange, acknowledgeLevel);

        AlertsDao alertsDao = new AlertsDao();
        Alert alert = alertsDao.getAlertByName(name);  
        
        String alertAssociations = "";
        
        if (level != null) {
            if (level.equalsIgnoreCase("Triggered")) alertAssociations = getTriggeredAlertAssociations(name, level);
            else if (level.equalsIgnoreCase("Caution")) alertAssociations = getCautionAlertAssociations(name, level);
            else if (level.equalsIgnoreCase("Danger")) alertAssociations = getDangerAlertAssociations(name, level);
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
            
            htmlBodyBuilder.append(getAcknowledgeButtonHtml(alert, level, name));
            
            htmlBodyBuilder.append(
                "      </div>\n" + 
                "    </div>\n " +
                "    <div class=\"statsagg_force_word_wrap\">" +
                alertAssociations +
                "    </div>\n" +
                "  </div>\n" +
                "</div>\n");
            
            String htmlBody = statsAggHtmlFramework.createHtmlBody(htmlBodyBuilder.toString());
 
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
        
        String name = request.getParameter("Name");
        String level = request.getParameter("Level");
        String forgetMetric = request.getParameter("ForgetMetric");

        // if the user clicked on the X button to remove a metric, perform that action & reload the page
        if ((forgetMetric != null) && !forgetMetric.isEmpty()) {
            boolean forgetSuccess = forgetMetricAndReloadPage(forgetMetric, name, level, response);
            if (!forgetSuccess) processGetRequest(request, response);
        }
        else processGetRequest(request, response);
    }
        
    private boolean forgetMetricAndReloadPage(String metricToForget, String alertName, String level, HttpServletResponse response) {
        
        if ((metricToForget == null) || metricToForget.isEmpty() || (alertName == null) || (alertName.isEmpty()) || (level == null) || (level.isEmpty()) || (response == null)) {
            return false;
        }
        
        String trimmedMetricToForget = metricToForget.trim();
        GlobalVariables.immediateCleanupMetrics.put(trimmedMetricToForget, trimmedMetricToForget);
        String cleanMetricKey = StringUtilities.removeNewlinesFromString(trimmedMetricToForget);
        logger.info("Action=AlertAssociations_ForgetMetric, " + "MetricKey=\"" + cleanMetricKey + "\"");
        
        if (GlobalVariables.cleanupInvokerThread != null) GlobalVariables.cleanupInvokerThread.runCleanupThread();

        StatsAggHtmlFramework.redirectAndGet(response, 303, "AlertAssociations?" + "Name=" + StatsAggHtmlFramework.urlEncode(alertName) + "&" + "Level=" + StatsAggHtmlFramework.urlEncode(level));
        
        return true;
    }
    
    private void acknowledgeAlert(String alertName, String acknowledgeChange, String acknowledgeLevel) {
        
        if ((alertName == null) || (acknowledgeChange == null) || (acknowledgeLevel == null)) {
            return;
        }
        
        AlertsDao alertsDao = new AlertsDao();
        Alert alert = alertsDao.getAlertByName(alertName);  
        if (alert == null) return;
        
        Boolean acknowledgeChange_Boolean = null;
        try {
            acknowledgeChange_Boolean = Boolean.parseBoolean(acknowledgeChange);
        }
        catch (Exception e){}
        
        if ((alert.isCautionAlertActive() != null) && alert.isCautionAlertActive() && (acknowledgeLevel.equalsIgnoreCase("Caution") || acknowledgeLevel.equalsIgnoreCase("Triggered"))) {
            AlertsLogic.changeAlertCautionAcknowledge(alertName, acknowledgeChange_Boolean);
        }     

        if ((alert.isDangerAlertActive() != null) && alert.isDangerAlertActive() && (acknowledgeLevel.equalsIgnoreCase("Danger") || acknowledgeLevel.equalsIgnoreCase("Triggered"))) {
            AlertsLogic.changeAlertDangerAcknowledge(alertName, acknowledgeChange_Boolean);
        }    
    }
    
    private String getAcknowledgeButtonHtml(Alert alert, String level, String name) {
        
        if ((level == null) || (alert == null) || (name == null)) {
            return "";
        }
        
        StringBuilder htmlBodyBuilder = new StringBuilder();
        
        if (level.equalsIgnoreCase("Triggered")) {
            if (                // caution & danger both acknowledged
                ((alert.isCautionAlertActive() && (alert.isCautionAcknowledged() != null) && alert.isCautionAcknowledged()) &&
                (alert.isDangerAlertActive() && (alert.isDangerAcknowledged() != null) && alert.isDangerAcknowledged())) 
                ||              // danger acknowledged, caution not active (therefore not acknowledged)
                (!alert.isCautionAlertActive() && (alert.isDangerAlertActive() && (alert.isDangerAcknowledged() != null) && alert.isDangerAcknowledged()))
                ||              // caution acknowledged, danger not active (therefore not acknowledged)
                (!alert.isDangerAlertActive() && (alert.isCautionAlertActive() && (alert.isCautionAcknowledged() != null) && alert.isCautionAcknowledged()))
               )              
            {
                htmlBodyBuilder.append("<a href=\"AlertAssociations?AcknowledgeLevel=Triggered&amp;AcknowledgeChange=False&amp;Level=Triggered&amp;Name=").
                        append(StatsAggHtmlFramework.urlEncode(name)).append("\" class=\"btn btn-primary statsagg_page_content_font\">Unacknowledge Triggered Alert</a>\n");
            }
            else if ((alert.isCautionAlertActive() && ((alert.isCautionAcknowledged() == null) || ((alert.isCautionAcknowledged() != null) && !alert.isCautionAcknowledged()))) || 
                    (alert.isDangerAlertActive() && ((alert.isDangerAcknowledged() == null) || ((alert.isDangerAcknowledged() != null) && !alert.isDangerAcknowledged())))) {
                htmlBodyBuilder.append("<a href=\"AlertAssociations?AcknowledgeLevel=Triggered&amp;AcknowledgeChange=True&amp;Level=Triggered&amp;Name=").
                        append(StatsAggHtmlFramework.urlEncode(name)).append("\" class=\"btn btn-primary statsagg_page_content_font\">Acknowledge Triggered Alert</a>\n");
            }
        }
        else if (level.equalsIgnoreCase("Caution")) {
            if ((alert.isCautionAlertActive() != null) && alert.isCautionAlertActive()) {
                if ((alert.isCautionAcknowledged() == null) || ((alert.isCautionAcknowledged() != null) && !alert.isCautionAcknowledged())) {
                    htmlBodyBuilder.append("<a href=\"AlertAssociations?AcknowledgeLevel=Caution&amp;AcknowledgeChange=True&amp;Level=Caution&amp;Name=").
                            append(StatsAggHtmlFramework.urlEncode(name)).append("\" class=\"btn btn-primary statsagg_page_content_font\">Acknowledge Caution Alert</a>\n");
                }
                else if (((alert.isCautionAcknowledged() != null) && alert.isCautionAcknowledged())) {
                    htmlBodyBuilder.append("<a href=\"AlertAssociations?AcknowledgeLevel=Caution&amp;AcknowledgeChange=False&amp;Level=Caution&amp;Name=").
                            append(StatsAggHtmlFramework.urlEncode(name)).append("\" class=\"btn btn-primary statsagg_page_content_font\">Unacknowledge Caution Alert</a>\n");
                }
            }
        }
        else if (level.equalsIgnoreCase("Danger")) {
            if ((alert.isDangerAlertActive() != null) && alert.isDangerAlertActive()) {
                if ((alert.isDangerAcknowledged() == null) || ((alert.isDangerAcknowledged() != null) && !alert.isDangerAcknowledged())) {
                    htmlBodyBuilder.append("<a href=\"AlertAssociations?AcknowledgeLevel=Danger&amp;AcknowledgeChange=True&amp;Level=Danger&amp;Name=").
                            append(StatsAggHtmlFramework.urlEncode(name)).append("\" class=\"btn btn-primary statsagg_page_content_font\">Acknowledge Danger Alert</a>\n");
                }
                else if (((alert.isDangerAcknowledged() != null) && alert.isDangerAcknowledged())) {
                    htmlBodyBuilder.append("<a href=\"AlertAssociations?AcknowledgeLevel=Danger&amp;AcknowledgeChange=False&amp;Level=Danger&amp;Name=").
                            append(StatsAggHtmlFramework.urlEncode(name)).append("\" class=\"btn btn-primary statsagg_page_content_font\">Unacknowledge Danger Alert</a>\n");
                }
            }
        }
        
        return htmlBodyBuilder.toString();
    }

    private String getTriggeredAlertAssociations(String alertName, String level) {
        
        if ((alertName == null) || (level == null)) {
            return "<b>No alert specified</b>";
        }
        
        AlertsDao altersDao = new AlertsDao();
        Alert alert = altersDao.getAlertByName(alertName);
        
        if (alert == null) {
            return "<b>Alert not found</b>";
        }
        else {
            StringBuilder outputString = new StringBuilder();

            outputString.append("<b>Alert Name</b> = ").append(StatsAggHtmlFramework.htmlEncode(alert.getName())).append("<br>");
            
            outputString.append("<b>Caution Acknowledged</b> = ");
            if (alert.isCautionAcknowledged() == null) outputString.append("N/A");
            else if (alert.isCautionAcknowledged()) outputString.append("Yes");   
            else if (!alert.isCautionAcknowledged()) outputString.append("No");    
            outputString.append("<br>");
            
            outputString.append("<b>Caution First Triggered At</b> = ");
            if (alert.getCautionFirstActiveAt() == null) outputString.append("N/A");
            else outputString.append(DateAndTime.getFormattedDateAndTime(alert.getCautionFirstActiveAt(), "yyyy-MM-dd, h:mm:ss a"));   
            outputString.append("<br>");
            
            outputString.append("<b>Danger Acknowledged</b> = ");
            if (alert.isDangerAcknowledged() == null) outputString.append("N/A");
            else if (alert.isDangerAcknowledged()) outputString.append("Yes");   
            else if (!alert.isDangerAcknowledged()) outputString.append("No");    
            outputString.append("<br>");
            
            outputString.append("<b>Danger First Triggered At</b> = ");
            if (alert.getDangerFirstActiveAt() == null) outputString.append("N/A");
            else outputString.append(DateAndTime.getFormattedDateAndTime(alert.getDangerFirstActiveAt(), "yyyy-MM-dd, h:mm:ss a"));   
            outputString.append("<br>");
            
            outputString.append("<hr>");

            String cautionBody = getCautionAlertAssociations_Body(alert, level);
            outputString.append(cautionBody);
            
            outputString.append("<hr>");
            
            String dangerBody = getDangerAlertAssociations_Body(alert, level);
            outputString.append(dangerBody);
            
            return outputString.toString();
        }
    }
    
    private String getCautionAlertAssociations(String alertName, String level) {
        
        if ((alertName == null) || (level == null)) {
            return "<b>No alert specified</b>";
        }
        
        AlertsDao altersDao = new AlertsDao();
        Alert alert = altersDao.getAlertByName(alertName);
        
        if (alert == null) {
            return "<b>Alert not found</b>";
        }
        else {
            StringBuilder outputString = new StringBuilder();

            outputString.append("<b>Alert Name</b> = ").append(StatsAggHtmlFramework.htmlEncode(alert.getName())).append("<br>");
            
            outputString.append("<b>Caution Acknowledged</b> = ");
            if (alert.isCautionAcknowledged() == null) outputString.append("N/A");
            else if (alert.isCautionAcknowledged()) outputString.append("Yes");   
            else if (!alert.isCautionAcknowledged()) outputString.append("No");    
            outputString.append("<br>");
            
            outputString.append("<b>Caution First Triggered At</b> = ");
            if (alert.getCautionFirstActiveAt() == null) outputString.append("N/A");
            else outputString.append(DateAndTime.getFormattedDateAndTime(alert.getCautionFirstActiveAt(), "yyyy-MM-dd, h:mm:ss a"));   
            outputString.append("<br>");
            
            String body = getCautionAlertAssociations_Body(alert, level);
            outputString.append(body);
            
            return outputString.toString();
        }
    }
    
    private String getCautionAlertAssociations_Body(Alert alert, String level) {
        
        if ((alert == null) || (alert.getId() == null) || (level == null)) {
            return "";
        }
        
        StringBuilder outputString = new StringBuilder();
        
        // make a local copy of the relevant 'active alerts' list
        List<String> activeCautionAlertMetricKeys;
        HashSet<String> activeCautionAlertMetricKeysLocal = new HashSet<>();
        synchronized(GlobalVariables.activeCautionAlertMetricKeysByAlertId) {
            activeCautionAlertMetricKeys = GlobalVariables.activeCautionAlertMetricKeysByAlertId.get(alert.getId());
            if (activeCautionAlertMetricKeys != null) activeCautionAlertMetricKeysLocal = new HashSet<>(activeCautionAlertMetricKeys);
        }
        
        // don't display metrics that have been 'forgotten', but haven't gone through the alert-routine yet
        List<String> keysToRemove = new ArrayList<>();
        for (String activeCautionAlertMetricKey : activeCautionAlertMetricKeysLocal) {
            if (!GlobalVariables.metricKeysLastSeenTimestamp_UpdateOnResend.containsKey(activeCautionAlertMetricKey)) keysToRemove.add(activeCautionAlertMetricKey);
        }
        activeCautionAlertMetricKeysLocal.removeAll(GlobalVariables.immediateCleanupMetrics.keySet());
        activeCautionAlertMetricKeysLocal.removeAll(keysToRemove);
        
        // limit the number of metrics displayed
        int i = 1;
        List<String> activeCautionAlertMetricKeysSorted_Reduced = new ArrayList<>();
        for (String activeCautionAlertMetricKey : activeCautionAlertMetricKeysLocal) {
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
            outputString.append("<b>Total Triggered Caution Metrics</b> = ").append(activeCautionAlertMetricKeysLocal.size()).append("<br><br>");

            if (activeCautionAlertMetricKeysLocal.size() > 0) {
                outputString.append("<b>Triggered Metrics...</b>").append("<br>");

                int associationOutputCounter = 0;
                outputString.append("<ul>");

                for (String activeCautionAlertMetricKey : activeCautionAlertMetricKeysSorted_Reduced) {
                    String metricValueString = null;

                    BigDecimal alertMetricValue = activeCautionAlertMetricValuesLocal.get(activeCautionAlertMetricKey + "-" + alert.getId());
                    if (alertMetricValue != null) metricValueString = Alert.getCautionMetricValueString_WithLabel(alert, alertMetricValue);

                    String forgetMetric = "";

                    if ((alert.getAlertType() != null) && (alert.getAlertType() == Alert.TYPE_AVAILABILITY)) {
                        List<KeyValue> keysAndValues = new ArrayList<>();
                        keysAndValues.add(new KeyValue("ForgetMetric", Encode.forHtmlAttribute(activeCautionAlertMetricKey)));
                        keysAndValues.add(new KeyValue("Name", Encode.forHtmlAttribute(alert.getName())));
                        keysAndValues.add(new KeyValue("Level", level));
                        forgetMetric = StatsAggHtmlFramework.buildJavaScriptPostLink("ForgetMetric_" + activeCautionAlertMetricKey, "AlertAssociations", 
                                "<i class=\"fa fa-times\"></i>", keysAndValues, true, 
                                "Are you sure you want to remove this metric? " +
                                "This will completely erase the metric from StatsAgg. Other alerts may be affected.");
                    }

                    outputString.append("<li>");
                    outputString.append("<a href=\"MetricRecentValues?MetricKey=").append(StatsAggHtmlFramework.urlEncode(activeCautionAlertMetricKey)).append("\">");
                    outputString.append(StatsAggHtmlFramework.htmlEncode(activeCautionAlertMetricKey)).append("</a>");
                    outputString.append("&nbsp;=&nbsp;").append(metricValueString);
                    if (!forgetMetric.isEmpty()) outputString.append("&nbsp;&nbsp;").append(forgetMetric);
                    outputString.append("</li>");

                    associationOutputCounter++;
                }

                int numAssociationsNotOutputted = activeCautionAlertMetricKeysLocal.size() - associationOutputCounter;
                if (numAssociationsNotOutputted > 0) {
                    outputString.append("<li>").append(numAssociationsNotOutputted).append(" more...").append("</li>");
                }

                outputString.append("</ul>");
            }
        }
        
        return outputString.toString();
    }

    private String getDangerAlertAssociations(String alertName, String level) {
        
        if ((alertName == null) || (level == null)) {
            return "<b>No alert specified</b>";
        }
        
        AlertsDao altersDao = new AlertsDao();
        Alert alert = altersDao.getAlertByName(alertName);
        
        if (alert == null) {
            return "<b>Alert not found</b>";
        }
        else {
            StringBuilder outputString = new StringBuilder();
            outputString.append("<b>Name</b> = ").append(StatsAggHtmlFramework.htmlEncode(alert.getName())).append("<br>");
            
            outputString.append("<b>Danger Acknowledged</b> = ");
            if (alert.isDangerAcknowledged() == null) outputString.append("N/A");
            else if (alert.isDangerAcknowledged()) outputString.append("Yes");   
            else if (!alert.isDangerAcknowledged()) outputString.append("No");    
            outputString.append("<br>");
            
            outputString.append("<b>Danger First Triggered At</b> = ");
            if (alert.getDangerFirstActiveAt() == null) outputString.append("N/A");
            else outputString.append(DateAndTime.getFormattedDateAndTime(alert.getDangerFirstActiveAt(), "yyyy-MM-dd, h:mm:ss a"));   
            outputString.append("<br>");
            
            String body = getDangerAlertAssociations_Body(alert, level);
            outputString.append(body);
            
            return outputString.toString();
        }
    }
    
    private String getDangerAlertAssociations_Body(Alert alert, String level) {
        
        if ((alert == null) || (alert.getId() == null) || (level == null)) {
            return "";
        }
        
        StringBuilder outputString = new StringBuilder();
        
        // make a local copy of the relevant 'active alerts' list
        List<String> activeDangerAlertMetricKeys;
        HashSet<String> activeDangerAlertMetricKeysLocal = new HashSet<>();
        synchronized(GlobalVariables.activeDangerAlertMetricKeysByAlertId) {
            activeDangerAlertMetricKeys = GlobalVariables.activeDangerAlertMetricKeysByAlertId.get(alert.getId());
            if (activeDangerAlertMetricKeys != null) activeDangerAlertMetricKeysLocal = new HashSet<>(activeDangerAlertMetricKeys);
        }
        
        // don't display metrics that have been 'forgotten', but haven't gone through the alert-routine yet
        List<String> keysToRemove = new ArrayList<>();
        for (String activeDangerAlertMetricKey : activeDangerAlertMetricKeysLocal) {
            if (!GlobalVariables.metricKeysLastSeenTimestamp_UpdateOnResend.containsKey(activeDangerAlertMetricKey)) keysToRemove.add(activeDangerAlertMetricKey);
        }
        activeDangerAlertMetricKeysLocal.removeAll(GlobalVariables.immediateCleanupMetrics.keySet());
        activeDangerAlertMetricKeysLocal.removeAll(keysToRemove);
        
        // limit the number of metrics displayed
        int i = 1;
        List<String> activeDangerAlertMetricKeysSorted_Reduced = new ArrayList<>();
        for (String activeDangerAlertMetricKey : activeDangerAlertMetricKeysLocal) {
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
            outputString.append("<b>Total Triggered Danger Metrics</b> = ").append(activeDangerAlertMetricKeysLocal.size()).append("<br><br>");

            if (activeDangerAlertMetricKeysLocal.size() > 0) {
                outputString.append("<b>Triggered Metrics...</b>").append("<br>");

                int associationOutputCounter = 0;
                outputString.append("<ul>");

                for (String activeDangerAlertMetricKey : activeDangerAlertMetricKeysSorted_Reduced) {
                    String metricValueString = null;

                    BigDecimal alertMetricValue = activeDangerAlertMetricValuesLocal.get(activeDangerAlertMetricKey + "-" + alert.getId());
                    if (alertMetricValue != null) metricValueString = Alert.getDangerMetricValueString_WithLabel(alert, alertMetricValue);

                    String forgetMetric = "";

                    if ((alert.getAlertType() != null) && (alert.getAlertType() == Alert.TYPE_AVAILABILITY)) {
                        List<KeyValue> keysAndValues = new ArrayList<>();
                        keysAndValues.add(new KeyValue("ForgetMetric", Encode.forHtmlAttribute(activeDangerAlertMetricKey)));
                        keysAndValues.add(new KeyValue("Name", Encode.forHtmlAttribute(alert.getName())));
                        keysAndValues.add(new KeyValue("Level", level));
                        forgetMetric = StatsAggHtmlFramework.buildJavaScriptPostLink("ForgetMetric_" + activeDangerAlertMetricKey, "AlertAssociations", 
                                "<i class=\"fa fa-times\"></i>", keysAndValues, true, 
                                "Are you sure you want to remove this metric? " +
                                "This will completely erase the metric from StatsAgg. Other alerts may be affected.");
                    }

                    outputString.append("<li>");
                    outputString.append("<a href=\"MetricRecentValues?MetricKey=").append(StatsAggHtmlFramework.urlEncode(activeDangerAlertMetricKey)).append("\">");
                    outputString.append(StatsAggHtmlFramework.htmlEncode(activeDangerAlertMetricKey)).append("</a>");
                    outputString.append("&nbsp;=&nbsp;").append(metricValueString);
                    if (!forgetMetric.isEmpty()) outputString.append("&nbsp;&nbsp;").append(forgetMetric);
                    outputString.append("</li>");

                    associationOutputCounter++;
                }

                int numAssociationsNotOutputted = activeDangerAlertMetricKeysLocal.size() - associationOutputCounter;
                if (numAssociationsNotOutputted > 0) {
                    outputString.append("<li>").append(numAssociationsNotOutputted).append(" more...").append("</li>");
                }

                outputString.append("</ul>");
            }
        }
        
        return outputString.toString();
    }
    
}
