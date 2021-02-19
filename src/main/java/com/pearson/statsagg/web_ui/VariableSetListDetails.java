package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.database_objects.variable_set.VariableSet;
import com.pearson.statsagg.database_objects.variable_set.VariableSetsDao;
import com.pearson.statsagg.globals.DatabaseConnections;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database_objects.variable_set_list.VariableSetList;
import com.pearson.statsagg.database_objects.variable_set_list.VariableSetListsDao;
import com.pearson.statsagg.database_objects.variable_set_list_entry.VariableSetListEntriesDao;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jeffrey Schmidt
 */
public class VariableSetListDetails extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(VariableSetListDetails.class.getName());
    
    public static final String PAGE_NAME = "Variable Set List Details";
    
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
        String variableSetListDetails = getNotificationDetailsString(name);
                
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
            variableSetListDetails +
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

    private String getNotificationDetailsString(String variableSetListName) {
        
        if (variableSetListName == null) {
            return "<b>No variable set list specified</b>";
        }
        
        Connection connection = DatabaseConnections.getConnection();
        VariableSetList variableSetList = VariableSetListsDao.getVariableSetList(connection, false, variableSetListName);
        List<Integer> variableSetIds = (variableSetList != null) ? VariableSetListEntriesDao.getVariableSetIds_ForVariableSetListId(connection, false, variableSetList.getId()) : new ArrayList<>();
        Set<String> variableSetNames = new TreeSet<>();
        if (variableSetIds != null) {
            for (Integer variableSetId : variableSetIds) {
                VariableSet variableSet = VariableSetsDao.getVariableSet(connection, false, variableSetId);
                if ((variableSet != null) && (variableSet.getName() != null)) variableSetNames.add(variableSet.getName());
            }
        }
        DatabaseUtils.cleanup(connection);
        
        if (variableSetList == null) {
            return "<b>Variable set list not found</b>";
        }
        else {     
            StringBuilder outputString = new StringBuilder();
            
            outputString.append("<b>Name:</b> ").append(StatsAggHtmlFramework.htmlEncode(variableSetList.getName())).append("<br>");
            
            outputString.append("<b>ID:</b> ").append(variableSetList.getId()).append("<br><br>");
            
            outputString.append("<b>Description:</b> ");
            if (variableSetList.getDescription() != null) {
                outputString.append("<br>");
                String encodedMetricGroupDescription = StatsAggHtmlFramework.htmlEncode(variableSetList.getDescription());
                outputString.append(encodedMetricGroupDescription.replaceAll("\n", "<br>")).append("<br><br>");
            }
            else outputString.append("<br><br>");
            
            outputString.append("<b>Variable Sets:</b> ");
            if (!variableSetNames.isEmpty()) {
                outputString.append("<ul>");
                for (String variableSetName : variableSetNames) {
                    outputString.append("<li>");
                    String variableSetPopupLink = "<a class=\"iframe cboxElement\" href=\"VariableSetDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(variableSetName) + "\">" + StatsAggHtmlFramework.htmlEncode(variableSetName) + "</a>";
                    outputString.append(variableSetPopupLink);
                    outputString.append("</li>");
                }
                outputString.append("</ul>");
            }
                
            return outputString.toString();
        }
    }

}
