package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.alerts.MetricAssociation;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.database_objects.metric_group.MetricGroup;
import com.pearson.statsagg.database_objects.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database_objects.suspensions.Suspension;
import com.pearson.statsagg.database_objects.suspensions.SuspensionsDao;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class Benchmark extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(Benchmark.class.getName());
    
    public static final String PAGE_NAME = "Benchmark";
    
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

            String htmlBodyContents = buildMetricGroupBenckmarkHtml(-1, false);
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
            String parameter = request.getParameter("MetricCount");
            if (parameter != null) parameter = parameter.trim();
            Integer metricCountInteger = null;
            
            try {
                metricCountInteger = Integer.parseInt(parameter);
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
            
            StringBuilder htmlBuilder = new StringBuilder();

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");

            String htmlBodyContents = buildMetricGroupBenckmarkHtml(metricCountInteger, true);
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
    
    private String buildMetricGroupBenckmarkHtml(Integer metricKeysToBenchmark_Count, boolean performBenchmark) {

        StringBuilder htmlBody = new StringBuilder();

        htmlBody.append(
            "<div id=\"page-content-wrapper\">\n" +
            "<!-- Keep all page content within the page-content inset div! -->\n" +
            "<div class=\"page-content inset statsagg_page_content_font\">\n" +
            "  <div class=\"content-header\"> \n" +
            "    <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "  </div> " +
            "  <form action=\"Benchmark\" method=\"POST\">\n");
        
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">The page is used to benchmark the regular expressions used by Metric Groups & Metric Suspensions.</label><br>\n" +
            "  <label class=\"label_small_margin\">Number of metrics to test against. If unsure, 50000 is a good starting point.</label>\n" +
            "  <input class=\"form-control-statsagg\" placeholder=\"How many metrics would you like to run each metric group regex against?\" name=\"MetricCount\" ");
        
        if ((metricKeysToBenchmark_Count != null) && (metricKeysToBenchmark_Count > 0)) {
            htmlBody.append(" value=\"").append(metricKeysToBenchmark_Count).append("\"");
        }
        
        htmlBody.append(">\n</div>\n");
       
        htmlBody.append(
            "  <button type=\"submit\" class=\"btn btn-default statsagg_page_content_font\">Submit</button>\n" +
            "</form>\n");
            
        if (performBenchmark && (metricKeysToBenchmark_Count != null) && (metricKeysToBenchmark_Count > 0)) {
            Set<String> metricKeysToBenchmark = getMetricKeysToBenchmark(metricKeysToBenchmark_Count);
            String benchmarkResultsHtml_MetricGroups = benchmarkMetricGroups(metricKeysToBenchmark);
            String benchmarkResultsHtml_Suspension = benchmarkSuspensions(metricKeysToBenchmark);
            htmlBody.append("<div class=\"statsagg_force_word_wrap\">");
            htmlBody.append(benchmarkResultsHtml_MetricGroups).append(benchmarkResultsHtml_Suspension);
            htmlBody.append("</div>");
        }
        
        htmlBody.append("</div>\n" + "</div>\n");
        
        return htmlBody.toString();
    }
    
    private static String benchmarkMetricGroups(Set<String> metricKeysToBenchmark) {
        
        if (metricKeysToBenchmark == null) return "";
        
        Map<Integer,String> mergedMatchRegexesByMetricGroupId_Local = new HashMap<>(GlobalVariables.mergedMatchRegexesByMetricGroupId);
        List<BenchmarkResult> benchmarkResults = new ArrayList<>();
        
        // run benchmark
        for (Integer metricGroupId : mergedMatchRegexesByMetricGroupId_Local.keySet()) {
            try {
                MetricGroup metricGroup = MetricGroupsDao.getMetricGroup(DatabaseConnections.getConnection(), true, metricGroupId);
                if ((metricGroup == null) || (metricGroup.getName() == null)) continue;

                long startTime = System.currentTimeMillis();
                String regex = mergedMatchRegexesByMetricGroupId_Local.get(metricGroupId);
                Set<String> regexMatches = MetricAssociation.getRegexMatches(metricKeysToBenchmark, regex, null, -1);
                long elapsedTime = System.currentTimeMillis() - startTime;
                BenchmarkResult benchmarkResult = new BenchmarkResult(metricGroup.getName(), elapsedTime);
                benchmarkResults.add(benchmarkResult);
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }

                
        // generate output html 
        benchmarkResults.sort(BenchmarkResult.COMPARE_BY_BENCHMARK_RESULT_DESC);
        StringBuilder outputHtmlString = new StringBuilder();
            if (!benchmarkResults.isEmpty()) {
            outputHtmlString.append("<br><hr><br>");
            outputHtmlString.append("<b>Metric Group Benchmark Results</b><br>").append("<ul>");
            for (BenchmarkResult benchmarkResult : benchmarkResults) {
                outputHtmlString.append("<li>");
                outputHtmlString.append("<a class=\"iframe cboxElement\" href=\"MetricGroupDetails?ExcludeNavbar=true&amp;Name=").
                        append(StatsAggHtmlFramework.urlEncode(benchmarkResult.getName())).
                        append("\">").append(StatsAggHtmlFramework.htmlEncode(benchmarkResult.getName())).
                        append("</a>").append(" = ").append(benchmarkResult.getBenchmarkResult()).append("ms");
                outputHtmlString.append("</li>");
            }
            outputHtmlString.append("</ul>");
        }
        
        return outputHtmlString.toString();
    }
    
    private static String benchmarkSuspensions(Set<String> metricKeysToBenchmark) {
        
        if (metricKeysToBenchmark == null) return "";
        
        Map<Integer,String> mergedMatchRegexesBySuspensionId_Local = new HashMap<>(GlobalVariables.mergedMatchRegexesBySuspensionId);
        List<BenchmarkResult> benchmarkResults = new ArrayList<>();
        
        // run benchmark
        for (Integer suspensionId : mergedMatchRegexesBySuspensionId_Local.keySet()) {
            try {
                Suspension suspension = SuspensionsDao.getSuspension(DatabaseConnections.getConnection(), true, suspensionId);
                if ((suspension == null) || (suspension.getName() == null)) continue;

                long startTime = System.currentTimeMillis();
                String regex = mergedMatchRegexesBySuspensionId_Local.get(suspensionId);
                Set<String> regexMatches = MetricAssociation.getRegexMatches(metricKeysToBenchmark, regex, null, -1);
                long elapsedTime = System.currentTimeMillis() - startTime;
                BenchmarkResult benchmarkResult = new BenchmarkResult(suspension.getName(), elapsedTime);
                benchmarkResults.add(benchmarkResult);
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }

        
        // generate output html 
        benchmarkResults.sort(BenchmarkResult.COMPARE_BY_BENCHMARK_RESULT_DESC);
        StringBuilder outputHtmlString = new StringBuilder();
            if (!benchmarkResults.isEmpty()) {
            outputHtmlString.append("<br><hr><br>");
            outputHtmlString.append("<b>Suspend Metrics Benchmark Results</b><br>").append("<ul>");
            for (BenchmarkResult benchmarkResult : benchmarkResults) {
                outputHtmlString.append("<li>");
                outputHtmlString.append("<a class=\"iframe cboxElement\" href=\"SuspensionDetails?ExcludeNavbar=true&amp;Name=").
                        append(StatsAggHtmlFramework.urlEncode(benchmarkResult.getName())).
                        append("\">").append(StatsAggHtmlFramework.htmlEncode(benchmarkResult.getName())).
                        append("</a>").append(" = ").append(benchmarkResult.getBenchmarkResult()).append("ms");
                outputHtmlString.append("</li>");
            }
            outputHtmlString.append("</ul>");
        }
        
        return outputHtmlString.toString();
    }
    
    private static Set<String> getMetricKeysToBenchmark(Integer metricKeysToBenchmark_Count) {
    
        Set<String> metricKeysToBenchmark = new HashSet<>();
        
        int metricCountFromExistingMetricKeys = 0;
        for (String metricKey : GlobalVariables.metricKeysLastSeenTimestamp.keySet()) {
            metricKeysToBenchmark.add(metricKey);
            metricCountFromExistingMetricKeys++;
            if (metricCountFromExistingMetricKeys >= metricKeysToBenchmark_Count) break;
        }

        for (int i = metricCountFromExistingMetricKeys; i < metricKeysToBenchmark_Count; i++) {
            String randomString = RandomStringUtils.randomAlphanumeric(50, 200);
            metricKeysToBenchmark.add(randomString);
        }
        
        return metricKeysToBenchmark;
    }
    
    private static class BenchmarkResult {

        private final String name__;
        private final long benchmarkResult__;
        
        public BenchmarkResult(String name, long benchmarkResult) {
            this.name__ = name;
            this.benchmarkResult__ = benchmarkResult;
        }

        public final static Comparator<BenchmarkResult> COMPARE_BY_BENCHMARK_RESULT_DESC = new Comparator<BenchmarkResult>() {

            @Override
            public int compare(BenchmarkResult benchmarkResult1, BenchmarkResult benchmarkResult2) {
                if (benchmarkResult1.getBenchmarkResult() < benchmarkResult2.getBenchmarkResult()) return 1;
                else if (benchmarkResult1.getBenchmarkResult() > benchmarkResult2.getBenchmarkResult()) return -1;
                else return 0;
            }

        };

        @Override
        public int hashCode() {
            return new HashCodeBuilder(71, 109).append(name__).append(benchmarkResult__).toHashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj == this) return true;
            if (obj.getClass() != getClass()) return false;

            BenchmarkResult benchmarkResult = (BenchmarkResult) obj;

            return new EqualsBuilder().append(name__, benchmarkResult.getName()).append(benchmarkResult__, benchmarkResult.getBenchmarkResult()).isEquals();
        }
        
        public String getName() {
            return name__;
        }

        public long getBenchmarkResult() {
            return benchmarkResult__;
        }
    }
    
}
