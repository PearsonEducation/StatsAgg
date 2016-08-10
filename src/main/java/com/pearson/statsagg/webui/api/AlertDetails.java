package com.pearson.statsagg.webui.api;

import com.google.gson.JsonObject;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.metric_group.MetricGroup;
import com.pearson.statsagg.database_objects.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database_objects.notifications.NotificationGroup;
import com.pearson.statsagg.database_objects.notifications.NotificationGroupsDao;
import com.pearson.statsagg.utilities.JsonUtils;
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
 * @author Jeffrey Schmidt
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
        
        PrintWriter out = null;
        
        try {
            String json = getAlertDetails(request);
            response.setContentType("application/json");
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

    /**
     * Returns a json string containing the details of the requested alert.
     * 
     * @param request servlet request
     * @return details of the requested alert
     */
    protected String getAlertDetails(HttpServletRequest request) {
        
        if (request == null) {
            return Helper.ERROR_UNKNOWN_JSON;
        }
        
        try {
            Integer alertId = null;
            String alertName = null;

            if (request.getParameter("id") != null) alertId = Integer.parseInt(request.getParameter("id"));
            if (request.getParameter("name") != null) alertName = request.getParameter("name");

            if ((alertId == null) && (alertName == null)) {
                JsonObject jsonObject = Helper.getJsonObjectFromRequestBody(request);
                alertId = JsonUtils.getIntegerFieldFromJsonObject(jsonObject, "id");
                alertName = JsonUtils.getStringFieldFromJsonObject(jsonObject, "name");
            }

            Alert alert = null;
            AlertsDao alertsDao = new AlertsDao(false);
            if (alertId != null) alert = alertsDao.getAlert(alertId);
            else if (alertName != null) alert = alertsDao.getAlertByName(alertName);

            MetricGroup metricGroup = null;
            if ((alert != null) && (alert.getMetricGroupId() != null)) {
                MetricGroupsDao metricGroupsDao = new MetricGroupsDao(alertsDao.getDatabaseInterface());
                metricGroup = metricGroupsDao.getMetricGroup(alert.getMetricGroupId());
            }

            NotificationGroup cautionNotificationGroup = null;
            NotificationGroup cautionPositiveNotificationGroup = null;
            NotificationGroup dangerNotificationGroup = null;
            NotificationGroup dangerPositiveNotificationGroup = null;
            NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao(alertsDao.getDatabaseInterface());
            if ((alert != null) && (alert.getCautionNotificationGroupId() != null)) cautionNotificationGroup = notificationGroupsDao.getNotificationGroup(alert.getCautionNotificationGroupId());
            if ((alert != null) && (alert.getCautionPositiveNotificationGroupId() != null)) cautionPositiveNotificationGroup = notificationGroupsDao.getNotificationGroup(alert.getCautionPositiveNotificationGroupId());
            if ((alert != null) && (alert.getDangerNotificationGroupId() != null)) dangerNotificationGroup = notificationGroupsDao.getNotificationGroup(alert.getDangerNotificationGroupId());
            if ((alert != null) && (alert.getDangerPositiveNotificationGroupId() != null)) dangerPositiveNotificationGroup = notificationGroupsDao.getNotificationGroup(alert.getDangerPositiveNotificationGroupId());
                
            alertsDao.close();
            
            if (alert != null) return Alert.getJsonString_ApiFriendly(alert, metricGroup, cautionNotificationGroup, cautionPositiveNotificationGroup, dangerNotificationGroup, dangerPositiveNotificationGroup);
            else return Helper.ERROR_NOTFOUND_JSON;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));        
        }
        
        return Helper.ERROR_UNKNOWN_JSON;
    }
    
}
