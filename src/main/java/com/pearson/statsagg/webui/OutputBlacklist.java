package com.pearson.statsagg.webui;

import com.pearson.statsagg.alerts.MetricAssociation;
import com.pearson.statsagg.controller.threads.MetricAssociationOutputBlacklistInvokerThread;
import com.pearson.statsagg.database_objects.metric_group.MetricGroup;
import com.pearson.statsagg.database_objects.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database_objects.output_blacklist.OutputBlacklistDao;
import com.pearson.statsagg.globals.GlobalVariables;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.utilities.StackTrace;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "OutputBlacklist", urlPatterns = {"/OutputBlacklist"})
public class OutputBlacklist extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(OutputBlacklist.class.getName());
    
    public static final String PAGE_NAME = "Output Blacklist";
    
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

            List<String> additionalJavascript = new ArrayList<>();
            additionalJavascript.add("js/statsagg_output_blacklist.js");
            
            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            String htmlBodyContents = buildOutputBlacklistHtml();
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
        
        response.setContentType("text/html");
        PrintWriter out = null;

        String metricGroupName = request.getParameter("MetricGroupName");
        boolean updateSuccess = updateOutputBlacklistMetricGroupId(metricGroupName);
        if (updateSuccess) {
            MetricAssociation.IsMetricGroupChangeOutputBlacklist.set(true);
            GlobalVariables.metricAssociationOutputBlacklistInvokerThread.runMetricAssociationOutputBlacklistThread();
        }
                
        try {
            String result;
            if (updateSuccess) result = "Successfully updated the output blacklist's metric group association.";
            else result = "Failed to update the output blacklist's metric group association.";
            
            StringBuilder htmlBuilder = new StringBuilder();
            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            String htmlBodyContent = statsAggHtmlFramework.buildHtmlBodyForPostResult(PAGE_NAME + " Update ", StatsAggHtmlFramework.htmlEncode(result), "OutputBlacklist", PAGE_NAME);
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
    
    private String buildOutputBlacklistHtml() {

        StringBuilder htmlBody = new StringBuilder();

        htmlBody.append(
            "<div id=\"page-content-wrapper\">\n" +
            " <!-- Keep all page content within the page-content inset div! -->\n" +
            "   <div class=\"page-content inset statsagg_page_content_font\">\n" +
            "     <div class=\"content-header\"> \n" +
            "       <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "     </div> ");

        String metricGroupName = getMetricGroupNameAssociatedWithOutputBlacklist();
        
        String metricAssociationsLink = "";
        com.pearson.statsagg.database_objects.output_blacklist.OutputBlacklist outputBlacklist = OutputBlacklistDao.getSingleOutputBlacklistRow();
        if ((outputBlacklist != null) && (outputBlacklist.getMetricGroupId() != null) && (outputBlacklist.getMetricGroupId() >= 0)) {
            Set<String> matchingMetricKeysAssociatedWithOutputBlacklistMetricGroup = GlobalVariables.matchingMetricKeysAssociatedWithOutputBlacklistMetricGroup.get(outputBlacklist.getMetricGroupId());
            int matchingMetricKeysAssociatedWithOutputBlacklistMetricGroup_Count = 0;
            if (matchingMetricKeysAssociatedWithOutputBlacklistMetricGroup != null) matchingMetricKeysAssociatedWithOutputBlacklistMetricGroup_Count = matchingMetricKeysAssociatedWithOutputBlacklistMetricGroup.size();
            metricAssociationsLink = "<b>Current Output Blacklist Metric Associations: </b>" + 
                    "<a class=\"iframe cboxElement\" href=\"MetricGroupMetricKeyAssociations?ExcludeNavbar=true&amp;Name=" + 
                    StatsAggHtmlFramework.urlEncode(metricGroupName) + "\">" + 
                    StatsAggHtmlFramework.htmlEncode(Integer.toString(matchingMetricKeysAssociatedWithOutputBlacklistMetricGroup_Count)) + "</a>";
        }
        
        htmlBody.append(metricAssociationsLink);
        if (!metricAssociationsLink.isEmpty()) htmlBody.append("<br><br>").append("\n"); 
        
        htmlBody.append(
            "<form action=\"OutputBlacklist\" method=\"POST\">\n" +
            "<div class=\"form-group\" id=\"MetricGroupName_Lookup\">\n" +
            "  <label class=\"label_small_margin\">Metric group name</label>\n" +
            "  <input class=\"typeahead form-control-statsagg\" autocomplete=\"off\" name=\"MetricGroupName\" id=\"MetricGroupName\" ");

        if ((metricGroupName != null)) htmlBody.append(" value=\"").append(Encode.forHtmlAttribute(metricGroupName)).append("\"");
        htmlBody.append(">\n</div>\n");
        
        htmlBody.append("<button type=\"submit\" class=\"btn btn-default statsagg_page_content_font\">Submit</button>\n");
        htmlBody.append("</form>\n</div>\n</div>\n");
            
        return htmlBody.toString();
    }

    private static String getMetricGroupNameAssociatedWithOutputBlacklist() {
        
        OutputBlacklistDao outputBlacklistDao = new OutputBlacklistDao();
        List<com.pearson.statsagg.database_objects.output_blacklist.OutputBlacklist> outputBlacklists = outputBlacklistDao.getAllDatabaseObjectsInTable();

        if ((outputBlacklists != null) && !outputBlacklists.isEmpty()) {
            Integer metricGroupId = null;
            
            for (com.pearson.statsagg.database_objects.output_blacklist.OutputBlacklist outputBlacklist : outputBlacklists) {
                if ((outputBlacklist != null) && (outputBlacklist.getMetricGroupId() != null) && outputBlacklist.getMetricGroupId() > -1) {
                    metricGroupId = outputBlacklist.getMetricGroupId();
                    break;
                }
            }
            
            if (metricGroupId != null) {
                MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
                MetricGroup metricGroup = metricGroupsDao.getMetricGroup(metricGroupId);
                if ((metricGroup != null) && (metricGroup.getName() != null)) return metricGroup.getName();
            }
        }
        
        return null;
    }
    
    private static boolean updateOutputBlacklistMetricGroupId(String metricGroupName) {
        
        boolean upsertSuccess = false;
        
        try {
            MetricGroup metricGroup = null;
            boolean wasMetricGroupNameSpecified = false;
            
            if ((metricGroupName != null) && !metricGroupName.trim().isEmpty()) {
                wasMetricGroupNameSpecified = true;
                MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
                metricGroup = metricGroupsDao.getMetricGroupByName(metricGroupName);
            }
            
            com.pearson.statsagg.database_objects.output_blacklist.OutputBlacklist outputBlacklist = OutputBlacklistDao.getSingleOutputBlacklistRow();

            if ((metricGroup != null) && (metricGroup.getId() != null)) {
                if (outputBlacklist == null) outputBlacklist = new com.pearson.statsagg.database_objects.output_blacklist.OutputBlacklist(-1, metricGroup.getId());
                else outputBlacklist.setMetricGroupId(metricGroup.getId());
                OutputBlacklistDao outputBlacklistDao = new OutputBlacklistDao();
                upsertSuccess = outputBlacklistDao.upsert(outputBlacklist);
            }
            else if (wasMetricGroupNameSpecified) {
                return false;
            }
            else if (outputBlacklist != null) {
                outputBlacklist.setMetricGroupId(null);
                OutputBlacklistDao outputBlacklistDao = new OutputBlacklistDao();
                upsertSuccess = outputBlacklistDao.upsert(outputBlacklist);
            }
            else {
                outputBlacklist = new com.pearson.statsagg.database_objects.output_blacklist.OutputBlacklist(-1, null);
                OutputBlacklistDao outputBlacklistDao = new OutputBlacklistDao();
                upsertSuccess = outputBlacklistDao.upsert(outputBlacklist);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return upsertSuccess;
    }
    
}
