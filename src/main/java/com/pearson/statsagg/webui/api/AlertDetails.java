package com.pearson.statsagg.webui.api;

import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.utilities.StackTrace;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
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
        logger.debug("doGet");
        try {    
            JSONObject json = getAlertDetails(request, new AlertsDao());       
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
     * Returns a json object containing the details of the requested alert.
     * 
     * @param request servlet request
     * @param alertsDao AlertsDao object
     * @return details of the requested alert
     */
    protected JSONObject getAlertDetails(HttpServletRequest request, AlertsDao alertsDao) {
        logger.debug("getAlertDetails");
        logger.debug(PAGE_NAME);
        
        JSONObject alertDetails = new JSONObject();
        int alertId = 0;
        
        try {
            if (request.getParameter(Helper.id) != null) {
              alertId = Integer.parseInt(request.getParameter(Helper.id));
            }
            
            Alert alert = alertsDao.getAlert(alertId);
            
            if (alert != null) {
                if (alert.getId() != null) {
                    alertDetails.put("id", alert.getId());
                }
                if (alert.getName() != null) {
                    alertDetails.put("name", alert.getName());
                }
                if (alert.getDescription() != null) {
                    alertDetails.put("description", alert.getDescription());
                }
                if (alert.getAlertType() != null) {
                    alertDetails.put("alert_type", alert.getAlertType());
                }
                if (alert.getMetricGroupId() != null) {
                    alertDetails.put("metricgroup_id", alert.getMetricGroupId());
                }
                if (alert.isEnabled() != null) {
                    alertDetails.put("enabled", alert.isEnabled());
                }
                if (alert.isCautionEnabled() != null) {
                    alertDetails.put("caution_enabled", alert.isCautionEnabled());
                }
                if (alert.isDangerEnabled() != null) {
                    alertDetails.put("danger_enabled", alert.isDangerEnabled());
                }
                if (alert.getCautionNotificationGroupId() != null) {
                    alertDetails.put("caution_notificationgroup_id", alert.getCautionNotificationGroupId());
                }
                if (alert.getCautionNotificationGroupId() != null) {
                    alertDetails.put("caution_positive_notificationgroup_id", alert.getCautionPositiveNotificationGroupId());
                }
                if (alert.isCautionAlertActive() != null) {
                    alertDetails.put("caution_alert_active", alert.isCautionAlertActive());
                }
                if (alert.isDangerAlertActive() != null) {
                    alertDetails.put("danger_alert_active", alert.isDangerAlertActive());
                }
                if (alert.isAlertOnPositive() != null) {
                    alertDetails.put("alert_on_positive", alert.isAlertOnPositive());
                }
                if (alert.isAllowResendAlert() != null) {
                    alertDetails.put("allow_resend_alert", alert.isAllowResendAlert());
                }
                if (alert.getResendAlertEvery() != null) {
                    alertDetails.put("resend_alert_every", alert.getResendAlertEvery());
                }
                if (alert.getCautionPositiveNotificationGroupId() != null) {
                    alertDetails.put("caution_positive_notification_group_id", alert.getCautionPositiveNotificationGroupId());
                }
                if (alert.getCautionOperator() != null) {
                    alertDetails.put("caution_operator", alert.getCautionOperator());
                }
                if (alert.getCautionCombination() != null) {
                    alertDetails.put("caution_combination", alert.getCautionCombination());
                }
                if (alert.getCautionCombinationCount() != null) {
                    alertDetails.put("caution_combination_count", alert.getCautionCombinationCount());
                }
                if (alert.getCautionThreshold() != null) {
                    alertDetails.put("caution_threshold", alert.getCautionThreshold());
                }
                if (alert.getCautionWindowDuration() != null) {
                    alertDetails.put("caution_window_duration", alert.getCautionWindowDuration());
                }
                if (alert.getCautionStopTrackingAfter() != null) {
                    alertDetails.put("caution_stop_tracking_after", alert.getCautionStopTrackingAfter());
                }
                if (alert.getCautionMinimumSampleCount() != null) {
                    alertDetails.put("caution_minimum_sample_count", alert.getCautionMinimumSampleCount());
                }
                if (alert.getCautionAlertLastSentTimestamp() != null) {
                    alertDetails.put("caution_alert_last_sent_timestamp", alert.getCautionAlertLastSentTimestamp());
                }
                if (alert.isCautionAcknowledged() != null) {
                    alertDetails.put("is_caution_acknowledged", alert.isCautionAcknowledged());
                }
                if (alert.getCautionFirstActiveAt() != null) {
                    alertDetails.put("caution_first_active_at", alert.getCautionFirstActiveAt());
                }
                if (alert.getDangerNotificationGroupId() != null) {
                    alertDetails.put("danger_notificationgroup_id", alert.getDangerNotificationGroupId());
                }
                if (alert.getDangerPositiveNotificationGroupId() != null) {
                    alertDetails.put("danger_positive_notification_group_id", alert.getDangerPositiveNotificationGroupId());
                }
                if (alert.getDangerOperator() != null) {
                    alertDetails.put("danger_operator", alert.getDangerOperator());
                }
                if (alert.getDangerCombination() != null) {
                    alertDetails.put("danger_combination", alert.getDangerCombination());
                }
                if (alert.getDangerCombinationCount() != null) {
                    alertDetails.put("danger_combination_count", alert.getDangerCombinationCount());
                }
                if (alert.getDangerThreshold() != null) {
                    alertDetails.put("danger_threshold", alert.getDangerThreshold());
                }
                if (alert.getDangerWindowDuration() != null) {
                    alertDetails.put("danger_window_duration", alert.getDangerWindowDuration());
                }
                if (alert.getDangerStopTrackingAfter() != null) {
                    alertDetails.put("danger_stop_tracking_after", alert.getDangerStopTrackingAfter());
                }
                if (alert.getDangerMinimumSampleCount() != null) {
                    alertDetails.put("danger_minimum_sample_count", alert.getDangerMinimumSampleCount());
                }
                if (alert.getDangerAlertLastSentTimestamp() != null) {
                    alertDetails.put("danger_alert_last_sent_time_stamp", alert.getDangerAlertLastSentTimestamp());
                }
                if (alert.isDangerAcknowledged() != null) {
                    alertDetails.put("is_danger_acknowledged", alert.isDangerAcknowledged());
                }
                if (alert.getDangerFirstActiveAt() != null) {
                    alertDetails.put("danger_first_active_at", alert.getDangerFirstActiveAt());
                }
            }
            else {
                alertDetails.put(Helper.error, Helper.noResult);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            alertDetails.put(Helper.error, Helper.errorMsg);
        }
        
        return alertDetails;
        
    }
}
