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
public class RemoveAlertTest extends Mockito {
    
    private static final Logger logger = LoggerFactory.getLogger(RemoveAlertTest.class.getName());
    
    private static com.pearson.statsagg.webui.Alerts alert;
    private static final String mockReturnString = "Delete alert success. AlertName=\"alert_name\".";
    private static final String alertName = "alert_name";

    @BeforeClass
    public static void setUp() {
        alert = mock(com.pearson.statsagg.webui.Alerts.class);
        when(alert.removeAlert(alertName)).thenReturn(mockReturnString);
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testprocessPostRequest() throws Exception {
        String responseMsg;
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("name")).thenReturn(alertName);
        RemoveAlert removeAlert = new RemoveAlert();
        responseMsg = removeAlert.processPostRequest(request, alert);
        assertEquals(mockReturnString, responseMsg);     
    }
    
}
