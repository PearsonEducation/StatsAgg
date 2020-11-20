package com.pearson.statsagg.database_objects.suspensions;

import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import java.sql.Timestamp;
import java.util.Calendar;
import com.pearson.statsagg.utilities.time_utils.DateAndTime;
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
public class SuspensionTest {
    
    public Suspension suspension_Reference_;
    
    public SuspensionTest() {
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
        
        suspension_Reference_ = new Suspension(
                -1, "Suspension Name 1", "desc", true, Suspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2", "", true, true, 
                true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, (60000l * 40), DatabaseObjectCommon.TIME_UNIT_MINUTES, endTimeTimestamp);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of copy method, of class Suspension.
     */
    @Test
    public void testCopy() {
    }

    /**
     * Test of isEqual method, of class Suspension.
     */
    @Test
    public void testIsEqual() {
    }

    /**
     * Test of isValid method, of class Suspension.
     */
    @Test
    public void testIsValid() {
        assertTrue(testIsValid_MissingInputs_AlertId());
        
        assertTrue(testIsValid_DurationLessThanOneDay());
        assertTrue(testIsValid_DurationOneDay());
        assertFalse(testIsValid_DurationMoreThanOneDay());
    }
    
    public boolean testIsValid_MissingInputs_AlertId() {
        Suspension suspension_Reference_Copy = Suspension.copy(suspension_Reference_);
        suspension_Reference_Copy.setAlertId(null);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.MINUTE, 1440);
        Timestamp endTimeTimestamp = new Timestamp(endCalendar.getTimeInMillis());
        suspension_Reference_Copy.setDeleteAtTimestamp(endTimeTimestamp);
        return Suspension.isValid(suspension_Reference_Copy).isValid();
    }
    
    public boolean testIsValid_DurationLessThanOneDay() {
        Calendar seedCalendar = Calendar.getInstance();
        
        Calendar startDate = DateAndTime.getCalendarWithSameDateAtStartofDay(seedCalendar);
        Timestamp startDateTimestamp = new Timestamp(startDate.getTimeInMillis());
        Calendar startTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 21, 0, 0, 0);
        Timestamp startTimeTimestamp = new Timestamp(startTime.getTimeInMillis());
        
        Suspension suspension = new Suspension(
                -1, "Suspension Name 1", "desc", true, Suspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2", "",
                false, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, (60000l * 1439), DatabaseObjectCommon.TIME_UNIT_MINUTES, null);
        
