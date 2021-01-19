package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.database_objects.DatabaseObjectValidation;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupsDaoWrapper;
import com.pearson.statsagg.globals.DatabaseConnections;
import java.io.PrintWriter;
import java.util.List;
import java.util.TreeSet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroup;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class CreateMetricGroup extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CreateMetricGroup.class.getName());
    
    public static final String PAGE_NAME = "Create Metric Group";
    
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
            
            MetricGroup metricGroup = null;
            String parameter = request.getParameter("Name");
            if (parameter != null) {
                metricGroup = MetricGroupsDao.getMetricGroup(DatabaseConnections.getConnection(), true, parameter.trim());
            }       
            
            String htmlBodyContents = buildCreateMetricGroupHtml(metricGroup);
            String htmlBody = statsAggHtmlFramework.createHtmlBody(htmlBodyContents);
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
            String result = parseAndAlterMetricGroup(request);  
            
            StringBuilder htmlBuilder = new StringBuilder();
            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            String htmlBodyContent = statsAggHtmlFramework.buildHtmlBodyForPostResult(PAGE_NAME, StatsAggHtmlFramework.htmlEncode(result), "MetricGroups", MetricGroups.PAGE_NAME);
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
    
    private String buildCreateMetricGroupHtml(MetricGroup metricGroup) {

        StringBuilder htmlBody = new StringBuilder();

        htmlBody.append(
            "<div id=\"page-content-wrapper\">\n" +
            "<!-- Keep all page content within the page-content inset div! -->\n" +
            "<div class=\"page-content inset statsagg_page_content_font\">\n" +
            "  <div class=\"content-header\"> \n" +
            "    <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "  </div> " +
            "  <form action=\"CreateMetricGroup\" method=\"POST\">\n");
        
        if ((metricGroup != null) && (metricGroup.getName() != null) && !metricGroup.getName().isEmpty()) {
            htmlBody.append("<input type=\"hidden\" name=\"Old_Name\" value=\"").append(StatsAggHtmlFramework.htmlEncode(metricGroup.getName(), true)).append("\">");
        }
        
        
        // match group name
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Metric Group Name</label>\n" +
            "  <input class=\"form-control-statsagg\" placeholder=\"Enter a unique name for this metric group.\" name=\"Name\" id=\"Name\" ");

        if ((metricGroup != null) && (metricGroup.getName() != null)) {
            htmlBody.append("value=\"").append(StatsAggHtmlFramework.htmlEncode(metricGroup.getName(), true)).append("\"");
        }
        
        htmlBody.append("</div>\n");

        
        // match regexes
        htmlBody.append(      
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Description</label>\n" +
            "  <textarea class=\"form-control-statsagg\" rows=\"3\" name=\"Description\" id=\"Description\" >");

        if ((metricGroup != null) && (metricGroup.getDescription() != null)) {
            htmlBody.append(StatsAggHtmlFramework.htmlEncode(metricGroup.getDescription(), true));
        }
        
        htmlBody.append("</textarea>\n" + "</div>\n");
        
        
        // match regexes
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Regular Expressions</label>\n" +
            "  <a id=\"MergedRegexMetricsPreview_Match\" name=\"MergedRegexMetricsPreview_Match\" class=\"iframe cboxElement statsagg_merged_regex_metrics_preview pull-right\" href=\"#\" onclick=\"generateMergedRegexMetricsPreview_Match();\">Preview Regex Matches</a>" +
            "  <textarea class=\"form-control-statsagg\" placeholder=\"One regex per line.\" rows=\"3\" name=\"MatchRegexes\" id=\"MatchRegexes\" >");

        if ((metricGroup != null) && (metricGroup.getMatchRegexes() != null)) {
            List<String> matchRegexes = new ArrayList<>(metricGroup.getMatchRegexes());
            for (int i = 0; i < matchRegexes.size(); i++) {
                htmlBody.append(StatsAggHtmlFramework.htmlEncode(matchRegexes.get(i).trim(), true));
                if ((i + 1) < matchRegexes.size()) htmlBody.append("\n");
            }
        }
        
        htmlBody.append("</textarea>\n" +"</div>\n");
        
        
        // blacklist regexes
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Blacklist Regular Expressions</label>\n" +
            "  <a id=\"MergedRegexMetricsPreview_Blacklist\" name=\"MergedRegexMetricsPreview_Blacklist\" class=\"iframe cboxElement statsagg_merged_regex_metrics_preview pull-right\" href=\"#\" onclick=\"generateMergedRegexMetricsPreview_Blacklist();\">Preview Regex Matches With Blacklist</a>" +
            "  <textarea class=\"form-control-statsagg\" placeholder=\"One regex per line.\" rows=\"3\" name=\"BlacklistRegexes\" id=\"BlacklistRegexes\" >");

        if ((metricGroup != null) && (metricGroup.getBlacklistRegexes() != null)) {
            List<String> blacklistRegexes = new ArrayList<>(metricGroup.getBlacklistRegexes());
            for (int i = 0; i < blacklistRegexes.size(); i++) {
                htmlBody.append(StatsAggHtmlFramework.htmlEncode(blacklistRegexes.get(i).trim(), true));
                if ((i + 1) < blacklistRegexes.size()) htmlBody.append("\n");
            }
        }

        htmlBody.append("</textarea>\n" +"</div>\n");
                   
        
        // tags
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Tags</label>\n" +
            "  <textarea class=\"form-control-statsagg\" placeholder=\"One tag per line.\" rows=\"4\" name=\"Tags\" id=\"Tags\" >");

        if ((metricGroup != null) && (metricGroup.getTags() != null)) {
            List<String> tags = new ArrayList<>(metricGroup.getTags());
            for (int i = 0; i < tags.size(); i++) {
                htmlBody.append(StatsAggHtmlFramework.htmlEncode(tags.get(i).trim(), true));
                if ((i + 1) < tags.size()) htmlBody.append("\n");
            }
        }

        htmlBody.append("</textarea>\n</div>\n");
        
        
        htmlBody.append(
            "      <button type=\"submit\" class=\"btn btn-default btn-primary statsagg_button_no_shadow statsagg_page_content_font\">Submit</button>" +
            "&nbsp;&nbsp;&nbsp;" +
            "      <a href=\"MetricGroups\" class=\"btn btn-default statsagg_page_content_font\" role=\"button\">Cancel</a>" +
            "    </form>\n"       +          
            "  </div>" +
            "</div>\n");
            
        return htmlBody.toString();
    }
    
    public String parseAndAlterMetricGroup(Object request) {
        
        if (request == null) {
            return null;
        }
        
        String returnString;
        
        MetricGroup metricGroup = getMetricGroupFromMetricGroupParameters(request);
        String oldName = getOldMetricGroupName(request);
        
        boolean isMetricGroupCreatedByMetricGroupTemplate = MetricGroupsDao.isMetricGroupCreatedByMetricGroupTemplate(DatabaseConnections.getConnection(), true, metricGroup, oldName);

        if (metricGroup == null) {
            returnString = "Failed to create or alter metric group. Reason=\"One or more invalid metric group fields detected\".";
            logger.warn(returnString);
        } 
        else if (isMetricGroupCreatedByMetricGroupTemplate) {
            returnString = "Failed to create or alter metric group. Reason=\"Cannot alter a metric group that was created by a metric group template\".";
            logger.warn(returnString);
        }
        else {
            DatabaseObjectValidation databaseObjectValidation = MetricGroup.isValid(metricGroup);

            if (!databaseObjectValidation.isValid()) {
                returnString = "Failed to create or alter metric group. Reason=\"" + databaseObjectValidation.getReason() + "\".";
                logger.warn(returnString);
            }
            else {
                MetricGroupsDaoWrapper metricGroupsDaoWrapper = MetricGroupsDaoWrapper.alterRecordInDatabase(metricGroup, oldName);
                returnString = metricGroupsDaoWrapper.getReturnString();

                if ((GlobalVariables.alertInvokerThread != null) && (MetricGroupsDaoWrapper.STATUS_CODE_SUCCESS == metricGroupsDaoWrapper.getLastAlterRecordStatus())) {
                    GlobalVariables.alertInvokerThread.runAlertThread(true, false);
                }
            }
        }
        
        return returnString;
    }
    
    protected static String getOldMetricGroupName(Object request) {
        
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
                        MetricGroup oldMetricGroup = MetricGroupsDao.getMetricGroup(DatabaseConnections.getConnection(), true, id_Integer);
                        oldName = oldMetricGroup.getName();
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
    
    private MetricGroup getMetricGroupFromMetricGroupParameters(Object request) {
        
        if (request == null) {
            return null;
        }
        
        boolean didEncounterError = false;
        
        MetricGroup metricGroup = new MetricGroup();

        try {
            String parameter;

            parameter = Common.getSingleParameterAsString(request, "Name");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "name");
            String trimmedName = parameter.trim();
            metricGroup.setName(trimmedName);
            if ((metricGroup.getName() == null) || metricGroup.getName().isEmpty()) didEncounterError = true;

            parameter = Common.getSingleParameterAsString(request, "Description");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "description");
            if (parameter != null) {
                String trimmedParameter = parameter.trim();
                String description;
                if (trimmedParameter.length() > 100000) description = trimmedParameter.substring(0, 99999);
                else description = trimmedParameter;
                metricGroup.setDescription(description);
            }
            else metricGroup.setDescription("");
            
            TreeSet<String> matchRegexes = null;
            TreeSet<String> matchRegexes_Ui = Common.getMultilineParameterValues(request, "MatchRegexes");
            TreeSet<String> matchRegexes_Api_1 = Common.getMultilineParameterValues(request, "match_regexes");
            TreeSet<String> matchRegexes_Api_2 = Common.getMultilineParameterValues(request, "match-regexes");
            if ((matchRegexes_Ui != null) || (matchRegexes_Api_1 != null) || (matchRegexes_Api_2 != null)) {
                matchRegexes = new TreeSet<>();
                if (matchRegexes_Ui != null) matchRegexes.addAll(matchRegexes_Ui);
                if (matchRegexes_Api_1 != null) matchRegexes.addAll(matchRegexes_Api_1);
                if (matchRegexes_Api_2 != null) matchRegexes.addAll(matchRegexes_Api_2);
            }
            metricGroup.setMatchRegexes(matchRegexes);

            TreeSet<String> blacklistRegexes = null;
            TreeSet<String> blacklistRegexes_Ui = Common.getMultilineParameterValues(request, "BlacklistRegexes");
            TreeSet<String> blacklistRegexes_Api_1 = Common.getMultilineParameterValues(request, "blacklist_regexes");
            TreeSet<String> blacklistRegexes_Api_2 = Common.getMultilineParameterValues(request, "blacklist-regexes");
            if ((blacklistRegexes_Ui != null) || (blacklistRegexes_Api_1 != null) || (blacklistRegexes_Api_2 != null)) {
                blacklistRegexes = new TreeSet<>();
                if (blacklistRegexes_Ui != null) blacklistRegexes.addAll(blacklistRegexes_Ui);
                if (blacklistRegexes_Api_1 != null) blacklistRegexes.addAll(blacklistRegexes_Api_1);
                if (blacklistRegexes_Api_2 != null) blacklistRegexes.addAll(blacklistRegexes_Api_2);
            }
            metricGroup.setBlacklistRegexes(blacklistRegexes);

            TreeSet<String> tags = null;
            TreeSet<String> tags_Ui = Common.getMultilineParameterValues(request, "Tags");
            TreeSet<String> tags_Api = Common.getMultilineParameterValues(request, "tags");
            if ((tags_Ui != null) || (tags_Api != null)) {
                tags = new TreeSet<>();
                if (tags_Ui != null) tags.addAll(tags_Ui);
                if (tags_Api != null) tags.addAll(tags_Api);
            }
            metricGroup.setTags(tags);
        }
        catch (Exception e) {
            didEncounterError = true;
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
            
        if (didEncounterError) {
            metricGroup = null;
        }
        
        return metricGroup;
    }

}
