package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTag;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTagsDao;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroup;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupsDao;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroupsDao;
import com.pearson.statsagg.database_objects.suspensions.Suspension;
import com.pearson.statsagg.database_objects.suspensions.SuspensionsDao;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.core_utils.KeyValue;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import static com.pearson.statsagg.web_ui.Alerts.getIsAcknowledgedTableValue;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class AlertTemplates extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AlertTemplates.class.getName());
    
    public static final String PAGE_NAME = "Alerts Templates";
    
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
            String html = buildAlertTemplatesHtml();
            
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
                Alerts.changeAlertEnabled(alertId, isEnabled);
            }

            if ((operation != null) && operation.equals("Clone")) {
                Integer alertId = Integer.parseInt(request.getParameter("Id"));
                Alerts.cloneAlert(alertId);
            }

            if ((operation != null) && operation.equals("Remove")) {
                Integer alertId = Integer.parseInt(Common.getSingleParameterAsString(request, "Id"));
                Alerts.removeAlert(alertId);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        StatsAggHtmlFramework.redirectAndGet(response, 303, "AlertTemplates");
    }
    
    public static String buildAlertTemplatesHtml() {
        
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
            "     <a href=\"CreateAlertTemplate\" class=\"btn btn-primary statsagg_page_content_font\">Create New Alert Template <i class=\"fa fa-long-arrow-right\"></i></a> \n" +
            "    </div>\n" + 
            "  </div>\n" +   
            "  <table id=\"AlertTemplatesTable\" style=\"display:none\" class=\"table table-bordered table-hover compact\">\n" +
            "     <thead>\n" +
            "       <tr>\n" +
            "         <th>Alert Template Name</th>\n" +
            "         <th>Metric Group Association</th>\n" +
            "         <th>Metric Group Tags</th>\n" +
            "         <th>Caution Notification Group</th>\n" +
            "         <th>Danger Notification Group</th>\n" +
            "         <th>Enabled?</th>\n" +  
            "         <th>Operations</th>\n" +
            "       </tr>\n" +
            "     </thead>\n" +
            "     <tbody>\n");

        Connection connection = DatabaseConnections.getConnection();
        DatabaseUtils.setAutoCommit(connection, false);
        List<Alert> alerts = AlertsDao.getAlertTemplates(connection, false);
        Map<Integer, List<MetricGroupTag>> tagsByMetricGroupId = MetricGroupTagsDao.getAllMetricGroupTagsByMetricGroupId(connection, false);
        Map<Integer, String> notificationGroupNames_ById = NotificationGroupsDao.getNotificationGroupNames_ById(connection, false);
        DatabaseUtils.cleanup(connection);
        
        for (Alert alert : alerts) {
            if (alert == null) continue;
            
            String rowAlertStatusContext = "";

            String alertDetails = "<a class=\"iframe cboxElement\" href=\"AlertTemplateDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(alert.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(alert.getName()) + "</a>";
            
            String metricGroupNameAndLink;
            MetricGroup metricGroup = MetricGroupsDao.getMetricGroup(DatabaseConnections.getConnection(), true, alert.getMetricGroupId());
            if ((metricGroup == null) || (metricGroup.getName() == null)) metricGroupNameAndLink = "N/A";
            else metricGroupNameAndLink = "<a class=\"iframe cboxElement\" href=\"MetricGroupDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(metricGroup.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(metricGroup.getName()) + "</a>";
            
            StringBuilder tagsCsv = new StringBuilder();
            if ((metricGroup != null) && (metricGroup.getId() != null) && (tagsByMetricGroupId != null)) {
                List<MetricGroupTag> metricGroupTags = tagsByMetricGroupId.get(metricGroup.getId());
                
                if (metricGroupTags != null) {
                    for (int i = 0; i < metricGroupTags.size(); i++) {
                        MetricGroupTag metricGroupTag = metricGroupTags.get(i);
                        tagsCsv = tagsCsv.append("<u>").append(StatsAggHtmlFramework.htmlEncode(metricGroupTag.getTag())).append("</u>");
                        if ((i + 1) < metricGroupTags.size()) tagsCsv.append(" &nbsp;");
                    }
                }
            }
            
            String cautionNotificationGroupNameAndLink;
            if ((notificationGroupNames_ById == null) || (alert.getCautionNotificationGroupId() == null) || ((alert.isCautionEnabled() != null) && !alert.isCautionEnabled()) || 
                    !notificationGroupNames_ById.containsKey(alert.getCautionNotificationGroupId())) {
                cautionNotificationGroupNameAndLink = "N/A";
            }
            else {
                cautionNotificationGroupNameAndLink = "<a class=\"iframe cboxElement\" href=\"NotificationGroupDetails?ExcludeNavbar=true&amp;Name=" + 
                    StatsAggHtmlFramework.urlEncode(notificationGroupNames_ById.get(alert.getCautionNotificationGroupId())) + "\">" + 
                    StatsAggHtmlFramework.htmlEncode(notificationGroupNames_ById.get(alert.getCautionNotificationGroupId())) + "</a>";
            }

            String dangerNotificationGroupNameAndLink;
            if ((notificationGroupNames_ById == null) || (alert.getDangerNotificationGroupId() == null) || ((alert.isDangerEnabled() != null) && !alert.isDangerEnabled()) || 
                    !notificationGroupNames_ById.containsKey(alert.getDangerNotificationGroupId())) {
                dangerNotificationGroupNameAndLink = "N/A";
            }
            else {
                dangerNotificationGroupNameAndLink = "<a class=\"iframe cboxElement\" href=\"NotificationGroupDetails?ExcludeNavbar=true&amp;Name=" + 
                    StatsAggHtmlFramework.urlEncode(notificationGroupNames_ById.get(alert.getDangerNotificationGroupId())) + "\">" + 
                    StatsAggHtmlFramework.htmlEncode(notificationGroupNames_ById.get(alert.getDangerNotificationGroupId())) + "</a>";
            }
            
            String isAlertEnabled = "No";
            if ((alert.isEnabled() != null) && alert.isEnabled()) isAlertEnabled = "Yes";
      
            String enable; 
            if (alert.isEnabled()) {
                List<KeyValue<String,String>> keysAndValues = new ArrayList<>();
                keysAndValues.add(new KeyValue("Operation", "Enable"));
                keysAndValues.add(new KeyValue("Id", alert.getId().toString()));
                keysAndValues.add(new KeyValue("Enabled", "false"));
                enable = StatsAggHtmlFramework.buildJavaScriptPostLink("Enable_" + alert.getName(), "AlertTemplates", "disable", keysAndValues);
            }
            else {
                List<KeyValue<String,String>> keysAndValues = new ArrayList<>();
                keysAndValues.add(new KeyValue("Operation", "Enable"));
                keysAndValues.add(new KeyValue("Id", alert.getId().toString()));
                keysAndValues.add(new KeyValue("Enabled", "true"));
                enable = StatsAggHtmlFramework.buildJavaScriptPostLink("Enable_" + alert.getName(), "AlertTemplates", "enable", keysAndValues);
            }

            String alter = "<a href=\"CreateAlertTemplate?Operation=Alter&amp;Name=" + StatsAggHtmlFramework.urlEncode(alert.getName()) + "\">alter</a>";
            
            List<KeyValue<String,String>> cloneKeysAndValues = new ArrayList<>();
            cloneKeysAndValues.add(new KeyValue("Operation", "Clone"));
            cloneKeysAndValues.add(new KeyValue("Id", alert.getId().toString()));
            String clone = StatsAggHtmlFramework.buildJavaScriptPostLink("Clone_" + alert.getName(), "AlertTemplates", "clone", cloneKeysAndValues);
                    
            List<KeyValue<String,String>> removeKeysAndValues = new ArrayList<>();
            removeKeysAndValues.add(new KeyValue("Operation", "Remove"));
            removeKeysAndValues.add(new KeyValue("Id", alert.getId().toString()));
            String remove = StatsAggHtmlFramework.buildJavaScriptPostLink("Remove_" + alert.getName(), "AlertTemplates", "remove", 
                    removeKeysAndValues, true, "Are you sure you want to remove this alert template?");

            htmlBodyStringBuilder
                    .append("<tr ").append(rowAlertStatusContext).append(">\n")
                    .append("<td class=\"statsagg_force_word_break\">").append(alertDetails).append("</td>\n")
                    .append("<td class=\"statsagg_force_word_break\">").append(metricGroupNameAndLink).append("</td>\n")
                    .append("<td class=\"statsagg_force_word_break\">").append(tagsCsv.toString()).append("</td>\n")
                    .append("<td class=\"statsagg_force_word_break\">").append(cautionNotificationGroupNameAndLink).append("</td>\n")
                    .append("<td class=\"statsagg_force_word_break\">").append(dangerNotificationGroupNameAndLink).append("</td>\n")
                    .append("<td>").append(isAlertEnabled).append("</td>\n")
                    .append("<td>").append(enable).append(", ").append(alter).append(", ").append(clone).append(", ").append(remove);
                    
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
