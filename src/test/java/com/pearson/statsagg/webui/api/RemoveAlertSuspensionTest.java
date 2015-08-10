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
public class RemoveAlertSuspensionTest extends Mockito {
    private static final Logger logger = LoggerFactory.getLogger(RemoveAlert.class.getName());
    private static com.pearson.statsagg.webui.AlertSuspensions alertSuspension;
    private static final String mockReturnString = "Delete alert suspension success. AlertSuspensionName=\"alert_suspension_name\".";
    private static final String alertSuspensionName = "alert_suspension_name";

    @BeforeClass
    public static void setUp() {
        alertSuspension = mock(com.pearson.statsagg.webui.AlertSuspensions.class);
        when(alertSuspension.removeAlertSuspension(alertSuspensionName)).thenReturn(mockReturnString);
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testprocessPostRequest() throws Exception {
        String responseMsg;
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("name")).thenReturn(alertSuspensionName);
        RemoveAlertSuspension removeAlertSuspension = new RemoveAlertSuspension();
        responseMsg = removeAlertSuspension.processPostRequest(request, alertSuspension);
        assertEquals(mockReturnString, responseMsg);     
    }
}
