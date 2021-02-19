package com.pearson.statsagg.web_ui;

import com.google.gson.JsonObject;
import com.pearson.statsagg.configuration.ApplicationConfiguration;
import com.pearson.statsagg.database_objects.alert_templates.AlertTemplate;
import com.pearson.statsagg.database_objects.alert_templates.AlertTemplatesDao;
import com.pearson.statsagg.database_objects.alerts.AlertsDaoWrapper;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.database_objects.suspensions.Suspension;
import com.pearson.statsagg.database_objects.suspensions.SuspensionsDao;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroup;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupsDao;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroupsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.threads.alert_related.NotificationThread;
import com.pearson.statsagg.utilities.core_utils.KeyValue;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.Connection;
import java.util.HashMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class Alerts extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(Alerts.class.getName());
    
    public static final String PAGE_NAME = "Alerts";
    
        /**
     * Handles the HTTP
     * <code>GET</code> method.
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
            String html = buildAlertsHtml();
            
            Document htmlDocument = Jsoup.parse(html);
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
        
        try {
            String operation = Common.getSingleParameterAsString(request, "Operation");

            if ((operation != null) && operation.equals("Enable")) {
                Integer alertId = Integer.parseInt(Common.getSingleParameterAsString(request, "Id"));
                Boolean isEnabled = Boolean.parseBoolean(Common.getSingleParameterAsString(request, "Enabled"));
                changeAlertEnabled(alertId, isEnabled);
            }

            if ((operation != null) && operation.equals("Clone")) {
                Integer alertId = Integer.parseInt(request.getParameter("Id"));
                cloneAlert(alertId);
            }

            if ((operation != null) && operation.equals("Remove")) {
                Integer alertId = Integer.parseInt(Common.getSingleParameterAsString(request, "Id"));
                removeAlert(alertId);
            }

            if ((operation != null) && operation.equals("Acknowledge")) {
                String isAcknowledged_String = request.getParameter("IsAcknowledged");
                Integer alertId = Integer.parseInt(Common.getSingleParameterAsString(request, "Id"));

                try {
                    Boolean isAcknowledged_Boolean = Boolean.parseBoolean(isAcknowledged_String);
                    AlertsDaoWrapper.changeAlertAcknowledge(alertId, isAcknowledged_Boolean);
                    sendPagerDutyAcknowledgeRequest(alertId);
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        StatsAggHtmlFramework.redirectAndGet(response, 303, "Alerts");
    }

    public static String changeAlertEnabled(Integer alertId, Boolean isEnabled) {

        if ((alertId == null) || (isEnabled == null)) {
            return "Invalid input!";
        }
        
        boolean isSuccess = false;
        
        Connection connection = null;
        
        try {
            connection = DatabaseConnections.getConnection(); 
            
            Alert alert = AlertsDao.getAlert(connection, false, alertId);

            if (alert != null) {
                alert.setIsEnabled(isEnabled);

                if (!isEnabled) alert.disableAndNullifyAlertStatusFields();

                boolean isAlertCreatedByAlertTemplate = AlertsDao.isAlertCreatedByAlertTemplate(connection, true, alert, null);

                if (!isAlertCreatedByAlertTemplate) {
                    AlertsDaoWrapper alertsDaoWrapper = AlertsDaoWrapper.alterRecordInDatabase(alert, alert.getName());

                    if ((GlobalVariables.alertInvokerThread != null) && (AlertsDaoWrapper.STATUS_CODE_SUCCESS == alertsDaoWrapper.getLastAlterRecordStatus())) {
                        isSuccess = true;
                        GlobalVariables.alertInvokerThread.runAlertThread(false, true);
                    }
                }
                else logger.warn("Can't enabled/disable an alert that was created by an alert template. AlertName=\"" + alert.getName() + "\".");
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            DatabaseUtils.cleanup(connection);
        }
        
        if (isSuccess && isEnabled) return "Successfully enabled alert";
        if (isSuccess && !isEnabled) return "Successfully disabled alert";
        else return "Error -- could not alter alert";
    }
    
    protected static void cloneAlert(Integer alertId) {
        
        if (alertId == null) {
            return;
        }
        
        Connection connection = null;
        
        try {
            connection = DatabaseConnections.getConnection(); 
            
            Alert alert = AlertsDao.getAlert(connection, false, alertId);
            boolean isAlertCreatedByAlertTemplate = AlertsDao.isAlertCreatedByAlertTemplate(connection, false, alert, null);
            if (isAlertCreatedByAlertTemplate) logger.warn("Can't clone an alert that was created by an alert template. AlertName=\"" + alert.getName() + "\".");

            if ((alert != null) && (alert.getName() != null) && !isAlertCreatedByAlertTemplate) {
                Set<String> allAlertNames = AlertsDao.getAlertNames(connection, true);

                Alert clonedAlert = Alert.copy(alert);
                clonedAlert.setId(-1);
                String clonedAlertName = StatsAggHtmlFramework.createCloneName(alert.getName(), allAlertNames);

                clonedAlert.setName(clonedAlertName);
                clonedAlert.setIsEnabled(false);
                clonedAlert.setIsCautionAlertActive(false);
                clonedAlert.setCautionFirstActiveAt(null);
                clonedAlert.setIsCautionAlertAcknowledged(null);
                clonedAlert.setCautionAlertLastSentTimestamp(null);
                clonedAlert.setCautionActiveAlertsSet(null);
                clonedAlert.setIsDangerAlertActive(false);
                clonedAlert.setIsDangerAlertAcknowledged(null);
                clonedAlert.setDangerAlertLastSentTimestamp(null);
                clonedAlert.setDangerActiveAlertsSet(null);
                clonedAlert.setDangerFirstActiveAt(null);
                
                AlertsDaoWrapper alertsDaoWrapper = AlertsDaoWrapper.createRecordInDatabase(clonedAlert);
                
                if ((GlobalVariables.alertInvokerThread != null) && (AlertsDaoWrapper.STATUS_CODE_SUCCESS == alertsDaoWrapper.getLastAlterRecordStatus())) {
                    GlobalVariables.alertInvokerThread.runAlertThread(false, true);
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static String removeAlert(Integer alertId) {
        
        if (alertId == null) {
            return null;
        }
        
        String returnString = null;
        
        Connection connection = null;
        
        try {
            connection = DatabaseConnections.getConnection(); 
            
            Alert alert = AlertsDao.getAlert(connection, false, alertId);
            boolean isAlertCreatedByAlertTemplate = AlertsDao.isAlertCreatedByAlertTemplate(connection, true, alert, null);
            
            if (!isAlertCreatedByAlertTemplate) {
                AlertsDaoWrapper alertsDaoWrapper = AlertsDaoWrapper.deleteRecordInDatabase(alert);
                returnString = alertsDaoWrapper.getReturnString();
                
                if ((GlobalVariables.alertInvokerThread != null) && (AlertsDaoWrapper.STATUS_CODE_SUCCESS == alertsDaoWrapper.getLastDeleteRecordStatus())) {
                    GlobalVariables.alertInvokerThread.runAlertThread(false, true);
                }
            }
            else {
                String alertName = (alert == null) ? "Unknown" : alert.getName();
                returnString = "Can't remove an alert that was created by an alert template. AlertName=\"" + alertName + "\".";
                logger.warn(returnString);
            }
        }
        catch (Exception e) {
            returnString = "Error removing alert";
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            DatabaseUtils.cleanup(connection);
        }
        
        return returnString;
    }
    
    public static String buildAlertsHtml() {
        
        StringBuilder html = new StringBuilder();

        StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
        String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");

        StringBuilder htmlBodyStringBuilder = new StringBuilder();
        htmlBodyStringBuilder.append(
            "<div id=\"page-content-wrapper\">\n" +
            "<!-- Keep all page content within the page-content inset div! -->\n" +
            "<div class=\"page-content inset statsagg_page_content_font\">\n" +
            "  <div class=\"content-header\"> \n" +
            "    <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "    <div class=\"pull-right \">\n" +
            "     <a href=\"CreateAlert\" class=\"btn btn-primary statsagg_page_content_font\">Create New Alert <i class=\"fa fa-long-arrow-right\"></i></a> \n" +
            "    </div>\n" + 
            "  </div>\n" +   
            "  <table id=\"AlertsTable\" style=\"display:none\" class=\"table table-bordered table-hover compact\">\n" +
            "     <thead>\n" +
            "       <tr>\n" +
            "         <th>Alert Name</th>\n" +
            "         <th>Template</th>\n" +
            "         <th>Metric Group Association</th>\n" +
            "         <th>Metric Group Tags</th>\n" +
            "         <th>Caution Notification Group</th>\n" +
            "         <th>Danger Notification Group</th>\n" +
            "         <th>Enabled?</th>\n" +
            "         <th>Suspended?</th>\n" +
            "         <th>Triggered?</th>\n" +
            "         <th>Caution Triggered?</th>\n" +
            "         <th>Danger Triggered?</th>\n" +
            "         <th>Acknowledged?</th>\n" +      
            "         <th>Operations</th>\n" +
            "       </tr>\n" +
            "     </thead>\n" +
            "     <tbody>\n");


        Connection connection = null;
        
        try {
            connection = DatabaseConnections.getConnection();
            List<Alert> alerts = AlertsDao.getAlerts(connection, false);
            if (alerts == null) alerts = new ArrayList<>();
            Map<Integer, String> notificationGroupNames_ById = NotificationGroupsDao.getNotificationGroupNames_ById(connection, false);
            if (notificationGroupNames_ById == null) notificationGroupNames_ById = new HashMap<>();
            Map<Integer,List<Suspension>> suspensions_SuspendByAlertId_ByAlertId = SuspensionsDao.getSuspensions_SuspendByAlertId_ByAlertId(connection, false);
            if (suspensions_SuspendByAlertId_ByAlertId == null) suspensions_SuspendByAlertId_ByAlertId = new HashMap<>();
            
            for (Alert alert : alerts) {
                if (alert == null) continue;

                String rowAlertStatusContext = "";
                boolean isRowStatusInfo = false;

                synchronized(GlobalVariables.pendingCautionAlertsByAlertId) {
                    if (GlobalVariables.pendingCautionAlertsByAlertId.containsKey(alert.getId())) {
                        rowAlertStatusContext = "class=\"info\"";
                        isRowStatusInfo = true;
                    }
                }
                synchronized(GlobalVariables.pendingDangerAlertsByAlertId) {
                    if (GlobalVariables.pendingDangerAlertsByAlertId.containsKey(alert.getId())) {
                        rowAlertStatusContext = "class=\"info\"";
                        isRowStatusInfo = true;
                    }
                }
                synchronized(GlobalVariables.suspensionStatusByAlertId) {
                    if (GlobalVariables.suspensionStatusByAlertId.containsKey(alert.getId()) && GlobalVariables.suspensionStatusByAlertId.get(alert.getId())) {
                        rowAlertStatusContext = "class=\"info\"";
                        isRowStatusInfo = true;
                    }
                }

                if (!isRowStatusInfo && (alert.isCautionAlertActive() && !alert.isDangerAlertActive())) rowAlertStatusContext = "class=\"warning\"";
                else if (!isRowStatusInfo && alert.isDangerAlertActive()) rowAlertStatusContext = "class=\"danger\"";

                String alertDetails = "<a class=\"iframe cboxElement\" href=\"AlertDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(alert.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(alert.getName()) + "</a>";

                String alertTemplateNameAndLink;
                AlertTemplate alertTemplate = AlertTemplatesDao.getAlertTemplate(connection, false, alert.getAlertTemplateId());
                if ((alertTemplate == null) || (alertTemplate.getName() == null)) alertTemplateNameAndLink = "N/A";
                else alertTemplateNameAndLink = "<a class=\"iframe cboxElement\" href=\"AlertTemplateDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(alertTemplate.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(alertTemplate.getName()) + "</a>";

                String metricGroupNameAndLink;
                MetricGroup metricGroup = MetricGroupsDao.getMetricGroup(connection, false, alert.getMetricGroupId());
                if ((metricGroup == null) || (metricGroup.getName() == null)) metricGroupNameAndLink = "N/A";
                else metricGroupNameAndLink = "<a class=\"iframe cboxElement\" href=\"MetricGroupDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(metricGroup.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(metricGroup.getName()) + "</a>";

                StringBuilder metricGroupTagsCsv = new StringBuilder();
                if ((metricGroup != null) && (metricGroup.getTags() != null)) {
                    List<String> metricGroupTagsList = new ArrayList<>(metricGroup.getTags());
                    for (int i = 0; i < metricGroupTagsList.size(); i++) {
                        metricGroupTagsCsv = metricGroupTagsCsv.append("<u>").append(StatsAggHtmlFramework.htmlEncode(metricGroupTagsList.get(i).trim())).append("</u>");
                        if ((i + 1) < metricGroupTagsList.size()) metricGroupTagsCsv.append(" &nbsp;");
                    }
                }

                String cautionNotificationGroupNameAndLink;
                if ((alert.getCautionNotificationGroupId() == null) || ((alert.isCautionEnabled() != null) && !alert.isCautionEnabled()) || !notificationGroupNames_ById.containsKey(alert.getCautionNotificationGroupId())) {
                    cautionNotificationGroupNameAndLink = "N/A";
                }
                else {
                    cautionNotificationGroupNameAndLink = "<a class=\"iframe cboxElement\" href=\"NotificationGroupDetails?ExcludeNavbar=true&amp;Name=" + 
                        StatsAggHtmlFramework.urlEncode(notificationGroupNames_ById.get(alert.getCautionNotificationGroupId())) + "\">" + 
                        StatsAggHtmlFramework.htmlEncode(notificationGroupNames_ById.get(alert.getCautionNotificationGroupId())) + "</a>";
                }

                String dangerNotificationGroupNameAndLink;
                if ((alert.getDangerNotificationGroupId() == null) || ((alert.isDangerEnabled() != null) && !alert.isDangerEnabled()) || !notificationGroupNames_ById.containsKey(alert.getDangerNotificationGroupId())) {
                    dangerNotificationGroupNameAndLink = "N/A";
                }
                else {
                    dangerNotificationGroupNameAndLink = "<a class=\"iframe cboxElement\" href=\"NotificationGroupDetails?ExcludeNavbar=true&amp;Name=" + 
                        StatsAggHtmlFramework.urlEncode(notificationGroupNames_ById.get(alert.getDangerNotificationGroupId())) + "\">" + 
                        StatsAggHtmlFramework.htmlEncode(notificationGroupNames_ById.get(alert.getDangerNotificationGroupId())) + "</a>";
                }

                String isAlertEnabled = "No";
                if ((alert.isEnabled() != null) && alert.isEnabled()) isAlertEnabled = "Yes";

                String isSuspended = "No";
                synchronized(GlobalVariables.suspensionStatusByAlertId) {
                    if (GlobalVariables.suspensionStatusByAlertId.get(alert.getId()) != null) {
                        if (GlobalVariables.suspensionStatusByAlertId.get(alert.getId())) isSuspended = "Yes";
                    }
                }
                String isSuspendedLink = "<a class=\"iframe cboxElement\" href=\"Alert-SuspensionAssociations?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(alert.getName()) + "\">" + isSuspended + "</a>";

                String triggeredLink = "No", cautionLink = "No", dangerLink = "No";
                if ((alert.isCautionAlertActive() != null) && alert.isCautionAlertActive()) cautionLink = "<a class=\"iframe cboxElement\" href=\"AlertAssociations?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(alert.getName()) + "&Level=" + "Caution" + "\">Yes</a>";
                if ((alert.isDangerAlertActive() != null) && alert.isDangerAlertActive()) dangerLink = "<a class=\"iframe cboxElement\" href=\"AlertAssociations?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(alert.getName()) + "&Level=" + "Danger" + "\">Yes</a>";
                if (cautionLink.endsWith("Yes</a>") || dangerLink.endsWith("Yes</a>")) triggeredLink = "<a class=\"iframe cboxElement\" href=\"AlertAssociations?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(alert.getName()) + "&Level=" + "Triggered" + "\">Yes</a>";

                String enable; 
                if (alert.isEnabled()) {
                    List<KeyValue<String,String>> keysAndValues = new ArrayList<>();
                    keysAndValues.add(new KeyValue("Operation", "Enable"));
                    keysAndValues.add(new KeyValue("Id", alert.getId().toString()));
                    keysAndValues.add(new KeyValue("Enabled", "false"));
                    enable = StatsAggHtmlFramework.buildJavaScriptPostLink("Enable_" + alert.getName(), "Alerts", "disable", keysAndValues);
                }
                else {
                    List<KeyValue<String,String>> keysAndValues = new ArrayList<>();
                    keysAndValues.add(new KeyValue("Operation", "Enable"));
                    keysAndValues.add(new KeyValue("Id", alert.getId().toString()));
                    keysAndValues.add(new KeyValue("Enabled", "true"));
                    enable = StatsAggHtmlFramework.buildJavaScriptPostLink("Enable_" + alert.getName(), "Alerts", "enable", keysAndValues);
                }

                // decide whether the 'acknowledge' and/or 'unacknowledge' operations are presented 
                String acknowledge = "";
                if (                // caution & danger both acknowledged
                    ((alert.isCautionAlertActive() && (alert.isCautionAlertAcknowledged() != null) && alert.isCautionAlertAcknowledged()) &&
                    (alert.isDangerAlertActive() && (alert.isDangerAlertAcknowledged() != null) && alert.isDangerAlertAcknowledged())) 
                    ||              // danger acknowledged, caution not active (therefore not acknowledged)
                    (!alert.isCautionAlertActive() && (alert.isDangerAlertActive() && (alert.isDangerAlertAcknowledged() != null) && alert.isDangerAlertAcknowledged()))
                    ||              // caution acknowledged, danger not active (therefore not acknowledged)
                    (!alert.isDangerAlertActive() && (alert.isCautionAlertActive() && (alert.isCautionAlertAcknowledged() != null) && alert.isCautionAlertAcknowledged()))
                   )              
                {
                    List<KeyValue<String,String>> keysAndValues = new ArrayList<>();
                    keysAndValues.add(new KeyValue("Operation", "Acknowledge"));
                    keysAndValues.add(new KeyValue("Id", alert.getId().toString()));
                    keysAndValues.add(new KeyValue("IsAcknowledged", "false"));
                    acknowledge = StatsAggHtmlFramework.buildJavaScriptPostLink("Acknowledge_" + alert.getName(), "Alerts", "unacknowledge", keysAndValues);
                }
                else if ((alert.isCautionAlertActive() && ((alert.isCautionAlertAcknowledged() == null) || ((alert.isCautionAlertAcknowledged() != null) && !alert.isCautionAlertAcknowledged()))) || 
                        (alert.isDangerAlertActive() && ((alert.isDangerAlertAcknowledged() == null) || ((alert.isDangerAlertAcknowledged() != null) && !alert.isDangerAlertAcknowledged())))) {
                    List<KeyValue<String,String>> keysAndValues = new ArrayList<>();
                    keysAndValues.add(new KeyValue("Operation", "Acknowledge"));
                    keysAndValues.add(new KeyValue("Id", alert.getId().toString()));
                    keysAndValues.add(new KeyValue("IsAcknowledged", "true"));
                    acknowledge = StatsAggHtmlFramework.buildJavaScriptPostLink("Acknowledge_" + alert.getName(), "Alerts", "acknowledge", keysAndValues);
                }

                String isAcknowledged = getIsAcknowledgedTableValue(alert);

                String alter = "<a href=\"CreateAlert?Operation=Alter&amp;Name=" + StatsAggHtmlFramework.urlEncode(alert.getName()) + "\">alter</a>";

                List<KeyValue<String,String>> cloneKeysAndValues = new ArrayList<>();
                cloneKeysAndValues.add(new KeyValue("Operation", "Clone"));
                cloneKeysAndValues.add(new KeyValue("Id", alert.getId().toString()));
                String clone = StatsAggHtmlFramework.buildJavaScriptPostLink("Clone_" + alert.getName(), "Alerts", "clone", cloneKeysAndValues);

                List<KeyValue<String,String>> removeKeysAndValues = new ArrayList<>();
                removeKeysAndValues.add(new KeyValue("Operation", "Remove"));
                removeKeysAndValues.add(new KeyValue("Id", alert.getId().toString()));
                String remove = StatsAggHtmlFramework.buildJavaScriptPostLink("Remove_" + alert.getName(), "Alerts", "remove", 
                        removeKeysAndValues, true, "Are you sure you want to remove this alert?");

                htmlBodyStringBuilder
                        .append("<tr ").append(rowAlertStatusContext).append(">\n")
                        .append("<td class=\"statsagg_force_word_break\">").append(alertDetails).append("</td>\n")
                        .append("<td class=\"statsagg_force_word_break\">").append(alertTemplateNameAndLink).append("</td>\n")
                        .append("<td class=\"statsagg_force_word_break\">").append(metricGroupNameAndLink).append("</td>\n")
                        .append("<td class=\"statsagg_force_word_break\">").append(metricGroupTagsCsv.toString()).append("</td>\n")
                        .append("<td class=\"statsagg_force_word_break\">").append(cautionNotificationGroupNameAndLink).append("</td>\n")
                        .append("<td class=\"statsagg_force_word_break\">").append(dangerNotificationGroupNameAndLink).append("</td>\n")
                        .append("<td>").append(isAlertEnabled).append("</td>\n")
                        .append("<td>").append(isSuspendedLink).append("</td>\n")
                        .append("<td>").append(triggeredLink).append("</td>\n")
                        .append("<td>").append(cautionLink).append("</td>\n")
                        .append("<td>").append(dangerLink).append("</td>\n")
                        .append("<td>").append(isAcknowledged).append("</td>\n")
                        .append("<td>");

                if (alert.getAlertTemplateId() == null) htmlBodyStringBuilder.append(enable).append(", ").append(alter).append(", ").append(clone);
                if ((alert.getAlertTemplateId() == null) && !suspensions_SuspendByAlertId_ByAlertId.containsKey(alert.getId())) htmlBodyStringBuilder.append(", ").append(remove);
                if ((alert.getAlertTemplateId() == null) && !acknowledge.isEmpty()) htmlBodyStringBuilder.append(", ").append(acknowledge);
                else if ((alert.getAlertTemplateId() != null) && !acknowledge.isEmpty()) htmlBodyStringBuilder.append(acknowledge);

                htmlBodyStringBuilder.append("</td>\n").append("</tr>\n");
            }

            htmlBodyStringBuilder.append(""
                    + "</tbody>\n"
                    + "<tfoot> \n"
                    + "  <tr>\n" 
                    + "    <th></th>\n"
                    + "    <th></th>\n"
                    + "    <th></th>\n" 
                    + "    <th></th>\n" 
                    + "    <th></th>\n" 
                    + "    <th></th>\n" 
                    + "    <th></th>\n" 
                    + "    <th></th>\n" 
                    + "    <th></th>\n" 
                    + "    <th></th>\n" 
                    + "    <th></th>\n" 
                    + "    <th></th>\n" 
                    + "    <th></th>\n" 
                    + "  </tr>\n" 
                    + "</tfoot>" 
                    + "</table>\n"
                    + "</div>\n"
                    + "</div>\n");

            String htmlBody = (statsAggHtmlFramework.createHtmlBody(htmlBodyStringBuilder.toString()));

            html.append(""
                    + "<!DOCTYPE html>\n"
                    + "<html>\n")
                    .append(htmlHeader)
                    .append(htmlBody)
                    .append("</html>");

            return html.toString();
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return "Fatal error encountered";
        }
        finally {
            DatabaseUtils.cleanup(connection);
        }
        
    }

    // Gets value for Alerts 'Acknowledged?' column 
    protected static String getIsAcknowledgedTableValue(Alert alert) {
        
        if (alert == null) {
            return "N/A";
        }
        
        String isAcknowledged = "N/A";
        
        try {
            if ((alert.isCautionAlertActive() && (alert.isCautionAlertAcknowledged() != null) && alert.isCautionAlertAcknowledged()) &&
                (alert.isDangerAlertActive() && (alert.isDangerAlertAcknowledged() != null) && alert.isDangerAlertAcknowledged())) {
                // caution & danger both acknowledged
                isAcknowledged = "Yes";
            }
            else if ((alert.isCautionAlertActive() && (alert.isCautionAlertAcknowledged() != null) && alert.isCautionAlertAcknowledged()) &&
                (alert.isDangerAlertActive() && (alert.isDangerAlertAcknowledged() != null) && !alert.isDangerAlertAcknowledged())) {
                // caution acknowledged, danger not acknowledged
                isAcknowledged = "Caution Only";
            }
            else if ((alert.isCautionAlertActive() && (alert.isCautionAlertAcknowledged() != null) && !alert.isCautionAlertAcknowledged()) &&
                (alert.isDangerAlertActive() && (alert.isDangerAlertAcknowledged() != null) && alert.isDangerAlertAcknowledged())) {
                // caution & danger both acknowledged
                isAcknowledged = "Danger Only";
            }
            else if ((alert.isCautionAlertActive() && (alert.isCautionAlertAcknowledged() != null) && !alert.isCautionAlertAcknowledged()) &&
                (alert.isDangerAlertActive() && (alert.isDangerAlertAcknowledged() != null) && !alert.isDangerAlertAcknowledged())) {
                // caution & danger both active & unacknowledged
                isAcknowledged = "No";
            }
            else if (!alert.isCautionAlertActive() && (alert.isDangerAlertActive() && (alert.isDangerAlertAcknowledged() != null) && alert.isDangerAlertAcknowledged())) {
                // danger acknowledged, caution not active (therefore not acknowledged)
                isAcknowledged = "Yes";
            }
            else if (!alert.isDangerAlertActive() && (alert.isCautionAlertActive() && (alert.isCautionAlertAcknowledged() != null) && alert.isCautionAlertAcknowledged())) {
                // caution acknowledged, danger not active (therefore not acknowledged)
                isAcknowledged = "Yes";
            }
            else if (!alert.isDangerAlertActive() && (alert.isCautionAlertActive() && (alert.isCautionAlertAcknowledged() != null) && !alert.isCautionAlertAcknowledged())) {
                // caution not acknowledged, danger not active (therefore not acknowledged)
                isAcknowledged = "No";
            }
            else if (!alert.isCautionAlertActive() && (alert.isDangerAlertActive() && (alert.isDangerAlertAcknowledged() != null) && !alert.isDangerAlertAcknowledged())) {
                // danger not acknowledged, caution not active (therefore not acknowledged)
                isAcknowledged = "No";
            }
            else {
                isAcknowledged = "N/A";
            }
        }
        catch (Exception e) {
            logger.debug(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return isAcknowledged;
    }
    
    protected static void sendPagerDutyAcknowledgeRequest(Integer alertId) {

        if (!ApplicationConfiguration.isPagerdutyIntegrationEnabled()) return;

        Alert alert = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, alertId);
        if (alert == null) return;

        int alertLevel, notificationGroupId;
        
        if ((alert.isDangerAlertActive() != null) && alert.isDangerAlertActive() && (alert.getDangerNotificationGroupId() != null)) {
            alertLevel = Alert.DANGER;
            notificationGroupId = alert.getDangerNotificationGroupId();
        } 
        else if ((alert.isCautionAlertActive() != null) && alert.isCautionAlertActive() && (alert.getCautionNotificationGroupId() != null)) {
            alertLevel = Alert.CAUTION;
            notificationGroupId = alert.getCautionNotificationGroupId();
        }
        else {
            return;
        }

        String routingKey = NotificationThread.getPagerdutyRoutingKeyForAlert(notificationGroupId);
        if (routingKey == null) return;
        
        NotificationThread notificationThread = new NotificationThread(alert, alertLevel, new ArrayList<>(), new HashMap<>(), new HashMap<>(), false, false, "");
        JsonObject event = notificationThread.buildPagerdutyAcknowledgeEvent();
        notificationThread.sendPagerdutyEvent(routingKey, event);
    }
    
}
