package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.database_objects.metric_groups.MetricGroupsLogic;
import com.pearson.statsagg.globals.DatabaseConnections;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroup;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupsDao;
import com.pearson.statsagg.database_objects.metric_group_regexes.MetricGroupRegex;
import com.pearson.statsagg.database_objects.metric_group_regexes.MetricGroupRegexesDao;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTag;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTagsDao;
import com.pearson.statsagg.database_objects.output_blacklist.OutputBlacklistDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.core_utils.KeyValue;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroups extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroups.class.getName());

    public static final String PAGE_NAME = "Metric Groups";
    
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
            String html = buildMetricGroupsHtml();
            
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
                Integer id = Integer.parseInt(Common.getSingleParameterAsString(request, "Id"));
                cloneMetricGroup(id);
            }

            if ((operation != null) && operation.equals("Remove")) {
                Integer id = Integer.parseInt(Common.getSingleParameterAsString(request, "Id"));
                removeMetricGroup(id);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        StatsAggHtmlFramework.redirectAndGet(response, 303, "MetricGroups");
    }

    private void cloneMetricGroup(Integer metricGroupId) {
        
        if (metricGroupId == null) {
            return;
        }
        
        try {
            Connection connection = DatabaseConnections.getConnection();
            MetricGroup metricGroup = MetricGroupsDao.getMetricGroup(connection, false, metricGroupId);
            List<MetricGroup> allMetricGroups = MetricGroupsDao.getMetricGroups(connection, false);
            DatabaseUtils.cleanup(connection);

            if ((metricGroup != null) && (metricGroup.getId() != null) && (metricGroup.getName() != null)) {
                Set<String> allMetricGroupNames = new HashSet<>();
                for (MetricGroup currentMetricGroup : allMetricGroups) {
                    if (currentMetricGroup.getName() != null) {
                        allMetricGroupNames.add(currentMetricGroup.getName());
                    }
                }

                List<MetricGroupRegex> metricGroupRegexes = MetricGroupRegexesDao.getMetricGroupRegexesByMetricGroupId(DatabaseConnections.getConnection(), true, metricGroup.getId());
                TreeSet<String> allMetricGroupMatchRegexes = new TreeSet<>();
                TreeSet<String> allMetricGroupBlacklistRegexes = new TreeSet<>();
                if (metricGroupRegexes != null) {
                    for (MetricGroupRegex currentMetricGroupRegex : metricGroupRegexes) {
                        if (!currentMetricGroupRegex.isBlacklistRegex()) allMetricGroupMatchRegexes.add(currentMetricGroupRegex.getPattern());
                        else allMetricGroupBlacklistRegexes.add(currentMetricGroupRegex.getPattern());
                    }
                }

                List<MetricGroupTag> metricGroupTags = MetricGroupTagsDao.getMetricGroupTagsByMetricGroupId(DatabaseConnections.getConnection(), true, metricGroup.getId());
                TreeSet<String> allMetricGroupTags = new TreeSet<>();
                if (metricGroupTags != null) {
                    for (MetricGroupTag currentMetricGroupTag : metricGroupTags) {
                        allMetricGroupTags.add(currentMetricGroupTag.getTag());
                    }
                }

                MetricGroup clonedMetricGroup = MetricGroup.copy(metricGroup);
                clonedMetricGroup.setId(-1);
                String clonedAlterName = StatsAggHtmlFramework.createCloneName(metricGroup.getName(), allMetricGroupNames);
                clonedMetricGroup.setName(clonedAlterName);
                clonedMetricGroup.setUppercaseName(clonedAlterName.toUpperCase());

                MetricGroupsLogic metricGroupsLogic = new MetricGroupsLogic();
                metricGroupsLogic.alterRecordInDatabase(clonedMetricGroup, allMetricGroupMatchRegexes, allMetricGroupBlacklistRegexes, allMetricGroupTags);
                
                if ((GlobalVariables.alertInvokerThread != null) && (MetricGroupsLogic.STATUS_CODE_SUCCESS == metricGroupsLogic.getLastAlterRecordStatus())) {
                    GlobalVariables.alertInvokerThread.runAlertThread(true, false);
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
    public String removeMetricGroup(Integer metricGroupId) {
        
        String returnString = "Metric Group ID field can't be null.";
        if (metricGroupId == null) return returnString;
        
        try {
            MetricGroup metricGroup = MetricGroupsDao.getMetricGroup(DatabaseConnections.getConnection(), true, metricGroupId);                
            MetricGroupsLogic metricGroupsLogic = new MetricGroupsLogic();
            returnString = metricGroupsLogic.deleteRecordInDatabase(metricGroup.getName());

            if ((GlobalVariables.alertInvokerThread != null) && (MetricGroupsLogic.STATUS_CODE_SUCCESS == metricGroupsLogic.getLastDeleteRecordStatus())) {
                GlobalVariables.alertInvokerThread.runAlertThread(true, false);
            }
            
            return returnString;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            returnString = "Error removing metric group";
            return returnString;
        }
    }
    
    private String buildMetricGroupsHtml() {
        
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
            "     <a href=\"CreateMetricGroup\" class=\"btn btn-primary statsagg_page_content_font\">Create New Metric Group <i class=\"fa fa-long-arrow-right\"></i></a> \n" +
            "    </div>" +    
            "  </div> " +
            "  <table id=\"MetricGroupsTable\" style=\"display:none\" class=\"table table-bordered table-hover \">\n" +
            "    <thead>\n" +
            "      <tr>\n" +
            "        <th>Metric Group Name</th>\n" +
            "        <th># Regexes</th>\n" +
            "        <th>Tags</th>\n" +
            "        <th># Metric Associations</th>\n" +
            "        <th>Operations</th>\n" +
            "      </tr>\n" +
            "    </thead>\n" +
            "    <tbody>\n");

        List<MetricGroup> metricGroups = MetricGroupsDao.getMetricGroups(DatabaseConnections.getConnection(), true);
        Map<Integer,List<MetricGroupRegex>> metricGroupRegexesByMetricGroupId = MetricGroupRegexesDao.getAllMetricGroupRegexesByMetricGroupId(DatabaseConnections.getConnection(), true);
        
        Set<Integer> metricGroupIdsAssociatedWithAlerts = AlertsDao.getDistinctMetricGroupIdsAssociatedWithAlerts(DatabaseConnections.getConnection(), true);
        
        com.pearson.statsagg.database_objects.output_blacklist.OutputBlacklist outputBlacklist = OutputBlacklistDao.getOutputBlacklist_SingleRow(DatabaseConnections.getConnection(), true);
                
        Set<Integer> metricGroupIdsWithAssociations = new HashSet<>();
        if (metricGroupIdsAssociatedWithAlerts != null) metricGroupIdsWithAssociations.addAll(metricGroupIdsAssociatedWithAlerts);
        if ((outputBlacklist != null) && (outputBlacklist.getMetricGroupId() != null)) metricGroupIdsWithAssociations.add(outputBlacklist.getMetricGroupId());
        
        Map<Integer, List<MetricGroupTag>> tagsByMetricGroupId = MetricGroupTagsDao.getAllMetricGroupTagsByMetricGroupId(DatabaseConnections.getConnection(), true);
        
        for (MetricGroup metricGroup : metricGroups) {
            if ((metricGroup.getId() == null) || (metricGroup.getName() == null)) continue;
            
            List<MetricGroupRegex> metricGroupRegexes = metricGroupRegexesByMetricGroupId.get(metricGroup.getId());
            
            String metricGroupDetails = "<a class=\"iframe cboxElement\" href=\"MetricGroupDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(metricGroup.getName()) + "\">" + 
                    StatsAggHtmlFramework.htmlEncode(metricGroup.getName()) + "</a>";
            
            int regexCount;
            if (metricGroupRegexes == null) regexCount = 0;
            else regexCount = metricGroupRegexes.size();
            
            StringBuilder tagsCsv = new StringBuilder();
            if (tagsByMetricGroupId != null) {
                List<MetricGroupTag> metricGroupTags = tagsByMetricGroupId.get(metricGroup.getId());
                
                if (metricGroupTags != null) {
                    for (int i = 0; i < metricGroupTags.size(); i++) {
                        MetricGroupTag metricGroupTag = metricGroupTags.get(i);
                        tagsCsv = tagsCsv.append("<u>").append(StatsAggHtmlFramework.htmlEncode(metricGroupTag.getTag())).append("</u>");
                        if ((i + 1) < metricGroupTags.size()) tagsCsv.append(" &nbsp;");
                    }
                }
            }
            
            Set<String> matchingMetricKeysAssociatedWithMetricGroup = GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.get(metricGroup.getId());
            int matchingMetricKeysAssociatedWithMetricGroup_Count = 0;
            if (matchingMetricKeysAssociatedWithMetricGroup != null) matchingMetricKeysAssociatedWithMetricGroup_Count = matchingMetricKeysAssociatedWithMetricGroup.size();
            String metricAssociationsLink = "<a class=\"iframe cboxElement\" href=\"MetricGroupMetricKeyAssociations?ExcludeNavbar=true&amp;Name=" + 
                    StatsAggHtmlFramework.urlEncode(metricGroup.getName()) + "\">" + 
                    StatsAggHtmlFramework.htmlEncode(Integer.toString(matchingMetricKeysAssociatedWithMetricGroup_Count)) + "</a>";

            String alter = "<a href=\"CreateMetricGroup?Operation=Alter&amp;Name=" + StatsAggHtmlFramework.urlEncode(metricGroup.getName()) + "\">alter</a>";
            
            List<KeyValue<String,String>> cloneKeysAndValues = new ArrayList<>();
            cloneKeysAndValues.add(new KeyValue("Operation", "Clone"));
            cloneKeysAndValues.add(new KeyValue("Id", metricGroup.getId().toString()));
            String clone = StatsAggHtmlFramework.buildJavaScriptPostLink("Clone_" + metricGroup.getName(), "MetricGroups", "clone", cloneKeysAndValues);
            
            List<KeyValue<String,String>> removeKeysAndValues = new ArrayList<>();
            removeKeysAndValues.add(new KeyValue("Operation", "Remove"));
            removeKeysAndValues.add(new KeyValue("Id", metricGroup.getId().toString()));
            String remove = StatsAggHtmlFramework.buildJavaScriptPostLink("Remove_" + metricGroup.getName(), "MetricGroups", "remove", 
                    removeKeysAndValues, true, "Are you sure you want to remove this metric group?");
            
            htmlBodyStringBuilder
                .append("<tr>\n")
                .append("<td class=\"statsagg_force_word_break\">").append(metricGroupDetails).append("</td>\n")
                .append("<td>").append(regexCount).append("</td>\n")
                .append("<td class=\"statsagg_force_word_break\">").append(tagsCsv.toString()).append("</td>\n")
                .append("<td>").append(metricAssociationsLink).append("</td>\n")    
                .append("<td>").append(alter).append(", ").append(clone);
            
            if (!metricGroupIdsWithAssociations.contains(metricGroup.getId())) htmlBodyStringBuilder.append(", ").append(remove);
            
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
