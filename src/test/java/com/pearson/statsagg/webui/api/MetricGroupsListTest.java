package com.pearson.statsagg.webui.api;

import com.pearson.statsagg.database_objects.metric_group.MetricGroupsDao;
import com.pearson.statsagg.webui.MetricGroupsLogic;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prashant kumar (prashant4nov)
 */
public class MetricGroupsListTest extends Mockito{
    
    private static final Logger logger = LoggerFactory.getLogger(MetricGroupsListTest.class.getName());

    private static final JSONObject mockMetricGroupsJson = new JSONObject();
    private static MetricGroupsDao metricGroupsDao;
    private static MetricGroupsLogic metricGroupsLogic_ = new MetricGroupsLogic();

    @BeforeClass
    public static void setUp() {
        JSONArray mockMetricGroupsList = new JSONArray();
        JSONObject mockMetric = new JSONObject();
        
        mockMetric.put("name", "abcd");
        mockMetric.put("id", "3");
        mockMetricGroupsList.add(mockMetric);
        
        mockMetric = new JSONObject();
        mockMetric.put("name", "xyz");
        mockMetric.put("id", "4");
        mockMetricGroupsList.add(mockMetric);
             
        mockMetricGroupsJson.put("metricgroups", mockMetricGroupsList);
        mockMetricGroupsJson.put("count", 2);
        metricGroupsDao = mock(MetricGroupsDao.class);
        when(metricGroupsDao.getMetricGroups(10, 2)).thenReturn(mockMetricGroupsJson);
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testGetMetricGroupsJson() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("page_size")).thenReturn("2");
        when(request.getParameter("page_number")).thenReturn("5");

        MetricGroupsList metricGroups = new MetricGroupsList();
        JSONObject result = metricGroups.getMetricGroupsList(request, metricGroupsDao);

        verify(request, atLeast(1)).getParameter("page_size");
        verify(request, atLeast(1)).getParameter("page_number");
        assertEquals(mockMetricGroupsJson, result);
    }   
    
}
