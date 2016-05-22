package com.pearson.statsagg.webui.api;

import com.pearson.statsagg.database_objects.suspensions.SuspensionsDao;
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
public class SuspensionListTest extends Mockito {
    
    private static final Logger logger = LoggerFactory.getLogger(SuspensionListTest.class.getName());

    private static JSONObject mockSuspensionJson = new JSONObject();
    private static SuspensionsDao suspensionsDao;

    @BeforeClass
    public static void setUp() {
        JSONArray mockSuspensionList = new JSONArray();
        JSONObject mockSuspension = new JSONObject();
        
        mockSuspension.put("name", "abcd");
        mockSuspension.put("id", "1");
        mockSuspensionList.add(mockSuspension);
        
        mockSuspension = new JSONObject();
        mockSuspension.put("name", "xyz");
        mockSuspension.put("id", "2");
        mockSuspensionList.add(mockSuspension);
             
        mockSuspensionJson.put("suspensions", mockSuspensionList);
        mockSuspensionJson.put("count", 2);
        suspensionsDao = mock(SuspensionsDao.class);
        when(suspensionsDao.getSuspension(10, 2)).thenReturn(mockSuspensionJson);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testGetSuspensionList() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("page_size")).thenReturn("2");
        when(request.getParameter("page_number")).thenReturn("5");

        SuspensionsList suspensionList = new SuspensionsList();
        JSONObject result = suspensionList.getSuspensionsList(request);

        verify(request, atLeast(1)).getParameter("page_size");
        verify(request, atLeast(1)).getParameter("page_number");
        assertEquals(mockSuspensionJson, result);
    }
    
}
