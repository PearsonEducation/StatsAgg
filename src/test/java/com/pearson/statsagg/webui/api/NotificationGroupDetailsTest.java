package com.pearson.statsagg.webui.api;

import com.pearson.statsagg.database_objects.notifications.NotificationGroup;
import com.pearson.statsagg.database_objects.notifications.NotificationGroupsDao;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONObject;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Prashant Kumar (prashant4nov)
 */
public class NotificationGroupDetailsTest extends Mockito {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupDetailsTest.class.getName());

    private static NotificationGroupsDao notificationGroupsDao;

    @Before
    public void setUp() {
        NotificationGroup mockNotificationGroup = new NotificationGroup(101, "notification_name", "xyz@gmail.com");
        notificationGroupsDao = mock(NotificationGroupsDao.class);
        when(notificationGroupsDao.getNotificationGroup(101)).thenReturn(mockNotificationGroup);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testGetNotificationGroupDetails() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("id")).thenReturn("101");

        NotificationGroupDetails notificationGroupDetails = new NotificationGroupDetails();
        
//        JSONObject result = notificationGroupDetails.getNotificationGroup(request);
//        System.out.println(result);
//        verify(request, atLeast(1)).getParameter("id");
//        assertEquals("xyz@gmail.com", result.get("email_addresses"));
//        assertEquals(101, result.get("id"));
//        assertEquals("notification_name", result.get("name"));
    }
    
}
