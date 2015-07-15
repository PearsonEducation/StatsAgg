/*
 * Copyright 2015 prashant kumar(prashant4nov).
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

import com.pearson.statsagg.database.notifications.NotificationGroup;
import com.pearson.statsagg.utilities.StackTrace;
import com.pearson.statsagg.webui.NotificationGroupsLogic;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author prashant kumar(prashant4nov)
 */
@WebServlet(name = "API_CreateNotificationGroup", urlPatterns = {"/api/create-notification-group"})
public class CreateNotificationGroup extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(CreateNotificationGroup.class.getName());
    
    public static final String PAGE_NAME = "API_CreateNotificationGroup";
    
    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            processPostRequest(request, response);
        } catch (IOException e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
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
    
    protected void processPostRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.debug("create notification request");
        if ((request == null) || (response == null)) {
            return;
        }

        String line = null;
        StringBuilder requestData = new StringBuilder();
        BufferedReader reader = request.getReader();

        while ((line = reader.readLine()) != null) {
            requestData.append(line);
        }

        JSONObject notificationData = new JSONObject();
        notificationData = (JSONObject) JSONValue.parse(requestData.toString());

        JSONObject responseMsg = new JSONObject();
        response.setContentType("application/json");
        PrintWriter out = null;

        try {
            String result = parseAndAlterNotificationGroup(notificationData);
            responseMsg.put("response", result);
            out = response.getWriter();
            out.println(responseMsg);
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
       
    private String parseAndAlterNotificationGroup(JSONObject notificationData) {
        
        if (notificationData == null) {
            return null;
        }
        
        String returnString;
        
        NotificationGroup notificationGroup = getNotificationGroupFromNotificationGroupParameters(notificationData);
        String oldName = null;
        if (notificationData.get("old_name") != null) {
          oldName = (String) notificationData.get("old_name");
        }
        // insert/update/delete records in the database
        if ((notificationGroup != null) && (notificationGroup.getName() != null)) {
            NotificationGroupsLogic notificationGroupsLogic = new NotificationGroupsLogic();
            returnString = notificationGroupsLogic.alterRecordInDatabase(notificationGroup, oldName);
        }
        else {
            returnString = "Failed to add notification group. Reason=\"Field validation failed.\"";
            logger.warn(returnString);
        }
        
        return returnString;
    }

    private NotificationGroup getNotificationGroupFromNotificationGroupParameters(JSONObject notificationData) {
        
        if (notificationData == null) {
            return null;
        }
        
        boolean didEncounterError = false;
        
        NotificationGroup notificationGroup = new NotificationGroup();

        try {
            String parameter;

            parameter = (String) notificationData.get("name");
            String trimmedName = parameter.trim();
            notificationGroup.setName(trimmedName);
            notificationGroup.setUppercaseName(trimmedName.toUpperCase());
            if ((notificationGroup.getName() == null) || notificationGroup.getName().isEmpty()) didEncounterError = true;

            parameter = (String) notificationData.get("email_address");
            if (parameter != null) {
                String trimmedParameter = parameter.trim();
                String emailAddresses;
                if (trimmedParameter.length() > 65535) emailAddresses = trimmedParameter.substring(0, 65534);
                else emailAddresses = trimmedParameter;
                notificationGroup.setEmailAddresses(emailAddresses);
            }
            else notificationGroup.setEmailAddresses("");
        }
        catch (Exception e) {
            didEncounterError = true;
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
            
        if (didEncounterError) {
            notificationGroup = null;
        }
        
        return notificationGroup;
    }   
    
}