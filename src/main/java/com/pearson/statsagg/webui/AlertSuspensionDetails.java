package com.pearson.statsagg.webui;

import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import com.pearson.statsagg.database_objects.alert_suspensions.AlertSuspension;
import com.pearson.statsagg.database_objects.alert_suspensions.AlertSuspensionsDao;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.utilities.DateAndTime;
import com.pearson.statsagg.utilities.StackTrace;
import java.math.BigDecimal;
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
        String alertSuspensionDetails = getSuspensionDetailsString(alertSuspensionName);
                
        try {  
            StringBuilder htmlBuilder = new StringBuilder();

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            
            String htmlBody = statsAggHtmlFramework.createHtmlBody(
            "<div id=\"page-content-wrapper\">\n" +
            "  <!-- Keep all page content within the page-content inset div! -->\n" +
            "  <div class=\"page-content inset statsagg_page_content_font\">\n" +
            "    <div class=\"content-header\"> \n" +
            "      <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "    </div>\n " +
            "    <div class=\"row create-alert-form-row\">\n" +
            alertSuspensionDetails +
            "  </div></div>\n" +
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

    private String getSuspensionDetailsString(String suspensionName) {
        
        if (suspensionName == null) {
            return "<div class=\"col-md-4\"><b>No suspension specified</b></div>";
        }
        
        AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao();
        AlertSuspension suspension = alertSuspensionsDao.getSuspensionByName(suspensionName);
        
        if (suspension == null) {
            return "<div class=\"col-md-4\"><b>Suspension not found</b></div>";
        }
        else {
            StringBuilder outputString = new StringBuilder();
            
            outputString.append("<div class=\"col-md-4 statsagg_three_panel_first_panel\">\n");
            outputString.append("<div class=\"panel panel-info\"> <div class=\"panel-heading\"><b>Core Details</b></div> <div class=\"panel-body statsagg_force_word_wrap\">");
            
            outputString.append("<b>Name</b> = ").append(StatsAggHtmlFramework.htmlEncode(suspension.getName())).append("<br>");
            
            outputString.append("<b>ID</b> = ").append(suspension.getId()).append("<br>");
            
            outputString.append("<b>Description</b> = ");
            if (suspension.getDescription() != null) {
                String encodedAlertDescription = StatsAggHtmlFramework.htmlEncode(suspension.getDescription());
                outputString.append(encodedAlertDescription.replaceAll("\n", "<br>")).append("<br><br>");
            }
            else outputString.append("<br><br>");
            
            outputString.append("<b>Is Enabled?</b> = ");
            String isEnabled = "No";
            if ((suspension.isEnabled() != null) && suspension.isEnabled()) isEnabled = "Yes";
            if (suspension.isEnabled() != null) outputString.append(isEnabled).append("<br>");
            else outputString.append("N/A <br>");
                
            outputString.append("<b>Suspend notification only?</b> = ");
            String isSuspendNotificationOnly = "No";
            if ((suspension.isSuspendNotificationOnly() != null) && suspension.isSuspendNotificationOnly()) isSuspendNotificationOnly = "Yes";
            if (suspension.isSuspendNotificationOnly() != null) outputString.append(isSuspendNotificationOnly).append("<br>");
            else outputString.append("N/A <br>");
            
            outputString.append("<br>");
            
            String isValid = "No";
            if (AlertSuspension.isValid(suspension)) isValid = "Yes";
            outputString.append("<b>Is suspension configuration valid?</b> = ").append(isValid).append("<br>");
            
            String isAlertSuspensionInSuspensionTimeWindow = "No";
            if (AlertSuspension.isSuspensionInSuspensionTimeWindow(suspension)) isAlertSuspensionInSuspensionTimeWindow = "Yes";
            outputString.append("<b>Is currently in the suspension window?</b> = ").append(isAlertSuspensionInSuspensionTimeWindow).append("<br>");
            
            String isAlertSuspensionActive = "No";
            if (AlertSuspension.isSuspensionActive(suspension)) isAlertSuspensionActive = "Yes";
            outputString.append("<b>Is suspension active?</b> = ").append(isAlertSuspensionActive).append("<br>");
            
            outputString.append("<br>");
            outputString.append("<b>Alert Associations</b> = ");            
            String alertSuspensionAlertAssociationsLink = "<a href=\"AlertSuspensionAlertAssociations?Name=" + StatsAggHtmlFramework.urlEncode(suspension.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(suspension.getName()) + "</a>";
            outputString.append(alertSuspensionAlertAssociationsLink);  
            
            outputString.append("</div></div></div>").append("<div class=\"col-md-4 statsagg_three_panel_second_panel\">\n");
            outputString.append("<div class=\"panel panel-info\"> <div class=\"panel-heading\"><b>Suspension Type</b></div> <div class=\"panel-body statsagg_force_word_wrap\">");
            
            outputString.append("<b>Suspend by...</b> = ");
            if ((suspension.getSuspendBy() != null) && (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_ALERT_ID)) outputString.append("Alert name").append("<br>");
            else if ((suspension.getSuspendBy() != null) && (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS)) outputString.append("Metric group tags").append("<br>");
            else if ((suspension.getSuspendBy() != null) && (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_EVERYTHING)) outputString.append("Everything").append("<br>");
            else if ((suspension.getSuspendBy() != null) && (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_METRICS)) outputString.append("Metrics").append("<br>");
            else outputString.append("N/A <br>");
            
            outputString.append("<br>");

            if ((suspension.getSuspendBy() != null) && (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_ALERT_ID)) {
                outputString.append("<b>Alert name</b> = ");
                if (suspension.getAlertId() != null) {
                    AlertsDao alertsDao = new AlertsDao();
                    Alert alert = alertsDao.getAlert(suspension.getAlertId());

                    if ((alert != null) && (alert.getName() != null)) {
                        String alertDetails = "<a href=\"AlertDetails?Name=" + StatsAggHtmlFramework.urlEncode(alert.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(alert.getName()) + "</a>";
                        outputString.append(alertDetails).append("<br>");
                    }
                    else outputString.append("N/A <br>");
                }
                else outputString.append("N/A <br>");
            }

            if ((suspension.getSuspendBy() != null) && (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS)) {
                outputString.append("<b>Metric group tags to include</b> = ");
                if ((suspension.getMetricGroupTagsInclusive() != null) && !suspension.getMetricGroupTagsInclusive().isEmpty()) {
                    String metricGroupTagsExclusiveFormatted = StringUtils.replace(StatsAggHtmlFramework.htmlEncode(suspension.getMetricGroupTagsInclusive()), "\n", "<br>&nbsp;&nbsp;&nbsp;");
                    outputString.append("<br>&nbsp;&nbsp;&nbsp;").append(metricGroupTagsExclusiveFormatted).append("<br>");
                }
                else outputString.append("N/A <br>");
            }
            
            if ((suspension.getSuspendBy() != null) && (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_EVERYTHING)) {
                outputString.append("<b>Metric group tags to exclude</b> = ");
                if ((suspension.getMetricGroupTagsExclusive() != null) && !suspension.getMetricGroupTagsExclusive().isEmpty()) {
                    String metricGroupTagsInclusiveFormatted = StringUtils.replace(StatsAggHtmlFramework.htmlEncode(suspension.getMetricGroupTagsExclusive()), "\n", "<br>&nbsp;&nbsp;&nbsp;");
                    outputString.append("<br>&nbsp;&nbsp;&nbsp;").append(metricGroupTagsInclusiveFormatted).append("<br>");
                }
                else outputString.append("N/A <br>");     
            }
            
            if ((suspension.getSuspendBy() != null) && (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_METRICS)) {
                outputString.append("<b>Metric suspension regexes</b> = ");
                if ((suspension.getMetricSuspensionRegexes() != null) && !suspension.getMetricSuspensionRegexes().isEmpty()) {
                    String metricSuspensionRegexesFormatted = StringUtils.replace(StatsAggHtmlFramework.htmlEncode(suspension.getMetricSuspensionRegexes()), "\n", "<br>&nbsp;&nbsp;&nbsp;");
                    outputString.append("<br>&nbsp;&nbsp;&nbsp;").append(metricSuspensionRegexesFormatted).append("<br>");
                }
                else outputString.append("N/A <br>");     
            }
            
            outputString.append("</div></div></div>").append("<div class=\"col-md-4 statsagg_three_panel_third_panel\">\n");
            outputString.append("<div class=\"panel panel-info\"> <div class=\"panel-heading\"><b>Suspension Schedule</b></div> <div class=\"panel-body statsagg_force_word_wrap\">");
            
            outputString.append("<b>Suspension Type</b> = ");
            if ((suspension.isOneTime() != null) && suspension.isOneTime()) outputString.append("One time").append("<br>");
            else if ((suspension.isOneTime() != null) && !suspension.isOneTime()) outputString.append("Recurring").append("<br>");
            else outputString.append("N/A <br>");
            
            outputString.append("<br>");

            outputString.append("<b>Start date</b> = ");
            if (suspension.getStartDate() != null) {
                String startDateString = DateAndTime.getFormattedDateAndTime(suspension.getStartDate(), "yyyy-MM-dd");
                outputString.append(startDateString).append("<br>");
            }
            else outputString.append("N/A <br>");
            
            outputString.append("<b>Start time</b> = ");
            if (suspension.getStartTime() != null) {
                String startTimeString = DateAndTime.getFormattedDateAndTime(suspension.getStartTime(), "h:mm a");
                outputString.append(startTimeString).append("<br>");
            }
            else outputString.append("N/A <br>");
            
            outputString.append("<b>Duration</b> = ");
            if (suspension.getDuration() != null) {
                BigDecimal duration = DatabaseObjectCommon.getValueForTimeFromMilliseconds(suspension.getDuration(), suspension.getDurationTimeUnit());
                if (duration != null) outputString.append(duration.stripTrailingZeros().toPlainString());
                
                if (suspension.getDurationTimeUnit() != null) {
                    String timeUnitString = DatabaseObjectCommon.getTimeUnitStringFromCode(suspension.getDurationTimeUnit(), true);
                    if (timeUnitString != null) outputString.append(" ").append(timeUnitString);
                }
                outputString.append("<br>");
            }
            else outputString.append("N/A <br>");
            
            if ((suspension.isOneTime() != null) && suspension.isOneTime()) {
                outputString.append("<b>Suspension ends at</b> = ");
                if (suspension.getDeleteAtTimestamp() != null) {
                    String deleteAtTimestampString = DateAndTime.getFormattedDateAndTime(suspension.getDeleteAtTimestamp(), "yyyy-MM-dd, h:mm:ss a");
                    outputString.append(deleteAtTimestampString).append("<br>");
                }
                else outputString.append("N/A <br>");
            }
            
            if ((suspension.isOneTime() != null) && !suspension.isOneTime()) {
                outputString.append("<br>");
                
                outputString.append("<b>Recur Sunday?</b> = ");
                String isRecurSunday = "No";
                if ((suspension.isRecurSunday() != null) && suspension.isRecurSunday()) isRecurSunday = "Yes";
                if (suspension.isRecurSunday() != null) outputString.append(isRecurSunday).append("<br>");
                else outputString.append("N/A <br>");

                outputString.append("<b>Recur Monday?</b> = ");
                String isRecurMonday = "No";
                if ((suspension.isRecurMonday() != null) && suspension.isRecurMonday()) isRecurMonday = "Yes";
                if (suspension.isRecurMonday() != null) outputString.append(isRecurMonday).append("<br>");
                else outputString.append("N/A <br>");

                outputString.append("<b>Recur Tuesday?</b> = ");
                String isRecurTuesday = "No";
                if ((suspension.isRecurTuesday() != null) && suspension.isRecurTuesday()) isRecurTuesday = "Yes";
                if (suspension.isRecurTuesday() != null) outputString.append(isRecurTuesday).append("<br>");
                else outputString.append("N/A <br>");

                outputString.append("<b>Recur Wednesday?</b> = ");
                String isRecurWednesday = "No";
                if ((suspension.isRecurWednesday() != null) && suspension.isRecurWednesday()) isRecurWednesday = "Yes";
                if (suspension.isRecurWednesday() != null) outputString.append(isRecurWednesday).append("<br>");
                else outputString.append("N/A <br>");

                outputString.append("<b>Recur Thursday?</b> = ");
                String isRecurThursday = "No";
                if ((suspension.isRecurThursday() != null) && suspension.isRecurThursday()) isRecurThursday = "Yes";
                if (suspension.isRecurThursday() != null) outputString.append(isRecurThursday).append("<br>");
                else outputString.append("N/A <br>");

                outputString.append("<b>Recur Friday?</b> = ");
                String isRecurFriday = "No";
                if ((suspension.isRecurFriday() != null) && suspension.isRecurFriday()) isRecurFriday = "Yes";
                if (suspension.isRecurFriday() != null) outputString.append(isRecurFriday).append("<br>");
                else outputString.append("N/A <br>");

                outputString.append("<b>Recur Saturday?</b> = ");
                String isRecurSaturday = "No";
                if ((suspension.isRecurSaturday() != null) && suspension.isRecurSaturday()) isRecurSaturday = "Yes";
                if (suspension.isRecurSaturday() != null) outputString.append(isRecurSaturday).append("<br>");
                else outputString.append("N/A <br>");
            }
            
            outputString.append("</div></div></div>");

            return outputString.toString();
        }
    }

}
