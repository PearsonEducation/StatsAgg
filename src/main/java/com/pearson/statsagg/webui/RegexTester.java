package com.pearson.statsagg.webui;

import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.StackTrace;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.owasp.encoder.Encode;
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
        
        response.setContentType("text/html");
        PrintWriter out = null;
    
        try {  
            StringBuilder htmlBuilder = new StringBuilder();

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");

            String htmlBodyContents = buildRegexTesterHtml("", "");
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
        
        PrintWriter out = null;
        response.setContentType("text/html");    
        
        try {
            String parameter = request.getParameter("Regex");
            Set<String> metricKeys = getRegexMatches(GlobalVariables.metricKeysLastSeenTimestamp_UpdateOnResend.keySet(), parameter, null, 1000);
            String regexMatchesHtml = getRegexMatchesHtml(metricKeys, 1000);
  
            StringBuilder htmlBuilder = new StringBuilder();

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");

            String htmlBodyContents = buildRegexTesterHtml(parameter, regexMatchesHtml);
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
    
    private String buildRegexTesterHtml(String regex, String regexMatches) {

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
            htmlBody.append(" value=\"").append(Encode.forHtmlAttribute(regex)).append("\"");
        }
        
        htmlBody.append(">\n</div>\n");
       
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
    
    // if metricMatchLimit < 0, then it is treated as infinite
    public static Set<String> getRegexMatches(Set<String> metricKeys, String matchRegex, String blacklistRegex, int metricMatchLimit) {
        
        if (matchRegex == null) {
            return null;
        }
        
        Pattern matchPattern = null, blacklistPattern = null;
        
        try {
            matchPattern = Pattern.compile(matchRegex.trim());
            if ((blacklistRegex != null) && !blacklistRegex.isEmpty()) blacklistPattern = Pattern.compile(blacklistRegex.trim());
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        Set<String> matchingMetricKeys = new HashSet<>();
        
        if (matchPattern != null) {
            int matchCounter = 0;
            boolean isAnyMatchLimt = (metricMatchLimit >= 0);
            
            for (String metricKey : metricKeys) {
                Matcher matcher = matchPattern.matcher(metricKey);
                
                if (matcher.matches()) {
                    if (blacklistPattern != null) {
                        Matcher blacklistMatcher = blacklistPattern.matcher(metricKey);
                        
                        if (!blacklistMatcher.matches()) {
                            matchingMetricKeys.add(metricKey);
                            matchCounter++;
                        }
                    }
                    else {
                        matchingMetricKeys.add(metricKey);
                        matchCounter++;
                    }
                }

                if (isAnyMatchLimt && (matchCounter == metricMatchLimit)) {
                    break;
                }
            }
        }
 
        return matchingMetricKeys;
    }
    
    public static String getRegexMatchesHtml(Set<String> metricKeys, int metricMatchLimit) {
        List<String> metricKeysList = null;
        
        if (metricKeys != null) {
            metricKeysList = new ArrayList<>(metricKeys);
            Collections.sort(metricKeysList);
        }
        
        return getRegexMatchesHtml(metricKeysList, metricMatchLimit);
    }
    
    public static String getRegexMatchesHtml(List<String> metricKeys, int metricMatchLimit) {
        
        if (metricKeys == null) {
            return "<b>Regex Match Count</b> = 0";
        }
        
        StringBuilder outputString = new StringBuilder();
                
        String metricKeyCountString;
        if (metricKeys.size() > metricMatchLimit) metricKeyCountString = "More than " + Integer.toString(metricMatchLimit);
        else metricKeyCountString = Integer.toString(metricKeys.size());
        
        outputString.append("<b>Regex Match Count</b> = ").append(metricKeyCountString).append("<br><br>");

        if (metricKeys.size() > 0) {
            outputString.append("<b>Matching Metrics...</b>").append("<br>");

            int outputCounter = 0;
            outputString.append("<ul>");

            for (String metricKey : metricKeys) {

                if (outputCounter < metricMatchLimit)  {
                    outputString.append("<li>").append(StatsAggHtmlFramework.htmlEncode(metricKey)).append("</li>");
                }
                else {
                    break;
                }

                outputCounter++;
            }
            
            outputString.append("</ul>");
        }
        
        return outputString.toString();
    }
    
}
