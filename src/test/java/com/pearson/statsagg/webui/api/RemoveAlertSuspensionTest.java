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
public class RemoveAlertSuspensionTest extends Mockito {
    
    private static final Logger logger = LoggerFactory.getLogger(RemoveAlertSuspensionTest.class.getName());
    
    private static com.pearson.statsagg.webui.AlertSuspensions alertSuspension;
    private static final String mockReturnString = "Delete alert suspension success. AlertSuspensionName=\"alert_suspension_name\".";
    private static final String alertSuspensionName = "alert_suspension_name";

    @BeforeClass
    public static void setUp() {
        alertSuspension = mock(com.pearson.statsagg.webui.AlertSuspensions.class);
        when(alertSuspension.removeSuspension(alertSuspensionName)).thenReturn(mockReturnString);
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testprocessPostRequest() throws Exception {
        String responseMsg;
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("name")).thenReturn(alertSuspensionName);
        RemoveAlertSuspension removeAlertSuspension = new RemoveAlertSuspension();
        responseMsg = removeAlertSuspension.processPostRequest(request, alertSuspension);
        assertEquals(mockReturnString, responseMsg);     
    }
}
