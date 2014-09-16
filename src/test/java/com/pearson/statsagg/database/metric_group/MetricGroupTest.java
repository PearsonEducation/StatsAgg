package com.pearson.statsagg.database.metric_group;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroupTest {
    
    public MetricGroupTest() {
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
     * Test of isEqual method, of class MetricGroup.
     */
    @Test
    public void testIsEqual() {
        MetricGroup metricGroup1 = new MetricGroup(1, "MetricGroup JUnit1 Name", "MetricGroup JUnit1 Desc");
        MetricGroup metricGroup2 = new MetricGroup(1, "MetricGroup JUnit1 Name", "MetricGroup JUnit1 Desc");
        assertTrue(metricGroup1.isEqual(metricGroup2));
        
        metricGroup1.setId(-1);
        assertFalse(metricGroup1.isEqual(metricGroup2));
        metricGroup1.setId(1);
        
        metricGroup1.setName("MetricGroup JUnit1 Name Fake");
        assertFalse(metricGroup1.isEqual(metricGroup2));
        metricGroup1.setName("MetricGroup JUnit1 Name");
        
        metricGroup1.setDescription("MetricGroup JUnit1 Desc Fake");
        assertFalse(metricGroup1.isEqual(metricGroup2));
        metricGroup1.setDescription("MetricGroup JUnit1 Desc");
        
        assertTrue(metricGroup1.isEqual(metricGroup2));
    }

    /**
     * Test of copy method, of class MetricGroup.
     */
    @Test
    public void testCopy() {
        MetricGroup metricGroup1 = new MetricGroup(1, "MetricGroup JUnit1 Name", "MetricGroup JUnit1 Desc");
        
        MetricGroup metricGroup2 = MetricGroup.copy(metricGroup1);
        assertTrue(metricGroup1.isEqual(metricGroup2));
        
        metricGroup1.setId(-1);
        assertFalse(metricGroup1.isEqual(metricGroup2));
        assertTrue(metricGroup2.getId() == 1);
        metricGroup1.setId(1);
        
        metricGroup1.setName("MetricGroup JUnit1 Name Bad");
        assertFalse(metricGroup1.isEqual(metricGroup2));
        assertTrue(metricGroup2.getName().equals("MetricGroup JUnit1 Name"));
        metricGroup1.setName("MetricGroup JUnit1 Name");
        
        metricGroup1.setDescription("MetricGroup JUnit1 Desc Bad");
        assertFalse(metricGroup1.isEqual(metricGroup2));
        assertTrue(metricGroup2.getDescription().equals("MetricGroup JUnit1 Desc"));
        metricGroup1.setDescription("MetricGroup JUnit1 Desc");
        
        assertTrue(metricGroup1.isEqual(metricGroup2));
    }

}