        return Suspension.isValid(suspension).isValid();
    }
    
    public boolean testIsValid_DurationOneDay() {
        Calendar seedCalendar = Calendar.getInstance();
        
        Calendar startDate = DateAndTime.getCalendarWithSameDateAtStartofDay(seedCalendar);
        Timestamp startDateTimestamp = new Timestamp(startDate.getTimeInMillis());
        Calendar startTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 21, 0, 0, 0);
        Timestamp startTimeTimestamp = new Timestamp(startTime.getTimeInMillis());
        
        Suspension suspension = new Suspension(
                -1, "Suspension Name 1", "desc", true, Suspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "tag1 tag2", "excl tag1 tag2", "",
                false, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, (60000l * 1440), DatabaseObjectCommon.TIME_UNIT_MINUTES, null);
        
        return Suspension.isValid(suspension).isValid();
    }
    
    public boolean testIsValid_DurationMoreThanOneDay() {
        Calendar seedCalendar = Calendar.getInstance();
        Calendar startDate = DateAndTime.getCalendarWithSameDateAtStartofDay(seedCalendar);
        Timestamp startDateTimestamp = new Timestamp(startDate.getTimeInMillis());
        Calendar startTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 21, 0, 0, 0);
        Timestamp startTimeTimestamp = new Timestamp(startTime.getTimeInMillis());
        
        Suspension suspension = new Suspension(
                -1, "Suspension Name 1", "desc", true, Suspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2", "",
                false, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, (60000l * 1441), DatabaseObjectCommon.TIME_UNIT_MINUTES, null);
        
        return Suspension.isValid(suspension).isValid();
    }

    /**
     * Test of isDateAndTimeInSuspensionWindow method, of class Suspension.
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
        
        Suspension suspension = new Suspension(
                -1, "Suspension Name 1", "desc", true, Suspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2", "",
                false, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, (60000l * 30), DatabaseObjectCommon.TIME_UNIT_MINUTES, null);
        
        Calendar checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 23, 00, 0, 0);
        assertTrue(Suspension.isDateAndTimeInSuspensionWindow(suspension, checkDateAndTime));
        
        checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 23, 29, 59, 999);
        assertTrue(Suspension.isDateAndTimeInSuspensionWindow(suspension, checkDateAndTime));
    }
    
    private void testIsDateAndTimeInSuspensionWindow_DateTooEarly_TimeOutOfWindow_SingleDay(Calendar seedCalendar) {
        Timestamp startDateTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        Timestamp startTimeTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        
        Suspension suspension = new Suspension(
                -1, "Suspension Name 1", "desc", true, Suspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2", "",
                false, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, (60000l * 30), DatabaseObjectCommon.TIME_UNIT_MINUTES, null);
        
        Calendar checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 22, 59, 59, 999);
        checkDateAndTime.add(Calendar.DATE, -1);
        assertFalse(Suspension.isDateAndTimeInSuspensionWindow(suspension, checkDateAndTime));
        
        checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 23, 30, 0, 0);
        checkDateAndTime.add(Calendar.DATE, -1);
        assertFalse(Suspension.isDateAndTimeInSuspensionWindow(suspension, checkDateAndTime));
    }

    private void testIsDateAndTimeInSuspensionWindow_DateTooEarly_TimeInWindow_SingleDay(Calendar seedCalendar) {
        Timestamp startDateTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        Timestamp startTimeTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        
        Suspension suspension = new Suspension(
                -1, "Suspension Name 1", "desc", true, Suspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2", "",
                false, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, (60000l * 30), DatabaseObjectCommon.TIME_UNIT_MINUTES, null);
        
        Calendar checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 23, 00, 0, 0);
        checkDateAndTime.add(Calendar.DATE, -1);
        assertFalse(Suspension.isDateAndTimeInSuspensionWindow(suspension, checkDateAndTime));
        
        checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 23, 29, 59, 999);
        checkDateAndTime.add(Calendar.DATE, -1);
        assertFalse(Suspension.isDateAndTimeInSuspensionWindow(suspension, checkDateAndTime));
    }
    
    public void testIsDateAndTimeInSuspensionWindow_DateInWindow_TimeOutOfWindow_SingleDay(Calendar seedCalendar) {
        Timestamp startDateTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        Timestamp startTimeTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        
        Suspension suspension = new Suspension(
                -1, "Suspension Name 1", "desc", true, Suspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2", "",
                false, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, (60000l * 30), DatabaseObjectCommon.TIME_UNIT_MINUTES, null);
        
        Calendar checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 22, 59, 59, 999);
        assertFalse(Suspension.isDateAndTimeInSuspensionWindow(suspension, checkDateAndTime));
        
        checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 23, 30, 0, 0);
        assertFalse(Suspension.isDateAndTimeInSuspensionWindow(suspension, checkDateAndTime));
    }
    
    private void testIsDateAndTimeInSuspensionWindow_DateInWindow_TimeInWindow_MultiDay(Calendar seedCalendar) {
        Timestamp startDateTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        Timestamp startTimeTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        
        Suspension suspension = new Suspension(
                -1, "Suspension Name 1", "desc", true, Suspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2", "",
                false, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, (60000l * 75), DatabaseObjectCommon.TIME_UNIT_MINUTES, null);
        
        Calendar checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime.add(Calendar.DATE, 1);
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 00, 00, 00, 000);
        assertTrue(Suspension.isDateAndTimeInSuspensionWindow(suspension, checkDateAndTime));
        
        checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime.add(Calendar.DATE, 1);
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 00, 14, 59, 999);
        assertTrue(Suspension.isDateAndTimeInSuspensionWindow(suspension, checkDateAndTime));
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
        
        Suspension suspension = new Suspension(
                -1, "Suspension Name 1", "desc", true, Suspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2", "",
                false, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, (60000l * 1440), DatabaseObjectCommon.TIME_UNIT_MINUTES, null);
        
        Calendar checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime.add(Calendar.DATE, 40);
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 18, 00, 00, 000);
        
        System.out.println(seedCalendar.getTimeInMillis() + "   " + checkDateAndTime.getTimeInMillis());
        
        assertTrue(Suspension.isDateAndTimeInSuspensionWindow(suspension, checkDateAndTime));
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
        
        Suspension suspension = new Suspension(
                -1, "Suspension Name 1", "desc", true, Suspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2", "",
                false, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, (60000l * 1440), DatabaseObjectCommon.TIME_UNIT_MINUTES, null);
        
        Calendar checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 2, 20, 00, 000);
        assertTrue(Suspension.isDateAndTimeInSuspensionWindow(suspension, checkDateAndTime));
    }
    
    private void testIsDateAndTimeInSuspensionWindow_DateTooEarly_TimeOutOfWindow_MultiDay(Calendar seedCalendar) {
        Timestamp startDateTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        Timestamp startTimeTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        
        Suspension suspension = new Suspension(
                -1, "Suspension Name 1", "desc", true, Suspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2", "",
                false, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, (60000l * 75), DatabaseObjectCommon.TIME_UNIT_MINUTES, null);
        
        Calendar checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 00, 15, 0, 0);
        checkDateAndTime.add(Calendar.DATE, -1);
        assertFalse(Suspension.isDateAndTimeInSuspensionWindow(suspension, checkDateAndTime));
        
        checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 00, 15, 0, 0);
        assertFalse(Suspension.isDateAndTimeInSuspensionWindow(suspension, checkDateAndTime));
        
        checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 00, 16, 0, 0);
        checkDateAndTime.add(Calendar.DATE, -1);
        assertFalse(Suspension.isDateAndTimeInSuspensionWindow(suspension, checkDateAndTime));
        
        checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 00, 16, 0, 0);
        assertFalse(Suspension.isDateAndTimeInSuspensionWindow(suspension, checkDateAndTime));
    }

    private void testIsDateAndTimeInSuspensionWindow_DateTooEarly_TimeInWindow_MultiDay(Calendar seedCalendar) {
        Timestamp startDateTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        Timestamp startTimeTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        
        Suspension suspension = new Suspension(
                -1, "Suspension Name 1", "desc", true, Suspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2", "",
                false, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, (60000l * 75), DatabaseObjectCommon.TIME_UNIT_MINUTES, null);
        
        Calendar checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime.add(Calendar.DATE, -1);
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 00, 14, 59, 999);
        assertFalse(Suspension.isDateAndTimeInSuspensionWindow(suspension, checkDateAndTime));
        
        checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 00, 14, 59, 999);
        assertFalse(Suspension.isDateAndTimeInSuspensionWindow(suspension, checkDateAndTime));
    }
    
    public void testIsDateAndTimeInSuspensionWindow_DateInWindow_TimeOutOfWindow_MultiDay(Calendar seedCalendar) {
        Timestamp startDateTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        Timestamp startTimeTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        Calendar endTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 22, 0, 0, 0);
        Timestamp endTimeTimestamp = new Timestamp(endTime.getTimeInMillis());
        
        Suspension suspension = new Suspension(
                -1, "Suspension Name 1", "desc", true, Suspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2", "",
                true, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, (60000l * 75), DatabaseObjectCommon.TIME_UNIT_MINUTES, endTimeTimestamp);
        
        Calendar checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime.add(Calendar.DATE, 1);
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 00, 15, 0, 0);
        assertFalse(Suspension.isDateAndTimeInSuspensionWindow(suspension, checkDateAndTime));
        
        checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime.add(Calendar.DATE, 1);
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 00, 16, 0, 0);
        assertFalse(Suspension.isDateAndTimeInSuspensionWindow(suspension, checkDateAndTime));
    }
    
    private void testIsDateAndTimeInSuspensionWindow_DateInWindow_TimeInWindow_MultiDay_DayOfWeekNotAllowed(Calendar seedCalendar) {
        Timestamp startDateTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        Timestamp startTimeTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        
        Suspension suspension = new Suspension(
                -1, "Suspension Name 1", "desc", true, Suspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2", "",
                false, true, true, true, true, true, true, false, true, 
                startDateTimestamp, startTimeTimestamp, (60000l * 75), DatabaseObjectCommon.TIME_UNIT_MINUTES, null);
        
        Calendar checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime.add(Calendar.DATE, 1);
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 00, 14, 00, 000);
        assertFalse(Suspension.isDateAndTimeInSuspensionWindow(suspension, checkDateAndTime));
    }
    
    private void testIsDateAndTimeInSuspensionWindow_DateInWindow_TimeInWindow_SingleDay_DayOfWeekNotAllowed(Calendar seedCalendar) {
        Timestamp startDateTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        Timestamp startTimeTimestamp = new Timestamp(seedCalendar.getTimeInMillis());
        
        Suspension suspension = new Suspension(
                -1, "Suspension Name 1", "desc", true, Suspension.SUSPEND_BY_METRIC_GROUP_TAGS, 1, "incl tag1 tag2", "excl tag1 tag2", "",
                false, true, true, true, true, true, true, false, true, 
                startDateTimestamp, startTimeTimestamp, (60000l * 15), DatabaseObjectCommon.TIME_UNIT_MINUTES, null);
        
        Calendar checkDateAndTime = Calendar.getInstance();
        checkDateAndTime.setTimeInMillis(seedCalendar.getTimeInMillis());
        checkDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(checkDateAndTime, 23, 10, 00, 000);
        assertFalse(Suspension.isDateAndTimeInSuspensionWindow(suspension, checkDateAndTime));
    }
    
}
