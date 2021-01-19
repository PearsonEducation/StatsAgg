package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.database_objects.variable_set.VariableSetsDao;
import com.pearson.statsagg.database_objects.variable_set_list.VariableSetList;
import com.pearson.statsagg.database_objects.variable_set_list.VariableSetListsDao;
import com.pearson.statsagg.database_objects.variable_set_list.VariableSetListsDaoWrapper;
import com.pearson.statsagg.database_objects.variable_set_list_entry.VariableSetListEntriesDao;
import com.pearson.statsagg.globals.DatabaseConnections;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.Connection;
import java.util.List;
import java.util.TreeSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class CreateVariableSetList extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CreateVariableSetList.class.getName());
    
    public static final String PAGE_NAME = "Create Variable Set List";
    
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
            
            VariableSetList variableSetList = null;
            String name = request.getParameter("Name");
            if (name != null) variableSetList = VariableSetListsDao.getVariableSetList(DatabaseConnections.getConnection(), true, name.trim()); 

            String htmlBodyContents = buildCreateVariableSetListHtml(variableSetList);
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
            String result = parseAndAlterVariableSetList(request);

            StringBuilder htmlBuilder = new StringBuilder();
            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            String htmlBodyContent = statsAggHtmlFramework.buildHtmlBodyForPostResult(PAGE_NAME, StatsAggHtmlFramework.htmlEncode(result), "VariableSetLists", VariableSetLists.PAGE_NAME);
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
    
    private String buildCreateVariableSetListHtml(VariableSetList variableSetList) {

        StringBuilder htmlBody = new StringBuilder();

        htmlBody.append(
            "<div id=\"page-content-wrapper\">\n" +
            " <!-- Keep all page content within the page-content inset div! -->\n" +
            "   <div class=\"page-content inset statsagg_page_content_font\">\n" +
            "     <div class=\"content-header\"> \n" +
            "       <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "     </div> " +
            "     <form action=\"CreateVariableSetList\" method=\"POST\">\n");
        
        if ((variableSetList != null) && (variableSetList.getName() != null) && !variableSetList.getName().isEmpty()) {
            htmlBody.append("<input type=\"hidden\" name=\"Old_Name\" value=\"").append(StatsAggHtmlFramework.htmlEncode(variableSetList.getName(), true)).append("\">");
        }
        
        
        // name
        htmlBody.append(
            "       <div class=\"form-group\">\n" +
            "         <label class=\"label_small_margin\">Variable set list name</label>\n" +
            "         <input class=\"form-control-statsagg\" placeholder=\"Enter a unique name for this variable set list.\" name=\"Name\" id=\"Name\" ");

        if ((variableSetList != null) && (variableSetList.getName() != null)) {
            htmlBody.append("value=\"").append(StatsAggHtmlFramework.htmlEncode(variableSetList.getName(), true)).append("\"");
        }
        
        htmlBody.append(">\n</div>\n");
        
                    
        // description
        htmlBody.append(      
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Description</label>\n" +
            "  <textarea class=\"form-control-statsagg\" rows=\"3\" name=\"Description\" id=\"Description\" >");

        if ((variableSetList != null) && (variableSetList.getDescription() != null)) {
            htmlBody.append(StatsAggHtmlFramework.htmlEncode(variableSetList.getDescription(), true));
        }
        
        htmlBody.append("</textarea>\n" + "</div>\n");
        
        
        // variable sets
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Variable Sets</label>\n " +
            "    <textarea class=\"form-control-statsagg\" placeholder=\"List one variable set per line.\" " +
            "              rows=\"15\" name=\"VariableSets\" id=\"VariableSets\" >");

        Connection connection = DatabaseConnections.getConnection();
        if ((variableSetList != null)) {
            List<Integer> variableSetListEntries =  VariableSetListEntriesDao.getVariableSetIds_ForVariableSetListId(connection, false, variableSetList.getId());
            List<String> variableSetNames = VariableSetsDao.getVariableSetNames_OrderedByName(connection, true, variableSetListEntries);
                    
            for (int i = 0; (variableSetNames != null) && i < variableSetNames.size(); i++) {
                htmlBody.append(StatsAggHtmlFramework.htmlEncode(variableSetNames.get(i), true));
                if ((i + 1) < variableSetListEntries.size()) htmlBody.append("\n");
            }
        }
        DatabaseUtils.cleanup(connection);

        htmlBody.append("</textarea>\n" + "</div>\n");
        
        
        htmlBody.append(
            "       <button type=\"submit\" class=\"btn btn-default btn-primary statsagg_button_no_shadow statsagg_page_content_font\">Submit</button>" +
            "&nbsp;&nbsp;&nbsp;" +
            "       <a href=\"VariableSetLists\" class=\"btn btn-default statsagg_page_content_font\" role=\"button\">Cancel</a>" +
            "     </form>\n"       +          
            "   </div>\n" +
            "</div>\n");
            
        return htmlBody.toString();
    }
    
    public String parseAndAlterVariableSetList(Object request) {
        
        if (request == null) {
            return null;
        }
        
        String returnString;
        
        VariableSetList variableSetList = getVariableSetListFromVariableSetListParameters(request);
        String oldName = getOldVariableSetListName(request);
        
        TreeSet<String> variableSetNames = null;
        TreeSet<String> variableSets_Ui = Common.getMultilineParameterValues(request, "VariableSets");
        TreeSet<String> variableSets_Api = Common.getMultilineParameterValues(request, "variable_sets");
        if ((variableSets_Ui != null) || (variableSets_Api != null)) {
            variableSetNames = new TreeSet<>();
            if (variableSets_Ui != null) variableSetNames.addAll(variableSets_Ui);
            if (variableSets_Api != null) variableSetNames.addAll(variableSets_Api);
        }
            
        // insert/update/delete records in the database
        if ((variableSetList != null) && (variableSetList.getName() != null)) {
            returnString = VariableSetListsDaoWrapper.alterRecordInDatabase(variableSetList, variableSetNames, oldName).getReturnString();
        }
        else {
            returnString = "Failed to create or alter variable set list. Reason=\"Field validation failed.\"";
            logger.warn(returnString);
        }
        
        return returnString;
    }
    
    protected static String getOldVariableSetListName(Object request) {
        
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
                        VariableSetList oldVariableSetList = VariableSetListsDao.getVariableSetList(DatabaseConnections.getConnection(), true, id_Integer);
                        oldName = oldVariableSetList.getName();
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
    
    private VariableSetList getVariableSetListFromVariableSetListParameters(Object request) {
        
        if (request == null) {
            return null;
        }
        
        boolean didEncounterError = false;
        
        VariableSetList variableSetList = new VariableSetList();

        try {
            String parameter;

            parameter = Common.getSingleParameterAsString(request, "Name");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "name");
            String trimmedName = parameter.trim();
            variableSetList.setName(trimmedName);
            if ((variableSetList.getName() == null) || variableSetList.getName().isEmpty()) didEncounterError = true;

            parameter = Common.getSingleParameterAsString(request, "Description");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "description");
            if (parameter != null) {
                String trimmedParameter = parameter.trim();
                String description;
                if (trimmedParameter.length() > 100000) description = trimmedParameter.substring(0, 99999);
                else description = trimmedParameter;
                variableSetList.setDescription(description);
            }
            else variableSetList.setDescription("");
        }
        catch (Exception e) {
            didEncounterError = true;
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
            
        if (didEncounterError) {
            variableSetList = null;
        }
        
        return variableSetList;
    }

}
