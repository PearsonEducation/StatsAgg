package com.pearson.statsagg.database.alert_suspensions;

import java.sql.Timestamp;
import java.util.Calendar;
import com.pearson.statsagg.controller.ContextManager;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.utilities.DateAndTime;
import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Jeffrey Schmidt
 */
public class AlertSuspensionsDaoTest {
    
    private static AlertSuspension alertSuspension1_ = null;
    private static AlertSuspension alertSuspension2_ = null;

    public AlertSuspensionsDaoTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        ContextManager ContextManager = new ContextManager();
        ContextManager.initializeDatabaseFromFile(System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator +
                "webapp" + File.separator + "WEB-INF" + File.separator + "config", "database.properties");
        
        setUpClass_AlertSuspensions();
        deleteDataFromPreviousTest();
    }
    
    private static void setUpClass_AlertSuspensions() {
        Calendar seedCalendar = Calendar.getInstance();
        
        Calendar startDate = DateAndTime.getCalendarWithSameDateAtStartofDay(seedCalendar);
        Timestamp startDateTimestamp = new Timestamp(startDate.getTimeInMillis());
        Calendar startTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 21, 0, 0, 0);
        Timestamp startTimeTimestamp = new Timestamp(startTime.getTimeInMillis());
        Calendar endTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(seedCalendar, 22, 0, 0, 0);
        Timestamp endTimeTimestamp = new Timestamp(endTime.getTimeInMillis());
        
        alertSuspension1_ = new AlertSuspension(
                -1, "AlertSuspension JUnit 1", true, AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS, null, "incl\ntag1\ntag2", "excl\ntag1\ntag2", 
                true, true, true, true, true, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, 40, endTimeTimestamp);
        
        alertSuspension2_ = new AlertSuspension(
                -1, "AlertSuspension JUnit 2", true, AlertSuspension.SUSPEND_BY_ALERT_ID, null, null, null,
                true, true, true, true, false, true, true, true, true, 
                startDateTimestamp, startTimeTimestamp, 40, endTimeTimestamp);  
    }
    
    private static void deleteDataFromPreviousTest() {
        AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao(false);
        
        AlertSuspension alertSuspension1 = alertSuspensionsDao.getAlertSuspensionByName(alertSuspension1_.getName());
        alertSuspensionsDao.delete(alertSuspension1);
        
        AlertSuspension alertSuspension2 = alertSuspensionsDao.getAlertSuspensionByName(alertSuspension2_.getName());
        alertSuspensionsDao.delete(alertSuspension2);

        alertSuspensionsDao.close();
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
     * Test of dropTable method, of class AlertSuspensionsDao.
     */
    @Test
    public void testDropTable() {
    }

    /**
     * Test of createTable method, of class AlertSuspensionsDao.
     */
    @Test
    public void testCreateTable() {
    }

    /**
     * Test of getDatabaseObject method, of class AlertSuspensionsDao.
     */
    @Test
    public void testGetDatabaseObject() {
    }

    /**
     * Test of insert, update, and delete methods, of class AlertSuspensionsDao.
     */
    @Test
    public void testDML() {
        testInsert();
        testUpdate();
        testDelete();
    }
    
    /**
     * Test of insert method, of class AlertSuspensionsDao.
     */
    public void testInsert() {
        // insert the records & check to see if the inserts reported successful insertion
        AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao(false);
        assertTrue(alertSuspensionsDao.insert(alertSuspension1_));
        assertTrue(alertSuspensionsDao.insert(alertSuspension2_));
        alertSuspensionsDao.close();
        
        
        // validate the inserts were successful
        alertSuspensionsDao = new AlertSuspensionsDao(false);
        
        AlertSuspension alertSuspension1 = alertSuspensionsDao.getAlertSuspensionByName(alertSuspension1_.getName());
        if (alertSuspension1.getId() == -1) fail("alertSuspension1.getId() == -1");
        else alertSuspension1.setId(-1);
        assertTrue(alertSuspension1.isEqual(alertSuspension1_));
        assertFalse(alertSuspension1.isEqual(alertSuspension2_));
        
        AlertSuspension alertSuspension2 = alertSuspensionsDao.getAlertSuspensionByName(alertSuspension2_.getName());
        if (alertSuspension2.getId() == -1) fail("alertSuspension2.getId() == -1");
        else alertSuspension2.setId(-1);
        assertFalse(alertSuspension2.isEqual(alertSuspension1_));
        assertTrue(alertSuspension2.isEqual(alertSuspension2_));
        alertSuspensionsDao.close();
    }

    /**
     * Test of update method, of class AlertSuspensionsDao.
     */
    public void testUpdate() {
        // get the records, change a value, and update the records
        AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao(false);
        AlertSuspension alertSuspension1 = alertSuspensionsDao.getAlertSuspensionByName(alertSuspension1_.getName());
        if (alertSuspension1 == null) fail("alertSuspension1 == null");
        AlertSuspension alertSuspension2 = alertSuspensionsDao.getAlertSuspensionByName(alertSuspension2_.getName());
        if (alertSuspension2 == null) fail("alertSuspension2 == null");

        if ((alertSuspension1 != null) && !alertSuspension1.isRecurSunday()) fail("!alertSuspension1.isRecurSunday()");
        else if (alertSuspension1 != null) alertSuspension1.setIsRecurSunday(false);
        alertSuspensionsDao.update(alertSuspension1);
        
        if ((alertSuspension2 != null) && !alertSuspension2.isRecurMonday()) fail("!alertSuspension2.isRecurMonday()");
        else if (alertSuspension2 != null) alertSuspension2.setIsRecurMonday(false);
        alertSuspensionsDao.update(alertSuspension2);
        alertSuspensionsDao.close();
        
        
        // verify the updates
        alertSuspensionsDao = new AlertSuspensionsDao(false);
        alertSuspension1 = alertSuspensionsDao.getAlertSuspensionByName(alertSuspension1_.getName());
        assertFalse(alertSuspension1.isRecurSunday());
        
        alertSuspension2 = alertSuspensionsDao.getAlertSuspensionByName(alertSuspension2_.getName());
        assertFalse(alertSuspension2.isRecurMonday());
        alertSuspensionsDao.close();
    }

    /**
     * Test of delete method, of class AlertSuspensionsDao.
     */
    public void testDelete() {
        // get the records & delete them
        AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao(false);
        AlertSuspension alertSuspension1 = alertSuspensionsDao.getAlertSuspensionByName(alertSuspension1_.getName());
        AlertSuspension alertSuspension2 = alertSuspensionsDao.getAlertSuspensionByName(alertSuspension2_.getName());
        
        assertTrue(alertSuspensionsDao.delete(alertSuspension1));
        assertTrue(alertSuspensionsDao.delete(alertSuspension2));
        alertSuspensionsDao.close();
        
        
        // verify that the records were deleted
        alertSuspensionsDao = new AlertSuspensionsDao(false);
        alertSuspension1 = alertSuspensionsDao.getAlertSuspensionByName(alertSuspension1_.getName());
        assertEquals(alertSuspension1, null);
        alertSuspension2 = alertSuspensionsDao.getAlertSuspensionByName(alertSuspension2_.getName());
        assertEquals(alertSuspension2, null);
        alertSuspensionsDao.close();
    }
    
}
