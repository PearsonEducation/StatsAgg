package com.pearson.statsagg.webui;

import com.pearson.statsagg.database.alert_suspensions.AlertSuspension;
import com.pearson.statsagg.database.alert_suspensions.AlertSuspensionsDao;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database.alerts.Alert;
import com.pearson.statsagg.database.alerts.AlertsDao;
import com.pearson.statsagg.database.metric_group.MetricGroup;
import com.pearson.statsagg.database.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database.metric_group_tags.MetricGroupTag;
import com.pearson.statsagg.database.metric_group_tags.MetricGroupTagsDao;
import com.pearson.statsagg.database.notifications.NotificationGroupsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.KeyValue;
import com.pearson.statsagg.utilities.StackTrace;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "Alerts", urlPatterns = {"/Alerts"})
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
        
        response.setContentType("text/html");
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
        
        String operation = request.getParameter("Operation");

        if ((operation != null) && operation.equals("Enable")) {
            String name = request.getParameter("Name");
            Boolean isEnabled = Boolean.parseBoolean(request.getParameter("Enabled"));
            changeAlertEnabled(name, isEnabled);
        }
        
        if ((operation != null) && operation.equals("Clone")) {
            String name = request.getParameter("Name");
            cloneAlert(name);
        }
        
        if ((operation != null) && operation.equals("Remove")) {
            String name = request.getParameter("Name");
            removeAlert(name);
        }
        
        processGetRequest(request, response);
    }
        
    private void changeAlertEnabled(String alertName, Boolean isEnabled) {
        
        if ((alertName == null) || (isEnabled == null)) {
            return;
        }
        
        AlertsDao alertsDao = new AlertsDao();
        Alert alert = alertsDao.getAlertByName(alertName);
        
        if (alert != null) {
            alert.setIsEnabled(isEnabled);

            if (!isEnabled) {
                alert.setIsCautionAlertActive(false);
                alert.setCautionAlertLastSentTimestamp(null);
                alert.setCautionActiveAlertsSet(null);
                alert.setIsDangerAlertActive(false);
                alert.setDangerAlertLastSentTimestamp(null);
                alert.setDangerActiveAlertsSet(null);
            }

            AlertsLogic alertsLogic = new AlertsLogic();
            alertsLogic.alterRecordInDatabase(alert, alertName);
        }
    }

    private void cloneAlert(String alertName) {
        
        if (alertName == null) {
            return;
        }
        
        try {
            AlertsDao alertsDao = new AlertsDao(false);
            Alert alert = alertsDao.getAlertByName(alertName);
            List<Alert> allAlerts = alertsDao.getAllDatabaseObjectsInTable();
            alertsDao.close();

            if ((alert != null) && (alert.getName() != null)) {
                Set<String> allAlertNames = new HashSet<>();
                for (Alert currentAlert : allAlerts) {
                    if (currentAlert.getName() != null) {
                        allAlertNames.add(currentAlert.getName());
                    }
                }
                
                Alert clonedAlert = Alert.copy(alert);
                clonedAlert.setId(-1);
                String clonedAlertName = StatsAggHtmlFramework.createCloneName(alert.getName(), allAlertNames);

                clonedAlert.setName(clonedAlertName);
                clonedAlert.setUppercaseName(clonedAlertName.toUpperCase());
                clonedAlert.setIsCautionAlertActive(false);
                clonedAlert.setCautionAlertLastSentTimestamp(null);
                clonedAlert.setCautionActiveAlertsSet(null);
                clonedAlert.setIsDangerAlertActive(false);
                clonedAlert.setDangerAlertLastSentTimestamp(null);
                clonedAlert.setDangerActiveAlertsSet(null);

                AlertsLogic alertsLogic = new AlertsLogic();
                alertsLogic.alterRecordInDatabase(clonedAlert);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
    private void removeAlert(String alertName) {
        
        if (alertName == null) {
            return;
        }
        
        AlertsLogic alertsLogic = new AlertsLogic();
        alertsLogic.deleteRecordInDatabase(alertName);
    }
    
    private String buildAlertsHtml() {
        
        StringBuilder html = new StringBuilder("");

        StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
        String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");

        StringBuilder htmlBodyStringBuilder = new StringBuilder("");
        htmlBodyStringBuilder.append(
            "<div id=\"page-content-wrapper\">\n" +
            "<!-- Keep all page content within the page-content inset div! -->\n" +
            "<div class=\"page-content inset\">\n" +
            "  <div class=\"content-header\"> \n" +
            "    <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "    <div class=\"pull-right \">\n" +
            "     <a href=\"CreateAlert\" class=\"btn btn-primary\">Create New Alert <i class=\"fa fa-long-arrow-right\"></i></a> \n" +
            "    </div>\n" + 
            "  </div>\n" +   
            "  <table id=\"AlertsTable\" style=\"font-size:12px;\" class=\"table table-bordered table-hover\">\n" +
            "     <thead>\n" +
            "       <tr>\n" +
            "         <th>Alert name</th>\n" +
            "         <th>Metric Group Association</th>\n" +
            "         <th>Metric Group Tags</th>\n" +
            "         <th>Caution Notification Group</th>\n" +
            "         <th>Danger Notification Group</th>\n" +
            "         <th>Enabled?</th>\n" +
            "         <th>Suspended?</th>\n" +
            "         <th>Triggered?</th>\n" +
            "         <th>Caution Triggered?</th>\n" +
            "         <th>Danger Triggered?</th>\n" +
            "         <th>Operations</th>\n" +
            "       </tr>\n" +
            "     </thead>\n" +
            "     <tbody>\n");

        AlertsDao alertsDao = new AlertsDao();
        List<Alert> alerts = alertsDao.getAllDatabaseObjectsInTable();
        
        MetricGroupTagsDao metricGroupTagsDao = new MetricGroupTagsDao();
        Map<Integer, List<MetricGroupTag>> tagsByMetricGroupId = metricGroupTagsDao.getAllMetricGroupTagsByMetricGroupId();
        
        NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
        Map<Integer, String> notificationGroupNames_ById = notificationGroupsDao.getNotificationGroupNames_ById();
        
        AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao();
        Map<Integer,List<AlertSuspension>> alertSuspensions_SuspendByAlertId_ByAlertId = alertSuspensionsDao.getAlertSuspensions_SuspendByAlertId_ByAlertId();
        
        for (Alert alert : alerts) {

            String rowAlertStatusContext = "";
            if ((GlobalVariables.pendingCautionAlertsByAlertId != null) && GlobalVariables.pendingCautionAlertsByAlertId.containsKey(alert.getId())) rowAlertStatusContext = "class=\"info\"";
            else if ((GlobalVariables.pendingDangerAlertsByAlertId != null) && GlobalVariables.pendingDangerAlertsByAlertId.containsKey(alert.getId())) rowAlertStatusContext = "class=\"info\"";
            else if ((GlobalVariables.alertSuspensionStatusByAlertId != null) && GlobalVariables.alertSuspensionStatusByAlertId.containsKey(alert.getId()) && GlobalVariables.alertSuspensionStatusByAlertId.get(alert.getId())) rowAlertStatusContext = "class=\"info\"";
            else if (alert.isCautionAlertActive() && !alert.isDangerAlertActive()) rowAlertStatusContext = "class=\"warning\"";
            else if (alert.isDangerAlertActive()) rowAlertStatusContext = "class=\"danger\"";
            
            String alertDetails = "<a href=\"AlertDetails?Name=" + StatsAggHtmlFramework.urlEncode(alert.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(alert.getName()) + "</a>";
            
            String metricGroupNameAndLink;
            MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
            MetricGroup metricGroup = metricGroupsDao.getMetricGroup(alert.getMetricGroupId());
            if ((metricGroup == null) || (metricGroup.getName() == null)) metricGroupNameAndLink = "N/A";
            else metricGroupNameAndLink = "<a href=\"MetricGroupDetails?Name=" + StatsAggHtmlFramework.urlEncode(metricGroup.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(metricGroup.getName()) + "</a>";
            
            StringBuilder tagsCsv = new StringBuilder("");
            if ((metricGroup != null) && (metricGroup.getId() != null) && (tagsByMetricGroupId != null)) {
                List<MetricGroupTag> metricGroupTags = tagsByMetricGroupId.get(metricGroup.getId());
                
                if (metricGroupTags != null) {
                    for (int i = 0; i < metricGroupTags.size(); i++) {
                        MetricGroupTag metricGroupTag = metricGroupTags.get(i);
                        tagsCsv = tagsCsv.append("<u>").append(StatsAggHtmlFramework.htmlEncode(metricGroupTag.getTag())).append("</u>");
                        if ((i + 1) < metricGroupTags.size()) tagsCsv.append("&nbsp;&nbsp;");
                    }
                }
            }
            
            String cautionNotificationGroupNameAndLink;
            if ((notificationGroupNames_ById == null) || (alert.getCautionNotificationGroupId() == null) || !notificationGroupNames_ById.containsKey(alert.getCautionNotificationGroupId())) {
                cautionNotificationGroupNameAndLink = "N/A";
            }
            else {
                cautionNotificationGroupNameAndLink = "<a href=\"NotificationGroupDetails?Name=" + 
                    StatsAggHtmlFramework.urlEncode(notificationGroupNames_ById.get(alert.getCautionNotificationGroupId())) + "\">" + 
                    StatsAggHtmlFramework.htmlEncode(notificationGroupNames_ById.get(alert.getCautionNotificationGroupId())) + "</a>";
            }

            String dangerNotificationGroupNameAndLink;
            if ((notificationGroupNames_ById == null) || (alert.getDangerNotificationGroupId() == null) || !notificationGroupNames_ById.containsKey(alert.getDangerNotificationGroupId())) {
                dangerNotificationGroupNameAndLink = "N/A";
            }
            else {
                dangerNotificationGroupNameAndLink = "<a href=\"NotificationGroupDetails?Name=" + 
                    StatsAggHtmlFramework.urlEncode(notificationGroupNames_ById.get(alert.getDangerNotificationGroupId())) + "\">" + 
                    StatsAggHtmlFramework.htmlEncode(notificationGroupNames_ById.get(alert.getDangerNotificationGroupId())) + "</a>";
            }
            
            Boolean isSuspended = GlobalVariables.alertSuspensionStatusByAlertId.get(alert.getId());
            if (isSuspended == null) isSuspended = false;
            String isSuspendedLink = "<a href=\"AlertAlertSuspensionAssociations?Name=" + StatsAggHtmlFramework.urlEncode(alert.getName()) + "\">" + isSuspended + "</a>";
            
            String triggeredLink = "false";
            String cautionLink = Boolean.toString(alert.isCautionAlertActive());
            String dangerLink = Boolean.toString(alert.isDangerAlertActive());
            
            if (cautionLink.equalsIgnoreCase("true") || dangerLink.equalsIgnoreCase("true")) {
                triggeredLink = "<a href=\"AlertAssociations?Name=" + StatsAggHtmlFramework.urlEncode(alert.getName()) + "&Level=" + "Triggered" + "\">true</a>";
            }
            
            if ((alert.isCautionAlertActive() != null) && alert.isCautionAlertActive()) {
                cautionLink = "<a href=\"AlertAssociations?Name=" + StatsAggHtmlFramework.urlEncode(alert.getName()) + "&Level=" + "Caution" + "\">true</a>";
            }
            
            if ((alert.isDangerAlertActive() != null) && alert.isDangerAlertActive()) {
                dangerLink = "<a href=\"AlertAssociations?Name=" + StatsAggHtmlFramework.urlEncode(alert.getName()) + "&Level=" + "Danger" + "\">true</a>";
            }
            
            String enable; 
            if (alert.isEnabled()) {
                List<KeyValue> keysAndValues = new ArrayList<>();
                keysAndValues.add(new KeyValue("Operation", "Enable"));
                keysAndValues.add(new KeyValue("Name", Encode.forHtmlAttribute(alert.getName())));
                keysAndValues.add(new KeyValue("Enabled", "false"));
                enable = StatsAggHtmlFramework.buildJavaScriptPostLink("Enable_" + alert.getName(), "Alerts", "disable", keysAndValues);
            }
            else {
                List<KeyValue> keysAndValues = new ArrayList<>();
                keysAndValues.add(new KeyValue("Operation", "Enable"));
                keysAndValues.add(new KeyValue("Name", Encode.forHtmlAttribute(alert.getName())));
                keysAndValues.add(new KeyValue("Enabled", "true"));
                enable = StatsAggHtmlFramework.buildJavaScriptPostLink("Enable_" + alert.getName(), "Alerts", "enable", keysAndValues);
            }
            
            String alter = "<a href=\"CreateAlert?Operation=Alter&amp;Name=" + StatsAggHtmlFramework.urlEncode(alert.getName()) + "\">alter</a>";
            
            List<KeyValue> cloneKeysAndValues = new ArrayList<>();
            cloneKeysAndValues.add(new KeyValue("Operation", "Clone"));
            cloneKeysAndValues.add(new KeyValue("Name", Encode.forHtmlAttribute(alert.getName())));
            String clone = StatsAggHtmlFramework.buildJavaScriptPostLink("Clone_" + alert.getName(), "Alerts", "clone", cloneKeysAndValues);
                    
            List<KeyValue> removeKeysAndValues = new ArrayList<>();
            removeKeysAndValues.add(new KeyValue("Operation", "Remove"));
            removeKeysAndValues.add(new KeyValue("Name", Encode.forHtmlAttribute(alert.getName())));
            String remove = StatsAggHtmlFramework.buildJavaScriptPostLink("Remove_" + alert.getName(), "Alerts", "remove", 
                    removeKeysAndValues, true, "Are you sure you want to remove this alert?");

            htmlBodyStringBuilder
                    .append("<tr ").append(rowAlertStatusContext).append(">\n")
                    .append("<td>").append(alertDetails).append("</td>\n")
                    .append("<td>").append(metricGroupNameAndLink).append("</td>\n")
                    .append("<td>").append(tagsCsv.toString()).append("</td>\n")
                    .append("<td>").append(cautionNotificationGroupNameAndLink).append("</td>\n")
                    .append("<td>").append(dangerNotificationGroupNameAndLink).append("</td>\n")
                    .append("<td>").append(alert.isEnabled()).append("</td>\n")
                    .append("<td>").append(isSuspendedLink).append("</td>\n")
                    .append("<td>").append(triggeredLink).append("</td>\n")
                    .append("<td>").append(cautionLink).append("</td>\n")
                    .append("<td>").append(dangerLink).append("</td>\n")
                    .append("<td>").append(enable).append(", ").append(alter).append(", ").append(clone);
            
            if ((alertSuspensions_SuspendByAlertId_ByAlertId == null) || !alertSuspensions_SuspendByAlertId_ByAlertId.containsKey(alert.getId())) { 
                htmlBodyStringBuilder.append(", ").append(remove);
            }
                    
            htmlBodyStringBuilder.append("</td>\n").append("</tr>\n");
        }

        htmlBodyStringBuilder.append(""
                + "</tbody>\n"
                + "<tfoot> \n"
                + "  <tr class=\"statsagg_table_footer\" >\n" 
                + "    <th>Filter</th>\n"
                + "    <th>Filter</th>\n" 
                + "    <th>Filter</th>\n" 
                + "    <th>Filter</th>\n" 
                + "    <th>Filter</th>\n" 
                + "    <th>Filter</th>\n" 
                + "    <th>Filter</th>\n" 
                + "    <th>Filter</th>\n" 
                + "    <th>Filter</th>\n" 
                + "    <th>Filter</th>\n" 
                + "    <th>Filter</th>\n" 
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

}
