package com.pearson.statsagg.webui;

import com.pearson.statsagg.web_ui.MetricGroupsLogic;
import com.pearson.statsagg.web_ui.NotificationGroupsLogic;
import com.pearson.statsagg.web_ui.AlertsLogic;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.TreeSet;
import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroup;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupsDao;
import com.pearson.statsagg.database_objects.notifications.NotificationGroup;
import com.pearson.statsagg.database_objects.notifications.NotificationGroupsDao;
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
public class AlertsLogicTest {
    
    private MetricGroup metricGroup_ = null;
    private static final String metricGroupName_ = "JUnit - MetricGroup for AlertsLogicTest";
    private NotificationGroup notificationGroup_ = null;
    private static final String notificationGroupName_ = "JUnit - NotificationGroup for AlertsLogicTest";

    private final AlertsLogic alertsLogic_ = new AlertsLogic();
    private final MetricGroupsLogic metricGroupsLogic_ = new MetricGroupsLogic();
    private final NotificationGroupsLogic notificationGroupsLogic_ = new NotificationGroupsLogic();
    
    public AlertsLogicTest() {
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
        String result = alertsLogic_.deleteRecordInDatabase("alert junit 1");
        assertTrue(result.contains("success") || result.contains("Alert not found"));
        
        // delete an alert that was inserted into the database from a previous test. verify that it was deleted.
        result = alertsLogic_.deleteRecordInDatabase("alert junit 1_1");
        assertTrue(result.contains("success") || result.contains("Alert not found"));
        
        // delete previous notification group, create a new notification group, and retrieve it from the db. this notification group exists solely for this unit test.
        result = notificationGroupsLogic_.deleteRecordInDatabase(notificationGroupName_);
        assertTrue(result.contains("success") || result.contains("Notification group not found"));
        NotificationGroup notificationGroup = new NotificationGroup(-1, notificationGroupName_, notificationGroupName_);   
        result = notificationGroupsLogic_.alterRecordInDatabase(notificationGroup);
        assertTrue(result.contains("Success"));
        notificationGroup_ = NotificationGroupsDao.getNotificationGroup(DatabaseConnections.getConnection(), true, notificationGroupName_);
        
        // delete previous metric group, create a new metric group, and retrieve it from the db. this metric group exists solely for this unit test.
        result = metricGroupsLogic_.deleteRecordInDatabase(metricGroupName_);
        assertTrue(result.contains("success") || result.contains("Metric group not found"));
        MetricGroup metricGroup = new MetricGroup(-1, metricGroupName_, metricGroupName_);   
        TreeSet<String> regexes = new TreeSet<>();
        regexes.add(metricGroupName_);
        TreeSet<String> tags = new TreeSet<>();
        regexes.add(metricGroupName_);
        result = metricGroupsLogic_.alterRecordInDatabase(metricGroup, regexes, null, tags);
        assertTrue(result.contains("Success"));
        metricGroup_ = MetricGroupsDao.getMetricGroup(DatabaseConnections.getConnection(), true, metricGroupName_);
    }
    
    @After
    public void tearDown() {
        // delete notification group. this notification group existed solely for this unit test, so it is safe to delete.
        String result = notificationGroupsLogic_.deleteRecordInDatabase(notificationGroupName_);
        assertTrue(result.contains("success"));
        
        // delete metric group. this metric group existed solely for this unit test, so it is safe to delete.
        result = metricGroupsLogic_.deleteRecordInDatabase(metricGroupName_);
        assertTrue(result.contains("success"));
    }

    /**
     * Test of alterRecordInDatabase method, of class AlertsLogic.
     */
    @Test
    public void testAlterRecordInDatabase() {
        // cleanup from previous unit test
        String result = alertsLogic_.deleteRecordInDatabase("alert junit 1");
        assertTrue(result.contains("success") || result.contains("Alert not found"));
        
        // create & insert an Alert, insert it into the db, retrieve it from the db, & check for correctness of the retrieved records
        Alert alert1 = new Alert(1, "alert junit 1", false, false, "alert junit 1" , metricGroup_.getId(), false, true, true, Alert.TYPE_THRESHOLD, true, true, 300000l, DatabaseObjectCommon.TIME_UNIT_SECONDS, 
            notificationGroup_.getId(), notificationGroup_.getId(), Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 1, false, new Timestamp(System.currentTimeMillis()), false, null, null,
            notificationGroup_.getId(), notificationGroup_.getId(), Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("200"), 1000L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 2, true, new Timestamp(System.currentTimeMillis()), false, null, null);
        result = alertsLogic_.alterRecordInDatabase(alert1);
        assertTrue(result.contains("Success"));
        Alert alert1FromDb = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, "alert junit 1");
        assertTrue(alert1FromDb.getName().contains("alert junit 1"));
        assertTrue(alert1FromDb.getDescription().contains("alert junit 1"));
        
