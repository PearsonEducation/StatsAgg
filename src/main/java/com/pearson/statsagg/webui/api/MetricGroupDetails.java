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

import com.pearson.statsagg.database.metric_group.MetricGroup;
import com.pearson.statsagg.database.metric_group.MetricGroupsDao;
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
 *
 * @author prashant kumar (prashant4nov)
 */
@WebServlet(name="MetricGroupDetails", urlPatterns={"/api/metricgroup"})
public class MetricGroupDetails extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricGroupDetails.class.getName());
    public static final String PAGE_NAME = "MetricGroupDetails";
    
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
            JSONObject json = getMetricGroup(request, new MetricGroupsDao());       
            PrintWriter out = null;
            response.setContentType("application/json");
            out = response.getWriter();
            out.println(json);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }

    private JSONObject getMetricGroup(HttpServletRequest request, MetricGroupsDao metricGroupsDao) {
        logger.debug("getMetricGroup");
        logger.debug(PAGE_NAME);
        JSONObject metricGroupDetails = new JSONObject();
        int metricGroupId = 0;
        try {
            if (request.getParameter(Common.id) != null) {
              metricGroupId = Integer.parseInt(request.getParameter(Common.id));
            }
            MetricGroup metricGroup = metricGroupsDao.getMetricGroup(metricGroupId);
            if (metricGroup != null) {
              if (metricGroup.getId() != null) {
                metricGroupDetails.put("id", metricGroup.getId());
              }
              if (metricGroup.getName() != null) {
                metricGroupDetails.put("name", metricGroup.getName());
              }
              if (metricGroup.getDescription()!= null) {
                metricGroupDetails.put("description", metricGroup.getDescription());
              }
            } else {
                metricGroupDetails.put(Common.error, Common.noResult);
            }
        } catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            metricGroupDetails.put(Common.error, Common.errorMsg);
        }
        return metricGroupDetails;
    }
}
