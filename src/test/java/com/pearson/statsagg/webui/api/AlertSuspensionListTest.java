/*
 * Copyright 2015 prashant4nov.
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

import com.pearson.statsagg.database_objects.alert_suspensions.AlertSuspensionsDao;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
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
 * @author Prashant Kumar (prashant4nov)
 */
public class AlertSuspensionListTest extends Mockito {
    private static JSONObject mockAlertSuspensionJson = new JSONObject();
    private static AlertSuspensionsDao alertSuspensionsDao;
    private static final Logger logger = LoggerFactory.getLogger(AlertSuspensionList.class.getName());

    @BeforeClass
    public static void setUp() {
        JSONArray mockAlertSuspensionList = new JSONArray();
        JSONObject mockAlertSuspension = new JSONObject();
        
        mockAlertSuspension.put("name", "abcd");
        mockAlertSuspension.put("id", "1");
        mockAlertSuspensionList.add(mockAlertSuspension);
        
        mockAlertSuspension = new JSONObject();
        mockAlertSuspension.put("name", "xyz");
        mockAlertSuspension.put("id", "2");
        mockAlertSuspensionList.add(mockAlertSuspension);
             
        mockAlertSuspensionJson.put("alert_suspensions", mockAlertSuspensionList);
        mockAlertSuspensionJson.put("count", 2);
        alertSuspensionsDao = mock(AlertSuspensionsDao.class);
        when(alertSuspensionsDao.getAlertSuspension(10, 2)).thenReturn(mockAlertSuspensionJson);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testGetAlertSuspensionList() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("page_size")).thenReturn("2");
        when(request.getParameter("page_number")).thenReturn("5");

        AlertSuspensionList alertSuspensionList = new AlertSuspensionList();
        JSONObject result = alertSuspensionList.getAlertSuspensionList(request, alertSuspensionsDao);

        verify(request, atLeast(1)).getParameter("page_size");
        verify(request, atLeast(1)).getParameter("page_number");
        assertEquals(mockAlertSuspensionJson, result);
    }
    
}
