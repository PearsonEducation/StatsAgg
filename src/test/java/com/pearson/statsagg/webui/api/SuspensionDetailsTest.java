package com.pearson.statsagg.webui.api;

import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import com.pearson.statsagg.database_objects.suspensions.Suspension;
import com.pearson.statsagg.database_objects.suspensions.SuspensionsDao;
import com.pearson.statsagg.utilities.DateAndTime;
import java.sql.Timestamp;
import java.util.Calendar;
import javax.servlet.http.HttpServletRequest;
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
 * @author Prashant Kumar (prashant4nov)
 */
public class SuspensionDetailsTest extends Mockito {
    
    private static final Logger logger = LoggerFactory.getLogger(SuspensionDetailsTest.class.getName());

    private static SuspensionsDao suspensionsDao;

    @BeforeClass
    public static void setUp() {
        Calendar seedCalendar = Calendar.getInstance();
        Calendar startDate = DateAndTime.getCalendarWithSameDateAtStartofDay(seedCalendar);
        Timestamp startDateTimestamp = new Timestamp(startDate.getTimeInMillis());
        Calendar startTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 21, 0, 0, 0);
        Timestamp startTimeTimestamp = new Timestamp(startTime.getTimeInMillis());
        Calendar endTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 22, 0, 0, 0);
        Timestamp endTimeTimestamp = new Timestamp(endTime.getTimeInMillis());
        Suspension mockSuspension = new Suspension(
        -1, "suspension junit name 1", "desc", true, Suspension.SUSPEND_BY_METRIC_GROUP_TAGS, null,
        "incl\ntag1\ntag2", "excl\ntag1\ntag2", "", true, true, true, true, true, true, true, true, true,
        startDateTimestamp, startTimeTimestamp, (60000l * 40), DatabaseObjectCommon.TIME_UNIT_MINUTES, endTimeTimestamp);
        suspensionsDao = mock(SuspensionsDao.class);
        when(suspensionsDao.getSuspension(21)).thenReturn(mockSuspension);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testGetSuspensionDetails() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("id")).thenReturn("21");

        SuspensionDetails suspensionDetails = new SuspensionDetails();
//        String result = suspensionDetails.getSuspensionDetails(request);
//
//        verify(request, atLeast(1)).getParameter(Helper.id);
//        assertEquals("excl\ntag1\ntag2", result.get("MetricGroupTagsExclusive"));
//        assertEquals("desc", result.get("Description"));
//        assertEquals("incl\ntag1\ntag2", result.get("MetricGroupTagsInclusive"));
//        assertEquals("suspension junit name 1", result.get("Name"));
    }
    
}
