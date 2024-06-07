package com.pearson.statsagg.database_objects.alerts;

import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Jeffrey Schmidt
 */
public class AlertTest {
    
    public AlertTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
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
     * Test of copy method, of class Alert.
     * Note -- this is not a comprehensive test. It just covers the various object types.
     */
    @Test
    public void testCopy() {
        long currentTimeInMs = System.currentTimeMillis();
        
        Alert alert1 = new Alert(1, "Alert JUnit1 Name", "Alert JUnit1 Desc" , 11, 12, 555, false, true, true, Alert.TYPE_THRESHOLD, true, true, 300000l, DatabaseObjectCommon.TIME_UNIT_SECONDS,
            666, 667, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 1, false, null, false, null, null,
            666, 667, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("200"), 1000L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 2, true, new Timestamp(currentTimeInMs), false, null, null);
        
        Alert alert2 = Alert.copy(alert1);
        assertTrue(alert1.isEqual(alert2));
        
        alert1.setId(-1);
        assertFalse(alert1.isEqual(alert2));
        assertTrue(alert2.getId() == 1);
        alert1.setId(1);
        
        alert1.setDescription("Alert JUnit1 Desc Bad");
        assertFalse(alert1.isEqual(alert2));
        assertTrue(alert2.getDescription().equals("Alert JUnit1 Desc"));
        alert1.setDescription("Alert JUnit1 Desc");
       
        alert1.setCautionThreshold(BigDecimal.TEN);
        assertFalse(alert1.isEqual(alert2));
        assertTrue(alert2.getCautionThreshold().equals(new BigDecimal("100")));
        alert1.setCautionThreshold(new BigDecimal("100"));
        
        alert1.setDangerOperator(Alert.OPERATOR_GREATER_EQUALS);
        assertFalse(alert1.isEqual(alert2));
        assertTrue(Objects.equals(alert2.getDangerOperator(), Alert.OPERATOR_GREATER));
        alert1.setDangerOperator(Alert.OPERATOR_GREATER);
        
        alert1.setIsDangerAlertActive(false);
        assertFalse(alert1.isEqual(alert2));
        assertTrue(alert2.isDangerAlertActive() == true);
        alert1.setIsDangerAlertActive(true);
        
        alert1.setDangerAlertLastSentTimestamp(new Timestamp(System.currentTimeMillis() - 360000));
        assertFalse(alert1.isEqual(alert2));
        assertTrue(alert2.getDangerAlertLastSentTimestamp().getTime() == currentTimeInMs);
        alert1.setDangerAlertLastSentTimestamp(new Timestamp(currentTimeInMs));
        
        assertTrue(alert1.isEqual(alert2));
    }

    /**
     * Test of isEqual method, of class Alert.
     */
    @Test
    public void testIsEqual() {
    }
   
    /**
     * Test of isValid_CautionOperation method, of class Alert.
     */
    @Test
    public void testIsValid_CautionOperation() {
        
        Alert alert1 = new Alert(1, "Alert JUnit1 Name", "Alert JUnit1 Desc" , null, null, 555, false, true, true, Alert.TYPE_THRESHOLD, true, true, 300000l, DatabaseObjectCommon.TIME_UNIT_SECONDS, 
            666, 667, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 1, false, new Timestamp(System.currentTimeMillis()), false, null, null, 
            666, 667, null, null, null, null, null, null, null, null, null, null, null, false, null, null);
        
        assertTrue(alert1.isValid_CautionOperation());
        
        alert1.setCautionOperator(Alert.OPERATOR_GREATER);
        assertTrue(alert1.isValid_CautionOperation());

        alert1.setCautionOperator(Alert.OPERATOR_GREATER_EQUALS);
        assertTrue(alert1.isValid_CautionOperation());
        
        alert1.setCautionOperator(Alert.OPERATOR_LESS);
        assertTrue(alert1.isValid_CautionOperation());
        
        alert1.setCautionOperator(Alert.OPERATOR_LESS_EQUALS);
        assertTrue(alert1.isValid_CautionOperation());
        
        alert1.setCautionOperator(Alert.OPERATOR_EQUALS);
        assertTrue(alert1.isValid_CautionOperation());
        
        alert1.setCautionOperator(0);
        assertFalse(alert1.isValid_CautionOperation());
        
        alert1.setCautionOperator(6);
        assertFalse(alert1.isValid_CautionOperation());
        
        alert1.setCautionOperator(null);
        assertFalse(alert1.isValid_CautionOperation());
    }
    
