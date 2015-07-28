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

import com.pearson.statsagg.database_objects.notifications.NotificationGroupsDao;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.webui.NotificationGroupsLogic;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author prashant kumar (prashant4nov)
 */
public class NotificationGroupsTest extends Mockito{
    private static final JSONObject mockNotificationGroupsJson = new JSONObject();
    private static NotificationGroupsDao notificationGroupsDao;
    private static NotificationGroupsLogic notificationGroupsLogic_ = new NotificationGroupsLogic();
    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupsList.class.getName());

    @BeforeClass
    public static void setUp() {
        JSONArray mockNotificationGroupsList = new JSONArray();
        JSONObject mockNotification = new JSONObject();
        
        mockNotification.put("name", "abcd");
        mockNotification.put("id", "1");
        mockNotificationGroupsList.add(mockNotification);
        
        mockNotification = new JSONObject();
        mockNotification.put("name", "xyz");
        mockNotification.put("id", "2");
        mockNotificationGroupsList.add(mockNotification);
             
        mockNotificationGroupsJson.put("metricgroups", mockNotificationGroupsList);
        mockNotificationGroupsJson.put("count", 2);
        notificationGroupsDao = mock(NotificationGroupsDao.class);
        when(notificationGroupsDao.getNotificationGroups(10, 2)).thenReturn(mockNotificationGroupsJson);
    }
    
    @After
    public void tearDown() {
    }
   
    @AfterClass
    public static void tearDownClass() {

        String result = notificationGroupsLogic_.deleteRecordInDatabase("abcd");
        assertTrue(result.contains("success") || result.contains("Notification group not found"));
        result = notificationGroupsLogic_.deleteRecordInDatabase("xyz");
        assertTrue(result.contains("success") || result.contains("Notification group not found"));

        DatabaseConnections.disconnectAndShutdown();
    }

    @Test
    public void testGetNotificationGroupsJson() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("page_size")).thenReturn("2");
        when(request.getParameter("page_number")).thenReturn("5");

        NotificationGroupsList notificationGroups = new NotificationGroupsList();
        JSONObject result = notificationGroups.getNotificationGroups(request, notificationGroupsDao);

        verify(request, atLeast(1)).getParameter("page_size");
        verify(request, atLeast(1)).getParameter("page_number");
        assertEquals(mockNotificationGroupsJson, result);
    }   
}
