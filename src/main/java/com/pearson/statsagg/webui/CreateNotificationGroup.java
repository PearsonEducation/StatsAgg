package com.pearson.statsagg.webui;

import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database.notifications.NotificationGroup;
import com.pearson.statsagg.database.notifications.NotificationGroupsDao;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.utilities.StackTrace;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "CreateNotificationGroup", urlPatterns = {"/CreateNotificationGroup"})
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
        
        response.setContentType("text/html");
        PrintWriter out = null;
    
        try {  
            StringBuilder htmlBuilder = new StringBuilder("");

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            
            NotificationGroup notificationGroup = null;
            String name = request.getParameter("Name");
            if (name != null) {
                NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
                notificationGroup = notificationGroupsDao.getNotificationGroupByName(name.trim());
            }    

            String htmlBodyContents = buildCreateNotificationGroupHtml(notificationGroup);
            String htmlBody = statsAggHtmlFramework.createHtmlBody(htmlBodyContents);
            htmlBuilder.append("<!DOCTYPE html>\n<html>\n").append(htmlHeader).append(htmlBody).append("</html>");
            
            Document htmlDocument = Jsoup.parse(htmlBuilder.toString());
            String htmlFormatted  = htmlDocument.toString();
            out = response.getWriter();
            out.println(htmlFormatted);
            //if (ApplicationConfiguration.isDebugModeEnabled()) out.println(htmlBuilder.toString());
            //else out.println(htmlFormatted);
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
        
        PrintWriter out = null;
        
        try {
            String result = parseAndAlterNotificationGroup(request);
            
            response.setContentType("text/html");     
            
            StringBuilder htmlBuilder = new StringBuilder("");
            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            String htmlBodyContent = statsAggHtmlFramework.buildHtmlBodyForPostResult(PAGE_NAME, StatsAggHtmlFramework.htmlEncode(result), "NotificationGroups", NotificationGroups.PAGE_NAME);
            String htmlBody = statsAggHtmlFramework.createHtmlBody(htmlBodyContent);
            htmlBuilder.append("<!DOCTYPE html>\n<html>\n").append(htmlHeader).append(htmlBody).append("</html>");
            
            Document htmlDocument = Jsoup.parse(htmlBuilder.toString());
            String htmlFormatted  = htmlDocument.toString();
            out = response.getWriter();
            out.println(htmlFormatted);
            //if (ApplicationConfiguration.isDebugModeEnabled()) out.println(htmlBuilder.toString());
            //else out.println(htmlFormatted);
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

        StringBuilder htmlBody = new StringBuilder("");

        htmlBody.append(
            "<div id=\"page-content-wrapper\">\n" +
            " <!-- Keep all page content within the page-content inset div! -->\n" +
            "   <div class=\"page-content inset\" style=\"font-size:12px;\">\n" +
            "     <div class=\"content-header\"> \n" +
            "       <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "     </div> " +
            "     <form action=\"CreateNotificationGroup\" method=\"POST\">\n");
        
        if ((notificationGroup != null) && (notificationGroup.getName() != null) && !notificationGroup.getName().isEmpty()) {
            htmlBody.append("<input type=\"hidden\" name=\"Old_Name\" value=\"").append(Encode.forHtmlAttribute(notificationGroup.getName())).append("\">");
        }
        
        htmlBody.append(
            "       <div class=\"form-group\">\n" +
            "         <label class=\"label_small_margin\">Notification Group Name</label>\n" +
            "         <input class=\"form-control-statsagg\" placeholder=\"Enter a unique name for this notification group.\" name=\"Name\" id=\"Name\" ");

        if ((notificationGroup != null) && (notificationGroup.getName() != null)) {
            htmlBody.append("value=\"").append(Encode.forHtmlAttribute(notificationGroup.getName())).append("\"");
        }

        htmlBody.append(
            ">\n       </div>\n" +
            "       <div class=\"form-group\">\n" +
            "         <label class=\"label_small_margin\">Email addresses</label>\n" +
            "         <input class=\"form-control-statsagg\" placeholder=\"Enter a csv-delimited list of email addresses\" name=\"EmailAddresses\" id=\"EmailAddresses\" ");

        if ((notificationGroup != null) && (notificationGroup.getEmailAddresses() != null)) {
            htmlBody.append("value=\"").append(Encode.forHtmlAttribute(notificationGroup.getEmailAddresses())).append("\"");
        }

        htmlBody.append(
            ">\n</div>\n" +
            "       <button type=\"submit\" class=\"btn btn-default btn-primary statsagg_button_no_shadow\">Submit</button>" +
            "&nbsp;&nbsp;&nbsp;" +
            "       <a href=\"NotificationGroups\" class=\"btn btn-default\" role=\"button\">Cancel</a>" +
            "     </form>\n"       +          
            "   </div>\n" +
            "</div>\n");
            
        return htmlBody.toString();
    }
    
    private String parseAndAlterNotificationGroup(HttpServletRequest request) {
        
        if (request == null) {
            return null;
        }
        
        String returnString;
        
        NotificationGroup notificationGroup = getNotificationGroupFromNotificationGroupParameters(request);
        String oldName = request.getParameter("Old_Name");
        
        // insert/update/delete records in the database
        if ((notificationGroup != null) && (notificationGroup.getName() != null)) {
            NotificationGroupsLogic notificationGroupsLogic = new NotificationGroupsLogic();
            returnString = notificationGroupsLogic.alterRecordInDatabase(notificationGroup, oldName);
        }
        else {
            returnString = "Failed to add notification group. Reason=\"Field validation failed.\"";
            logger.warn(returnString);
        }
        
        return returnString;
    }
    
    private NotificationGroup getNotificationGroupFromNotificationGroupParameters(HttpServletRequest request) {
        
        if (request == null) {
            return null;
        }
        
        boolean didEncounterError = false;
        
        NotificationGroup notificationGroup = new NotificationGroup();

        try {
            String parameter;

            parameter = request.getParameter("Name");
            String trimmedName = parameter.trim();
            notificationGroup.setName(trimmedName);
            notificationGroup.setUppercaseName(trimmedName.toUpperCase());
            if ((notificationGroup.getName() == null) || notificationGroup.getName().isEmpty()) didEncounterError = true;

            parameter = request.getParameter("EmailAddresses");
            if (parameter != null) notificationGroup.setEmailAddresses(parameter.trim());
            else notificationGroup.setEmailAddresses("");
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

}
