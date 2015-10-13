package com.pearson.statsagg.webui.api;

import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Prashant Kumar (prashant4nov)
 */
public class AlertsListTest extends Mockito {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertsListTest.class.getName());

    private static JSONObject mockAlertsJson = new JSONObject();
    private static AlertsDao alertsDao;

    @BeforeClass
    public static void setUp() {
        JSONArray mockAlertsList = new JSONArray();
        JSONObject mockAlert = new JSONObject();
        
        mockAlert.put("name", "abcd");
        mockAlert.put("id", "1");
        mockAlertsList.add(mockAlert);
        
        mockAlert = new JSONObject();
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
    public void testGetAlertsJson() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("page_size")).thenReturn("2");
        when(request.getParameter("page_number")).thenReturn("5");

        AlertsList alerts = new AlertsList();
        JSONObject result = alerts.getAlertsList(request, alertsDao);

        verify(request, atLeast(1)).getParameter("page_size");
        verify(request, atLeast(1)).getParameter("page_number");
        assertEquals(mockAlertsJson, result);
    }  
    
}
