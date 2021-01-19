package com.pearson.statsagg.web_ui;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.threads.alert_related.NotificationThread;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroup;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupsDao;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupTag;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupTagsDao;
import com.pearson.statsagg.configuration.ApplicationConfiguration;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class AlertPreview extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AlertPreview.class.getName());
    
    public static final String PAGE_NAME = "Alert Preview";
    
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
    
        Alert alert = CreateAlert.getAlertFromAlertParameters(request);
        String alertBody = getExampleEmailAlert(request.getParameter("WarningLevel"), alert, request.getParameter("MetricGroupName"));        
        
        try {  
            StringBuilder htmlBuilder = new StringBuilder();

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            
            String htmlBody =
            "<body>" +
            "  <div id=\"page-content-wrapper\">\n" +
            "    <!-- Keep all page content within the page-content inset div! -->\n" +
            "    <div class=\"page-content inset statsagg_page_content_font\">\n" +
            "      <div class=\"statsagg_force_word_wrap\">" +
            alertBody +
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
    
    /*
    For this method to return a valid result, warningLevel must be equalsIgnoreCase to either 'caution' or 'danger'
    */
    private String getExampleEmailAlert(String warningLevel, Alert alert, String metricGroupName) {
        
        if ((alert == null) || (warningLevel == null) || (!warningLevel.equalsIgnoreCase("caution") && !warningLevel.equalsIgnoreCase("danger"))) {
            return "Unable to preview alert";
        }
       
        if (metricGroupName == null) metricGroupName = "";

        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.add(Calendar.HOUR, -50);
        currentCalendar.add(Calendar.MINUTE, -25);
        currentCalendar.add(Calendar.SECOND, -31);
        
        alert.setId(99999);
        alert.setMetricGroupId(77777);
        alert.setCautionNotificationGroupId(77777);
        alert.setDangerNotificationGroupId(77777);
        alert.setCautionFirstActiveAt(new Timestamp(currentCalendar.getTimeInMillis()));
        alert.setDangerFirstActiveAt(new Timestamp(currentCalendar.getTimeInMillis()));

        MetricGroup metricGroup = MetricGroupsDao.getMetricGroup(DatabaseConnections.getConnection(), true, metricGroupName);
        List<MetricGroupTag> metricGroupTags = new ArrayList<>();
        if (metricGroup != null) metricGroupTags = MetricGroupTagsDao.getMetricGroupTagsByMetricGroupId(DatabaseConnections.getConnection(), true, metricGroup.getId());
        else metricGroup = new MetricGroup(88888, metricGroupName, metricGroupName.toUpperCase(), "", null, null, null, null, null);

        List<String> metricKeys = new ArrayList<>();
        metricKeys.add("preview.metric1");
        metricKeys.add("preview.metric2");
        metricKeys.add("preview.metric3");
        
        Map<String,BigDecimal> alertMetricValues = generateFakeMetricValues(warningLevel, alert);
        
        if (warningLevel.equalsIgnoreCase("caution")) {
            boolean isAlertValidAndEnabled = alert.isCautionAlertCriteriaValid().isValid() && (alert.isCautionEnabled() != null) && alert.isCautionEnabled();
            
            if (isAlertValidAndEnabled) {
                NotificationThread notificationThread = new NotificationThread(alert, Alert.CAUTION, metricKeys, alertMetricValues, new ConcurrentHashMap<>(),
                        false, true, ApplicationConfiguration.getAlertStatsAggLocation());
                notificationThread.buildAlertEmail(2, metricGroup, metricGroupTags);
                return notificationThread.getEmailBody();
            }
            else {
                return "This alert's caution criteria is not valid";
            }
        }
        
        if (warningLevel.equalsIgnoreCase("danger")) {
            boolean isAlertValidAndEnabled = alert.isDangerAlertCriteriaValid().isValid() && (alert.isDangerEnabled() != null) && alert.isDangerEnabled();
            
            if (isAlertValidAndEnabled) {
                NotificationThread emailThread = new NotificationThread(alert, Alert.DANGER, metricKeys, alertMetricValues, new ConcurrentHashMap<>(),
                        false, true, ApplicationConfiguration.getAlertStatsAggLocation());
                emailThread.buildAlertEmail(2, metricGroup, metricGroupTags);
                return emailThread.getEmailBody();
            }
            else {
                return "This alert's danger criteria is not valid";
            }
        }

        return "Unable to preview alert";
    }
    
    private Map generateFakeMetricValues(String warningLevel, Alert alert) {
        
        if ((warningLevel == null) || (alert == null)) {
            return new HashMap<>();
        }
        
        Map<String,BigDecimal> alertMetricValues = new HashMap<>();
        
        if (warningLevel.equalsIgnoreCase("caution") && (alert.getCautionOperator() != null) && (alert.getCautionThreshold() != null) &&
                 (alert.getAlertType() != null) && (alert.getAlertType() == Alert.TYPE_THRESHOLD)) {
            if ((Objects.equals(alert.getCautionOperator(), Alert.OPERATOR_GREATER_EQUALS)) || (Objects.equals(alert.getCautionOperator(), Alert.OPERATOR_GREATER))) {
                alertMetricValues.put("preview.metric1" + "-" + alert.getId(), alert.getCautionThreshold().add(BigDecimal.ONE));
                alertMetricValues.put("preview.metric2" + "-" + alert.getId(), alert.getCautionThreshold().add(BigDecimal.TEN));
            }
            
            if ((Objects.equals(alert.getCautionOperator(), Alert.OPERATOR_LESS_EQUALS)) || (Objects.equals(alert.getCautionOperator(), Alert.OPERATOR_LESS))) {
                alertMetricValues.put("preview.metric1" + "-" + alert.getId(), alert.getCautionThreshold().subtract(BigDecimal.ONE));
                alertMetricValues.put("preview.metric2" + "-" + alert.getId(), alert.getCautionThreshold().subtract(BigDecimal.TEN));
            }
            
            if (Objects.equals(alert.getCautionOperator(), Alert.OPERATOR_EQUALS)) {
                alertMetricValues.put("preview.metric1" + "-" + alert.getId(), alert.getCautionThreshold());
                alertMetricValues.put("preview.metric2" + "-" + alert.getId(), alert.getCautionThreshold());
            }
        } 
            
        if (warningLevel.equalsIgnoreCase("danger") && (alert.getDangerOperator() != null) && (alert.getDangerThreshold() != null) && 
                (alert.getAlertType() != null) && (alert.getAlertType() == Alert.TYPE_THRESHOLD)) {
            if ((Objects.equals(alert.getDangerOperator(), Alert.OPERATOR_GREATER_EQUALS)) || (Objects.equals(alert.getDangerOperator(), Alert.OPERATOR_GREATER))) {
                alertMetricValues.put("preview.metric1" + "-" + alert.getId(), alert.getDangerThreshold().add(BigDecimal.ONE));
                alertMetricValues.put("preview.metric2" + "-" + alert.getId(), alert.getDangerThreshold().add(BigDecimal.TEN));
            }
            
            if ((Objects.equals(alert.getDangerOperator(), Alert.OPERATOR_LESS_EQUALS)) || (Objects.equals(alert.getDangerOperator(), Alert.OPERATOR_LESS))) {
                alertMetricValues.put("preview.metric1" + "-" + alert.getId(), alert.getDangerThreshold().subtract(BigDecimal.ONE));
                alertMetricValues.put("preview.metric2" + "-" + alert.getId(), alert.getDangerThreshold().subtract(BigDecimal.TEN));
            }
            
            if (Objects.equals(alert.getDangerOperator(), Alert.OPERATOR_EQUALS)) {
                alertMetricValues.put("preview.metric1" + "-" + alert.getId(), alert.getDangerThreshold());
                alertMetricValues.put("preview.metric2" + "-" + alert.getId(), alert.getDangerThreshold());
            }
        } 
        
        return alertMetricValues;
    }
   
}
