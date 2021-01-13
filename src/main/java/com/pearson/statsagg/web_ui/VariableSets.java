package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.database_objects.variable_set.VariableSet;
import com.pearson.statsagg.database_objects.variable_set.VariableSetsDao;
import com.pearson.statsagg.database_objects.variable_set.VariableSetsDaoWrapper;
import com.pearson.statsagg.database_objects.variable_set_list_entry.VariableSetListEntriesDao;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.globals.DatabaseConnections;
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
public class VariableSets extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(VariableSets.class.getName());

    public static final String PAGE_NAME = "Variable Sets";
    
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
            String html = buildVariableSetsHtml();
            
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
                Integer id = Integer.parseInt(request.getParameter("Id"));
                cloneVariableSet(id);
            }

            if ((operation != null) && operation.equals("Remove")) {
                Integer id = Integer.parseInt(Common.getSingleParameterAsString(request, "Id"));
                removeVariableSet(id);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        StatsAggHtmlFramework.redirectAndGet(response, 303, "VariableSets");
    }
    
    private void cloneVariableSet(Integer variableSetId) {
        
        if (variableSetId == null) {
            return;
        }
        
        try {
            Connection connection = DatabaseConnections.getConnection();
            VariableSet variableSet = VariableSetsDao.getVariableSet(connection, false, variableSetId);
            List<VariableSet> allVariableSets = VariableSetsDao.getVariableSets(connection, false);
            DatabaseUtils.cleanup(connection);

            if ((variableSet != null) && (variableSet.getName() != null)) {
                Set<String> allVariableSetNames = new HashSet<>();
                for (VariableSet currentVariableSet : allVariableSets) {
                    if (currentVariableSet.getName() != null) allVariableSetNames.add(currentVariableSet.getName());
                }

                VariableSet clonedVariableSet = VariableSet.copy(variableSet);
                clonedVariableSet.setId(-1);
                String clonedAlterName = StatsAggHtmlFramework.createCloneName(variableSet.getName(), allVariableSetNames);
                clonedVariableSet.setName(clonedAlterName);

                VariableSetsDaoWrapper.createRecordInDatabase(clonedVariableSet);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
    public String removeVariableSet(Integer variableSetId) {
        
        String returnString = "Variable Set ID field can't be null.";
        if (variableSetId == null) return returnString;
        
        try {
            VariableSet variableSet = VariableSetsDao.getVariableSet(DatabaseConnections.getConnection(), true, variableSetId);   
            returnString = VariableSetsDaoWrapper.deleteRecordInDatabase(variableSet).getReturnString();
            return returnString;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            returnString = "Error removing variable set";
            return returnString;
        }

    }
    
    private String buildVariableSetsHtml() {
        
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
            "     <a href=\"CreateVariableSet\" class=\"btn btn-primary statsagg_page_content_font\">Create New Variable Set <i class=\"fa fa-long-arrow-right\"></i></a> \n" +
            "    </div>" +   
            "  </div>" +    
            "  <table id=\"VariableSetsTable\" style=\"display:none\" class=\"table table-bordered table-hover \">\n" +
            "    <thead>\n" +
            "      <tr>\n" +
            "        <th>Variable Set Name</th>\n" +
            "        <th>Operations</th>\n" +
            "      </tr>\n" +
            "    </thead>\n" +
            "    <tbody>\n");

        Connection connection = DatabaseConnections.getConnection();
        Set<Integer> variableSetIdsAssociatedWithVariableSetLists = VariableSetListEntriesDao.getAllDistinctVariableSetIds(connection, false);
        List<VariableSet> variableSets = VariableSetsDao.getVariableSets(connection, false);
        DatabaseUtils.cleanup(connection);
        
        for (VariableSet variableSet : variableSets) {     
            if (variableSet == null) continue;
            
            String variableSetDetails = "<a class=\"iframe cboxElement\" href=\"VariableSetDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(variableSet.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(variableSet.getName()) + "</a>";
            
            String alter = "<a href=\"CreateVariableSet?Operation=Alter&amp;Name=" + StatsAggHtmlFramework.urlEncode(variableSet.getName()) + "\">alter</a>";

            List<KeyValue<String,String>> cloneKeysAndValues = new ArrayList<>();
            cloneKeysAndValues.add(new KeyValue("Operation", "Clone"));
            cloneKeysAndValues.add(new KeyValue("Id", variableSet.getId().toString()));
            String clone = StatsAggHtmlFramework.buildJavaScriptPostLink("Clone_" + variableSet.getName(), "VariableSets", "clone", cloneKeysAndValues);
            
            List<KeyValue<String,String>> removeKeysAndValues = new ArrayList<>();
            removeKeysAndValues.add(new KeyValue("Operation", "Remove"));
            removeKeysAndValues.add(new KeyValue("Id", variableSet.getId().toString()));
            String remove = StatsAggHtmlFramework.buildJavaScriptPostLink("Remove_" + variableSet.getName(), "VariableSets", "remove", 
                    removeKeysAndValues, true, "Are you sure you want to remove this variable set?");       
            
            htmlBodyStringBuilder.append("<tr>\n")
                .append("<td class=\"statsagg_force_word_break\">").append(variableSetDetails).append("</td>\n")
                .append("<td>").append(alter).append(", ").append(clone);
            
            if (variableSetIdsAssociatedWithVariableSetLists == null) htmlBodyStringBuilder.append(", ").append(remove);
            else if (!variableSetIdsAssociatedWithVariableSetLists.contains(variableSet.getId())) htmlBodyStringBuilder.append(", ").append(remove);
 
            htmlBodyStringBuilder.append("</td>\n").append("</tr>\n");
        }

        htmlBodyStringBuilder.append(""
                + "</tbody>\n"
                + "<tfoot> \n"
                + "  <tr>\n" 
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
