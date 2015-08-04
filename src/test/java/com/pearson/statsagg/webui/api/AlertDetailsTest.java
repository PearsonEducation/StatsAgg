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

import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import java.math.BigDecimal;
import java.sql.Timestamp;
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
 *
 * @author Prashant Kumar (prashant4nov)
 */
public class AlertDetailsTest extends Mockito {
    private static AlertsDao alertsDao;
    private static final Logger logger = LoggerFactory.getLogger(AlertDetails.class.getName());

    @Before
    public void setUp() {
        Alert mockAlert = new Alert(101, "alert junit 1", "alert junit 1" , 12, false, true, true, Alert.TYPE_THRESHOLD, true, true, 300000, 
            13, 13, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 1, false, new Timestamp(System.currentTimeMillis()), false, null, null,
            13, 13, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("200"), 1000L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 2, true, new Timestamp(System.currentTimeMillis()), false, null, null);
        alertsDao = mock(AlertsDao.class);
        when(alertsDao.getAlert(101)).thenReturn(mockAlert);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testGetAlertDetails() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(Helper.id)).thenReturn("101");

        AlertDetails alertDetails = new AlertDetails();
        JSONObject result = alertDetails.getAlertDetails(request, alertsDao);

        verify(request, atLeast(1)).getParameter(Helper.id);
        assertEquals("alert junit 1", result.get("description"));
        assertEquals(12, result.get("metricgroup_id"));
        assertEquals("alert junit 1", result.get("name"));
    }
}
