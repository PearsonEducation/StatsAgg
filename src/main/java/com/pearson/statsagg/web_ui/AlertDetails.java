package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import com.pearson.statsagg.database_objects.alert_templates.AlertTemplate;
import com.pearson.statsagg.database_objects.alert_templates.AlertTemplatesDao;
import java.io.PrintWriter;
import java.math.BigDecimal;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroup;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupsDao;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroup;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroupsDao;
import com.pearson.statsagg.database_objects.variable_set.VariableSet;
import com.pearson.statsagg.database_objects.variable_set.VariableSetsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.time_utils.DateAndTime;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import java.sql.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class AlertDetails extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AlertDetails.class.getName());
    
    public static final String PAGE_NAME = "Alert Details";
    
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
        
        try {  
            request.setCharacterEncoding("UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html");
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }

        PrintWriter out = null;
    
        String alertName = request.getParameter("Name");
        boolean excludeNavbar = StringUtilities.isStringValueBooleanTrue(request.getParameter("ExcludeNavbar"));
        String alertDetails = getAlertDetailsString(alertName);
                
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
            alertDetails +
            "  </div></div>\n" +
            "</div>\n",
            excludeNavbar);
            
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

    protected static String getAlertDetailsString(String alertName) {
        
        if (alertName == null) return "<div class=\"col-md-4\"><b>No alert specified</b></div>";   
        
        Alert alert = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, alertName);
        
        if (alert == null) {
            return "<div class=\"col-md-4\"><b>Alert not found</b></div>";
        }

        StringBuilder outputString = new StringBuilder();

        Connection connection = DatabaseConnections.getConnection();
        AlertTemplate alertTemplate = null;
        VariableSet variableSet = null;
        MetricGroup metricGroup = null;
        NotificationGroup cautionNotificationGroup = null, cautionPositiveNotificationGroup = null, dangerNotificationGroup = null, dangerPositiveNotificationGroup = null;
        if (alert.getMetricGroupId() != null) metricGroup = MetricGroupsDao.getMetricGroup(connection, false, alert.getMetricGroupId());
        if (alert.getCautionNotificationGroupId() != null) cautionNotificationGroup = NotificationGroupsDao.getNotificationGroup(connection, false, alert.getCautionNotificationGroupId());
        if (alert.getCautionPositiveNotificationGroupId() != null) cautionPositiveNotificationGroup = NotificationGroupsDao.getNotificationGroup(connection, false, alert.getCautionPositiveNotificationGroupId());
        if (alert.getDangerNotificationGroupId() != null) dangerNotificationGroup = NotificationGroupsDao.getNotificationGroup(connection, false, alert.getDangerNotificationGroupId());
        if (alert.getDangerPositiveNotificationGroupId() != null) dangerPositiveNotificationGroup = NotificationGroupsDao.getNotificationGroup(connection, false, alert.getDangerPositiveNotificationGroupId());
        if (alert.getAlertTemplateId() != null) alertTemplate = AlertTemplatesDao.getAlertTemplate(connection, false, alert.getAlertTemplateId());
        if (alert.getVariableSetId() != null) variableSet = VariableSetsDao.getVariableSet(connection, false, alert.getVariableSetId());
        DatabaseUtils.cleanup(connection);

        outputString.append("<div class=\"col-md-4 statsagg_three_panel_first_panel\">\n");
        outputString.append("<div class=\"panel panel-default\"> <div class=\"panel-heading\"><b>Core Details</b></div> <div class=\"panel-body statsagg_force_word_wrap\">");

        outputString.append("<b>Name</b> = ").append(StatsAggHtmlFramework.htmlEncode(alert.getName())).append("<br>");

        outputString.append("<b>ID</b> = ").append(alert.getId()).append("<br>");

        outputString.append("<br>");
        
        outputString.append("<b>Description</b> = ");
        if (alert.getDescription() != null) {
            String encodedAlertDescription = StatsAggHtmlFramework.htmlEncode(alert.getDescription());
            outputString.append(encodedAlertDescription.replaceAll("\n", "<br>")).append("<br><br>");
        }
        else outputString.append("<br><br>");

        if (alertTemplate != null) {
            outputString.append("<b>Alert Template</b> = ");
            if (alertTemplate.getName() != null) {
                String alertTemplateDetailsPopup = "<a class=\"iframe cboxElement\" href=\"AlertTemplateDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(alertTemplate.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(alertTemplate.getName()) + "</a>";
                outputString.append(alertTemplateDetailsPopup).append("<br>");
            }
            else outputString.append("<br>");
        }

        if (variableSet != null) {
            outputString.append("<b>Variable Set</b> = ");
            if (variableSet.getName() != null) {
                String variableSetDetailsPopup = "<a class=\"iframe cboxElement\" href=\"VariableSetDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(variableSet.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(variableSet.getName()) + "</a>";
                outputString.append(variableSetDetailsPopup).append("<br>");
            }
            else outputString.append("<br>");
        }

        outputString.append("<b>Metric group name</b> = ");
        if (metricGroup != null) {
            String metricGroupDetailsPopup = "<a class=\"iframe cboxElement\" href=\"MetricGroupDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(metricGroup.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(metricGroup.getName()) + "</a>";
            outputString.append(metricGroupDetailsPopup).append("<br>");
        }
        else outputString.append("<br>");

        String isEnabled = "No";
        if ((alert.isEnabled() != null) && alert.isEnabled()) isEnabled = "Yes";
        outputString.append("<b>Is alert enabled?</b> = ").append(isEnabled).append("<br>");

        String isCautionEnabled = "No";
        if ((alert.isCautionEnabled() != null) && alert.isCautionEnabled()) isCautionEnabled = "Yes";
        outputString.append("<b>Is caution alerting enabled?</b> = ").append(isCautionEnabled).append("<br>");

        String isDangerEnabled = "No";
        if ((alert.isDangerEnabled() != null) && alert.isDangerEnabled()) isDangerEnabled = "Yes";
        outputString.append("<b>Is danger alerting enabled?</b> = ").append(isDangerEnabled).append("<br>");

        outputString.append("<b>Alert type</b> = ");
        if (alert.getAlertType() != null) {
            if (alert.getAlertType() == Alert.TYPE_AVAILABILITY) outputString.append("Availability").append("<br>");
            else if (alert.getAlertType() == Alert.TYPE_THRESHOLD) outputString.append("Threshold").append("<br>");
            else outputString.append("N/A").append("<br>");
        }
        else outputString.append("N/A <br>");

        String isAlertOnPositive = "No";
        if ((alert.isAlertOnPositive() != null) && alert.isAlertOnPositive()) isAlertOnPositive = "Yes";
        outputString.append("<b>Alert on positive?</b> = ").append(isAlertOnPositive).append("<br>");

        String isAllowResendAlert = "No";
        if ((alert.isAllowResendAlert() != null) && alert.isAllowResendAlert()) isAllowResendAlert = "Yes";
        outputString.append("<b>Resend Alert?</b> = ").append(isAllowResendAlert).append("<br>");

        outputString.append("<b>Resend alert every</b> = ");
        if (alert.getResendAlertEvery() != null) {
            BigDecimal sentAlertEvery = DatabaseObjectCommon.getValueForTimeFromMilliseconds(alert.getResendAlertEvery(), alert.getResendAlertEveryTimeUnit());
            if (sentAlertEvery != null) outputString.append(sentAlertEvery.stripTrailingZeros().toPlainString());

            if (alert.getResendAlertEveryTimeUnit() != null) {
                String timeUnitString = DatabaseObjectCommon.getTimeUnitStringFromCode(alert.getResendAlertEveryTimeUnit(), true);
                if (timeUnitString != null) outputString.append(" ").append(timeUnitString);
            }
            outputString.append("<br>");
        }
        else outputString.append("N/A <br>");

        outputString.append("<br>");

        boolean isAlertSuspended = false;
        synchronized(GlobalVariables.suspensionStatusByAlertId) {
            outputString.append("<b>Is alert suspended?</b> = ");

            if ((GlobalVariables.suspensionStatusByAlertId != null) && (GlobalVariables.suspensionStatusByAlertId.get(alert.getId()) != null) &&
                    GlobalVariables.suspensionStatusByAlertId.get(alert.getId())) {
                outputString.append("Yes <br>");
                isAlertSuspended = true;
            }
            else outputString.append("No <br>");
        }

        synchronized(GlobalVariables.suspensionLevelsByAlertId) {
            outputString.append("<b>Is alert suspended, notification only?</b> = ");
            if (isAlertSuspended && (GlobalVariables.suspensionLevelsByAlertId != null) && (GlobalVariables.suspensionLevelsByAlertId.get(alert.getId()) != null)) {
                Integer suspensionLevel = GlobalVariables.suspensionLevelsByAlertId.get(alert.getId());

                if (com.pearson.statsagg.threads.alert_related.Suspensions.LEVEL_SUSPEND_ALERT_NOTIFICATION_ONLY == suspensionLevel) outputString.append("Yes <br>");
                else outputString.append("No <br>");
            }
            else outputString.append("N/A <br>");
        }

        outputString.append("<b>View Triggered Metrics</b> = ").append("<a class=\"iframe cboxElement\" href=\"AlertAssociations?ExcludeNavbar=true&amp;Name=").append(StatsAggHtmlFramework.urlEncode(alert.getName())).append("&Level=" + "Triggered" + "\">Triggered Metrics</a>");

        outputString.append("</div></div></div>").append("<div class=\"col-md-4 statsagg_three_panel_second_panel\">\n");
        outputString.append("<div class=\"panel panel-warning\"> <div class=\"panel-heading\"><b>Caution Details</b></div> <div class=\"panel-body statsagg_force_word_wrap\">");

        if ((alert.isCautionEnabled() != null) && !alert.isCautionEnabled()) outputString.append("<del>");

        outputString.append("<b>Caution notification group name</b> = ");
        if (cautionNotificationGroup != null) {
            String notificationGroupDetailsPopup = "<a class=\"iframe cboxElement\" href=\"NotificationGroupDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(cautionNotificationGroup.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(cautionNotificationGroup.getName()) + "</a>";
            outputString.append(notificationGroupDetailsPopup).append("<br>");
        }
        else outputString.append("N/A <br>");

        outputString.append("<b>Caution positive notification group name</b> = ");
        if (cautionPositiveNotificationGroup != null) {
            String notificationGroupDetailsPopup = "<a class=\"iframe cboxElement\" href=\"NotificationGroupDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(cautionPositiveNotificationGroup.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(cautionPositiveNotificationGroup.getName()) + "</a>";
            outputString.append(notificationGroupDetailsPopup).append("<br>");
        }
        else outputString.append("N/A <br>");

        outputString.append("<b>Caution window duration</b> = ");
        if (alert.getCautionWindowDuration() != null) {
            BigDecimal cautionWindowDuration = DatabaseObjectCommon.getValueForTimeFromMilliseconds(alert.getCautionWindowDuration(), alert.getCautionWindowDurationTimeUnit());
            if (cautionWindowDuration != null) outputString.append(cautionWindowDuration.stripTrailingZeros().toPlainString());

            if (alert.getCautionWindowDurationTimeUnit() != null) {
                String timeUnitString = DatabaseObjectCommon.getTimeUnitStringFromCode(alert.getCautionWindowDurationTimeUnit(), true);
                if (timeUnitString != null) outputString.append(" ").append(timeUnitString);
            }
            outputString.append("<br>");
        }
        else outputString.append("N/A <br>");

        if ((alert.getAlertType() != null) && (alert.getAlertType() == Alert.TYPE_AVAILABILITY)) {
            outputString.append("<b>Caution stop tracking after...</b> = ");
            if (alert.getCautionStopTrackingAfter() != null) {
                BigDecimal cautionStopTrackingAfter = DatabaseObjectCommon.getValueForTimeFromMilliseconds(alert.getCautionStopTrackingAfter(), alert.getCautionStopTrackingAfterTimeUnit());
                if (cautionStopTrackingAfter != null) outputString.append(cautionStopTrackingAfter.stripTrailingZeros().toPlainString());

                if (alert.getCautionStopTrackingAfterTimeUnit() != null) {
                    String timeUnitString = DatabaseObjectCommon.getTimeUnitStringFromCode(alert.getCautionStopTrackingAfterTimeUnit(), true);
                    if (timeUnitString != null) outputString.append(" ").append(timeUnitString);
                }
                outputString.append("<br>");
            }
            else outputString.append("N/A <br>");
        }          

        if ((alert.getAlertType() != null) && (alert.getAlertType() == Alert.TYPE_THRESHOLD)) {
            outputString.append("<b>Caution minimum sample count</b> = ");
            if (alert.getCautionMinimumSampleCount() != null) outputString.append(alert.getCautionMinimumSampleCount()).append("<br>");
            else outputString.append("N/A <br>");

            outputString.append("<b>Caution operator</b> = ");
            if (alert.getOperatorString(Alert.CAUTION, true, true) != null) outputString.append("'").append(alert.getOperatorString(Alert.CAUTION, true, true)).append("'<br>");
            else outputString.append("N/A <br>");

            outputString.append("<b>Caution combination</b> = ");
            if (alert.getCombinationString(Alert.CAUTION) != null) outputString.append(alert.getCombinationString(Alert.CAUTION)).append("<br>");
            else outputString.append("N/A <br>");

            outputString.append("<b>Caution combination count</b> = ");
            if (alert.getCautionCombinationCount() != null) outputString.append(alert.getCautionCombinationCount()).append("<br>");
            else outputString.append("N/A <br>");

            outputString.append("<b>Caution threshold</b> = ");
            if (alert.getCautionThreshold() != null) outputString.append(alert.getCautionThreshold().stripTrailingZeros().toPlainString()).append("<br>");
            else outputString.append("N/A <br>");
        }

        outputString.append("<br>");

        String isCautionAlertCriteriaValid = "No";
        if (alert.isCautionAlertCriteriaValid().isValid()) isCautionAlertCriteriaValid = "Yes";
        outputString.append("<b>Is the caution criteria valid?</b> = ").append(isCautionAlertCriteriaValid).append("<br>");

        String isCautionAlertActive = "No";
        if ((alert.isCautionAlertActive() != null) && alert.isCautionAlertActive()) isCautionAlertActive = "<a class=\"iframe cboxElement\" href=\"AlertAssociations?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(alert.getName()) + "&amp;Level=" + "Caution" + "\">Yes</a>";
        outputString.append("<b>Caution alert active?</b> = ").append(isCautionAlertActive).append("<br>");

        outputString.append("<b>Caution alert initially triggered at</b> = ");
        if (alert.getCautionFirstActiveAt() != null) outputString.append(DateAndTime.getFormattedDateAndTime(alert.getCautionFirstActiveAt(), "yyyy-MM-dd, h:mm:ss a")).append("<br>");
        else outputString.append("N/A <br>");

        outputString.append("<b>Caution acknowledged?</b> = ");
        if ((alert.isCautionAlertAcknowledged() != null) && alert.isCautionAlertAcknowledged()) outputString.append("Yes ").append("<br>");
        else if (alert.isCautionAlertAcknowledged() != null) outputString.append("No ").append("<br>");
        else outputString.append("N/A <br>");

        outputString.append("<b>Caution alert last notification timestamp</b> = ");
        if (alert.getCautionAlertLastSentTimestamp() != null) outputString.append(DateAndTime.getFormattedDateAndTime(alert.getCautionAlertLastSentTimestamp(), "yyyy-MM-dd, h:mm:ss a")).append("<br>");
        else outputString.append("N/A <br>");

        synchronized(GlobalVariables.pendingCautionAlertsByAlertId) {
            outputString.append("<b>Is caution alert status pending? (caused by application restart) </b> = ");
            if ((alert.getId() != null) && (GlobalVariables.pendingCautionAlertsByAlertId != null) && GlobalVariables.pendingCautionAlertsByAlertId.containsKey(alert.getId())) {
                outputString.append("Yes <br>");  
            }
            else outputString.append("No <br>");  
        }

        if ((alert.isCautionEnabled() != null) && !alert.isCautionEnabled()) outputString.append("</del>");

        outputString.append("</div></div></div>").append("<div class=\"col-md-4 statsagg_three_panel_third_panel\">\n");
        outputString.append("<div class=\"panel panel-danger\"> <div class=\"panel-heading\"><b>Danger Details</b></div> <div class=\"panel-body statsagg_force_word_wrap\">");

        if ((alert.isDangerEnabled() != null) && !alert.isDangerEnabled()) outputString.append("<del>");

        outputString.append("<b>Danger notification group name</b> = ");
        if (dangerNotificationGroup != null) {
            String notificationGroupDetailsPopup = "<a class=\"iframe cboxElement\" href=\"NotificationGroupDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(dangerNotificationGroup.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(dangerNotificationGroup.getName()) + "</a>";
            outputString.append(notificationGroupDetailsPopup).append("<br>");
        }
        else outputString.append("N/A <br>");

        outputString.append("<b>Danger positive notification group name</b> = ");
        if (dangerPositiveNotificationGroup != null) {
            String notificationGroupDetailsPopup = "<a class=\"iframe cboxElement\" href=\"NotificationGroupDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(dangerPositiveNotificationGroup.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(dangerPositiveNotificationGroup.getName()) + "</a>";
            outputString.append(notificationGroupDetailsPopup).append("<br>");
        }
        else outputString.append("N/A <br>");

        outputString.append("<b>Danger window duration</b> = ");
        if (alert.getDangerWindowDuration() != null) {
            BigDecimal dangerWindowDuration = DatabaseObjectCommon.getValueForTimeFromMilliseconds(alert.getDangerWindowDuration(), alert.getDangerWindowDurationTimeUnit());
            if (dangerWindowDuration != null) outputString.append(dangerWindowDuration.stripTrailingZeros().toPlainString());

            if (alert.getDangerWindowDurationTimeUnit() != null) {
                String timeUnitString = DatabaseObjectCommon.getTimeUnitStringFromCode(alert.getDangerWindowDurationTimeUnit(), true);
                if (timeUnitString != null) outputString.append(" ").append(timeUnitString);
            }
            outputString.append("<br>");
        }
        else outputString.append("N/A <br>");

        if ((alert.getAlertType() != null) && (alert.getAlertType() == Alert.TYPE_AVAILABILITY)) {
            outputString.append("<b>Danger stop tracking after...</b> = ");
            if (alert.getDangerStopTrackingAfter() != null) {
                BigDecimal dangerStopTrackingAfter = DatabaseObjectCommon.getValueForTimeFromMilliseconds(alert.getDangerStopTrackingAfter(), alert.getDangerStopTrackingAfterTimeUnit());
                if (dangerStopTrackingAfter != null) outputString.append(dangerStopTrackingAfter.stripTrailingZeros().toPlainString());

                if (alert.getDangerStopTrackingAfterTimeUnit() != null) {
                    String timeUnitString = DatabaseObjectCommon.getTimeUnitStringFromCode(alert.getDangerStopTrackingAfterTimeUnit(), true);
                    if (timeUnitString != null) outputString.append(" ").append(timeUnitString);
                }
                outputString.append("<br>");
            }
            else outputString.append("N/A <br>");
        }

        if ((alert.getAlertType() != null) && (alert.getAlertType() == Alert.TYPE_THRESHOLD)) {
            outputString.append("<b>Danger minimum sample count</b> = ");
            if (alert.getDangerMinimumSampleCount() != null) outputString.append(alert.getDangerMinimumSampleCount()).append("<br>");
            else outputString.append("N/A <br>");

            outputString.append("<b>Danger operator</b> = ");
            if (alert.getOperatorString(Alert.DANGER, true, true) != null) outputString.append("'").append(alert.getOperatorString(Alert.DANGER, true, true)).append("'<br>");
            else outputString.append("N/A <br>");

            outputString.append("<b>Danger combination</b> = ");
            if (alert.getCombinationString(Alert.DANGER) != null) outputString.append(alert.getCombinationString(Alert.DANGER)).append("<br>");
            else outputString.append("N/A <br>");

            outputString.append("<b>Danger combination count</b> = ");
            if (alert.getDangerCombinationCount() != null) outputString.append(alert.getDangerCombinationCount()).append("<br>");
            else outputString.append("N/A <br>");

            outputString.append("<b>Danger threshold</b> = ");
            if (alert.getDangerThreshold() != null) outputString.append(alert.getDangerThreshold().stripTrailingZeros().toPlainString()).append("<br>");
            else outputString.append("N/A <br>");
        }

        outputString.append("<br>");

        String isDangerAlertCriteriaValid = "No";
        if (alert.isDangerAlertCriteriaValid().isValid()) isDangerAlertCriteriaValid = "Yes";
        outputString.append("<b>Is the danger criteria valid?</b> = ").append(isDangerAlertCriteriaValid).append("<br>");

        String isDangerAlertActive = "No";
        if ((alert.isDangerAlertActive() != null) && alert.isDangerAlertActive()) isDangerAlertActive = "<a class=\"iframe cboxElement\" href=\"AlertAssociations?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(alert.getName()) + "&amp;Level=" + "Danger" + "\">Yes</a>";
        outputString.append("<b>Danger alert active?</b> = ").append(isDangerAlertActive).append("<br>");

        outputString.append("<b>Danger alert initially triggered at</b> = ");
        if (alert.getDangerFirstActiveAt() != null) outputString.append(DateAndTime.getFormattedDateAndTime(alert.getDangerFirstActiveAt(), "yyyy-MM-dd, h:mm:ss a")).append("<br>");
        else outputString.append("N/A <br>");

        outputString.append("<b>Danger acknowledged?</b> = ");
        if ((alert.isDangerAlertAcknowledged() != null) && alert.isDangerAlertAcknowledged()) outputString.append("Yes ").append("<br>");
        else if (alert.isDangerAlertAcknowledged() != null) outputString.append("No ").append("<br>");
        else outputString.append("N/A <br>");

        outputString.append("<b>Danger alert last notification timestamp</b> = ");
        if (alert.getDangerAlertLastSentTimestamp() != null) outputString.append(DateAndTime.getFormattedDateAndTime(alert.getDangerAlertLastSentTimestamp(), "yyyy-MM-dd, h:mm:ss a")).append("<br>");
        else outputString.append("N/A <br>");

        synchronized(GlobalVariables.pendingDangerAlertsByAlertId) {
            outputString.append("<b>Is danger alert status pending? (caused by application restart) </b> = ");
            if ((alert.getId() != null) && (GlobalVariables.pendingDangerAlertsByAlertId != null) && GlobalVariables.pendingDangerAlertsByAlertId.containsKey(alert.getId())) {
                outputString.append("Yes <br>");  
            }
            else outputString.append("No <br>");  
        }

        if ((alert.isDangerEnabled() != null) && !alert.isDangerEnabled()) outputString.append("</del>");

        outputString.append("</div></div></div>");

        return outputString.toString();
    }

}
