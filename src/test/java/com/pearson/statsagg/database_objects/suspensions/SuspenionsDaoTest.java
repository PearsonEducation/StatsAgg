package com.pearson.statsagg.database_objects.suspensions;

import java.sql.Timestamp;
import java.util.Calendar;
import com.pearson.statsagg.controller.ContextManager;
import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import com.pearson.statsagg.database_engine.DatabaseConnections;
import com.pearson.statsagg.utilities.DateAndTime;
import java.io.InputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Jeffrey Schmidt
 */
public class SuspenionsDaoTest {
    
    private static Suspension suspension1_ = null;
    private static Suspension suspension2_ = null;

    public SuspenionsDaoTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        ContextManager contextManager = new ContextManager();
        InputStream ephemeralDatabaseConfiguration = contextManager.getEphemeralDatabaseConfiguration();
        contextManager.initializeDatabaseFromInputStream(ephemeralDatabaseConfiguration);
        contextManager.createDatabaseSchemas();
        
        setUpClass_Suspensions();
        deleteDataFromPreviousTest();
    }
    
    private static void setUpClass_Suspensions() {
        Calendar seedCalendar = Calendar.getInstance();
        
        Calendar startDate = DateAndTime.getCalendarWithSameDateAtStartofDay(seedCalendar);
        Timestamp startDateTimestamp = new Timestamp(startDate.getTimeInMillis());
        Calendar startTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 21, 0, 0, 0);
        Timestamp startTimeTimestamp = new Timestamp(startTime.getTimeInMillis());
        Calendar endTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 22, 0, 0, 0);
        Timestamp endTimeTimestamp = new Timestamp(endTime.getTimeInMillis());
        
        suspension1_ = new Suspension(
                -1, "Suspension JUnit 1", "desc", true, Suspension.SUSPEND_BY_METRIC_GROUP_TAGS, null, "incl\ntag1\ntag2", "excl\ntag1\ntag2", "metricSuspendRegex",
                true, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, (60000l * 40), DatabaseObjectCommon.TIME_UNIT_MINUTES, endTimeTimestamp);
        
        suspension2_ = new Suspension(
                -1, "Suspension JUnit 2", "desc", true, Suspension.SUSPEND_BY_ALERT_ID, null, null, null, "",
                true, true, true, true, false, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, (60000l * 40), DatabaseObjectCommon.TIME_UNIT_MINUTES, endTimeTimestamp);  
    }
    
    private static void deleteDataFromPreviousTest() {
        SuspensionsDao suspensionsDao = new SuspensionsDao(false);
        
        Suspension suspension1 = suspensionsDao.getSuspensionByName(suspension1_.getName());
        suspensionsDao.delete(suspension1);
        
        Suspension suspension2 = suspensionsDao.getSuspensionByName(suspension2_.getName());
        suspensionsDao.delete(suspension2);

        suspensionsDao.close();
    }
    
    @AfterClass
    public static void tearDownClass() {
        DatabaseConnections.disconnectAndShutdown();
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of dropTable method, of class SuspensionsDao.
     */
    @Test
    public void testDropTable() {
    }

    /**
     * Test of createTable method, of class SuspensionsDao.
     */
    @Test
    public void testCreateTable() {
    }

    /**
     * Test of getDatabaseObject method, of class SuspensionsDao.
     */
    @Test
    public void testGetDatabaseObject() {
    }

    /**
     * Test of insert, update, and delete methods, of class SuspensionsDao.
     */
    @Test
    public void testDML() {
        testInsert();
        testUpdate();
        testDelete();
    }
    
    /**
     * Test of insert method, of class SuspensionsDao.
     */
    public void testInsert() {
        // insert the records & check to see if the inserts reported successful insertion
        SuspensionsDao suspensionsDao = new SuspensionsDao(false);
        assertTrue(suspensionsDao.insert(suspension1_));
        assertTrue(suspensionsDao.insert(suspension2_));
        suspensionsDao.close();
        
        
        // validate the inserts were successful
        suspensionsDao = new SuspensionsDao(false);
        
        Suspension suspension1 = suspensionsDao.getSuspensionByName(suspension1_.getName());
        if (suspension1.getId() == -1) fail("suspension1.getId() == -1");
        else suspension1.setId(-1);
        assertTrue(suspension1.isEqual(suspension1_));
        assertFalse(suspension1.isEqual(suspension2_));
        
        Suspension suspension2 = suspensionsDao.getSuspensionByName(suspension2_.getName());
        if (suspension2.getId() == -1) fail("suspension2.getId() == -1");
        else suspension2.setId(-1);
        assertFalse(suspension2.isEqual(suspension1_));
        assertTrue(suspension2.isEqual(suspension2_));
        suspensionsDao.close();
    }

    /**
     * Test of update method, of class SuspensionsDao.
     */
    public void testUpdate() {
        // get the records, change a value, and update the records
        SuspensionsDao suspensionsDao = new SuspensionsDao(false);
        Suspension suspension1 = suspensionsDao.getSuspensionByName(suspension1_.getName());
        if (suspension1 == null) fail("suspension1 == null");
        Suspension suspension2 = suspensionsDao.getSuspensionByName(suspension2_.getName());
        if (suspension2 == null) fail("suspension2 == null");

        if ((suspension1 != null) && !suspension1.isRecurSunday()) fail("!suspension1.isRecurSunday()");
        else if (suspension1 != null) suspension1.setIsRecurSunday(false);
        suspensionsDao.update(suspension1);
        
        if ((suspension2 != null) && !suspension2.isRecurMonday()) fail("!suspension2.isRecurMonday()");
        else if (suspension2 != null) suspension2.setIsRecurMonday(false);
        suspensionsDao.update(suspension2);
        suspensionsDao.close();
        
        
        // verify the updates
        suspensionsDao = new SuspensionsDao(false);
        suspension1 = suspensionsDao.getSuspensionByName(suspension1_.getName());
        assertFalse(suspension1.isRecurSunday());
        
        suspension2 = suspensionsDao.getSuspensionByName(suspension2_.getName());
        assertFalse(suspension2.isRecurMonday());
        suspensionsDao.close();
    }

    /**
     * Test of delete method, of class SuspensionsDao.
     */
    public void testDelete() {
        // get the records & delete them
        SuspensionsDao suspensionsDao = new SuspensionsDao(false);
        Suspension suspension1 = suspensionsDao.getSuspensionByName(suspension1_.getName());
        Suspension suspension2 = suspensionsDao.getSuspensionByName(suspension2_.getName());
        
        assertTrue(suspensionsDao.delete(suspension1));
        assertTrue(suspensionsDao.delete(suspension2));
        suspensionsDao.close();
        
        
        // verify that the records were deleted
        suspensionsDao = new SuspensionsDao(false);
        suspension1 = suspensionsDao.getSuspensionByName(suspension1_.getName());
        assertEquals(suspension1, null);
        suspension2 = suspensionsDao.getSuspensionByName(suspension2_.getName());
        assertEquals(suspension2, null);
        suspensionsDao.close();
    }
    
}
