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
public class RemoveMetricGroupTest extends Mockito {
    
    private static final Logger logger = LoggerFactory.getLogger(RemoveMetricGroupTest.class.getName());
    
    private static com.pearson.statsagg.webui.MetricGroups metricGroup;
    private static final String mockReturnString = "Delete metric group success. MetricGroupName=\"metric_grp_name\".";
    private static final String metricGroupName = "metric_grp_name";

    @BeforeClass
    public static void setUp() {
        metricGroup = mock(com.pearson.statsagg.webui.MetricGroups.class);
        when(metricGroup.removeMetricGroup(metricGroupName)).thenReturn(mockReturnString);
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testprocessPostRequest() throws Exception {
        String responseMsg;
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("name")).thenReturn(metricGroupName);
        RemoveMetricGroup removeMetricGroup = new RemoveMetricGroup();
        responseMsg = removeMetricGroup.processPostRequest(request, metricGroup);
        assertEquals(mockReturnString, responseMsg);     
    }
    
}
