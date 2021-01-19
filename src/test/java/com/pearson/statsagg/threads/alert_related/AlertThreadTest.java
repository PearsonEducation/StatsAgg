package com.pearson.statsagg.threads.alert_related;

import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.metric_aggregation.MetricTimestampAndValue;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Jeffrey Schmidt
 */
public class AlertThreadTest {
    
    private static AlertThread alertThread_;
    private static final AtomicLong hashKeyGen_ = GlobalVariables.metricHashKeyGenerator;
        
    private static final List<MetricTimestampAndValue> metricTimestampsAndValues_ = new ArrayList<>();
    private static Alert alert1_ = null;
    private static Alert alert2_ = null;
    private static Alert alert3_ = null;
    private static Alert alert4_ = null;
    
    public AlertThreadTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        alertThread_ = new AlertThread(System.currentTimeMillis(), true, true, null, 2);
        
        metricTimestampsAndValues_.add(new MetricTimestampAndValue((long) 100, new BigDecimal("69.9"), hashKeyGen_.incrementAndGet())); 
        metricTimestampsAndValues_.add(new MetricTimestampAndValue((long) 200, new BigDecimal("71"), hashKeyGen_.incrementAndGet()));
        metricTimestampsAndValues_.add(new MetricTimestampAndValue((long) 300, new BigDecimal("72"), hashKeyGen_.incrementAndGet())); 
        metricTimestampsAndValues_.add(new MetricTimestampAndValue((long) 400, new BigDecimal("73"), hashKeyGen_.incrementAndGet()));
        metricTimestampsAndValues_.add(new MetricTimestampAndValue((long) 500, new BigDecimal("74"), hashKeyGen_.incrementAndGet())); 
        metricTimestampsAndValues_.add(new MetricTimestampAndValue((long) 700, new BigDecimal("75"), hashKeyGen_.incrementAndGet())); 
        metricTimestampsAndValues_.add(new MetricTimestampAndValue((long) 800, new BigDecimal("76"), hashKeyGen_.incrementAndGet()));
        metricTimestampsAndValues_.add(new MetricTimestampAndValue((long) 900, new BigDecimal("77"), hashKeyGen_.incrementAndGet())); 
        metricTimestampsAndValues_.add(new MetricTimestampAndValue((long) 1000, new BigDecimal("78"), hashKeyGen_.incrementAndGet()));
        metricTimestampsAndValues_.add(new MetricTimestampAndValue((long) 1100, new BigDecimal("79"), hashKeyGen_.incrementAndGet())); 
        metricTimestampsAndValues_.add(new MetricTimestampAndValue((long) 1200, new BigDecimal("80.1"), hashKeyGen_.incrementAndGet()));     
        
        alert1_ = new Alert(1, "alert1", "alert1_description", null, null, 11, false, true, true, Alert.TYPE_THRESHOLD, true, true, 300000l, DatabaseObjectCommon.TIME_UNIT_SECONDS, 
            1, 2, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, 1, DatabaseObjectCommon.TIME_UNIT_SECONDS, false, new Timestamp(System.currentTimeMillis()), false, null, null,
            1, 2, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("200"), 1000L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, 2, DatabaseObjectCommon.TIME_UNIT_SECONDS, true, new Timestamp(System.currentTimeMillis()), false, null, null);
        
        alert2_ = new Alert(2, "alert2", "alert2_description", null, null, 11, true, true, true, Alert.TYPE_THRESHOLD, true, true, 300000l, DatabaseObjectCommon.TIME_UNIT_SECONDS, 
            1, 2, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, 1, DatabaseObjectCommon.TIME_UNIT_SECONDS, true, new Timestamp(System.currentTimeMillis()), false, null, null,
            1, 2, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("200"), 1000L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, 2, DatabaseObjectCommon.TIME_UNIT_SECONDS, false, new Timestamp(System.currentTimeMillis()), false, null, null);
        
        alert3_ = new Alert(3, "alert3", "alert3_description", null, null, 11, false, true, true, Alert.TYPE_THRESHOLD, true, true, 300000l, DatabaseObjectCommon.TIME_UNIT_SECONDS, 
            1, 2, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, 1, DatabaseObjectCommon.TIME_UNIT_SECONDS, true, new Timestamp(System.currentTimeMillis()), false, null, null,
            1, 2, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("200"), 1000L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, 2, DatabaseObjectCommon.TIME_UNIT_SECONDS, true, new Timestamp(System.currentTimeMillis()), false, null, null);
  
        alert4_ = new Alert(4, "alert4", "alert4_description", null, null, 11, true, true, true, Alert.TYPE_THRESHOLD, false, true, 300000l, DatabaseObjectCommon.TIME_UNIT_SECONDS, 
            1, 2, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, 1, DatabaseObjectCommon.TIME_UNIT_SECONDS, false, new Timestamp(System.currentTimeMillis()), false, null, null,
            1, 2, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("200"), 1000L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, 2, DatabaseObjectCommon.TIME_UNIT_SECONDS, false, new Timestamp(System.currentTimeMillis()), false, null, null); 
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    /**
     * Test of separateMetricKeysByCpuCore method, of class AlertThread.
     */
    @Test
    public void testSeparateMetricKeysByCpuCore() {
        List<Alert> alerts = new ArrayList<>();
        
        for (int i = 1; i <= 7877; i++) {
            Alert alert = new Alert(i, "alert-" + i, "alert-" + i , null, null, 11, false, true, true, Alert.TYPE_THRESHOLD, true, true, 300000l, DatabaseObjectCommon.TIME_UNIT_SECONDS, 
                1, 2, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 1, false, new Timestamp(System.currentTimeMillis()), false, null, null, 
                1, 2, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("200"), 1000L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 2, true, new Timestamp(System.currentTimeMillis()), false, null, null);
            
            alerts.add(alert);
        }
 
        Map<Integer,List<Alert>> metricKeysByCpuCore = AlertThread.separateAlertsByCpuCore(alerts);
        
        if (Runtime.getRuntime().availableProcessors() > 2) assertEquals(metricKeysByCpuCore.size(), Runtime.getRuntime().availableProcessors() - 1);
        else assertEquals(1, Runtime.getRuntime().availableProcessors() - 1);
        
        int totalAlerts = 0;
        for (List<Alert> metricKeysSingleCore : metricKeysByCpuCore.values()) {
            totalAlerts = totalAlerts + metricKeysSingleCore.size();
        }
        
        assertTrue(7877 == totalAlerts);
    }
    
