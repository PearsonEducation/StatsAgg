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
 *
 * @author prashant kumar (Prashant4nov)
 */
@WebServlet(name="API_AlertSuspensionList", urlPatterns={"/api/AlertsSuspension-list"})
public class AlertSuspensionList extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AlertsList.class.getName());
    public static final String PAGE_NAME = "API_AlertSuspensionList";
    
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
        logger.info("doGet");
        try {    
            JSONObject json = getAlertSuspensionJson(request, new AlertSuspensionsDao());       
            PrintWriter out = null;
            response.setContentType("application/json");
            out = response.getWriter();
            out.println(json);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }     
    }
    
    public JSONObject getAlertSuspensionJson(HttpServletRequest request, AlertSuspensionsDao alertSuspensionsDao) {
        logger.info("getAlertSuspensionJson");
        JSONObject errorMsg = null;
        JSONObject alertSuspensionJson = null;
        int pageNumber = 0, pageSize = 0;
        
        try {
            if (request.getParameter(Helper.pageNumber) != null) {
                pageNumber = Integer.parseInt(request.getParameter(Helper.pageNumber));
            }

            if (request.getParameter(Helper.pageSize) != null) {
                pageSize = Integer.parseInt(request.getParameter(Helper.pageSize));
            }
            
            alertSuspensionJson = alertSuspensionsDao.getAlertSuspension(pageNumber*pageSize, pageSize);
        } catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            errorMsg = new JSONObject();
            errorMsg.put(Helper.error, Helper.errorMsg);
        }
        if (alertSuspensionJson != null) return alertSuspensionJson;
        else if (errorMsg != null) return errorMsg;
        else return null;
    }
}
