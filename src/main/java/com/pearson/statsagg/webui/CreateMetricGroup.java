package com.pearson.statsagg.webui;

import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database.metric_group.MetricGroup;
import com.pearson.statsagg.database.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database.metric_group_regex.MetricGroupRegex;
import com.pearson.statsagg.database.metric_group_regex.MetricGroupRegexsDao;
import com.pearson.statsagg.database.metric_group_tags.MetricGroupTag;
import com.pearson.statsagg.database.metric_group_tags.MetricGroupTagsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.StackTrace;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.owasp.encoder.Encode;
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
        
        response.setContentType("text/html");
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
        
        PrintWriter out = null;
        
        try {
            String result = parseMetricGroup(request);
            
            response.setContentType("text/html");     
            
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
            "<div class=\"page-content inset\" style=\"font-size:12px;\">\n" +
            "  <div class=\"content-header\"> \n" +
            "    <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "  </div> " +
            "  <form action=\"CreateMetricGroup\" method=\"POST\">\n");
        
        if ((metricGroup != null) && (metricGroup.getName() != null) && !metricGroup.getName().isEmpty()) {
            htmlBody.append("<input type=\"hidden\" name=\"Old_Name\" value=\"").append(Encode.forHtmlAttribute(metricGroup.getName())).append("\">");
        }
        
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Metric Group Name</label>\n" +
            "  <input class=\"form-control-statsagg\" placeholder=\"Enter a unique name for this metric group.\" name=\"Name\" id=\"Name\" ");

        if ((metricGroup != null) && (metricGroup.getName() != null)) {
            htmlBody.append("value=\"").append(Encode.forHtmlAttribute(metricGroup.getName())).append("\"");
        }

        htmlBody.append(      
            ">\n</div>\n" +
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Description</label>\n" +
            "  <textarea class=\"form-control-statsagg\" rows=\"3\" name=\"Description\" id=\"Description\" >");

        if ((metricGroup != null) && (metricGroup.getDescription() != null)) {
            htmlBody.append(Encode.forHtmlAttribute(metricGroup.getDescription()));
        }

        htmlBody.append(
            "</textarea>\n" +
            "</div>\n" +
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Regular Expressions</label>\n" +
            "  <a id=\"MergedRegexMetricsPreview\" name=\"MergedRegexMetricsPreview\" class=\"iframe cboxElement statsagg_merged_regex_metrics_preview pull-right\" href=\"#\" onclick=\"generateMergedRegexMetricsPreview();\">Preview Regex Matches</a>" +
            "  <textarea class=\"form-control-statsagg\" placeholder=\"One regex per line.\" rows=\"5\" name=\"Regexs\" id=\"Regexs\" >");

        if ((metricGroup != null)) {
            MetricGroupRegexsDao metricGroupRegexsDao = new MetricGroupRegexsDao();
            List<MetricGroupRegex> metricGroupRegexs =  metricGroupRegexsDao.getMetricGroupRegexsByMetricGroupId(metricGroup.getId());
            
            if (metricGroupRegexs != null) {
                for (int i = 0; i < metricGroupRegexs.size(); i++) {
                    htmlBody.append(Encode.forHtmlAttribute(metricGroupRegexs.get(i).getPattern()));
                    if ((i + 1) < metricGroupRegexs.size()) {
                        htmlBody.append("\n");
                    }
                }
            }
        }
         
        htmlBody.append(
            "</textarea>\n" +
            "</div>\n" +
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Tags</label>\n" +
            "  <textarea class=\"form-control-statsagg\" placeholder=\"One tag per line.\" rows=\"5\" name=\"Tags\" id=\"Tags\" >");

        if ((metricGroup != null)) {
            MetricGroupTagsDao metricGroupTagsDao = new MetricGroupTagsDao();
            List<MetricGroupTag> metricGroupTags =  metricGroupTagsDao.getMetricGroupTagsByMetricGroupId(metricGroup.getId());
            
            if (metricGroupTags != null) {
                for (int i = 0; i < metricGroupTags.size(); i++) {
                    htmlBody.append(Encode.forHtmlAttribute(metricGroupTags.get(i).getTag()));
                    if ((i + 1) < metricGroupTags.size()) {
                        htmlBody.append("\n");
                    }
                }
            }
        }

        htmlBody.append("</textarea>\n</div>\n");
        
        htmlBody.append(
            "      <button type=\"submit\" class=\"btn btn-default btn-primary statsagg_button_no_shadow\">Submit</button>" +
            "&nbsp;&nbsp;&nbsp;" +
            "      <a href=\"MetricGroups\" class=\"btn btn-default\" role=\"button\">Cancel</a>" +
            "    </form>\n"       +          
            "  </div>" +
            "</div>\n");
            
        return htmlBody.toString();
    }
    
    private String parseMetricGroup(HttpServletRequest request) {
        
        if (request == null) {
            return null;
        }
        
        String returnString;
        
        MetricGroup metricGroup = getMetricGroupFromMetricGroupParameters(request);
        String oldName = request.getParameter("Old_Name");
        TreeSet<String> regexs = getMetricGroupRegexsFromMetricGroupParameters(request);
        TreeSet<String> tags = getMetricGroupTagsFromMetricGroupParameters(request);
        
        // insert/update records in the database
        if ((metricGroup != null) && (metricGroup.getName() != null)) {
            MetricGroupsLogic metricGroupsLogic = new MetricGroupsLogic();
            returnString = metricGroupsLogic.alterRecordInDatabase(metricGroup, regexs, tags, oldName);
            
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
    
    private MetricGroup getMetricGroupFromMetricGroupParameters(HttpServletRequest request) {
        
        if (request == null) {
            return null;
        }
        
        boolean didEncounterError = false;
        
        MetricGroup metricGroup = new MetricGroup();

        try {
            String parameter;

            parameter = request.getParameter("Name");
            String trimmedName = parameter.trim();
            metricGroup.setName(trimmedName);
            metricGroup.setUppercaseName(trimmedName.toUpperCase());
            if ((metricGroup.getName() == null) || metricGroup.getName().isEmpty()) didEncounterError = true;

            parameter = request.getParameter("Description");
            if (parameter != null) metricGroup.setDescription(parameter.trim());
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

    protected static TreeSet<String> getMetricGroupRegexsFromMetricGroupParameters(HttpServletRequest request) {
        
        if (request == null) {
            return null;
        }
        
        boolean didEncounterError = false;
        
        TreeSet<String> regexs = new TreeSet<>();

        try {
            String parameter;

            parameter = request.getParameter("Regexs");
            if (parameter != null) {
                Scanner regexScanner = new Scanner(parameter);
                
                while (regexScanner.hasNext()) {
                    regexs.add(regexScanner.nextLine().trim());
                }
            }
        }
        catch (Exception e) {
            didEncounterError = true;
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
            
        if (didEncounterError) {
            regexs = null;
        }
        
        return regexs;
    }
    
    protected static TreeSet<String> getMetricGroupTagsFromMetricGroupParameters(HttpServletRequest request) {
        
        if (request == null) {
            return null;
        }
        
        boolean didEncounterError = false;
        
        TreeSet<String> tags = new TreeSet<>();

        try {
            String parameter;

            parameter = request.getParameter("Tags");
            if (parameter != null) {
                Scanner tagScanner = new Scanner(parameter);
                
                while (tagScanner.hasNext()) {
                    tags.add(tagScanner.nextLine().trim());
                }
            }
        }
        catch (Exception e) {
            didEncounterError = true;
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
            
        if (didEncounterError) {
            tags = null;
        }
        
        return tags;
    }
    
}
