/*
 * Copyright 2015 prashant kumar (Prashant4nov).
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

import com.pearson.statsagg.database_objects.alert_suspensions.AlertSuspension;
import com.pearson.statsagg.database_objects.alert_suspensions.AlertSuspensionsDao;
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
@WebServlet(name="API_AlertSuspensionDetails", urlPatterns={"/api/alertsuspension-details"})
public class AlertSuspensionDetails extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertSuspensionDetails.class.getName());
    
    public static final String PAGE_NAME = "API_AlertSuspensionDetails";
    
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
            JSONObject json = getAlertSuspensionDetails(request, new AlertSuspensionsDao());       
            response.setContentType("application/json");
            PrintWriter out;
            out = response.getWriter();
            out.println(json);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }

    private JSONObject getAlertSuspensionDetails(HttpServletRequest request, AlertSuspensionsDao alertSuspensionsDao) {
        logger.debug("getAlertSuspensionDetails");
        logger.debug(PAGE_NAME);
        JSONObject alertSuspensionDetails = new JSONObject();
        int alertSuspensionId = 0;
        try {
            if (request.getParameter(Helper.id) != null) {
              alertSuspensionId = Integer.parseInt(request.getParameter(Helper.id));
            }
            AlertSuspension alertSuspension = alertSuspensionsDao.getAlertSuspension(alertSuspensionId);
            if (alertSuspension != null) {
              if (alertSuspension.getAlertId()!= null) {
                alertSuspensionDetails.put("AlertId", alertSuspension.getAlertId());
              }
              if (alertSuspension.getId()!= null) {
                alertSuspensionDetails.put("Id", alertSuspension.getId());
              }
              if (alertSuspension.getDescription()!= null) {
                alertSuspensionDetails.put("Description", alertSuspension.getDescription());
              }
              if (alertSuspension.getDuration()!= null) {
                alertSuspensionDetails.put("Duration", alertSuspension.getDuration());
              }
              if (alertSuspension.getMetricGroupTagsExclusive()!= null) {
                alertSuspensionDetails.put("MetricGroupTagsExclusive", alertSuspension.getMetricGroupTagsExclusive());
              }
              if (alertSuspension.getMetricGroupTagsInclusive()!= null) {
                alertSuspensionDetails.put("MetricGroupTagsInclusive", alertSuspension.getMetricGroupTagsInclusive());
              }
              if (alertSuspension.getName()!= null) {
                alertSuspensionDetails.put("Name", alertSuspension.getName());
              }
              if (alertSuspension.getStartDate()!= null) {
                alertSuspensionDetails.put("StartDate", alertSuspension.getStartDate());
              }
              if (alertSuspension.getStartTime()!= null) {
                alertSuspensionDetails.put("StartTime", alertSuspension.getStartTime());
              }
              if (alertSuspension.getSuspendBy()!= null) {
                alertSuspensionDetails.put("SuspendBy", alertSuspension.getSuspendBy());
              }
              if (alertSuspension.getDeleteAtTimestamp()!= null) {
                alertSuspensionDetails.put("DeleteAtTimestamp", alertSuspension.getDeleteAtTimestamp());
              }
              if (alertSuspension.getDurationTimeUnit()!= null) {
                alertSuspensionDetails.put("DurationTimeUnit", alertSuspension.getDurationTimeUnit());
              }
            } else {
                alertSuspensionDetails.put(Helper.error, Helper.noResult);
            }
        } catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            alertSuspensionDetails.put(Helper.error, Helper.errorMsg);
        }
        return alertSuspensionDetails;
    }
}
