package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.database_objects.DatabaseObjectStatus;
import com.pearson.statsagg.database_objects.DatabaseObjectValidation;
import com.pearson.statsagg.database_objects.metric_group_templates.MetricGroupTemplate;
import com.pearson.statsagg.database_objects.metric_group_templates.MetricGroupTemplatesDao;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroup;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupsDao;
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
public class MetricGroupTemplate_DerivedMetricGroups extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroupTemplate_DerivedMetricGroups.class.getName());
    
    public static final String PAGE_NAME = "Metric Group Template - Derived Metric Groups";
    
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
        String metricGroupTemplate_DerivedMetricGroups = getMetricGroupTemplate_DerivedMetricGroups(name, excludeNavbar);
                
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
            metricGroupTemplate_DerivedMetricGroups +
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

    private String getMetricGroupTemplate_DerivedMetricGroups(String metricGroupTemplateName, boolean excludeNavbar) {
        
        if (metricGroupTemplateName == null) {
            return "<b>No metric group template specified</b>";
        }
        
        StringBuilder outputString = new StringBuilder();
        
        Connection connection = null;
        
        try {
            connection = DatabaseConnections.getConnection();
            
            Map<String,String> metricGroupStrings = new HashMap<>();

            MetricGroupTemplate metricGroupTemplate = MetricGroupTemplatesDao.getMetricGroupTemplate(connection, false, metricGroupTemplateName);
            if ((metricGroupTemplate == null) || (metricGroupTemplate.getId() == null)) return "<b>Metric group template not found</b>";

            Map<String,MetricGroup> metricGroups_ByUppercaseName = MetricGroupsDao.getMetricGroups_ByUppercaseName(connection, false);
            if (metricGroups_ByUppercaseName == null) return "<b>Error retrieving metric groups</b>";

            Map<Integer,String> metricGroupNamesThatMetricGroupTemplateWantsToCreate_ByVariableSetId = com.pearson.statsagg.threads.template_related.Common.getNamesThatTemplateWantsToCreate_ByVariableSetId(metricGroupTemplate.getVariableSetListId(), metricGroupTemplate.getMetricGroupNameVariable());
            if (metricGroupNamesThatMetricGroupTemplateWantsToCreate_ByVariableSetId == null) return "<b>Error retrieving desired derived metric groups</b>";

            outputString.append("<b>Metric Group Template Name</b> = ").append(StatsAggHtmlFramework.htmlEncode(metricGroupTemplate.getName())).append("<br>");

            int desiredDerivedMetricGroupCount = metricGroupNamesThatMetricGroupTemplateWantsToCreate_ByVariableSetId.size();
            outputString.append("<b>Total Desired Derived Metric Groups</b> = ").append(desiredDerivedMetricGroupCount).append("<br><br>");
            if (desiredDerivedMetricGroupCount <= 0) return outputString.toString();

            outputString.append("<b>Desired Derived Metric Groups...</b>").append("<br>");

            outputString.append("<ul>");
            
            Map<String,MetricGroup> metricGroups_ByName = MetricGroupsDao.getMetricGroups_ByName(connection, false);

            for (Integer variableSetId : metricGroupNamesThatMetricGroupTemplateWantsToCreate_ByVariableSetId.keySet()) {
                if (variableSetId == null) continue;

                String metricGroupNameThatMetricGroupTemplateWantsToCreate = metricGroupNamesThatMetricGroupTemplateWantsToCreate_ByVariableSetId.get(variableSetId);
                if (metricGroupNameThatMetricGroupTemplateWantsToCreate == null) continue;

                MetricGroup metricGroup = metricGroups_ByUppercaseName.get(metricGroupNameThatMetricGroupTemplateWantsToCreate.toUpperCase());

                DatabaseObjectStatus metricGroupStatus = getMetricGroupStatus(connection, metricGroupTemplate, metricGroup, variableSetId, metricGroups_ByName);
                
                String metricGroupDetailsUrl = "<a class=\"iframe cboxElement\" href=\"MetricGroupDetails?ExcludeNavbar=true&amp;Name=" + 
                        StatsAggHtmlFramework.urlEncode(metricGroupNameThatMetricGroupTemplateWantsToCreate) + "\">" + 
                        StatsAggHtmlFramework.htmlEncode(metricGroupNameThatMetricGroupTemplateWantsToCreate) + "</a>";

                if (metricGroupStatus == null) metricGroupStrings.put(metricGroupNameThatMetricGroupTemplateWantsToCreate, "<li><b>" + metricGroupDetailsUrl + "&nbsp(unknown error)</b></li>");
                else if (metricGroupStatus.getStatus() == DatabaseObjectStatus.STATUS_GOOD) metricGroupStrings.put(metricGroupNameThatMetricGroupTemplateWantsToCreate, "<li>" + metricGroupDetailsUrl + "</li>");
                else metricGroupStrings.put(metricGroupNameThatMetricGroupTemplateWantsToCreate, "<li><b>" + metricGroupDetailsUrl + "&nbsp(" + metricGroupStatus.getReason() + ")</b></li>");
            }
            
            List<String> sortedMetricGroupStrings = new ArrayList<>(metricGroupStrings.keySet());
            Collections.sort(sortedMetricGroupStrings);

            for (String metricGroupString : sortedMetricGroupStrings) {
                String metricGroupOutputString = metricGroupStrings.get(metricGroupString);
                outputString.append(metricGroupOutputString);
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

    private static DatabaseObjectStatus getMetricGroupStatus(Connection connection, MetricGroupTemplate metricGroupTemplate, MetricGroup metricGroup, 
            Integer variableSetId, Map<String,MetricGroup> metricGroups_ByName) {
        
        DatabaseObjectStatus metricGroupStatus = new DatabaseObjectStatus(DatabaseObjectStatus.STATUS_GOOD, "");
        
        try {
            if ((metricGroupTemplate != null) && (metricGroupTemplate.isMarkedForDelete() != null) && (metricGroupTemplate.isMarkedForDelete())) {
                if (metricGroup == null) {
                    metricGroupStatus = new DatabaseObjectStatus(DatabaseObjectStatus.STATUS_DANGER, "metric group doesn't exist because metric group template is marked for deletion");
                }
                else if ((metricGroupTemplate.getId() != null) && !metricGroupTemplate.getId().equals(metricGroup.getMetricGroupTemplateId())) {
                    metricGroupStatus = new DatabaseObjectStatus(DatabaseObjectStatus.STATUS_DANGER, "metric group exists, but won't be deleted, because it is not associated with this metric group template");
                }
                else {
                    metricGroupStatus = new DatabaseObjectStatus(DatabaseObjectStatus.STATUS_DANGER, "metric group is marked for deletion by metric group template");
                }
            }
            else if (metricGroup == null) {
                VariableSet variableSet = VariableSetsDao.getVariableSet(connection, false, variableSetId);

                if (variableSet != null) {
                    MetricGroup metricGroupThatTemplateWantsToCreate = TemplateThread.createMetricGroupFromMetricGroupTemplate(metricGroupTemplate, variableSet, metricGroups_ByName);
                    DatabaseObjectValidation databaseObjectValidation = MetricGroup.isValid(metricGroupThatTemplateWantsToCreate);

                    if ((databaseObjectValidation != null) && !databaseObjectValidation.isValid()) {
                        String databaseObjectValidationReason = (databaseObjectValidation.getReason() == null) ? "unknown" : databaseObjectValidation.getReason().toLowerCase();
                        metricGroupStatus = new DatabaseObjectStatus(DatabaseObjectStatus.STATUS_DANGER, "metric group does not exist because: '" + databaseObjectValidationReason + "')");
                    }
                    else {
                        metricGroupStatus = new DatabaseObjectStatus(DatabaseObjectStatus.STATUS_DANGER, "metric group does not exist - issue creating metric group");
                    }
                }
                else {
                    metricGroupStatus = new DatabaseObjectStatus(DatabaseObjectStatus.STATUS_DANGER, "metric group does not exist - issue creating metric group");
                }
            }
            else if (metricGroup.getMetricGroupTemplateId() == null) {
                metricGroupStatus = new DatabaseObjectStatus(DatabaseObjectStatus.STATUS_DANGER, "metric group name conflict with another not-templated metric group");
            }
            else if ((metricGroupTemplate != null) && (metricGroupTemplate.getId() != null) && !metricGroupTemplate.getId().equals(metricGroup.getMetricGroupTemplateId())) {
                MetricGroupTemplate metricGroupTemplateFromDb = MetricGroupTemplatesDao.getMetricGroupTemplate(connection, false, metricGroup.getMetricGroupTemplateId());

                if (metricGroupTemplateFromDb == null) {
                    metricGroupStatus = new DatabaseObjectStatus(DatabaseObjectStatus.STATUS_DANGER, "metric group name conflict with another templated metric group");
                }
                else {
                    String metricGroupTemplateUrl = "<a class=\"iframe cboxElement\" href=\"MetricGroupTemplateDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(metricGroupTemplateFromDb.getName()) + "\">" + "templated" + "</a>";
                    metricGroupStatus = new DatabaseObjectStatus(DatabaseObjectStatus.STATUS_DANGER, "metric group name conflict with another " + metricGroupTemplateUrl + " metric group");
                }
            }
            else if ((metricGroupTemplate == null) || (metricGroupTemplate.getId() == null)) {
                metricGroupStatus = new DatabaseObjectStatus(DatabaseObjectStatus.STATUS_DANGER, "unknown error reading metric group template");
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            metricGroupStatus = new DatabaseObjectStatus(DatabaseObjectStatus.STATUS_DANGER, "unknown error");
        }
        
        return metricGroupStatus;
    }
    
}
