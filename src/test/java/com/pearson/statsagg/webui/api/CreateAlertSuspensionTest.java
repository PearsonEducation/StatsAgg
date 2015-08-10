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
import org.json.simple.JSONObject;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
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
 *
 * @author Prashant Kumar (prashant4nov)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Helper.class )
public class CreateAlertSuspensionTest extends Mockito {
    private static final Logger logger = LoggerFactory.getLogger(CreateAlert.class.getName());
    private static final String mockReturnString = "Successful alert suspension creation.";
    private static final JSONObject alertSuspensionData = new JSONObject();
    static com.pearson.statsagg.webui.CreateAlertSuspension testCreateAlertSuspension = mock(com.pearson.statsagg.webui.CreateAlertSuspension.class);
    static HttpServletRequest request = mock(HttpServletRequest.class);

    @BeforeClass
    public static void setUp() {
        when(testCreateAlertSuspension.parseAndAlterAlertSuspension(alertSuspensionData)).thenReturn(mockReturnString);
        PowerMockito.mockStatic(Helper.class);
        PowerMockito.when(Helper.getRequestData(request)).thenReturn(alertSuspensionData);
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testProcessPostRequest() throws Exception {
        String responseMsg;
        CreateAlertSuspension createAlertSuspension = new CreateAlertSuspension();
        responseMsg = createAlertSuspension.processPostRequest(request, testCreateAlertSuspension);
        assertEquals(mockReturnString, responseMsg);     
    }
}
