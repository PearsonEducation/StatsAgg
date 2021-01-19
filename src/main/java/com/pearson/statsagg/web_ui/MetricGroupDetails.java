package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.database_objects.metric_group_templates.MetricGroupTemplate;
import com.pearson.statsagg.database_objects.metric_group_templates.MetricGroupTemplatesDao;
import com.pearson.statsagg.globals.DatabaseConnections;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroup;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupsDao;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupRegex;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupRegexesDao;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupTag;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupTagsDao;
import com.pearson.statsagg.database_objects.variable_set.VariableSet;
import com.pearson.statsagg.database_objects.variable_set.VariableSetsDao;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import java.sql.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroupDetails extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroupDetails.class.getName());
    
    public static final String PAGE_NAME = "Metric Group Details";
    
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
        String metricGroupDetails = getMetricGroupDetailsString(name, excludeNavbar);
                
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
            metricGroupDetails +
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

    private String getMetricGroupDetailsString(String metricGroupName, boolean excludeNavbar) {
        

        if (metricGroupName == null) {
            return "<b>No metric group specified</b>";
        }
        
        MetricGroup metricGroup = MetricGroupsDao.getMetricGroup(DatabaseConnections.getConnection(), true, metricGroupName);

        if (metricGroup == null) {
            return "<b>Metric group not found</b>";
        }
        
        Connection connection = DatabaseConnections.getConnection();
        MetricGroupTemplate metricGroupTemplate = null;
        VariableSet variableSet = null;
        if (metricGroup.getMetricGroupTemplateId() != null) metricGroupTemplate = MetricGroupTemplatesDao.getMetricGroupTemplate(connection, false, metricGroup.getMetricGroupTemplateId());
        if (metricGroup.getVariableSetId() != null) variableSet = VariableSetsDao.getVariableSet(connection, false, metricGroup.getVariableSetId());
        DatabaseUtils.cleanup(connection);

        StringBuilder outputString = new StringBuilder();

        outputString.append("<b>Name</b> = ");
        if (metricGroup.getName() != null) outputString.append(StatsAggHtmlFramework.htmlEncode(metricGroup.getName())).append("<br>");
        else outputString.append("N/A <br>");

        outputString.append("<b>ID</b> = ");
        if (metricGroup.getName() != null) outputString.append(metricGroup.getId()).append("<br>");
        else outputString.append("N/A <br>");

        if (metricGroupTemplate != null) {
            outputString.append("<b>Metric Group Template</b> = ");
            if (metricGroupTemplate.getName() != null) {
                String metricGroupTemplateDetailsPopup = "<a class=\"iframe cboxElement\" href=\"MetricGroupTemplateDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(metricGroupTemplate.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(metricGroupTemplate.getName()) + "</a>";
                outputString.append(metricGroupTemplateDetailsPopup).append("<br>");
            }
            else outputString.append("<br>");
        }

        if (variableSet != null) {
            outputString.append("<b>Variable Set</b> = ");
            if (variableSet.getName() != null) {
                String variableSetDetailsPopup = "<a class=\"iframe cboxElement\" href=\"VariableSetDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(variableSet.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(variableSet.getName()) + "</a>";
                outputString.append(variableSetDetailsPopup).append("<br>");
            }
            else outputString.append("<br>");
        }

        outputString.append("<br>");
        
        outputString.append("<b>Description</b> = ");
        if (metricGroup.getDescription() != null) {
            String encodedMetricGroupDescription = StatsAggHtmlFramework.htmlEncode(metricGroup.getDescription());
            outputString.append(encodedMetricGroupDescription.replaceAll("\n", "<br>")).append("<br><br>");
        }
        else outputString.append("<br><br>");

        boolean didOutputMatchRegex = false;
        if ((metricGroup.getMatchRegexes() != null) && !metricGroup.getMatchRegexes().isEmpty()) {
            int i = 1;
            for (String matchRegex : metricGroup.getMatchRegexes()) {
                outputString.append("<b>Regex #").append(i).append("</b> = ").append(StatsAggHtmlFramework.htmlEncode(matchRegex)).append("<br>");
                i++;
                didOutputMatchRegex = true;
            }
        }

        if (didOutputMatchRegex) outputString.append("<br>");

        boolean didOutputBlacklistRegex = false;
        if ((metricGroup.getBlacklistRegexes() != null) && !metricGroup.getBlacklistRegexes().isEmpty()) {
            int i = 1;
            for (String blacklistRegex : metricGroup.getBlacklistRegexes()) {
                outputString.append("<b>Blacklist Regex #").append(i).append("</b> = ").append(StatsAggHtmlFramework.htmlEncode(blacklistRegex)).append("<br>");
                i++;
                didOutputBlacklistRegex = true;
            }
        }

        if (didOutputBlacklistRegex) outputString.append("<br>"); 
        
        if (!didOutputMatchRegex && !didOutputBlacklistRegex) outputString.append("<br>");

        if ((metricGroup.getTags() != null) && !metricGroup.getTags().isEmpty()) {
            int i = 1;
            for (String metricGroupTag : metricGroup.getTags()) {
                outputString.append("<b>Tag #").append(i).append("</b> = ").append(StatsAggHtmlFramework.htmlEncode(metricGroupTag)).append("<br>");
                i++;
            }
        }

        if ((metricGroup.getTags() != null) && !metricGroup.getTags().isEmpty()) outputString.append("<br>");

        outputString.append("<b>Metric Key Associations</b> = ");            
        String metricGroup_MetricKeyAssociations_Link = "<a class=\"iframe cboxElement\" href=\"MetricGroupMetricKeyAssociations?ExcludeNavbar=" + excludeNavbar + "&amp;Name=" + StatsAggHtmlFramework.urlEncode(metricGroup.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(metricGroup.getName()) + "</a>";
        outputString.append(metricGroup_MetricKeyAssociations_Link);  

        outputString.append("<br>");
        outputString.append("<b>Alert Associations</b> = ");            
        String metricGroup_AlertAssociations_Link = "<a class=\"iframe cboxElement\" href=\"MetricGroupAlertAssociations?ExcludeNavbar=" + excludeNavbar + "&amp;Name=" + StatsAggHtmlFramework.urlEncode(metricGroup.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(metricGroup.getName()) + "</a>";
        outputString.append(metricGroup_AlertAssociations_Link);  

        return outputString.toString();  
    }

}
