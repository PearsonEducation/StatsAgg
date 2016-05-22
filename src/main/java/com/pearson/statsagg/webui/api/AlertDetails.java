package com.pearson.statsagg.webui.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.utilities.StackTrace;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prashant kumar (prashant4nov)
 */
@WebServlet(name="API_AlertDetails", urlPatterns={"/api/alert-details"})
public class AlertDetails extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertDetails.class.getName());
    
    public static final String PAGE_NAME = "API_AlertDetails";
    
    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return PAGE_NAME;
    }
    
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        processRequest(request, response);
    }
    
    private void processRequest(HttpServletRequest request, HttpServletResponse response) {
        try {
            String json = getAlertDetails(request);
            response.setContentType("application/json");
            PrintWriter out;
            out = response.getWriter();
            out.println(json);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }

    /**
     * Returns a json string containing the details of the requested alert.
     * 
     * @param request servlet request
     * @return details of the requested alert
     */
    protected String getAlertDetails(HttpServletRequest request) {
        
        if (request == null) {
            return Helper.ERROR_JSON;
        }
        
        try {
            Integer alertId = null;
            String alertName = null;

            if (request.getParameter("id") != null) alertId = Integer.parseInt(request.getParameter("id"));
            if (request.getParameter("name") != null) alertName = request.getParameter("name");

            if ((alertId == null) && (alertName == null)) {
                JsonObject jsonObject = Helper.getJsonObjectFromRequetBody(request);
                alertId = Helper.getIntegerFieldFromJsonObject(jsonObject, "id");
                alertName = Helper.getStringFieldFromJsonObject(jsonObject, "name");
            }

            Alert alert = null;
            AlertsDao alertsDao = new AlertsDao();
            if (alertId != null) alert = alertsDao.getAlert(alertId);
            else if (alertName != null) alert = alertsDao.getAlertByName(alertName);
            else alertsDao.close();
            
            if (alert != null) return getApiFriendlyJsonObject(alert);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));        
        }
        
        return Helper.ERROR_JSON;
    }
    
    private String getApiFriendlyJsonObject(Alert alert) {
        
        if (alert == null) {
            return null;
        }

        Gson alert_Gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();   
        JsonElement alert_JsonElement = alert_Gson.toJsonTree(alert);
        JsonObject jsonObject = new Gson().toJsonTree(alert_JsonElement).getAsJsonObject();
        String currentFieldToAlter;
        JsonElement currentField_JsonElement;
                    
        currentFieldToAlter = "alert_type";
        if (alert.getAlertType() == Alert.TYPE_THRESHOLD) {
            jsonObject.remove(currentFieldToAlter);
            jsonObject.addProperty(currentFieldToAlter, "Threshold");
        }
        else if (alert.getAlertType() == Alert.TYPE_AVAILABILITY) {
            jsonObject.remove(currentFieldToAlter);
            jsonObject.addProperty(currentFieldToAlter, "Availability");
        }
        else jsonObject.remove(currentFieldToAlter);        

        Helper.getApiFriendlyJsonObject_CorrectTimesAndTimeUnits(jsonObject, "resend_alert_every", "resend_alert_every_time_unit");

        currentFieldToAlter = "caution_operator";
        currentField_JsonElement = jsonObject.get(currentFieldToAlter);
        if (currentField_JsonElement != null) {
            String operatorString = alert.getOperatorString(Alert.CAUTION, true, false);
            jsonObject.remove(currentFieldToAlter);
            jsonObject.addProperty(currentFieldToAlter, operatorString);
        }
        
        currentFieldToAlter = "caution_combination";
        currentField_JsonElement = jsonObject.get(currentFieldToAlter);
        if (currentField_JsonElement != null) {
            String combinationString = alert.getCombinationString(Alert.CAUTION);
            jsonObject.remove(currentFieldToAlter);
            jsonObject.addProperty(currentFieldToAlter, combinationString);
        }
        
        currentFieldToAlter = "caution_threshold";
        currentField_JsonElement = jsonObject.get(currentFieldToAlter);
        if (currentField_JsonElement != null) {
            jsonObject.remove(currentFieldToAlter);
            JsonBigDecimal jsonBigDecimal = new JsonBigDecimal(alert.getCautionThreshold());
            jsonObject.addProperty(currentFieldToAlter, jsonBigDecimal);
        }
        
        Helper.getApiFriendlyJsonObject_CorrectTimesAndTimeUnits(jsonObject, "caution_window_duration", "caution_window_duration_time_unit");
        Helper.getApiFriendlyJsonObject_CorrectTimesAndTimeUnits(jsonObject, "caution_stop_tracking_after", "caution_stop_tracking_after_time_unit");
        
        currentFieldToAlter = "danger_operator";
        currentField_JsonElement = jsonObject.get(currentFieldToAlter);
        if (currentField_JsonElement != null) {
            String operatorString = alert.getOperatorString(Alert.DANGER, true, false);
            jsonObject.remove(currentFieldToAlter);
            jsonObject.addProperty(currentFieldToAlter, operatorString);
        }
        
        currentFieldToAlter = "danger_combination";
        currentField_JsonElement = jsonObject.get(currentFieldToAlter);
        if (currentField_JsonElement != null) {
            String combinationString = alert.getCombinationString(Alert.DANGER);
            jsonObject.remove(currentFieldToAlter);
            jsonObject.addProperty(currentFieldToAlter, combinationString);
        }
        
        currentFieldToAlter = "danger_threshold";
        currentField_JsonElement = jsonObject.get(currentFieldToAlter);
        if (currentField_JsonElement != null) {
            jsonObject.remove(currentFieldToAlter);
            JsonBigDecimal jsonBigDecimal = new JsonBigDecimal(alert.getDangerThreshold());
            jsonObject.addProperty(currentFieldToAlter, jsonBigDecimal);
        }
        
        Helper.getApiFriendlyJsonObject_CorrectTimesAndTimeUnits(jsonObject, "danger_window_duration", "danger_window_duration_time_unit");
        Helper.getApiFriendlyJsonObject_CorrectTimesAndTimeUnits(jsonObject, "danger_stop_tracking_after", "danger_stop_tracking_after_time_unit");
        
        currentFieldToAlter = "allow_resend_alert";
        currentField_JsonElement = jsonObject.get(currentFieldToAlter);
        if ((currentField_JsonElement == null) || (alert.isAllowResendAlert() == null) || !alert.isAllowResendAlert()) {
            jsonObject.remove("resend_alert_every");
            jsonObject.remove("resend_alert_every_time_unit");
        }
        
        if ((alert.isCautionEnabled() == null) || !alert.isCautionEnabled()) {
            jsonObject.remove("caution_notification_group_id");
            jsonObject.remove("caution_positive_notification_group_id");
            jsonObject.remove("caution_minimum_sample_count");
            jsonObject.remove("caution_combination");
            jsonObject.remove("caution_alert_active");
            jsonObject.remove("caution_window_duration");
            jsonObject.remove("caution_window_duration_time_unit");
            jsonObject.remove("caution_stop_tracking_after");
            jsonObject.remove("caution_stop_tracking_after_time_unit");
            jsonObject.remove("caution_operator");
            jsonObject.remove("caution_threshold");
        }
        
        if ((alert.isDangerEnabled() == null) || !alert.isDangerEnabled()) {
            jsonObject.remove("danger_notification_group_id");
            jsonObject.remove("danger_positive_notification_group_id");
            jsonObject.remove("danger_minimum_sample_count");
            jsonObject.remove("danger_combination");
            jsonObject.remove("danger_alert_active");
            jsonObject.remove("danger_window_duration");
            jsonObject.remove("danger_window_duration_time_unit");
            jsonObject.remove("danger_stop_tracking_after");
            jsonObject.remove("danger_stop_tracking_after_time_unit");
            jsonObject.remove("danger_operator");
            jsonObject.remove("danger_threshold");
        }
        
        if (alert.getAlertType() == Alert.TYPE_AVAILABILITY) {
            jsonObject.remove("caution_minimum_sample_count");
            jsonObject.remove("caution_combination");
            jsonObject.remove("caution_operator");
            jsonObject.remove("caution_threshold");
            
            jsonObject.remove("danger_minimum_sample_count");
            jsonObject.remove("danger_combination");
            jsonObject.remove("danger_operator");
            jsonObject.remove("danger_threshold");
        }
        
        if (alert.getAlertType() == Alert.TYPE_THRESHOLD) {
            jsonObject.remove("caution_stop_tracking_after");
            jsonObject.remove("caution_stop_tracking_after_time_unit");
            
            jsonObject.remove("danger_stop_tracking_after");
            jsonObject.remove("danger_stop_tracking_after_time_unit");
        }        
        
        return alert_Gson.toJson(jsonObject);
    }
    
}
