package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.database_objects.notification_groups.NotificationGroupsDaoWrapper;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.threads.alert_related.NotificationThread;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroup;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupTag;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroup;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroupsDao;
import com.pearson.statsagg.configuration.ApplicationConfiguration;
import com.pearson.statsagg.database_objects.notification_group_templates.NotificationGroupTemplate;
import com.pearson.statsagg.database_objects.notification_group_templates.NotificationGroupTemplatesDao;
import com.pearson.statsagg.database_objects.pagerduty_services.PagerdutyService;
import com.pearson.statsagg.database_objects.pagerduty_services.PagerdutyServicesDao;
import com.pearson.statsagg.utilities.core_utils.KeyValue;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import java.sql.Connection;
import java.util.concurrent.ConcurrentHashMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class NotificationGroups extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(NotificationGroups.class.getName());

    public static final String PAGE_NAME = "Notification Groups";
    
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
            String html = buildNotificationGroupsHtml();
            
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
        
        try {
            String operation = request.getParameter("Operation");

            if ((operation != null) && operation.equals("Clone")) {
                Integer id = Integer.parseInt(request.getParameter("Id"));
                cloneNotificationGroup(id);
            }

            if ((operation != null) && operation.equals("Test")) {
                Integer id = Integer.parseInt(request.getParameter("Id"));
                testNotificationGroupEmail(id);
                testNotificationGroupPagerDuty(id);
            }

            if ((operation != null) && operation.equals("Remove")) {
                Integer id = Integer.parseInt(Common.getSingleParameterAsString(request, "Id"));
                removeNotificationGroup(id);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        StatsAggHtmlFramework.redirectAndGet(response, 303, "NotificationGroups");
    }
    
    private void cloneNotificationGroup(Integer notificationGroupId) {
        
        if (notificationGroupId == null) {
            return;
        }
        
        Connection connection = null;
        
        try {
            connection = DatabaseConnections.getConnection();

            NotificationGroup notificationGroup = NotificationGroupsDao.getNotificationGroup(connection, false, notificationGroupId);
            boolean isNotificationGroupCreatedByNotificationGroupTemplate = NotificationGroupsDao.isNotificationGroupCreatedByNotificationGroupTemplate(connection, false, notificationGroup, null);
            if (isNotificationGroupCreatedByNotificationGroupTemplate) logger.warn("Can't clone an notification group that was created by an notification group template. NotificationGroupName=\"" + notificationGroup.getName() + "\".");
            
            if ((notificationGroup != null) && (notificationGroup.getName() != null) && !isNotificationGroupCreatedByNotificationGroupTemplate) {
                List<NotificationGroup> allNotificationGroups = NotificationGroupsDao.getNotificationGroups(connection, true);
                
                Set<String> allNotificationGroupNames = new HashSet<>();
                for (NotificationGroup currentNotificationGroup : allNotificationGroups) {
                    if (currentNotificationGroup.getName() != null) allNotificationGroupNames.add(currentNotificationGroup.getName());
                }

                NotificationGroup clonedNotificationGroup = NotificationGroup.copy(notificationGroup);
                clonedNotificationGroup.setId(-1);
                String clonedAlterName = StatsAggHtmlFramework.createCloneName(notificationGroup.getName(), allNotificationGroupNames);
                clonedNotificationGroup.setName(clonedAlterName);

                NotificationGroupsDaoWrapper.createRecordInDatabase(clonedNotificationGroup);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public String removeNotificationGroup(Integer notificationGroupId) {
        
        if (notificationGroupId == null) {
            return "Notification Group ID field can't be null";
        }
        
        String returnString = null;
        
        Connection connection = null;
        
        try {
            connection = DatabaseConnections.getConnection(); 
           
            NotificationGroup notificationGroup = NotificationGroupsDao.getNotificationGroup(DatabaseConnections.getConnection(), true, notificationGroupId);   
            boolean isNotificationGroupCreatedByNotificationGroupTemplate = NotificationGroupsDao.isNotificationGroupCreatedByNotificationGroupTemplate(connection, false, notificationGroup, null);
            
            if (!isNotificationGroupCreatedByNotificationGroupTemplate) {
                NotificationGroupsDaoWrapper notificationGroupsDaoWrapper = NotificationGroupsDaoWrapper.deleteRecordInDatabase(notificationGroup);
                returnString = notificationGroupsDaoWrapper.getReturnString();
            }
            else {
                String notificationGroupName = (notificationGroup == null) ? "Unknown" : notificationGroup.getName();
                returnString = "Can't remove a notification group that was created by a notification group template. NotificationGroupName=\"" + notificationGroupName + "\".";
                logger.warn(returnString);
            }
        }
        catch (Exception e) {
            returnString = "Error removing notification group";
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            DatabaseUtils.cleanup(connection);
        }
        
        return returnString;
    }
    
    private String buildNotificationGroupsHtml() {
        
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
            "    <div class=\"pull-right \">\n" +
            "     <a href=\"CreateNotificationGroup\" class=\"btn btn-primary statsagg_page_content_font\">Create New Notification Group <i class=\"fa fa-long-arrow-right\"></i></a> \n" +
            "    </div>" +   
            "  </div>" +    
            "  <table id=\"NotificationGroupsTable\" style=\"display:none\" class=\"table table-bordered table-hover \">\n" +
            "    <thead>\n" +
            "      <tr>\n" +
            "        <th>Notification Group Name</th>\n" +
            "        <th>Template</th>\n");
        
        htmlBodyStringBuilder.append("<th>Email addresses");
        if (ApplicationConfiguration.isPagerdutyIntegrationEnabled()) htmlBodyStringBuilder.append(" & PagerDuty services");
        htmlBodyStringBuilder.append("</th>\n");
        
        htmlBodyStringBuilder.append(
            "        <th>Operations</th>\n" +
            "      </tr>\n" +
            "    </thead>\n" +
            "    <tbody>\n");

        Connection connection = null;
        
        try {
            connection = DatabaseConnections.getConnection();
            Set<Integer> notificationGroupIdsAssociatedWithAlerts = AlertsDao.getDistinctNotificationGroupIdsAssociatedWithAlerts(connection, false);
            if (notificationGroupIdsAssociatedWithAlerts == null) notificationGroupIdsAssociatedWithAlerts = new HashSet<>();
            List<NotificationGroup> notificationGroups = NotificationGroupsDao.getNotificationGroups(connection, false);
            if (notificationGroups == null) notificationGroups = new ArrayList<>();
            Map<Integer,PagerdutyService> pagerdutyServices_ById = PagerdutyServicesDao.getPagerdutyServices_ById(connection, false);
            if (pagerdutyServices_ById == null) pagerdutyServices_ById = new HashMap<>();
            
            for (NotificationGroup notificationGroup : notificationGroups) {     
                if ((notificationGroup == null) || (notificationGroup.getId() == null) || (notificationGroup.getName() == null)) continue;

                String notificationGroupDetails = "<a class=\"iframe cboxElement\" href=\"NotificationGroupDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(notificationGroup.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(notificationGroup.getName()) + "</a>";

                String notificationGroupTemplateNameAndLink;
                NotificationGroupTemplate notificationGroupTemplate = NotificationGroupTemplatesDao.getNotificationGroupTemplate(connection, false, notificationGroup.getNotificationGroupTemplateId());
                if ((notificationGroupTemplate == null) || (notificationGroupTemplate.getName() == null)) notificationGroupTemplateNameAndLink = "N/A";
                else notificationGroupTemplateNameAndLink = "<a class=\"iframe cboxElement\" href=\"NotificationGroupTemplateDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(notificationGroupTemplate.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(notificationGroupTemplate.getName()) + "</a>";
                
                String emailAddressesAndServicesCsv = notificationGroup.getEmailAddressesCsv();
                if (emailAddressesAndServicesCsv == null) emailAddressesAndServicesCsv = "";

                String pagerdutyServiceName = "";
                if (notificationGroup.getPagerdutyServiceId() != null) {
                    PagerdutyService pagerdutyService = pagerdutyServices_ById.get(notificationGroup.getPagerdutyServiceId());
                    if ((pagerdutyService != null) && (pagerdutyService.getName() != null)) pagerdutyServiceName = pagerdutyService.getName();
                }

                if (ApplicationConfiguration.isPagerdutyIntegrationEnabled() && emailAddressesAndServicesCsv.isEmpty()) emailAddressesAndServicesCsv = pagerdutyServiceName;
                else if (ApplicationConfiguration.isPagerdutyIntegrationEnabled() && !emailAddressesAndServicesCsv.isEmpty() && !pagerdutyServiceName.isEmpty()) {
                    emailAddressesAndServicesCsv = emailAddressesAndServicesCsv + ", " + pagerdutyServiceName;
                }

                String alter = "<a href=\"CreateNotificationGroup?Operation=Alter&amp;Name=" + StatsAggHtmlFramework.urlEncode(notificationGroup.getName()) + "\">alter</a>";

                List<KeyValue<String,String>> cloneKeysAndValues = new ArrayList<>();
                cloneKeysAndValues.add(new KeyValue("Operation", "Clone"));
                cloneKeysAndValues.add(new KeyValue("Id", notificationGroup.getId().toString()));
                String clone = StatsAggHtmlFramework.buildJavaScriptPostLink("Clone_" + notificationGroup.getName(), "NotificationGroups", "clone", cloneKeysAndValues);

                List<KeyValue<String,String>> testKeysAndValues = new ArrayList<>();
                testKeysAndValues.add(new KeyValue("Operation", "Test"));
                testKeysAndValues.add(new KeyValue("Id", notificationGroup.getId().toString()));

                String test;
                if (ApplicationConfiguration.isPagerdutyIntegrationEnabled()) test = StatsAggHtmlFramework.buildJavaScriptPostLink("Test_" + notificationGroup.getName(), "NotificationGroups", "test", testKeysAndValues, true, "Are you sure you want to send a test alert to email and/or PagerDuty for \\'" + Encode.forJavaScript(notificationGroup.getName()) + "\\'?");
                else test = StatsAggHtmlFramework.buildJavaScriptPostLink("Test_" + notificationGroup.getName(), "NotificationGroups", "test", testKeysAndValues, true, "Are you sure you want to send a test email alert to \\'" + Encode.forJavaScript(notificationGroup.getName()) + "\\'?");

                List<KeyValue<String,String>> removeKeysAndValues = new ArrayList<>();
                removeKeysAndValues.add(new KeyValue("Operation", "Remove"));
                removeKeysAndValues.add(new KeyValue("Id", notificationGroup.getId().toString()));
                String remove = StatsAggHtmlFramework.buildJavaScriptPostLink("Remove_" + notificationGroup.getName(), "NotificationGroups", "remove", 
                        removeKeysAndValues, true, "Are you sure you want to remove this notification group?");       

                htmlBodyStringBuilder.append("<tr>\n")
                    .append("<td class=\"statsagg_force_word_break\">").append(notificationGroupDetails).append("</td>\n")
                    .append("<td class=\"statsagg_force_word_break\">").append(notificationGroupTemplateNameAndLink).append("</td>\n")
                    .append("<td class=\"statsagg_force_word_break\">").append(StatsAggHtmlFramework.htmlEncode(emailAddressesAndServicesCsv)).append("</td>\n")
                    .append("<td>");

                if (notificationGroup.getNotificationGroupTemplateId() == null) htmlBodyStringBuilder.append(alter).append(", ").append(clone).append(", ").append(test);
                else htmlBodyStringBuilder.append(test);
                if ((notificationGroup.getNotificationGroupTemplateId() == null) && !notificationGroupIdsAssociatedWithAlerts.contains(notificationGroup.getId())) htmlBodyStringBuilder.append(", ").append(remove);

                htmlBodyStringBuilder.append("</td>\n").append("</tr>\n");
            }

            htmlBodyStringBuilder.append(""
                    + "</tbody>\n"
                    + "<tfoot> \n"
                    + "  <tr>\n" 
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
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return "Fatal error encountered";
        }
        finally {
            DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public void testNotificationGroupEmail(Integer notificationGroupId) {
        
        if (notificationGroupId == null) {
            logger.error("Failed to send email alert to notification group. ID cannot be null" );
            return;
        }

        try {
            NotificationGroup notificationGroup = NotificationGroupsDao.getNotificationGroup(DatabaseConnections.getConnection(), true, notificationGroupId);

            if ((notificationGroup == null) || (notificationGroup.getName() == null) || (notificationGroup.getId() == null)) {
                logger.warn("Failed to send email alert to notification group id=" + notificationGroupId + ". Notification group does not exist." );
                return;
            }

            String testAlertName = "Notification test - alert";
            Alert testAlert = new Alert(99999, testAlertName, testAlertName.toUpperCase(), 
                    "This is a fake alert to test sending email alerts to the notification group named '" + notificationGroup.getName() + "'",
                    null, null,
                    88888, true, true, true, Alert.TYPE_THRESHOLD, false, false, 60l, DatabaseObjectCommon.TIME_UNIT_MINUTES,
                    77777, 77777, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 9900L, 
                    DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 1, true, new Timestamp(System.currentTimeMillis()), false, null, null,
                    77777, 77777, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("200"), 91000L, 
                    DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 2, true, new Timestamp(System.currentTimeMillis()), false, null, null);

            String testMetricGroupName = "Notification test - metric group";
            MetricGroup metricGroup = new MetricGroup(88888, testMetricGroupName, testMetricGroupName.toUpperCase(),
                    "This is a fake metric group to test sending email alerts to the notification group named '" + notificationGroup.getName() + "'",
                    null, null, null, null, null);

            List<String> metricKeys = new ArrayList<>();
            metricKeys.add("emailtest.metric2");
            metricKeys.add("emailtest.metric1");
            metricKeys.add("emailtest.metric3");
            metricKeys.add("emailtest.metric4");
            metricKeys.add("emailtest.metric5");

            Map<String,BigDecimal> alertMetricValues = new HashMap<>();
            alertMetricValues.put("emailtest.metric1-99999", new BigDecimal(101));
            alertMetricValues.put("emailtest.metric2-99999", new BigDecimal(102));
            alertMetricValues.put("emailtest.metric3-99999", new BigDecimal(103));
            alertMetricValues.put("emailtest.metric4-99999", new BigDecimal(104));

            List<MetricGroupTag> metricGroupTags = new ArrayList<>();
            metricGroupTags.add(new MetricGroupTag(777, 88888, "tag1"));
            metricGroupTags.add(new MetricGroupTag(778, 88888, "tag2"));
            metricGroupTags.add(new MetricGroupTag(779, 88888, "tag3"));

            NotificationThread notificationThread = new NotificationThread(testAlert, Alert.CAUTION, metricKeys, alertMetricValues, new ConcurrentHashMap<>(),
                    false, false, ApplicationConfiguration.getAlertStatsAggLocation());
            notificationThread.buildAlertEmail(3, metricGroup, metricGroupTags);

            List<String> emailsAddresses = NotificationThread.getToEmailsAddressesForAlert(notificationGroup.getId());

            notificationThread.sendEmail(emailsAddresses, notificationThread.getSubject(), notificationThread.getEmailBody());

            String cleanNotificationGroupName = StringUtilities.removeNewlinesFromString(notificationGroup.getName(), ' ');
            logger.info("Sent test email alert to notification group '" + cleanNotificationGroupName + "'");
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }
    
    public static void testNotificationGroupPagerDuty(Integer notificationGroupId) {
        
        if (notificationGroupId == null) {
            logger.error("Failed to send pager duty alert to notification group. ID cannot be null" );
            return;
        }
        
        try {
            NotificationGroup notificationGroup = NotificationGroupsDao.getNotificationGroup(DatabaseConnections.getConnection(), true, notificationGroupId);
        
            if ((notificationGroup == null) || (notificationGroup.getName() == null) || (notificationGroup.getId() == null)) {
                logger.warn("Failed to send pager duty alert to notification group id=" + notificationGroupId + ". Notification group does not exist." );
                return;
            }

            String testAlertName = "Notification test - alert";
            Alert testAlert = new Alert(99999, testAlertName, testAlertName.toUpperCase(), 
                    "This is a fake alert to test sending email alerts to the notification group named '" + notificationGroup.getName() + "'",
                    null, null,
                    88888, true, true, true, Alert.TYPE_THRESHOLD, false, false, 60l, DatabaseObjectCommon.TIME_UNIT_MINUTES,
                    77777, 77777, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 9900L, 
                    DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 1, true, new Timestamp(System.currentTimeMillis()), false, null, null,
                    77777, 77777, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("200"), 91000L, 
                    DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 2, true, new Timestamp(System.currentTimeMillis()), false, null, null);

            String testMetricGroupName = "Notification test - metric group";
            MetricGroup metricGroup = new MetricGroup(88888, testMetricGroupName, testMetricGroupName.toUpperCase(),
                    "This is a fake metric group to test sending pager duty alerts to the notification group named '" + notificationGroup.getName() + "'",
                    null, null, null, null, null);

            List<String> metricKeys = new ArrayList<>();
            metricKeys.add("pagerduty.test.metric2");
            metricKeys.add("pagerduty.test.metric1");
            metricKeys.add("pagerduty.test.metric3");
            metricKeys.add("pagerduty.test.metric4");
            metricKeys.add("pagerduty.test.metric5");

            Map<String,BigDecimal> alertMetricValues = new HashMap<>();
            alertMetricValues.put("pagerduty.test.metric1-99999", new BigDecimal(101));
            alertMetricValues.put("pagerduty.test.metric2-99999", new BigDecimal(102));
            alertMetricValues.put("pagerduty.test.metric3-99999", new BigDecimal(103));
            alertMetricValues.put("pagerduty.test.metric4-99999", new BigDecimal(104));

            List<MetricGroupTag> metricGroupTags = new ArrayList<>();
            metricGroupTags.add(new MetricGroupTag(777, 88888, "tag1"));
            metricGroupTags.add(new MetricGroupTag(778, 88888, "tag2"));
            metricGroupTags.add(new MetricGroupTag(779, 88888, "tag3"));

            NotificationThread notificationThread = new NotificationThread(testAlert, Alert.CAUTION, metricKeys, alertMetricValues, new ConcurrentHashMap<>(),
                    false, false, ApplicationConfiguration.getAlertStatsAggLocation());
            notificationThread.buildPagerdutyEvent(3, metricGroup, metricGroupTags);

            String apiKey = NotificationThread.getPagerdutyRoutingKeyForAlert(notificationGroup.getId());

            notificationThread.sendPagerdutyEvent(apiKey, notificationThread.getPagerdutyPayload());

            String cleanNotificationGroupName = StringUtilities.removeNewlinesFromString(notificationGroup.getName(), ' ');
            logger.info("Sent test pager duty event to notification group '" + cleanNotificationGroupName + "'");
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
    }
    
}
