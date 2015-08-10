package com.pearson.statsagg.database_objects.alerts;

import com.pearson.statsagg.controller.ContextManager;
import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import com.pearson.statsagg.database_objects.metric_group.MetricGroup;
import com.pearson.statsagg.database_objects.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database_objects.notifications.NotificationGroup;
import com.pearson.statsagg.database_objects.notifications.NotificationGroupsDao;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.webui.AlertsLogic;
import com.pearson.statsagg.webui.MetricGroupsLogic;
import com.pearson.statsagg.webui.NotificationGroupsLogic;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.TreeSet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prashant4nov
 */
public class AlertsDaoTest  {
    
   // private AlertsDao alertsDao; 
    private static final JSONObject mockAlertsJson = new JSONObject();
    private static final Logger logger = LoggerFactory.getLogger(AlertsLogic.class.getName());
    private MetricGroup metricGroup_ = null;
    private static final String metricGroupName_ = "JUnit - MetricGroup for AlertsLogicTest";
    private NotificationGroup notificationGroup_;
    private static final String notificationGroupName_ = "JUnit - NotificationGroup for AlertsLogicTest";

    private final AlertsLogic alertsLogic_ = new AlertsLogic();
    private final MetricGroupsLogic metricGroupsLogic_ = new MetricGroupsLogic();
    private final NotificationGroupsLogic notificationGroupsLogic_ = new NotificationGroupsLogic();

    
    @BeforeClass
    public static void setUpClass() {
        ContextManager contextManager = new ContextManager();
        InputStream ephemeralDatabaseConfiguration = contextManager.getEphemeralDatabaseConfiguration();
        contextManager.initializeDatabaseFromInputStream(ephemeralDatabaseConfiguration);
        contextManager.createDatabaseSchemas();
        
        JSONArray mockAlertsList = new JSONArray();
        JSONObject mockFirstAlert = new JSONObject();
        
        mockFirstAlert.put("name", "alert junit 1");
        mockFirstAlert.put("id", "1");
        mockAlertsList.add(mockFirstAlert);

        JSONObject mockSecondAlert = new JSONObject();
        mockSecondAlert.put("name", "alert junit 2");
        mockSecondAlert.put("id", "2");
        mockAlertsList.add(mockSecondAlert);
             
        mockAlertsJson.put("alerts", mockAlertsList);
        mockAlertsJson.put("count", 2);
    }
    
    @AfterClass
    public static void tearDownClass() {
        DatabaseConnections.disconnectAndShutdown();
    }

    public AlertsDaoTest() {
        this.notificationGroup_ = null;
    }
    
    @Before
    public void setUp() {
        NotificationGroup notificationGroup = new NotificationGroup(-1, notificationGroupName_, notificationGroupName_);   
        String result = notificationGroupsLogic_.alterRecordInDatabase(notificationGroup);
        assertTrue(result.contains("Success"));
        
        NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
        notificationGroup_ = notificationGroupsDao.getNotificationGroupByName(notificationGroupName_);
        result = metricGroupsLogic_.deleteRecordInDatabase(metricGroupName_);
        assertTrue(result.contains("success") || result.contains("Metric group not found"));
        
        MetricGroup metricGroup = new MetricGroup(-1, metricGroupName_, metricGroupName_);   
        TreeSet<String> regexs = new TreeSet<>();
        regexs.add(metricGroupName_);
        TreeSet<String> tags = new TreeSet<>();
        regexs.add(metricGroupName_);
        result = metricGroupsLogic_.alterRecordInDatabase(metricGroup, regexs, null, tags);
        assertTrue(result.contains("Success"));
        
        MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
        metricGroup_ = metricGroupsDao.getMetricGroupByName(metricGroupName_);
    }
    
    @After
    public void tearDown() {
        // Delete an alert that was inserted into the database from a previous test. Verify that it was deleted.
        String result = alertsLogic_.deleteRecordInDatabase("alert junit 1");
        assertTrue(result.contains("success") || result.contains("Alert not found"));
        
        // Delete an alert that was inserted into the database from a previous test. Verify that it was deleted.
        result = alertsLogic_.deleteRecordInDatabase("alert junit 2");
        assertTrue(result.contains("success") || result.contains("Alert not found"));
        
        // Delete notification group. This notification group existed solely for this unit test, so it is safe to delete.
        result = notificationGroupsLogic_.deleteRecordInDatabase(notificationGroupName_);
        assertTrue(result.contains("success"));
        
        // Delete metric group. This metric group existed solely for this unit test, so it is safe to delete.
        result = metricGroupsLogic_.deleteRecordInDatabase(metricGroupName_);
        assertTrue(result.contains("success"));
    }

