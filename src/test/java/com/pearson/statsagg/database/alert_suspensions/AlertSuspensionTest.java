package com.pearson.statsagg.database.alert_suspensions;

import java.sql.Timestamp;
import java.util.Calendar;
import com.pearson.statsagg.utilities.DateAndTime;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jeffrey Schmidt
 */
public class AlertSuspensionTest {
    
    public AlertSuspension alertSuspension_Reference_;
    
    public AlertSuspensionTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        Calendar seedCalendar = Calendar.getInstance();
        
        Calendar startDate = DateAndTime.getCalendarWithSameDateAtStartofDay(seedCalendar);
        Timestamp startDateTimestamp = new Timestamp(startDate.getTimeInMillis());
        Calendar startTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 21, 0, 0, 0);
        Timestamp startTimeTimestamp = new Timestamp(startTime.getTimeInMillis());
        Calendar endTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 22, 0, 0, 0);
        Timestamp endTimeTimestamp = new Timestamp(endTime.getTimeInMillis());
        
        alertSuspension_Reference_ = new AlertSuspension(
                -1, "AlertSuspension Name 1", "desc", true, AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2", true, true, 
                true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, 40, endTimeTimestamp);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of copy method, of class AlertSuspension.
     */
    @Test
    public void testCopy() {
    }

    /**
     * Test of isEqual method, of class AlertSuspension.
     */
    @Test
    public void testIsEqual() {
    }

    /**
     * Test of isValid method, of class AlertSuspension.
     */
    @Test
    public void testIsValid() {
        assertTrue(testIsValid_MissingInputs_AlertId());
        
        assertTrue(testIsValid_DurationLessThanOneDay());
        assertTrue(testIsValid_DurationOneDay());
        assertFalse(testIsValid_DurationMoreThanOneDay());
    }
    
    public boolean testIsValid_MissingInputs_AlertId() {
        AlertSuspension alertSuspension_Reference_Copy = AlertSuspension.copy(alertSuspension_Reference_);
        alertSuspension_Reference_Copy.setAlertId(null);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.MINUTE, 1440);
        Timestamp endTimeTimestamp = new Timestamp(endCalendar.getTimeInMillis());
        alertSuspension_Reference_Copy.setDeleteAtTimestamp(endTimeTimestamp);
        return AlertSuspension.isValid(alertSuspension_Reference_Copy);
    }
    
    public boolean testIsValid_DurationLessThanOneDay() {
        Calendar seedCalendar = Calendar.getInstance();
        
        Calendar startDate = DateAndTime.getCalendarWithSameDateAtStartofDay(seedCalendar);
        Timestamp startDateTimestamp = new Timestamp(startDate.getTimeInMillis());
        Calendar startTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 21, 0, 0, 0);
        Timestamp startTimeTimestamp = new Timestamp(startTime.getTimeInMillis());
        
        AlertSuspension alertSuspension = new AlertSuspension(
                -1, "AlertSuspension Name 1", "desc", true, AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2", 
                false, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, 1439, null);
        
        return AlertSuspension.isValid(alertSuspension);
    }
    
    public boolean testIsValid_DurationOneDay() {
        Calendar seedCalendar = Calendar.getInstance();
        
        Calendar startDate = DateAndTime.getCalendarWithSameDateAtStartofDay(seedCalendar);
        Timestamp startDateTimestamp = new Timestamp(startDate.getTimeInMillis());
        Calendar startTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 21, 0, 0, 0);
        Timestamp startTimeTimestamp = new Timestamp(startTime.getTimeInMillis());
        
        AlertSuspension alertSuspension = new AlertSuspension(
                -1, "AlertSuspension Name 1", "desc", true, AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "tag1 tag2", "excl tag1 tag2", 
                false, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, 1440, null);
        
        return AlertSuspension.isValid(alertSuspension);
    }
    
    public boolean testIsValid_DurationMoreThanOneDay() {
        Calendar seedCalendar = Calendar.getInstance();
        Calendar startDate = DateAndTime.getCalendarWithSameDateAtStartofDay(seedCalendar);
        Timestamp startDateTimestamp = new Timestamp(startDate.getTimeInMillis());
        Calendar startTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 21, 0, 0, 0);
        Timestamp startTimeTimestamp = new Timestamp(startTime.getTimeInMillis());
        
        AlertSuspension alertSuspension = new AlertSuspension(
                -1, "AlertSuspension Name 1", "desc", true, AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2", 
                false, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, 1441, null);
        
        return AlertSuspension.isValid(alertSuspension);
    }

    /**
     * Test of isDateAndTimeInSuspensionWindow method, of class AlertSuspension.
     */
    @Test
    public void testIsDateAndTimeInSuspensionWindow() {
        // seed calendar = 2014-07-18, 23:00:00.000
        Calendar seedCalendar = Calendar.getInstance();
        seedCalendar.set(Calendar.YEAR, 2014);
        seedCalendar.set(Calendar.MONTH, Calendar.JULY);
        seedCalendar.set(Calendar.DATE, 18);
        seedCalendar = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 23, 0, 0, 0);

        long startTime = System.currentTimeMillis();
        
        testIsDateAndTimeInSuspensionWindow_DateInWindow_TimeInWindow_SingleDay(seedCalendar);
        testIsDateAndTimeInSuspensionWindow_DateTooEarly_TimeOutOfWindow_SingleDay(seedCalendar);
        testIsDateAndTimeInSuspensionWindow_DateTooEarly_TimeInWindow_SingleDay(seedCalendar);
        testIsDateAndTimeInSuspensionWindow_DateInWindow_TimeOutOfWindow_SingleDay(seedCalendar);
        testIsDateAndTimeInSuspensionWindow_DateInWindow_TimeInWindow_MultiDay(seedCalendar);
        testIsDateAndTimeInSuspensionWindow_DateInWindow_TimeInWindow_MultiDay_NotInYesterdaysWindow();
        testIsDateAndTimeInSuspensionWindow_DateInWindow_TimeInWindow_MultiDay_RecentStartDate();
        testIsDateAndTimeInSuspensionWindow_DateTooEarly_TimeOutOfWindow_MultiDay(seedCalendar);
        testIsDateAndTimeInSuspensionWindow_DateTooEarly_TimeInWindow_MultiDay(seedCalendar);
        testIsDateAndTimeInSuspensionWindow_DateInWindow_TimeOutOfWindow_MultiDay(seedCalendar);
        testIsDateAndTimeInSuspensionWindow_DateInWindow_TimeInWindow_MultiDay_DayOfWeekNotAllowed(seedCalendar);
        testIsDateAndTimeInSuspensionWindow_DateInWindow_TimeInWindow_SingleDay_DayOfWeekNotAllowed(seedCalendar);
        
        System.out.println("testIsDateAndTimeInSuspensionWindow Time Elapsed: " + (System.currentTimeMillis() - startTime));
    }
    
    private void testIsDateAndTimeInSuspensionWindow_DateInWindow_TimeInWindow_SingleDay(Calendar seedCalendar) {
        Timestamp startDateTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        Timestamp startTimeTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        
        AlertSuspension alertSuspension = new AlertSuspension(
                -1, "AlertSuspension Name 1", "desc", true, AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2",
                false, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, 30, null);
        
        Calendar checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 23, 00, 0, 0);
        assertTrue(AlertSuspension.isDateAndTimeInSuspensionWindow(alertSuspension, checkDateAndTime));
        
        checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 23, 29, 59, 999);
        assertTrue(AlertSuspension.isDateAndTimeInSuspensionWindow(alertSuspension, checkDateAndTime));
    }
    
    private void testIsDateAndTimeInSuspensionWindow_DateTooEarly_TimeOutOfWindow_SingleDay(Calendar seedCalendar) {
        Timestamp startDateTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        Timestamp startTimeTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        
        AlertSuspension alertSuspension = new AlertSuspension(
                -1, "AlertSuspension Name 1", "desc", true, AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2",
                false, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, 30, null);
        
        Calendar checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 22, 59, 59, 999);
        checkDateAndTime.add(Calendar.DATE, -1);
        assertFalse(AlertSuspension.isDateAndTimeInSuspensionWindow(alertSuspension, checkDateAndTime));
        
        checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 23, 30, 0, 0);
        checkDateAndTime.add(Calendar.DATE, -1);
        assertFalse(AlertSuspension.isDateAndTimeInSuspensionWindow(alertSuspension, checkDateAndTime));
    }

    private void testIsDateAndTimeInSuspensionWindow_DateTooEarly_TimeInWindow_SingleDay(Calendar seedCalendar) {
        Timestamp startDateTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        Timestamp startTimeTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        
        AlertSuspension alertSuspension = new AlertSuspension(
                -1, "AlertSuspension Name 1", "desc", true, AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2",
                false, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, 30, null);
        
        Calendar checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 23, 00, 0, 0);
        checkDateAndTime.add(Calendar.DATE, -1);
        assertFalse(AlertSuspension.isDateAndTimeInSuspensionWindow(alertSuspension, checkDateAndTime));
        
        checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 23, 29, 59, 999);
        checkDateAndTime.add(Calendar.DATE, -1);
        assertFalse(AlertSuspension.isDateAndTimeInSuspensionWindow(alertSuspension, checkDateAndTime));
    }
    
    public void testIsDateAndTimeInSuspensionWindow_DateInWindow_TimeOutOfWindow_SingleDay(Calendar seedCalendar) {
        Timestamp startDateTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        Timestamp startTimeTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        
        AlertSuspension alertSuspension = new AlertSuspension(
                -1, "AlertSuspension Name 1", "desc", true, AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2",
                false, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, 30, null);
        
        Calendar checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 22, 59, 59, 999);
        assertFalse(AlertSuspension.isDateAndTimeInSuspensionWindow(alertSuspension, checkDateAndTime));
        
        checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 23, 30, 0, 0);
        assertFalse(AlertSuspension.isDateAndTimeInSuspensionWindow(alertSuspension, checkDateAndTime));
    }
    
    private void testIsDateAndTimeInSuspensionWindow_DateInWindow_TimeInWindow_MultiDay(Calendar seedCalendar) {
        Timestamp startDateTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        Timestamp startTimeTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        
        AlertSuspension alertSuspension = new AlertSuspension(
                -1, "AlertSuspension Name 1", "desc", true, AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2",
                false, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, 75, null);
        
        Calendar checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime.add(Calendar.DATE, 1);
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 00, 00, 00, 000);
        assertTrue(AlertSuspension.isDateAndTimeInSuspensionWindow(alertSuspension, checkDateAndTime));
        
        checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime.add(Calendar.DATE, 1);
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 00, 14, 59, 999);
        assertTrue(AlertSuspension.isDateAndTimeInSuspensionWindow(alertSuspension, checkDateAndTime));
    }
    
    private void testIsDateAndTimeInSuspensionWindow_DateInWindow_TimeInWindow_MultiDay_NotInYesterdaysWindow() {
        // seed calendar = 2014-07-17, 23:00:00.000
        Calendar seedCalendar = Calendar.getInstance();
        seedCalendar.set(Calendar.YEAR, 2014);
        seedCalendar.set(Calendar.MONTH, Calendar.JULY);
        seedCalendar.set(Calendar.DATE, 17);
        seedCalendar = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 17, 0, 0, 0);
        
        Timestamp startDateTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        Timestamp startTimeTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        
        AlertSuspension alertSuspension = new AlertSuspension(
                -1, "AlertSuspension Name 1", "desc", true, AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2",
                false, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, 1440, null);
        
        Calendar checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime.add(Calendar.DATE, 40);
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 18, 00, 00, 000);
        
        System.out.println(seedCalendar.getTimeInMillis() + "   " + checkDateAndTime.getTimeInMillis());
        
        assertTrue(AlertSuspension.isDateAndTimeInSuspensionWindow(alertSuspension, checkDateAndTime));
    }
    
    private void testIsDateAndTimeInSuspensionWindow_DateInWindow_TimeInWindow_MultiDay_RecentStartDate() {
        // seed calendar = 2014-07-17, 02:00:00.000
        Calendar seedCalendar = Calendar.getInstance();
        seedCalendar.set(Calendar.YEAR, 2014);
        seedCalendar.set(Calendar.MONTH, Calendar.JULY);
        seedCalendar.set(Calendar.DATE, 17);
        seedCalendar = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 2, 0, 0, 0);
        
        Timestamp startDateTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        Timestamp startTimeTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        
        AlertSuspension alertSuspension = new AlertSuspension(
                -1, "AlertSuspension Name 1", "desc", true, AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2",
                false, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, 1440, null);
        
        Calendar checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 2, 20, 00, 000);
        assertTrue(AlertSuspension.isDateAndTimeInSuspensionWindow(alertSuspension, checkDateAndTime));
    }
    
    private void testIsDateAndTimeInSuspensionWindow_DateTooEarly_TimeOutOfWindow_MultiDay(Calendar seedCalendar) {
        Timestamp startDateTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        Timestamp startTimeTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        
        AlertSuspension alertSuspension = new AlertSuspension(
                -1, "AlertSuspension Name 1", "desc", true, AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2", 
                false, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, 75, null);
        
        Calendar checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 00, 15, 0, 0);
        checkDateAndTime.add(Calendar.DATE, -1);
        assertFalse(AlertSuspension.isDateAndTimeInSuspensionWindow(alertSuspension, checkDateAndTime));
        
        checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 00, 15, 0, 0);
        assertFalse(AlertSuspension.isDateAndTimeInSuspensionWindow(alertSuspension, checkDateAndTime));
        
        checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 00, 16, 0, 0);
        checkDateAndTime.add(Calendar.DATE, -1);
        assertFalse(AlertSuspension.isDateAndTimeInSuspensionWindow(alertSuspension, checkDateAndTime));
        
        checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 00, 16, 0, 0);
        assertFalse(AlertSuspension.isDateAndTimeInSuspensionWindow(alertSuspension, checkDateAndTime));
    }

    private void testIsDateAndTimeInSuspensionWindow_DateTooEarly_TimeInWindow_MultiDay(Calendar seedCalendar) {
        Timestamp startDateTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        Timestamp startTimeTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        
        AlertSuspension alertSuspension = new AlertSuspension(
                -1, "AlertSuspension Name 1", "desc", true, AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2",
                false, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, 75, null);
        
        Calendar checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime.add(Calendar.DATE, -1);
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 00, 14, 59, 999);
        assertFalse(AlertSuspension.isDateAndTimeInSuspensionWindow(alertSuspension, checkDateAndTime));
        
        checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 00, 14, 59, 999);
        assertFalse(AlertSuspension.isDateAndTimeInSuspensionWindow(alertSuspension, checkDateAndTime));
    }
    
    public void testIsDateAndTimeInSuspensionWindow_DateInWindow_TimeOutOfWindow_MultiDay(Calendar seedCalendar) {
        Timestamp startDateTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        Timestamp startTimeTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        Calendar endTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 22, 0, 0, 0);
        Timestamp endTimeTimestamp = new Timestamp(endTime.getTimeInMillis());
        
        AlertSuspension alertSuspension = new AlertSuspension(
                -1, "AlertSuspension Name 1", "desc", true, AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2",
                true, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, 75, endTimeTimestamp);
        
        Calendar checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime.add(Calendar.DATE, 1);
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 00, 15, 0, 0);
        assertFalse(AlertSuspension.isDateAndTimeInSuspensionWindow(alertSuspension, checkDateAndTime));
        
        checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime.add(Calendar.DATE, 1);
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 00, 16, 0, 0);
        assertFalse(AlertSuspension.isDateAndTimeInSuspensionWindow(alertSuspension, checkDateAndTime));
    }
    
    private void testIsDateAndTimeInSuspensionWindow_DateInWindow_TimeInWindow_MultiDay_DayOfWeekNotAllowed(Calendar seedCalendar) {
        Timestamp startDateTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        Timestamp startTimeTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        
        AlertSuspension alertSuspension = new AlertSuspension(
                -1, "AlertSuspension Name 1", "desc", true, AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2",
                false, true, true, true, true, true, true, false, true, 
                startDateTimestamp, startTimeTimestamp, 75, null);
        
        Calendar checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime.add(Calendar.DATE, 1);
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 00, 14, 00, 000);
        assertFalse(AlertSuspension.isDateAndTimeInSuspensionWindow(alertSuspension, checkDateAndTime));
    }
    
    private void testIsDateAndTimeInSuspensionWindow_DateInWindow_TimeInWindow_SingleDay_DayOfWeekNotAllowed(Calendar seedCalendar) {
        Timestamp startDateTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        Timestamp startTimeTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        
        AlertSuspension alertSuspension = new AlertSuspension(
                -1, "AlertSuspension Name 1", "desc", true, AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2",
                false, true, true, true, true, true, true, false, true, 
                startDateTimestamp, startTimeTimestamp, 15, null);
        
        Calendar checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 23, 10, 00, 000);
        assertFalse(AlertSuspension.isDateAndTimeInSuspensionWindow(alertSuspension, checkDateAndTime));
    }
    
}
