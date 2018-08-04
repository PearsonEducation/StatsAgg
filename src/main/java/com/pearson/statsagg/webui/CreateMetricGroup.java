package com.pearson.statsagg.webui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database_objects.metric_group.MetricGroup;
import com.pearson.statsagg.database_objects.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database_objects.metric_group_regex.MetricGroupRegex;
import com.pearson.statsagg.database_objects.metric_group_regex.MetricGroupRegexesDao;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTag;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTagsDao;
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
@WebServlet(name = "CreateMetricGroup", urlPatterns = {"/CreateMetricGroup"})
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
                MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
                metricGroup = metricGroupsDao.getMetricGroupByName(parameter.trim());
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
        
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Metric Group Name</label>\n" +
            "  <input class=\"form-control-statsagg\" placeholder=\"Enter a unique name for this metric group.\" name=\"Name\" id=\"Name\" ");

        if ((metricGroup != null) && (metricGroup.getName() != null)) {
            htmlBody.append("value=\"").append(StatsAggHtmlFramework.htmlEncode(metricGroup.getName(), true)).append("\"");
        }

        htmlBody.append(      
            ">\n</div>\n" +
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Description</label>\n" +
            "  <textarea class=\"form-control-statsagg\" rows=\"3\" name=\"Description\" id=\"Description\" >");

        if ((metricGroup != null) && (metricGroup.getDescription() != null)) {
            htmlBody.append(StatsAggHtmlFramework.htmlEncode(metricGroup.getDescription(), true));
        }

        htmlBody.append(
            "</textarea>\n" +
            "</div>\n" +
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Regular Expressions</label>\n" +
            "  <a id=\"MergedRegexMetricsPreview_Match\" name=\"MergedRegexMetricsPreview_Match\" class=\"iframe cboxElement statsagg_merged_regex_metrics_preview pull-right\" href=\"#\" onclick=\"generateMergedRegexMetricsPreview_Match();\">Preview Regex Matches</a>" +
            "  <textarea class=\"form-control-statsagg\" placeholder=\"One regex per line.\" rows=\"3\" name=\"MatchRegexes\" id=\"MatchRegexes\" >");

        if ((metricGroup != null)) {
            MetricGroupRegexesDao metricGroupRegexesDao = new MetricGroupRegexesDao();
            List<MetricGroupRegex> metricGroupRegexes =  metricGroupRegexesDao.getMetricGroupRegexesByMetricGroupId(metricGroup.getId());
            List<MetricGroupRegex> matchMetricGroupRegexes = new ArrayList<>();
            
            if (metricGroupRegexes != null) {
                for (MetricGroupRegex metricGroupRegex : metricGroupRegexes) {
                    if ((metricGroupRegex.isBlacklistRegex() != null) && !metricGroupRegex.isBlacklistRegex()) matchMetricGroupRegexes.add(metricGroupRegex);
                }
            }
            
            for (int i = 0; i < matchMetricGroupRegexes.size(); i++) {
                htmlBody.append(StatsAggHtmlFramework.htmlEncode(matchMetricGroupRegexes.get(i).getPattern(), true));
                if ((i + 1) < matchMetricGroupRegexes.size()) htmlBody.append("\n");
            }
        }
         
        htmlBody.append(
            "</textarea>\n" +
            "</div>\n" +
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Blacklist Regular Expressions</label>\n" +
            "  <a id=\"MergedRegexMetricsPreview_Blacklist\" name=\"MergedRegexMetricsPreview_Blacklist\" class=\"iframe cboxElement statsagg_merged_regex_metrics_preview pull-right\" href=\"#\" onclick=\"generateMergedRegexMetricsPreview_Blacklist();\">Preview Regex Matches With Blacklist</a>" +
            "  <textarea class=\"form-control-statsagg\" placeholder=\"One regex per line.\" rows=\"3\" name=\"BlacklistRegexes\" id=\"BlacklistRegexes\" >");

        if ((metricGroup != null)) {
            MetricGroupRegexesDao metricGroupRegexesDao = new MetricGroupRegexesDao();
            List<MetricGroupRegex> metricGroupRegexes =  metricGroupRegexesDao.getMetricGroupRegexesByMetricGroupId(metricGroup.getId());
            List<MetricGroupRegex> blacklistMetricGroupRegexes = new ArrayList<>();
            
            if (metricGroupRegexes != null) {
                for (MetricGroupRegex metricGroupRegex : metricGroupRegexes) {
                    if ((metricGroupRegex.isBlacklistRegex() != null) && metricGroupRegex.isBlacklistRegex()) blacklistMetricGroupRegexes.add(metricGroupRegex);
                }
            }
            
            for (int i = 0; i < blacklistMetricGroupRegexes.size(); i++) {
                htmlBody.append(StatsAggHtmlFramework.htmlEncode(blacklistMetricGroupRegexes.get(i).getPattern(), true));
                if ((i + 1) < blacklistMetricGroupRegexes.size()) htmlBody.append("\n");
            }
        }

        htmlBody.append(
            "</textarea>\n" +
            "</div>\n" +
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Tags</label>\n" +
            "  <textarea class=\"form-control-statsagg\" placeholder=\"One tag per line.\" rows=\"4\" name=\"Tags\" id=\"Tags\" >");

        if ((metricGroup != null)) {
            MetricGroupTagsDao metricGroupTagsDao = new MetricGroupTagsDao();
            List<MetricGroupTag> metricGroupTags =  metricGroupTagsDao.getMetricGroupTagsByMetricGroupId(metricGroup.getId());
            
            if (metricGroupTags != null) {
                for (int i = 0; i < metricGroupTags.size(); i++) {
                    htmlBody.append(StatsAggHtmlFramework.htmlEncode(metricGroupTags.get(i).getTag(), true));
                    if ((i + 1) < metricGroupTags.size()) {
                        htmlBody.append("\n");
                    }
                }
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
        String oldName = Common.getParameterAsString(request, "Old_Name");
        if (oldName == null) oldName = Common.getParameterAsString(request, "old_name");
        if (oldName == null) {
            String id = Common.getParameterAsString(request, "Id");
            if (id == null) id = Common.getParameterAsString(request, "id");
            
            if (id != null) {
                try {
                    Integer id_Integer = Integer.parseInt(id.trim());
                    MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
                    MetricGroup oldMetricGroup = metricGroupsDao.getMetricGroup(id_Integer);
                    oldName = oldMetricGroup.getName();
                }
                catch (Exception e){}
            }
        }

        TreeSet<String> matchRegexes = null;
        TreeSet<String> matchRegexes_Ui = getMetricGroupNewlineDelimitedParameterValues(request, "MatchRegexes");
        TreeSet<String> matchRegexes_Api_1 = getMetricGroupNewlineDelimitedParameterValues(request, "match_regexes");
        TreeSet<String> matchRegexes_Api_2 = getMetricGroupNewlineDelimitedParameterValues(request, "match-regexes");
        if ((matchRegexes_Ui != null) || (matchRegexes_Api_1 != null) || (matchRegexes_Api_2 != null)) {
            matchRegexes = new TreeSet<>();
            if (matchRegexes_Ui != null) matchRegexes.addAll(matchRegexes_Ui);
            if (matchRegexes_Api_1 != null) matchRegexes.addAll(matchRegexes_Api_1);
            if (matchRegexes_Api_2 != null) matchRegexes.addAll(matchRegexes_Api_2);
        }
        
        TreeSet<String> blacklistRegexes = null;
        TreeSet<String> blacklistRegexes_Ui = getMetricGroupNewlineDelimitedParameterValues(request, "BlacklistRegexes");
        TreeSet<String> blacklistRegexes_Api_1 = getMetricGroupNewlineDelimitedParameterValues(request, "blacklist_regexes");
        TreeSet<String> blacklistRegexes_Api_2 = getMetricGroupNewlineDelimitedParameterValues(request, "blacklist-regexes");
        if ((blacklistRegexes_Ui != null) || (blacklistRegexes_Api_1 != null) || (blacklistRegexes_Api_2 != null)) {
            blacklistRegexes = new TreeSet<>();
            if (blacklistRegexes_Ui != null) blacklistRegexes.addAll(blacklistRegexes_Ui);
            if (blacklistRegexes_Api_1 != null) blacklistRegexes.addAll(blacklistRegexes_Api_1);
            if (blacklistRegexes_Api_2 != null) blacklistRegexes.addAll(blacklistRegexes_Api_2);
        }
        
        TreeSet<String> tags = null;
        TreeSet<String> tags_Ui = getMetricGroupNewlineDelimitedParameterValues(request, "Tags");
        TreeSet<String> tags_Api = getMetricGroupNewlineDelimitedParameterValues(request, "tags");
        if ((tags_Ui != null) || (tags_Api != null)) {
            tags = new TreeSet<>();
            if (tags_Ui != null) tags.addAll(tags_Ui);
            if (tags_Api != null) tags.addAll(tags_Api);
        }
        
        // insert/update records in the database
        if ((metricGroup != null) && (metricGroup.getName() != null)) {
            MetricGroupsLogic metricGroupsLogic = new MetricGroupsLogic();
            returnString = metricGroupsLogic.alterRecordInDatabase(metricGroup, matchRegexes, blacklistRegexes, tags, oldName);
            
            if ((GlobalVariables.alertInvokerThread != null) && (MetricGroupsLogic.STATUS_CODE_SUCCESS == metricGroupsLogic.getLastAlterRecordStatus())) {
                GlobalVariables.alertInvokerThread.runAlertThread(true, false);
            }
        }
        else {
            returnString = "Failed to add metric group. Reason=\"Field validation failed.\"";
            logger.warn(returnString);
        }
        
        return returnString;
    }
    
    private MetricGroup getMetricGroupFromMetricGroupParameters(Object request) {
        
        if (request == null) {
            return null;
        }
        
        boolean didEncounterError = false;
        
        MetricGroup metricGroup = new MetricGroup();

        try {
            String parameter;

            parameter = Common.getParameterAsString(request, "Name");
            if (parameter == null) parameter = Common.getParameterAsString(request, "name");
            String trimmedName = parameter.trim();
            metricGroup.setName(trimmedName);
            metricGroup.setUppercaseName(trimmedName.toUpperCase());
            if ((metricGroup.getName() == null) || metricGroup.getName().isEmpty()) didEncounterError = true;

            parameter = Common.getParameterAsString(request, "Description");
            if (parameter == null) parameter = Common.getParameterAsString(request, "description");
            if (parameter != null) {
                String trimmedParameter = parameter.trim();
                String description;
                if (trimmedParameter.length() > 100000) description = trimmedParameter.substring(0, 99999);
                else description = trimmedParameter;
                metricGroup.setDescription(description);
            }
            else metricGroup.setDescription("");
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

    protected static TreeSet<String> getMetricGroupNewlineDelimitedParameterValues(Object request, String parameterName) {
        
        if ((request == null) || (parameterName == null)) {
            return null;
        }
        
        boolean didEncounterError = false;
        TreeSet<String> parameterValues = new TreeSet<>();

        try {
            String parameter = Common.getParameterAsString(request, parameterName);
            
            if (parameter != null) {
                Scanner scanner = new Scanner(parameter);
                
                while (scanner.hasNext()) {
                    parameterValues.add(scanner.nextLine().trim());
                }
            }
            else if (request instanceof JsonObject) {
                JsonObject jsonObject = (JsonObject) request;
                JsonArray jsonArray = jsonObject.getAsJsonArray(parameterName);
                if (jsonArray != null) for (JsonElement jsonElement : jsonArray) parameterValues.add(jsonElement.getAsString());
            }
        }
        catch (Exception e) {
            didEncounterError = true;
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
            
        if (didEncounterError) parameterValues = null;
        
        return parameterValues;
    }

}
