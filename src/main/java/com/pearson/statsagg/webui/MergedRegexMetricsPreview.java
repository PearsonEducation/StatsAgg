package com.pearson.statsagg.webui;

import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import static com.pearson.statsagg.webui.CreateMetricGroup.getMetricGroupRegexsFromMetricGroupParameters;
import static com.pearson.statsagg.webui.RegexTester.getRegexMatchesHtml;
import com.pearson.statsagg.utilities.StackTrace;
import com.pearson.statsagg.utilities.StringUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "MergedRegexMetricsPreview", urlPatterns = {"/MergedRegexMetricsPreview"})
public class MergedRegexMetricsPreview extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(MergedRegexMetricsPreview.class.getName());
    
    public static final String PAGE_NAME = "Merged Regex - Metrics Preview";
    
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
        
        TreeSet<String> regexs = getMetricGroupRegexsFromMetricGroupParameters(request);
        
        List regexs_List;
        if (regexs != null) regexs_List = new ArrayList<>(regexs);
        else regexs_List = new ArrayList<>();
        
        String mergedRegex = StringUtilities.createMergedRegex(regexs_List);
        
        String regexMatchesHtml = getRegexMatchesHtml(mergedRegex, 1000);
        
        try {  
            StringBuilder htmlBuilder = new StringBuilder("");

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            
            String htmlBody =
            "<body>" +
            "  <div id=\"page-content-wrapper\">\n" +
            "    <!-- Keep all page content within the page-content inset div! -->\n" +
            "    <div class=\"page-content inset\">\n" +
                 regexMatchesHtml +
            "    </div>\n" +
            "  </div>\n" +
            "</body>";
            
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
    
}
