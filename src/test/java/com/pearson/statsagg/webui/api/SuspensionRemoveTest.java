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
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author Prashant Kumar (prashant4nov)
 */
public class SuspensionRemoveTest extends Mockito {
    
    private static final Logger logger = LoggerFactory.getLogger(SuspensionRemoveTest.class.getName());
    
    private static com.pearson.statsagg.webui.Suspensions suspension;
    private static final String mockReturnString = "Delete suspension success. SuspensionName=\"suspension_name\".";
    private static final String suspensionName = "suspension_name";

    @BeforeClass
    public static void setUp() {
        suspension = mock(com.pearson.statsagg.webui.Suspensions.class);
        when(suspension.removeSuspension(suspensionName)).thenReturn(mockReturnString);
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testprocessPostRequest() throws Exception {
        String responseMsg;
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("name")).thenReturn(suspensionName);
        SuspensionRemove removeSuspension = new SuspensionRemove();
        responseMsg = removeSuspension.processPostRequest(request);
        assertEquals(mockReturnString, responseMsg);     
    }
    
}
