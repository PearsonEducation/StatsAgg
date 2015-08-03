/*
 * Copyright 2015 prashant kumar (prashant4nov).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 *
 * @author prashant kumar (prashant4nov)
 */
public class MetricGroupsListTest extends Mockito{
    private static final JSONObject mockMetricGroupsJson = new JSONObject();
    private static MetricGroupsDao metricGroupsDao;
    private static MetricGroupsLogic metricGroupsLogic_ = new MetricGroupsLogic();
    private static final Logger logger = LoggerFactory.getLogger(MetricGroupsList.class.getName());

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
