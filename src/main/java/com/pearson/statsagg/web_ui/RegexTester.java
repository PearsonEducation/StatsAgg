package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.alerts.MetricAssociation;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.MetricTimestampAndValue;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "RegexTester", urlPatterns = {"/RegexTester"})
public class RegexTester extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(RegexTester.class.getName());
    
    public static final String PAGE_NAME = "Regex Tester";
    
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

            String countAllMatchesParameter = request.getParameter("CountAllMatches");
            boolean countAllMatches = false;
            if ((countAllMatchesParameter != null) && (countAllMatchesParameter.equalsIgnoreCase("true") || (countAllMatchesParameter.contains("on")))) countAllMatches = true;
            
            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");

            String htmlBodyContents = buildRegexTesterHtml("", "", countAllMatches);
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
            String regexParameter = request.getParameter("Regex");
            
            String countAllMatchesParameter = request.getParameter("CountAllMatches");
            boolean countAllMatches = false;
            if ((countAllMatchesParameter != null) && (countAllMatchesParameter.equalsIgnoreCase("true") || (countAllMatchesParameter.contains("on")))) countAllMatches = true;
            
            Set<String> metricKeys;
            
            if (countAllMatches) metricKeys = MetricAssociation.getRegexMatches(GlobalVariables.metricKeysLastSeenTimestamp.keySet(), regexParameter, null, -1);
            else metricKeys = MetricAssociation.getRegexMatches(GlobalVariables.metricKeysLastSeenTimestamp.keySet(), regexParameter, null, 1001);
            
            String regexMatchesHtml = getRegexMatchesHtml(metricKeys, 1000, countAllMatches);
  
            StringBuilder htmlBuilder = new StringBuilder();

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");

            String htmlBodyContents = buildRegexTesterHtml(regexParameter, regexMatchesHtml, countAllMatches);
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
    
    private String buildRegexTesterHtml(String regex, String regexMatches, boolean countAllMatches) {

        StringBuilder htmlBody = new StringBuilder();

        htmlBody.append(
            "<div id=\"page-content-wrapper\">\n" +
            "<!-- Keep all page content within the page-content inset div! -->\n" +
            "<div class=\"page-content inset statsagg_page_content_font\">\n" +
            "  <div class=\"content-header\"> \n" +
            "    <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "  </div> " +
            "  <form action=\"RegexTester\" method=\"POST\">\n");
        
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Regex to test</label>\n" +
            "  <input class=\"form-control-statsagg\" placeholder=\"Enter a regex that you want to test against (or more) recently seen metrics.\" name=\"Regex\" ");
            
        if ((regex != null) && (!regex.isEmpty())) {
            htmlBody.append(" value=\"").append(StatsAggHtmlFramework.htmlEncode(regex, true)).append("\"");
        }
        
        htmlBody.append(">\n</div>\n");
       
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Count all matches?&nbsp;&nbsp;</label>\n" +
            "  <input name=\"CountAllMatches\" id=\"CountAllMatches\" type=\"checkbox\" ");
        
        if (countAllMatches) htmlBody.append(" checked=\"checked\"");

        htmlBody.append(">\n</div>\n<br>");
        
        
        htmlBody.append(
            "  <button type=\"submit\" class=\"btn btn-default statsagg_page_content_font\">Submit</button>\n" +
            "</form>\n");
        
        if (!regexMatches.equals("")) {
            htmlBody.append("<br><hr><br>");
        }
        
        htmlBody.append("<div class=\"statsagg_force_word_wrap\">");
        htmlBody.append(regexMatches);
        htmlBody.append("</div>");
        
        htmlBody.append("</div>\n" + "</div>\n");
            
        return htmlBody.toString();
    }
    
    public static String getRegexMatchesHtml(Set<String> metricKeys, int metricMatchLimit, boolean countAllMatches) {
        List<String> metricKeysList = null;
        
        if (metricKeys != null) {
            metricKeysList = new ArrayList<>(metricKeys);
            Collections.sort(metricKeysList);
        }
        
        return getRegexMatchesHtml(metricKeysList, metricMatchLimit, countAllMatches);
    }
    
    public static String getRegexMatchesHtml(List<String> metricKeys, int metricMatchLimit, boolean countAllMatches) {
        
        if (metricKeys == null) {
            return "<b>Regex Match Count</b> = 0";
        }
        
        StringBuilder outputString = new StringBuilder();
                
        String metricKeyCountString;
        if ((metricKeys.size() > metricMatchLimit) && !countAllMatches) metricKeyCountString = "More than " + Integer.toString(metricMatchLimit);
        else metricKeyCountString = Integer.toString(metricKeys.size());
        
        outputString.append("<b>Regex Match Count</b> = ").append(metricKeyCountString).append("<br><br>");

        if (metricKeys.size() > 0) {
            outputString.append("<b>Matching Metrics...</b>").append("<br>");

            int associationOutputCounter = 0;
            outputString.append("<ul>");

            for (String metricKey : metricKeys) {
                List<MetricTimestampAndValue> metricTimestampsAndValues = MetricGroupMetricKeyAssociations.getSortedMetricTimestampsAndValues(metricKey);
                BigDecimal mostRecentValue = null;
                if ((metricTimestampsAndValues != null) && !metricTimestampsAndValues.isEmpty()) mostRecentValue = metricTimestampsAndValues.get(metricTimestampsAndValues.size() - 1).getMetricValue();

                outputString.append("<li>");
                outputString.append("<a class=\"iframe cboxElement\" href=\"MetricRecentValues?ExcludeNavbar=true&amp;MetricKey=").append(StatsAggHtmlFramework.urlEncode(metricKey)).append("\">");
                outputString.append(StatsAggHtmlFramework.htmlEncode(metricKey)).append("</a>");
                if (mostRecentValue != null) outputString.append(" = ").append(mostRecentValue.stripTrailingZeros().toPlainString()).append(" (most recent value)");
                outputString.append("</li>");

                associationOutputCounter++;
                if (associationOutputCounter >= metricMatchLimit) break;
            }

            outputString.append("</ul>");
        }
      
        if (countAllMatches && ((metricKeys.size() - metricMatchLimit) > 0)) {
            outputString.append("<b>... and ").append((metricKeys.size() - metricMatchLimit)).append(" more matches</b>");
        }
        
        return outputString.toString();
    }
    
}
