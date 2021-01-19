package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroup;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupsDao;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroup;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroupsDao;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class AlertsReport extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AlertsReport.class.getName());
    
    public static final String PAGE_NAME = "Alerts Report";
    
        /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        processGetRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
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
                
        try {
            boolean wordWrap = true;
            String parameter = request.getParameter("WordWrap");
            if ((parameter != null) && parameter.equalsIgnoreCase("false")) wordWrap = false;
            else if ((parameter != null) && parameter.equalsIgnoreCase("true")) wordWrap = true;
            
            String html = buildAlertsReportHtml(wordWrap);
            
            Document htmlDocument = Jsoup.parse(html);
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

    private String buildAlertsReportHtml(boolean wordWrap) {
        
        StringBuilder html = new StringBuilder();

        StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
        String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");

        StringBuilder htmlBodyStringBuilder = new StringBuilder();
        htmlBodyStringBuilder.append(
            "<div id=\"page-content-wrapper\">\n" +
            "<!-- Keep all page content within the page-content inset div! -->\n" +
            "<div class=\"page-content inset statsagg_page_content_font\">\n" +
            "  <div class=\"content-header\"> \n" +
            "    <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "    <div class=\"pull-right \">\n");
        
        if (wordWrap) htmlBodyStringBuilder.append("<a href=\"AlertsReport?WordWrap=false\" class=\"btn btn-primary statsagg_page_content_font\">View Without Word-Wrapping <i class=\"fa fa-long-arrow-right\"></i></a> \n");
        else htmlBodyStringBuilder.append("<a href=\"AlertsReport?WordWrap=true\" class=\"btn btn-primary statsagg_page_content_font\">View With Word-Wrapping <i class=\"fa fa-long-arrow-right\"></i></a> \n");

        htmlBodyStringBuilder.append(
            "    </div>\n" + 
            "  </div>\n" +   
            "  <table id=\"AlertsReportTable\" style=\"display:none\" class=\"table table-bordered table-hover compact\">\n" +
            "     <thead>\n" +
            "       <tr>\n" +
            "         <th>Alert ID</th>\n" +
            "         <th>Alert Name</th>\n" +
            "         <th>Alert Type</th>\n" + 
            "         <th>Alert Enabled?</th>\n" +
            "         <th>Alert Caution Enabled?</th>\n" +
            "         <th>Alert Danger Enabled?</th>\n" +
            "         <th>Alert Description</th>\n" +
            "         <th>Metric Group Name</th>\n" +
            "         <th>Metric Group Tags</th>\n" +
            "         <th>Metric Group Regexes</th>\n" +
            "         <th>Metric Group Regexes Blacklist</th>\n" +
            "         <th>Alert Caution Criteria</th>\n" +
            "         <th>Alert Danger Criteria</th>\n" +
            "         <th>Resend Notification Criteria</th>\n" +
            "         <th>Caution Notification Group</th>\n" +
            "         <th>Caution Notification Group Email Addresses</th>\n" +
            "         <th>Danger Notification Group</th>\n" +
            "         <th>Danger Notification Group Email Addresses</th>\n" +
            "         <th>Caution Positive Notification Group</th>\n" +
            "         <th>Caution Positive Notification Group Email Addresses</th>\n" +
            "         <th>Danger Positive Notification Group</th>\n" +
            "         <th>Danger Positive Notification Group Email Addresses</th>\n" +
            "       </tr>\n" +
            "     </thead>\n" +
            "     <tbody>\n");

        List<Alert> alerts = AlertsDao.getAlerts(DatabaseConnections.getConnection(), true);
        if (alerts == null) alerts = new ArrayList<>();
        
        Connection connection = DatabaseConnections.getConnection();
        Map<Integer, String> notificationGroupNames_ById = NotificationGroupsDao.getNotificationGroupNames_ById(connection, false);
        Map<Integer, NotificationGroup> notificationGroups_ById = NotificationGroupsDao.getNotificationGroups_ById(connection, false);
        DatabaseUtils.cleanup(connection);
        
        for (Alert alert : alerts) {
            if (alert == null) continue;
            
            try {
                MetricGroup metricGroup = MetricGroupsDao.getMetricGroup(DatabaseConnections.getConnection(), true, alert.getMetricGroupId());

                // alert id
                Integer alertId = alert.getId();

                // alert name + link
                String alertName = "<a class=\"iframe cboxElement\" href=\"AlertDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(alert.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(alert.getName()) + "</a>";

                // alert type
                String alertType = "N/A";
                if ((alert.getAlertType() != null) && (Alert.TYPE_AVAILABILITY == alert.getAlertType())) alertType = "Availability";
                else if ((alert.getAlertType() != null) && (Alert.TYPE_THRESHOLD == alert.getAlertType())) alertType = "Threshold";
                
                // alert enabled
                String alertEnabled = "No";
                if ((alert.isEnabled() != null) && alert.isEnabled()) alertEnabled = "Yes";

                // alert - caution enabled
                String alertCautionEnabled = "No";
                if ((alert.isCautionEnabled() != null) && alert.isCautionEnabled()) alertCautionEnabled = "Yes";
                
                // alert - danger enabled
                String alertDangerEnabled = "No";
                if ((alert.isDangerEnabled() != null) && alert.isDangerEnabled()) alertDangerEnabled = "Yes";
                
                // alert description
                String alertDescription = StatsAggHtmlFramework.htmlEncode(alert.getDescription());

                // metric group name + link
                String metricGroupNameAndLink;
                if ((metricGroup == null) || (metricGroup.getName() == null)) metricGroupNameAndLink = "N/A";
                else metricGroupNameAndLink = "<a class=\"iframe cboxElement\" href=\"MetricGroupDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(metricGroup.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(metricGroup.getName()) + "</a>";

                // metric tags csv
                StringBuilder metricGroupTagsCsv = new StringBuilder();
                if ((metricGroup != null) && (metricGroup.getTags() != null)) {
                    List<String> metricGroupTagsList = new ArrayList<>(metricGroup.getTags());
                    for (int i = 0; i < metricGroupTagsList.size(); i++) {
                        metricGroupTagsCsv = metricGroupTagsCsv.append("<u>").append(StatsAggHtmlFramework.htmlEncode(metricGroupTagsList.get(i).trim())).append("</u>");
                        if ((i + 1) < metricGroupTagsList.size()) metricGroupTagsCsv.append(" &nbsp;");
                    }
                }

                // metric group regexes
                StringBuilder metricGroupMatchRegexes_StringBuilder = new StringBuilder();
                if ((metricGroup != null) && (metricGroup.getMatchRegexes() != null) && !metricGroup.getMatchRegexes().isEmpty()) {
                    for (String matchRegex : metricGroup.getMatchRegexes()) {
                        metricGroupMatchRegexes_StringBuilder.append(StatsAggHtmlFramework.htmlEncode(matchRegex.trim())).append("<br>");
                    }
                }

                // metric group regexes blacklist
                StringBuilder metricGroupBlacklistRegexes_StringBuilder = new StringBuilder();
                if ((metricGroup != null) && (metricGroup.getBlacklistRegexes() != null) && !metricGroup.getBlacklistRegexes().isEmpty()) {
                    for (String blacklistRegex : metricGroup.getBlacklistRegexes()) {
                        metricGroupBlacklistRegexes_StringBuilder.append(StatsAggHtmlFramework.htmlEncode(blacklistRegex.trim())).append("<br>");
                    }
                }

                // alert - caution criteria
                String alertCriteriaCaution = "N/A";
                if (alert.isCautionEnabled() && (alert.getAlertType() == Alert.TYPE_THRESHOLD) && alert.isCautionAlertCriteriaValid().isValid()) {
                    alertCriteriaCaution = alert.getHumanReadable_AlertCriteria_MinimumSampleCount(Alert.CAUTION) + "<br><br>" + alert.getHumanReadable_AlertCriteria_ThresholdCriteria(Alert.CAUTION);
                }
                else if (alert.isCautionEnabled() && (alert.getAlertType() == Alert.TYPE_AVAILABILITY) && alert.isCautionAlertCriteriaValid().isValid()) {
                    alertCriteriaCaution = alert.getHumanReadable_AlertCriteria_AvailabilityCriteria(Alert.CAUTION);
                }
                
                // alert - danger criteria
                String alertCriteriaDanger = "N/A";
                if (alert.isDangerEnabled() && (alert.getAlertType() == Alert.TYPE_THRESHOLD) && alert.isDangerAlertCriteriaValid().isValid()) {
                    alertCriteriaDanger = alert.getHumanReadable_AlertCriteria_MinimumSampleCount(Alert.DANGER) + "<br><br>" + alert.getHumanReadable_AlertCriteria_ThresholdCriteria(Alert.DANGER);
                }
                else if (alert.isDangerEnabled() && (alert.getAlertType() == Alert.TYPE_AVAILABILITY) && alert.isDangerAlertCriteriaValid().isValid()) {
                    alertCriteriaDanger = alert.getHumanReadable_AlertCriteria_AvailabilityCriteria(Alert.DANGER);
                }
                
                // alert - resend criteria
                String alertResend = "N/A";
                if (alert.isAllowResendAlert() != null && alert.isAllowResendAlert()) {
                    BigDecimal sentAlertEvery_BigDecimal = DatabaseObjectCommon.getValueForTimeFromMilliseconds(alert.getResendAlertEvery(), alert.getResendAlertEveryTimeUnit());
                    String timeUnitString = null;
                    if (alert.getResendAlertEveryTimeUnit() != null) timeUnitString = DatabaseObjectCommon.getTimeUnitStringFromCode(alert.getResendAlertEveryTimeUnit(), true);
                    if ((sentAlertEvery_BigDecimal != null) && (timeUnitString != null)) alertResend = sentAlertEvery_BigDecimal.stripTrailingZeros().toPlainString() + " " + timeUnitString;
                }
                
                // caution notification name + link
                String cautionNotificationGroupNameAndLink, cautionEmailAddressesCsv = "";
                if ((notificationGroupNames_ById == null) || (alert.getCautionNotificationGroupId() == null) || (alert.isCautionEnabled() == null) || !alert.isCautionEnabled() ||
                        !notificationGroupNames_ById.containsKey(alert.getCautionNotificationGroupId())) {
                    cautionNotificationGroupNameAndLink = "N/A";
                    cautionEmailAddressesCsv = "N/A";
                }
                else {
                    cautionNotificationGroupNameAndLink = "<a class=\"iframe cboxElement\" href=\"NotificationGroupDetails?ExcludeNavbar=true&amp;Name=" + 
                        StatsAggHtmlFramework.urlEncode(notificationGroupNames_ById.get(alert.getCautionNotificationGroupId())) + "\">" + 
                        StatsAggHtmlFramework.htmlEncode(notificationGroupNames_ById.get(alert.getCautionNotificationGroupId())) + "</a>";

                    NotificationGroup notificationGroup = notificationGroups_ById.get(alert.getCautionNotificationGroupId());
                    if (notificationGroup != null) cautionEmailAddressesCsv = StatsAggHtmlFramework.htmlEncode(notificationGroup.getEmailAddressesCsv());
                }

                // danger notification name + link, also the danger email addresses
                String dangerNotificationGroupNameAndLink, dangerEmailAddressesCsv = "";
                if ((notificationGroupNames_ById == null) || (alert.getDangerNotificationGroupId() == null) || (alert.isDangerEnabled() == null) || !alert.isDangerEnabled() ||
                        !notificationGroupNames_ById.containsKey(alert.getDangerNotificationGroupId())) {
                    dangerNotificationGroupNameAndLink = "N/A";
                    dangerEmailAddressesCsv = "N/A";
                }
                else {
                    dangerNotificationGroupNameAndLink = "<a class=\"iframe cboxElement\" href=\"NotificationGroupDetails?ExcludeNavbar=true&amp;Name=" + 
                        StatsAggHtmlFramework.urlEncode(notificationGroupNames_ById.get(alert.getDangerNotificationGroupId())) + "\">" + 
                        StatsAggHtmlFramework.htmlEncode(notificationGroupNames_ById.get(alert.getDangerNotificationGroupId())) + "</a>";

                    NotificationGroup notificationGroup = notificationGroups_ById.get(alert.getDangerNotificationGroupId());
                    if (notificationGroup != null) dangerEmailAddressesCsv = StatsAggHtmlFramework.htmlEncode(notificationGroup.getEmailAddressesCsv());
                }
                
                //  caution positive notification name + link
                String cautionPositiveNotificationGroupNameAndLink, cautionPositiveEmailAddressesCsv = "";
                if ((notificationGroupNames_ById == null) || (alert.getCautionPositiveNotificationGroupId() == null) || (alert.isCautionEnabled() == null) || !alert.isCautionEnabled() ||
                        !notificationGroupNames_ById.containsKey(alert.getCautionPositiveNotificationGroupId()) && (alert.isAlertOnPositive() != null) && alert.isAlertOnPositive()) {
                    cautionPositiveNotificationGroupNameAndLink = "N/A";
                    cautionPositiveEmailAddressesCsv = "N/A";
                }
                else {
                    cautionPositiveNotificationGroupNameAndLink = "<a class=\"iframe cboxElement\" href=\"NotificationGroupDetails?ExcludeNavbar=true&amp;Name=" + 
                        StatsAggHtmlFramework.urlEncode(notificationGroupNames_ById.get(alert.getCautionPositiveNotificationGroupId())) + "\">" + 
                        StatsAggHtmlFramework.htmlEncode(notificationGroupNames_ById.get(alert.getCautionPositiveNotificationGroupId())) + "</a>";

                    NotificationGroup notificationGroup = notificationGroups_ById.get(alert.getCautionPositiveNotificationGroupId());
                    if (notificationGroup != null) cautionPositiveEmailAddressesCsv = StatsAggHtmlFramework.htmlEncode(notificationGroup.getEmailAddressesCsv());
                }
                
                //  danger positive notification name + link
                String dangerPositiveNotificationGroupNameAndLink, dangerPositiveEmailAddressesCsv = "";
                if ((notificationGroupNames_ById == null) || (alert.getDangerPositiveNotificationGroupId() == null) || (alert.isDangerEnabled() == null) || !alert.isDangerEnabled() ||
                        !notificationGroupNames_ById.containsKey(alert.getDangerPositiveNotificationGroupId()) && (alert.isAlertOnPositive() != null) && alert.isAlertOnPositive()) {
                    dangerPositiveNotificationGroupNameAndLink = "N/A";
                    dangerPositiveEmailAddressesCsv = "N/A";
                }
                else {
                    dangerPositiveNotificationGroupNameAndLink = "<a class=\"iframe cboxElement\" href=\"NotificationGroupDetails?ExcludeNavbar=true&amp;Name=" + 
                        StatsAggHtmlFramework.urlEncode(notificationGroupNames_ById.get(alert.getDangerPositiveNotificationGroupId())) + "\">" + 
                        StatsAggHtmlFramework.htmlEncode(notificationGroupNames_ById.get(alert.getDangerPositiveNotificationGroupId())) + "</a>";

                    NotificationGroup notificationGroup = notificationGroups_ById.get(alert.getDangerPositiveNotificationGroupId());
                    if (notificationGroup != null) dangerPositiveEmailAddressesCsv = StatsAggHtmlFramework.htmlEncode(notificationGroup.getEmailAddressesCsv());
                }

                String wordWrapClass = (wordWrap) ? "class=\"statsagg_force_word_break\"" : "";
                
                htmlBodyStringBuilder
                        .append("<tr>\n")
                        .append("<td>").append(alertId).append("</td>\n")
                        .append("<td ").append(wordWrapClass).append(">").append(alertName).append("</td>\n")
                        .append("<td>").append(alertType).append("</td>\n")
                        .append("<td>").append(alertEnabled).append("</td>\n")
                        .append("<td>").append(alertCautionEnabled).append("</td>\n")
                        .append("<td>").append(alertDangerEnabled).append("</td>\n")
                        .append("<td ").append(wordWrapClass).append(">").append(alertDescription).append("</td>\n")
                        .append("<td ").append(wordWrapClass).append(">").append(metricGroupNameAndLink).append("</td>\n")
                        .append("<td ").append(wordWrapClass).append(">").append(metricGroupTagsCsv.toString()).append("</td>\n")
                        .append("<td ").append(wordWrapClass).append(">").append(metricGroupMatchRegexes_StringBuilder.toString()).append("</td>\n")
                        .append("<td ").append(wordWrapClass).append(">").append(metricGroupBlacklistRegexes_StringBuilder.toString()).append("</td>\n")
                        .append("<td ").append(wordWrapClass).append(">").append(alertCriteriaCaution).append("</td>\n")
                        .append("<td ").append(wordWrapClass).append(">").append(alertCriteriaDanger).append("</td>\n")
                        .append("<td ").append(wordWrapClass).append(">").append(alertResend).append("</td>\n")
                        .append("<td ").append(wordWrapClass).append(">").append(cautionNotificationGroupNameAndLink).append("</td>\n")
                        .append("<td ").append(wordWrapClass).append(">").append(cautionEmailAddressesCsv).append("</td>\n")
                        .append("<td ").append(wordWrapClass).append(">").append(dangerNotificationGroupNameAndLink).append("</td>\n")
                        .append("<td ").append(wordWrapClass).append(">").append(dangerEmailAddressesCsv).append("</td>\n")
                        .append("<td ").append(wordWrapClass).append(">").append(cautionPositiveNotificationGroupNameAndLink).append("</td>\n")
                        .append("<td ").append(wordWrapClass).append(">").append(cautionPositiveEmailAddressesCsv).append("</td>\n")
                        .append("<td ").append(wordWrapClass).append(">").append(dangerPositiveNotificationGroupNameAndLink).append("</td>\n")
                        .append("<td ").append(wordWrapClass).append(">").append(dangerPositiveEmailAddressesCsv).append("</td>\n")
                        ;

                htmlBodyStringBuilder.append("</tr>\n");
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }

        htmlBodyStringBuilder.append(""
                + "</tbody>\n"
                + "<tfoot> \n"
                + "  <tr>\n" 
                + "    <th></th>\n"
                + "    <th></th>\n" 
                + "    <th></th>\n" 
                + "    <th></th>\n" 
                + "    <th></th>\n" 
                + "    <th></th>\n" 
                + "    <th></th>\n" 
                + "    <th></th>\n" 
                + "    <th></th>\n" 
                + "    <th></th>\n" 
                + "    <th></th>\n" 
                + "    <th></th>\n" 
                + "    <th></th>\n" 
                + "    <th></th>\n" 
                + "    <th></th>\n" 
                + "    <th></th>\n" 
                + "    <th></th>\n" 
                + "    <th></th>\n" 
                + "    <th></th>\n" 
                + "    <th></th>\n" 
                + "    <th></th>\n" 
                + "    <th></th>\n" 
                + "  </tr>\n" 
                + "</tfoot>" 
                + "</table>\n"
                + "</div>\n"
                + "</div>\n");
        
        String htmlBody = (statsAggHtmlFramework.createHtmlBody(htmlBodyStringBuilder.toString()));

        html.append(""
                + "<!DOCTYPE html>\n"
                + "<html>\n")
                .append(htmlHeader)
                .append(htmlBody)
                .append("</html>");
        
        return html.toString();
    }
    
}