    @Test
    public void testGetAlerts() {
        // Create & insert an Alert, insert it into the db, retrieve it from the db, & check for correctness of the retrieved records.
        Alert alert_1 = new Alert(1, "alert junit 1", "alert junit 1" , metricGroup_.getId(), false, true, true, Alert.TYPE_THRESHOLD, true, true, 300000l, DatabaseObjectCommon.TIME_UNIT_SECONDS, 
            notificationGroup_.getId(), notificationGroup_.getId(), Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 1, false, new Timestamp(System.currentTimeMillis()), false, null, null,
            notificationGroup_.getId(), notificationGroup_.getId(), Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("200"), 1000L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 2, true, new Timestamp(System.currentTimeMillis()), false, null, null);
        String result = alertsLogic_.alterRecordInDatabase(alert_1);
        assertTrue(result.contains("Success"));
        
        Alert alert_2 = new Alert(2, "alert junit 2", "alert junit 2" , metricGroup_.getId(), false, true, true, Alert.TYPE_THRESHOLD, true, true, 300000l, DatabaseObjectCommon.TIME_UNIT_SECONDS, 
            notificationGroup_.getId(), notificationGroup_.getId(), Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 1, false, new Timestamp(System.currentTimeMillis()), false, null, null,
            notificationGroup_.getId(), notificationGroup_.getId(), Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("200"), 1000L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 2, true, new Timestamp(System.currentTimeMillis()), false, null, null);
        result = alertsLogic_.alterRecordInDatabase(alert_2);
        assertTrue(result.contains("Success"));
        
        AlertsDao alertsDao = new AlertsDao();
        JSONObject resultAlerts = alertsDao.getAlerts(0, 10);
        assertEquals(mockAlertsJson, resultAlerts);
    }
    
    @Test
    public void testGetAlertsNoAlerts() {
        JSONObject mockAlertsJsonNoAlert = new JSONObject();
        mockAlertsJsonNoAlert.put("alerts", new JSONArray());
        mockAlertsJsonNoAlert.put("count", 0);

        JSONObject resultAlerts = new AlertsDao().getAlerts(0, 0);
        assertEquals(mockAlertsJsonNoAlert, resultAlerts);

        resultAlerts = new AlertsDao().getAlerts(0, 100);
        assertEquals(mockAlertsJsonNoAlert, resultAlerts);
        
        // Create & insert an Alert, insert it into the db, retrieve it from the db, & check for correctness of the retrieved records.
        Alert alert_1 = new Alert(1, "alert junit 1", "alert junit 1" , metricGroup_.getId(), false, true, true, Alert.TYPE_THRESHOLD, true, true, 300000l, DatabaseObjectCommon.TIME_UNIT_SECONDS, 
            notificationGroup_.getId(), notificationGroup_.getId(), Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 1, false, new Timestamp(System.currentTimeMillis()), false, null, null,
            notificationGroup_.getId(), notificationGroup_.getId(), Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("200"), 1000L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 2, true, new Timestamp(System.currentTimeMillis()), false, null, null);
        String result = alertsLogic_.alterRecordInDatabase(alert_1);
        assertTrue(result.contains("Success"));
        
        Alert alert_2 = new Alert(2, "alert junit 2", "alert junit 2" , metricGroup_.getId(), false, true, true, Alert.TYPE_THRESHOLD, true, true, 300000l, DatabaseObjectCommon.TIME_UNIT_SECONDS, 
            notificationGroup_.getId(), notificationGroup_.getId(), Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 1, false, new Timestamp(System.currentTimeMillis()), false, null, null,
            notificationGroup_.getId(), notificationGroup_.getId(), Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("200"), 1000L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 2, true, new Timestamp(System.currentTimeMillis()), false, null, null);
        result = alertsLogic_.alterRecordInDatabase(alert_2);
        assertTrue(result.contains("Success"));
        
        resultAlerts = new AlertsDao().getAlerts(115600, 34);
        assertEquals(mockAlertsJsonNoAlert, resultAlerts);
    }
    
}
