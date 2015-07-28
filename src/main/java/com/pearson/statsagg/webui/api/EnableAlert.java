/*
 * Copyright 2015 prashant4nov (Prashant Kumar).
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

import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.utilities.StackTrace;
import com.pearson.statsagg.webui.Common;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author prashant4nov (Prashant Kumar)
 */
@WebServlet(name = "API_Enable_Alert", urlPatterns = {"/api/alert-enable"})
public class EnableAlert extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(AlertsList.class.getName());
    public static final String PAGE_NAME = "API_Enable_Alert";
 
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        processPostRequest(request, response);
  
    }

    protected void processPostRequest(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("Enable/Disable alert request");
        try {
            String returnString = null;
            if ((request == null) || (response == null)) {
                return;
            }
            String alertName = null;
            logger.info(request.getParameter(Helper.name));
            if (request.getParameter(Helper.name) != null) {
                alertName = request.getParameter(Helper.name);
            }
            Boolean isEnabled = Boolean.parseBoolean(request.getParameter("Enabled"));
            com.pearson.statsagg.webui.Alerts alert = new com.pearson.statsagg.webui.Alerts();
            returnString = alert.changeAlertEnabled(alertName, isEnabled);
            JSONObject responseMsg = new JSONObject();
            responseMsg.put("response", returnString);
            response.setContentType("application/json");
            PrintWriter out = null;
            out = response.getWriter();
            out.println(responseMsg);
        } catch (IOException e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
}
