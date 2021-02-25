package com.pearson.statsagg.database_objects.alerts;

import com.pearson.statsagg.database_objects.metric_groups.MetricGroupsDaoWrapper;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroupsDaoWrapper;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.TreeSet;
import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroup;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupsDao;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroup;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroupsDao;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.drivers.Driver;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.Connection;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Jeffrey Schmidt
 */
public class AlertsDaoWrapperTest {
    
    private MetricGroup metricGroup_ = null;
    private static final String metricGroupName_ = "JUnit - MetricGroup for AlertsDaoWrapperTest";
    private NotificationGroup notificationGroup_ = null;
    private static final String notificationGroupName_ = "JUnit - NotificationGroup for AlertsDaoWrapperTest";

    public AlertsDaoWrapperTest() {
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
        // delete an alert that was inserted into the database from a previous test. verify that it was deleted.
        String result = AlertsDaoWrapper.deleteRecordInDatabase("alert junit 1").getReturnString();
        assertTrue(result.contains("success") || result.contains("The alert was not found"));
        
        // delete an alert that was inserted into the database from a previous test. verify that it was deleted.
        result = AlertsDaoWrapper.deleteRecordInDatabase("alert junit 1_1").getReturnString();
        assertTrue(result.contains("success") || result.contains("The alert was not found"));
        
        // delete previous notification group, create a new notification group, and retrieve it from the db. this notification group exists solely for this unit test.
        result = NotificationGroupsDaoWrapper.deleteRecordInDatabase(notificationGroupName_).getReturnString();
        assertTrue(result.contains("success") || result.contains("The notification group was not found"));
        NotificationGroup notificationGroup = new NotificationGroup(-1, notificationGroupName_, null, null, notificationGroupName_, null);   
        result = NotificationGroupsDaoWrapper.createRecordInDatabase(notificationGroup).getReturnString();
        assertTrue(result.contains("Success"));
        notificationGroup_ = NotificationGroupsDao.getNotificationGroup(DatabaseConnections.getConnection(), true, notificationGroupName_);

        // delete previous metric group, create a new metric group, and retrieve it from the db. this metric group exists solely for this unit test.
        result = MetricGroupsDaoWrapper.deleteRecordInDatabase(metricGroupName_).getReturnString();
        assertTrue(result.contains("success") || result.contains("The metric group was not found"));
        TreeSet<String> regexes = new TreeSet<>();
        regexes.add(metricGroupName_);
        TreeSet<String> tags = new TreeSet<>();
        regexes.add(metricGroupName_);
        MetricGroup metricGroup = new MetricGroup(-1, metricGroupName_, metricGroupName_, null, null, regexes, regexes, tags);   
        result = MetricGroupsDaoWrapper.createRecordInDatabase(metricGroup).getReturnString();
        assertTrue(result.contains("Success"));
        metricGroup_ = MetricGroupsDao.getMetricGroup(DatabaseConnections.getConnection(), true, metricGroupName_);
    }
    
    @After
    public void tearDown() {
        // delete notification group. this notification group existed solely for this unit test, so it is safe to delete.
        String result = NotificationGroupsDaoWrapper.deleteRecordInDatabase(notificationGroupName_).getReturnString();
        assertTrue(result.contains("success"));
        
        // delete metric group. this metric group existed solely for this unit test, so it is safe to delete.
        result = MetricGroupsDaoWrapper.deleteRecordInDatabase(metricGroupName_).getReturnString();
        assertTrue(result.contains("success"));
    }

    /**
     * Test of alterRecordInDatabase method, of class AlertsDaoWrapperTest.
     */
    @Test
    public void testAlterRecordInDatabase() {
        // cleanup from previous unit test
        String result = AlertsDaoWrapper.deleteRecordInDatabase("alert junit 1").getReturnString();
        assertTrue(result.contains("success") || result.contains("The alert was not found"));
        
        // create & insert an Alert, insert it into the db, retrieve it from the db, & check for correctness of the retrieved records
        Alert alert1 = new Alert(1, "alert junit 1", "alert junit 1" , null, null, metricGroup_.getId(), false, true, true, Alert.TYPE_THRESHOLD, true, true, 300000l, DatabaseObjectCommon.TIME_UNIT_SECONDS, 
            notificationGroup_.getId(), notificationGroup_.getId(), Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 1, false, new Timestamp(System.currentTimeMillis()), false, null, null,
            notificationGroup_.getId(), notificationGroup_.getId(), Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("200"), 1000L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 2, true, new Timestamp(System.currentTimeMillis()), false, null, null);
        result = AlertsDaoWrapper.createRecordInDatabase(alert1).getReturnString();
        assertTrue(result.contains("Success"));
        Alert alert1FromDb = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, "alert junit 1");
        assertTrue(alert1FromDb.getName().contains("alert junit 1"));
        assertTrue(alert1FromDb.getDescription().contains("alert junit 1"));
        
