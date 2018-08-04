package com.pearson.statsagg.webui;

import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "ForgetMetrics", urlPatterns = {"/ForgetMetrics"})
public class ForgetMetrics extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ForgetMetrics.class.getName());
    
    public static final String PAGE_NAME = "Forget Metrics(s)";
    
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

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");

            String htmlBodyContents = buildForgetMetricsHtml();
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
            String result = parseParametersAndReturnResultMessage(request);

            StringBuilder htmlBuilder = new StringBuilder();
            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            String htmlBodyContent = statsAggHtmlFramework.buildHtmlBodyForPostResult(PAGE_NAME, result, "ForgetMetrics", PAGE_NAME);
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
    
    private String buildForgetMetricsHtml() {

        StringBuilder htmlBody = new StringBuilder();

        htmlBody.append(
            "<div id=\"page-content-wrapper\">\n" +
            " <!-- Keep all page content within the page-content inset div! -->\n" +
            "   <div class=\"page-content inset statsagg_page_content_font\">\n" +
            "     <div class=\"content-header\"> \n" +
            "       <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "     </div> " +
            "     <form action=\"ForgetMetrics\" method=\"POST\">\n");
        
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Forget individual metric</label>\n" +
            "  <input class=\"form-control-statsagg\" placeholder=\"Enter the exact metric key for the metric you want to 'forget'.\" name=\"ForgetMetric\" > " +
            "</div>\n");

        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Forget metrics that match a regular expression</label>\n" +
               "<a id=\"ForgetMetricsPreview\" name=\"ForgetMetricsPreview\" class=\"iframe cboxElement statsagg_forget_metrics_preview pull-right\" href=\"#\" onclick=\"generateForgetMetricsPreviewLink();\">Preview Regex Matches</a>" +
            "  <input class=\"form-control-statsagg\" placeholder=\"Enter a regex that matches the metric(s) that you want to 'forgot'.\" name=\"ForgetMetricRegex\" id=\"ForgetMetricRegex\">" +
            "</div>\n");        
        
        htmlBody.append(
            "  <button type=\"submit\" class=\"btn btn-default statsagg_page_content_font\">Submit</button>\n" +
            "</form>\n"       +          
            "</div>\n" +
            "</div>\n");
            
        return htmlBody.toString();
    }

    
    private String parseParametersAndReturnResultMessage(HttpServletRequest request) {
        
        if (request == null) {
            return null;
        }

        getForgetMetricsParameters(request);

        String returnString = "The specified metrics will be forgotten shortly.";
        
        return returnString;
    }

    private void getForgetMetricsParameters(HttpServletRequest request) {
        
        if (request == null) {
            return;
        }

        try {
            String parameter;

            parameter = request.getParameter("ForgetMetric");
            if ((parameter != null) && !parameter.isEmpty()) {
                String trimmedParameter = parameter.trim();
                GlobalVariables.immediateCleanupMetrics.put(trimmedParameter, trimmedParameter);

                String cleanMetricKey = StringUtilities.removeNewlinesFromString(trimmedParameter);
                logger.info("Action=ForgetMetrics, " + "MetricKey=\"" + cleanMetricKey + "\"");
                
                if (GlobalVariables.cleanupInvokerThread != null) GlobalVariables.cleanupInvokerThread.runCleanupThread();
            }
            
            parameter = request.getParameter("ForgetMetricRegex");
            if ((parameter != null) && !parameter.isEmpty()) {
                String trimmedParameter = parameter.trim();
                Thread forgetMetrics_RegexThread = new Thread(new ForgetMetrics_RegexThread(trimmedParameter));
                forgetMetrics_RegexThread.setPriority(Thread.MIN_PRIORITY);
                forgetMetrics_RegexThread.start();
                
                String cleanRegex = StringUtilities.removeNewlinesFromString(trimmedParameter);
                logger.info("Action=ForgetMetrics, " + "Rexex=\"" + cleanRegex + "\"");
            }

        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }
    
}
