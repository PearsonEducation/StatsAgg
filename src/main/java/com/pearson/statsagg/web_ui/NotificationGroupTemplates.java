package com.pearson.statsagg.web_ui;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.database_objects.notification_group_templates.NotificationGroupTemplate;
import com.pearson.statsagg.database_objects.notification_group_templates.NotificationGroupTemplatesDao;
import com.pearson.statsagg.database_objects.notification_group_templates.NotificationGroupTemplatesDaoWrapper;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroup;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroupsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.core_utils.KeyValue;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.core_utils.Threads;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class NotificationGroupTemplates extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupTemplates.class.getName());

    public static final String PAGE_NAME = "Notification Group Templates";
    
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
            String html = buildNotificationGroupsHtml();
            
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
            String operation = request.getParameter("Operation");

            if ((operation != null) && operation.equals("Clone")) {
                Integer id = Integer.parseInt(request.getParameter("Id"));
                cloneNotificationGroupTemplate(id);
            }

            if ((operation != null) && operation.equals("Remove")) {
                Integer id = Integer.parseInt(Common.getSingleParameterAsString(request, "Id"));
                removeNotificationGroupTemplate(id);
            }
            
            if ((operation != null) && operation.equals("Undelete")) {
                Integer id = Integer.parseInt(Common.getSingleParameterAsString(request, "Id"));
                undeleteNotificationGroupTemplate(id);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        StatsAggHtmlFramework.redirectAndGet(response, 303, "NotificationGroupTemplates");
    }
    
    private void cloneNotificationGroupTemplate(Integer notificationGroupTemplateId) {
        
        if (notificationGroupTemplateId == null) {
            return;
        }
        
        Connection connection = null;
        
        try {
            connection = DatabaseConnections.getConnection();
            NotificationGroupTemplate notificationGroupTemplate = NotificationGroupTemplatesDao.getNotificationGroupTemplate(connection, false, notificationGroupTemplateId);
            
            if ((notificationGroupTemplate != null) && (notificationGroupTemplate.getName() != null)) {
                List<NotificationGroupTemplate> allNotificationGroupTemplates = NotificationGroupTemplatesDao.getNotificationGroupTemplates(connection, true);
                
                Set<String> allNotificationGroupTemplateNames = new HashSet<>();
                for (NotificationGroupTemplate currentNotificationGroupTemplate : allNotificationGroupTemplates) {
                    if (currentNotificationGroupTemplate.getName() != null) allNotificationGroupTemplateNames.add(currentNotificationGroupTemplate.getName());
                }

                NotificationGroupTemplate clonedNotificationGroupTemplate = NotificationGroupTemplate.copy(notificationGroupTemplate);
                clonedNotificationGroupTemplate.setId(-1);
                String clonedAlterName = StatsAggHtmlFramework.createCloneName(notificationGroupTemplate.getName(), allNotificationGroupTemplateNames);
                
                clonedNotificationGroupTemplate.setName(clonedAlterName);

                NotificationGroupTemplatesDaoWrapper notificationGroupTemplatesDaoWrapper = NotificationGroupTemplatesDaoWrapper.createRecordInDatabase(clonedNotificationGroupTemplate);
                
                if ((GlobalVariables.templateInvokerThread != null) && (NotificationGroupTemplatesDaoWrapper.STATUS_CODE_SUCCESS == notificationGroupTemplatesDaoWrapper.getLastAlterRecordStatus())) {
                    logger.info("Running template routine due to notification group template clone operation");
                    GlobalVariables.templateInvokerThread.runTemplateThread();
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
    
    public String removeNotificationGroupTemplate(Integer notificationGroupTemplateId) {
        
        if (notificationGroupTemplateId == null) {
            return "Notification Group Template ID field can't be null";
        }
        
        String returnString;
        
        try {
            NotificationGroupTemplate notificationGroupTemplate = NotificationGroupTemplatesDao.getNotificationGroupTemplate(DatabaseConnections.getConnection(), true, notificationGroupTemplateId);   
            if (notificationGroupTemplate != null) notificationGroupTemplate.setIsMarkedForDelete(true);
            
            NotificationGroupTemplatesDaoWrapper notificationGroupTemplatesDaoWrapper = NotificationGroupTemplatesDaoWrapper.alterRecordInDatabase(notificationGroupTemplate);
            
            if ((GlobalVariables.templateInvokerThread != null) && (NotificationGroupTemplatesDaoWrapper.STATUS_CODE_SUCCESS == notificationGroupTemplatesDaoWrapper.getLastAlterRecordStatus())) {
                logger.info("Running template routine due to notification group template remove operation");
                GlobalVariables.templateInvokerThread.runTemplateThread();
                Threads.sleepMilliseconds(300); // sleep for 300ms to give the template thread a change to run before re-rendering the page
            }
            
            returnString = notificationGroupTemplatesDaoWrapper.getReturnString();
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            returnString = "Error removing notification group template";
        }
        
        return returnString;
    }
    
    public String undeleteNotificationGroupTemplate(Integer notificationGroupTemplateId) {
        
        if (notificationGroupTemplateId == null) {
            return "Notification Group Template ID field can't be null";
        }
        
        String returnString;
        
        try {
            NotificationGroupTemplate notificationGroupTemplate = NotificationGroupTemplatesDao.getNotificationGroupTemplate(DatabaseConnections.getConnection(), true, notificationGroupTemplateId);   
            if (notificationGroupTemplate != null) notificationGroupTemplate.setIsMarkedForDelete(false);
            
            NotificationGroupTemplatesDaoWrapper notificationGroupTemplatesDaoWrapper = NotificationGroupTemplatesDaoWrapper.alterRecordInDatabase(notificationGroupTemplate);
            
            if ((GlobalVariables.templateInvokerThread != null) && (NotificationGroupTemplatesDaoWrapper.STATUS_CODE_SUCCESS == notificationGroupTemplatesDaoWrapper.getLastAlterRecordStatus())) {
                logger.info("Running template routine due to notification group template undelete operation");
                GlobalVariables.templateInvokerThread.runTemplateThread();
                Threads.sleepMilliseconds(300); // sleep for 300ms to give the template thread a change to run before re-rendering the page
            }
            
            returnString = notificationGroupTemplatesDaoWrapper.getReturnString();
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            returnString = "Error undeleting notification group template";
        }
        
        return returnString;
    }
    
    private String buildNotificationGroupsHtml() {
        
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
            "     <a href=\"CreateNotificationGroupTemplate\" class=\"btn btn-primary statsagg_page_content_font\">Create New Notification Group Template <i class=\"fa fa-long-arrow-right\"></i></a> \n" +
            "    </div>" +   
            "  </div>" +    
            "  <table id=\"NotificationGroupTemplatesTable\" style=\"display:none\" class=\"table table-bordered table-hover \">\n" +
            "    <thead>\n" +
            "      <tr>\n" +
            "        <th>Notification Group Template Name</th>\n" + 
            "        <th>Derived Notification Groups</th>\n" +
            "        <th>Operations</th>\n" +
            "      </tr>\n" +
            "    </thead>\n" +
            "    <tbody>\n");

        Connection connection = null;
        
        try {
            connection = DatabaseConnections.getConnection();
            List<NotificationGroupTemplate> notificationGroupTemplates = NotificationGroupTemplatesDao.getNotificationGroupTemplates(connection, false);
            if (notificationGroupTemplates == null) notificationGroupTemplates = new ArrayList<>();
            DatabaseUtils.cleanup(connection);
            
            for (NotificationGroupTemplate notificationGroupTemplate : notificationGroupTemplates) {     
                if ((notificationGroupTemplate == null) || (notificationGroupTemplate.getId() == null) || (notificationGroupTemplate.getName() == null)) continue;

                List<NotificationGroup> notificationGroups = NotificationGroupsDao.getNotificationGroups_FilterByNotificationGroupTemplateId(DatabaseConnections.getConnection(), true, notificationGroupTemplate.getId());
                Set<String> notificationGroupNamesThatNotificationGroupTemplateWantsToCreate = com.pearson.statsagg.threads.template_related.Common.getNamesThatTemplateWantsToCreate(notificationGroupTemplate.getVariableSetListId(), notificationGroupTemplate.getNotificationGroupNameVariable());
                String numberOfDerivedNotificationGroups = (notificationGroups == null) ? "0" : (notificationGroups.size() + "");
                
                String rowStatusContext = "";
                if ((notificationGroupNamesThatNotificationGroupTemplateWantsToCreate != null) && (notificationGroups != null) && (notificationGroups.size() != notificationGroupNamesThatNotificationGroupTemplateWantsToCreate.size())) {
                    rowStatusContext = "class=\"danger\"";
                }
                
                String notificationGroupTemplateDetails = "<a class=\"iframe cboxElement\" href=\"NotificationGroupTemplateDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(notificationGroupTemplate.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(notificationGroupTemplate.getName()) + "</a>";

                String derivedNotificationGroupDetails = "<a class=\"iframe cboxElement\" href=\"NotificationGroupTemplate-DerivedNotificationGroups?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(notificationGroupTemplate.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(numberOfDerivedNotificationGroups) + "</a>";

                String alter = "<a href=\"CreateNotificationGroupTemplate?Operation=Alter&amp;Name=" + StatsAggHtmlFramework.urlEncode(notificationGroupTemplate.getName()) + "\">alter</a>";

                List<KeyValue<String,String>> cloneKeysAndValues = new ArrayList<>();
                cloneKeysAndValues.add(new KeyValue("Operation", "Clone"));
                cloneKeysAndValues.add(new KeyValue("Id", notificationGroupTemplate.getId().toString()));
                String clone = StatsAggHtmlFramework.buildJavaScriptPostLink("Clone_" + notificationGroupTemplate.getName(), "NotificationGroupTemplates", "clone", cloneKeysAndValues);

                List<KeyValue<String,String>> removeKeysAndValues = new ArrayList<>();
                removeKeysAndValues.add(new KeyValue("Operation", "Remove"));
                removeKeysAndValues.add(new KeyValue("Id", notificationGroupTemplate.getId().toString()));
                String remove = StatsAggHtmlFramework.buildJavaScriptPostLink("Remove_" + notificationGroupTemplate.getName(), "NotificationGroupTemplates", "remove", 
                        removeKeysAndValues, true, "Are you sure you want to remove this notification group template?");       
                
                List<KeyValue<String,String>> undeleteKeysAndValues = new ArrayList<>();
                undeleteKeysAndValues.add(new KeyValue("Operation", "Undelete"));
                undeleteKeysAndValues.add(new KeyValue("Id", notificationGroupTemplate.getId().toString()));
                String undelete = StatsAggHtmlFramework.buildJavaScriptPostLink("Undelete_" + notificationGroupTemplate.getName(), "NotificationGroupTemplates", "undelete", undeleteKeysAndValues);       
                
                htmlBodyStringBuilder
                    .append("<tr ").append(rowStatusContext).append(">\n")
                    .append("<td class=\"statsagg_force_word_break\">").append(notificationGroupTemplateDetails).append("</td>\n")
                    .append("<td class=\"statsagg_force_word_break\">").append(derivedNotificationGroupDetails).append("</td>\n")
                    .append("<td>").append(alter).append(", ").append(clone).append(", ");
                
                if ((notificationGroupTemplate.isMarkedForDelete() != null) && !notificationGroupTemplate.isMarkedForDelete()) htmlBodyStringBuilder.append(remove);
                else htmlBodyStringBuilder.append(undelete);
                    
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
