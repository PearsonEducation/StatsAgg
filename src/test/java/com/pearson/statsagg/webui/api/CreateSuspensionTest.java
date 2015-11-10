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
public class CreateSuspensionTest extends Mockito {
    
    private static final Logger logger = LoggerFactory.getLogger(CreateSuspensionTest.class.getName());
    
    private static final String mockReturnString = "Successful suspension creation.";
    private static final JSONObject suspensionData = new JSONObject();
    private static com.pearson.statsagg.webui.CreateSuspension testCreateSuspension = mock(com.pearson.statsagg.webui.CreateSuspension.class);
    private static HttpServletRequest request = mock(HttpServletRequest.class);

    @BeforeClass
    public static void setUp() {
        when(testCreateSuspension.parseAndAlterSuspension(suspensionData)).thenReturn(mockReturnString);
        PowerMockito.mockStatic(Helper.class);
        PowerMockito.when(Helper.getRequestData(request)).thenReturn(suspensionData);
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testProcessPostRequest() throws Exception {
        String responseMsg;
        CreateSuspension createSuspension = new CreateSuspension();
        responseMsg = createSuspension.processPostRequest(request, testCreateSuspension);
        assertEquals(mockReturnString, responseMsg);     
    }
    
}