    /**
     * Test of appendActiveAlertsToSet method, of class AlertThread.
     */
    @Test
    public void testAppendActiveAlertsToClob() {
        
        String previousMetricKeySet = "89\n88\n90\n91\n92\n";
        
        List<String> currentActiveMetricKeys = new ArrayList<>();
        currentActiveMetricKeys.add("3\n");
        currentActiveMetricKeys.add("2\n");
        currentActiveMetricKeys.add("1\n");
        currentActiveMetricKeys.add("5\r");
        currentActiveMetricKeys.add("0\r\n");

        String mergedString = AlertThread.appendActiveAlertsToSet(currentActiveMetricKeys, previousMetricKeySet, 7);
        assertEquals("2\n3\n88\n89\n90\n91\n92\n", mergedString);
    }

    /**
     * Test of getAlertsByAlertId method, of class AlertThread.
     */
    @Test
    public void testGetAlertsByAlertId() {
        List<Alert> alerts = new ArrayList<>();
        
        alerts.add(alert1_);
        alerts.add(alert2_);
        alerts.add(alert3_);
        alerts.add(alert4_);
        
        Map<Integer,Alert> alertsByAlertId = AlertThread.getAlertsByAlertId(alerts);
        
        assertTrue(alertsByAlertId.containsValue(alert1_));
        assertTrue(alertsByAlertId.containsValue(alert2_));
        assertTrue(alertsByAlertId.containsValue(alert3_));
        assertTrue(alertsByAlertId.containsValue(alert4_));
        assertTrue(alertsByAlertId.size() == 4);
    }
    
    /**
     * Test of getEnabledAlerts method, of class AlertThread.
     */
    @Test
    public void testGetEnabledAlerts() {
        List<Alert> alerts = new ArrayList<>();
        
        alerts.add(alert1_);
        alerts.add(alert2_);
        alerts.add(alert3_);
        alerts.add(alert4_);
        
        List<Alert> enabledAlerts = AlertThread.getEnabledAlerts(alerts);
        
        assertEquals(false, enabledAlerts.contains(alert1_));
        assertEquals(true, enabledAlerts.contains(alert2_));
        assertEquals(false, enabledAlerts.contains(alert3_));
        assertEquals(true, enabledAlerts.contains(alert4_));
    }
    
    /**
     * Test of getActiveCautionAlerts method, of class AlertThread.
     */
    @Test
    public void testGetActiveCautionAlerts() {
        List<Alert> alerts = new ArrayList<>();
        
        alerts.add(alert1_);
        alerts.add(alert2_);
        alerts.add(alert3_);
        alerts.add(alert4_);
        
        List<Alert> activeAlerts = AlertThread.getActiveCautionAlerts(alerts);
        
        assertEquals(false, activeAlerts.contains(alert1_));
        assertEquals(true, activeAlerts.contains(alert2_));
        assertEquals(true, activeAlerts.contains(alert3_));
        assertEquals(false, activeAlerts.contains(alert4_));
    }
    
    /**
     * Test of getActiveDangerAlerts method, of class AlertThread.
     */
    @Test
    public void testGetActiveDangerAlerts() {
        List<Alert> alerts = new ArrayList<>();
        
        alerts.add(alert1_);
        alerts.add(alert2_);
        alerts.add(alert3_);
        alerts.add(alert4_);
        
        List<Alert> activeAlerts = AlertThread.getActiveDangerAlerts(alerts);
        
        assertEquals(true, activeAlerts.contains(alert1_));
        assertEquals(false, activeAlerts.contains(alert2_));
        assertEquals(true, activeAlerts.contains(alert3_));
        assertEquals(false, activeAlerts.contains(alert4_));
    }
    
    /**
     * Test of getActiveAlerts method, of class AlertThread.
     */
    @Test
    public void testGetActiveAlerts() {
        List<Alert> alerts = new ArrayList<>();
        
        alerts.add(alert1_);
        alerts.add(alert2_);
        alerts.add(alert3_);
        alerts.add(alert4_);
        
        List<Alert> activeAlerts = AlertThread.getActiveAlerts(alerts);
        
        assertEquals(true, activeAlerts.contains(alert1_));
        assertEquals(true, activeAlerts.contains(alert2_));
        assertEquals(true, activeAlerts.contains(alert3_));
        assertEquals(false, activeAlerts.contains(alert4_));
    }
    
