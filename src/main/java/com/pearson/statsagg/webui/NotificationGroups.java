package com.pearson.statsagg.webui;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.alerts.EmailThread;
import com.pearson.statsagg.database.alerts.Alert;
import com.pearson.statsagg.database.alerts.AlertsDao;
import com.pearson.statsagg.database.metric_group.MetricGroup;
import com.pearson.statsagg.database.notifications.NotificationGroup;
import com.pearson.statsagg.database.notifications.NotificationGroupsDao;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.utilities.KeyValue;
import com.pearson.statsagg.utilities.StackTrace;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "NotificationGroups", urlPatterns = {"/NotificationGroups"})
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
        
        response.setContentType("text/html");
        PrintWriter out = null;

        try {
            String html = buildNotificationGroupsHtml();
            
            Document htmlDocument = Jsoup.parse(html);
            String htmlFormatted  = htmlDocument.toString();
            out = response.getWriter();
            if (ApplicationConfiguration.isDebugModeEnabled()) out.println(html);
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
    
    protected void processPostRequest(HttpServletRequest request, HttpServletResponse response) {
        
        if ((request == null) || (response == null)) {
            return;
        }
        
        String operation = request.getParameter("Operation");
        
        if ((operation != null) && operation.equals("Clone")) {
            String name = request.getParameter("Name");
            cloneNotificationGroup(name);
        }
        
        if ((operation != null) && operation.equals("Test")) {
            String name = request.getParameter("Name");
            testNotificationGroup(name);
        }
        
        if ((operation != null) && operation.equals("Remove")) {
            String name = request.getParameter("Name");
            removeNotificationGroup(name);
        }
        
        processGetRequest(request, response);
    }
    
    private void cloneNotificationGroup(String notificationGroupName) {
        
        if (notificationGroupName == null) {
            return;
        }
        
        try {
            NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao(false);
            NotificationGroup notificationGroup = notificationGroupsDao.getNotificationGroupByName(notificationGroupName);
            List<NotificationGroup> allNotificationGroups = notificationGroupsDao.getAllDatabaseObjectsInTable();
            notificationGroupsDao.close();

            Set<String> allNotificationGroupNames = new HashSet<>();
            for (NotificationGroup currentNotificationGroup : allNotificationGroups) {
                allNotificationGroupNames.add(currentNotificationGroup.getName());
            }

            NotificationGroup clonedNotificationGroup = NotificationGroup.copy(notificationGroup);
            clonedNotificationGroup.setId(-1);
            String clonedAlterName = StatsAggHtmlFramework.createCloneName(notificationGroup.getName(), allNotificationGroupNames);
            clonedNotificationGroup.setName(clonedAlterName);
            clonedNotificationGroup.setUppercaseName(clonedAlterName.toUpperCase());
            
            NotificationGroupsLogic notificationGroupsLogic = new NotificationGroupsLogic();
            notificationGroupsLogic.alterRecordInDatabase(clonedNotificationGroup);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
    private void removeNotificationGroup(String notificationGroupName) {
        
        if (notificationGroupName == null) {
            return;
        }
        
        NotificationGroupsLogic notificationGroupsLogic = new NotificationGroupsLogic();
        notificationGroupsLogic.deleteRecordInDatabase(notificationGroupName);
    }
    
    private String buildNotificationGroupsHtml() {
        
        StringBuilder html = new StringBuilder("");

        StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
        String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");

        StringBuilder htmlBodyStringBuilder = new StringBuilder("");
        htmlBodyStringBuilder.append(
            "<div id=\"page-content-wrapper\">\n" + 
            "<!-- Keep all page content within the page-content inset div! -->\n" +
            "<div class=\"page-content inset\">\n" +
            "  <div class=\"content-header\"> \n" +
            "    <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "    <div class=\"pull-right \">\n" +
            "     <a href=\"CreateNotificationGroup\" class=\"btn btn-primary\">Create New Notification Group <i class=\"fa fa-long-arrow-right\"></i></a> \n" +
            "    </div>" +   
            "  </div>" +    
            "  <table id=\"NotificationGroupsTable\" style=\"font-size:12px;\" class=\"table table-bordered table-hover \">\n" +
            "    <thead>\n" +
            "      <tr>\n" +
            "        <th>Notification Group Name</th>\n" +
            "        <th>Email addresses</th>\n" +
            "        <th>Operations</th>\n" +
            "      </tr>\n" +
            "    </thead>\n" +
            "    <tbody>\n");

        Set<Integer> notificationGroupIdsAssociatedWithAlerts = AlertsDao.getDistinctNotificationGroupIds();
        
        NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
        List<NotificationGroup> notificationGroups = notificationGroupsDao.getAllDatabaseObjectsInTable();

        for (NotificationGroup notificationGroup : notificationGroups) {     
            
            String metricGroupDetails = "<a href=\"NotificationGroupDetails?Name=" + StatsAggHtmlFramework.urlEncode(notificationGroup.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(notificationGroup.getName()) + "</a>";
            String alter = "<a href=\"CreateNotificationGroup?Operation=Alter&amp;Name=" + StatsAggHtmlFramework.urlEncode(notificationGroup.getName()) + "\">alter</a>";

            List<KeyValue> cloneKeysAndValues = new ArrayList<>();
            cloneKeysAndValues.add(new KeyValue("Operation", "Clone"));
            cloneKeysAndValues.add(new KeyValue("Name", notificationGroup.getName()));
            String clone = StatsAggHtmlFramework.buildJavaScriptPostLink("Clone_" + notificationGroup.getName(), "NotificationGroups", "clone", cloneKeysAndValues);
            
            List<KeyValue> testKeysAndValues = new ArrayList<>();
            testKeysAndValues.add(new KeyValue("Operation", "Test"));
            testKeysAndValues.add(new KeyValue("Name", notificationGroup.getName()));
            String test = StatsAggHtmlFramework.buildJavaScriptPostLink("Test_" + notificationGroup.getName(), "NotificationGroups", "test", 
                    testKeysAndValues, true, "Are you sure you want to send a test email alert to \\'" + notificationGroup.getName() + "\\'?");
            
            List<KeyValue> removeKeysAndValues = new ArrayList<>();
            removeKeysAndValues.add(new KeyValue("Operation", "Remove"));
            removeKeysAndValues.add(new KeyValue("Name", notificationGroup.getName()));
            String remove = StatsAggHtmlFramework.buildJavaScriptPostLink("Remove_" + notificationGroup.getName(), "NotificationGroups", "remove", 
                    removeKeysAndValues, true, "Are you sure you want to remove this notification group?");       
            
            htmlBodyStringBuilder.append("<tr>\n")
                .append("<td>").append(metricGroupDetails).append("</td>\n")
                .append("<td>").append(StatsAggHtmlFramework.htmlEncode(notificationGroup.getEmailAddresses())).append("</td>\n")
                .append("<td>").append(alter).append(", ").append(clone).append(", ").append(test);
            
            if (notificationGroupIdsAssociatedWithAlerts == null) htmlBodyStringBuilder.append(", ").append(remove);
            else if (!notificationGroupIdsAssociatedWithAlerts.contains(notificationGroup.getId())) htmlBodyStringBuilder.append(", ").append(remove);
 
            htmlBodyStringBuilder.append("</td>\n").append("</tr>\n");
        }

        htmlBodyStringBuilder.append(""
                + "</tbody>\n"
                + "<tfoot> \n"
                + "  <tr class=\"statsagg_table_footer\" >\n" 
                + "    <th>Filter</th>\n"
                + "    <th>Filter</th>\n" 
                + "    <th>Filter</th>\n" 
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
    
    public void testNotificationGroup(String notificationGroupName) {
        
        if (notificationGroupName == null) {
            logger.info("Failed to send email alert to notification group. Name cannot be null" );
            return;
        }

        NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
        NotificationGroup notificationGroup = notificationGroupsDao.getNotificationGroupByName(notificationGroupName);
        
        if (notificationGroup == null) {
            logger.info("Failed to send email alert to notification group '" + notificationGroupName + "'. Notification group does not exist." );
            return;
        }
        
        String testAlertName = "Notification test - alert";
        Alert testAlert = new Alert(99999, testAlertName, testAlertName.toUpperCase(),
                "This is a fake alert to test sending email alerts to the notification group named '" + notificationGroup.getName() + "'",
                88888, true, false, false, 300000, 
                Alert.TYPE_THRESHOLD, 77777, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 9900, 1, true, new Timestamp(System.currentTimeMillis()), null,
                Alert.TYPE_THRESHOLD, 77777, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("200"), 91000, 2, true, new Timestamp(System.currentTimeMillis()), null);
        
        String testMetricGroupName = "Notification test - metric group";
        MetricGroup metricGroup = new MetricGroup(88888, testMetricGroupName, testMetricGroupName.toUpperCase(),
                "This is a fake metric group to test sending email alerts to the notification group named '" + notificationGroup.getName() + "'");
        
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
        
        EmailThread emailThread = new EmailThread(testAlert, EmailThread.WARNING_LEVEL_CAUTION, metricKeys, alertMetricValues, 
                false, ApplicationConfiguration.getAlertStatsAggLocation());
        emailThread.buildAlertEmail(3, metricGroup);
        
        List<String> emailsAddresses = EmailThread.getToEmailsAddressesForAlert(notificationGroup.getId());
        
        emailThread.sendEmail(emailsAddresses, emailThread.getSubject(), emailThread.getBody());

        logger.info("Sent test email alert to notification group '" + notificationGroup.getName() + "'");
    }

}