    /**
     * Test of isValid_DangerOperation method, of class Alert.
     */
    @Test
    public void testIsValid_DangerOperation() {
        
        Alert alert1 = new Alert(1, "Alert JUnit1 Name", "Alert JUnit1 Desc" , null, null, 555, false, true, true, Alert.TYPE_THRESHOLD, true, true, 300000l, DatabaseObjectCommon.TIME_UNIT_SECONDS, 
            666, 667, null, null, null, null, null, null, null, null, null, null, null, false, null, null,
            666, 667, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 1, false, new Timestamp(System.currentTimeMillis()), false, null, null);
        
        assertTrue(alert1.isValid_DangerOperation());
        
        alert1.setDangerOperator(Alert.OPERATOR_GREATER);
        assertTrue(alert1.isValid_DangerOperation());

        alert1.setDangerOperator(Alert.OPERATOR_GREATER_EQUALS);
        assertTrue(alert1.isValid_DangerOperation());
        
        alert1.setDangerOperator(Alert.OPERATOR_LESS);
        assertTrue(alert1.isValid_DangerOperation());
        
        alert1.setDangerOperator(Alert.OPERATOR_LESS_EQUALS);
        assertTrue(alert1.isValid_DangerOperation());
        
        alert1.setDangerOperator(Alert.OPERATOR_EQUALS);
        assertTrue(alert1.isValid_DangerOperation());
        
        alert1.setDangerOperator(0);
        assertFalse(alert1.isValid_DangerOperation());
        
        alert1.setDangerOperator(6);
        assertFalse(alert1.isValid_DangerOperation());
        
        alert1.setDangerOperator(null);
        assertFalse(alert1.isValid_DangerOperation());
    }
    
    /**
     * Test of isValid_CautionCombination method, of class Alert.
     */
    @Test
    public void testIsValid_CautionCombination() {
        
        Alert alert1 = new Alert(1, "Alert JUnit1 Name", "Alert JUnit1 Desc" , null, null, 555, false, true, true, Alert.TYPE_THRESHOLD, true, true, 300000l, DatabaseObjectCommon.TIME_UNIT_SECONDS, 
            666, 667, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 1, false, new Timestamp(System.currentTimeMillis()), false, null, null,
            666, 667, null, null, null, null, null, null, null, null, null, null, null, false, null, null);
        
        assertTrue(alert1.isValid_CautionCombination());
        
        alert1.setCautionCombination(Alert.COMBINATION_ALL);
        assertTrue(alert1.isValid_CautionCombination());

        alert1.setCautionCombination(Alert.COMBINATION_ANY);
        assertTrue(alert1.isValid_CautionCombination());
        
        alert1.setCautionCombination(Alert.COMBINATION_AVERAGE);
        assertTrue(alert1.isValid_CautionCombination());
        
        alert1.setCautionCombination(Alert.COMBINATION_AT_LEAST_COUNT);
        assertFalse(alert1.isValid_CautionCombination());
        
        alert1.setCautionCombination(Alert.COMBINATION_AT_MOST_COUNT);
        assertFalse(alert1.isValid_CautionCombination());
        
        alert1.setCautionCombinationCount(3);
        alert1.setCautionCombination(Alert.COMBINATION_AT_LEAST_COUNT);
        assertTrue(alert1.isValid_CautionCombination());
        alert1.setCautionCombination(Alert.COMBINATION_AT_MOST_COUNT);
        assertTrue(alert1.isValid_CautionCombination());
        
        alert1.setCautionCombinationCount(-1);
        alert1.setCautionCombination(Alert.COMBINATION_AT_LEAST_COUNT);
        assertFalse(alert1.isValid_CautionCombination());
        alert1.setCautionCombination(Alert.COMBINATION_AT_MOST_COUNT);
        assertFalse(alert1.isValid_CautionCombination());
        alert1.setCautionCombinationCount(3);
        
        alert1.setCautionCombination(100);
        assertFalse(alert1.isValid_CautionCombination());
        
        alert1.setCautionCombination(104);
        assertFalse(alert1.isValid_CautionCombination());
        
        alert1.setCautionCombination(107);
        assertFalse(alert1.isValid_CautionCombination());
        
        alert1.setCautionCombination(null);
        assertFalse(alert1.isValid_CautionCombination());
    }
    
