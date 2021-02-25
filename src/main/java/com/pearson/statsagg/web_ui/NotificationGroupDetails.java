package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.configuration.ApplicationConfiguration;
import com.pearson.statsagg.database_objects.notification_group_templates.NotificationGroupTemplate;
import com.pearson.statsagg.database_objects.notification_group_templates.NotificationGroupTemplatesDao;
import com.pearson.statsagg.globals.DatabaseConnections;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroup;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroupsDao;
import com.pearson.statsagg.database_objects.pagerduty_services.PagerdutyService;
import com.pearson.statsagg.database_objects.pagerduty_services.PagerdutyServicesDao;
import com.pearson.statsagg.database_objects.variable_set.VariableSet;
import com.pearson.statsagg.database_objects.variable_set.VariableSetsDao;
import com.pearson.statsagg.utilities.web_utils.EmailUtils;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import java.sql.Connection;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jeffrey Schmidt
 */
public class NotificationGroupDetails extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupDetails.class.getName());
    
    public static final String PAGE_NAME = "Notification Group Details";
    
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
        String notificationGroupDetails = getNotificationDetailsString(name, excludeNavbar);
                
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
            notificationGroupDetails +
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

    private String getNotificationDetailsString(String notificationGroupName, boolean excludeNavbar) {
        
        if (notificationGroupName == null) {
            return "<b>No notification group specified</b>";
        }
        
        NotificationGroup notificationGroup = NotificationGroupsDao.getNotificationGroup(DatabaseConnections.getConnection(), true, notificationGroupName);
        
        if (notificationGroup == null) {
            return "<b>Notification group not found</b>";
        }
        
        Connection connection = DatabaseConnections.getConnection();
        NotificationGroupTemplate notificationGroupTemplate = null;
        VariableSet variableSet = null;
        if (notificationGroup.getNotificationGroupTemplateId() != null) notificationGroupTemplate = NotificationGroupTemplatesDao.getNotificationGroupTemplate(connection, false, notificationGroup.getNotificationGroupTemplateId());
        if (notificationGroup.getVariableSetId() != null) variableSet = VariableSetsDao.getVariableSet(connection, false, notificationGroup.getVariableSetId());
        DatabaseUtils.cleanup(connection);
        
        StringBuilder outputString = new StringBuilder();

        outputString.append("<b>Name:</b> ").append(StatsAggHtmlFramework.htmlEncode(notificationGroup.getName())).append("<br>");

        outputString.append("<b>ID:</b> ").append(notificationGroup.getId()).append("<br><br>");

        if (notificationGroupTemplate != null) {
            outputString.append("<b>Notification Group Template:</b> ");
            if (notificationGroupTemplate.getName() != null) {
                String notificationGroupTemplateDetailsPopup = "<a class=\"iframe cboxElement\" href=\"NotificationGroupTemplateDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(notificationGroupTemplate.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(notificationGroupTemplate.getName()) + "</a>";
                outputString.append(notificationGroupTemplateDetailsPopup).append("<br>");
            }
            else outputString.append("<br>");
        }

        if (variableSet != null) {
            outputString.append("<b>Variable Set:</b> ");
            if (variableSet.getName() != null) {
                String variableSetDetailsPopup = "<a class=\"iframe cboxElement\" href=\"VariableSetDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(variableSet.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(variableSet.getName()) + "</a>";
                outputString.append(variableSetDetailsPopup).append("<br>");
            }
            else outputString.append("<br>");
        }

        outputString.append("<b>Email Addresses:</b> ");
        if (notificationGroup.getEmailAddresses() != null) outputString.append(StatsAggHtmlFramework.htmlEncode(notificationGroup.getEmailAddressesCsv())).append("<br>");
        else outputString.append("N/A <br>");

        if (ApplicationConfiguration.isPagerdutyIntegrationEnabled()) {
            PagerdutyService pagerdutyService = PagerdutyServicesDao.getPagerdutyService(DatabaseConnections.getConnection(), true, notificationGroup.getPagerdutyServiceId());
            String pagerdutyServiceName = "";
            if ((pagerdutyService != null) && (pagerdutyService.getName() != null)) pagerdutyServiceName = pagerdutyService.getName();

            outputString.append("<b>PagerDuty Service Name:</b> ");
            if (!pagerdutyServiceName.isEmpty()) outputString.append("<a class=\"iframe cboxElement\" href=\"PagerDutyServiceDetails?ExcludeNavbar=" + excludeNavbar + "&amp;Name=" + StatsAggHtmlFramework.urlEncode(pagerdutyServiceName) + "\">" + StatsAggHtmlFramework.htmlEncode(pagerdutyServiceName) + "</a>");
            else outputString.append("N/A");

            outputString.append("<br>");
        }

        outputString.append("<br>");

        List<String> emailAddresses = notificationGroup.getEmailAddressesList();
        for (String emailAddress : emailAddresses) {
            String trimmedEmailAddress = emailAddress.trim();
            boolean isValidEmailAddress = EmailUtils.isValidEmailAddress(trimmedEmailAddress);
            outputString.append("<b>Is \"").append(StatsAggHtmlFramework.htmlEncode(trimmedEmailAddress)).
                    append("\" a valid email address? :</b> ").append(isValidEmailAddress).append("<br>");
        }

        return outputString.toString();
    }

}
