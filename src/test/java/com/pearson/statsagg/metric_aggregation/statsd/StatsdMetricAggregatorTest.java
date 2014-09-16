package com.pearson.statsagg.metric_aggregation.statsd;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.pearson.statsagg.utilities.Threads;
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
public class StatsdMetricAggregatorTest {
    
    public StatsdMetricAggregatorTest() {
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
     * Test of aggregateStatsdMetrics method, of class StatsdMetricAggregator.
     */
    @Test
    public void testAggregateStatsdMetrics() {

    }

    /**
     * Test of getStatsdMetricsRawByMetricType method, of class StatsdMetricAggregator.
     */
    @Test
    public void testGetStatsdMetricsRawByMetricType() {

    }

    /**
     * Test of getStatsdMetricsRawByMetricTypeKey method, of class StatsdMetricAggregator.
     */
    @Test
    public void testGetStatsdMetricsRawByMetricTypeKey() {

    }

    /**
     * Test of getStatsdMetricsRawExcludeMetricType method, of class StatsdMetricAggregator.
     */
    @Test
    public void testGetStatsdMetricsRawExcludeMetricType() {

    }

    /**
     * Test of getStatsdMetricsRawExcludeMetricTypeKey method, of class StatsdMetricAggregator.
     */
    @Test
    public void testGetStatsdMetricsRawExcludeMetricTypeKey() {

    }

    /**
     * Test of divideStatsdMetricsRawByMetricTypeKey method, of class StatsdMetricAggregator.
     */
    @Test
    public void testDivideStatsdMetricsRawByMetricTypeKey() {

    }

    /**
     * Test of divideStatsdMetricsRawByBucket method, of class StatsdMetricAggregator.
     */
    @Test
    public void testDivideStatsdMetricsRawByBucket() {

    }

    /**
     * Test of aggregateCounter method, of class StatsdMetricAggregator.
     */
    @Test
    public void testAggregateCounter() {
        List<StatsdMetricRaw> statsdMetricsRaw = new ArrayList<>();
        
        StatsdMetricRaw statsdMetricRaw1 = StatsdMetricRaw.parseStatsdMetricRaw("counterMetric:100|c");
        StatsdMetricRaw statsdMetricRaw2 = StatsdMetricRaw.parseStatsdMetricRaw("counterMetric:+800.3|c");
        StatsdMetricRaw statsdMetricRaw3 = StatsdMetricRaw.parseStatsdMetricRaw("counterMetric:-215.1|c");
        StatsdMetricRaw statsdMetricRaw4 = StatsdMetricRaw.parseStatsdMetricRaw("counterMetric:237.5|c|@0.1");
        StatsdMetricRaw statsdMetricRaw5 = StatsdMetricRaw.parseStatsdMetricRaw("counterMetric:66.18|c|@-0.1");
        statsdMetricsRaw.add(statsdMetricRaw1); statsdMetricsRaw.add(statsdMetricRaw2); 
        statsdMetricsRaw.add(statsdMetricRaw3); statsdMetricsRaw.add(statsdMetricRaw4);
        statsdMetricsRaw.add(statsdMetricRaw5);
        
        statsdMetricRaw1.setHashKey(new Long(1)); statsdMetricRaw2.setHashKey(new Long(2)); 
        statsdMetricRaw3.setHashKey(new Long(3)); statsdMetricRaw4.setHashKey(new Long(4)); 
        statsdMetricRaw5.setHashKey(new Long(5)); 
        
        long timestampAverage = 0;
        for (StatsdMetricRaw statsdMetricRaw : statsdMetricsRaw) {
            timestampAverage += statsdMetricRaw.getMetricReceivedTimestampInMilliseconds();
        }
        timestampAverage = Math.round((double) ((double) timestampAverage / (double) statsdMetricsRaw.size()));
        
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateCounter(statsdMetricsRaw, 10000, ".");
        
        int matchCount = 0;
        for (StatsdMetricAggregated statsdMetricAggregated : statsdMetricsAggregated) {
            if (statsdMetricAggregated.getBucket().equals("counterMetric.Count")) {
                assertEquals("counterMetric.Count", statsdMetricAggregated.getBucket());
                assertEquals(new BigDecimal("3126.38"), statsdMetricAggregated.getMetricValue());
                assertEquals(timestampAverage, statsdMetricAggregated.getMetricTimestampInMilliseconds().longValue());
                assertEquals(timestampAverage, statsdMetricAggregated.getMetricReceivedTimestampInMilliseconds().longValue());
                assertEquals(timestampAverage / 1000, statsdMetricAggregated.getMetricTimestampInSeconds());
                assertEquals(StatsdMetricAggregated.COUNTER_TYPE, statsdMetricAggregated.getMetricTypeKey());
                matchCount++;
            }
            else if (statsdMetricAggregated.getBucket().equals("counterMetric.PerSecondRate")) {
                assertEquals("counterMetric.PerSecondRate", statsdMetricAggregated.getBucket());
                assertEquals(new BigDecimal("312.638"), statsdMetricAggregated.getMetricValue());
                assertEquals(timestampAverage, statsdMetricAggregated.getMetricTimestampInMilliseconds().longValue());
                assertEquals(timestampAverage, statsdMetricAggregated.getMetricReceivedTimestampInMilliseconds().longValue());
                assertEquals(timestampAverage / 1000, statsdMetricAggregated.getMetricTimestampInSeconds());
                assertEquals(StatsdMetricAggregated.COUNTER_TYPE, statsdMetricAggregated.getMetricTypeKey());
                matchCount++;
            }
        }
        
        assertEquals(matchCount, statsdMetricsAggregated.size());

    }

    /**
     * Test of aggregateTimer method, of class StatsdMetricAggregator.
     */
    @Test
    public void testAggregateTimer() {

    }

    /**
     * Test of aggregateGauge method, of class StatsdMetricAggregator.
     */
    @Test
    public void testAggregateGauge() {
        List<StatsdMetricRaw> statsdMetricsRaw = new ArrayList<>();
        
        StatsdMetricRaw statsdMetricRaw1 = StatsdMetricRaw.parseStatsdMetricRaw("gaugeMetric:100|g\r\n");
        Threads.sleepSeconds(0.2);
        StatsdMetricRaw statsdMetricRaw2 = StatsdMetricRaw.parseStatsdMetricRaw("gaugeMetric:+800.3|g");
        StatsdMetricRaw statsdMetricRaw3 = StatsdMetricRaw.parseStatsdMetricRaw("gaugeMetric:-215.1|g");
        Threads.sleepSeconds(0.7);
        StatsdMetricRaw statsdMetricRaw4 = StatsdMetricRaw.parseStatsdMetricRaw("gaugeMetric:237.5|g");
        statsdMetricsRaw.add(statsdMetricRaw1); statsdMetricsRaw.add(statsdMetricRaw2); statsdMetricsRaw.add(statsdMetricRaw3); statsdMetricsRaw.add(statsdMetricRaw4);
        
        statsdMetricRaw1.setHashKey(new Long(1)); statsdMetricRaw2.setHashKey(new Long(2)); statsdMetricRaw3.setHashKey(new Long(3)); statsdMetricRaw4.setHashKey(new Long(4)); 
        StatsdMetricAggregated statsdMetricAggregated = StatsdMetricAggregator.aggregateGauge(statsdMetricsRaw, null);
        assertEquals(new BigDecimal("237.5"), statsdMetricAggregated.getMetricValue());
        
        statsdMetricRaw1.setHashKey(new Long(2)); statsdMetricRaw2.setHashKey(new Long(1)); statsdMetricRaw3.setHashKey(new Long(4)); statsdMetricRaw4.setHashKey(new Long(3)); 
        statsdMetricAggregated = StatsdMetricAggregator.aggregateGauge(statsdMetricsRaw, null);
        assertEquals(new BigDecimal("22.4"), statsdMetricAggregated.getMetricValue());
        
        statsdMetricRaw1.setHashKey(new Long(4)); statsdMetricRaw2.setHashKey(new Long(1)); statsdMetricRaw3.setHashKey(new Long(2)); statsdMetricRaw4.setHashKey(new Long(3)); 
        statsdMetricAggregated = StatsdMetricAggregator.aggregateGauge(statsdMetricsRaw, null);
        assertEquals(new BigDecimal("100"), statsdMetricAggregated.getMetricValue());
        
        statsdMetricRaw1.setHashKey(new Long(2)); statsdMetricRaw2.setHashKey(new Long(3)); statsdMetricRaw3.setHashKey(new Long(4)); statsdMetricRaw4.setHashKey(new Long(1)); 
        statsdMetricAggregated = StatsdMetricAggregator.aggregateGauge(statsdMetricsRaw, null);
        assertEquals(new BigDecimal("685.2"), statsdMetricAggregated.getMetricValue());
        
        long timestampAverage = 0;
        for (StatsdMetricRaw statsdMetricRaw : statsdMetricsRaw) {
            timestampAverage += statsdMetricRaw.getMetricReceivedTimestampInMilliseconds();
        }
        timestampAverage = Math.round((double) ((double) timestampAverage / (double) statsdMetricsRaw.size()));
        
        assertEquals("gaugeMetric", statsdMetricAggregated.getBucket());
        assertEquals(timestampAverage, statsdMetricAggregated.getMetricTimestampInMilliseconds().longValue());
        assertEquals(timestampAverage, statsdMetricAggregated.getMetricReceivedTimestampInMilliseconds().longValue());
        assertEquals(timestampAverage / 1000, statsdMetricAggregated.getMetricTimestampInSeconds());
        assertEquals(StatsdMetricAggregated.GAUGE_TYPE, statsdMetricAggregated.getMetricTypeKey());
    }

    /**
     * Test of aggregateSet method, of class StatsdMetricAggregator.
     */
    @Test
    public void testAggregateSet() {
        List<StatsdMetricRaw> statsdMetricsRaw = new ArrayList<>();
        
        StatsdMetricRaw statsdMetricRaw1 = StatsdMetricRaw.parseStatsdMetricRaw("setMetric:100|s");
        StatsdMetricRaw statsdMetricRaw2 = StatsdMetricRaw.parseStatsdMetricRaw("setMetric:101|s");
        StatsdMetricRaw statsdMetricRaw3 = StatsdMetricRaw.parseStatsdMetricRaw("setMetric:-215.1|s");
        StatsdMetricRaw statsdMetricRaw4 = StatsdMetricRaw.parseStatsdMetricRaw("setMetric:-215.1|s");
        StatsdMetricRaw statsdMetricRaw5 = StatsdMetricRaw.parseStatsdMetricRaw("setMetric:-215|s");

        statsdMetricsRaw.add(statsdMetricRaw1); statsdMetricsRaw.add(statsdMetricRaw2); 
        statsdMetricsRaw.add(statsdMetricRaw3); statsdMetricsRaw.add(statsdMetricRaw4);
        statsdMetricsRaw.add(statsdMetricRaw5);
        
        statsdMetricRaw1.setHashKey(new Long(1)); statsdMetricRaw2.setHashKey(new Long(2)); 
        statsdMetricRaw3.setHashKey(new Long(3)); statsdMetricRaw4.setHashKey(new Long(4)); 
        statsdMetricRaw5.setHashKey(new Long(5)); 
        
        long timestampAverage = 0;
        for (StatsdMetricRaw statsdMetricRaw : statsdMetricsRaw) {
            timestampAverage += statsdMetricRaw.getMetricReceivedTimestampInMilliseconds();
        }
        timestampAverage = Math.round((double) ((double) timestampAverage / (double) statsdMetricsRaw.size()));
        
        StatsdMetricAggregated statsdMetricAggregated = StatsdMetricAggregator.aggregateSet(statsdMetricsRaw);    
        
        
    }


}