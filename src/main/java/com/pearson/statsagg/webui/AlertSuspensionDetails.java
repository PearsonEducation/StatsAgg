package com.pearson.statsagg.webui;

import com.pearson.statsagg.database.alert_suspensions.AlertSuspension;
import com.pearson.statsagg.database.alert_suspensions.AlertSuspensionsDao;
import com.pearson.statsagg.database.alerts.Alert;
import com.pearson.statsagg.database.alerts.AlertsDao;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.utilities.DateAndTime;
import com.pearson.statsagg.utilities.StackTrace;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "AlertSuspensionDetails", urlPatterns = {"/AlertSuspensionDetails"})
public class AlertSuspensionDetails extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AlertSuspensionDetails.class.getName());
    
    public static final String PAGE_NAME = "Alert Suspension Details";
    
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
    
        String alertSuspensionName = request.getParameter("Name");
        String alertSuspensionDetails = getAlertSuspensionDetailsString(alertSuspensionName);
                
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
            "    </div>\n " +
            alertSuspensionDetails +
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

    private String getAlertSuspensionDetailsString(String alertSuspensionName) {
        
        if (alertSuspensionName == null) {
            return "";
        }
        
        StringBuilder outputString = new StringBuilder("");
        
        AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao();
        AlertSuspension alertSuspension = alertSuspensionsDao.getAlertSuspensionByName(alertSuspensionName);
        
        if (alertSuspension != null) {
            outputString.append("<b>Name</b> = ").append(StatsAggHtmlFramework.htmlEncode(alertSuspension.getName())).append("<br>");
            
            outputString.append("<b>Is Enabled?</b> = ");
            if (alertSuspension.isEnabled() != null) outputString.append(alertSuspension.isEnabled()).append("<br>");
            else outputString.append("N/A <br>");
                
            outputString.append("<b>Suspend notification only?</b> = ");
            if (alertSuspension.isSuspendNotificationOnly() != null) outputString.append(alertSuspension.isSuspendNotificationOnly()).append("<br>");
            else outputString.append("N/A <br>");
            
            outputString.append("<br>");
            
            outputString.append("<b>Suspend by...</b> = ");
            if ((alertSuspension.getSuspendBy() != null) && (alertSuspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_ALERT_ID)) outputString.append("Alert name").append("<br>");
            else if ((alertSuspension.getSuspendBy() != null) && (alertSuspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS)) outputString.append("Metric group tags").append("<br>");
            else if ((alertSuspension.getSuspendBy() != null) && (alertSuspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_EVERYTHING)) outputString.append("Everything").append("<br>");
            else outputString.append("N/A <br>");
            
            if ((alertSuspension.getSuspendBy() != null) && (alertSuspension.getSuspendBy() != AlertSuspension.SUSPEND_BY_ALERT_ID)) outputString.append("<del>");
            outputString.append("<b>Suspend by: Alert name. Alert name</b> = ");
            if (alertSuspension.getAlertId() != null) {
                AlertsDao alertsDao = new AlertsDao();
                Alert alert = alertsDao.getAlert(alertSuspension.getAlertId());
               
                if ((alert != null) && (alert.getName() != null)) outputString.append(StatsAggHtmlFramework.htmlEncode(alert.getName())).append("<br>");
                else outputString.append("N/A <br>");
            }
            else outputString.append("N/A <br>");
            if ((alertSuspension.getSuspendBy() != null) && (alertSuspension.getSuspendBy() != AlertSuspension.SUSPEND_BY_ALERT_ID)) outputString.append("</del>");

            if ((alertSuspension.getSuspendBy() != null) && (alertSuspension.getSuspendBy() != AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS)) outputString.append("<del>");
            outputString.append("<b>Suspend by: Metric group tags. Metric group tags to include</b> = ");
            if ((alertSuspension.getMetricGroupTagsInclusive() != null) && !alertSuspension.getMetricGroupTagsInclusive().isEmpty()) {
                String metricGroupTagsExclusiveFormatted = StringUtils.replace(StatsAggHtmlFramework.htmlEncode(alertSuspension.getMetricGroupTagsInclusive()), "\n", "<br>&nbsp;&nbsp;&nbsp;");
                outputString.append("<br>&nbsp;&nbsp;&nbsp;").append(metricGroupTagsExclusiveFormatted).append("<br>");
            }
            else outputString.append("N/A <br>");
            if ((alertSuspension.getSuspendBy() != null) && (alertSuspension.getSuspendBy() != AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS)) outputString.append("</del>");
            
            if ((alertSuspension.getSuspendBy() != null) && (alertSuspension.getSuspendBy() != AlertSuspension.SUSPEND_BY_EVERYTHING)) outputString.append("<del>");
            outputString.append("<b>Suspend by: Everything. Metric group tags to exclude</b> = ");
            if ((alertSuspension.getMetricGroupTagsExclusive() != null) && !alertSuspension.getMetricGroupTagsExclusive().isEmpty()) {
                String metricGroupTagsInclusiveFormatted = StringUtils.replace(StatsAggHtmlFramework.htmlEncode(alertSuspension.getMetricGroupTagsExclusive()), "\n", "<br>&nbsp;&nbsp;&nbsp;");
                outputString.append("<br>&nbsp;&nbsp;&nbsp;").append(metricGroupTagsInclusiveFormatted).append("<br>");
            }
            else outputString.append("N/A <br>");     
            if ((alertSuspension.getSuspendBy() != null) && (alertSuspension.getSuspendBy() != AlertSuspension.SUSPEND_BY_EVERYTHING)) outputString.append("</del>");
            
            outputString.append("<br>");

            outputString.append("<b>Suspension Type</b> = ");
            if ((alertSuspension.isOneTime() != null) && alertSuspension.isOneTime()) outputString.append("One time").append("<br>");
            else if ((alertSuspension.isOneTime() != null) && !alertSuspension.isOneTime()) outputString.append("Recurring").append("<br>");
            else outputString.append("N/A <br>");
            
            outputString.append("<b>Start date</b> = ");
            if (alertSuspension.getStartDate() != null) {
                String startDateString = DateAndTime.getFormattedDateAndTime(alertSuspension.getStartDate(), "yyyy-MM-dd");
                outputString.append(startDateString).append("<br>");
            }
            else outputString.append("N/A <br>");
            
            outputString.append("<b>Start time</b> = ");
            if (alertSuspension.getStartTime() != null) {
                String startTimeString = DateAndTime.getFormattedDateAndTime(alertSuspension.getStartTime(), "h:mm a");
                outputString.append(startTimeString).append("<br>");
            }
            else outputString.append("N/A <br>");
            
            if ((alertSuspension.isOneTime() != null) && !alertSuspension.isOneTime()) outputString.append("<del>");
            outputString.append("<b>Delete alert suspension at</b> = ");
            if (alertSuspension.getDeleteAtTimestamp() != null) {
                String deleteAtTimestampString = DateAndTime.getFormattedDateAndTime(alertSuspension.getDeleteAtTimestamp(), "yyyy-MM-dd, h:mm:ss a");
                outputString.append(deleteAtTimestampString).append("<br>");
            }
            else outputString.append("N/A <br>");
            if ((alertSuspension.isOneTime() != null) && !alertSuspension.isOneTime()) outputString.append("</del>");
            
            outputString.append("<b>Duration</b> = ");
            if (alertSuspension.getDuration() != null) outputString.append(alertSuspension.getDuration()).append("<br>");
            else outputString.append("N/A <br>");
            
            if ((alertSuspension.isOneTime() != null) && alertSuspension.isOneTime()) outputString.append("<del>");
            
            outputString.append("<b>Recur Sunday?</b> = ");
            if (alertSuspension.isRecurSunday() != null) outputString.append(alertSuspension.isRecurSunday()).append("<br>");
            else outputString.append("N/A <br>");
            
            outputString.append("<b>Recur Monday?</b> = ");
            if (alertSuspension.isRecurMonday() != null) outputString.append(alertSuspension.isRecurMonday()).append("<br>");
            else outputString.append("N/A <br>");
            
            outputString.append("<b>Recur Tuesday?</b> = ");
            if (alertSuspension.isRecurTuesday() != null) outputString.append(alertSuspension.isRecurTuesday()).append("<br>");
            else outputString.append("N/A <br>");
     
            outputString.append("<b>Recur Wednesday?</b> = ");
            if (alertSuspension.isRecurWednesday() != null) outputString.append(alertSuspension.isRecurWednesday()).append("<br>");
            else outputString.append("N/A <br>");
     
            outputString.append("<b>Recur Thursday?</b> = ");
            if (alertSuspension.isRecurThursday() != null) outputString.append(alertSuspension.isRecurThursday()).append("<br>");
            else outputString.append("N/A <br>");
            
            outputString.append("<b>Recur Friday?</b> = ");
            if (alertSuspension.isRecurFriday() != null) outputString.append(alertSuspension.isRecurFriday()).append("<br>");
            else outputString.append("N/A <br>");
            
            outputString.append("<b>Recur Saturday?</b> = ");
            if (alertSuspension.isRecurSaturday() != null) outputString.append(alertSuspension.isRecurSaturday()).append("<br>");
            else outputString.append("N/A <br>");
            
            if ((alertSuspension.isOneTime() != null) && alertSuspension.isOneTime()) outputString.append("</del>");
            
            outputString.append("<br>");

            outputString.append("<b>Is alert suspension configuration valid?</b> = ").append(AlertSuspension.isValid(alertSuspension)).append("<br>");
            outputString.append("<b>Is currently in the suspension window?</b> = ").append(AlertSuspension.isAlertSuspensionInSuspensionTimeWindow(alertSuspension)).append("<br>");
            outputString.append("<b>Is suspension active?</b> = ").append(AlertSuspension.isAlertSuspensionActive(alertSuspension)).append("<br>");
        }
        
        return outputString.toString();
    }

}
