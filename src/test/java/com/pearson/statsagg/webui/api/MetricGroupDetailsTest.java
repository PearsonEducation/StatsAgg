package com.pearson.statsagg.webui.api;

import com.pearson.statsagg.database_objects.metric_group.MetricGroup;
import com.pearson.statsagg.database_objects.metric_group.MetricGroupsDao;
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
public class MetricGroupDetailsTest extends Mockito {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricGroupDetailsTest.class.getName());

    private static MetricGroupsDao metricGroupsDao;

    @Before
    public void setUp() {
        MetricGroup mockMetricGroup = new MetricGroup(101, "metric_name", "metric_description");
        metricGroupsDao = mock(MetricGroupsDao.class);
        when(metricGroupsDao.getMetricGroup(101)).thenReturn(mockMetricGroup);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testGetMetricGroupDetails() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(Helper.id)).thenReturn("101");

        MetricGroupDetails metricGroupDetails = new MetricGroupDetails();
        
        JSONObject result = metricGroupDetails.getMetricGroup(request, metricGroupsDao);
        System.out.println(result);
        verify(request, atLeast(1)).getParameter(Helper.id);
        assertEquals("metric_description", result.get("description"));
        assertEquals(101, result.get("id"));
        assertEquals("metric_name", result.get("name"));
    }
    
}
