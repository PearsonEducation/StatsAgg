/*
 * Copyright 2015 UKUMAP5.
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

import com.pearson.statsagg.database.alerts.AlertsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.StackTrace;
import java.io.IOException;
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
 * @author Prashant Kumar
 */
@WebServlet(name="AlertsList", urlPatterns={"/api/alerts"})
public class Alerts extends HttpServlet {
    int pageNumber = 0;
    int pageSize = 0;
    private static final Logger logger = LoggerFactory.getLogger(Alerts.class.getName());



    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @return 
     */
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("doGet");
        getAlertsJson(request, response);
    }
    
    protected  void getAlertsJson(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("getAlertsJson");
        PrintWriter out = null;
        out = response.getWriter();
        JSONObject errorMsg = new JSONObject();
        try {
        response.setContentType("application/json");
        JSONObject alertsJson = new JSONObject();
        if (request.getParameter(GlobalVariables.pageNumber) != null) {
            pageNumber = Integer.parseInt(request.getParameter(GlobalVariables.pageNumber));
        }
        
        if (request.getParameter(GlobalVariables.pageSize) != null) {
          pageSize = Integer.parseInt(request.getParameter(GlobalVariables.pageSize));
        }
        
        AlertsDao alertsDao = new AlertsDao();
        alertsJson = alertsDao.getAlerts(pageNumber*pageSize, pageSize);
        out.println(alertsJson);
        
        } catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            errorMsg.put(GlobalVariables.error, GlobalVariables.errorMsg);
            out.println(errorMsg);
        }          
    }
}
