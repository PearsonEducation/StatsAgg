package com.pearson.statsagg.webui.api;

import com.pearson.statsagg.database_objects.alert_suspensions.AlertSuspensionsDao;
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
    
    private static final Logger logger = LoggerFactory.getLogger(AlertSuspensionListTest.class.getName());

    private static JSONObject mockAlertSuspensionJson = new JSONObject();
    private static AlertSuspensionsDao alertSuspensionsDao;

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
        when(alertSuspensionsDao.getSuspension(10, 2)).thenReturn(mockAlertSuspensionJson);
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