    /**
     * Test of getAvailabilityAlert_TimeSinceLastSeen method, of class AlertThread.
     */
    @Test
    public void testGetAvailabilityAlert_TimeSinceLastSeen() {
        
        AlertThread alertThread = new AlertThread(Long.valueOf(950), true, true, null, 2);
        BigDecimal result;
        
        result = AlertThread.getAvailabilityAlert_TimeSinceLastSeen(alertThread.threadStartTimestampInMilliseconds_, new Long(800), Alert.TYPE_AVAILABILITY, 100L);
        assertEquals(new BigDecimal("150"), result);
        
        result = AlertThread.getAvailabilityAlert_TimeSinceLastSeen(alertThread.threadStartTimestampInMilliseconds_, new Long(850), Alert.TYPE_AVAILABILITY, 100L);
        assertEquals(null, result);
        
        result = AlertThread.getAvailabilityAlert_TimeSinceLastSeen(alertThread.threadStartTimestampInMilliseconds_, new Long(950), Alert.TYPE_AVAILABILITY, 100L);
        assertEquals(null, result);
        
        result = AlertThread.getAvailabilityAlert_TimeSinceLastSeen(alertThread.threadStartTimestampInMilliseconds_, new Long(1000), Alert.TYPE_AVAILABILITY, 100L);
        assertEquals(null, result);
    }
    
    
    /**
     * Test of isAlertActive_Threshold method, of class AlertThread.
     */
    @Test
    public void testIsAlertActive_Threshold() {

        AlertThread alertThread = new AlertThread(Long.valueOf(950), true, true, null, 2);
        BigDecimal result;
        
        // test operators
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 300L, Alert.OPERATOR_GREATER, Alert.COMBINATION_AVERAGE, null, new BigDecimal(75.9), 1);
        assertEquals(new BigDecimal("76"), result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 300L, Alert.OPERATOR_GREATER, Alert.COMBINATION_AVERAGE, null, new BigDecimal(76), 1);
        assertEquals(null, result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 300L, Alert.OPERATOR_GREATER, Alert.COMBINATION_AVERAGE, null, new BigDecimal(76.1), 1);
        assertEquals(null, result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 300L, Alert.OPERATOR_GREATER_EQUALS, Alert.COMBINATION_AVERAGE, null, new BigDecimal(75.9), 1);
        assertEquals(new BigDecimal("76"), result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 300L, Alert.OPERATOR_GREATER_EQUALS, Alert.COMBINATION_AVERAGE, null, new BigDecimal(76), 1);
        assertEquals(new BigDecimal("76"), result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 300L, Alert.OPERATOR_GREATER_EQUALS, Alert.COMBINATION_AVERAGE, null, new BigDecimal(76.1), 1);
        assertEquals(null, result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 300L, Alert.OPERATOR_LESS, Alert.COMBINATION_AVERAGE, null, new BigDecimal(75.9), 1);
        assertEquals(null, result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 300L, Alert.OPERATOR_LESS, Alert.COMBINATION_AVERAGE, null, new BigDecimal(76), 1);
        assertEquals(null, result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 300L, Alert.OPERATOR_LESS, Alert.COMBINATION_AVERAGE, null, new BigDecimal(76.1), 1);
        assertEquals(new BigDecimal("76"), result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 300L, Alert.OPERATOR_LESS_EQUALS, Alert.COMBINATION_AVERAGE, null, new BigDecimal(75.9), 1);
        assertEquals(null, result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 300L, Alert.OPERATOR_LESS_EQUALS, Alert.COMBINATION_AVERAGE, null, new BigDecimal(76), 1);
        assertEquals(new BigDecimal("76"), result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 300L, Alert.OPERATOR_LESS_EQUALS, Alert.COMBINATION_AVERAGE, null, new BigDecimal(76.1), 1);
        assertEquals(new BigDecimal("76"), result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 300L, Alert.OPERATOR_EQUALS, Alert.COMBINATION_AVERAGE, null, new BigDecimal(75.9), 1);
        assertEquals(null, result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 300L, Alert.OPERATOR_EQUALS, Alert.COMBINATION_AVERAGE, null, new BigDecimal(76), 1);
        assertEquals(new BigDecimal("76"), result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 300L, Alert.OPERATOR_EQUALS, Alert.COMBINATION_AVERAGE, null, new BigDecimal(76.1), 1);
        assertEquals(null, result);
        
