package com.pearson.statsagg.webui.api;

import java.io.PrintWriter;
import java.math.BigDecimal;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database.alerts.Alert;
import com.pearson.statsagg.database.metric_group.MetricGroup;
import com.pearson.statsagg.database.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database.notifications.NotificationGroup;
import com.pearson.statsagg.database.notifications.NotificationGroupsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.StackTrace;
import com.pearson.statsagg.webui.AlertsLogic;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prashant kumar(prashant4nov)
 */
@WebServlet(name = "CreateAlerts", urlPatterns = {"/api/CreateAlert"})
public class CreateAlert extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CreateAlert.class.getName());
    
    public static final String PAGE_NAME = "CreateAlerts";

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
       
    protected void processPostRequest(HttpServletRequest request, HttpServletResponse response) {
        
        if ((request == null) || (response == null)) {
            return;
        }
        
        JSONObject json = new JSONObject();
        response.setContentType("application/json");
        PrintWriter out = null;
        
        try {
            String result = parseAndAlterAlert(request);
            json.put("response", result);
            out = response.getWriter();
            out.println(json);
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

    private String parseAndAlterAlert(HttpServletRequest request) {
        
        if (request == null) {
            return null;
        }
        
        String returnString;
        
        Alert alert = getAlertFromAlertParameters(request);
        String oldName = request.getParameter("Old_Name");

        // insert/update/delete records in the database
        if ((alert != null) && (alert.getName() != null)) {
            AlertsLogic alertsLogic = new AlertsLogic();
            returnString = alertsLogic.alterRecordInDatabase(alert, oldName, false);
            
            if ((GlobalVariables.alertInvokerThread != null) && (AlertsLogic.STATUS_CODE_SUCCESS == alertsLogic.getLastAlterRecordStatus())) {
                GlobalVariables.alertInvokerThread.runAlertThread(false, true);
            }
        }
        else {
            returnString = "Failed to add alert. Reason=\"Field validation failed.\"";
            logger.warn(returnString);
        }
        
        return returnString;
    }
    
    private Alert getAlertFromAlertParameters(HttpServletRequest request) {
        
        if (request == null) {
            return null;
        }
        
        boolean didEncounterError = false;
        
        Alert alert = new Alert();

        try {
            String parameter;

            parameter = request.getParameter("Name");
            String trimmedName = parameter.trim();
            alert.setName(trimmedName);
            alert.setUppercaseName(trimmedName.toUpperCase());
            if ((alert.getName() == null) || alert.getName().isEmpty()) didEncounterError = true;
            
            parameter = request.getParameter("Description");
            if (parameter != null) {
                String trimmedParameter = parameter.trim();
                String description;
                if (trimmedParameter.length() > 100000) description = trimmedParameter.substring(0, 99999);
                else description = trimmedParameter;
                alert.setDescription(description);
            }
            else alert.setDescription("");
                
            parameter = request.getParameter("MetricGroupName");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
                    MetricGroup metricGroup = metricGroupsDao.getMetricGroupByName(parameterTrimmed);
                    if (metricGroup != null) alert.setMetricGroupId(metricGroup.getId());
                }
            }

            parameter = request.getParameter("Enabled");
            if ((parameter != null) && parameter.contains("on")) alert.setIsEnabled(true);
            else alert.setIsEnabled(false);

            parameter = request.getParameter("CautionEnabled");
            if ((parameter != null) && parameter.contains("on")) alert.setIsCautionEnabled(true);
            else alert.setIsCautionEnabled(false);
            
            parameter = request.getParameter("DangerEnabled");
            if ((parameter != null) && parameter.contains("on")) alert.setIsDangerEnabled(true);
            else alert.setIsDangerEnabled(false);
            
            parameter = request.getParameter("CreateAlert_Type");
            if ((parameter != null) && parameter.contains("Availability")) alert.setAlertType(Alert.TYPE_AVAILABILITY);
            else if ((parameter != null) && parameter.contains("Threshold")) alert.setAlertType(Alert.TYPE_THRESHOLD);
            
            parameter = request.getParameter("AlertOnPositive");
            if ((parameter != null) && parameter.contains("on")) alert.setAlertOnPositive(true);
            else alert.setAlertOnPositive(false);

            parameter = request.getParameter("AllowResendAlert");
            if ((parameter != null) && parameter.contains("on")) alert.setAllowResendAlert(true);
            else alert.setAllowResendAlert(false);

            parameter = request.getParameter("SendAlertEveryNumMilliseconds");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {                
                    BigDecimal bigDecimalValueMs = new BigDecimal(parameterTrimmed);
                    BigDecimal bigDecimalValueInSeconds = bigDecimalValueMs.multiply(new BigDecimal(1000));
                    alert.setSendAlertEveryNumMilliseconds(bigDecimalValueInSeconds.intValue());
                }
            }
            
            parameter = request.getParameter("CautionNotificationGroupName");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {
                    NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
                    NotificationGroup notificationGroup = notificationGroupsDao.getNotificationGroupByName(parameterTrimmed);
                    if ((notificationGroup != null) && (notificationGroup.getId() != null)) alert.setCautionNotificationGroupId(notificationGroup.getId());
                }
            }
            
            parameter = request.getParameter("CautionWindowDuration");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    BigDecimal bigDecimalValueMs = new BigDecimal(parameterTrimmed);
                    BigDecimal bigDecimalValueInSeconds = bigDecimalValueMs.multiply(new BigDecimal(1000));
                    alert.setCautionWindowDuration(bigDecimalValueInSeconds.longValue());
                }
            }
            
            parameter = request.getParameter("CautionStopTrackingAfter");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {      
                    BigDecimal bigDecimalValueMs = new BigDecimal(parameterTrimmed);
                    BigDecimal bigDecimalValueInSeconds = bigDecimalValueMs.multiply(new BigDecimal(1000));
                    alert.setCautionStopTrackingAfter(bigDecimalValueInSeconds.longValue());
                }
            }

            parameter = request.getParameter("CautionMinimumSampleCount");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = Integer.parseInt(parameterTrimmed);
                    alert.setCautionMinimumSampleCount(intValue);
                }
            }
            
            parameter = request.getParameter("CautionOperator");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = Alert.getOperatorCodeFromOperatorString(parameterTrimmed);
                    alert.setCautionOperator(intValue);
                }
            }

            parameter = request.getParameter("CautionCombination");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = Alert.getCombinationCodeFromString(parameterTrimmed);
                    alert.setCautionCombination(intValue);                
                }
            }
            
            parameter = request.getParameter("CautionCombinationCount");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {                    
                    Integer intValue = Integer.parseInt(parameterTrimmed);
                    alert.setCautionCombinationCount(intValue);
                }
            }

            parameter = request.getParameter("CautionThreshold");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    BigDecimal bigDecimalValue = new BigDecimal(parameterTrimmed);
                    alert.setCautionThreshold(bigDecimalValue);
                }
            }

            parameter = request.getParameter("DangerNotificationGroupName");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
                    NotificationGroup notificationGroup = notificationGroupsDao.getNotificationGroupByName(parameterTrimmed);
                    if ((notificationGroup != null) && (notificationGroup.getId() != null)) alert.setDangerNotificationGroupId(notificationGroup.getId());
                }
            }
            
            parameter = request.getParameter("DangerWindowDuration");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    BigDecimal bigDecimalValueMs = new BigDecimal(parameterTrimmed);
                    BigDecimal bigDecimalValueInSeconds = bigDecimalValueMs.multiply(new BigDecimal(1000));
                    alert.setDangerWindowDuration(bigDecimalValueInSeconds.longValue());
                }
            }

            parameter = request.getParameter("DangerStopTrackingAfter");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    BigDecimal bigDecimalValueMs = new BigDecimal(parameterTrimmed);
                    BigDecimal bigDecimalValueInSeconds = bigDecimalValueMs.multiply(new BigDecimal(1000));
                    alert.setDangerStopTrackingAfter(bigDecimalValueInSeconds.longValue());
                }
            }

            parameter = request.getParameter("DangerMinimumSampleCount");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    Integer intValue = Integer.parseInt(parameterTrimmed);
                    alert.setDangerMinimumSampleCount(intValue);
                }
            }
            
            parameter = request.getParameter("DangerOperator");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    Integer intValue = Alert.getOperatorCodeFromOperatorString(parameterTrimmed);
                    alert.setDangerOperator(intValue);
                }
            }

            parameter = request.getParameter("DangerCombination");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    Integer intValue = Alert.getCombinationCodeFromString(parameterTrimmed);
                    alert.setDangerCombination(intValue);
                }
            }
            
            parameter = request.getParameter("DangerCombinationCount");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    Integer intValue = Integer.parseInt(parameterTrimmed);
                    alert.setDangerCombinationCount(intValue);
                }
            }

            parameter = request.getParameter("DangerThreshold");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    BigDecimal bigDecimalValue = new BigDecimal(parameterTrimmed);
                    alert.setDangerThreshold(bigDecimalValue);
                }
            }
        }
        catch (Exception e) {
            didEncounterError = true;
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
            
        if (!didEncounterError) {
            alert.setIsCautionAlertActive(false);
            alert.setCautionFirstActiveAt(null);
            alert.setIsCautionAcknowledged(null);
            alert.setCautionAlertLastSentTimestamp(null);
            alert.setCautionActiveAlertsSet(null);
            alert.setIsDangerAlertActive(false);
            alert.setDangerFirstActiveAt(null);
            alert.setIsDangerAcknowledged(null);
            alert.setDangerAlertLastSentTimestamp(null);
            alert.setDangerActiveAlertsSet(null);
        }
        else {
            alert = null;
        }
        
        return alert;
    }
    
}
