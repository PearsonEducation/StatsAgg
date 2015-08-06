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

import com.pearson.statsagg.utilities.StackTrace;
import static com.pearson.statsagg.webui.api.CreateAlertSuspension.PAGE_NAME;
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
 * @author prashant kumar (Prashant4nov)
 */
@WebServlet(name = "API_CreateAlertSuspension", urlPatterns = {"/api/create-alertsuspension"})
public class CreateAlertSuspension extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(CreateAlertSuspension.class.getName());
    
    public static final String PAGE_NAME = "API_CreateAlertSuspension";
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            JSONObject responseMsg = new JSONObject();
            response.setContentType("application/json");
            PrintWriter out = null;
            String result = processPostRequest(request, new com.pearson.statsagg.webui.CreateAlertSuspension());
            responseMsg.put("response", result);
            out = response.getWriter();
            out.println(responseMsg);
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
    
    String processPostRequest(HttpServletRequest request, com.pearson.statsagg.webui.CreateAlertSuspension createAlertSuspension) throws IOException {
        String result = null;
        JSONObject alertSuspensionData = Helper.getRequestData(request);
        JSONObject responseMsg = new JSONObject();
        try {  
            result = createAlertSuspension.parseAndAlterAlertSuspension(alertSuspensionData);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        return result;
    }
}
