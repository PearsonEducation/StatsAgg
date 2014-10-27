package com.pearson.statsagg.network.http;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database.alerts.Alert;
import com.pearson.statsagg.database.alerts.AlertsDao;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.StackTrace;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "Home", urlPatterns = {"/Home"})
public class Home extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(Home.class.getName());
    
    public static final String PAGE_NAME = "Home";
    
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
    
        try {  
            StringBuilder htmlBuilder = new StringBuilder("");

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            String htmlBodyContent = createHomeHtmlBody();
            
            String htmlBody = statsAggHtmlFramework.createHtmlBody(
            "<div id=\"page-content-wrapper\">\n" +
            "<!-- Keep all page content within the page-content inset div! -->\n" +
            "  <div class=\"page-content inset\">\n" +
            "    <div class=\"content-header\"> \n" +
            "      <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "    </div>\n" +
            htmlBodyContent +     
            "  </div>\n" +
            "</div>\n");
            
            htmlBuilder.append("<!DOCTYPE html>\n<html>\n").append(htmlHeader).append(htmlBody).append("</html>");
            
            Document htmlDocument = Jsoup.parse(htmlBuilder.toString());
            String htmlFormatted  = htmlDocument.toString();
            out = response.getWriter();
            if (ApplicationConfiguration.isDebugModeEnabled()) out.println(htmlBuilder.toString());
            else out.println(htmlFormatted);
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
    
    public String createHomeHtmlBody() {

        SimpleDateFormat dateAndTimeFormat = new SimpleDateFormat("yyyy-MM-dd  h:mm:ss a  z");
        
        String statsaggStartTimestamp = "N/A";
        if (GlobalVariables.statsaggStartTimestamp.get() != 0) {
            statsaggStartTimestamp = dateAndTimeFormat.format(GlobalVariables.statsaggStartTimestamp.get());
        }
        
        AlertsDao alertDao = new AlertsDao(false);
        List<Alert> alerts = alertDao.getAllDatabaseObjectsInTable();
        boolean isConnectionValid = alertDao.getDatabaseInterface().isConnectionValid();
        alertDao.close();
        
        String errorBox = "";
        if (!isConnectionValid) {
            errorBox = "" +
                "<div class=\"col-ld-12\">\n" +
                "  <div class=\"panel panel-danger\">\n" +
                "    <div class=\"panel-heading\">\n" +
                "      Error\n" +
                "    </div>\n" +
                "    <div class=\"panel-body\">\n" +
                "      <p>StatsAgg did not successfully connect to the database</p>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</div>\n";
        }

        int numCautionAlertsActive = 0;
        int numDangerAlertsActive = 0;
        
        for (Alert alert : alerts) {
            if (alert.isCautionAlertActive()) numCautionAlertsActive++;
            if (alert.isDangerAlertActive()) numDangerAlertsActive++;
        }
        
        String alertRoutineLastExecuted = "N/A";
        long alertRountineLastExecutedTimestamp = GlobalVariables.alertRountineLastExecutedTimestamp.get();
        if (alertRountineLastExecutedTimestamp != 0) {
            alertRoutineLastExecuted = dateAndTimeFormat.format(alertRountineLastExecutedTimestamp);
        }
        
        long numAssociatedMetricsWithValues = GlobalVariables.associatedMetricsWithValuesCount.longValue();
        long numMetricKeysTrackedLast24Hrs = GlobalVariables.metricKeysLastSeenTimestamp.size();
        long avgIncomingMetricsProcessedPerSecond = GlobalVariables.incomingMetricsRollingAverage.longValue();
        
        Calendar homeLastRefreshedTimestamp = Calendar.getInstance();
  
        String html = 
            "<div class=\"col-ld-12\" style=\"padding-left: 0; padding-right: 0;\">\n" +
            errorBox + 
            "    <div class=\"panel panel-primary\">\n" +
            "        <div class=\"panel-heading\">\n" +
            "            Status\n" +
            "        </div>\n" +
            "        <div class=\"panel-body\">\n" +
            "            <p><b>StatsAgg startup time:</b> " + statsaggStartTimestamp + "</p>\n" +
            "            <p><b>Active caution alerts:</b> " + numCautionAlertsActive + "</p>\n" +
            "            <p><b>Active danger alerts:</b> " + numDangerAlertsActive + "</p>\n" +
            "            <p><b>Last alert routine execution time:</b> " + alertRoutineLastExecuted + "</p>\n" +
            "            <p><b>Recent/unique metric keys associated with metric groups:</b> " + numAssociatedMetricsWithValues + "</p>\n" +
            "            <p><b>Unique metric keys tracked (last 24hrs):</b> " + numMetricKeysTrackedLast24Hrs + "</p>\n" +
            "            <p><b>Average incoming metrics processed per second (last 15sec):</b> " + avgIncomingMetricsProcessedPerSecond + "</p>\n" +
            "        </div>\n" +
            "        <div class=\"panel-footer\">\n" +
            "            Last refreshed: " + dateAndTimeFormat.format(homeLastRefreshedTimestamp.getTime()) + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + 
            "            Version: " + com.pearson.statsagg.controller.Version.getProjectVersion() + "-" + com.pearson.statsagg.controller.Version.getBuildTimestamp() + "\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</div>";
        
        return html;
    }

}