        // alter the alert description. update the record in the db, retrieve it from the db, & check for correctness of the retrieved records
        alert1FromDb.setDescription("alert junit 1_2");
        result = alertsLogic_.alterRecordInDatabase(alert1FromDb);
        assertTrue(result.contains("Failed"));
        result = alertsLogic_.alterRecordInDatabase(alert1FromDb, alert1FromDb.getName(), false);
        assertTrue(result.contains("Success"));
        Alert alert2FromDb = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, "alert junit 1");
        assertTrue(alert2FromDb.getName().contains("alert junit 1"));
        assertTrue(alert2FromDb.getDescription().contains("alert junit 1_2"));
        
        // attempt to delete an alert in the database that doesn't exist. make sure it didn't delete the record that was inserted into the db earlier.
        result = alertsLogic_.deleteRecordInDatabase("alert junit fake 1");
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
        result = alertsLogic_.alterRecordInDatabase(alertFromDbNewName, alertFromDbOriginalName.getName(), false);
        assertTrue(result.contains("Successful"));
        Alert alertFromDbNewNameVerify = AlertsDao.getAlert(connection, false, alertFromDbNewName.getName()); //pt2
        assertFalse(alertFromDbNewNameVerify.getName().equals(alertFromDbOriginalName.getName()));
        assertTrue(alertFromDbNewNameVerify.getName().contains(alertFromDbNewName.getName()));
        assertEquals(alertFromDbOriginalName.getId(), alertFromDbNewNameVerify.getId());
        Alert alertFromDbOriginalName_NoResult = AlertsDao.getAlert(connection, false, alertFromDbOriginalName.getName()); //pt3
        assertEquals(alertFromDbOriginalName_NoResult, null);
        Alert alertFromDbOriginalName_Reset = Alert.copy(alertFromDbOriginalName); // pt4
        alertFromDbOriginalName_Reset.setName(alertFromDbOriginalName.getName());  
        result = alertsLogic_.alterRecordInDatabase(alertFromDbOriginalName_Reset, alertFromDbOriginalName.getName(), false);
        assertTrue(result.contains("Successful"));
        DatabaseUtils.cleanup(connection);
        
        // delete the alert that was inserted into the database earlier. verify that it was deleted.
        result = alertsLogic_.deleteRecordInDatabase("alert junit 1");
        assertTrue(result.contains("success"));
        Alert alert7FromDb = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, "alert junit 1");
        assertEquals(null, alert7FromDb);
    }

    /**
     * Test of deleteRecordInDatabase method, of class AlertsLogic.
     */
    @Test
    public void testDeleteRecordInDatabase() {
        // cleanup from previous unit test
        String result = alertsLogic_.deleteRecordInDatabase("alert junit name 1");
        assertTrue(result.contains("success") || result.contains("Alert not found"));
        
        Alert alert1 = new Alert(1, "alert junit 1", false, false, "alert junit 1" , metricGroup_.getId(), false, true, true, Alert.TYPE_THRESHOLD, true, true, 300000l, DatabaseObjectCommon.TIME_UNIT_SECONDS, 
            notificationGroup_.getId(), notificationGroup_.getId(), Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 1, false, new Timestamp(System.currentTimeMillis()), false, null, null,
            notificationGroup_.getId(), notificationGroup_.getId(), Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("200"), 1000L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 2, true, new Timestamp(System.currentTimeMillis()), false, null, null);
        result = alertsLogic_.alterRecordInDatabase(alert1);
        assertTrue(result.contains("Success"));
        Alert alert1FromDb = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, "alert junit 1");
        assertTrue(alert1FromDb.getName().contains("alert junit 1"));
        assertTrue(alert1FromDb.getDescription().contains("alert junit 1"));
        
        result = alertsLogic_.deleteRecordInDatabase("alert junit 1");
        assertTrue(result.contains("success"));
        Alert alert2FromDb = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, "alert junit 1");
        assertEquals(null, alert2FromDb);
        
        result = alertsLogic_.deleteRecordInDatabase("alert junit fake 1");
        assertTrue(result.contains("Cancelling"));
    }
    
}
