package com.pearson.statsagg.database_objects.metric_groups;

import com.pearson.statsagg.database_objects.metric_groups.MetricGroupRegex;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroupRegexesTest {
    
    public MetricGroupRegexesTest() {
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
     * Test of isEqual method, of class MetricGroupRegexes.
     */
    @Test
    public void testIsEqual() {
        MetricGroupRegex metricGroupRegex1 = new MetricGroupRegex(1, 55, true, "SomePattern");
        MetricGroupRegex metricGroupRegex2 = new MetricGroupRegex(1, 55, true, "SomePattern");
        
        assertTrue(metricGroupRegex1.isEqual(metricGroupRegex2));
        
        metricGroupRegex1.setId(-1);
        assertFalse(metricGroupRegex1.isEqual(metricGroupRegex2));
        metricGroupRegex1.setId(1);
        
        metricGroupRegex1.setMetricGroupId(66);
        assertFalse(metricGroupRegex1.isEqual(metricGroupRegex2));
        metricGroupRegex1.setMetricGroupId(55);
        
        metricGroupRegex1.setPattern("SomePattern Bad");
        assertFalse(metricGroupRegex1.isEqual(metricGroupRegex2));
        metricGroupRegex1.setPattern("SomePattern");
        
        metricGroupRegex1.setIsBlacklistRegex(false);
        assertFalse(metricGroupRegex1.isEqual(metricGroupRegex2));
        metricGroupRegex1.setIsBlacklistRegex(true);
        
        assertTrue(metricGroupRegex1.isEqual(metricGroupRegex2));
    }

    /**
     * Test of copy method, of class MetricGroupRegexes.
     */
    @Test
    public void testCopy() {
        MetricGroupRegex metricGroupRegex1 = new MetricGroupRegex(1, 55, true, "SomePattern");
        
        MetricGroupRegex metricGroupRegex2 = MetricGroupRegex.copy(metricGroupRegex1);
        assertTrue(metricGroupRegex1.isEqual(metricGroupRegex2));
        
        metricGroupRegex1.setId(-1);
        assertFalse(metricGroupRegex1.isEqual(metricGroupRegex2));
        assertTrue(metricGroupRegex2.getId() == 1);
        metricGroupRegex1.setId(1);
        
        metricGroupRegex1.setMetricGroupId(66);
        assertFalse(metricGroupRegex1.isEqual(metricGroupRegex2));
        assertTrue(metricGroupRegex2.getMetricGroupId() == 55);
        metricGroupRegex1.setMetricGroupId(55);
        
        metricGroupRegex1.setIsBlacklistRegex(false);
        assertFalse(metricGroupRegex1.isEqual(metricGroupRegex2));
        assertTrue(metricGroupRegex2.isBlacklistRegex() == true);
        metricGroupRegex1.setIsBlacklistRegex(true);

        metricGroupRegex1.setPattern("SomePattern Bad");
        assertFalse(metricGroupRegex1.isEqual(metricGroupRegex2));
        assertTrue(metricGroupRegex2.getPattern().equals("SomePattern"));
        metricGroupRegex1.setPattern("SomePattern");
        
        assertTrue(metricGroupRegex1.isEqual(metricGroupRegex2));
    }

}
