package com.pearson.statsagg.webui.api;

import javax.servlet.http.HttpServletRequest;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Prashant Kumar (prashant4nov)
 */
public class NotificationGroupRemoveTest extends Mockito {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupRemoveTest.class.getName());
    
    private static com.pearson.statsagg.webui.NotificationGroups notificationGroup;
    private static final String mockReturnString = "Delete notification group success. NotificationGroupName=\"notification_grp_name\".";
    private static final String notificationGroupName = "notification_grp_name";

    @BeforeClass
    public static void setUp() {
        notificationGroup = mock(com.pearson.statsagg.webui.NotificationGroups.class);
        when(notificationGroup.removeNotificationGroup(notificationGroupName)).thenReturn(mockReturnString);
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testprocessPostRequest() throws Exception {
        String responseMsg;
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("name")).thenReturn(notificationGroupName);
        NotificationGroupRemove removeNotificationGroup = new NotificationGroupRemove();
        responseMsg = removeNotificationGroup.processPostRequest(request, notificationGroup);
        assertEquals(mockReturnString, responseMsg);     
    }
    
}
