/*
 * Copyright 2015 prashant4nov.
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
package com.pearson.statsagg.database.notifications;

import com.pearson.statsagg.controller.ContextManager;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.webui.AlertsLogic;
import com.pearson.statsagg.webui.NotificationGroupsLogic;
import java.io.InputStream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author prashant kumar (prashant4nov)
 */
public class NotificationGroupsDaoTest {
    
   // private NotificationGroupsDao notificationGroupsDao; 
    private static final JSONObject mockNotificationGroupsJson = new JSONObject();
    private static final Logger logger = LoggerFactory.getLogger(AlertsLogic.class.getName());
    private final NotificationGroup notificationGroup_ = null;
    private static final String notificationGroupName_ = "JUnit - NotificationGroup for AlertsLogicTest";
    private final NotificationGroupsLogic notificationGroupsLogic_ = new NotificationGroupsLogic();
    
    @BeforeClass
    public static void setUpClass() {
        ContextManager contextManager = new ContextManager();
        InputStream ephemeralDatabaseConfiguration = contextManager.getEphemeralDatabaseConfiguration();
        contextManager.initializeDatabaseFromInputStream(ephemeralDatabaseConfiguration);
        contextManager.createDatabaseSchemas();
         
        JSONArray mockNotificationGroupsList = new JSONArray();
        JSONObject mockNotification = new JSONObject();
        
        mockNotification.put("name", "notificationgroup junit_test name 1");
        mockNotification.put("id", "1");
        mockNotificationGroupsList.add(mockNotification);
        
        mockNotification = new JSONObject();
        
        mockNotification.put("name", "notificationgroup junit_test name 2");
        mockNotification.put("id", "2");
        mockNotificationGroupsList.add(mockNotification);
             
        mockNotificationGroupsJson.put("notificationgroups", mockNotificationGroupsList);
        mockNotificationGroupsJson.put("count", 2);
    }
    
    @AfterClass
    public static void tearDownClass() {
        DatabaseConnections.disconnectAndShutdown();
    }
    
    @Before
    public void setUp() {
        
    }
    
    @After
    public void tearDown() {
        // delete a notification group that was inserted into the database from a previous test. verify that it was deleted.
        String result = notificationGroupsLogic_.deleteRecordInDatabase("notificationgroup junit_test name 1");
        assertTrue(result.contains("success") || result.contains("Notification group not found"));
        
        // delete a notification group that was inserted into the database from a previous test. verify that it was deleted.
        result = notificationGroupsLogic_.deleteRecordInDatabase("notificationgroup junit_test name 2");
        assertTrue(result.contains("success") || result.contains("Notification group not found"));
    }

    @Test
    public void testGetNotificationGroups() {
        // create & insert a NotificationGroup, insert it into the db.
        NotificationGroup notification1 = new NotificationGroup(-1, "notificationgroup junit_test name 1", "notification junit_test email 1");   
        String result = notificationGroupsLogic_.alterRecordInDatabase(notification1);
        assertTrue(result.contains("Success"));
        
        // create & insert a NotificationGroup, insert it into the db.
        NotificationGroup notification2 = new NotificationGroup(-1, "notificationgroup junit_test name 2", "notification junit_test email 2");   
        result = notificationGroupsLogic_.alterRecordInDatabase(notification2);
        assertTrue(result.contains("Success"));
        
        NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
        JSONObject resultNotificationGroups = notificationGroupsDao.getNotificationGroups(0, 10);
        assertEquals(mockNotificationGroupsJson, resultNotificationGroups);
    }
    
    @Test
    public void testGetNotificationGroupsNoNotificationGroups() {
        JSONObject mockNotificationGroupsJsonNoNotification = new JSONObject();
        mockNotificationGroupsJsonNoNotification.put("notificationgroups", new JSONArray());
        mockNotificationGroupsJsonNoNotification.put("count", 0);

        JSONObject resultNotificationGroups = new NotificationGroupsDao().getNotificationGroups(0, 0);
        assertEquals(mockNotificationGroupsJsonNoNotification, resultNotificationGroups);

        resultNotificationGroups = new NotificationGroupsDao().getNotificationGroups(0, 100);
        assertEquals(mockNotificationGroupsJsonNoNotification, resultNotificationGroups);
        
        // create & insert a NotificationGroup, insert it into the db.
        NotificationGroup notification1 = new NotificationGroup(-1, "notificationgroup junit_test name 1", "notification junit_test email 1");   
        String result = notificationGroupsLogic_.alterRecordInDatabase(notification1);
        assertTrue(result.contains("Success"));
        
        // create & insert a NotificationGroup, insert it into the db.
        NotificationGroup notification2 = new NotificationGroup(-1, "notificationgroup junit_test name 2", "notification junit_test email 2");   
        result = notificationGroupsLogic_.alterRecordInDatabase(notification2);
        assertTrue(result.contains("Success"));
              
        resultNotificationGroups = new NotificationGroupsDao().getNotificationGroups(115600, 34);
        assertEquals(mockNotificationGroupsJsonNoNotification, resultNotificationGroups);
    }
}
