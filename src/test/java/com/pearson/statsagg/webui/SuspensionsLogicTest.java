package com.pearson.statsagg.webui;

import com.pearson.statsagg.web_ui.SuspensionsLogic;
import com.pearson.statsagg.controller.ContextManager;
import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import com.pearson.statsagg.database_objects.suspensions.Suspension;
import com.pearson.statsagg.database_objects.suspensions.SuspensionsDao;
import com.pearson.statsagg.database_engine.DatabaseConnections;
import com.pearson.statsagg.utilities.time_utils.DateAndTime;
import java.io.InputStream;
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
public class SuspensionsLogicTest {
    
    private final SuspensionsLogic suspensionsLogic_ = new SuspensionsLogic();
    
    public SuspensionsLogicTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        ContextManager contextManager = new ContextManager();
        InputStream ephemeralDatabaseConfiguration = contextManager.getEphemeralDatabaseConfiguration();
        contextManager.initializeDatabaseFromInputStream(ephemeralDatabaseConfiguration);
        contextManager.createDatabaseSchemas();
    }
    
    @AfterClass
    public static void tearDownClass() {
        DatabaseConnections.disconnectAndShutdown();
    }
    
    @Before
    public void setUp() {
        // delete suspension that was inserted into the database from a previous test. verify that it was deleted.
        String result = suspensionsLogic_.deleteRecordInDatabase("suspension junit name 1");
        assertTrue(result.contains("success") || result.contains("Suspension not found"));
        
        // delete a suspension that was inserted into the database from a previous test. verify that it was deleted.
        result = suspensionsLogic_.deleteRecordInDatabase("suspension junit name 11");
        assertTrue(result.contains("success") || result.contains("Suspension not found"));
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of alterRecordInDatabase method, of class SuspensionsLogic.
     */
    @Test
    public void testAlterRecordInDatabase() {
        // cleanup from previous unit test
        String result = suspensionsLogic_.deleteRecordInDatabase("suspension junit name 1");
        assertTrue(result.contains("success") || result.contains("Suspension not found"));
        
        Calendar seedCalendar = Calendar.getInstance();
        Calendar startDate = DateAndTime.getCalendarWithSameDateAtStartofDay(seedCalendar);
        Timestamp startDateTimestamp = new Timestamp(startDate.getTimeInMillis());
        Calendar startTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 21, 0, 0, 0);
        Timestamp startTimeTimestamp = new Timestamp(startTime.getTimeInMillis());
        Calendar endTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 22, 0, 0, 0);
        Timestamp endTimeTimestamp = new Timestamp(endTime.getTimeInMillis());
        
        // create & insert a suspension, insert it into the db, retrieve it from the db, & compare the original & retrieved records
        Suspension suspension1 = new Suspension(
                -1, "suspension junit name 1", "desc", true, Suspension.SUSPEND_BY_METRIC_GROUP_TAGS, null, "incl\ntag1\ntag2", "excl\ntag1\ntag2", "",
                true, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, (60000l * 40), DatabaseObjectCommon.TIME_UNIT_MINUTES, endTimeTimestamp);
         
        result = suspensionsLogic_.alterRecordInDatabase(suspension1);
        assertTrue(result.contains("Success"));
        SuspensionsDao suspensionsDao = new SuspensionsDao();
        Suspension suspension1FromDb = suspensionsDao.getSuspensionByName("suspension junit name 1");
        assertTrue(suspension1FromDb.getName().contains("suspension junit name 1"));
        assertTrue(suspension1FromDb.getMetricGroupTagsInclusive().contains("incl\ntag1\ntag2"));
        
        suspension1FromDb.setMetricGroupTagsInclusive("incl\ntag1\ntag2\ntag3");
        result = suspensionsLogic_.alterRecordInDatabase(suspension1FromDb);
        assertTrue(result.contains("Fail"));
        result = suspensionsLogic_.alterRecordInDatabase(suspension1FromDb, suspension1FromDb.getName());
        assertTrue(result.contains("Success"));
        suspensionsDao = new SuspensionsDao();
        Suspension suspension2FromDb = suspensionsDao.getSuspensionByName("suspension junit name 1");
        assertTrue(suspension2FromDb.getName().contains("suspension junit name 1"));
        assertTrue(suspension2FromDb.getMetricGroupTagsInclusive().contains("incl\ntag1\ntag2\ntag3"));
        
        result = suspensionsLogic_.deleteRecordInDatabase("suspension junit fake 1");
        assertTrue(result.contains("Cancelling"));
        suspensionsDao = new SuspensionsDao();
        Suspension suspension3FromDb = suspensionsDao.getSuspensionByName("suspension junit name 1");
        assertTrue(suspension3FromDb.getName().contains("suspension junit name 1"));
        
        // test altering suspension name
        suspensionsDao = new SuspensionsDao(false);
        Suspension suspensionFromDbOriginalName = suspensionsDao.getSuspensionByName("suspension junit name 1"); // pt1
        assertTrue(suspensionFromDbOriginalName.getName().contains("suspension junit name 1"));
        Suspension suspensionFromDbNewName = Suspension.copy(suspensionFromDbOriginalName);
        suspensionFromDbNewName.setName("suspension junit name 11");
        result = suspensionsLogic_.alterRecordInDatabase(suspensionFromDbNewName, suspensionFromDbNewName.getName());
        assertTrue(result.contains("Successful"));
        Suspension suspensionFromDbNewNameVerify = suspensionsDao.getSuspensionByName(suspensionFromDbNewName.getName()); // pt2
        assertTrue(suspensionFromDbNewNameVerify.getName().contains(suspensionFromDbNewName.getName()));
        assertFalse(suspensionFromDbNewNameVerify.getName().equals(suspensionFromDbOriginalName.getName()));
        assertEquals(suspensionFromDbOriginalName.getId(), suspensionFromDbNewNameVerify.getId());
        Suspension suspensionFromDbOriginalName_NoResult = suspensionsDao.getSuspensionByName(suspensionFromDbOriginalName.getName()); // pt3
        assertEquals(suspensionFromDbOriginalName_NoResult, null);
        Suspension suspensionFromDbOriginalName_Reset = Suspension.copy(suspensionFromDbOriginalName); // pt4
        suspensionFromDbOriginalName_Reset.setName(suspensionFromDbOriginalName.getName());  
        result = suspensionsLogic_.alterRecordInDatabase(suspensionFromDbOriginalName_Reset, suspensionFromDbOriginalName.getName());
        assertTrue(result.contains("Successful"));
        suspensionsDao.close();
        
        result = suspensionsLogic_.deleteRecordInDatabase("suspension junit name 1");
        assertTrue(result.contains("success"));
        suspensionsDao = new SuspensionsDao();
        Suspension suspension4FromDb = suspensionsDao.getSuspensionByName("suspension junit name 1");
        assertEquals(null, suspension4FromDb);
    }

    /**
     * Test of deleteRecordInDatabase method, of class SuspensionsLogic.
     */
    @Test
    public void testDeleteRecordInDatabase() {
        String result = suspensionsLogic_.deleteRecordInDatabase("suspension junit name 1");
        assertTrue(result.contains("success") || result.contains("Suspension not found"));
        
        Calendar seedCalendar = Calendar.getInstance();
        Calendar startDate = DateAndTime.getCalendarWithSameDateAtStartofDay(seedCalendar);
        Timestamp startDateTimestamp = new Timestamp(startDate.getTimeInMillis());
        Calendar startTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 21, 0, 0, 0);
        Timestamp startTimeTimestamp = new Timestamp(startTime.getTimeInMillis());
        Calendar endTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 22, 0, 0, 0);
        Timestamp endTimeTimestamp = new Timestamp(endTime.getTimeInMillis());
        
        // create & insert a suspension, insert it into the db, retrieve it from the db, & compare the original & retrieved records
        Suspension suspension1 = new Suspension(
                -1, "suspension junit name 1", "desc", true, Suspension.SUSPEND_BY_METRIC_GROUP_TAGS, null, "incl\ntag1\ntag2", "excl\ntag1\ntag2", "",
                true, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, (60000l * 40), DatabaseObjectCommon.TIME_UNIT_MINUTES, endTimeTimestamp);
        
        result = suspensionsLogic_.alterRecordInDatabase(suspension1);
        assertTrue(result.contains("Success"));
        SuspensionsDao suspensionsDao = new SuspensionsDao();
        Suspension suspension1FromDb = suspensionsDao.getSuspensionByName("suspension junit name 1");
        assertTrue(suspension1FromDb.getName().contains("suspension junit name 1"));
        assertTrue(suspension1FromDb.getMetricGroupTagsInclusive().contains("incl\ntag1\ntag2"));
        
        result = suspensionsLogic_.deleteRecordInDatabase("suspension junit name 1");
        assertTrue(result.contains("success"));
        suspensionsDao = new SuspensionsDao();
        Suspension suspension2FromDb = suspensionsDao.getSuspensionByName("suspension junit name 1");
        assertEquals(null, suspension2FromDb);
        
        result = suspensionsLogic_.deleteRecordInDatabase("suspension junit fake 1");
        assertTrue(result.contains("Cancelling"));
    }
    
}
