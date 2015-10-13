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
@PrepareForTest(Helper.class )
public class CreateMetricGroupTest extends Mockito {
    
    private static final Logger logger = LoggerFactory.getLogger(CreateMetricGroupTest.class.getName());
    
    private static final String mockReturnString = "Successful metric group creation.";
    private static final JSONObject metricData = new JSONObject();
    private static com.pearson.statsagg.webui.CreateMetricGroup testCreateMetricGroup = mock(com.pearson.statsagg.webui.CreateMetricGroup.class);
    private static HttpServletRequest request = mock(HttpServletRequest.class);

    @Before
    public void setUp() {
        when(testCreateMetricGroup.parseMetricGroup(metricData)).thenReturn(mockReturnString);
        PowerMockito.mockStatic(Helper.class);
        PowerMockito.when(Helper.getRequestData(request)).thenReturn(metricData);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testProcessPostRequest() throws Exception {
        String responseMsg;
        CreateMetricGroup createMetricGroup = new CreateMetricGroup();
        responseMsg = createMetricGroup.processPostRequest(request, testCreateMetricGroup);
        assertEquals(mockReturnString, responseMsg);     
    }
}
