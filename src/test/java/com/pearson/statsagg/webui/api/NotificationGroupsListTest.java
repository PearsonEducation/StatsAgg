package com.pearson.statsagg.webui.api;

import com.pearson.statsagg.database_objects.notifications.NotificationGroupsDao;
import com.pearson.statsagg.database_engine.DatabaseConnections;
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
 * @author prashant kumar (prashant4nov)
 */
public class NotificationGroupsListTest extends Mockito{
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupsListTest.class.getName());

    private static final JSONObject mockNotificationGroupsJson = new JSONObject();
    private static NotificationGroupsDao notificationGroupsDao;
    private static NotificationGroupsLogic notificationGroupsLogic_ = new NotificationGroupsLogic();

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
        JSONObject result = notificationGroups.getNotificationGroupsList(request, notificationGroupsDao);

        verify(request, atLeast(1)).getParameter("page_size");
        verify(request, atLeast(1)).getParameter("page_number");
        assertEquals(mockNotificationGroupsJson, result);
    }   
    
}
