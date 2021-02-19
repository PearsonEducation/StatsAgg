package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.globals.DatabaseConnections;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database_objects.variable_set.VariableSet;
import com.pearson.statsagg.database_objects.variable_set.VariableSetsDao;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jeffrey Schmidt
 */
public class VariableSetDetails extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(VariableSetDetails.class.getName());
    
    public static final String PAGE_NAME = "Variable Set Details";
    
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
        String variableSetDetails = getNotificationDetailsString(name);
                
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
            variableSetDetails +
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

    private String getNotificationDetailsString(String variableSetName) {
        
        if (variableSetName == null) {
            return "<b>No variable set specified</b>";
        }
        
        VariableSet variableSet = VariableSetsDao.getVariableSet(DatabaseConnections.getConnection(), true, variableSetName);
        
        if (variableSet == null) {
            return "<b>Variable set not found</b>";
        }
        else {     
            StringBuilder outputString = new StringBuilder();
            
            outputString.append("<b>Name:</b> ").append(StatsAggHtmlFramework.htmlEncode(variableSet.getName())).append("<br>");
            
            outputString.append("<b>ID:</b> ").append(variableSet.getId()).append("<br><br>");
                        
            outputString.append("<b>Description:</b>");
            if (variableSet.getDescription() != null) {
                outputString.append("<br>");
                String encodedMetricGroupDescription = StatsAggHtmlFramework.htmlEncode(variableSet.getDescription());
                outputString.append(encodedMetricGroupDescription.replaceAll("\n", "<br>")).append("<br><br>");
            }
            else outputString.append("<br><br>");
            
            outputString.append("<b>Variables:</b> ");
            if ((variableSet.getVariables() != null) && !variableSet.getVariables().isEmpty()) {
                String metricSuspensionRegexesFormatted = StringUtils.replace(StatsAggHtmlFramework.htmlEncode(variableSet.getVariables()), "\n", "<br>&nbsp;&nbsp;&nbsp;");
                outputString.append("<br>&nbsp;&nbsp;&nbsp;").append(metricSuspensionRegexesFormatted).append("<br>");
            }
            else outputString.append("N/A <br>");    
                
            return outputString.toString();
        }
    }

}
