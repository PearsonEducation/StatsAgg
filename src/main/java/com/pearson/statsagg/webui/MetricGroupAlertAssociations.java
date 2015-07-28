package com.pearson.statsagg.webui;

import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.metric_group.MetricGroup;
import com.pearson.statsagg.database_objects.metric_group.MetricGroupsDao;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.utilities.StackTrace;
import java.util.Collections;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "MetricGroupAlertAssociations", urlPatterns = {"/MetricGroupAlertAssociations"})
public class MetricGroupAlertAssociations extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroupAlertAssociations.class.getName());
    
    public static final String PAGE_NAME = "Metric Group - Alert Associations";
    
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
        
        response.setContentType("text/html");
        PrintWriter out = null;
    
        String name = request.getParameter("Name");
        String metricGroup_AlertAssociations = getMetricGroup_AlertAssociations(name);
                
        try {  
            StringBuilder htmlBuilder = new StringBuilder();

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            
            String htmlBody = statsAggHtmlFramework.createHtmlBody(
            "<div id=\"page-content-wrapper\">\n" +
            "<!-- Keep all page content within the page-content inset div! -->\n" +
            "  <div class=\"page-content inset statsagg_page_content_font\">\n" +
            "    <div class=\"content-header\"> \n" +
            "      <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "    </div> " +
            "    <div class=\"statsagg_force_word_wrap\">" +
            metricGroup_AlertAssociations +
            "    </div>\n" +
            "  </div>\n" +
            "</div>\n");
            
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

    private String getMetricGroup_AlertAssociations(String metricGroupName) {
        
        if (metricGroupName == null) {
            return "<b>No metric group specified</b>";
        }
 
        MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
        MetricGroup metricGroup = metricGroupsDao.getMetricGroupByName(metricGroupName);
        if (metricGroup == null) return "<b>Metric Group not found</b>";
        
        StringBuilder outputString = new StringBuilder();
        
        outputString.append("<b>Metric Group Name</b> = ").append(StatsAggHtmlFramework.htmlEncode(metricGroup.getName())).append("<br>");

        AlertsDao alertsDao = new AlertsDao();
        List<String> alertNames = alertsDao.getAlertsAssociatedWithMetricGroupId(metricGroup.getId());
        if (alertNames != null) Collections.sort(alertNames);
        
        if ((alertNames == null) || alertNames.isEmpty()) {
            outputString.append("<b>Total Associations</b> = ").append("0");
            return outputString.toString();
        }

        int associationCount = alertNames.size();
        outputString.append("<b>Total Associations</b> = ").append(associationCount).append("<br><br>");
        if (associationCount <= 0) return outputString.toString();

        outputString.append("<b>Associations...</b>").append("<br>");

        outputString.append("<ul>");
        
        for (String alertName : alertNames) {
            String alertDetailsUrl = "<a href=\"AlertDetails?Name=" + StatsAggHtmlFramework.urlEncode(alertName) + "\">" + StatsAggHtmlFramework.htmlEncode(alertName) + "</a>";
            outputString.append("<li>").append(alertDetailsUrl).append("</li>");
        }

        outputString.append("</ul>");
    
        return outputString.toString();
    }

}
