package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.database_objects.variable_set.VariableSet;
import com.pearson.statsagg.database_objects.variable_set.VariableSetsDao;
import com.pearson.statsagg.database_objects.variable_set.VariableSetsDaoWrapper;
import com.pearson.statsagg.globals.DatabaseConnections;
import java.io.PrintWriter;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class CreateVariableSet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CreateVariableSet.class.getName());
    
    public static final String PAGE_NAME = "Create Variable Set";
    
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
            
            VariableSet variableSet = null;
            String name = request.getParameter("Name");
            if (name != null) variableSet = VariableSetsDao.getVariableSet(DatabaseConnections.getConnection(), true, name.trim()); 

            String htmlBodyContents = buildCreateVariableSetHtml(variableSet);
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
            String result = parseAndAlterVariableSet(request);

            StringBuilder htmlBuilder = new StringBuilder();
            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            String htmlBodyContent = statsAggHtmlFramework.buildHtmlBodyForPostResult(PAGE_NAME, StatsAggHtmlFramework.htmlEncode(result), "VariableSets", VariableSets.PAGE_NAME);
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
    
    private String buildCreateVariableSetHtml(VariableSet variableSet) {

        StringBuilder htmlBody = new StringBuilder();

        htmlBody.append(
            "<div id=\"page-content-wrapper\">\n" +
            " <!-- Keep all page content within the page-content inset div! -->\n" +
            "   <div class=\"page-content inset statsagg_page_content_font\">\n" +
            "     <div class=\"content-header\"> \n" +
            "       <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "     </div> " +
            "     <form action=\"CreateVariableSet\" method=\"POST\">\n");
        
        if ((variableSet != null) && (variableSet.getName() != null) && !variableSet.getName().isEmpty()) {
            htmlBody.append("<input type=\"hidden\" name=\"Old_Name\" value=\"").append(StatsAggHtmlFramework.htmlEncode(variableSet.getName(), true)).append("\">");
        }
        
        
        // name
        htmlBody.append(
            "       <div class=\"form-group\">\n" +
            "         <label class=\"label_small_margin\">Variable Set Name</label>\n" +
            "         <button type=\"button\" id=\"Name_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"A unique name for this variable set.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "         <input class=\"form-control-statsagg\" name=\"Name\" id=\"Name\" ");

        if ((variableSet != null) && (variableSet.getName() != null)) {
            htmlBody.append("value=\"").append(StatsAggHtmlFramework.htmlEncode(variableSet.getName(), true)).append("\"");
        }
        
        htmlBody.append(">\n</div>\n");

        
        // description
        htmlBody.append(      
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Description</label>\n" +
            "  <textarea class=\"form-control-statsagg\" rows=\"3\" name=\"Description\" id=\"Description\" >");

        if ((variableSet != null) && (variableSet.getDescription() != null)) {
            htmlBody.append(StatsAggHtmlFramework.htmlEncode(variableSet.getDescription(), true));
        }
        
        htmlBody.append("</textarea>\n" + "</div>\n");
                    
        
        // variables
        htmlBody.append( //
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Variables</label>\n " +
            "    <button type=\"button\" id=\"Name_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"Variables in key=value format. Ex: pet=cat. List one variable pair per line.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "    <textarea class=\"form-control-statsagg\" rows=\"15\" name=\"Variables\" id=\"Variables\" >");

        if ((variableSet != null) && (variableSet.getVariables() != null)) {
            htmlBody.append(StatsAggHtmlFramework.htmlEncode(variableSet.getVariables(), true));
        }
        
        htmlBody.append("</textarea>\n" + "</div>\n");

        
        htmlBody.append(
            "       <button type=\"submit\" class=\"btn btn-default btn-primary statsagg_button_no_shadow statsagg_page_content_font\">Submit</button>" +
            "&nbsp;&nbsp;&nbsp;" +
            "       <a href=\"VariableSets\" class=\"btn btn-default statsagg_page_content_font\" role=\"button\">Cancel</a>" +
            "     </form>\n"       +          
            "   </div>\n" +
            "</div>\n");
            
        return htmlBody.toString();
    }
    
    public String parseAndAlterVariableSet(Object request) {
        
        if (request == null) {
            return null;
        }
        
        String returnString;
        
        VariableSet variableSet = getVariableSetFromVariableSetParameters(request);
        String oldName = Common.getSingleParameterAsString(request, "Old_Name");
        if (oldName == null) oldName = Common.getSingleParameterAsString(request, "old_name");
        if (oldName == null) {
            String id = Common.getSingleParameterAsString(request, "Id");
            if (id == null) id = Common.getSingleParameterAsString(request, "id");
            
            if (id != null) {
                try {
                    Integer id_Integer = Integer.parseInt(id.trim());
                    VariableSet oldVariableSet = VariableSetsDao.getVariableSet(DatabaseConnections.getConnection(), true, id_Integer);
                    oldName = oldVariableSet.getName();
                }
                catch (Exception e){}
            }
        }
        
        // insert/update/delete records in the database
        if ((variableSet != null) && (variableSet.getName() != null)) {
            returnString = VariableSetsDaoWrapper.alterRecordInDatabase(variableSet, oldName).getReturnString();
        }
        else {
            returnString = "Failed to create or alter variable set. Reason=\"Field validation failed.\"";
            logger.warn(returnString);
        }
        
        return returnString;
    }
    
    private VariableSet getVariableSetFromVariableSetParameters(Object request) {
        
        if (request == null) {
            return null;
        }
        
        boolean didEncounterError = false;
        
        VariableSet variableSet = new VariableSet();

        try {
            String parameter;

            parameter = Common.getSingleParameterAsString(request, "Name");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "name");
            String trimmedName = parameter.trim();
            variableSet.setName(trimmedName);
            if ((variableSet.getName() == null) || variableSet.getName().isEmpty()) didEncounterError = true;

            parameter = Common.getSingleParameterAsString(request, "Description");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "description");
            if (parameter != null) {
                String trimmedParameter = parameter.trim();
                String description;
                if (trimmedParameter.length() > 100000) description = trimmedParameter.substring(0, 99999);
                else description = trimmedParameter;
                variableSet.setDescription(description);
            }
            else variableSet.setDescription("");
            
            String variablesString = "";
            TreeSet<String> variables;
            TreeSet<String> variables_Ui = Common.getMultilineParameterValues(request, "Variables", false);
            TreeSet<String> variables_Api = Common.getMultilineParameterValues(request, "variables", false);
            if ((variables_Ui != null) || (variables_Api != null)) {
                variables = new TreeSet<>();
                if (variables_Ui != null) variables.addAll(variables_Ui);
                if (variables_Api != null) variables.addAll(variables_Api);
                
                List<String> variablesArray = new ArrayList<>(variables);
                List<String> variablesNotBlankArray = new ArrayList<>();
                for (String variable : variablesArray) if ((variable != null) && !variable.isBlank()) variablesNotBlankArray.add(variable);
                
                for (int i = 0; i < variablesNotBlankArray.size(); i++) {
                    String variable = variablesNotBlankArray.get(i);
                    if (i+1 >= variablesNotBlankArray.size()) variablesString += variable;
                    else variablesString += variable + "\n";
                }
            }
            variableSet.setVariables(variablesString);
        }
        catch (Exception e) {
            didEncounterError = true;
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
            
        if (didEncounterError) {
            variableSet = null;
        }
        
        return variableSet;
    }

}
