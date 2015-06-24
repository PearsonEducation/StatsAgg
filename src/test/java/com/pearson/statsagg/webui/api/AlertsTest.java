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

import com.pearson.statsagg.database.alerts.AlertsDao;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author prashant4nov
 */
public class AlertsTest extends Mockito {
    private static JSONObject mockAlertsJson = new JSONObject();
    private static AlertsDao alertsDao;
    private static final Logger logger = LoggerFactory.getLogger(Alerts.class.getName());

    @BeforeClass
    public static void setUp() {
        JSONArray mockAlertsList = new JSONArray();
        JSONObject mockAlert = new JSONObject();
        
        mockAlert.put("name", "abcd");
        mockAlert.put("id", "1");
        mockAlertsList.add(mockAlert);
        
        mockAlert.put("name", "xyz");
        mockAlert.put("id", "2");
        mockAlertsList.add(mockAlert);
             
        mockAlertsJson.put("alerts", mockAlertsList);
        mockAlertsJson.put("count", 2);
        alertsDao = mock(AlertsDao.class);
        when(alertsDao.getAlerts(10, 2)).thenReturn(mockAlertsJson);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testgetAlertsJson() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("page_size")).thenReturn("2");
        when(request.getParameter("page_number")).thenReturn("5");

        Alerts alerts = new Alerts();
        JSONObject result = alerts.getAlertsJson(request, alertsDao);

        verify(request, atLeast(1)).getParameter("page_size");
        verify(request, atLeast(1)).getParameter("page_number");
        assertEquals(mockAlertsJson, result);
    }
}
