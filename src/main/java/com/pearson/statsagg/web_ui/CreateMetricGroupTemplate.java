package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.database_objects.DatabaseObjectValidation;
import com.pearson.statsagg.globals.DatabaseConnections;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database_objects.metric_group_templates.MetricGroupTemplate;
import com.pearson.statsagg.database_objects.metric_group_templates.MetricGroupTemplatesDao;
import com.pearson.statsagg.database_objects.metric_group_templates.MetricGroupTemplatesDaoWrapper;
import com.pearson.statsagg.database_objects.variable_set_list.VariableSetList;
import com.pearson.statsagg.database_objects.variable_set_list.VariableSetListsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.math_utils.MathUtilities;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class CreateMetricGroupTemplate extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CreateMetricGroupTemplate.class.getName());
    
    public static final String PAGE_NAME = "Create Metric Group Template";
    
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
            
            MetricGroupTemplate metricGroupTemplate = null;
            String parameter = request.getParameter("Name");
            if (parameter != null) metricGroupTemplate = MetricGroupTemplatesDao.getMetricGroupTemplate(DatabaseConnections.getConnection(), true, parameter.trim());     
            
            String htmlBodyContents = buildCreateMetricGroupTemplateHtml(metricGroupTemplate);
            List<String> additionalJavascript = new ArrayList<>();
            additionalJavascript.add("js/statsagg_create_metric_group_template.js");
            String htmlBody = statsAggHtmlFramework.createHtmlBody(htmlBodyContents, additionalJavascript, false);
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
            String result = parseAndAlterMetricGroupTemplate(request);  
            
            StringBuilder htmlBuilder = new StringBuilder();
            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            String htmlBodyContent = statsAggHtmlFramework.buildHtmlBodyForPostResult(PAGE_NAME, StatsAggHtmlFramework.htmlEncode(result), "MetricGroupTemplates", MetricGroupTemplates.PAGE_NAME);
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
    
    private String buildCreateMetricGroupTemplateHtml(MetricGroupTemplate metricGroupTemplate) {

        StringBuilder htmlBody = new StringBuilder();

        htmlBody.append(
            "<div id=\"page-content-wrapper\">\n" +
            "<!-- Keep all page content within the page-content inset div! -->\n" +
            "<div class=\"page-content inset statsagg_page_content_font\">\n" +
            "  <div class=\"content-header\"> \n" +
            "    <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "  </div> " +
            "  <form action=\"CreateMetricGroupTemplate\" method=\"POST\">\n");
        
        if ((metricGroupTemplate != null) && (metricGroupTemplate.getName() != null) && !metricGroupTemplate.getName().isEmpty()) {
            htmlBody.append("<input type=\"hidden\" name=\"Old_Name\" value=\"").append(StatsAggHtmlFramework.htmlEncode(metricGroupTemplate.getName(), true)).append("\">");
        }
        
        
        // metric group template name
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Metric Group Template Name</label>\n" +
            "  <button type=\"button\" id=\"Name_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"A unique name for this metric group template.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <input class=\"form-control-statsagg\" name=\"Name\" id=\"Name\" ");

        if ((metricGroupTemplate != null) && (metricGroupTemplate.getName() != null)) {
            htmlBody.append("value=\"").append(StatsAggHtmlFramework.htmlEncode(metricGroupTemplate.getName(), true)).append("\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // variable set list name
        htmlBody.append(
            "<div class=\"form-group\" id=\"VariableSetListName_Lookup\">\n" +
            "  <label class=\"label_small_margin\">Variable Set List</label>\n" +
            "  <button type=\"button\" id=\"VariableSetListName_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"The exact name of the variable set list to associate with this metric group template.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <input class=\"typeahead form-control-statsagg\" autocomplete=\"off\" name=\"VariableSetListName\" id=\"VariableSetListName\" ");

        if ((metricGroupTemplate != null) && (metricGroupTemplate.getVariableSetListId() != null)) {
            VariableSetList variableSetList = VariableSetListsDao.getVariableSetList(DatabaseConnections.getConnection(), true, metricGroupTemplate.getVariableSetListId());

            if ((variableSetList != null) && (variableSetList.getName() != null)) {
                htmlBody.append(" value=\"").append(StatsAggHtmlFramework.htmlEncode(variableSetList.getName(), true)).append("\"");
            }
        }
        
        htmlBody.append(">\n</div>\n");
        
        
        // metric group name variable
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Metric Group Name Variable</label>\n" +
            "  <button type=\"button\" id=\"MetricGroupNameVariable_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"A templated metric group name. Variable values are substituted using ```key```.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <input class=\"form-control-statsagg\" name=\"MetricGroupNameVariable\" id=\"MetricGroupNameVariable\" ");

        if ((metricGroupTemplate != null) && (metricGroupTemplate.getMetricGroupNameVariable() != null)) {
            htmlBody.append(" value=\"").append(StatsAggHtmlFramework.htmlEncode(metricGroupTemplate.getMetricGroupNameVariable(), true)).append("\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // description variable
        htmlBody.append("<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Description Variable</label>\n" +
            "  <button type=\"button\" id=\"DescriptionVariable_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"A templated description. Variable values are substituted using ```key```.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <textarea class=\"form-control-statsagg\" rows=\"3\" name=\"DescriptionVariable\" id=\"DescriptionVariable\" >");

        if ((metricGroupTemplate != null) && (metricGroupTemplate.getDescriptionVariable() != null)) {
            htmlBody.append(StatsAggHtmlFramework.htmlEncode(metricGroupTemplate.getDescriptionVariable().trim(), true));
        }

        htmlBody.append("</textarea>\n" + "</div>\n");
        
        
        // match regex variable
        htmlBody.append("<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Regular Expressions Variable</label>\n" +
            "  <button type=\"button\" id=\"MatchRegexesVariable_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"A templated set of regular expressions. Variable values are substituted using ```key```. Only put one regex per line.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <textarea class=\"form-control-statsagg\" placeholder=\"One regex per line.\" rows=\"3\" name=\"MatchRegexesVariable\" id=\"MatchRegexesVariable\" >");

        if ((metricGroupTemplate != null) && (metricGroupTemplate.getMatchRegexesVariable() != null)) {
            htmlBody.append(StatsAggHtmlFramework.htmlEncode(metricGroupTemplate.getMatchRegexesVariable().trim(), true));
        }
        
        htmlBody.append("</textarea>\n" + "</div>\n");
        
        
        // blacklist regex variable
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Blacklist Regular Expressions Variable</label>\n" +
            "  <button type=\"button\" id=\"BlacklistRegexesVariable_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"A templated set of blacklist regular expressions. Variable values are substituted using ```key```. Only put one regex per line.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <textarea class=\"form-control-statsagg\" placeholder=\"One regex per line.\" rows=\"3\" name=\"BlacklistRegexesVariable\" id=\"BlacklistRegexesVariable\" >");

        if ((metricGroupTemplate != null) && (metricGroupTemplate.getBlacklistRegexesVariable() != null)) {
            htmlBody.append(StatsAggHtmlFramework.htmlEncode(metricGroupTemplate.getBlacklistRegexesVariable().trim(), true));
        }
        
        htmlBody.append("</textarea>\n" + "</div>\n");

        
        // tags variable
        htmlBody.append("<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Tags Variable</label>\n" +
            "  <button type=\"button\" id=\"TagsVariable_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"A templated set of tags. Variable values are substituted using ```key```. Only put one tag per line.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <textarea class=\"form-control-statsagg\" placeholder=\"One tag per line.\" rows=\"4\" name=\"TagsVariable\" id=\"TagsVariable\" >");

        if ((metricGroupTemplate != null) && (metricGroupTemplate.getTagsVariable() != null)) {
            htmlBody.append(StatsAggHtmlFramework.htmlEncode(metricGroupTemplate.getTagsVariable().trim(), true));
        }

        htmlBody.append("</textarea>\n</div>\n");
        
        
        htmlBody.append(
            "      <button type=\"submit\" class=\"btn btn-default btn-primary statsagg_button_no_shadow statsagg_page_content_font\">Submit</button>" +
            "&nbsp;&nbsp;&nbsp;" +
            "      <a href=\"MetricGroupTemplates\" class=\"btn btn-default statsagg_page_content_font\" role=\"button\">Cancel</a>" +
            "    </form>\n"       +          
            "  </div>" +
            "</div>\n");
            
        return htmlBody.toString();
    }
    
    public static String parseAndAlterMetricGroupTemplate(Object request) {
        
        if (request == null) {
            return null;
        }
        
        String returnString;
        
        MetricGroupTemplate metricGroupTemplate = getMetricGroupTemplateFromMetricGroupTemplateParameters(request);
        
        Boolean isMarkedForDelete = false;
        String oldName = Common.getSingleParameterAsString(request, "Old_Name");
        if (oldName == null) oldName = Common.getSingleParameterAsString(request, "old_name");
        if (oldName == null) {
            String id = Common.getSingleParameterAsString(request, "Id");
            if (id == null) id = Common.getSingleParameterAsString(request, "id");
            
            if (id != null) {
                try {
                    Integer id_Integer = Integer.parseInt(id.trim());
                    MetricGroupTemplate oldMetricGroupTemplate = MetricGroupTemplatesDao.getMetricGroupTemplate(DatabaseConnections.getConnection(), true, id_Integer);
                    oldName = oldMetricGroupTemplate.getName();
                    isMarkedForDelete = oldMetricGroupTemplate.isMarkedForDelete();
                }
                catch (Exception e){}
            }
        }

        if (metricGroupTemplate == null) {
            returnString = "Failed to create or alter metric group template. Reason=\"One or more invalid metric group template fields detected\".";
            logger.warn(returnString);
        } 
        else {
            metricGroupTemplate.setIsMarkedForDelete(isMarkedForDelete);
            DatabaseObjectValidation databaseObjectValidation = MetricGroupTemplate.isValid(metricGroupTemplate);

            if (!databaseObjectValidation.isValid()) {
                returnString = "Failed to create or alter metric group template. Reason=\"" + databaseObjectValidation.getReason() + "\".";
                logger.warn(returnString);
            }
            else {
                MetricGroupTemplatesDaoWrapper metricGroupTemplatesDaoWrapper = MetricGroupTemplatesDaoWrapper.alterRecordInDatabase(metricGroupTemplate, oldName);
                returnString = metricGroupTemplatesDaoWrapper.getReturnString();

                if ((GlobalVariables.templateInvokerThread != null) && (MetricGroupTemplatesDaoWrapper.STATUS_CODE_SUCCESS == metricGroupTemplatesDaoWrapper.getLastAlterRecordStatus())) {
                    logger.info("Running metric group template routine due to metric group template create or alter operation");
                    GlobalVariables.templateInvokerThread.runTemplateThread();
                }
                else logger.warn(returnString);
            }
        }
        
        return returnString;
    }
    
    private static MetricGroupTemplate getMetricGroupTemplateFromMetricGroupTemplateParameters(Object request) {
        
        if (request == null) {
            return null;
        }
        
        boolean didEncounterError = false;
        
        MetricGroupTemplate metricGroupTemplate = new MetricGroupTemplate();

        try {
            String parameter;

            parameter = Common.getSingleParameterAsString(request, "Name");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "name");
            String trimmedName = parameter.trim();
            metricGroupTemplate.setName(trimmedName);
            if ((metricGroupTemplate.getName() == null) || metricGroupTemplate.getName().isEmpty()) didEncounterError = true;

            parameter = Common.getSingleParameterAsString(request, "VariableSetListName");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "variable_set_list_name");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {    
                    VariableSetList variableSetList = VariableSetListsDao.getVariableSetList(DatabaseConnections.getConnection(), true, parameterTrimmed);
                    if (variableSetList != null) metricGroupTemplate.setVariableSetListId(variableSetList.getId());
                }
            }
            else {
                parameter = Common.getSingleParameterAsString(request, "VariableSetListId");
                if (parameter == null) parameter = Common.getSingleParameterAsString(request, "variable_set_list_id");
                if (parameter != null) {
                    String parameterTrimmed = parameter.trim();
                    if (!parameterTrimmed.isEmpty() && MathUtilities.isStringAnInteger(parameterTrimmed)) metricGroupTemplate.setVariableSetListId(Integer.parseInt(parameterTrimmed));
                }
            }
            
            parameter = Common.getSingleParameterAsString(request, "MetricGroupNameVariable");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "metric_group_name_variable");
            String trimmedMetricGroupNameVariable = (parameter != null) ? parameter.trim() : "";
            metricGroupTemplate.setMetricGroupNameVariable(trimmedMetricGroupNameVariable);
            
            parameter = Common.getSingleParameterAsString(request, "DescriptionVariable");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "description_variable");
            if (parameter != null) {
                String trimmedParameter = parameter.trim();
                String descriptionVariable;
                if (trimmedParameter.length() > 100000) descriptionVariable = trimmedParameter.substring(0, 99999);
                else descriptionVariable = trimmedParameter;
                metricGroupTemplate.setDescriptionVariable(descriptionVariable);
            }
            else metricGroupTemplate.setDescriptionVariable("");
            
            parameter = Common.getSingleParameterAsString(request, "MatchRegexesVariable");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "match_regexes_variable");
            if (parameter != null) {
                String trimmedParameter = parameter.trim();
                String matchRegexesVariable;
                if (trimmedParameter.length() > 100000) matchRegexesVariable = trimmedParameter.substring(0, 99999);
                else matchRegexesVariable = trimmedParameter;
                metricGroupTemplate.setMatchRegexesVariable(matchRegexesVariable);
            }
            else metricGroupTemplate.setMatchRegexesVariable("");
            
            parameter = Common.getSingleParameterAsString(request, "BlacklistRegexesVariable");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "blacklist_regexes_variable");
            if (parameter != null) {
                String trimmedParameter = parameter.trim();
                String blacklistRegexesVariable;
                if (trimmedParameter.length() > 100000) blacklistRegexesVariable = trimmedParameter.substring(0, 99999);
                else blacklistRegexesVariable = trimmedParameter;
                metricGroupTemplate.setBlacklistRegexsVariable(blacklistRegexesVariable);
            }
            else metricGroupTemplate.setBlacklistRegexsVariable("");
            
            parameter = Common.getSingleParameterAsString(request, "TagsVariable");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "tags_variable");
            if (parameter != null) {
                String trimmedParameter = parameter.trim();
                String tagsVariable;
                if (trimmedParameter.length() > 100000) tagsVariable = trimmedParameter.substring(0, 99999);
                else tagsVariable = trimmedParameter;
                metricGroupTemplate.setTagsVariable(tagsVariable);
            }
            else metricGroupTemplate.setTagsVariable("");
        }
        catch (Exception e) {
            didEncounterError = true;
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
            
        if (didEncounterError) {
            metricGroupTemplate = null;
        }
        
        return metricGroupTemplate;
    }

}
