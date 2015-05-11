package com.pearson.statsagg.webui;

import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeSet;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database.metric_group.MetricGroup;
import com.pearson.statsagg.database.metric_group.MetricGroupsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.StackTrace;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "MetricGroupMetricKeyAssociations", urlPatterns = {"/MetricGroupMetricKeyAssociations"})
public class MetricGroupMetricKeyAssociations extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroupMetricKeyAssociations.class.getName());
    
    public static final String PAGE_NAME = "Metric Group - Metric Key Associations";
    
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
        String metricGroupMetricKeyAssociations = getMetricGroupMetricKeyAssociations(name);
                
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
            metricGroupMetricKeyAssociations +
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

    private String getMetricGroupMetricKeyAssociations(String metricGroupName) {
        
        if (metricGroupName == null) {
            return "";
        }
        
        StringBuilder outputString = new StringBuilder();
        
        MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
        MetricGroup metricGroup = metricGroupsDao.getMetricGroupByName(metricGroupName);
        
        if ((metricGroup != null) && (metricGroup.getId() != null) && (metricGroup.getName() != null)) {
            outputString.append("<b>Name</b> = ").append(StatsAggHtmlFramework.htmlEncode(metricGroup.getName())).append("<br>");

            Set<String> matchingMetricKeysAssociatedWithMetricGroup = GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.get(metricGroup.getId());
            
            if (matchingMetricKeysAssociatedWithMetricGroup == null) {
                outputString.append("<b>Total Metric Group - Metric Key Associations (over the last 24 hours)</b> = ").append("0");
            }
            else {
                TreeSet<String> matchingMetricKeysAssociatedWithMetricGroupSorted;
                synchronized(matchingMetricKeysAssociatedWithMetricGroup) {
                    matchingMetricKeysAssociatedWithMetricGroupSorted = new TreeSet<>(matchingMetricKeysAssociatedWithMetricGroup);
                }
                
                int associationCount = matchingMetricKeysAssociatedWithMetricGroupSorted.size();
                outputString.append("<b>Total Metric Group - Metric Key Associations (over the last 24 hours)</b> = ").append(associationCount).append("<br><br>");
                
                if (associationCount > 0) {
                    outputString.append("<b>Metric Group - Metric Key Associations (over the last 24 hours)...</b>").append("<br>");
                    
                    int associationOutputCounter = 0;
                    outputString.append("<ul>");

                    for (String metricKey : matchingMetricKeysAssociatedWithMetricGroupSorted) {
                        if (associationOutputCounter < 1000)  {
                            outputString.append("<li>");
                            outputString.append("<a href=\"MetricRecentValues?MetricKey=").append(StatsAggHtmlFramework.urlEncode(metricKey)).append("\">");
                            outputString.append(StatsAggHtmlFramework.htmlEncode(metricKey)).append("</a>");
                            outputString.append("</li>");
                        }
                        else {
                            break;
                        }

                        associationOutputCounter++;
                    }

                    int numAssociationsNotOutputted = associationCount - associationOutputCounter;
                    if (numAssociationsNotOutputted > 0) {
                        outputString.append("<li>").append(numAssociationsNotOutputted).append(" more...").append("</li>");
                    }
                    
                    outputString.append("</ul>");
                }
            }
            
        }
        
        return outputString.toString();
    }

}
