package com.pearson.statsagg.webui;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database.alerts.AlertsDao;
import com.pearson.statsagg.database.metric_group.MetricGroup;
import com.pearson.statsagg.database.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database.metric_group_regex.MetricGroupRegex;
import com.pearson.statsagg.database.metric_group_regex.MetricGroupRegexsDao;
import com.pearson.statsagg.database.metric_group_tags.MetricGroupTag;
import com.pearson.statsagg.database.metric_group_tags.MetricGroupTagsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.KeyValue;
import com.pearson.statsagg.utilities.StackTrace;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "MetricGroups", urlPatterns = {"/MetricGroups"})
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
        
        response.setContentType("text/html");
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
        
        String operation = request.getParameter("Operation");
        
        if ((operation != null) && operation.equals("Clone")) {
            String name = request.getParameter("Name");
            cloneMetricGroup(name);
        }
        
        if ((operation != null) && operation.equals("Remove")) {
            String name = request.getParameter("Name");
            removeMetricGroup(name);
        }
        
        StatsAggHtmlFramework.redirectAndGet(response, 303, "MetricGroups");
    }

    private void cloneMetricGroup(String metricGroupName) {
        
        if (metricGroupName == null) {
            return;
        }
        
        try {
            MetricGroupsDao metricGroupsDao = new MetricGroupsDao(false);
            MetricGroup metricGroup = metricGroupsDao.getMetricGroupByName(metricGroupName);
            List<MetricGroup> allMetricGroups = metricGroupsDao.getAllDatabaseObjectsInTable();
            metricGroupsDao.close();

            if ((metricGroup != null) && (metricGroup.getId() != null) && (metricGroup.getName() != null)) {
                Set<String> allMetricGroupNames = new HashSet<>();
                for (MetricGroup currentMetricGroup : allMetricGroups) {
                    if (currentMetricGroup.getName() != null) {
                        allMetricGroupNames.add(currentMetricGroup.getName());
                    }
                }

                MetricGroupRegexsDao metricGroupRegexsDao = new MetricGroupRegexsDao();
                List<MetricGroupRegex> metricGroupRegexs = metricGroupRegexsDao.getMetricGroupRegexsByMetricGroupId(metricGroup.getId());
                TreeSet<String> allMetricGroupRegexs = new TreeSet<>();
                if (metricGroupRegexs != null) {
                    for (MetricGroupRegex currentMetricGroupRegex : metricGroupRegexs) {
                        allMetricGroupRegexs.add(currentMetricGroupRegex.getPattern());
                    }
                }

                MetricGroupTagsDao metricGroupTagsDao = new MetricGroupTagsDao();
                List<MetricGroupTag> metricGroupTags = metricGroupTagsDao.getMetricGroupTagsByMetricGroupId(metricGroup.getId());
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
                metricGroupsLogic.alterRecordInDatabase(clonedMetricGroup, allMetricGroupRegexs, allMetricGroupTags);
                
                if ((GlobalVariables.alertInvokerThread != null) && (MetricGroupsLogic.STATUS_CODE_SUCCESS == metricGroupsLogic.getLastAlterRecordStatus())) {
                    GlobalVariables.alertInvokerThread.runAlertThread(true, false);
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
    private void removeMetricGroup(String metricGroupName) {
        
        if (metricGroupName == null) {
            return;
        }
        
        MetricGroupsLogic metricGroupsLogic = new MetricGroupsLogic();
        metricGroupsLogic.deleteRecordInDatabase(metricGroupName);
        
        if ((GlobalVariables.alertInvokerThread != null) && (MetricGroupsLogic.STATUS_CODE_SUCCESS == metricGroupsLogic.getLastDeleteRecordStatus())) {
            GlobalVariables.alertInvokerThread.runAlertThread(true, false);
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
            "        <th># Regexs</th>\n" +
            "        <th>Tags</th>\n" +
            "        <th># Metric Associations</th>\n" +
            "        <th>Operations</th>\n" +
            "      </tr>\n" +
            "    </thead>\n" +
            "    <tbody>\n");

        MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
        List<MetricGroup> metricGroups = metricGroupsDao.getAllDatabaseObjectsInTable();
        
        MetricGroupRegexsDao metricGroupRegexsDao = new MetricGroupRegexsDao();
        Map<Integer,List<MetricGroupRegex>> metricGroupRegexsByMetricGroupId = metricGroupRegexsDao.getAllMetricGroupRegexsByMetricGroupId();
        
        AlertsDao alertsDao = new AlertsDao();
        Set<Integer> metricGroupIdsAssociatedWithAlerts = alertsDao.getDistinctMetricGroupIds();
        
        MetricGroupTagsDao metricGroupTagsDao = new MetricGroupTagsDao();
        Map<Integer, List<MetricGroupTag>> tagsByMetricGroupId = metricGroupTagsDao.getAllMetricGroupTagsByMetricGroupId();
        
        for (MetricGroup metricGroup : metricGroups) {
            if ((metricGroup.getId() == null) || (metricGroup.getName() == null)) continue;
            
            List<MetricGroupRegex> metricGroupRegexs = metricGroupRegexsByMetricGroupId.get(metricGroup.getId());
            
            String metricGroupDetails = "<a href=\"MetricGroupDetails?Name=" + StatsAggHtmlFramework.urlEncode(metricGroup.getName()) + "\">" + 
                    StatsAggHtmlFramework.htmlEncode(metricGroup.getName()) + "</a>";
            
            int regexCount;
            if (metricGroupRegexs == null) regexCount = 0;
            else regexCount = metricGroupRegexs.size();
            
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
            String metricAssociationsLink = "<a href=\"MetricGroupMetricKeyAssociations?Name=" + 
                    StatsAggHtmlFramework.urlEncode(metricGroup.getName()) + "\">" + 
                    StatsAggHtmlFramework.htmlEncode(Integer.toString(matchingMetricKeysAssociatedWithMetricGroup_Count)) + "</a>";

            String alter = "<a href=\"CreateMetricGroup?Operation=Alter&amp;Name=" + StatsAggHtmlFramework.urlEncode(metricGroup.getName()) + "\">alter</a>";
            
            List<KeyValue> cloneKeysAndValues = new ArrayList<>();
            cloneKeysAndValues.add(new KeyValue("Operation", "Clone"));
            cloneKeysAndValues.add(new KeyValue("Name", Encode.forHtmlAttribute(metricGroup.getName())));
            String clone = StatsAggHtmlFramework.buildJavaScriptPostLink("Clone_" + metricGroup.getName(), "MetricGroups", "clone", cloneKeysAndValues);
            
            List<KeyValue> removeKeysAndValues = new ArrayList<>();
            removeKeysAndValues.add(new KeyValue("Operation", "Remove"));
            removeKeysAndValues.add(new KeyValue("Name", Encode.forHtmlAttribute(metricGroup.getName())));
            String remove = StatsAggHtmlFramework.buildJavaScriptPostLink("Remove_" + metricGroup.getName(), "MetricGroups", "remove", 
                    removeKeysAndValues, true, "Are you sure you want to remove this metric group?");
            
            htmlBodyStringBuilder
                .append("<tr>\n")
                .append("<td class=\"statsagg_force_word_break\">").append(metricGroupDetails).append("</td>\n")
                .append("<td>").append(regexCount).append("</td>\n")
                .append("<td class=\"statsagg_force_word_break\">").append(tagsCsv.toString()).append("</td>\n")
                .append("<td>").append(metricAssociationsLink).append("</td>\n")    
                .append("<td>").append(alter).append(", ").append(clone);
            
            if (metricGroupIdsAssociatedWithAlerts == null) htmlBodyStringBuilder.append(", ").append(remove);
            else if (!metricGroupIdsAssociatedWithAlerts.contains(metricGroup.getId())) htmlBodyStringBuilder.append(", ").append(remove);
            
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
