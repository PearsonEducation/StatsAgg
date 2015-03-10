package com.pearson.statsagg.webui;

import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database.alert_suspensions.AlertSuspension;
import com.pearson.statsagg.database.alerts.Alert;
import com.pearson.statsagg.database.alerts.AlertsDao;
import com.pearson.statsagg.utilities.StackTrace;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "AlertSuspensionAlertAssociationsPreview", urlPatterns = {"/AlertSuspensionAlertAssociationsPreview"})
public class AlertSuspensionAlertAssociationsPreview extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AlertSuspensionAlertAssociationsPreview.class.getName());
    
    public static final String PAGE_NAME = "Alert Associations";
    
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
    
        AlertSuspension alertSuspension = getAlertSuspensionFromParameters(request);
        String alertAssociationsBody = getAlertSuspension_AlertAssociations(alertSuspension);
        
        try {  
            StringBuilder htmlBuilder = new StringBuilder("");

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            
            String htmlBody =
            "<body>" +
            "  <div id=\"page-content-wrapper\">\n" +
            "    <!-- Keep all page content within the page-content inset div! -->\n" +
            "    <div class=\"page-content inset\">\n" +
            "      <div class=\"statsagg_force_word_wrap\">" +
            alertAssociationsBody +
            "      </div>\n" +
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

    private AlertSuspension getAlertSuspensionFromParameters(HttpServletRequest request) {
        
        if (request == null) {
            return null;
        }
        
        String alertSuspensionName = "AlertSuspension Preview";
        
        AlertSuspension alertSuspension = new AlertSuspension(
                -1, alertSuspensionName, alertSuspensionName.toUpperCase(), true, null, 1, null, null, false, true, 
                true, true, true, true, true, true, true, 
                new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), 1, null);
        
        try {
            String parameter;
            
            parameter = request.getParameter("CreateAlertSuspension_SuspendBy");
            if ((parameter != null) && parameter.contains("AlertName")) alertSuspension.setSuspendBy(AlertSuspension.SUSPEND_BY_ALERT_ID);
            else if ((parameter != null) && parameter.contains("Tags")) alertSuspension.setSuspendBy(AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS);
            else if ((parameter != null) && parameter.contains("Everything")) alertSuspension.setSuspendBy(AlertSuspension.SUSPEND_BY_EVERYTHING);

            parameter = request.getParameter("AlertName");
            AlertsDao alertsDao = new AlertsDao();
            Alert alert = alertsDao.getAlertByName(parameter);
            if (alert != null) alertSuspension.setAlertId(alert.getId());

            parameter = request.getParameter("MetricGroupTagsInclusive");
            if (parameter != null) {
                String trimmedTags = AlertSuspension.trimNewLineDelimitedTags(parameter);
                alertSuspension.setMetricGroupTagsInclusive(trimmedTags);
            }
            
            parameter = request.getParameter("MetricGroupTagsExclusive");
            if (parameter != null) {
                String trimmedTags = AlertSuspension.trimNewLineDelimitedTags(parameter);
                alertSuspension.setMetricGroupTagsExclusive(trimmedTags);
            }
        }
        catch (Exception e) {
            alertSuspension = null;
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
            
        if (AlertSuspension.isValid_CheckSuspendBy(alertSuspension)) return alertSuspension;
        else return null;
    }
   
    protected String getAlertSuspension_AlertAssociations(AlertSuspension alertSuspension) {
        
        if (alertSuspension == null) {
            return "";
        }
        
        List<String> alertNames = new ArrayList<>();
        
        AlertsDao alertsDao = new AlertsDao(false);
        List<Alert> alerts = alertsDao.getAllDatabaseObjectsInTable();
        for (Alert alert : alerts) {
            if ((alert.getName() == null) || alert.getName().isEmpty()) continue;
            
            boolean outputAlert = false;
            
            if (alertSuspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_ALERT_ID) {
                outputAlert = com.pearson.statsagg.alerts.AlertSuspensions.isSuspensionCriteriaMet_SuspendByAlertName(alert, alertSuspension);
            }
            else if (alertSuspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS) {
                outputAlert = com.pearson.statsagg.alerts.AlertSuspensions.isSuspensionCriteriaMet_SuspendedByMetricGroupTags(alert, alertSuspension);
            }
            else if (alertSuspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_EVERYTHING) {
                outputAlert = com.pearson.statsagg.alerts.AlertSuspensions.isSuspensionCriteriaMet_SuspendedByEverything(alert, alertSuspension);
            }
            
            if (outputAlert) {
                alertNames.add(alert.getName());
            }
        }
        alertsDao.close();

        Collections.sort(alertNames);
        
        StringBuilder outputString = new StringBuilder("");
        outputString.append("<b>Total Alert Associations</b> = ").append(alertNames.size()).append("<br><br>");
        
        if (alertNames.size() > 0) {
            outputString.append("<b>Alert Associations...</b>").append("<br>");

            outputString.append("<ul>");
            for (String alertName : alertNames) {
                String alertSuspensionDetailsUrl = "<a href=\"AlertDetails?Name=" + StatsAggHtmlFramework.urlEncode(alertName) + "\">" + StatsAggHtmlFramework.htmlEncode(alertName) + "</a>";
                outputString.append("<li>").append(alertSuspensionDetailsUrl).append("</li>");
            }
        }
        
        outputString.append("</ul>");

        return outputString.toString();
    }

}
