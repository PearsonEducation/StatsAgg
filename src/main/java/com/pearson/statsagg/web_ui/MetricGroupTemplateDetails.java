package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.globals.DatabaseConnections;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database_objects.metric_group_templates.MetricGroupTemplate;
import com.pearson.statsagg.database_objects.metric_group_templates.MetricGroupTemplatesDao;
import com.pearson.statsagg.database_objects.variable_set_list.VariableSetList;
import com.pearson.statsagg.database_objects.variable_set_list.VariableSetListsDao;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroupTemplateDetails extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroupTemplateDetails.class.getName());
    
    public static final String PAGE_NAME = "Metric Group Template Details";
    
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
        String metricGroupTemplateDetails = getMetricGroupTemplateDetailsString(name, excludeNavbar);
                
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
            metricGroupTemplateDetails +
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

    private String getMetricGroupTemplateDetailsString(String metricGroupTemplateName, boolean excludeNavbar) {
        
        if (metricGroupTemplateName == null) {
            return "<b>No metric group template specified</b>";
        }
        
        MetricGroupTemplate metricGroupTemplate = MetricGroupTemplatesDao.getMetricGroupTemplate(DatabaseConnections.getConnection(), true, metricGroupTemplateName);
        
        if (metricGroupTemplate == null) {
            return "<b>Metric group template not found</b>";
        }
        else {
            StringBuilder outputString = new StringBuilder();

            VariableSetList variableSetList = null;
            if (metricGroupTemplate.getVariableSetListId() != null) variableSetList = VariableSetListsDao.getVariableSetList(DatabaseConnections.getConnection(), true, metricGroupTemplate.getVariableSetListId());

            outputString.append("<b>Name</b> = ");
            if (metricGroupTemplate.getName() != null) outputString.append(StatsAggHtmlFramework.htmlEncode(metricGroupTemplate.getName())).append("<br>");
            else outputString.append("N/A <br>");
            
            outputString.append("<b>ID</b> = ");
            if (metricGroupTemplate.getName() != null) outputString.append(metricGroupTemplate.getId()).append("<br>");
            else outputString.append("N/A <br>");
            
            outputString.append("<b>Variable set list</b> = ");
            if (variableSetList != null) {
                String variableSetListDetailsPopup = "<a class=\"iframe cboxElement\" href=\"VariableSetListDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(variableSetList.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(variableSetList.getName()) + "</a>";
                outputString.append(variableSetListDetailsPopup).append("<br>");
            }
            else outputString.append("<br>");

            outputString.append("<b>Metric group name variable</b> = ").append(StatsAggHtmlFramework.htmlEncode(metricGroupTemplate.getMetricGroupNameVariable())).append("<br>");

            outputString.append("<b>Description variable</b> = ");
            if (metricGroupTemplate.getDescriptionVariable() != null) {
                String encodedMetricGroupDescriptionVariable = StatsAggHtmlFramework.htmlEncode(metricGroupTemplate.getDescriptionVariable());
                outputString.append(encodedMetricGroupDescriptionVariable.replaceAll("\n", "<br>")).append("<br><br>");
            }
            else outputString.append("<br><br>");
            
            outputString.append("<b>Match Regexes Variable</b> = ");
            if (metricGroupTemplate.getMatchRegexesVariable() != null) {
                String encodedMatchRegexesVariable = StatsAggHtmlFramework.htmlEncode(metricGroupTemplate.getMatchRegexesVariable());
                outputString.append(encodedMatchRegexesVariable.replaceAll("\n", "<br>")).append("<br><br>");
            }
            else outputString.append("<br><br>");
            
            outputString.append("<b>Blacklist Regexes Variable</b> = ");
            if (metricGroupTemplate.getBlacklistRegexesVariable() != null) {
                String encodedBlacklistRegexesVariable = StatsAggHtmlFramework.htmlEncode(metricGroupTemplate.getBlacklistRegexesVariable());
                outputString.append(encodedBlacklistRegexesVariable.replaceAll("\n", "<br>")).append("<br><br>");
            }
            else outputString.append("<br><br>");
            
            outputString.append("<b>Tags Variable</b> = ");
            if (metricGroupTemplate.getTagsVariable() != null) {
                String encodedTagsVariable = StatsAggHtmlFramework.htmlEncode(metricGroupTemplate.getTagsVariable());
                outputString.append(encodedTagsVariable.replaceAll("\n", "<br>")).append("<br><br>");
            }
            else outputString.append("<br><br>");

            outputString.append("<b>Is marked for delete?</b> = ");
            if (metricGroupTemplate.isMarkedForDelete() != null) outputString.append(metricGroupTemplate.isMarkedForDelete()).append("<br>");
            else outputString.append("N/A <br>");
            
            return outputString.toString();
        }
    }

}