    /**
     * Test of isValid_DangerCombination method, of class Alert.
     */
    @Test
    public void testIsValid_DangerCombination() {
        
        Alert alert1 = new Alert(1, "Alert JUnit1 Name", "Alert JUnit1 Desc" , null, null, 555, false, true, true, Alert.TYPE_THRESHOLD, true, true, 300000l, DatabaseObjectCommon.TIME_UNIT_SECONDS, 
            666, 667, null, null, null, null, null, null, null, null, null, null, null, false, null, null,
            666, 667, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 1, false, new Timestamp(System.currentTimeMillis()), false, null, null);
        
        assertTrue(alert1.isValid_DangerCombination());
        
        alert1.setDangerCombination(Alert.COMBINATION_ALL);
        assertTrue(alert1.isValid_DangerCombination());

        alert1.setDangerCombination(Alert.COMBINATION_ANY);
        assertTrue(alert1.isValid_DangerCombination());
        
        alert1.setDangerCombination(Alert.COMBINATION_AVERAGE);
        assertTrue(alert1.isValid_DangerCombination());
        
        alert1.setDangerCombination(Alert.COMBINATION_AT_LEAST_COUNT);
        assertFalse(alert1.isValid_DangerCombination());
        
        alert1.setDangerCombination(Alert.COMBINATION_AT_MOST_COUNT);
        assertFalse(alert1.isValid_DangerCombination());
        
        alert1.setDangerCombinationCount(3);
        alert1.setDangerCombination(Alert.COMBINATION_AT_LEAST_COUNT);
        assertTrue(alert1.isValid_DangerCombination());
        alert1.setDangerCombination(Alert.COMBINATION_AT_MOST_COUNT);
        assertTrue(alert1.isValid_DangerCombination());
        
        alert1.setDangerCombinationCount(-1);
        alert1.setDangerCombination(Alert.COMBINATION_AT_LEAST_COUNT);
        assertFalse(alert1.isValid_DangerCombination());
        alert1.setDangerCombination(Alert.COMBINATION_AT_MOST_COUNT);
        assertFalse(alert1.isValid_DangerCombination());
        alert1.setDangerCombinationCount(3);
        
        alert1.setDangerCombination(100);
        assertFalse(alert1.isValid_DangerCombination());

        alert1.setDangerCombination(104);
        assertFalse(alert1.isValid_DangerCombination());
        
        alert1.setDangerCombination(107);
        assertFalse(alert1.isValid_DangerCombination());
        
        alert1.setDangerCombination(null);
        assertFalse(alert1.isValid_DangerCombination());
    }
    
    /**
     * Test of isValid_CautionWindowDuration method, of class Alert.
     */
    @Test
    public void testIsValid_CautionWindowDuration() {
        
        Alert alert1 = new Alert(1, "Alert JUnit1 Name", "Alert JUnit1 Desc" , null, null, 555, false, true, true, Alert.TYPE_THRESHOLD, true, true, 300000l, DatabaseObjectCommon.TIME_UNIT_SECONDS, 
            666, 667, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 1, false, new Timestamp(System.currentTimeMillis()), false, null, null,
            666, 667, null, null, null, null, null, null, null, null, null, null, null, false, null, null);
        
        assertTrue(alert1.isValid_CautionWindowDuration());
        
        alert1.setCautionWindowDuration(0L);
        assertFalse(alert1.isValid_CautionWindowDuration());
        
        alert1.setCautionWindowDuration(-1L);
        assertFalse(alert1.isValid_CautionWindowDuration());
        
        alert1.setCautionWindowDuration(100L);
        assertTrue(alert1.isValid_CautionWindowDuration());
        
        alert1.setCautionWindowDuration(null);
        assertFalse(alert1.isValid_CautionWindowDuration());
    }
    
