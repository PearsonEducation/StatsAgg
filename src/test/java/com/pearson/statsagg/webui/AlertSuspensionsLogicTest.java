package com.pearson.statsagg.webui;

import com.pearson.statsagg.webui.AlertSuspensionsLogic;
import com.pearson.statsagg.controller.ContextManager;
import com.pearson.statsagg.database.alert_suspensions.AlertSuspension;
import com.pearson.statsagg.database.alert_suspensions.AlertSuspensionsDao;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.utilities.DateAndTime;
import java.io.File;
import java.sql.Timestamp;
import java.util.Calendar;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Jeffrey Schmidt
 */
public class AlertSuspensionsLogicTest {
    
    private final AlertSuspensionsLogic alertSuspensionsLogic_ = new AlertSuspensionsLogic();
    
    public AlertSuspensionsLogicTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        ContextManager ContextManager = new ContextManager();
        ContextManager.initializeDatabaseFromFile(System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator +
                "webapp" + File.separator + "WEB-INF" + File.separator + "config", "database.properties");
    }
    
    @AfterClass
    public static void tearDownClass() {
        DatabaseConnections.disconnectAndShutdown();
    }
    
    @Before
    public void setUp() {
        // delete alertSuspension that was inserted into the database from a previous test. verify that it was deleted.
        String result = alertSuspensionsLogic_.deleteRecordInDatabase("alertSuspension junit name 1");
        assertTrue(result.contains("success") || result.contains("Alert suspension not found"));
        
        // delete a alertSuspension that was inserted into the database from a previous test. verify that it was deleted.
        result = alertSuspensionsLogic_.deleteRecordInDatabase("alertSuspension junit name 11");
        assertTrue(result.contains("success") || result.contains("Alert suspension not found"));
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of alterRecordInDatabase method, of class AlertSuspensionsLogic.
     */
    @Test
    public void testAlterRecordInDatabase() {
        // cleanup from previous unit test
        String result = alertSuspensionsLogic_.deleteRecordInDatabase("alertSuspension junit name 1");
        assertTrue(result.contains("success") || result.contains("Alert suspension not found"));
        
        Calendar seedCalendar = Calendar.getInstance();
        Calendar startDate = DateAndTime.getCalendarWithSameDateAtStartofDay(seedCalendar);
        Timestamp startDateTimestamp = new Timestamp(startDate.getTimeInMillis());
        Calendar startTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 21, 0, 0, 0);
        Timestamp startTimeTimestamp = new Timestamp(startTime.getTimeInMillis());
        Calendar endTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 22, 0, 0, 0);
        Timestamp endTimeTimestamp = new Timestamp(endTime.getTimeInMillis());
        
        // create & insert a AlertSuspension, insert it into the db, retrieve it from the db, & compare the original & retrieved records
        AlertSuspension alertSuspension1 = new AlertSuspension(
                -1, "alertSuspension junit name 1", true, AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS, null, "incl\ntag1\ntag2", "excl\ntag1\ntag2", 
                true, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, 40, endTimeTimestamp);
         
        result = alertSuspensionsLogic_.alterRecordInDatabase(alertSuspension1);
        assertTrue(result.contains("Success"));
        AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao();
        AlertSuspension alertSuspension1FromDb = alertSuspensionsDao.getAlertSuspensionByName("alertSuspension junit name 1");
        assertTrue(alertSuspension1FromDb.getName().contains("alertSuspension junit name 1"));
        assertTrue(alertSuspension1FromDb.getMetricGroupTagsInclusive().contains("incl\ntag1\ntag2"));
        
        alertSuspension1FromDb.setMetricGroupTagsInclusive("incl\ntag1\ntag2\ntag3");
        result = alertSuspensionsLogic_.alterRecordInDatabase(alertSuspension1FromDb);
        assertTrue(result.contains("Fail"));
        result = alertSuspensionsLogic_.alterRecordInDatabase(alertSuspension1FromDb, alertSuspension1FromDb.getName());
        assertTrue(result.contains("Success"));
        alertSuspensionsDao = new AlertSuspensionsDao();
        AlertSuspension alertSuspension2FromDb = alertSuspensionsDao.getAlertSuspensionByName("alertSuspension junit name 1");
        assertTrue(alertSuspension2FromDb.getName().contains("alertSuspension junit name 1"));
        assertTrue(alertSuspension2FromDb.getMetricGroupTagsInclusive().contains("incl\ntag1\ntag2\ntag3"));
        
        result = alertSuspensionsLogic_.deleteRecordInDatabase("alertSuspension junit fake 1");
        assertTrue(result.contains("Cancelling"));
        alertSuspensionsDao = new AlertSuspensionsDao();
        AlertSuspension alertSuspension3FromDb = alertSuspensionsDao.getAlertSuspensionByName("alertSuspension junit name 1");
        assertTrue(alertSuspension3FromDb.getName().contains("alertSuspension junit name 1"));
        
        // test altering alert suspension name
        alertSuspensionsDao = new AlertSuspensionsDao(false);
        AlertSuspension alertSuspensionFromDbOriginalName = alertSuspensionsDao.getAlertSuspensionByName("alertSuspension junit name 1"); // pt1
        assertTrue(alertSuspensionFromDbOriginalName.getName().contains("alertSuspension junit name 1"));
        AlertSuspension alertSuspensionFromDbNewName = AlertSuspension.copy(alertSuspensionFromDbOriginalName);
        alertSuspensionFromDbNewName.setName("alertSuspension junit name 11");
        result = alertSuspensionsLogic_.alterRecordInDatabase(alertSuspensionFromDbNewName, alertSuspensionFromDbNewName.getName());
        assertTrue(result.contains("Successful"));
        AlertSuspension alertSuspensionFromDbNewNameVerify = alertSuspensionsDao.getAlertSuspensionByName(alertSuspensionFromDbNewName.getName()); // pt2
        assertTrue(alertSuspensionFromDbNewNameVerify.getName().contains(alertSuspensionFromDbNewName.getName()));
        assertFalse(alertSuspensionFromDbNewNameVerify.getName().equals(alertSuspensionFromDbOriginalName.getName()));
        assertEquals(alertSuspensionFromDbOriginalName.getId(), alertSuspensionFromDbNewNameVerify.getId());
        AlertSuspension alertSuspensionFromDbOriginalName_NoResult = alertSuspensionsDao.getAlertSuspensionByName(alertSuspensionFromDbOriginalName.getName()); // pt3
        assertEquals(alertSuspensionFromDbOriginalName_NoResult, null);
        AlertSuspension alertSuspensionFromDbOriginalName_Reset = AlertSuspension.copy(alertSuspensionFromDbOriginalName); // pt4
        alertSuspensionFromDbOriginalName_Reset.setName(alertSuspensionFromDbOriginalName.getName());  
        result = alertSuspensionsLogic_.alterRecordInDatabase(alertSuspensionFromDbOriginalName_Reset, alertSuspensionFromDbOriginalName.getName());
        assertTrue(result.contains("Successful"));
        alertSuspensionsDao.close();
        
        result = alertSuspensionsLogic_.deleteRecordInDatabase("alertSuspension junit name 1");
        assertTrue(result.contains("success"));
        alertSuspensionsDao = new AlertSuspensionsDao();
        AlertSuspension alertSuspension4FromDb = alertSuspensionsDao.getAlertSuspensionByName("alertSuspension junit name 1");
        assertEquals(null, alertSuspension4FromDb);
    }

    /**
     * Test of deleteRecordInDatabase method, of class AlertSuspensionsLogic.
     */
    @Test
    public void testDeleteRecordInDatabase() {
        String result = alertSuspensionsLogic_.deleteRecordInDatabase("alertSuspension junit name 1");
        assertTrue(result.contains("success") || result.contains("Alert suspension not found"));
        
        Calendar seedCalendar = Calendar.getInstance();
        Calendar startDate = DateAndTime.getCalendarWithSameDateAtStartofDay(seedCalendar);
        Timestamp startDateTimestamp = new Timestamp(startDate.getTimeInMillis());
        Calendar startTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 21, 0, 0, 0);
        Timestamp startTimeTimestamp = new Timestamp(startTime.getTimeInMillis());
        Calendar endTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 22, 0, 0, 0);
        Timestamp endTimeTimestamp = new Timestamp(endTime.getTimeInMillis());
        
        // create & insert a AlertSuspension, insert it into the db, retrieve it from the db, & compare the original & retrieved records
        AlertSuspension alertSuspension1 = new AlertSuspension(
                -1, "alertSuspension junit name 1", true, AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS, null, "incl\ntag1\ntag2", "excl\ntag1\ntag2", 
                true, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, 40, endTimeTimestamp);
        
        result = alertSuspensionsLogic_.alterRecordInDatabase(alertSuspension1);
        assertTrue(result.contains("Success"));
        AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao();
        AlertSuspension alertSuspension1FromDb = alertSuspensionsDao.getAlertSuspensionByName("alertSuspension junit name 1");
        assertTrue(alertSuspension1FromDb.getName().contains("alertSuspension junit name 1"));
        assertTrue(alertSuspension1FromDb.getMetricGroupTagsInclusive().contains("incl\ntag1\ntag2"));
        
        result = alertSuspensionsLogic_.deleteRecordInDatabase("alertSuspension junit name 1");
        assertTrue(result.contains("success"));
        alertSuspensionsDao = new AlertSuspensionsDao();
        AlertSuspension alertSuspension2FromDb = alertSuspensionsDao.getAlertSuspensionByName("alertSuspension junit name 1");
        assertEquals(null, alertSuspension2FromDb);
        
        result = alertSuspensionsLogic_.deleteRecordInDatabase("alertSuspension junit fake 1");
        assertTrue(result.contains("Cancelling"));
    }
    
}
