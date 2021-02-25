package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.configuration.ApplicationConfiguration;
import com.pearson.statsagg.database_objects.notification_group_templates.NotificationGroupTemplate;
import com.pearson.statsagg.database_objects.notification_group_templates.NotificationGroupTemplatesDao;
import com.pearson.statsagg.globals.DatabaseConnections;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database_objects.variable_set_list.VariableSetList;
import com.pearson.statsagg.database_objects.variable_set_list.VariableSetListsDao;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class NotificationGroupTemplateDetails extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupTemplateDetails.class.getName());
    
    public static final String PAGE_NAME = "Notification Group Template Details";
    
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
        processGetRequest(request, response);
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
        boolean excludeNavbar = StringUtilities.isStringValueBooleanTrue(request.getParameter("ExcludeNavbar"));
        String notificationGroupTemplateDetails = getNotificationGroupTemplateDetailsString(name, excludeNavbar);
                
        try {  
            StringBuilder htmlBuilder = new StringBuilder();

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            
            String htmlBody = statsAggHtmlFramework.createHtmlBody(
            "<div id=\"page-content-wrapper\">\n" +
            "  <!-- Keep all page content within the page-content inset div! -->\n" +
            "  <div class=\"page-content inset statsagg_page_content_font\">\n" +
            "    <div class=\"content-header\"> \n" +
            "      <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "    </div> " +
            "    <div class=\"statsagg_force_word_wrap\">" +
            notificationGroupTemplateDetails +
            "    </div>\n" +
            "  </div>\n" +
            "</div>\n",
            excludeNavbar);
            
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

    private String getNotificationGroupTemplateDetailsString(String notificationGroupTemplateName, boolean excludeNavbar) {
        
        if (notificationGroupTemplateName == null) {
            return "<b>No notification group template specified</b>";
        }
        
        NotificationGroupTemplate notificationGroupTemplate = NotificationGroupTemplatesDao.getNotificationGroupTemplate(DatabaseConnections.getConnection(), true, notificationGroupTemplateName);
        
        if (notificationGroupTemplate == null) {
            return "<b>Notification group template not found</b>";
        }

        StringBuilder outputString = new StringBuilder();

        VariableSetList variableSetList = null;
        if (notificationGroupTemplate.getVariableSetListId() != null) variableSetList = VariableSetListsDao.getVariableSetList(DatabaseConnections.getConnection(), true, notificationGroupTemplate.getVariableSetListId());

        outputString.append("<b>Name:</b> ");
        if (notificationGroupTemplate.getName() != null) outputString.append(StatsAggHtmlFramework.htmlEncode(notificationGroupTemplate.getName())).append("<br>");
        else outputString.append("N/A <br>");

        outputString.append("<b>ID:</b> ");
        if (notificationGroupTemplate.getName() != null) outputString.append(notificationGroupTemplate.getId()).append("<br>");
        else outputString.append("N/A <br>");

        outputString.append("<b>Variable Set List:</b> ");
        if (variableSetList != null) {
            String variableSetListDetailsPopup = "<a class=\"iframe cboxElement\" href=\"VariableSetListDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(variableSetList.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(variableSetList.getName()) + "</a>";
            outputString.append(variableSetListDetailsPopup).append("<br>");
        }
        else outputString.append("<br>");

        outputString.append("<br>");

        outputString.append("<b>Notification Group Name Variable:</b> ");
        outputString.append(StatsAggHtmlFramework.htmlEncode(notificationGroupTemplate.getNotificationGroupNameVariable())).append("<br>");

        outputString.append("<b>Email Addresses Variable:</b> ");
        if ((notificationGroupTemplate.getEmailAddressesVariable() != null) && !notificationGroupTemplate.getEmailAddressesVariable().isEmpty()) {
            outputString.append(StatsAggHtmlFramework.htmlEncode(notificationGroupTemplate.getEmailAddressesVariable())).append("<br>");
        }
        else outputString.append("<br>");

        if (ApplicationConfiguration.isPagerdutyIntegrationEnabled()) {
            outputString.append("<b>PagerDuty Service Name Variable:</b> ");
            if ((notificationGroupTemplate.getPagerdutyServiceNameVariable() != null) && !notificationGroupTemplate.getPagerdutyServiceNameVariable().isEmpty()) {
                outputString.append(StatsAggHtmlFramework.htmlEncode(notificationGroupTemplate.getPagerdutyServiceNameVariable())).append("<br>");
            }
            else outputString.append("<br>");
        }

        outputString.append("<br>");
        
        outputString.append("<b>Is marked for delete? :</b> ");
        if (notificationGroupTemplate.isMarkedForDelete() != null) outputString.append(notificationGroupTemplate.isMarkedForDelete()).append("<br>");
        else outputString.append("N/A <br>");

        return outputString.toString();
    }

}
