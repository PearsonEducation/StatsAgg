package com.pearson.statsagg.webui;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.alerts.EmailThread;
import com.pearson.statsagg.database.alerts.Alert;
import com.pearson.statsagg.database.metric_group.MetricGroup;
import com.pearson.statsagg.database.metric_group.MetricGroupsDao;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.utilities.StackTrace;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "AlertPreview", urlPatterns = {"/AlertPreview"})
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
        
        response.setContentType("text/html");
        PrintWriter out = null;
    
        Alert alert = getAlertFromAlertParameters(request);
        String alertBody = getExampleEmailAlert(request.getParameter("WarningLevel"), alert, request.getParameter("MetricGroupName"));        
        
        try {  
            StringBuilder htmlBuilder = new StringBuilder("");

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            
            String htmlBody =
            "<body>" +
            "  <div id=\"page-content-wrapper\">\n" +
            "    <!-- Keep all page content within the page-content inset div! -->\n" +
            "    <div class=\"page-content inset\">\n" +
                 alertBody +
            "    </div>\n" +
            "  </div>\n" +
            "</body>";
            
            htmlBuilder.append("<!DOCTYPE html>\n<html>\n").append(htmlHeader).append(htmlBody).append("</html>");
            
            Document htmlDocument = Jsoup.parse(htmlBuilder.toString());
            String htmlFormatted  = htmlDocument.toString();
            out = response.getWriter();
            out.println(htmlFormatted);
//            if (ApplicationConfiguration.isDebugModeEnabled()) out.println(htmlBuilder.toString());
//            else out.println(htmlFormatted);
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

    private Alert getAlertFromAlertParameters(HttpServletRequest request) {
        
        if (request == null) {
            return null;
        }
                
        Alert alert = new Alert();

        try {
            String parameter;

            parameter = request.getParameter("Name");
            if (parameter != null) alert.setName(parameter.trim());
            else alert.setName("");
            
            parameter = request.getParameter("Description");
            if (parameter != null) alert.setDescription(parameter.trim());
            else alert.setDescription("");

            parameter = request.getParameter("CreateAlertCaution_Type");
            if ((parameter != null) && parameter.contains("Availability")) alert.setCautionAlertType(Alert.TYPE_AVAILABILITY);

            parameter = request.getParameter("CreateAlertCaution_Type");
            if ((parameter != null) && parameter.contains("Threshold")) alert.setCautionAlertType(Alert.TYPE_THRESHOLD);
            
            parameter = request.getParameter("CreateAlertCaution_Type");
            if ((parameter != null) && parameter.contains("Disabled")) alert.setCautionAlertType(Alert.TYPE_DISABLED);
            
            parameter = request.getParameter("CautionWindowDuration");
            if ((parameter != null) && !parameter.isEmpty()) {
                BigDecimal bigDecimalValueMs = new BigDecimal(parameter.trim());
                BigDecimal bigDecimalValueInSeconds = bigDecimalValueMs.multiply(new BigDecimal(1000));
                alert.setCautionWindowDuration(bigDecimalValueInSeconds.intValue());
            }

            parameter = request.getParameter("CautionMinimumSampleCount");
            if ((parameter != null) && !parameter.isEmpty()) {
                Integer intValue = Integer.parseInt(parameter.trim());
                alert.setCautionMinimumSampleCount(intValue);
            }
            
            parameter = request.getParameter("CautionOperator");
            if ((parameter != null) && !parameter.isEmpty()) {
                Integer intValue = Alert.getOperatorCodeFromOperatorString(parameter.trim());
                alert.setCautionOperator(intValue);
            }

            parameter = request.getParameter("CautionCombination");
            if ((parameter != null) && !parameter.isEmpty()) {
                Integer intValue = Alert.getCombinationCodeFromString(parameter.trim());
                alert.setCautionCombination(intValue);
            }
            
            parameter = request.getParameter("CautionCombinationCount");
            if ((parameter != null) && !parameter.isEmpty()) {
                Integer intValue = Integer.parseInt(parameter.trim());
                alert.setCautionCombinationCount(intValue);
            }

            parameter = request.getParameter("CautionThreshold");
            if ((parameter != null) && !parameter.isEmpty()) {
                BigDecimal bigDecimalValue = new BigDecimal(parameter.trim());
                alert.setCautionThreshold(bigDecimalValue);
            }
            
            parameter = request.getParameter("CreateAlertDanger_Type");
            if ((parameter != null) && parameter.contains("Availability")) alert.setDangerAlertType(Alert.TYPE_AVAILABILITY);

            parameter = request.getParameter("CreateAlertDanger_Type");
            if ((parameter != null) && parameter.contains("Threshold")) alert.setDangerAlertType(Alert.TYPE_THRESHOLD);

            parameter = request.getParameter("CreateAlertDanger_Type");
            if ((parameter != null) && parameter.contains("Disabled")) alert.setDangerAlertType(Alert.TYPE_DISABLED);
            
            parameter = request.getParameter("DangerWindowDuration");
            if ((parameter != null) && !parameter.isEmpty()) {
                BigDecimal bigDecimalValueMs = new BigDecimal(parameter.trim());
                BigDecimal bigDecimalValueInSeconds = bigDecimalValueMs.multiply(new BigDecimal(1000));
                alert.setDangerWindowDuration(bigDecimalValueInSeconds.intValue());
            }

            parameter = request.getParameter("DangerMinimumSampleCount");
            if ((parameter != null) && !parameter.isEmpty()) {
                Integer intValue = Integer.parseInt(parameter.trim());
                alert.setDangerMinimumSampleCount(intValue);
            }
            
            parameter = request.getParameter("DangerOperator");
            if ((parameter != null) && !parameter.isEmpty()) {
                Integer intValue = Alert.getOperatorCodeFromOperatorString(parameter.trim());
                alert.setDangerOperator(intValue);
            }

            parameter = request.getParameter("DangerCombination");
            if ((parameter != null) && !parameter.isEmpty()) {
                Integer intValue = Alert.getCombinationCodeFromString(parameter.trim());
                alert.setDangerCombination(intValue);
            }
            
            parameter = request.getParameter("DangerCombinationCount");
            if ((parameter != null) && !parameter.isEmpty()) {
                Integer intValue = Integer.parseInt(parameter.trim());
                alert.setDangerCombinationCount(intValue);
            }

            parameter = request.getParameter("DangerThreshold");
            if ((parameter != null) && !parameter.isEmpty()) {
                BigDecimal bigDecimalValue = new BigDecimal(parameter.trim());
                alert.setDangerThreshold(bigDecimalValue);
            }
        }
        catch (Exception e) {
            alert = null;
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
            
        return alert;
    }
    
    /*
    For this method to return a valid result, warningLevel must be equalsIgnoreCase to either 'caution' or 'danger'
    */
    private String getExampleEmailAlert(String warningLevel, Alert alert, String metricGroupName) {
        
        if ((alert == null) || (warningLevel == null) || (!warningLevel.equalsIgnoreCase("caution") && !warningLevel.equalsIgnoreCase("danger"))) {
            return "Unable to preview alert";
        }
       
        if (metricGroupName == null) metricGroupName = "";

        alert.setId(99999);
        alert.setMetricGroupId(77777);
        alert.setCautionNotificationGroupId(77777);
        alert.setDangerNotificationGroupId(77777);
        
        MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
        MetricGroup metricGroup = metricGroupsDao.getMetricGroupByName(metricGroupName);
        if (metricGroup != null) metricGroup = new MetricGroup(88888, metricGroup.getName(), metricGroup.getUppercaseName(), metricGroup.getDescription());
        else metricGroup = new MetricGroup(88888, metricGroupName, metricGroupName.toUpperCase(), "");

        List<String> metricKeys = new ArrayList<>();
        metricKeys.add("preview.metric1");
        metricKeys.add("preview.metric2");
        metricKeys.add("preview.metric3");
        
        Map<String,BigDecimal> alertMetricValues = generateFakeMetricValues(warningLevel, alert);
        
        if (warningLevel.equalsIgnoreCase("caution")) {
            boolean isAlertValid = alert.isCautionAlertCriteriaValid();
            
            if (isAlertValid) {
                EmailThread emailThread = new EmailThread(alert, EmailThread.WARNING_LEVEL_CAUTION, metricKeys, alertMetricValues, false, ApplicationConfiguration.getAlertStatsAggLocation());
                emailThread.buildAlertEmail(2, metricGroup);
                return emailThread.getBody();
            }
            else {
                return "This alert's caution criteria is not valid";
            }
        }
        
        if (warningLevel.equalsIgnoreCase("danger")) {
            boolean isAlertValid = alert.isDangerAlertCriteriaValid();
            
            if (isAlertValid) {
                EmailThread emailThread = new EmailThread(alert, EmailThread.WARNING_LEVEL_DANGER, metricKeys, alertMetricValues, false, ApplicationConfiguration.getAlertStatsAggLocation());
                emailThread.buildAlertEmail(2, metricGroup);
                return emailThread.getBody();
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
        
        if (warningLevel.equalsIgnoreCase("caution") && (alert.getCautionOperator() != null) && (alert.getCautionThreshold() != null)) {
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
            
        if (warningLevel.equalsIgnoreCase("danger") && (alert.getDangerOperator() != null) && (alert.getDangerThreshold() != null)) {
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
