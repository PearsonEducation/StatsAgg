/*
 * Copyright 2015 Prashant Kumar (prashant4nov).
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
 *
 * @author Prashant Kumar (prashant4nov)
 */
public class EnableAlertTest extends Mockito {
    private static final Logger logger = LoggerFactory.getLogger(AlertsList.class.getName());
    private static com.pearson.statsagg.webui.Alerts alert;
    private static final String mockReturnString = "Successfully enabled alert.";
    private static final String alertName = "alert_name";
    private static final Boolean isEnabled = true;

    @BeforeClass
    public static void setUp() {
        alert = mock(com.pearson.statsagg.webui.Alerts.class);
        when(alert.changeAlertEnabled(alertName, isEnabled)).thenReturn(mockReturnString);
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testprocessPostRequest() throws Exception {
        String responseMsg;
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("name")).thenReturn(alertName);
        when(request.getParameter("Enabled")).thenReturn("true");
        EnableAlert enableAlert = new EnableAlert();
        responseMsg = enableAlert.processPostRequest(request, alert);
        assertEquals(mockReturnString, responseMsg);     
    }
}
