package com.pearson.statsagg.webui;

import java.io.PrintWriter;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database.metric_group.MetricGroup;
import com.pearson.statsagg.database.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database.metric_group_regex.MetricGroupRegex;
import com.pearson.statsagg.database.metric_group_regex.MetricGroupRegexsDao;
import com.pearson.statsagg.database.metric_group_tags.MetricGroupTag;
import com.pearson.statsagg.database.metric_group_tags.MetricGroupTagsDao;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.utilities.StackTrace;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "MetricGroupDetails", urlPatterns = {"/MetricGroupDetails"})
public class MetricGroupDetails extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroupDetails.class.getName());
    
    public static final String PAGE_NAME = "Metric Group Details";
    
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
        String metricGroupDetails = getMetricGroupDetailsString(name);
                
        try {  
            StringBuilder htmlBuilder = new StringBuilder("");

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            
            String htmlBody = statsAggHtmlFramework.createHtmlBody(
            "<div id=\"page-content-wrapper\">\n" +
            "  <!-- Keep all page content within the page-content inset div! -->\n" +
            "  <div class=\"page-content inset\">\n" +
            "    <div class=\"content-header\"> \n" +
            "      <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "    </div> " +
            metricGroupDetails +
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

    private String getMetricGroupDetailsString(String metricGroupName) {
        
        if (metricGroupName == null) {
            return "";
        }
        
        StringBuilder outputString = new StringBuilder("");
        
        MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
        MetricGroup metricGroup = metricGroupsDao.getMetricGroupByName(metricGroupName);
        
        if (metricGroup != null) {
            MetricGroupRegexsDao metricGroupRegexsDao = new MetricGroupRegexsDao();
            List<MetricGroupRegex> metricGroupRegexs =  metricGroupRegexsDao.getMetricGroupRegexsByMetricGroupId(metricGroup.getId());
                  
            MetricGroupTagsDao metricGroupTagsDao = new MetricGroupTagsDao();
            List<MetricGroupTag> metricGroupTags =  metricGroupTagsDao.getMetricGroupTagsByMetricGroupId(metricGroup.getId());
            
            outputString.append("<b>Name</b> = ");
            if (metricGroup.getName() != null) outputString.append(StatsAggHtmlFramework.htmlEncode(metricGroup.getName())).append("<br>");
            else outputString.append("N/A <br>");
            
            outputString.append("<b>Description</b> = ");
            if (metricGroup.getDescription() != null) {
                String encodedMetricGroupDescription = StatsAggHtmlFramework.htmlEncode(metricGroup.getDescription());
                outputString.append(encodedMetricGroupDescription.replaceAll("\n", "<br>")).append("<br><br>");
            }
            else outputString.append("<br><br>");
            
            if ((metricGroupRegexs != null) && !metricGroupRegexs.isEmpty()) {
                int i = 1;
                for (MetricGroupRegex metricGroupRegex : metricGroupRegexs) {
                    outputString.append("<b>Regex #").append(i).append("</b> = ").append(StatsAggHtmlFramework.htmlEncode(metricGroupRegex.getPattern())).append("<br>");
                    i++;
                }
            }
            
            if ((metricGroupRegexs == null) || metricGroupRegexs.isEmpty()) outputString.append("<br>");
            outputString.append("<br>");
            
            if ((metricGroupTags != null) && !metricGroupTags.isEmpty()) {
                int i = 1;
                for (MetricGroupTag metricGroupTag : metricGroupTags) {
                    outputString.append("<b>Tag #").append(i).append("</b> = ").append(StatsAggHtmlFramework.htmlEncode(metricGroupTag.getTag())).append("<br>");
                    i++;
                }
            }
        }
        
        return outputString.toString();
    }

}
