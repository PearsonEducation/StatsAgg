package com.pearson.statsagg.webui.api;

import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONObject;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
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
@PrepareForTest(Helper.class )
public class CreateAlertTest extends Mockito {
    
    private static final Logger logger = LoggerFactory.getLogger(CreateAlertTest.class.getName());
    
    private static final String mockReturnString = "Successful alert creation.";
    private static final JSONObject alertData = new JSONObject();
    private static com.pearson.statsagg.webui.CreateAlert testCreateAlert = mock(com.pearson.statsagg.webui.CreateAlert.class);
    private static HttpServletRequest request = mock(HttpServletRequest.class);

    @BeforeClass
    public static void setUp() {
        when(testCreateAlert.parseAndAlterAlert(alertData)).thenReturn(mockReturnString);
        PowerMockito.mockStatic(Helper.class);
        PowerMockito.when(Helper.getRequestData(request)).thenReturn(alertData);
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testProcessPostRequest() throws Exception {
        String responseMsg;
        CreateAlert createAlert = new CreateAlert();
        responseMsg = createAlert.processPostRequest(request, testCreateAlert);
        assertEquals(mockReturnString, responseMsg);     
    }
    
}
