package com.pearson.statsagg.webui.api;

import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import com.pearson.statsagg.database_objects.alert_suspensions.AlertSuspension;
import com.pearson.statsagg.database_objects.alert_suspensions.AlertSuspensionsDao;
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
public class AlertSuspensionDetailsTest extends Mockito {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertSuspensionDetailsTest.class.getName());

    private static AlertSuspensionsDao alertSuspensionsDao;
    private AlertSuspension mockAlertSuspension;

    @BeforeClass
    public static void setUp() {
        Calendar seedCalendar = Calendar.getInstance();
        Calendar startDate = DateAndTime.getCalendarWithSameDateAtStartofDay(seedCalendar);
        Timestamp startDateTimestamp = new Timestamp(startDate.getTimeInMillis());
        Calendar startTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 21, 0, 0, 0);
        Timestamp startTimeTimestamp = new Timestamp(startTime.getTimeInMillis());
        Calendar endTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 22, 0, 0, 0);
        Timestamp endTimeTimestamp = new Timestamp(endTime.getTimeInMillis());
        AlertSuspension mockAlertSuspension = new AlertSuspension(
        -1, "alertSuspension junit name 1", "desc", true, AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS, null,
        "incl\ntag1\ntag2", "excl\ntag1\ntag2", true, true, true, true, true, true, true, true, true,
        startDateTimestamp, startTimeTimestamp, (60000l * 40), DatabaseObjectCommon.TIME_UNIT_MINUTES, endTimeTimestamp);
        alertSuspensionsDao = mock(AlertSuspensionsDao.class);
        when(alertSuspensionsDao.getAlertSuspension(21)).thenReturn(mockAlertSuspension);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testGetAlertSuspensionDetails() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(Helper.id)).thenReturn("21");

        AlertSuspensionDetails alertSuspensionDetails = new AlertSuspensionDetails();
        JSONObject result = alertSuspensionDetails.getAlertSuspensionDetails(request, alertSuspensionsDao);

        verify(request, atLeast(1)).getParameter(Helper.id);
        assertEquals("excl\ntag1\ntag2", result.get("MetricGroupTagsExclusive"));
        assertEquals("desc", result.get("Description"));
        assertEquals("incl\ntag1\ntag2", result.get("MetricGroupTagsInclusive"));
        assertEquals("alertSuspension junit name 1", result.get("Name"));
    }
    
}
