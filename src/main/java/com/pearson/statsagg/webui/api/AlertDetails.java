/*
 * Copyright 2015 prashant kumar (prashant4nov).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pearson.statsagg.webui.api;

import com.pearson.statsagg.database.alerts.Alert;
import com.pearson.statsagg.database.alerts.AlertsDao;
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

    private JSONObject getAlertDetails(HttpServletRequest request, AlertsDao alertsDao) {
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
              if (alert.getDescription()!= null) {
                alertDetails.put("description", alert.getDescription());
              }
              if (alert.getAlertType()!= null) {
                alertDetails.put("alert_type", alert.getAlertType());
              }
              if (alert.getMetricGroupId()!= null) {
                alertDetails.put("metricgroup_id", alert.getMetricGroupId());
              }
              if (alert.isEnabled()!= null) {
                alertDetails.put("enabled", alert.isEnabled());
              }
              if (alert.isCautionEnabled()!= null) {
                alertDetails.put("caution_enabled", alert.isCautionEnabled());
              }
              if (alert.isDangerEnabled()!= null) {
                alertDetails.put("danger_enabled", alert.isDangerEnabled());
              }
              if (alert.getCautionNotificationGroupId()!= null) {
                alertDetails.put("caution_notificationgroup_id", alert.getCautionNotificationGroupId());
              }
              if (alert.isCautionAlertActive()!= null) {
                alertDetails.put("caution_alert_active", alert.isCautionAlertActive());
              }
              if (alert.getDangerNotificationGroupId()!= null) {
                alertDetails.put("danger_notificationgroup_id", alert.getDangerNotificationGroupId());
              }
              if (alert.isDangerAlertActive()!= null) {
                alertDetails.put("danger_alert_active", alert.isDangerAlertActive());
              }
            } else {
                alertDetails.put(Helper.error, Helper.noResult);
            }
        } catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            alertDetails.put(Helper.error, Helper.errorMsg);
        }
        return alertDetails;
    }
}
