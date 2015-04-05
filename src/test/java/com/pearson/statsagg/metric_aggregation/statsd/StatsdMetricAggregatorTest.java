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
    public void testAggregateCounter_RegularNamespacing() {
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
        for (StatsdMetricRaw statsdMetricRaw : statsdMetricsRaw) timestampAverage += statsdMetricRaw.getMetricReceivedTimestampInMilliseconds();
        timestampAverage = Math.round((double) timestampAverage / (double) statsdMetricsRaw.size());
        
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateCounter(statsdMetricsRaw, new BigDecimal(10000), ".", false);
        
        int matchCount = 0;
        for (StatsdMetricAggregated statsdMetricAggregated : statsdMetricsAggregated) {
            if (statsdMetricAggregated.getBucket().equals("counterMetric.count")) {
                assertEquals("counterMetric.count", statsdMetricAggregated.getBucket());
                assertEquals(new BigDecimal("3126.38"), statsdMetricAggregated.getMetricValue());
                assertEquals(timestampAverage, statsdMetricAggregated.getMetricTimestampInMilliseconds());
                assertEquals(timestampAverage, statsdMetricAggregated.getMetricReceivedTimestampInMilliseconds());
                assertEquals(timestampAverage / 1000, statsdMetricAggregated.getMetricTimestampInSeconds());
                assertEquals(StatsdMetricAggregated.COUNTER_TYPE, statsdMetricAggregated.getMetricTypeKey());
                matchCount++;
            }
            else if (statsdMetricAggregated.getBucket().equals("counterMetric.rate")) {
                assertEquals("counterMetric.rate", statsdMetricAggregated.getBucket());
                assertEquals(new BigDecimal("312.638"), statsdMetricAggregated.getMetricValue());
                assertEquals(timestampAverage, statsdMetricAggregated.getMetricTimestampInMilliseconds());
                assertEquals(timestampAverage, statsdMetricAggregated.getMetricReceivedTimestampInMilliseconds());
                assertEquals(timestampAverage / 1000, statsdMetricAggregated.getMetricTimestampInSeconds());
                assertEquals(StatsdMetricAggregated.COUNTER_TYPE, statsdMetricAggregated.getMetricTypeKey());
                matchCount++;
            }
            
            assertEquals(timestampAverage, statsdMetricAggregated.getMetricTimestampInMilliseconds());
            assertEquals(timestampAverage, statsdMetricAggregated.getMetricReceivedTimestampInMilliseconds());
            assertEquals(timestampAverage / 1000, statsdMetricAggregated.getMetricTimestampInSeconds());
        }
        
        assertEquals(matchCount, statsdMetricsAggregated.size());
    }

    /**
     * Test of aggregateCounter method, of class StatsdMetricAggregator.
     */
    @Test
    public void testAggregateCounter_LegacyNamespacing() {
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
        for (StatsdMetricRaw statsdMetricRaw : statsdMetricsRaw) timestampAverage += statsdMetricRaw.getMetricReceivedTimestampInMilliseconds();
        timestampAverage = Math.round((double) timestampAverage / (double) statsdMetricsRaw.size());
        
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateCounter(statsdMetricsRaw, new BigDecimal(10000), ".", true);
        
        int matchCount = 0;
        for (StatsdMetricAggregated statsdMetricAggregated : statsdMetricsAggregated) {
            if (statsdMetricAggregated.getBucket().equals("stats_counts.counterMetric")) {
                assertEquals("stats_counts.counterMetric", statsdMetricAggregated.getBucket());
                assertEquals(new BigDecimal("3126.38"), statsdMetricAggregated.getMetricValue());
                assertEquals(timestampAverage, statsdMetricAggregated.getMetricTimestampInMilliseconds());
                assertEquals(timestampAverage, statsdMetricAggregated.getMetricReceivedTimestampInMilliseconds());
                assertEquals(timestampAverage / 1000, statsdMetricAggregated.getMetricTimestampInSeconds());
                assertEquals(StatsdMetricAggregated.COUNTER_TYPE, statsdMetricAggregated.getMetricTypeKey());
                matchCount++;
            }
            else if (statsdMetricAggregated.getBucket().equals("stats.counterMetric")) {
                assertEquals("stats.counterMetric", statsdMetricAggregated.getBucket());
                assertEquals(new BigDecimal("312.638"), statsdMetricAggregated.getMetricValue());
                assertEquals(timestampAverage, statsdMetricAggregated.getMetricTimestampInMilliseconds());
                assertEquals(timestampAverage, statsdMetricAggregated.getMetricReceivedTimestampInMilliseconds());
                assertEquals(timestampAverage / 1000, statsdMetricAggregated.getMetricTimestampInSeconds());
                assertEquals(StatsdMetricAggregated.COUNTER_TYPE, statsdMetricAggregated.getMetricTypeKey());
                matchCount++;
            }
            
            assertEquals(timestampAverage, statsdMetricAggregated.getMetricTimestampInMilliseconds());
            assertEquals(timestampAverage, statsdMetricAggregated.getMetricReceivedTimestampInMilliseconds());
            assertEquals(timestampAverage / 1000, statsdMetricAggregated.getMetricTimestampInSeconds());
        }
        
        assertEquals(matchCount, statsdMetricsAggregated.size());
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
        StatsdMetricAggregated statsdMetricAggregated = StatsdMetricAggregator.aggregateGauge(statsdMetricsRaw, null, ".", false);
        assertEquals(new BigDecimal("237.5"), statsdMetricAggregated.getMetricValue());
        
        statsdMetricRaw1.setHashKey(new Long(2)); statsdMetricRaw2.setHashKey(new Long(1)); statsdMetricRaw3.setHashKey(new Long(4)); statsdMetricRaw4.setHashKey(new Long(3)); 
        statsdMetricAggregated = StatsdMetricAggregator.aggregateGauge(statsdMetricsRaw, null, ".", false);
        assertEquals(new BigDecimal("22.4"), statsdMetricAggregated.getMetricValue());
        
        statsdMetricRaw1.setHashKey(new Long(4)); statsdMetricRaw2.setHashKey(new Long(1)); statsdMetricRaw3.setHashKey(new Long(2)); statsdMetricRaw4.setHashKey(new Long(3)); 
        statsdMetricAggregated = StatsdMetricAggregator.aggregateGauge(statsdMetricsRaw, null, ".", false);
        assertEquals(new BigDecimal("100"), statsdMetricAggregated.getMetricValue());
        
        statsdMetricRaw1.setHashKey(new Long(2)); statsdMetricRaw2.setHashKey(new Long(3)); statsdMetricRaw3.setHashKey(new Long(4)); statsdMetricRaw4.setHashKey(new Long(1)); 
        statsdMetricAggregated = StatsdMetricAggregator.aggregateGauge(statsdMetricsRaw, null, ".", false);
        assertEquals(new BigDecimal("685.2"), statsdMetricAggregated.getMetricValue());
        
        long timestampAverage = 0;
        for (StatsdMetricRaw statsdMetricRaw : statsdMetricsRaw) {
            timestampAverage += statsdMetricRaw.getMetricReceivedTimestampInMilliseconds();
        }
        timestampAverage = Math.round((double) timestampAverage / (double) statsdMetricsRaw.size());
        
        assertEquals("gaugeMetric", statsdMetricAggregated.getBucket());
        assertEquals(timestampAverage, statsdMetricAggregated.getMetricTimestampInMilliseconds());
        assertEquals(timestampAverage, statsdMetricAggregated.getMetricReceivedTimestampInMilliseconds());
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
        StatsdMetricRaw statsdMetricRaw6 = StatsdMetricRaw.parseStatsdMetricRaw("setMetric:-215.100|s");

        statsdMetricsRaw.add(statsdMetricRaw1); statsdMetricsRaw.add(statsdMetricRaw2); 
        statsdMetricsRaw.add(statsdMetricRaw3); statsdMetricsRaw.add(statsdMetricRaw4);
        statsdMetricsRaw.add(statsdMetricRaw5); statsdMetricsRaw.add(statsdMetricRaw6);
        
        statsdMetricRaw1.setHashKey(new Long(1)); statsdMetricRaw2.setHashKey(new Long(2)); 
        statsdMetricRaw3.setHashKey(new Long(3)); statsdMetricRaw4.setHashKey(new Long(4)); 
        statsdMetricRaw5.setHashKey(new Long(5)); statsdMetricRaw5.setHashKey(new Long(6)); 
        
        long timestampAverage = 0;
        for (StatsdMetricRaw statsdMetricRaw : statsdMetricsRaw) timestampAverage += statsdMetricRaw.getMetricReceivedTimestampInMilliseconds();
        timestampAverage = Math.round((double) timestampAverage / (double) statsdMetricsRaw.size());
        
        StatsdMetricAggregated statsdMetricAggregated = StatsdMetricAggregator.aggregateSet(statsdMetricsRaw, ".", false);    
        assertEquals(timestampAverage, statsdMetricAggregated.getMetricTimestampInMilliseconds());
        assertEquals(timestampAverage, statsdMetricAggregated.getMetricReceivedTimestampInMilliseconds());
        assertEquals(timestampAverage / 1000, statsdMetricAggregated.getMetricTimestampInSeconds());
        assertEquals(StatsdMetricAggregated.SET_TYPE, statsdMetricAggregated.getMetricTypeKey());
        
        assertEquals(new BigDecimal("4"), statsdMetricAggregated.getMetricValue());
    }

    @Test
    public void testAggregateTimer_DocumentationTest_70thPct() {
        List<StatsdMetricRaw> statsdMetricsRaw = new ArrayList<>();

        StatsdMetricRaw statsdMetricRaw1 = StatsdMetricRaw.parseStatsdMetricRaw("timerMetric:4|ms");
        StatsdMetricRaw statsdMetricRaw2 = StatsdMetricRaw.parseStatsdMetricRaw("timerMetric:12|ms");
        StatsdMetricRaw statsdMetricRaw3 = StatsdMetricRaw.parseStatsdMetricRaw("timerMetric:2|ms");

        statsdMetricsRaw.add(statsdMetricRaw1); statsdMetricsRaw.add(statsdMetricRaw2); statsdMetricsRaw.add(statsdMetricRaw3);
        statsdMetricRaw1.setHashKey(new Long(1)); statsdMetricRaw2.setHashKey(new Long(2)); statsdMetricRaw3.setHashKey(new Long(3));
        
        long timestampAverage = 0;
        for (StatsdMetricRaw statsdMetricRaw : statsdMetricsRaw) timestampAverage += statsdMetricRaw.getMetricReceivedTimestampInMilliseconds();
        timestampAverage = Math.round((double) timestampAverage / (double) statsdMetricsRaw.size());
        
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("70.00000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetricsRaw, new BigDecimal(10000), ".", statsdNthPercentiles, false);  
        String nthPctShort = statsdNthPercentiles.getNthPercentiles_CleanStrings_StatsdFormatted().get(0);
        
        int matchCount = 0;

        for (StatsdMetricAggregated statsdMetricAggregated : statsdMetricsAggregated) {
            //System.out.println(statsdMetricAggregated.getGraphiteFormatString());
            if (statsdMetricAggregated.getGraphiteFormatString().contains(".mean_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "3"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains(".count_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "2"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains(".sum_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "6"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains(".sum_squares_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "20"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains(".upper_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "4"); matchCount++;}
            assertEquals(StatsdMetricAggregated.TIMER_TYPE, statsdMetricAggregated.getMetricTypeKey());
        }
        
        assertEquals(14, statsdMetricsAggregated.size());
        assertEquals(5, matchCount);
    }
    
    /**
     * Test of aggregateSet method, of class StatsdMetricAggregator. Tests everything except nth percentage
     */
    @Test
    public void testAggregateTimer() {
        List<StatsdMetricRaw> statsdMetricsRaw = new ArrayList<>();

        StatsdMetricRaw statsdMetricRaw1 = StatsdMetricRaw.parseStatsdMetricRaw("timerMetric:450|ms");
        StatsdMetricRaw statsdMetricRaw2 = StatsdMetricRaw.parseStatsdMetricRaw("timerMetric:120|ms");
        StatsdMetricRaw statsdMetricRaw3 = StatsdMetricRaw.parseStatsdMetricRaw("timerMetric:553|ms");
        StatsdMetricRaw statsdMetricRaw4 = StatsdMetricRaw.parseStatsdMetricRaw("timerMetric:994|ms");
        StatsdMetricRaw statsdMetricRaw5 = StatsdMetricRaw.parseStatsdMetricRaw("timerMetric:334|ms");
        StatsdMetricRaw statsdMetricRaw6 = StatsdMetricRaw.parseStatsdMetricRaw("timerMetric:844|ms");
        StatsdMetricRaw statsdMetricRaw7 = StatsdMetricRaw.parseStatsdMetricRaw("timerMetric:675|ms");
        StatsdMetricRaw statsdMetricRaw8 = StatsdMetricRaw.parseStatsdMetricRaw("timerMetric:496|ms");

        statsdMetricsRaw.add(statsdMetricRaw1); statsdMetricsRaw.add(statsdMetricRaw2); 
        statsdMetricsRaw.add(statsdMetricRaw3); statsdMetricsRaw.add(statsdMetricRaw4);
        statsdMetricsRaw.add(statsdMetricRaw5); statsdMetricsRaw.add(statsdMetricRaw6);
        statsdMetricsRaw.add(statsdMetricRaw7); statsdMetricsRaw.add(statsdMetricRaw8);
        
        statsdMetricRaw1.setHashKey(new Long(1)); statsdMetricRaw2.setHashKey(new Long(2)); 
        statsdMetricRaw3.setHashKey(new Long(3)); statsdMetricRaw4.setHashKey(new Long(4)); 
        statsdMetricRaw5.setHashKey(new Long(5)); statsdMetricRaw6.setHashKey(new Long(5)); 
        statsdMetricRaw5.setHashKey(new Long(7)); statsdMetricRaw8.setHashKey(new Long(5)); 
        
        long timestampAverage = 0;
        for (StatsdMetricRaw statsdMetricRaw : statsdMetricsRaw) timestampAverage += statsdMetricRaw.getMetricReceivedTimestampInMilliseconds();
        timestampAverage = Math.round((double) timestampAverage / (double) statsdMetricsRaw.size());
                        
        testAggregateTimer_NoNth(statsdMetricsRaw, timestampAverage);
        testAggregateTimer_Negative100thPct(statsdMetricsRaw);
        testAggregateTimer_Negative35thPct(statsdMetricsRaw);
        testAggregateTimer_Negative3rdPct(statsdMetricsRaw);
        testAggregateTimer_0thPct(statsdMetricsRaw);
        testAggregateTimer_5thPct(statsdMetricsRaw);
        testAggregateTimer_12_5thPct(statsdMetricsRaw);
        testAggregateTimer_35thPct(statsdMetricsRaw);
        testAggregateTimer_40thPct(statsdMetricsRaw);
        testAggregateTimer_50thPct(statsdMetricsRaw);
        testAggregateTimer_90thPct(statsdMetricsRaw);
        testAggregateTimer_100thPct(statsdMetricsRaw);
        testAggregateTimer_150thPct(statsdMetricsRaw);
        testAggregateTimer_Multi(statsdMetricsRaw);
    }
    
    public void testAggregateTimer_NoNth(List<StatsdMetricRaw> statsdMetricsRaw, long timestampAverage) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles(null);
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetricsRaw, new BigDecimal(10000), ".", statsdNthPercentiles, false);  
        
        int matchCount = 0;

        for (StatsdMetricAggregated statsdMetricAggregated : statsdMetricsAggregated) {
            //System.out.println(statsdMetricAggregated.getGraphiteFormatString());
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.mean ")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "558.25"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.median ")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "524.5"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.upper ")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "994"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.lower ")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "120"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.count ")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "8"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.count_ps ")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0.8"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum ")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "4466"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_squares ")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "3036278"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.std ")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "260.5603337"); matchCount++;}
            
            assertEquals(timestampAverage, statsdMetricAggregated.getMetricTimestampInMilliseconds());
            assertEquals(timestampAverage, statsdMetricAggregated.getMetricReceivedTimestampInMilliseconds());
            assertEquals(timestampAverage / 1000, statsdMetricAggregated.getMetricTimestampInSeconds());
        }
        
        assertEquals(9, statsdMetricsAggregated.size());
        assertEquals(9, matchCount);
    }
    
    public void testAggregateTimer_Negative100thPct(List<StatsdMetricRaw> statsdMetricsRaw) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("-100.000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetricsRaw, new BigDecimal(10000), ".", statsdNthPercentiles, false);  
        String nthPctShort = statsdNthPercentiles.getNthPercentiles_CleanStrings_StatsdFormatted().get(0);
        
        int matchCount = 0;
        
        for (StatsdMetricAggregated statsdMetricAggregated : statsdMetricsAggregated) {
            //System.out.println(statsdMetricAggregated.getGraphiteFormatString());
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.mean_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.count_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "8"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_squares_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.lower_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "120"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.upper_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
        }
        
        assertEquals(11, statsdMetricsAggregated.size());
        assertEquals(2, matchCount);
    }
    
    public void testAggregateTimer_Negative35thPct(List<StatsdMetricRaw> statsdMetricsRaw) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("-35.000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetricsRaw, new BigDecimal(10000), ".", statsdNthPercentiles, false);  
        String nthPctShort = statsdNthPercentiles.getNthPercentiles_CleanStrings_StatsdFormatted().get(0);
        
        int matchCount = 0;
        
        for (StatsdMetricAggregated statsdMetricAggregated : statsdMetricsAggregated) {
            //System.out.println(statsdMetricAggregated.getGraphiteFormatString());
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.mean_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "837.6666667"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.count_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "3"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "2513"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_squares_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "2155997"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.lower_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "675"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.upper_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
        }
        
        assertEquals(14, statsdMetricsAggregated.size());
        assertEquals(5, matchCount);
    }
    
    public void testAggregateTimer_Negative3rdPct(List<StatsdMetricRaw> statsdMetricsRaw) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("-3.000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetricsRaw, new BigDecimal(10000), ".", statsdNthPercentiles, false);  
        String nthPctShort = statsdNthPercentiles.getNthPercentiles_CleanStrings_StatsdFormatted().get(0);
        
        int matchCount = 0;
        
        for (StatsdMetricAggregated statsdMetricAggregated : statsdMetricsAggregated) {
            //System.out.println(statsdMetricAggregated.getGraphiteFormatString());
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.mean_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.count_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_squares_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.lower_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.upper_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
        }
        
        assertEquals(9, statsdMetricsAggregated.size());
        assertEquals(0, matchCount);
    }
    
    public void testAggregateTimer_0thPct(List<StatsdMetricRaw> statsdMetricsRaw) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("0.00000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetricsRaw, new BigDecimal(10000), ".", statsdNthPercentiles, false);  
        String nthPctShort = statsdNthPercentiles.getNthPercentiles_CleanStrings_StatsdFormatted().get(0); 
        
        int matchCount = 0;

        for (StatsdMetricAggregated statsdMetricAggregated : statsdMetricsAggregated) {
            //System.out.println(statsdMetricAggregated.getGraphiteFormatString());
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.mean_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "558.25"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.count_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "8"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "4466"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_squares_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "3036278"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.lower_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.upper_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "994"); matchCount++;}
        }
        
        assertEquals(9, statsdMetricsAggregated.size());
        assertEquals(0, matchCount);
    }
    
    public void testAggregateTimer_5thPct(List<StatsdMetricRaw> statsdMetricsRaw) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("5.00000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetricsRaw, new BigDecimal(10000), ".", statsdNthPercentiles, false);  
        String nthPctShort = statsdNthPercentiles.getNthPercentiles_CleanStrings_StatsdFormatted().get(0);
        
        int matchCount = 0;
        
        for (StatsdMetricAggregated statsdMetricAggregated : statsdMetricsAggregated) {
            //System.out.println(statsdMetricAggregated.getGraphiteFormatString());
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.mean_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.count_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_squares_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.lower_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.upper_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
        }
        
        assertEquals(9, statsdMetricsAggregated.size());
        assertEquals(0, matchCount);
    }
    
    public void testAggregateTimer_12_5thPct(List<StatsdMetricRaw> statsdMetricsRaw) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("12.50000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetricsRaw, new BigDecimal(10000), ".", statsdNthPercentiles, false);  
        String nthPctShort = statsdNthPercentiles.getNthPercentiles_CleanStrings_StatsdFormatted().get(0);
                
        int matchCount = 0;
        
        for (StatsdMetricAggregated statsdMetricAggregated : statsdMetricsAggregated) {
            //System.out.println(statsdMetricAggregated.getGraphiteFormatString());
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.mean_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "120"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.count_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "1"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "120"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_squares_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "14400"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.lower_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.upper_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "120"); matchCount++;}
        }
        
        assertEquals(14, statsdMetricsAggregated.size());
        assertEquals(5, matchCount);
    }
    
    public void testAggregateTimer_35thPct(List<StatsdMetricRaw> statsdMetricsRaw) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("35.00000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetricsRaw, new BigDecimal(10000), ".", statsdNthPercentiles, false);  
        String nthPctShort = statsdNthPercentiles.getNthPercentiles_CleanStrings_StatsdFormatted().get(0);
        
        int matchCount = 0;

        for (StatsdMetricAggregated statsdMetricAggregated : statsdMetricsAggregated) {
            //System.out.println(statsdMetricAggregated.getGraphiteFormatString());
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.mean_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "301.3333333"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.count_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "3"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "904"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_squares_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "328456"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.lower_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.upper_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "450"); matchCount++;}
        }
        
        assertEquals(14, statsdMetricsAggregated.size());
        assertEquals(5, matchCount);
    }
    
    public void testAggregateTimer_40thPct(List<StatsdMetricRaw> statsdMetricsRaw) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("40.00000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetricsRaw, new BigDecimal(10000), ".", statsdNthPercentiles, false);  
        String nthPctShort = statsdNthPercentiles.getNthPercentiles_CleanStrings_StatsdFormatted().get(0);
        
        int matchCount = 0;

        for (StatsdMetricAggregated statsdMetricAggregated : statsdMetricsAggregated) {
            //System.out.println(statsdMetricAggregated.getGraphiteFormatString());
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.mean_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "301.3333333"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.count_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "3"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "904"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_squares_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "328456"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.lower_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.upper_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "450"); matchCount++;}
        }
        
        assertEquals(14, statsdMetricsAggregated.size());
        assertEquals(5, matchCount);
    }
    
    public void testAggregateTimer_50thPct(List<StatsdMetricRaw> statsdMetricsRaw) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("50.00000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetricsRaw, new BigDecimal(10000), ".", statsdNthPercentiles, false);  
        String nthPctShort = statsdNthPercentiles.getNthPercentiles_CleanStrings_StatsdFormatted().get(0);
        
        int matchCount = 0;

        for (StatsdMetricAggregated statsdMetricAggregated : statsdMetricsAggregated) {
            //System.out.println(statsdMetricAggregated.getGraphiteFormatString());
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.mean_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "350"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.count_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "4"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "1400"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_squares_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "574472"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.lower_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.upper_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "496"); matchCount++;}
        }
        
        assertEquals(14, statsdMetricsAggregated.size());
        assertEquals(5, matchCount);
    }
    
    public void testAggregateTimer_90thPct(List<StatsdMetricRaw> statsdMetricsRaw) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("90.00000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetricsRaw, new BigDecimal(10000), ".", statsdNthPercentiles, false);  
        String nthPctShort = statsdNthPercentiles.getNthPercentiles_CleanStrings_StatsdFormatted().get(0);
        
        int matchCount = 0;

        for (StatsdMetricAggregated statsdMetricAggregated : statsdMetricsAggregated) {
            //System.out.println(statsdMetricAggregated.getGraphiteFormatString());
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.mean_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "496"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.count_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "7"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "3472"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_squares_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "2048242"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.lower_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.upper_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "844"); matchCount++;}
        }
        
        assertEquals(14, statsdMetricsAggregated.size());
        assertEquals(5, matchCount);
    }
    
    public void testAggregateTimer_100thPct(List<StatsdMetricRaw> statsdMetricsRaw) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("100.00000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetricsRaw, new BigDecimal(10000), ".", statsdNthPercentiles, false);  
        String nthPctShort = statsdNthPercentiles.getNthPercentiles_CleanStrings_StatsdFormatted().get(0);
        
        int matchCount = 0;

        for (StatsdMetricAggregated statsdMetricAggregated : statsdMetricsAggregated) {
            //System.out.println(statsdMetricAggregated.getGraphiteFormatString());
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.mean_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "558.25"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.count_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "8"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "4466"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_squares_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "3036278"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.lower_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.upper_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "994"); matchCount++;}
        }
        
        assertEquals(14, statsdMetricsAggregated.size());
        assertEquals(5, matchCount);
    }
    
    public void testAggregateTimer_150thPct(List<StatsdMetricRaw> statsdMetricsRaw) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("150.00000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetricsRaw, new BigDecimal(10000), ".", statsdNthPercentiles, false);  
        String nthPctShort = statsdNthPercentiles.getNthPercentiles_CleanStrings_StatsdFormatted().get(0);
        
        int matchCount = 0;

        for (StatsdMetricAggregated statsdMetricAggregated : statsdMetricsAggregated) {
            //System.out.println(statsdMetricAggregated.getGraphiteFormatString());
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.mean_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "558.25"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.count_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "8"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "4466"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_squares_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "3036278"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.lower_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.upper_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "994"); matchCount++;}
        }

        assertEquals(14, statsdMetricsAggregated.size());
        assertEquals(5, matchCount);
    }

    public void testAggregateTimer_Multi(List<StatsdMetricRaw> statsdMetricsRaw) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("12.5,5,40");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetricsRaw, new BigDecimal(10000), ".", statsdNthPercentiles, false);  
                
        int matchCount = 0;
        
        for (StatsdMetricAggregated statsdMetricAggregated : statsdMetricsAggregated) {
            //System.out.println(statsdMetricAggregated.getGraphiteFormatString());
            
            if (statsdMetricAggregated.getGraphiteFormatString().contains(".mean_" + "12_5")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "120"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains(".count_" + "12_5")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "1"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains(".sum_" + "12_5")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "120"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains(".sum_squares_" + "12_5")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "14400"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains(".lower_" + "12_5")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains(".upper_" + "12_5")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "120"); matchCount++;}
            
            if (statsdMetricAggregated.getGraphiteFormatString().contains(".mean_" + "5")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains(".count_" + "5")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains(".sum_" + "5")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains(".sum_squares_" + "5")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains(".lower_" + "5")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains(".upper_" + "5")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            
            if (statsdMetricAggregated.getGraphiteFormatString().contains(".mean_" + "40")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "301.3333333"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains(".count_" + "40")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "3"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains(".sum_" + "40")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "904"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains(".sum_squares_" + "40")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "328456"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains(".lower_" + "40")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains(".upper_" + "40")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "450"); matchCount++;}

        }
        
        assertEquals(19, statsdMetricsAggregated.size());
        assertEquals(10, matchCount);
    }
    
}