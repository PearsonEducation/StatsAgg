package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.database_objects.DatabaseObjectStatus;
import com.pearson.statsagg.database_objects.DatabaseObjectValidation;
import com.pearson.statsagg.database_objects.notification_group_templates.NotificationGroupTemplate;
import com.pearson.statsagg.database_objects.notification_group_templates.NotificationGroupTemplatesDao;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroup;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroupsDao;
import com.pearson.statsagg.database_objects.pagerduty_services.PagerdutyService;
import com.pearson.statsagg.database_objects.pagerduty_services.PagerdutyServicesDao;
import com.pearson.statsagg.database_objects.variable_set.VariableSet;
import com.pearson.statsagg.database_objects.variable_set.VariableSetsDao;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.threads.template_related.TemplateThread;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class NotificationGroupTemplate_DerivedNotificationGroups extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupTemplate_DerivedNotificationGroups.class.getName());
    
    public static final String PAGE_NAME = "Notification Group Template - Derived Notification Groups";
    
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
        String notificationGroupTemplate_DerivedNotificationGroups = getNotificationGroupTemplate_DerivedNotificationGroups(name, excludeNavbar);
                
        try {  
            StringBuilder htmlBuilder = new StringBuilder();

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            
            String htmlBody = statsAggHtmlFramework.createHtmlBody(
            "<div id=\"page-content-wrapper\">\n" +
            "<!-- Keep all page content within the page-content inset div! -->\n" +
            "  <div class=\"page-content inset statsagg_page_content_font\">\n" +
            "    <div class=\"content-header\"> \n" +
            "      <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "    </div> " +
            "    <div class=\"statsagg_force_word_wrap\">" +
            notificationGroupTemplate_DerivedNotificationGroups +
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

    private String getNotificationGroupTemplate_DerivedNotificationGroups(String notificationGroupTemplateName, boolean excludeNavbar) {
        
        if (notificationGroupTemplateName == null) {
            return "<b>No notification group template specified</b>";
        }
        
        StringBuilder outputString = new StringBuilder();
        
        Connection connection = null;
        
        try {
            connection = DatabaseConnections.getConnection();
            
            Map<String,String> notificationGroupStrings = new HashMap<>();

            NotificationGroupTemplate notificationGroupTemplate = NotificationGroupTemplatesDao.getNotificationGroupTemplate(connection, false, notificationGroupTemplateName);
            if ((notificationGroupTemplate == null) || (notificationGroupTemplate.getId() == null)) return "<b>Notification group template not found</b>";

            Map<String,NotificationGroup> notificationGroups_ByUppercaseName = NotificationGroupsDao.getNotificationGroups_ByUppercaseName(connection, false);
            if (notificationGroups_ByUppercaseName == null) return "<b>Error retrieving notification groups</b>";

            Map<Integer,String> notificationGroupNamesThatNotificationGroupTemplateWantsToCreate_ByVariableSetId = com.pearson.statsagg.threads.template_related.Common.getNamesThatTemplateWantsToCreate_ByVariableSetId(notificationGroupTemplate.getVariableSetListId(), notificationGroupTemplate.getNotificationGroupNameVariable());
            if (notificationGroupNamesThatNotificationGroupTemplateWantsToCreate_ByVariableSetId == null) return "<b>Error retrieving desired derived notification groups</b>";

            outputString.append("<b>Notification Group Template Name</b> = ").append(StatsAggHtmlFramework.htmlEncode(notificationGroupTemplate.getName())).append("<br>");

            int desiredDerivedNotificationGroupCount = notificationGroupNamesThatNotificationGroupTemplateWantsToCreate_ByVariableSetId.size();
            outputString.append("<b>Total Desired Derived Notification Groups</b> = ").append(desiredDerivedNotificationGroupCount).append("<br><br>");
            if (desiredDerivedNotificationGroupCount <= 0) return outputString.toString();

            outputString.append("<b>Desired Derived Notification Groups...</b>").append("<br>");

            outputString.append("<ul>");
            
            Map<String,NotificationGroup> notificationGroups_ByName = NotificationGroupsDao.getNotificationGroups_ByName(connection, false);
            Map<String,PagerdutyService> pagerdutyServices_ByName = PagerdutyServicesDao.getPagerdutyServices_ByName(connection, false);
                    
            for (Integer variableSetId : notificationGroupNamesThatNotificationGroupTemplateWantsToCreate_ByVariableSetId.keySet()) {
                if (variableSetId == null) continue;

                String notificationGroupNameThatNotificationGroupTemplateWantsToCreate = notificationGroupNamesThatNotificationGroupTemplateWantsToCreate_ByVariableSetId.get(variableSetId);
                if (notificationGroupNameThatNotificationGroupTemplateWantsToCreate == null) continue;

                NotificationGroup notificationGroup = notificationGroups_ByUppercaseName.get(notificationGroupNameThatNotificationGroupTemplateWantsToCreate.toUpperCase());

                DatabaseObjectStatus notificationGroupStatus = getNotificationGroupStatus(connection, notificationGroupTemplate, notificationGroup, variableSetId, notificationGroups_ByName, pagerdutyServices_ByName);
                
                String notificationGroupDetailsUrl = "<a class=\"iframe cboxElement\" href=\"NotificationGroupDetails?ExcludeNavbar=true&amp;Name=" + 
                        StatsAggHtmlFramework.urlEncode(notificationGroupNameThatNotificationGroupTemplateWantsToCreate) + "\">" + 
                        StatsAggHtmlFramework.htmlEncode(notificationGroupNameThatNotificationGroupTemplateWantsToCreate) + "</a>";

                if (notificationGroupStatus == null) notificationGroupStrings.put(notificationGroupNameThatNotificationGroupTemplateWantsToCreate, "<li><b>" + notificationGroupDetailsUrl + "&nbsp(unknown error)</b></li>");
                else if (notificationGroupStatus.getStatus() == DatabaseObjectStatus.STATUS_GOOD) notificationGroupStrings.put(notificationGroupNameThatNotificationGroupTemplateWantsToCreate, "<li>" + notificationGroupDetailsUrl + "</li>");
                else notificationGroupStrings.put(notificationGroupNameThatNotificationGroupTemplateWantsToCreate, "<li><b>" + notificationGroupDetailsUrl + "&nbsp(" + notificationGroupStatus.getReason() + ")</b></li>");
            }
            
            List<String> sortedNotificationGroupStrings = new ArrayList<>(notificationGroupStrings.keySet());
            Collections.sort(sortedNotificationGroupStrings);

            for (String notificationGroupString : sortedNotificationGroupStrings) {
                String notificationGroupOutputString = notificationGroupStrings.get(notificationGroupString);
                outputString.append(notificationGroupOutputString);
            }

            outputString.append("</ul>");
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            DatabaseUtils.cleanup(connection);
        }

        return outputString.toString();
    }

    private static DatabaseObjectStatus getNotificationGroupStatus(Connection connection, NotificationGroupTemplate notificationGroupTemplate, NotificationGroup notificationGroup, 
            Integer variableSetId, Map<String,NotificationGroup> notificationGroups_ByName, Map<String,PagerdutyService> pagerdutyServices_ByName) {
        
        DatabaseObjectStatus notificationGroupStatus = new DatabaseObjectStatus(DatabaseObjectStatus.STATUS_GOOD, "");
        
        try {
            if ((notificationGroupTemplate != null) && (notificationGroupTemplate.isMarkedForDelete() != null) && (notificationGroupTemplate.isMarkedForDelete())) {
                if (notificationGroup == null) {
                    notificationGroupStatus = new DatabaseObjectStatus(DatabaseObjectStatus.STATUS_DANGER, "notification group doesn't exist because notification group template is marked for deletion");
                }
                else if ((notificationGroupTemplate.getId() != null) && !notificationGroupTemplate.getId().equals(notificationGroup.getNotificationGroupTemplateId())) {
                    notificationGroupStatus = new DatabaseObjectStatus(DatabaseObjectStatus.STATUS_DANGER, "notification group exists, but won't be deleted, because it is not associated with this notification group template");
                }
                else {
                    notificationGroupStatus = new DatabaseObjectStatus(DatabaseObjectStatus.STATUS_DANGER, "notification group is marked for deletion by notification group template");
                }
            }
            else if (notificationGroup == null) {
                VariableSet variableSet = VariableSetsDao.getVariableSet(connection, false, variableSetId);

                if (variableSet != null) {
                    NotificationGroup notificationGroupThatTemplateWantsToCreate = TemplateThread.createNotificationGroupFromNotificationGroupTemplate(notificationGroupTemplate, variableSet, notificationGroups_ByName, pagerdutyServices_ByName);
                    DatabaseObjectValidation databaseObjectValidation = NotificationGroup.isValid(notificationGroupThatTemplateWantsToCreate);

                    if ((databaseObjectValidation != null) && !databaseObjectValidation.isValid()) {
                        String databaseObjectValidationReason = (databaseObjectValidation.getReason() == null) ? "unknown" : databaseObjectValidation.getReason().toLowerCase();
                        notificationGroupStatus = new DatabaseObjectStatus(DatabaseObjectStatus.STATUS_DANGER, "notification group does not exist because: '" + databaseObjectValidationReason + "')");
                    }
                    else {
                        notificationGroupStatus = new DatabaseObjectStatus(DatabaseObjectStatus.STATUS_DANGER, "notification group does not exist - issue creating notification group");
                    }
                }
                else {
                    notificationGroupStatus = new DatabaseObjectStatus(DatabaseObjectStatus.STATUS_DANGER, "notification group does not exist - issue creating notification group");
                }
            }
            else if (notificationGroup.getNotificationGroupTemplateId() == null) {
                notificationGroupStatus = new DatabaseObjectStatus(DatabaseObjectStatus.STATUS_DANGER, "notification group name conflict with another not-templated notification group");
            }
            else if ((notificationGroupTemplate != null) && (notificationGroupTemplate.getId() != null) && !notificationGroupTemplate.getId().equals(notificationGroup.getNotificationGroupTemplateId())) {
                NotificationGroupTemplate notificationGroupTemplateFromDb = NotificationGroupTemplatesDao.getNotificationGroupTemplate(connection, false, notificationGroup.getNotificationGroupTemplateId());

                if (notificationGroupTemplateFromDb == null) {
                    notificationGroupStatus = new DatabaseObjectStatus(DatabaseObjectStatus.STATUS_DANGER, "notification group name conflict with another templated notification group");
                }
                else {
                    String notificationGroupTemplateUrl = "<a class=\"iframe cboxElement\" href=\"NotificationGroupTemplateDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(notificationGroupTemplateFromDb.getName()) + "\">" + "templated" + "</a>";
                    notificationGroupStatus = new DatabaseObjectStatus(DatabaseObjectStatus.STATUS_DANGER, "notification group name conflict with another " + notificationGroupTemplateUrl + " notification group");
                }
            }
            else if ((notificationGroupTemplate == null) || (notificationGroupTemplate.getId() == null)) {
                notificationGroupStatus = new DatabaseObjectStatus(DatabaseObjectStatus.STATUS_DANGER, "unknown error reading notification group template");
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            notificationGroupStatus = new DatabaseObjectStatus(DatabaseObjectStatus.STATUS_DANGER, "unknown error");
        }
        
        return notificationGroupStatus;
    }
    
}
