package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.database_objects.notification_groups.NotificationGroupsDaoWrapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pearson.statsagg.configuration.ApplicationConfiguration;
import com.pearson.statsagg.database_objects.DatabaseObjectValidation;
import com.pearson.statsagg.globals.DatabaseConnections;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroup;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroupsDao;
import com.pearson.statsagg.database_objects.pagerduty_services.PagerdutyService;
import com.pearson.statsagg.database_objects.pagerduty_services.PagerdutyServicesDao;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class CreateNotificationGroup extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CreateNotificationGroup.class.getName());
    
    public static final String PAGE_NAME = "Create Notification Group";
    
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
            
            NotificationGroup notificationGroup = null;
            String name = request.getParameter("Name");
            if (name != null) {
                notificationGroup = NotificationGroupsDao.getNotificationGroup(DatabaseConnections.getConnection(), true, name.trim());
            }    

            String htmlBodyContents = buildCreateNotificationGroupHtml(notificationGroup);
            List<String> additionalJavascript = new ArrayList<>();
            additionalJavascript.add("js/statsagg_create_notification_group.js");
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
            String result = parseAndAlterNotificationGroup(request);

            StringBuilder htmlBuilder = new StringBuilder();
            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            String htmlBodyContent = statsAggHtmlFramework.buildHtmlBodyForPostResult(PAGE_NAME, StatsAggHtmlFramework.htmlEncode(result), "NotificationGroups", NotificationGroups.PAGE_NAME);
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
    
    private String buildCreateNotificationGroupHtml(NotificationGroup notificationGroup) {

        StringBuilder htmlBody = new StringBuilder();

        htmlBody.append(
            "<div id=\"page-content-wrapper\">\n" +
            " <!-- Keep all page content within the page-content inset div! -->\n" +
            "   <div class=\"page-content inset statsagg_page_content_font\">\n" +
            "     <div class=\"content-header\"> \n" +
            "       <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "     </div> " +
            "     <form action=\"CreateNotificationGroup\" method=\"POST\">\n");
        
        if ((notificationGroup != null) && (notificationGroup.getName() != null) && !notificationGroup.getName().isEmpty()) {
            htmlBody.append("<input type=\"hidden\" name=\"Old_Name\" value=\"").append(StatsAggHtmlFramework.htmlEncode(notificationGroup.getName(), true)).append("\">");
        }
        
        
        // name
        htmlBody.append(
            "       <div class=\"form-group\">\n" +
            "         <label class=\"label_small_margin\">Notification Group Name</label>\n" +
            "         <input class=\"form-control-statsagg\" placeholder=\"Enter a unique name for this notification group.\" name=\"Name\" id=\"Name\" ");

        if ((notificationGroup != null) && (notificationGroup.getName() != null)) {
            htmlBody.append("value=\"").append(StatsAggHtmlFramework.htmlEncode(notificationGroup.getName(), true)).append("\"");
        }

        htmlBody.append(">\n</div>\n");
                    
        
        // email addresses
        htmlBody.append(
            "       <div class=\"form-group\">\n" +
            "         <label class=\"label_small_margin\">Email Addresses</label>\n" +
            "         <input class=\"form-control-statsagg\" placeholder=\"A csv-delimited list of email addresses\" name=\"EmailAddresses\" id=\"EmailAddresses\" ");

        if ((notificationGroup != null) && (notificationGroup.getEmailAddresses() != null)) {
            htmlBody.append("value=\"").append(StatsAggHtmlFramework.htmlEncode(notificationGroup.getEmailAddresses(), true)).append("\"");
        }
        
        htmlBody.append(">\n</div>\n");
              
        
        // pagerduty service name field
        if (ApplicationConfiguration.isPagerdutyIntegrationEnabled()) {
            htmlBody.append(
                "       <div class=\"form-group\" id=\"PagerDutyServiceName_Lookup\">\n" +
                "         <label class=\"label_small_margin\">PagerDuty Service Name</label>\n" +
                "         <input class=\"typeahead form-control-statsagg\" autocomplete=\"off\" placeholder=\"The exact PagerDuty Service name to send alerts to.\" name=\"PagerDutyServiceName\" id=\"PagerDutyServiceName\" ");

            if ((notificationGroup != null) && (notificationGroup.getPagerdutyServiceId() != null)) {
                PagerdutyService pagerdutyService = PagerdutyServicesDao.getPagerdutyService(DatabaseConnections.getConnection(), true, notificationGroup.getPagerdutyServiceId());
                if (pagerdutyService != null) htmlBody.append("value=\"").append(StatsAggHtmlFramework.htmlEncode(pagerdutyService.getName(), true)).append("\"");
            }

            htmlBody.append(">\n</div>\n");
        }
               
        
        htmlBody.append(
            "       <button type=\"submit\" class=\"btn btn-default btn-primary statsagg_button_no_shadow statsagg_page_content_font\">Submit</button>" +
            "&nbsp;&nbsp;&nbsp;" +
            "       <a href=\"NotificationGroups\" class=\"btn btn-default statsagg_page_content_font\" role=\"button\">Cancel</a>" +
            "     </form>\n"       +          
            "   </div>\n" +
            "</div>\n");
            
        return htmlBody.toString();
    }
    
    public String parseAndAlterNotificationGroup(Object request) {
        
        if (request == null) {
            return null;
        }
        
        String returnString;
        
        NotificationGroup notificationGroup = getNotificationGroupFromNotificationGroupParameters(request);
        boolean isValidPagerdutyServiceValue = isValidPagerdutyService(request);
        String oldName = getOldNotificationGroupName(request);
        
        boolean isNotificationGroupCreatedByNotificationGroupTemplate = NotificationGroupsDao.isNotificationGroupCreatedByNotificationGroupTemplate(DatabaseConnections.getConnection(), true, notificationGroup, oldName);

        // insert/update/delete records in the database
        if (notificationGroup == null) {
            returnString = "Failed to create or alter notification group. Reason=\"One or more invalid notification group fields detected\".";
            logger.warn(returnString);
        } 
        else if (isNotificationGroupCreatedByNotificationGroupTemplate) {
            returnString = "Failed to create or alter notification group. Reason=\"Cannot alter a notification group that was created by a notification group template\".";
            logger.warn(returnString);
        }
        else if (!isValidPagerdutyServiceValue) {
            returnString = "Failed to create or alter notification group. Reason=\"Invalid PagerDuty service name.\"";
            logger.warn(returnString);
        }
        else {
            DatabaseObjectValidation databaseObjectValidation = NotificationGroup.isValid(notificationGroup);

            if (!databaseObjectValidation.isValid()) {
                returnString = "Failed to create or alter notification group. Reason=\"" + databaseObjectValidation.getReason() + "\".";
                logger.warn(returnString);
            }
            else {
                NotificationGroupsDaoWrapper notificationGroupsDaoWrapper = NotificationGroupsDaoWrapper.alterRecordInDatabase(notificationGroup, oldName);
                returnString = notificationGroupsDaoWrapper.getReturnString();
            }
        }
               
        return returnString;
    }
    
    protected static String getOldNotificationGroupName(Object request) {
        
        try {
            if (request == null) return null;

            String oldName = Common.getSingleParameterAsString(request, "Old_Name");
            if (oldName == null) oldName = Common.getSingleParameterAsString(request, "old_name");

            if (oldName == null) {
                String id = Common.getSingleParameterAsString(request, "Id");
                if (id == null) id = Common.getSingleParameterAsString(request, "id");

                if (id != null) {
                    try {
                        Integer id_Integer = Integer.parseInt(id.trim());
                        NotificationGroup oldNotificationGroup = NotificationGroupsDao.getNotificationGroup(DatabaseConnections.getConnection(), true, id_Integer);
                        oldName = oldNotificationGroup.getName();
                    }
                    catch (Exception e){}
                }
            }

            return oldName;
        }
        catch (Exception e){
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        
    }
    
    private NotificationGroup getNotificationGroupFromNotificationGroupParameters(Object request) {
        
        if (request == null) {
            return null;
        }
        
        boolean didEncounterError = false;
        
        NotificationGroup notificationGroup = new NotificationGroup();

        try {
            String parameter;

            parameter = Common.getSingleParameterAsString(request, "Name");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "name");
            String trimmedName = parameter.trim();
            notificationGroup.setName(trimmedName);
            if ((notificationGroup.getName() == null) || notificationGroup.getName().isEmpty()) didEncounterError = true;

            parameter = Common.getSingleParameterAsString(request, "EmailAddresses");
            if (parameter != null) {
                String trimmedParameter = parameter.trim();
                String emailAddresses;
                if (trimmedParameter.length() > 65535) emailAddresses = trimmedParameter.substring(0, 65534);
                else emailAddresses = trimmedParameter;
                notificationGroup.setEmailAddresses(emailAddresses);
            }
            else if (request instanceof JsonObject) {
                JsonObject jsonObject = (JsonObject) request;
                JsonArray jsonArray = jsonObject.getAsJsonArray("email_addresses");
                if (jsonArray != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (JsonElement jsonElement : jsonArray) stringBuilder.append(jsonElement.getAsString()).append(", ");
                    String emailsCsv = StringUtils.removeEnd(stringBuilder.toString(), ", ").trim();
                    notificationGroup.setEmailAddresses(emailsCsv);
                }
                else notificationGroup.setEmailAddresses("");
            }
            else notificationGroup.setEmailAddresses("");
            
            if (ApplicationConfiguration.isPagerdutyIntegrationEnabled()) {
                parameter = Common.getSingleParameterAsString(request, "PagerDutyServiceName");
                if (parameter == null) parameter = Common.getSingleParameterAsString(request, "pagerduty_service_name");
                if (parameter != null) {
                    String parameterTrimmed = parameter.trim();
                    if (!parameterTrimmed.isEmpty()) {
                        PagerdutyService pagerdutyService = PagerdutyServicesDao.getPagerdutyService(DatabaseConnections.getConnection(), true, parameterTrimmed);
                        if ((pagerdutyService != null) && (pagerdutyService.getId() != null)) notificationGroup.setPagerdutyServiceId(pagerdutyService.getId());
                    }
                }
                else {
                    parameter = Common.getSingleParameterAsString(request, "PagerDutyServiceId");
                    if (parameter == null) parameter = Common.getSingleParameterAsString(request, "pagerduty_service_id");
                    if (parameter != null) {
                        String parameterTrimmed = parameter.trim();
                        if (!parameterTrimmed.isEmpty()) notificationGroup.setPagerdutyServiceId(Integer.parseInt(parameterTrimmed));
                    }
                }
            }
            else notificationGroup.setPagerdutyServiceId(null);
        }
        catch (Exception e) {
            didEncounterError = true;
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
            
        if (didEncounterError) {
            notificationGroup = null;
        }
        
        return notificationGroup;
    }
    
    private static boolean isValidPagerdutyService(Object request) {
        
        if (!ApplicationConfiguration.isPagerdutyIntegrationEnabled()) {
            return true;
        }
        
        try {
            String parameter = Common.getSingleParameterAsString(request, "PagerDutyServiceName");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "pagerduty_service_name");

            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {
                    PagerdutyService pagerdutyService = PagerdutyServicesDao.getPagerdutyService(DatabaseConnections.getConnection(), true, parameterTrimmed);
                    if ((pagerdutyService == null) || (pagerdutyService.getId() == null)) return false;
                }
            }
            else {
                parameter = Common.getSingleParameterAsString(request, "PagerDutyServiceId");
                if (parameter == null) parameter = Common.getSingleParameterAsString(request, "pagerduty_service_id");
                
                if (parameter != null) {
                    String parameterTrimmed = parameter.trim();
                    if (!parameterTrimmed.isEmpty()) {
                        int pagerdutyServiceId = Integer.parseInt(parameterTrimmed);
                        PagerdutyService pagerdutyService = PagerdutyServicesDao.getPagerdutyService(DatabaseConnections.getConnection(), true, pagerdutyServiceId);
                        if (pagerdutyService == null) return false;
                    }
                }
            }
            
            return true;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return false;
        }

    }

}
