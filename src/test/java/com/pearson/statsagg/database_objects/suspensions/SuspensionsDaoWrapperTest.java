package com.pearson.statsagg.database_objects.suspensions;

import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.drivers.Driver;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.pearson.statsagg.utilities.time_utils.DateAndTime;
import java.sql.Connection;
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
public class SuspensionsDaoWrapperTest {

    public SuspensionsDaoWrapperTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        Driver.initializeApplication_Logger();
        Driver.initializeApplication_DatabaseConfiguration(true);
        Driver.connectToDatabase();
        Driver.setupDatabaseSchema();
    }
    
    @AfterClass
    public static void tearDownClass() {
        DatabaseConnections.disconnectAndShutdown();
    }
    
    @Before
    public void setUp() {
        // delete suspension that was inserted into the database from a previous test. verify that it was deleted.
        String result = SuspensionsDaoWrapper.deleteRecordInDatabase("suspension junit name 1").getReturnString();
        assertTrue(result.contains("success") || result.contains("The suspension was not found"));
        
        // delete a suspension that was inserted into the database from a previous test. verify that it was deleted.
        result = SuspensionsDaoWrapper.deleteRecordInDatabase("suspension junit name 11").getReturnString();
        assertTrue(result.contains("success") || result.contains("The suspension was not found"));
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of alterRecordInDatabase method, of class SuspensionsDaoWrapper.
     */
    @Test
    public void testAlterRecordInDatabase() {
        // cleanup from previous unit test
        String result = SuspensionsDaoWrapper.deleteRecordInDatabase("suspension junit name 1").getReturnString();
        assertTrue(result.contains("success") || result.contains("The suspension was not found"));
        
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
         
        result = SuspensionsDaoWrapper.createRecordInDatabase(suspension1).getReturnString();
        assertTrue(result.contains("Success"));
        Suspension suspension1FromDb = SuspensionsDao.getSuspension(DatabaseConnections.getConnection(), true, "suspension junit name 1");
        assertTrue(suspension1FromDb.getName().contains("suspension junit name 1"));
        assertTrue(suspension1FromDb.getMetricGroupTagsInclusive().contains("incl\ntag1\ntag2"));
        
        suspension1FromDb.setMetricGroupTagsInclusive("incl\ntag1\ntag2\ntag3");
        result = SuspensionsDaoWrapper.createRecordInDatabase(suspension1FromDb).getReturnString();
        assertTrue(result.contains("Fail"));
        result = SuspensionsDaoWrapper.alterRecordInDatabase(suspension1FromDb, suspension1FromDb.getName()).getReturnString();
        assertTrue(result.contains("Success"));
        Suspension suspension2FromDb = SuspensionsDao.getSuspension(DatabaseConnections.getConnection(), true,"suspension junit name 1");
        assertTrue(suspension2FromDb.getName().contains("suspension junit name 1"));
        assertTrue(suspension2FromDb.getMetricGroupTagsInclusive().contains("incl\ntag1\ntag2\ntag3"));
        
        result = SuspensionsDaoWrapper.deleteRecordInDatabase("suspension junit fake 1").getReturnString();
        assertTrue(result.contains("Cancelling"));
        Suspension suspension3FromDb = SuspensionsDao.getSuspension(DatabaseConnections.getConnection(), true, "suspension junit name 1");
        assertTrue(suspension3FromDb.getName().contains("suspension junit name 1"));
        
        // test altering suspension name
        Connection connection = DatabaseConnections.getConnection();
        Suspension suspensionFromDbOriginalName = SuspensionsDao.getSuspension(connection, false, "suspension junit name 1"); // pt1
        assertTrue(suspensionFromDbOriginalName.getName().contains("suspension junit name 1"));
        Suspension suspensionFromDbNewName = Suspension.copy(suspensionFromDbOriginalName);
        suspensionFromDbNewName.setName("suspension junit name 11");
        result = SuspensionsDaoWrapper.alterRecordInDatabase(suspensionFromDbNewName, suspensionFromDbOriginalName.getName()).getReturnString();
        assertTrue(result.contains("Successful"));
        Suspension suspensionFromDbNewNameVerify = SuspensionsDao.getSuspension(connection, false, suspensionFromDbNewName.getName()); // pt2
        assertTrue(suspensionFromDbNewNameVerify.getName().contains(suspensionFromDbNewName.getName()));
        assertFalse(suspensionFromDbNewNameVerify.getName().equals(suspensionFromDbOriginalName.getName()));
        assertEquals(suspensionFromDbOriginalName.getId(), suspensionFromDbNewNameVerify.getId());
        Suspension suspensionFromDbOriginalName_NoResult = SuspensionsDao.getSuspension(connection, false, suspensionFromDbOriginalName.getName()); // pt3
        assertEquals(suspensionFromDbOriginalName_NoResult, null);
        Suspension suspensionFromDbOriginalName_Reset = Suspension.copy(suspensionFromDbOriginalName); // pt4
        suspensionFromDbOriginalName_Reset.setName(suspensionFromDbOriginalName.getName());  
        result = SuspensionsDaoWrapper.alterRecordInDatabase(suspensionFromDbOriginalName_Reset, suspensionFromDbOriginalName.getName()).getReturnString();
        assertTrue(result.contains("Successful"));
        DatabaseUtils.cleanup(connection);
        
        result = SuspensionsDaoWrapper.deleteRecordInDatabase("suspension junit name 1").getReturnString();
        assertTrue(result.contains("success"));
        Suspension suspension4FromDb = SuspensionsDao.getSuspension(DatabaseConnections.getConnection(), true, "suspension junit name 1");
        assertEquals(null, suspension4FromDb);
    }

    /**
     * Test of deleteRecordInDatabase method, of class SuspensionsDaoWrapper.
     */
    @Test
    public void testDeleteRecordInDatabase() {
        String result = SuspensionsDaoWrapper.deleteRecordInDatabase("suspension junit name 1").getReturnString();
        assertTrue(result.contains("success") || result.contains("The suspension was not found"));
        
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
        
        result = SuspensionsDaoWrapper.createRecordInDatabase(suspension1).getReturnString();
        assertTrue(result.contains("Success"));
        Suspension suspension1FromDb = SuspensionsDao.getSuspension(DatabaseConnections.getConnection(), true, "suspension junit name 1");
        assertTrue(suspension1FromDb.getName().contains("suspension junit name 1"));
        assertTrue(suspension1FromDb.getMetricGroupTagsInclusive().contains("incl\ntag1\ntag2"));
        
        result = SuspensionsDaoWrapper.deleteRecordInDatabase("suspension junit name 1").getReturnString();
        assertTrue(result.contains("success"));
        Suspension suspension2FromDb = SuspensionsDao.getSuspension(DatabaseConnections.getConnection(), true, "suspension junit name 1");
        assertEquals(null, suspension2FromDb);
        
        result = SuspensionsDaoWrapper.deleteRecordInDatabase("suspension junit fake 1").getReturnString();
        assertTrue(result.contains("Cancelling"));
    }
    
}
