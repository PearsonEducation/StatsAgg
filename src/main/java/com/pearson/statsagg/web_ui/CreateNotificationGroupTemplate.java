package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.configuration.ApplicationConfiguration;
import com.pearson.statsagg.database_objects.DatabaseObjectValidation;
import com.pearson.statsagg.database_objects.notification_group_templates.NotificationGroupTemplate;
import com.pearson.statsagg.database_objects.notification_group_templates.NotificationGroupTemplatesDao;
import com.pearson.statsagg.database_objects.notification_group_templates.NotificationGroupTemplatesDaoWrapper;
import com.pearson.statsagg.database_objects.variable_set_list.VariableSetList;
import com.pearson.statsagg.database_objects.variable_set_list.VariableSetListsDao;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.globals.GlobalVariables;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.math_utils.MathUtilities;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class CreateNotificationGroupTemplate extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CreateNotificationGroupTemplate.class.getName());
    
    public static final String PAGE_NAME = "Create Notification Group Template";
    
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
            
            NotificationGroupTemplate notificationGroupTemplate = null;
            String name = request.getParameter("Name");
            if (name != null) {
                notificationGroupTemplate = NotificationGroupTemplatesDao.getNotificationGroupTemplate(DatabaseConnections.getConnection(), true, name.trim());
            }    

            String htmlBodyContents = buildCreateNotificationGroupTemplateHtml(notificationGroupTemplate);
            List<String> additionalJavascript = new ArrayList<>();
            additionalJavascript.add("js/statsagg_create_notification_group_template.js");
            String htmlBody = statsAggHtmlFramework.createHtmlBody(htmlBodyContents, additionalJavascript, false);
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
            String result = parseAndAlterNotificationGroupTemplate(request);

            StringBuilder htmlBuilder = new StringBuilder();
            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            String htmlBodyContent = statsAggHtmlFramework.buildHtmlBodyForPostResult(PAGE_NAME, StatsAggHtmlFramework.htmlEncode(result), "NotificationGroupTemplates", NotificationGroupTemplates.PAGE_NAME);
            String htmlBody = statsAggHtmlFramework.createHtmlBody(htmlBodyContent);
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
    
    private String buildCreateNotificationGroupTemplateHtml(NotificationGroupTemplate notificationGroupTemplate) {

        StringBuilder htmlBody = new StringBuilder();

        htmlBody.append(
            "<div id=\"page-content-wrapper\">\n" +
            " <!-- Keep all page content within the page-content inset div! -->\n" +
            "   <div class=\"page-content inset statsagg_page_content_font\">\n" +
            "     <div class=\"content-header\"> \n" +
            "       <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "     </div> " +
            "     <form action=\"CreateNotificationGroupTemplate\" method=\"POST\">\n");
        
        if ((notificationGroupTemplate != null) && (notificationGroupTemplate.getName() != null) && !notificationGroupTemplate.getName().isEmpty()) {
            htmlBody.append("<input type=\"hidden\" name=\"Old_Name\" value=\"").append(StatsAggHtmlFramework.htmlEncode(notificationGroupTemplate.getName(), true)).append("\">");
        }
        
        
        // notification group template name
        htmlBody.append(
            "       <div class=\"form-group\">\n" +
            "         <label class=\"label_small_margin\">Notification Group Template Name</label>\n" +
            "         <button type=\"button\" id=\"Name_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"A unique name for this notification group template.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "         <input class=\"form-control-statsagg\" name=\"Name\" id=\"Name\" ");

        if ((notificationGroupTemplate != null) && (notificationGroupTemplate.getName() != null)) {
            htmlBody.append("value=\"").append(StatsAggHtmlFramework.htmlEncode(notificationGroupTemplate.getName(), true)).append("\"");
        }

        htmlBody.append(">\n</div>\n");
                    
        
        // variable set list name
        htmlBody.append(
            "<div class=\"form-group\" id=\"VariableSetListName_Lookup\">\n" +
            "  <label class=\"label_small_margin\">Variable Set List</label>\n" +
            "  <button type=\"button\" id=\"VariableSetListName_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"The exact name of the variable set list to associate with this notification group template.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <input class=\"typeahead form-control-statsagg\" autocomplete=\"off\" name=\"VariableSetListName\" id=\"VariableSetListName\" ");

        if ((notificationGroupTemplate != null) && (notificationGroupTemplate.getVariableSetListId() != null)) {
            VariableSetList variableSetList = VariableSetListsDao.getVariableSetList(DatabaseConnections.getConnection(), true, notificationGroupTemplate.getVariableSetListId());

            if ((variableSetList != null) && (variableSetList.getName() != null)) {
                htmlBody.append(" value=\"").append(StatsAggHtmlFramework.htmlEncode(variableSetList.getName(), true)).append("\"");
            }
        }
        
        htmlBody.append(">\n</div>\n");
        
        
        // notification group name variable
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Notification Group Name Variable</label>\n" +
            "  <button type=\"button\" id=\"NotificationGroupNameVariable_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"A templated notification group name. Variable values are substituted using ```key```.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <input class=\"form-control-statsagg\" name=\"NotificationGroupNameVariable\" id=\"NotificationGroupNameVariable\" ");

        if ((notificationGroupTemplate != null) && (notificationGroupTemplate.getNotificationGroupNameVariable() != null)) {
            htmlBody.append(" value=\"").append(StatsAggHtmlFramework.htmlEncode(notificationGroupTemplate.getNotificationGroupNameVariable(), true)).append("\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // email addresses variable
        htmlBody.append(
            "       <div class=\"form-group\">\n" +
            "         <label class=\"label_small_margin\">Email Addresses Variable</label>\n" +
            "         <button type=\"button\" id=\"EmailAddressesVariable_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"A templated csv of email addresses. Variable values are substituted using ```key```.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "         <input class=\"form-control-statsagg\" name=\"EmailAddressesVariable\" id=\"EmailAddressesVariable\" ");

        if ((notificationGroupTemplate != null) && (notificationGroupTemplate.getEmailAddressesVariable() != null)) {
            htmlBody.append("value=\"").append(StatsAggHtmlFramework.htmlEncode(notificationGroupTemplate.getEmailAddressesVariable(), true)).append("\"");
        }
        
        htmlBody.append(">\n</div>\n");
              
        
        // pagerduty service name variable
        if (ApplicationConfiguration.isPagerdutyIntegrationEnabled()) {
            htmlBody.append(
                "       <div class=\"form-group\">\n" +
                "         <label class=\"label_small_margin\">PagerDuty Service Name Variable</label>\n" +
                "         <button type=\"button\" id=\"PagerDutyServiceNameVariable_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"A templated PagerDuty Service name. Variable values are substituted using ```key```.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
                "         <input class=\"form-control-statsagg\" name=\"PagerDutyServiceNameVariable\" id=\"PagerDutyServiceNameVariable\" ");

            if ((notificationGroupTemplate != null) && (notificationGroupTemplate.getPagerdutyServiceNameVariable() != null)) {
                htmlBody.append("value=\"").append(StatsAggHtmlFramework.htmlEncode(notificationGroupTemplate.getPagerdutyServiceNameVariable(), true)).append("\"");
            }

            htmlBody.append(">\n</div>\n");
        }
               
        
        htmlBody.append(
            "       <button type=\"submit\" class=\"btn btn-default btn-primary statsagg_button_no_shadow statsagg_page_content_font\">Submit</button>" +
            "&nbsp;&nbsp;&nbsp;" +
            "       <a href=\"NotificationGroupTemplates\" class=\"btn btn-default statsagg_page_content_font\" role=\"button\">Cancel</a>" +
            "     </form>\n"       +          
            "   </div>\n" +
            "</div>\n");
            
        return htmlBody.toString();
    }
    
    public String parseAndAlterNotificationGroupTemplate(Object request) {
        
        if (request == null) {
            return null;
        }
        
        String returnString;
        
        NotificationGroupTemplate notificationGroupTemplate = getNotificationGroupTemplateFromNotificationGroupTemplateParameters(request);
        
        Boolean isMarkedForDelete = false;
        String oldName = Common.getSingleParameterAsString(request, "Old_Name");
        if (oldName == null) oldName = Common.getSingleParameterAsString(request, "old_name");
        if (oldName == null) {
            String id = Common.getSingleParameterAsString(request, "Id");
            if (id == null) id = Common.getSingleParameterAsString(request, "id");
            
            if (id != null) {
                try {
                    Integer id_Integer = Integer.parseInt(id.trim());
                    NotificationGroupTemplate oldNotificationGroupTemplate = NotificationGroupTemplatesDao.getNotificationGroupTemplate(DatabaseConnections.getConnection(), true, id_Integer);
                    oldName = oldNotificationGroupTemplate.getName();
                    isMarkedForDelete = oldNotificationGroupTemplate.isMarkedForDelete();
                }
                catch (Exception e){}
            }
        }
        
        if (notificationGroupTemplate == null) {
            returnString = "Failed to create or alter notification group template. Reason=\"One or more invalid notification group template fields detected\".";
            logger.warn(returnString);
        } 
        else {
            notificationGroupTemplate.setIsMarkedForDelete(isMarkedForDelete);
            DatabaseObjectValidation databaseObjectValidation = NotificationGroupTemplate.isValid(notificationGroupTemplate);

            if (!databaseObjectValidation.isValid()) {
                returnString = "Failed to create or alter notification group template. Reason=\"" + databaseObjectValidation.getReason() + "\".";
                logger.warn(returnString);
            }
            else {
                NotificationGroupTemplatesDaoWrapper notificationGroupTemplatesDaoWrapper = NotificationGroupTemplatesDaoWrapper.alterRecordInDatabase(notificationGroupTemplate, oldName);
                returnString = notificationGroupTemplatesDaoWrapper.getReturnString();

                if ((GlobalVariables.templateInvokerThread != null) && (NotificationGroupTemplatesDaoWrapper.STATUS_CODE_SUCCESS == notificationGroupTemplatesDaoWrapper.getLastAlterRecordStatus())) {
                    logger.info("Running notification group template routine due to notification group template create or alter operation");
                    GlobalVariables.templateInvokerThread.runTemplateThread();
                }
                else logger.warn(returnString);
            }
        }
        
        return returnString;
    }
    
    private NotificationGroupTemplate getNotificationGroupTemplateFromNotificationGroupTemplateParameters(Object request) {
        
        if (request == null) {
            return null;
        }
        
        boolean didEncounterError = false;
        
        NotificationGroupTemplate notificationGroupTemplate = new NotificationGroupTemplate();

        try {
            String parameter;

            parameter = Common.getSingleParameterAsString(request, "Name");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "name");
            String trimmedName = parameter.trim();
            notificationGroupTemplate.setName(trimmedName);
            if ((notificationGroupTemplate.getName() == null) || notificationGroupTemplate.getName().isEmpty()) didEncounterError = true;

            parameter = Common.getSingleParameterAsString(request, "VariableSetListName");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "variable_set_list_name");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {    
                    VariableSetList variableSetList = VariableSetListsDao.getVariableSetList(DatabaseConnections.getConnection(), true, parameterTrimmed);
                    if (variableSetList != null) notificationGroupTemplate.setVariableSetListId(variableSetList.getId());
                }
            }
            else {
                parameter = Common.getSingleParameterAsString(request, "VariableSetListId");
                if (parameter == null) parameter = Common.getSingleParameterAsString(request, "variable_set_list_id");
                if (parameter != null) {
                    String parameterTrimmed = parameter.trim();
                    if (!parameterTrimmed.isEmpty() && MathUtilities.isStringAnInteger(parameterTrimmed)) notificationGroupTemplate.setVariableSetListId(Integer.parseInt(parameterTrimmed));
                }
            }
            
            parameter = Common.getSingleParameterAsString(request, "NotificationGroupNameVariable");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "notification_group_name_variable");
            String trimmedNotificationGroupNameVariable = (parameter != null) ? parameter.trim() : "";
            notificationGroupTemplate.setNotificationGroupNameVariable(trimmedNotificationGroupNameVariable);
            
            parameter = Common.getSingleParameterAsString(request, "EmailAddressesVariable");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "email_addresses_variable");
            String trimmedEmailAddressesVariable = (parameter != null) ? parameter.trim() : "";
            notificationGroupTemplate.setEmailAddressesVariable(trimmedEmailAddressesVariable);
            
            parameter = Common.getSingleParameterAsString(request, "PagerDutyServiceNameVariable");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "pagerduty_service_name_variable");
            String trimmedPagerdutyServiceNameVariable = (parameter != null) ? parameter.trim() : "";
            notificationGroupTemplate.setPagerdutyServiceNameVariable(trimmedPagerdutyServiceNameVariable);
        }
        catch (Exception e) {
            didEncounterError = true;
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
            
        if (didEncounterError) {
            notificationGroupTemplate = null;
        }
        
        return notificationGroupTemplate;
    }

}