        // alter the alert description. update_Name the record in the db, retrieve it from the db, & check for correctness of the retrieved records
        alert1FromDb.setDescription("alert junit 1_2");
        result = AlertsDaoWrapper.createRecordInDatabase(alert1FromDb).getReturnString();
        assertTrue(result.contains("Failed"));
        result = AlertsDaoWrapper.alterRecordInDatabase(alert1FromDb, alert1FromDb.getName(), false).getReturnString();
        assertTrue(result.contains("Success"));
        Alert alert2FromDb = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, "alert junit 1");
        assertTrue(alert2FromDb.getName().contains("alert junit 1"));
        assertTrue(alert2FromDb.getDescription().contains("alert junit 1_2"));
        
        // attempt to delete an alert in the database that doesn't exist. make sure it didn't delete the record that was inserted into the db earlier.
        result = AlertsDaoWrapper.deleteRecordInDatabase("alert junit fake 1").getReturnString();
        assertTrue(result.contains("Cancelling"));
        Alert alert3FromDb = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, "alert junit 1");
        assertTrue(alert3FromDb.getName().contains("alert junit 1"));
        
        // alters an alert's name, make sure it got properly updated in the db, then changes the alert name back to what it was originally named
        Connection connection = DatabaseConnections.getConnection();
        DatabaseUtils.setAutoCommit(connection, false);
        Alert alertFromDbOriginalName = AlertsDao.getAlert(connection, false, "alert junit 1"); //pt 1
        assertTrue(alertFromDbOriginalName.getName().contains("alert junit 1"));
        Alert alertFromDbNewName = Alert.copy(alertFromDbOriginalName);
        alertFromDbNewName.setName("alert junit 1_1");
        result = AlertsDaoWrapper.alterRecordInDatabase(alertFromDbNewName, alertFromDbOriginalName.getName(), false).getReturnString();
        assertTrue(result.contains("Successful"));
        Alert alertFromDbNewNameVerify = AlertsDao.getAlert(connection, false, alertFromDbNewName.getName()); //pt2
        assertFalse(alertFromDbNewNameVerify.getName().equals(alertFromDbOriginalName.getName()));
        assertTrue(alertFromDbNewNameVerify.getName().contains(alertFromDbNewName.getName()));
        assertEquals(alertFromDbOriginalName.getId(), alertFromDbNewNameVerify.getId());
        Alert alertFromDbOriginalName_NoResult = AlertsDao.getAlert(connection, false, alertFromDbOriginalName.getName()); //pt3
        assertEquals(alertFromDbOriginalName_NoResult, null);
        Alert alertFromDbOriginalName_Reset = Alert.copy(alertFromDbOriginalName); // pt4
        alertFromDbOriginalName_Reset.setName(alertFromDbOriginalName.getName());  
        result = AlertsDaoWrapper.alterRecordInDatabase(alertFromDbOriginalName_Reset, alertFromDbOriginalName.getName(), false).getReturnString();
        assertTrue(result.contains("Successful"));
        DatabaseUtils.commit(connection);
        DatabaseUtils.cleanup(connection);
        
        // delete the alert that was inserted into the database earlier. verify that it was deleted.
        result = AlertsDaoWrapper.deleteRecordInDatabase("alert junit 1").getReturnString();
        assertTrue(result.contains("success"));
        Alert alert7FromDb = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, "alert junit 1");
        assertEquals(null, alert7FromDb);
        
        // alter existing alert, no name change.  delete when done.
        Alert alert8FromDb = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, "alert junit 1_1");
        alert8FromDb.setDescription("taco taco taco");
        result = AlertsDaoWrapper.alterRecordInDatabase(alert8FromDb).getReturnString();
        assertTrue(result.contains("Successful"));
        Alert alert9FromDb = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, "alert junit 1_1");
        assertTrue(alert9FromDb.getDescription().equals("taco taco taco"));
        result = AlertsDaoWrapper.deleteRecordInDatabase("alert junit 1_1").getReturnString();
        assertTrue(result.contains("success"));
    }

    /**
     * Test of deleteRecordInDatabase method, of class AlertsDaoWrapperTest.
     */
    @Test
    public void testDeleteRecordInDatabase() {
        // cleanup from previous unit test
        String result = AlertsDaoWrapper.deleteRecordInDatabase("alert junit name 1").getReturnString();
        assertTrue(result.contains("success") || result.contains("The alert was not found"));
        
        Alert alert1 = new Alert(1, "alert junit 1", "alert junit 1" , null, null, metricGroup_.getId(), false, true, true, Alert.TYPE_THRESHOLD, true, true, 300000l, DatabaseObjectCommon.TIME_UNIT_SECONDS, 
            notificationGroup_.getId(), notificationGroup_.getId(), Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 1, false, new Timestamp(System.currentTimeMillis()), false, null, null,
            notificationGroup_.getId(), notificationGroup_.getId(), Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("200"), 1000L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 2, true, new Timestamp(System.currentTimeMillis()), false, null, null);
        result = AlertsDaoWrapper.createRecordInDatabase(alert1).getReturnString();
        assertTrue(result.contains("Success"));
        Alert alert1FromDb = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, "alert junit 1");
        assertTrue(alert1FromDb.getName().contains("alert junit 1"));
        assertTrue(alert1FromDb.getDescription().contains("alert junit 1"));
        
        result = AlertsDaoWrapper.deleteRecordInDatabase("alert junit 1").getReturnString();
        assertTrue(result.contains("success"));
        Alert alert2FromDb = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, "alert junit 1");
        assertEquals(null, alert2FromDb);
        
        result = AlertsDaoWrapper.deleteRecordInDatabase("alert junit fake 1").getReturnString();
        assertTrue(result.contains("Cancelling"));
    }
    
}