        // test combinations
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 300L, Alert.OPERATOR_GREATER, Alert.COMBINATION_AVERAGE, null, new BigDecimal(75.9), 1);
        assertEquals(new BigDecimal("76"), result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 300L, Alert.OPERATOR_GREATER,  Alert.COMBINATION_AVERAGE, null, new BigDecimal(76), 1);
        assertEquals(null, result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 300L, Alert.OPERATOR_GREATER, Alert.COMBINATION_AVERAGE, null, new BigDecimal(76.1), 1);
        assertEquals(null, result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 300L, Alert.OPERATOR_GREATER,  Alert.COMBINATION_ALL, null, new BigDecimal(74), 1);
        assertEquals(new BigDecimal("77"), result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 300L, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal(75), 1);
        assertEquals(null, result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 300L, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal(76), 1);
        assertEquals(null, result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 300L, Alert.OPERATOR_GREATER, Alert.COMBINATION_ANY, null, new BigDecimal(76), 1);
        assertEquals(new BigDecimal("77"), result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 300L, Alert.OPERATOR_GREATER, Alert.COMBINATION_ANY, null, new BigDecimal(77), 1);
        assertEquals(null, result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 1500L, Alert.OPERATOR_GREATER, Alert.COMBINATION_AT_LEAST_COUNT, 5, new BigDecimal(72), 1);
        assertEquals(new BigDecimal("5"), result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 1500L, Alert.OPERATOR_GREATER, Alert.COMBINATION_AT_LEAST_COUNT, 5, new BigDecimal(73), 1);
        assertEquals(null, result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 1500L, Alert.OPERATOR_GREATER, Alert.COMBINATION_AT_LEAST_COUNT, 5, new BigDecimal(74), 1);
        assertEquals(null, result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 1500L, Alert.OPERATOR_GREATER, Alert.COMBINATION_AT_MOST_COUNT, 4, new BigDecimal(72), 1);
        assertEquals(null, result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 1500L, Alert.OPERATOR_GREATER, Alert.COMBINATION_AT_MOST_COUNT, 4, new BigDecimal(73), 1);
        assertEquals(new BigDecimal("4"), result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 1500L, Alert.OPERATOR_GREATER, Alert.COMBINATION_AT_MOST_COUNT, 4, new BigDecimal(74), 1);
        assertEquals(new BigDecimal("3"), result);
        
        // test min sample count
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 300L, Alert.OPERATOR_GREATER, Alert.COMBINATION_AVERAGE, null, new BigDecimal(75.9), 2);
        assertEquals(new BigDecimal("76"), result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 300L, Alert.OPERATOR_GREATER, Alert.COMBINATION_AVERAGE, null, new BigDecimal(75.9), 3);
        assertEquals(new BigDecimal("76"), result);
        result = AlertThread.isAlertActive_Threshold(alertThread.threadStartTimestampInMilliseconds_, metricTimestampsAndValues_, Alert.TYPE_THRESHOLD, 300L, Alert.OPERATOR_GREATER, Alert.COMBINATION_AVERAGE, null, new BigDecimal(75.9), 4);
        assertEquals(null, result);
    }
    
    /**
     * Test of getStartAndEndIndexesOfTimestamps method, of class AlertThread.
     */
    @Test
    public void testGetStartAndEndIndexesOfTimestamps() {
        
        List<MetricTimestampAndValue> metricTimestampsAndValues = new ArrayList<>();
        
        metricTimestampsAndValues.add(new MetricTimestampAndValue((long) 10, new BigDecimal("0"), hashKeyGen_.incrementAndGet())); // 0
        metricTimestampsAndValues.add(new MetricTimestampAndValue((long) 30, new BigDecimal("0"), hashKeyGen_.incrementAndGet())); // 1
        metricTimestampsAndValues.add(new MetricTimestampAndValue((long) 50, new BigDecimal("0"), hashKeyGen_.incrementAndGet())); // 2
        metricTimestampsAndValues.add(new MetricTimestampAndValue((long) 70, new BigDecimal("0"), hashKeyGen_.incrementAndGet())); // 3
        metricTimestampsAndValues.add(new MetricTimestampAndValue((long) 90, new BigDecimal("0"), hashKeyGen_.incrementAndGet())); // 4
        metricTimestampsAndValues.add(new MetricTimestampAndValue((long) 110, new BigDecimal("0"), hashKeyGen_.incrementAndGet())); // 5
        
        int[] expectedResult; 
        int[] result;

        try { 
            Method method = AlertThread.class.getDeclaredMethod("getStartAndEndIndexesOfTimestamps", Long.TYPE, Long.TYPE, List.class);
            method.setAccessible(true);
        
            // start = < range, end = > range
            result = (int[]) method.invoke(alertThread_, (long) 0, (long) 120, metricTimestampsAndValues);
            expectedResult = new int[] {0,5};
            assertArrayEquals(expectedResult, result);
            
            // start = range-low-equal, end = range-high-equal
            result = (int[]) method.invoke(alertThread_, (long) 10, (long) 110, metricTimestampsAndValues);
            expectedResult = new int[] {0,5};
            assertArrayEquals(expectedResult, result);
            
            // start = range-mid-between, end = range-mid-between
            result = (int[]) method.invoke(alertThread_, (long) 20, (long) 80, metricTimestampsAndValues);
            expectedResult = new int[] {1,3};
            assertArrayEquals(expectedResult, result);

            // start = range-mid-equal, end = range-mid-equal
            result = (int[]) method.invoke(alertThread_, (long) 50, (long) 70, metricTimestampsAndValues);
            expectedResult = new int[] {2,3};
            assertArrayEquals(expectedResult, result);

            // start=end, range-mid-between
            result = (int[]) method.invoke(alertThread_, (long) 20, (long) 20, metricTimestampsAndValues);
            expectedResult = null;
            assertArrayEquals(expectedResult, result);

            // start & end range-mid-between, same between
            result = (int[]) method.invoke(alertThread_, (long) 21, (long) 25, metricTimestampsAndValues);
            expectedResult = null;
            assertArrayEquals(expectedResult, result);

            // start>end, range-mid-between
            result = (int[]) method.invoke(alertThread_, (long) 40, (long) 20, metricTimestampsAndValues);
            expectedResult = null;
            assertArrayEquals(expectedResult, result);
            
            // start>end, range-mid-equal
            result = (int[]) method.invoke(alertThread_, (long) 50, (long) 30, metricTimestampsAndValues);
            expectedResult = null;
            assertArrayEquals(expectedResult, result);
            
            // start=end, range-mid-equal
            result = (int[]) method.invoke(alertThread_, (long) 50, (long) 50, metricTimestampsAndValues);
            expectedResult = new int[] {2,2};
            assertArrayEquals(expectedResult, result);

            // start=end, range-high
            result = (int[]) method.invoke(alertThread_, (long) 110, (long) 110, metricTimestampsAndValues);
            expectedResult = new int[] {5,5};
            assertArrayEquals(expectedResult, result);

            // start=end, range-low
            result = (int[]) method.invoke(alertThread_, (long) 10, (long) 10, metricTimestampsAndValues);
            expectedResult = new int[] {0,0};
            assertArrayEquals(expectedResult, result);

            // start & end below range
            result = (int[]) method.invoke(alertThread_, new Long(-30), (long) 0, metricTimestampsAndValues);
            expectedResult = null;
            assertArrayEquals(expectedResult, result);

            // start & end above range
            result = (int[]) method.invoke(alertThread_, (long) 120, (long) 150, metricTimestampsAndValues);
            expectedResult = null;
            assertArrayEquals(expectedResult, result);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
        
    }

    /**
     * Test of doesMeetThresholdCriteria_All method, of class AlertThread.
     */
    @Test
    public void testDoesMeetThresholdCriteria_All() {
        
        try { 
            Method method = AlertThread.class.getDeclaredMethod("doesMeetThresholdCriteria_All", List.class, BigDecimal.class, Integer.class);
            method.setAccessible(true);

            testDoesMeetThresholdCriteria_All_Greater(method, metricTimestampsAndValues_);
            testDoesMeetThresholdCriteria_All_GreaterEquals(method, metricTimestampsAndValues_);
            testDoesMeetThresholdCriteria_All_Less(method, metricTimestampsAndValues_);
            testDoesMeetThresholdCriteria_All_LessEquals(method, metricTimestampsAndValues_);
            testDoesMeetThresholdCriteria_All_Equals(method, metricTimestampsAndValues_);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }

    
    private void testDoesMeetThresholdCriteria_All_Greater(Method method, List<MetricTimestampAndValue> metricTimestampsAndValues) {
        
        BigDecimal result;
        
        try {
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("69.8999"), Alert.OPERATOR_GREATER);
            assertEquals(metricTimestampsAndValues_.get(metricTimestampsAndValues_.size() - 1).getMetricValue(), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("69.9"), Alert.OPERATOR_GREATER);
            assertEquals(null, result);

            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("70"), Alert.OPERATOR_GREATER);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80.2"), Alert.OPERATOR_GREATER);
            assertEquals(null, result);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }
    
    
    private void testDoesMeetThresholdCriteria_All_GreaterEquals(Method method, List<MetricTimestampAndValue> metricTimestampsAndValues) {
        
        BigDecimal result;
        
        try {
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("69.8999"), Alert.OPERATOR_GREATER_EQUALS);
            assertEquals(metricTimestampsAndValues_.get(metricTimestampsAndValues_.size() - 1).getMetricValue(), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("69.9"), Alert.OPERATOR_GREATER_EQUALS);
            assertEquals(metricTimestampsAndValues_.get(metricTimestampsAndValues_.size() - 1).getMetricValue(), result);

            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("70"), Alert.OPERATOR_GREATER_EQUALS);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80.2"), Alert.OPERATOR_GREATER_EQUALS);
            assertEquals(null, result);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }
    
    private void testDoesMeetThresholdCriteria_All_Less(Method method, List<MetricTimestampAndValue> metricTimestampsAndValues) {
        
        BigDecimal result;
        
        try {
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80.1111"), Alert.OPERATOR_LESS);
            assertEquals(metricTimestampsAndValues_.get(metricTimestampsAndValues_.size() - 1).getMetricValue(), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80.1"), Alert.OPERATOR_LESS);
            assertEquals(null, result);

            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("75"), Alert.OPERATOR_LESS);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("65"), Alert.OPERATOR_LESS);
            assertEquals(null, result);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }
    
    private void testDoesMeetThresholdCriteria_All_LessEquals(Method method, List<MetricTimestampAndValue> metricTimestampsAndValues) {
        
        BigDecimal result;
        
        try {
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80.1111"), Alert.OPERATOR_LESS_EQUALS);
            assertEquals(metricTimestampsAndValues_.get(metricTimestampsAndValues_.size() - 1).getMetricValue(), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80.1"), Alert.OPERATOR_LESS_EQUALS);
            assertEquals(metricTimestampsAndValues_.get(metricTimestampsAndValues_.size() - 1).getMetricValue(), result);

            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("75"), Alert.OPERATOR_LESS_EQUALS);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("65"), Alert.OPERATOR_LESS_EQUALS);
            assertEquals(null, result);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }
    
    private void testDoesMeetThresholdCriteria_All_Equals(Method method, List<MetricTimestampAndValue> metricTimestampsAndValues) {
        
        BigDecimal result;
        
        try {
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80.1111"), Alert.OPERATOR_EQUALS);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80.1"), Alert.OPERATOR_EQUALS);
            assertEquals(null, result);

            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("75"), Alert.OPERATOR_EQUALS);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("65"), Alert.OPERATOR_EQUALS);
            assertEquals(null, result);

            List<MetricTimestampAndValue> equalsMetricTimestampsAndValues = new ArrayList<>();
            equalsMetricTimestampsAndValues.add(new MetricTimestampAndValue((long) 100, new BigDecimal("70"), hashKeyGen_.incrementAndGet())); 
            equalsMetricTimestampsAndValues.add(new MetricTimestampAndValue((long) 200, new BigDecimal("70"), hashKeyGen_.incrementAndGet()));
            equalsMetricTimestampsAndValues.add(new MetricTimestampAndValue((long) 300, new BigDecimal("70"), hashKeyGen_.incrementAndGet())); 
            equalsMetricTimestampsAndValues.add(new MetricTimestampAndValue((long) 400, new BigDecimal("70"), hashKeyGen_.incrementAndGet()));
            
            result = (BigDecimal) method.invoke(alertThread_, equalsMetricTimestampsAndValues, new BigDecimal("70"), Alert.OPERATOR_EQUALS);
            assertEquals(equalsMetricTimestampsAndValues.get(equalsMetricTimestampsAndValues.size() - 1).getMetricValue(), result);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }

    /**
     * Test of doesMeetThresholdCriteria_Any method, of class AlertThread.
     */
    @Test
    public void testDoesMeetThresholdCriteria_Any() {
        
        try { 
            Method method = AlertThread.class.getDeclaredMethod("doesMeetThresholdCriteria_Any", List.class, BigDecimal.class, Integer.class);
            method.setAccessible(true);

            testDoesMeetThresholdCriteria_Any_Greater(method, metricTimestampsAndValues_);
            testDoesMeetThresholdCriteria_Any_GreaterEquals(method, metricTimestampsAndValues_);
            testDoesMeetThresholdCriteria_Any_Less(method, metricTimestampsAndValues_);
            testDoesMeetThresholdCriteria_Any_LessEquals(method, metricTimestampsAndValues_);
            testDoesMeetThresholdCriteria_Any_Equals(method, metricTimestampsAndValues_);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }

    
    private void testDoesMeetThresholdCriteria_Any_Greater(Method method, List<MetricTimestampAndValue> metricTimestampsAndValues) {
        
        BigDecimal result;
        
        try {
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("69.8999"), Alert.OPERATOR_GREATER);
            assertEquals(new BigDecimal("80.1"), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("69.9"), Alert.OPERATOR_GREATER);
            assertEquals(new BigDecimal("80.1"), result);

            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80.1"), Alert.OPERATOR_GREATER);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80.2"), Alert.OPERATOR_GREATER);
            assertEquals(null, result);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }
    
    
    private void testDoesMeetThresholdCriteria_Any_GreaterEquals(Method method, List<MetricTimestampAndValue> metricTimestampsAndValues) {
        
        BigDecimal result;
        
        try {
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("69.8999"), Alert.OPERATOR_GREATER_EQUALS);
            assertEquals(new BigDecimal("80.1"), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("69.9"), Alert.OPERATOR_GREATER_EQUALS);
            assertEquals(new BigDecimal("80.1"), result);

            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80.1"), Alert.OPERATOR_GREATER_EQUALS);
            assertEquals(new BigDecimal("80.1"), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80.2"), Alert.OPERATOR_GREATER_EQUALS);
            assertEquals(null, result);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }
    
    private void testDoesMeetThresholdCriteria_Any_Less(Method method, List<MetricTimestampAndValue> metricTimestampsAndValues) {
        
        BigDecimal result;
        
        try {
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80.1"), Alert.OPERATOR_LESS);
            assertEquals(new BigDecimal("79"), result);

            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("75"), Alert.OPERATOR_LESS);
            assertEquals(new BigDecimal("74"), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("69.9"), Alert.OPERATOR_LESS);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("69.8"), Alert.OPERATOR_LESS);
            assertEquals(null, result);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }
    
    private void testDoesMeetThresholdCriteria_Any_LessEquals(Method method, List<MetricTimestampAndValue> metricTimestampsAndValues) {
        
        BigDecimal result;
        
        try {
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80.1111"), Alert.OPERATOR_LESS_EQUALS);
            assertEquals(new BigDecimal("80.1"), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("75"), Alert.OPERATOR_LESS_EQUALS);
            assertEquals(new BigDecimal("75"), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("69.9"), Alert.OPERATOR_LESS_EQUALS);
            assertEquals(new BigDecimal("69.9"), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("69.8"), Alert.OPERATOR_LESS_EQUALS);
            assertEquals(null, result);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }
    
    private void testDoesMeetThresholdCriteria_Any_Equals(Method method, List<MetricTimestampAndValue> metricTimestampsAndValues) {
        
        BigDecimal result;
        
        try {
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80.1111"), Alert.OPERATOR_EQUALS);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80.1"), Alert.OPERATOR_EQUALS);
            assertEquals(new BigDecimal("80.1"), result);

            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("75"), Alert.OPERATOR_EQUALS);
            assertEquals(new BigDecimal("75"), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("65"), Alert.OPERATOR_EQUALS);
            assertEquals(null, result);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }
    
    /**
     * Test of doesMeetThresholdCriteria_Average method, of class AlertThread.
     */
    @Test
    public void testDoesMeetThresholdCriteria_Average() {
        
        try { 
            Method method = AlertThread.class.getDeclaredMethod("doesMeetThresholdCriteria_Average", List.class, BigDecimal.class, Integer.class);
            method.setAccessible(true);

            testDoesMeetThresholdCriteria_Average_Greater(method, metricTimestampsAndValues_);
            testDoesMeetThresholdCriteria_Average_GreaterEquals(method, metricTimestampsAndValues_);
            testDoesMeetThresholdCriteria_Average_Less(method, metricTimestampsAndValues_);
            testDoesMeetThresholdCriteria_Average_LessEquals(method, metricTimestampsAndValues_);
            testDoesMeetThresholdCriteria_Average_Equals(method, metricTimestampsAndValues_);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }

    private void testDoesMeetThresholdCriteria_Average_Greater(Method method, List<MetricTimestampAndValue> metricTimestampsAndValues) {
        
        BigDecimal result;
        
        try {
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("74.999"), Alert.OPERATOR_GREATER);
            assertEquals(new BigDecimal("75.0"), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("75"), Alert.OPERATOR_GREATER);
            assertEquals(null, result);

            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("75.001"), Alert.OPERATOR_GREATER);
            assertEquals(null, result);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }
    
    private void testDoesMeetThresholdCriteria_Average_GreaterEquals(Method method, List<MetricTimestampAndValue> metricTimestampsAndValues) {
        
        BigDecimal result;
        
        try {
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("74.999"), Alert.OPERATOR_GREATER_EQUALS);
            assertEquals(new BigDecimal("75.0"), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("75"), Alert.OPERATOR_GREATER_EQUALS);
            assertEquals(new BigDecimal("75.0"), result);

            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("75.001"), Alert.OPERATOR_GREATER_EQUALS);
            assertEquals(null, result);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }
    
    private void testDoesMeetThresholdCriteria_Average_Less(Method method, List<MetricTimestampAndValue> metricTimestampsAndValues) {
        
        BigDecimal result;
        
        try {
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("74.999"), Alert.OPERATOR_LESS);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("75"), Alert.OPERATOR_LESS);
            assertEquals(null, result);

            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("75.001"), Alert.OPERATOR_LESS);
            assertEquals(new BigDecimal("75.0"), result);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }
    
    private void testDoesMeetThresholdCriteria_Average_LessEquals(Method method, List<MetricTimestampAndValue> metricTimestampsAndValues) {
        
        BigDecimal result;
        
        try {
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("74.999"), Alert.OPERATOR_LESS_EQUALS);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("75"), Alert.OPERATOR_LESS_EQUALS);
            assertEquals(new BigDecimal("75.0"), result);

            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("75.001"), Alert.OPERATOR_LESS_EQUALS);
            assertEquals(new BigDecimal("75.0"), result);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }
    
    private void testDoesMeetThresholdCriteria_Average_Equals(Method method, List<MetricTimestampAndValue> metricTimestampsAndValues) {
        
        BigDecimal result;
        
        try {
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("74.999"), Alert.OPERATOR_EQUALS);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("75"), Alert.OPERATOR_EQUALS);
            assertEquals(new BigDecimal("75.0"), result);

            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("75.001"), Alert.OPERATOR_EQUALS);
            assertEquals(null, result);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }
    
    /**
     * Test of doesMeetThresholdCriteria_AtLeastCount method, of class AlertThread.
     */
    @Test
    public void testDoesMeetThresholdCriteria_AtLeastCount() {
        
        try { 
            Method method = AlertThread.class.getDeclaredMethod("doesMeetThresholdCriteria_AtLeastCount", List.class, BigDecimal.class, Integer.class, Integer.class);
            method.setAccessible(true);
            
            testDoesMeetThresholdCriteria_AtLeastCount_Greater(method, metricTimestampsAndValues_);
            testDoesMeetThresholdCriteria_AtLeastCount_GreaterEquals(method, metricTimestampsAndValues_);
            testDoesMeetThresholdCriteria_AtLeastCount_Less(method, metricTimestampsAndValues_);
            testDoesMeetThresholdCriteria_AtLeastCount_LessEquals(method, metricTimestampsAndValues_);
            testDoesMeetThresholdCriteria_AtLeastCount_Equals(method, metricTimestampsAndValues_);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }

    private void testDoesMeetThresholdCriteria_AtLeastCount_Greater(Method method, List<MetricTimestampAndValue> metricTimestampsAndValues) {
        
        BigDecimal result;
        
        try {
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("72"), Alert.OPERATOR_GREATER, 7);
            assertEquals(new BigDecimal(8), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("72"), Alert.OPERATOR_GREATER, 8);
            assertEquals(new BigDecimal(8), result);

            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("72"), Alert.OPERATOR_GREATER, 9);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80"), Alert.OPERATOR_GREATER, 1);
            assertEquals(new BigDecimal(1), result);
           
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("79"), Alert.OPERATOR_GREATER, 2);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80.1"), Alert.OPERATOR_GREATER, 1);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80.2"), Alert.OPERATOR_GREATER, 1);
            assertEquals(null, result);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }
    
    private void testDoesMeetThresholdCriteria_AtLeastCount_GreaterEquals(Method method, List<MetricTimestampAndValue> metricTimestampsAndValues) {
        
        BigDecimal result;
        
        try {
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("72"), Alert.OPERATOR_GREATER_EQUALS, 7);
            assertEquals(new BigDecimal(9), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("72"), Alert.OPERATOR_GREATER_EQUALS, 8);
            assertEquals(new BigDecimal(9), result);

            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("72"), Alert.OPERATOR_GREATER_EQUALS, 9);
            assertEquals(new BigDecimal(9), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("79"), Alert.OPERATOR_GREATER_EQUALS, 2);
            assertEquals(new BigDecimal(2), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80"), Alert.OPERATOR_GREATER_EQUALS, 1);
            assertEquals(new BigDecimal(1), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80.1"), Alert.OPERATOR_GREATER_EQUALS, 1);
            assertEquals(new BigDecimal(1), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80.2"), Alert.OPERATOR_GREATER_EQUALS, 1);
            assertEquals(null, result);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }
    
    private void testDoesMeetThresholdCriteria_AtLeastCount_Less(Method method, List<MetricTimestampAndValue> metricTimestampsAndValues) {
        
        BigDecimal result;
        
        try {
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("78"), Alert.OPERATOR_LESS, 7);
            assertEquals(new BigDecimal(8), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("78"), Alert.OPERATOR_LESS, 8);
            assertEquals(new BigDecimal(8), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("78"), Alert.OPERATOR_LESS, 9);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("71"), Alert.OPERATOR_LESS, 2);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("70"), Alert.OPERATOR_LESS, 1);
            assertEquals(new BigDecimal(1), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("69.9"), Alert.OPERATOR_LESS, 1);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("69.8"), Alert.OPERATOR_LESS, 1);
            assertEquals(null, result);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }
    
    private void testDoesMeetThresholdCriteria_AtLeastCount_LessEquals(Method method, List<MetricTimestampAndValue> metricTimestampsAndValues) {
        
        BigDecimal result;
        
        try {
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("78"), Alert.OPERATOR_LESS_EQUALS, 7);
            assertEquals(new BigDecimal(9), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("78"), Alert.OPERATOR_LESS_EQUALS, 8);
            assertEquals(new BigDecimal(9), result);

            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("78"), Alert.OPERATOR_LESS_EQUALS, 9);
            assertEquals(new BigDecimal(9), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("71"), Alert.OPERATOR_LESS_EQUALS, 2);
            assertEquals(new BigDecimal(2), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("70"), Alert.OPERATOR_LESS_EQUALS, 1);
            assertEquals(new BigDecimal(1), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("69.9"), Alert.OPERATOR_LESS_EQUALS, 1);
            assertEquals(new BigDecimal(1), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("69.8"), Alert.OPERATOR_LESS_EQUALS, 1);
            assertEquals(null, result);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }
    
    private void testDoesMeetThresholdCriteria_AtLeastCount_Equals(Method method, List<MetricTimestampAndValue> metricTimestampsAndValues) {
        
        BigDecimal result;
        
        try {
            List<MetricTimestampAndValue> metricTimestampsAndValuesLocal = new  ArrayList<>(metricTimestampsAndValues);
            metricTimestampsAndValuesLocal.add(new MetricTimestampAndValue(new Long(701), new BigDecimal("75"), hashKeyGen_.incrementAndGet()));         
  
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValuesLocal, new BigDecimal("74"), Alert.OPERATOR_EQUALS, 0);
            assertEquals(new BigDecimal(1), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValuesLocal, new BigDecimal("74"), Alert.OPERATOR_EQUALS, 1);
            assertEquals(new BigDecimal(1), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValuesLocal, new BigDecimal("74"), Alert.OPERATOR_EQUALS, 2);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValuesLocal, new BigDecimal("75.1"), Alert.OPERATOR_EQUALS, 1);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValuesLocal, new BigDecimal("75"), Alert.OPERATOR_EQUALS, 1);
            assertEquals(new BigDecimal(2), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValuesLocal, new BigDecimal("75"), Alert.OPERATOR_EQUALS, 2);
            assertEquals(new BigDecimal(2), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValuesLocal, new BigDecimal("75"), Alert.OPERATOR_EQUALS, 3);
            assertEquals(null, result);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }
    
    /**
     * Test of doesMeetThresholdCriteria_AtMostCount method, of class AlertThread.
     */
    @Test
    public void testDoesMeetThresholdCriteria_AtMostCount() {
        
        try { 
            Method method = AlertThread.class.getDeclaredMethod("doesMeetThresholdCriteria_AtMostCount", List.class, BigDecimal.class, Integer.class, Integer.class);
            method.setAccessible(true);
            
            testDoesMeetThresholdCriteria_AtMostCount_Greater(method, metricTimestampsAndValues_);
            testDoesMeetThresholdCriteria_AtMostCount_GreaterEquals(method, metricTimestampsAndValues_);
            testDoesMeetThresholdCriteria_AtMostCount_Less(method, metricTimestampsAndValues_);
            testDoesMeetThresholdCriteria_AtMostCount_LessEquals(method, metricTimestampsAndValues_);
            testDoesMeetThresholdCriteria_AtMostCount_Equals(method, metricTimestampsAndValues_);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }

    private void testDoesMeetThresholdCriteria_AtMostCount_Greater(Method method, List<MetricTimestampAndValue> metricTimestampsAndValues) {
        
        BigDecimal result;
        
        try {
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("72"), Alert.OPERATOR_GREATER, 7);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("72"), Alert.OPERATOR_GREATER, 8);
            assertEquals(new BigDecimal(8), result);

            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("72"), Alert.OPERATOR_GREATER, 9);
            assertEquals(new BigDecimal(8), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80"), Alert.OPERATOR_GREATER, 1);
            assertEquals(new BigDecimal(1), result);
           
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("79"), Alert.OPERATOR_GREATER, 1);
            assertEquals(new BigDecimal(1), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80.1"), Alert.OPERATOR_GREATER, 1);
            assertEquals(new BigDecimal(0), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80.2"), Alert.OPERATOR_GREATER, 1);
            assertEquals(new BigDecimal(0), result);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }
    
    private void testDoesMeetThresholdCriteria_AtMostCount_GreaterEquals(Method method, List<MetricTimestampAndValue> metricTimestampsAndValues) {
        
        BigDecimal result;
        
        try {
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("72"), Alert.OPERATOR_GREATER_EQUALS, 7);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("72"), Alert.OPERATOR_GREATER_EQUALS, 8);
            assertEquals(null, result);

            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("72"), Alert.OPERATOR_GREATER_EQUALS, 9);
            assertEquals(new BigDecimal(9), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("79"), Alert.OPERATOR_GREATER_EQUALS, 1);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80"), Alert.OPERATOR_GREATER_EQUALS, 1);
            assertEquals(new BigDecimal(1), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80.1"), Alert.OPERATOR_GREATER_EQUALS, 1);
            assertEquals(new BigDecimal(1), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("80.2"), Alert.OPERATOR_GREATER_EQUALS, 1);
            assertEquals(new BigDecimal(0), result);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }
    
    private void testDoesMeetThresholdCriteria_AtMostCount_Less(Method method, List<MetricTimestampAndValue> metricTimestampsAndValues) {
        
        BigDecimal result;
        
        try {
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("78"), Alert.OPERATOR_LESS, 7);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("78"), Alert.OPERATOR_LESS, 8);
            assertEquals(new BigDecimal(8), result);

            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("78"), Alert.OPERATOR_LESS, 9);
            assertEquals(new BigDecimal(8), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("71"), Alert.OPERATOR_LESS, 1);
            assertEquals(new BigDecimal(1), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("70"), Alert.OPERATOR_LESS, 1);
            assertEquals(new BigDecimal(1), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("69.9"), Alert.OPERATOR_LESS, 1);
            assertEquals(new BigDecimal(0), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("69.8"), Alert.OPERATOR_LESS, 1);
            assertEquals(new BigDecimal(0), result);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }
    
    private void testDoesMeetThresholdCriteria_AtMostCount_LessEquals(Method method, List<MetricTimestampAndValue> metricTimestampsAndValues) {
        
        BigDecimal result;
        
        try {
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("78"), Alert.OPERATOR_LESS_EQUALS, 7);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("78"), Alert.OPERATOR_LESS_EQUALS, 8);
            assertEquals(null, result);

            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("78"), Alert.OPERATOR_LESS_EQUALS, 9);
            assertEquals(new BigDecimal(9), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("71"), Alert.OPERATOR_LESS_EQUALS, 1);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("70"), Alert.OPERATOR_LESS_EQUALS, 1);
            assertEquals(new BigDecimal(1), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("69.9"), Alert.OPERATOR_LESS_EQUALS, 1);
            assertEquals(new BigDecimal(1), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValues, new BigDecimal("69.8"), Alert.OPERATOR_LESS_EQUALS, 1);
            assertEquals(new BigDecimal(0), result);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }
    
    private void testDoesMeetThresholdCriteria_AtMostCount_Equals(Method method, List<MetricTimestampAndValue> metricTimestampsAndValues) {
        
        BigDecimal result;
        
        try {
            List<MetricTimestampAndValue> metricTimestampsAndValuesLocal = new  ArrayList<>(metricTimestampsAndValues);
            metricTimestampsAndValuesLocal.add(new MetricTimestampAndValue((long) 701, new BigDecimal("75"), hashKeyGen_.incrementAndGet()));         

            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValuesLocal, new BigDecimal("74"), Alert.OPERATOR_EQUALS, 0);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValuesLocal, new BigDecimal("74"), Alert.OPERATOR_EQUALS, 1);
            assertEquals(new BigDecimal(1), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValuesLocal, new BigDecimal("75.1"), Alert.OPERATOR_EQUALS, 1);
            assertEquals(new BigDecimal(0), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValuesLocal, new BigDecimal("75"), Alert.OPERATOR_EQUALS, 1);
            assertEquals(null, result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValuesLocal, new BigDecimal("75"), Alert.OPERATOR_EQUALS, 2);
            assertEquals(new BigDecimal(2), result);
            
            result = (BigDecimal) method.invoke(alertThread_, metricTimestampsAndValuesLocal, new BigDecimal("75"), Alert.OPERATOR_EQUALS, 3);
            assertEquals(new BigDecimal(2), result);
        }
        catch (Exception e) {
            System.out.println(StackTrace.getStringFromStackTrace(e));
            fail("Hit exception");
        }
    }
    
}