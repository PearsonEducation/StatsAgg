package com.pearson.statsagg.network.http;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.TreeSet;
import com.pearson.statsagg.controller.ContextManager;
import com.pearson.statsagg.database.alerts.Alert;
import com.pearson.statsagg.database.alerts.AlertsDao;
import com.pearson.statsagg.database.metric_group.MetricGroup;
import com.pearson.statsagg.database.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database.notifications.NotificationGroup;
import com.pearson.statsagg.database.notifications.NotificationGroupsDao;
import com.pearson.statsagg.globals.DatabaseConnections;
import java.io.File;
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
        NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
        notificationGroup_ = notificationGroupsDao.getNotificationGroupByName(notificationGroupName_);
        
        // delete previous metric group, create a new metric group, and retrieve it from the db. this metric group exists solely for this unit test.
        result = metricGroupsLogic_.deleteRecordInDatabase(metricGroupName_);
        assertTrue(result.contains("success") || result.contains("Metric group not found"));
        MetricGroup metricGroup = new MetricGroup(-1, metricGroupName_, metricGroupName_);   
        TreeSet<String> regexs = new TreeSet<>();
        regexs.add(metricGroupName_);
        TreeSet<String> tags = new TreeSet<>();
        regexs.add(metricGroupName_);
        result = metricGroupsLogic_.alterRecordInDatabase(metricGroup, regexs, tags);
        assertTrue(result.contains("Success"));
        MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
        metricGroup_ = metricGroupsDao.getMetricGroupByName(metricGroupName_);
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
        Alert alert1 = new Alert(1, "alert junit 1", "alert junit 1" , metricGroup_.getId(), notificationGroup_.getId(), false, true, true, 300000, 
            Alert.TYPE_THRESHOLD, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900, 1, false, new Timestamp(System.currentTimeMillis()), null,  
            Alert.TYPE_THRESHOLD, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("200"), 1000, 2, true, new Timestamp(System.currentTimeMillis()), null);
        result = alertsLogic_.alterRecordInDatabase(alert1);
        assertTrue(result.contains("Success"));
        AlertsDao alertsDao = new AlertsDao();
        Alert alert1FromDb = alertsDao.getAlertByName("alert junit 1");
        assertTrue(alert1FromDb.getName().contains("alert junit 1"));
        assertTrue(alert1FromDb.getDescription().contains("alert junit 1"));
        
        // alter the alert description. update the record in the db, retrieve it from the db, & check for correctness of the retrieved records
        alert1FromDb.setDescription("alert junit 1_2");
        result = alertsLogic_.alterRecordInDatabase(alert1FromDb);
        assertTrue(result.contains("Failed"));
        result = alertsLogic_.alterRecordInDatabase(alert1FromDb, alert1FromDb.getName());
        assertTrue(result.contains("Success"));
        alertsDao = new AlertsDao();
        Alert alert2FromDb = alertsDao.getAlertByName("alert junit 1");
        assertTrue(alert2FromDb.getName().contains("alert junit 1"));
        assertTrue(alert2FromDb.getDescription().contains("alert junit 1_2"));
        
        // attempt to delete an alert in the database that doesn't exist. make sure it didn't delete the record that was inserted into the db earlier.
        result = alertsLogic_.deleteRecordInDatabase("alert junit fake 1");
        assertTrue(result.contains("Cancelling"));
        alertsDao = new AlertsDao();
        Alert alert3FromDb = alertsDao.getAlertByName("alert junit 1");
        assertTrue(alert3FromDb.getName().contains("alert junit 1"));
        
        // alters an alert's name, make sure it got properly updated in the db, then changes the alert name back to what it was originally named
        alertsDao = new AlertsDao(false);
        Alert alertFromDbOriginalName = alertsDao.getAlertByName("alert junit 1"); //pt 1
        assertTrue(alertFromDbOriginalName.getName().contains("alert junit 1"));
        Alert alertFromDbNewName = Alert.copy(alertFromDbOriginalName);
        alertFromDbNewName.setName("alert junit 1_1");
        result = alertsLogic_.alterRecordInDatabase(alertFromDbNewName, alertFromDbOriginalName.getName());
        assertTrue(result.contains("Successful"));
        Alert alertFromDbNewNameVerify = alertsDao.getAlertByName(alertFromDbNewName.getName()); //pt2
        assertFalse(alertFromDbNewNameVerify.getName().equals(alertFromDbOriginalName.getName()));
        assertTrue(alertFromDbNewNameVerify.getName().contains(alertFromDbNewName.getName()));
        assertEquals(alertFromDbOriginalName.getId(), alertFromDbNewNameVerify.getId());
        Alert alertFromDbOriginalName_NoResult = alertsDao.getAlertByName(alertFromDbOriginalName.getName()); //pt3
        assertEquals(alertFromDbOriginalName_NoResult, null);
        Alert alertFromDbOriginalName_Reset = Alert.copy(alertFromDbOriginalName); // pt4
        alertFromDbOriginalName_Reset.setName(alertFromDbOriginalName.getName());  
        result = alertsLogic_.alterRecordInDatabase(alertFromDbOriginalName_Reset, alertFromDbOriginalName.getName());
        assertTrue(result.contains("Successful"));
        alertsDao.close();
        
        // delete the alert that was inserted into the database earlier. verify that it was deleted.
        result = alertsLogic_.deleteRecordInDatabase("alert junit 1");
        assertTrue(result.contains("success"));
        alertsDao = new AlertsDao();
        Alert alert7FromDb = alertsDao.getAlertByName("alert junit 1");
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
        
        Alert alert1 = new Alert(1, "alert junit 1", "alert junit 1" , metricGroup_.getId(), notificationGroup_.getId(), false, true, true, 300000, 
            Alert.TYPE_THRESHOLD, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900, 1, false, new Timestamp(System.currentTimeMillis()), null, 
            Alert.TYPE_THRESHOLD, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("200"), 1000, 2, true, new Timestamp(System.currentTimeMillis()), null);
        result = alertsLogic_.alterRecordInDatabase(alert1);
        assertTrue(result.contains("Success"));
        AlertsDao alertsDao = new AlertsDao();
        Alert alert1FromDb = alertsDao.getAlertByName("alert junit 1");
        assertTrue(alert1FromDb.getName().contains("alert junit 1"));
        assertTrue(alert1FromDb.getDescription().contains("alert junit 1"));
        
        result = alertsLogic_.deleteRecordInDatabase("alert junit 1");
        assertTrue(result.contains("success"));
        alertsDao = new AlertsDao();
        Alert alert2FromDb = alertsDao.getAlertByName("alert junit 1");
        assertEquals(null, alert2FromDb);
        
        result = alertsLogic_.deleteRecordInDatabase("alert junit fake 1");
        assertTrue(result.contains("Cancelling"));
    }
    
}
