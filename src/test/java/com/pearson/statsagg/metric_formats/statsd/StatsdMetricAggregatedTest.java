package com.pearson.statsagg.metric_formats.statsd;

import java.math.BigDecimal;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jeffrey Schmidt
 */
public class StatsdMetricAggregatedTest {
    
    public StatsdMetricAggregatedTest() {
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
     * Test of getGraphiteFormatString method, of class StatsdMetricAggregator.
     */
    @Test
    public void testGetGraphiteFormatString() {
        long currentTime = System.currentTimeMillis();
        int currentTimeInSeconds = (int) (currentTime / 1000);
        
        StatsdMetricAggregated statsdMetricAggregated = new StatsdMetricAggregated("aggregated..counterMetric@-\\/#$%^_123AaZz09.", 
                new BigDecimal("1.2222000000"), currentTime, StatsdMetricAggregated.COUNTER_TYPE);
        
        // unsanitized, without substitution
        assertEquals(("aggregated..counterMetric@-\\/#$%^_123AaZz09. 1.2222 " + currentTimeInSeconds), statsdMetricAggregated.getGraphiteFormatString(false, false));

        // sanitized, without substitution
        assertEquals(("aggregated.counterMetric@-\\/#$%^_123AaZz09. 1.2222 " + currentTimeInSeconds), statsdMetricAggregated.getGraphiteFormatString(true, false));

        // sanitized, with substitution
        assertEquals(("aggregated.counterMetric@-||#$Pct^_123AaZz09. 1.2222 " + currentTimeInSeconds), statsdMetricAggregated.getGraphiteFormatString(true, true));

        // unsanitized, with substitution
        assertEquals(("aggregated..counterMetric@-||#$Pct^_123AaZz09. 1.2222 " + currentTimeInSeconds), statsdMetricAggregated.getGraphiteFormatString(false, true));
    }

    /**
     * Test of getOpenTsdbTelnetFormatString method, of class StatsdMetricAggregator.
     */
    @Test
    public void testGetOpenTsdbTelnetFormatString() {
        long currentTime = System.currentTimeMillis();
        
        StatsdMetricAggregated statsdMetricAggregated1 = new StatsdMetricAggregated("aggregated.counterMetric@#$%^_123AaZz09", 
                new BigDecimal("1.2222000000"), currentTime, StatsdMetricAggregated.COUNTER_TYPE);
           
        // unsanitized
        assertEquals(("aggregated.counterMetric@#$%^_123AaZz09 " + currentTime + " 1.2222 " + "Format=StatsD"), statsdMetricAggregated1.getOpenTsdbTelnetFormatString(false));
        
        // sanitized
        assertEquals(("aggregated.counterMetric_123AaZz09 " + currentTime + " 1.2222 " + "Format=StatsD"), statsdMetricAggregated1.getOpenTsdbTelnetFormatString(true));
    }
    
    /**
     * Test of getOpenTsdbJsonFormatString method, of class StatsdMetricAggregator.
     */
    @Test
    public void testGetOpenTsdbJsonFormatString() {
        long currentTime = System.currentTimeMillis();
        
        StatsdMetricAggregated statsdMetricAggregated1 = new StatsdMetricAggregated("aggregated.counterMetric@#$%^_123AaZz09", 
                new BigDecimal("1.2222000000"), currentTime, StatsdMetricAggregated.COUNTER_TYPE);
        
        // unsanitized   
        assertEquals("{\"metric\":\"aggregated.counterMetric@#$%^_123AaZz09\",\"timestamp\":" + currentTime + ",\"value\":1.2222,\"tags\":{\"Format\":\"StatsD\"}}", 
                statsdMetricAggregated1.getOpenTsdbJsonFormatString(false));
        
        // sanitized
        assertEquals("{\"metric\":\"aggregated.counterMetric_123AaZz09\",\"timestamp\":" + currentTime + ",\"value\":1.2222,\"tags\":{\"Format\":\"StatsD\"}}", 
                statsdMetricAggregated1.getOpenTsdbJsonFormatString(true));
    }
    
    /**
     * Test of getInfluxdbV1JsonFormatString method, of class StatsdMetricAggregator.
     */
    @Test
    public void testGetInfluxdbV1JsonFormatString() {
        long currentTime = System.currentTimeMillis();
        
        StatsdMetricAggregated statsdMetricAggregated = new StatsdMetricAggregated("aggregated.counterMetric@#$%^_123AaZz09", 
                new BigDecimal("1.2222000000"), currentTime, StatsdMetricAggregated.COUNTER_TYPE);
                
        assertEquals("{\"name\":\"aggregated.counterMetric@#$%^_123AaZz09\",\"columns\":[\"value\",\"time\"],\"points\":[[1.2222," + currentTime + "]]}", statsdMetricAggregated.getInfluxdbV1JsonFormatString());
    }
    
}
