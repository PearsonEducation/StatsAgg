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
import java.io.BufferedReader;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prashant kumar(prashant4nov)
 */
@WebServlet(name = "API_CreateAlert", urlPatterns = {"/api/create-alert"})
public class CreateAlert extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CreateAlert.class.getName());
    
    public static final String PAGE_NAME = "API_CreateAlert";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            processPostRequest(request, response);
        } catch (IOException ex) {
            logger.error(ex.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(ex));
        }
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
       
    protected void processPostRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        if ((request == null) || (response == null)) {
            return;
        }
        String line = null;
        StringBuilder requestData = new StringBuilder();
        BufferedReader reader = request.getReader();

        while ((line = reader.readLine()) != null)
          requestData.append(line);
      
        JSONObject alertData = new JSONObject();
        alertData = (JSONObject) JSONValue.parse(requestData.toString());

        JSONObject responseMsg = new JSONObject();
        response.setContentType("application/json");
        PrintWriter out = null;
        
        try {
            String result = parseAndAlterAlert(alertData);
            responseMsg.put("response", result);
            out = response.getWriter();
            out.println(responseMsg);
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

    private String parseAndAlterAlert(JSONObject alertData) {
        
        if (alertData == null) {
            return null;
        }
        
        String returnString;
        
        Alert alert = getAlertFromAlertParameters(alertData);
        String oldName = (String) alertData.get("Old_Name");

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
    
    private Alert getAlertFromAlertParameters(JSONObject alertData) {
        
        if (alertData == null) {
            return null;
        }
        
        boolean didEncounterError = false;
        
        Alert alert = new Alert();

        try {
            String parameter;

            parameter = (String) alertData.get("Name");
            String trimmedName = parameter.trim();
            alert.setName(trimmedName);
            alert.setUppercaseName(trimmedName.toUpperCase());
            if ((alert.getName() == null) || alert.getName().isEmpty()) didEncounterError = true;
            
            parameter = (String) alertData.get("Description");
            if (parameter != null) {
                String trimmedParameter = parameter.trim();
                String description;
                if (trimmedParameter.length() > 100000) description = trimmedParameter.substring(0, 99999);
                else description = trimmedParameter;
                alert.setDescription(description);
            }
            else alert.setDescription("");
                
            parameter = (String) alertData.get("MetricGroupName");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
                    MetricGroup metricGroup = metricGroupsDao.getMetricGroupByName(parameterTrimmed);
                    if (metricGroup != null) alert.setMetricGroupId(metricGroup.getId());
                }
            }

            parameter = (String) alertData.get("Enabled");
            if ((parameter != null) && parameter.contains("on")) alert.setIsEnabled(true);
            else alert.setIsEnabled(false);

            parameter = (String) alertData.get("CautionEnabled");
            if ((parameter != null) && parameter.contains("on")) alert.setIsCautionEnabled(true);
            else alert.setIsCautionEnabled(false);
            
            parameter = (String) alertData.get("DangerEnabled");
            if ((parameter != null) && parameter.contains("on")) alert.setIsDangerEnabled(true);
            else alert.setIsDangerEnabled(false);
            
            parameter = (String) alertData.get("CreateAlert_Type");
            if ((parameter != null) && parameter.contains("Availability")) alert.setAlertType(Alert.TYPE_AVAILABILITY);
            else if ((parameter != null) && parameter.contains("Threshold")) alert.setAlertType(Alert.TYPE_THRESHOLD);
            
            parameter = (String) alertData.get("AlertOnPositive");
            if ((parameter != null) && parameter.contains("on")) alert.setAlertOnPositive(true);
            else alert.setAlertOnPositive(false);

            parameter = (String) alertData.get("AllowResendAlert");
            if ((parameter != null) && parameter.contains("on")) alert.setAllowResendAlert(true);
            else alert.setAllowResendAlert(false);

            parameter = (String) alertData.get("SendAlertEveryNumMilliseconds");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {                
                    BigDecimal bigDecimalValueMs = new BigDecimal(parameterTrimmed);
                    BigDecimal bigDecimalValueInSeconds = bigDecimalValueMs.multiply(new BigDecimal(1000));
                    alert.setSendAlertEveryNumMilliseconds(bigDecimalValueInSeconds.intValue());
                }
            }
            
            parameter = (String) alertData.get("CautionNotificationGroupName");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {
                    NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
                    NotificationGroup notificationGroup = notificationGroupsDao.getNotificationGroupByName(parameterTrimmed);
                    if ((notificationGroup != null) && (notificationGroup.getId() != null)) alert.setCautionNotificationGroupId(notificationGroup.getId());
                }
            }
            
            parameter = (String) alertData.get("CautionWindowDuration");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    BigDecimal bigDecimalValueMs = new BigDecimal(parameterTrimmed);
                    BigDecimal bigDecimalValueInSeconds = bigDecimalValueMs.multiply(new BigDecimal(1000));
                    alert.setCautionWindowDuration(bigDecimalValueInSeconds.longValue());
                }
            }
            
            parameter = (String) alertData.get("CautionStopTrackingAfter");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {      
                    BigDecimal bigDecimalValueMs = new BigDecimal(parameterTrimmed);
                    BigDecimal bigDecimalValueInSeconds = bigDecimalValueMs.multiply(new BigDecimal(1000));
                    alert.setCautionStopTrackingAfter(bigDecimalValueInSeconds.longValue());
                }
            }

            parameter = (String) alertData.get("CautionMinimumSampleCount");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = Integer.parseInt(parameterTrimmed);
                    alert.setCautionMinimumSampleCount(intValue);
                }
            }
            
            parameter = (String) alertData.get("CautionOperator");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = Alert.getOperatorCodeFromOperatorString(parameterTrimmed);
                    alert.setCautionOperator(intValue);
                }
            }

            parameter = (String) alertData.get("CautionCombination");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = Alert.getCombinationCodeFromString(parameterTrimmed);
                    alert.setCautionCombination(intValue);                
                }
            }
            
            parameter = (String) alertData.get("CautionCombinationCount");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {                    
                    Integer intValue = Integer.parseInt(parameterTrimmed);
                    alert.setCautionCombinationCount(intValue);
                }
            }

            parameter = (String) alertData.get("CautionThreshold");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    BigDecimal bigDecimalValue = new BigDecimal(parameterTrimmed);
                    alert.setCautionThreshold(bigDecimalValue);
                }
            }

            parameter = (String) alertData.get("DangerNotificationGroupName");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
                    NotificationGroup notificationGroup = notificationGroupsDao.getNotificationGroupByName(parameterTrimmed);
                    if ((notificationGroup != null) && (notificationGroup.getId() != null)) alert.setDangerNotificationGroupId(notificationGroup.getId());
                }
            }
            
            parameter = (String) alertData.get("DangerWindowDuration");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    BigDecimal bigDecimalValueMs = new BigDecimal(parameterTrimmed);
                    BigDecimal bigDecimalValueInSeconds = bigDecimalValueMs.multiply(new BigDecimal(1000));
                    alert.setDangerWindowDuration(bigDecimalValueInSeconds.longValue());
                }
            }

            parameter = (String) alertData.get("DangerStopTrackingAfter");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    BigDecimal bigDecimalValueMs = new BigDecimal(parameterTrimmed);
                    BigDecimal bigDecimalValueInSeconds = bigDecimalValueMs.multiply(new BigDecimal(1000));
                    alert.setDangerStopTrackingAfter(bigDecimalValueInSeconds.longValue());
                }
            }

            parameter = (String) alertData.get("DangerMinimumSampleCount");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    Integer intValue = Integer.parseInt(parameterTrimmed);
                    alert.setDangerMinimumSampleCount(intValue);
                }
            }
            
            parameter = (String) alertData.get("DangerOperator");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    Integer intValue = Alert.getOperatorCodeFromOperatorString(parameterTrimmed);
                    alert.setDangerOperator(intValue);
                }
            }

            parameter = (String) alertData.get("DangerCombination");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    Integer intValue = Alert.getCombinationCodeFromString(parameterTrimmed);
                    alert.setDangerCombination(intValue);
                }
            }
            
            parameter = (String) alertData.get("DangerCombinationCount");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    Integer intValue = Integer.parseInt(parameterTrimmed);
                    alert.setDangerCombinationCount(intValue);
                }
            }

            parameter = (String) alertData.get("DangerThreshold");
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