    /**
     * Test of isValid_DangerWindowDuration method, of class Alert.
     */
    @Test
    public void testIsValid_DangerWindowDuration() {
        
        Alert alert1 = new Alert(1, "Alert JUnit1 Name", "Alert JUnit1 Desc" , null, null, 555, false, true, true, Alert.TYPE_THRESHOLD, true, true, 300000l, DatabaseObjectCommon.TIME_UNIT_SECONDS, 
            666, 667, null, null, null, null, null, null, null, null, null, null, null, false, null, null,
            666, 667, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 1, false, new Timestamp(System.currentTimeMillis()), false, null, null);
        
        assertTrue(alert1.isValid_DangerWindowDuration());
        
        alert1.setDangerWindowDuration(0L);
        assertFalse(alert1.isValid_DangerWindowDuration());
        
        alert1.setDangerWindowDuration(-1L);
        assertFalse(alert1.isValid_DangerWindowDuration());
        
        alert1.setDangerWindowDuration(100L);
        assertTrue(alert1.isValid_DangerWindowDuration());
        
        alert1.setDangerWindowDuration(null);
        assertFalse(alert1.isValid_DangerWindowDuration());
    }
    
    /**
     * Test of isValid_CautionMinimumSampleCount method, of class Alert.
     */
    @Test
    public void testIsValid_CautionMinimumSampleCount() {
        
        Alert alert1 = new Alert(1, "Alert JUnit1 Name", "Alert JUnit1 Desc" , null, null, 555, false, true, true, Alert.TYPE_THRESHOLD, true, true, 300000l, DatabaseObjectCommon.TIME_UNIT_SECONDS, 
            666, 667, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 1, false, new Timestamp(System.currentTimeMillis()), false, null, null,
            666, 667, null, null, null, null, null, null, null, null, null, null, null, false, null, null);
        
        assertTrue(alert1.isValid_CautionMinimumSampleCount());
        
        alert1.setCautionMinimumSampleCount(0);
        assertFalse(alert1.isValid_CautionMinimumSampleCount());
        
        alert1.setCautionMinimumSampleCount(-1);
        assertFalse(alert1.isValid_CautionMinimumSampleCount());
        
        alert1.setCautionMinimumSampleCount(100);
        assertTrue(alert1.isValid_CautionMinimumSampleCount());
        
        alert1.setCautionMinimumSampleCount(null);
        assertFalse(alert1.isValid_CautionMinimumSampleCount());
    }
    
    /**
     * Test of isValid_DangerMinimumSampleCount method, of class Alert.
     */
    @Test
    public void testIsValid_DangerMinimumSampleCount() {
        
        Alert alert1 = new Alert(1, "Alert JUnit1 Name", "Alert JUnit1 Desc" , null, null, 555, false, true, true, Alert.TYPE_THRESHOLD, true, true, 300000l, DatabaseObjectCommon.TIME_UNIT_SECONDS, 
            666, 667, null, null, null, null, null, null, null, null, null, null, null, false, null, null,  
            666, 667, Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 1, false, new Timestamp(System.currentTimeMillis()), false, null, null);
        
        assertTrue(alert1.isValid_DangerMinimumSampleCount());
        
        alert1.setDangerMinimumSampleCount(0);
        assertFalse(alert1.isValid_DangerMinimumSampleCount());
        
        alert1.setDangerMinimumSampleCount(-1);
        assertFalse(alert1.isValid_DangerMinimumSampleCount());
        
        alert1.setDangerMinimumSampleCount(100);
        assertTrue(alert1.isValid_DangerMinimumSampleCount());
        
        alert1.setDangerMinimumSampleCount(null);
        assertFalse(alert1.isValid_DangerMinimumSampleCount());
    }
    
}
