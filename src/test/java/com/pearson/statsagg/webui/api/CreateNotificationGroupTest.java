package com.pearson.statsagg.webui.api;

import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONObject;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Prashant Kumar (prashant4nov)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Helper.class)
public class CreateNotificationGroupTest extends Mockito {
    
    private static final Logger logger = LoggerFactory.getLogger(CreateNotificationGroupTest.class.getName());
    
    private static final String mockReturnString = "Successful notification group creation.";
    private static final JSONObject notificationData = new JSONObject();
    private static final com.pearson.statsagg.webui.CreateNotificationGroup testCreateNotificationGroup = mock(com.pearson.statsagg.webui.CreateNotificationGroup.class);
    private static final HttpServletRequest request = mock(HttpServletRequest.class);

    @Before
    public void setUp() {
        when(testCreateNotificationGroup.parseAndAlterNotificationGroup(notificationData)).thenReturn(mockReturnString);
        PowerMockito.mockStatic(Helper.class);
        PowerMockito.when(Helper.getRequestData(request)).thenReturn(notificationData);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testProcessPostRequest() throws Exception {
        String responseMsg;
        CreateNotificationGroup createNotificationGroup = new CreateNotificationGroup();
        responseMsg = createNotificationGroup.processPostRequest(request, testCreateNotificationGroup);
        assertEquals(mockReturnString, responseMsg);     
    }
    
}
