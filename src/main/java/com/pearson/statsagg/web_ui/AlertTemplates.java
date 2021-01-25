package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.database_objects.DatabaseObjectStatus;
import com.pearson.statsagg.database_objects.alert_templates.AlertTemplate;
import com.pearson.statsagg.database_objects.alert_templates.AlertTemplatesDao;
import com.pearson.statsagg.database_objects.alert_templates.AlertTemplatesDaoWrapper;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroup;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupsDao;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroup;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroupsDao;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.core_utils.KeyValue;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class AlertTemplates extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AlertTemplates.class.getName());
    
    public static final String PAGE_NAME = "Alert Templates";
    
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
                Integer alertTemplateId = Integer.parseInt(Common.getSingleParameterAsString(request, "Id"));
                Boolean isEnabled = Boolean.parseBoolean(Common.getSingleParameterAsString(request, "Enabled"));
                AlertTemplates.changeAlertTemplateEnabled(alertTemplateId, isEnabled);
            }

            if ((operation != null) && operation.equals("Clone")) {
                Integer alertTemplateId = Integer.parseInt(request.getParameter("Id"));
                AlertTemplates.cloneAlertTemplate(alertTemplateId);
            }

            if ((operation != null) && operation.equals("Remove")) {
                Integer alertTemplateId = Integer.parseInt(Common.getSingleParameterAsString(request, "Id"));
                AlertTemplates.removeAlertTemplate(alertTemplateId);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        StatsAggHtmlFramework.redirectAndGet(response, 303, "AlertTemplates");
    }
    
    public static String changeAlertTemplateEnabled(Integer alertId, Boolean isEnabled) {

        if ((alertId == null) || (isEnabled == null)) {
            return "Invalid input!";
        }
        
        boolean isSuccess = false;
        
        AlertTemplate alertTemplate = AlertTemplatesDao.getAlertTemplate(DatabaseConnections.getConnection(), true, alertId);
        
        if (alertTemplate != null) {
            alertTemplate.setIsEnabled(isEnabled);

            AlertTemplatesDaoWrapper alertTemplatesDaoWrapper = AlertTemplatesDaoWrapper.alterRecordInDatabase(alertTemplate, alertTemplate.getName());
            if (AlertTemplatesDaoWrapper.STATUS_CODE_SUCCESS == alertTemplatesDaoWrapper.getLastAlterRecordStatus()) isSuccess = true;
            if (GlobalVariables.alertInvokerThread != null) GlobalVariables.alertInvokerThread.runAlertThread(false, true);
        }
        
        if (isSuccess && isEnabled) return "Successfully enabled alert template";
        if (isSuccess && !isEnabled) return "Successfully disabled alert template";
        else return "Error -- could not alter alert template";
    }
    
    protected static void cloneAlertTemplate(Integer alertTemplateId) {
        
        if (alertTemplateId == null) {
            return;
        }
        
        Connection connection = null;
        
        try {
            connection = DatabaseConnections.getConnection(); 
            AlertTemplate alertTemplate = AlertTemplatesDao.getAlertTemplate(connection, false, alertTemplateId);

            if ((alertTemplate != null) && (alertTemplate.getName() != null)) {
                Set<String> allAlertTemplateNames = AlertTemplatesDao.getAlertTemplateNames(connection, true);

                AlertTemplate clonedAlertTemplate = AlertTemplate.copy(alertTemplate);
                clonedAlertTemplate.setId(-1);
                String clonedAlertTemplateName = StatsAggHtmlFramework.createCloneName(alertTemplate.getName(), allAlertTemplateNames);

                clonedAlertTemplate.setName(clonedAlertTemplateName);
                
                AlertTemplatesDaoWrapper.createRecordInDatabase(clonedAlertTemplate);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static String removeAlertTemplate(Integer alertTemplateId) {
        
        if (alertTemplateId == null) {
            return null;
        }
        
        String returnString = null;
        
        try {
            AlertTemplate alertTemplate = AlertTemplatesDao.getAlertTemplate(DatabaseConnections.getConnection(), true, alertTemplateId);

            AlertTemplatesDaoWrapper alertTemplatesDaoWrapper = AlertTemplatesDaoWrapper.deleteRecordInDatabase(alertTemplate);
            returnString = alertTemplatesDaoWrapper.getReturnString();
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnString;
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
            "         <th>Derived Alerts</th>\n" +
            "         <th>Operations</th>\n" +
            "       </tr>\n" +
            "     </thead>\n" +
            "     <tbody>\n");

        Connection connection = null;

        try {
            connection = DatabaseConnections.getConnection();
            Map<String,Alert> alerts_ByName = AlertsDao.getAlerts_ByName(connection, false);
            Map<String,MetricGroup> metricGroups_ByName = MetricGroupsDao.getMetricGroups_ByName(connection, false);
            Map<String,NotificationGroup> notificationGroups_ByName = NotificationGroupsDao.getNotificationGroups_ByName(connection, false);
            List<AlertTemplate> alertTemplates = AlertTemplatesDao.getAlertTemplates(connection, false);
            if (alertTemplates == null) alertTemplates = new ArrayList<>();
            
            for (AlertTemplate alertTemplate : alertTemplates) {
                if (alertTemplate == null) continue;

                List<Alert> alerts = AlertsDao.getAlerts_FilterByAlertTemplateId(connection, false, alertTemplate.getId());
                Set<String> alertNamesThatAlertTemplateWantsToCreate = com.pearson.statsagg.threads.template_related.Common.getNamesThatTemplateWantsToCreate(alertTemplate.getVariableSetListId(), alertTemplate.getAlertNameVariable());
                String numberOfDerivedAlerts = (alerts == null) ? "0" : (alerts.size() + "");

                boolean isAlertStatusWarning = false, isAlertStatusDanger = false;
                if (alerts != null) {
                    for (Alert alert : alerts) {
                        DatabaseObjectStatus alertStatus = AlertTemplate_DerivedAlerts.getAlertStatus(connection, alertTemplate, alert, null, alerts_ByName, metricGroups_ByName, notificationGroups_ByName);
                        if ((alertStatus != null) && (alertStatus.getStatus() == DatabaseObjectStatus.STATUS_WARNING)) isAlertStatusWarning = true;
                        if ((alertStatus != null) && (alertStatus.getStatus() == DatabaseObjectStatus.STATUS_DANGER)) isAlertStatusDanger = true;
                    }
                }

                String rowAlertStatusContext = "";
                if ((alertNamesThatAlertTemplateWantsToCreate != null) && (alerts != null) && (alerts.size() != alertNamesThatAlertTemplateWantsToCreate.size())) rowAlertStatusContext = "class=\"danger\"";
                else if (isAlertStatusDanger) rowAlertStatusContext = "class=\"danger\"";
                else if (isAlertStatusWarning) rowAlertStatusContext = "class=\"warning\"";

                String alertTemplateDetails = "<a class=\"iframe cboxElement\" href=\"AlertTemplateDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(alertTemplate.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(alertTemplate.getName()) + "</a>";

                String derivedAlertDetails = "<a class=\"iframe cboxElement\" href=\"AlertTemplate-DerivedAlerts?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(alertTemplate.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(numberOfDerivedAlerts) + "</a>";

                String alter = "<a href=\"CreateAlertTemplate?Operation=Alter&amp;Name=" + StatsAggHtmlFramework.urlEncode(alertTemplate.getName()) + "\">alter</a>";

                List<KeyValue<String,String>> cloneKeysAndValues = new ArrayList<>();
                cloneKeysAndValues.add(new KeyValue("Operation", "Clone"));
                cloneKeysAndValues.add(new KeyValue("Id", alertTemplate.getId().toString()));
                String clone = StatsAggHtmlFramework.buildJavaScriptPostLink("Clone_" + alertTemplate.getName(), "AlertTemplates", "clone", cloneKeysAndValues);

                List<KeyValue<String,String>> removeKeysAndValues = new ArrayList<>();
                removeKeysAndValues.add(new KeyValue("Operation", "Remove"));
                removeKeysAndValues.add(new KeyValue("Id", alertTemplate.getId().toString()));
                String remove = StatsAggHtmlFramework.buildJavaScriptPostLink("Remove_" + alertTemplate.getName(), "AlertTemplates", "remove", 
                        removeKeysAndValues, true, "Are you sure you want to remove this alert template?");

                htmlBodyStringBuilder
                        .append("<tr ").append(rowAlertStatusContext).append(">\n")
                        .append("<td class=\"statsagg_force_word_break\">").append(alertTemplateDetails).append("</td>\n")
                        .append("<td class=\"statsagg_force_word_break\">").append(derivedAlertDetails).append("</td>\n")
                        .append("<td>").append(alter).append(", ").append(clone).append(", ").append(remove);

                htmlBodyStringBuilder.append("</td>\n").append("</tr>\n");
            }

            htmlBodyStringBuilder.append(""
                    + "</tbody>\n"
                    + "<tfoot> \n"
                    + "  <tr>\n" 
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

}
