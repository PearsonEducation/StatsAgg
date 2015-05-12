package com.pearson.statsagg.metric_aggregation.statsd;

import com.pearson.statsagg.globals.StatsdHistogramConfiguration;
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
     * Test of getGraphiteFormatString method, of class StatsdMetricAggregator.
     */
    @Test
    public void testGetGraphiteFormatString() {
        long currentTime = System.currentTimeMillis();
        int currentTimeInSeconds = (int) (currentTime / 1000);
        
        StatsdMetricAggregated statsdMetricAggregated = new StatsdMetricAggregated("aggregated.counterMetric", 
                new BigDecimal("1.2222000000"), currentTime, StatsdMetricAggregated.COUNTER_TYPE);
        
        assertEquals(("aggregated.counterMetric 1.2222 " + currentTimeInSeconds), statsdMetricAggregated.getGraphiteFormatString());
    }

    /**
     * Test of getOpenTsdbFormatString method, of class StatsdMetricAggregator.
     */
    @Test
    public void testGetOpenTsdbFormatString() {
        long currentTime = System.currentTimeMillis();
        
        StatsdMetricAggregated statsdMetricAggregated = new StatsdMetricAggregated("aggregated.counterMetric", 
                new BigDecimal("1.2222000000"), currentTime, StatsdMetricAggregated.COUNTER_TYPE);
        
        assertEquals(("aggregated.counterMetric " + currentTime + " 1.2222 " + "Format=StatsD"), statsdMetricAggregated.getOpenTsdbFormatString());
    }
    
    /**
     * Test of aggregateCounter method, of class StatsdMetricAggregator.
     */
    @Test
    public void testAggregateCounter_RegularNamespacing() {
        List<StatsdMetric> statsdMetrics = new ArrayList<>();
        
        StatsdMetric statsdMetric1 = StatsdMetric.parseStatsdMetric("counterMetric:100|c");
        StatsdMetric statsdMetric2 = StatsdMetric.parseStatsdMetric("counterMetric:+800.3|c");
        StatsdMetric statsdMetric3 = StatsdMetric.parseStatsdMetric("counterMetric:-215.1|c");
        StatsdMetric statsdMetric4 = StatsdMetric.parseStatsdMetric("counterMetric:237.5|c|@0.1");
        StatsdMetric statsdMetric5 = StatsdMetric.parseStatsdMetric("counterMetric:66.18|c|@-0.1");
        statsdMetrics.add(statsdMetric1); statsdMetrics.add(statsdMetric2); 
        statsdMetrics.add(statsdMetric3); statsdMetrics.add(statsdMetric4);
        statsdMetrics.add(statsdMetric5);
        
        statsdMetric1.setHashKey(new Long(1)); statsdMetric2.setHashKey(new Long(2)); 
        statsdMetric3.setHashKey(new Long(3)); statsdMetric4.setHashKey(new Long(4)); 
        statsdMetric5.setHashKey(new Long(5)); 
        
        long timestampAverage = 0;
        for (StatsdMetric statsdMetric : statsdMetrics) timestampAverage += statsdMetric.getMetricReceivedTimestampInMilliseconds();
        timestampAverage = Math.round((double) timestampAverage / (double) statsdMetrics.size());
        
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateCounter(statsdMetrics, new BigDecimal(10000), ".", false);
        
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
        List<StatsdMetric> statsdMetrics = new ArrayList<>();
        
        StatsdMetric statsdMetric1 = StatsdMetric.parseStatsdMetric("counterMetric:100|c");
        StatsdMetric statsdMetric2 = StatsdMetric.parseStatsdMetric("counterMetric:+800.3|c");
        StatsdMetric statsdMetric3 = StatsdMetric.parseStatsdMetric("counterMetric:-215.1|c");
        StatsdMetric statsdMetric4 = StatsdMetric.parseStatsdMetric("counterMetric:237.5|c|@0.1");
        StatsdMetric statsdMetric5 = StatsdMetric.parseStatsdMetric("counterMetric:66.18|c|@-0.1");
        statsdMetrics.add(statsdMetric1); statsdMetrics.add(statsdMetric2); 
        statsdMetrics.add(statsdMetric3); statsdMetrics.add(statsdMetric4);
        statsdMetrics.add(statsdMetric5);
        
        statsdMetric1.setHashKey(new Long(1)); statsdMetric2.setHashKey(new Long(2)); 
        statsdMetric3.setHashKey(new Long(3)); statsdMetric4.setHashKey(new Long(4)); 
        statsdMetric5.setHashKey(new Long(5)); 
        
        long timestampAverage = 0;
        for (StatsdMetric statsdMetric : statsdMetrics) timestampAverage += statsdMetric.getMetricReceivedTimestampInMilliseconds();
        timestampAverage = Math.round((double) timestampAverage / (double) statsdMetrics.size());
        
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateCounter(statsdMetrics, new BigDecimal(10000), ".", true);
        
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
        List<StatsdMetric> statsdMetrics = new ArrayList<>();
        
        StatsdMetric statsdMetric1 = StatsdMetric.parseStatsdMetric("gaugeMetric:100|g\r\n");
        Threads.sleepSeconds(0.2);
        StatsdMetric statsdMetric2 = StatsdMetric.parseStatsdMetric("gaugeMetric:+800.3|g");
        StatsdMetric statsdMetric3 = StatsdMetric.parseStatsdMetric("gaugeMetric:-215.1|g");
        Threads.sleepSeconds(0.7);
        StatsdMetric statsdMetric4 = StatsdMetric.parseStatsdMetric("gaugeMetric:237.5|g");
        statsdMetrics.add(statsdMetric1); statsdMetrics.add(statsdMetric2); statsdMetrics.add(statsdMetric3); statsdMetrics.add(statsdMetric4);
        
        statsdMetric1.setHashKey(new Long(1)); statsdMetric2.setHashKey(new Long(2)); statsdMetric3.setHashKey(new Long(3)); statsdMetric4.setHashKey(new Long(4)); 
        StatsdMetricAggregated statsdMetricAggregated = StatsdMetricAggregator.aggregateGauge(statsdMetrics, null, ".", false);
        assertEquals(new BigDecimal("237.5"), statsdMetricAggregated.getMetricValue());
        
        statsdMetric1.setHashKey(new Long(2)); statsdMetric2.setHashKey(new Long(1)); statsdMetric3.setHashKey(new Long(4)); statsdMetric4.setHashKey(new Long(3)); 
        statsdMetricAggregated = StatsdMetricAggregator.aggregateGauge(statsdMetrics, null, ".", false);
        assertEquals(new BigDecimal("22.4"), statsdMetricAggregated.getMetricValue());
        
        statsdMetric1.setHashKey(new Long(4)); statsdMetric2.setHashKey(new Long(1)); statsdMetric3.setHashKey(new Long(2)); statsdMetric4.setHashKey(new Long(3)); 
        statsdMetricAggregated = StatsdMetricAggregator.aggregateGauge(statsdMetrics, null, ".", false);
        assertEquals(new BigDecimal("100"), statsdMetricAggregated.getMetricValue());
        
        statsdMetric1.setHashKey(new Long(2)); statsdMetric2.setHashKey(new Long(3)); statsdMetric3.setHashKey(new Long(4)); statsdMetric4.setHashKey(new Long(1)); 
        statsdMetricAggregated = StatsdMetricAggregator.aggregateGauge(statsdMetrics, null, ".", false);
        assertEquals(new BigDecimal("685.2"), statsdMetricAggregated.getMetricValue());
        
        long timestampAverage = 0;
        for (StatsdMetric statsdMetric : statsdMetrics) {
            timestampAverage += statsdMetric.getMetricReceivedTimestampInMilliseconds();
        }
        timestampAverage = Math.round((double) timestampAverage / (double) statsdMetrics.size());
        
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
        List<StatsdMetric> statsdMetrics = new ArrayList<>();
        
        StatsdMetric statsdMetric1 = StatsdMetric.parseStatsdMetric("setMetric:100|s");
        StatsdMetric statsdMetric2 = StatsdMetric.parseStatsdMetric("setMetric:101|s");
        StatsdMetric statsdMetric3 = StatsdMetric.parseStatsdMetric("setMetric:-215.1|s");
        StatsdMetric statsdMetric4 = StatsdMetric.parseStatsdMetric("setMetric:-215.1|s");
        StatsdMetric statsdMetric5 = StatsdMetric.parseStatsdMetric("setMetric:-215|s");
        StatsdMetric statsdMetric6 = StatsdMetric.parseStatsdMetric("setMetric:-215.100|s");

        statsdMetrics.add(statsdMetric1); statsdMetrics.add(statsdMetric2); 
        statsdMetrics.add(statsdMetric3); statsdMetrics.add(statsdMetric4);
        statsdMetrics.add(statsdMetric5); statsdMetrics.add(statsdMetric6);
        
        statsdMetric1.setHashKey(new Long(1)); statsdMetric2.setHashKey(new Long(2)); 
        statsdMetric3.setHashKey(new Long(3)); statsdMetric4.setHashKey(new Long(4)); 
        statsdMetric5.setHashKey(new Long(5)); statsdMetric5.setHashKey(new Long(6)); 
        
        long timestampAverage = 0;
        for (StatsdMetric statsdMetric : statsdMetrics) timestampAverage += statsdMetric.getMetricReceivedTimestampInMilliseconds();
        timestampAverage = Math.round((double) timestampAverage / (double) statsdMetrics.size());
        
        StatsdMetricAggregated statsdMetricAggregated = StatsdMetricAggregator.aggregateSet(statsdMetrics, ".", false);    
        assertEquals(timestampAverage, statsdMetricAggregated.getMetricTimestampInMilliseconds());
        assertEquals(timestampAverage, statsdMetricAggregated.getMetricReceivedTimestampInMilliseconds());
        assertEquals(timestampAverage / 1000, statsdMetricAggregated.getMetricTimestampInSeconds());
        assertEquals(StatsdMetricAggregated.SET_TYPE, statsdMetricAggregated.getMetricTypeKey());
        
        assertEquals(new BigDecimal("4"), statsdMetricAggregated.getMetricValue());
    }

    @Test
    public void testAggregateTimer_DocumentationTest_70thPct() {
        List<StatsdMetric> statsdMetrics = new ArrayList<>();

        StatsdMetric statsdMetric1 = StatsdMetric.parseStatsdMetric("timerMetric:4|ms");
        StatsdMetric statsdMetric2 = StatsdMetric.parseStatsdMetric("timerMetric:12|ms");
        StatsdMetric statsdMetric3 = StatsdMetric.parseStatsdMetric("timerMetric:2|ms");

        statsdMetrics.add(statsdMetric1); statsdMetrics.add(statsdMetric2); statsdMetrics.add(statsdMetric3);
        statsdMetric1.setHashKey(new Long(1)); statsdMetric2.setHashKey(new Long(2)); statsdMetric3.setHashKey(new Long(3));
        
        long timestampAverage = 0;
        for (StatsdMetric statsdMetric : statsdMetrics) timestampAverage += statsdMetric.getMetricReceivedTimestampInMilliseconds();
        timestampAverage = Math.round((double) timestampAverage / (double) statsdMetrics.size());
        
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("70.00000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetrics, new BigDecimal(10000), ".", statsdNthPercentiles, null, false);  
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
     * Test of aggregateSet method, of class StatsdMetricAggregator.
     */
    @Test
    public void testAggregateTimer() {
        List<StatsdMetric> statsdMetrics = new ArrayList<>();

        StatsdMetric statsdMetric1 = StatsdMetric.parseStatsdMetric("timerMetric:450|ms");
        StatsdMetric statsdMetric2 = StatsdMetric.parseStatsdMetric("timerMetric:120|ms");
        StatsdMetric statsdMetric3 = StatsdMetric.parseStatsdMetric("timerMetric:553|ms");
        StatsdMetric statsdMetric4 = StatsdMetric.parseStatsdMetric("timerMetric:994|ms");
        StatsdMetric statsdMetric5 = StatsdMetric.parseStatsdMetric("timerMetric:334|ms");
        StatsdMetric statsdMetric6 = StatsdMetric.parseStatsdMetric("timerMetric:844|ms");
        StatsdMetric statsdMetric7 = StatsdMetric.parseStatsdMetric("timerMetric:675|ms");
        StatsdMetric statsdMetric8 = StatsdMetric.parseStatsdMetric("timerMetric:496|ms");

        statsdMetrics.add(statsdMetric1); statsdMetrics.add(statsdMetric2); 
        statsdMetrics.add(statsdMetric3); statsdMetrics.add(statsdMetric4);
        statsdMetrics.add(statsdMetric5); statsdMetrics.add(statsdMetric6);
        statsdMetrics.add(statsdMetric7); statsdMetrics.add(statsdMetric8);
        
        statsdMetric1.setHashKey(new Long(1)); statsdMetric2.setHashKey(new Long(2)); 
        statsdMetric3.setHashKey(new Long(3)); statsdMetric4.setHashKey(new Long(4)); 
        statsdMetric5.setHashKey(new Long(5)); statsdMetric6.setHashKey(new Long(5)); 
        statsdMetric5.setHashKey(new Long(7)); statsdMetric8.setHashKey(new Long(5)); 
        
        long timestampAverage = 0;
        for (StatsdMetric statsdMetric : statsdMetrics) timestampAverage += statsdMetric.getMetricReceivedTimestampInMilliseconds();
        timestampAverage = Math.round((double) timestampAverage / (double) statsdMetrics.size());
                        
        testAggregateTimer_40thPct_Sampled();
        testAggregateTimer_NoNth_Histogram_EmptyBins();
        testAggregateTimer_NoNth_Histogram_Inf();
        testAggregateTimer_NoNth_Histogram_NoInf();
        testAggregateTimer_NoNth_Histogram_OnlyInf();
        testAggregateTimer_NoNth(statsdMetrics, timestampAverage);
        testAggregateTimer_Negative100thPct(statsdMetrics);
        testAggregateTimer_Negative35thPct(statsdMetrics);
        testAggregateTimer_Negative3rdPct(statsdMetrics);
        testAggregateTimer_0thPct(statsdMetrics);
        testAggregateTimer_5thPct(statsdMetrics);
        testAggregateTimer_12_5thPct(statsdMetrics);
        testAggregateTimer_35thPct(statsdMetrics);
        testAggregateTimer_40thPct(statsdMetrics);
        testAggregateTimer_50thPct(statsdMetrics);
        testAggregateTimer_90thPct(statsdMetrics);
        testAggregateTimer_100thPct(statsdMetrics);
        testAggregateTimer_150thPct(statsdMetrics);
        testAggregateTimer_Multi(statsdMetrics);
    }
    
    public void testAggregateTimer_40thPct_Sampled() {
        List<StatsdMetric> statsdMetrics = new ArrayList<>();

        StatsdMetric statsdMetric1 = StatsdMetric.parseStatsdMetric("timerMetric:450|ms");
        StatsdMetric statsdMetric2 = StatsdMetric.parseStatsdMetric("timerMetric:120|ms");
        StatsdMetric statsdMetric3 = StatsdMetric.parseStatsdMetric("timerMetric:553|ms|@.2");
        StatsdMetric statsdMetric4 = StatsdMetric.parseStatsdMetric("timerMetric:994|ms");
        StatsdMetric statsdMetric5 = StatsdMetric.parseStatsdMetric("timerMetric:334|ms");
        StatsdMetric statsdMetric6 = StatsdMetric.parseStatsdMetric("timerMetric:844|ms|@0.1");
        StatsdMetric statsdMetric7 = StatsdMetric.parseStatsdMetric("timerMetric:675|ms");
        StatsdMetric statsdMetric8 = StatsdMetric.parseStatsdMetric("timerMetric:496|ms");
        statsdMetrics.add(statsdMetric1); statsdMetrics.add(statsdMetric2); statsdMetrics.add(statsdMetric3); statsdMetrics.add(statsdMetric4);
        statsdMetrics.add(statsdMetric5); statsdMetrics.add(statsdMetric6); statsdMetrics.add(statsdMetric7); statsdMetrics.add(statsdMetric8);
        statsdMetric1.setHashKey(new Long(1)); statsdMetric2.setHashKey(new Long(2)); statsdMetric3.setHashKey(new Long(3)); statsdMetric4.setHashKey(new Long(4)); 
        statsdMetric5.setHashKey(new Long(5)); statsdMetric6.setHashKey(new Long(5)); statsdMetric5.setHashKey(new Long(7)); statsdMetric8.setHashKey(new Long(5)); 
        
        long timestampAverage = 0;
        for (StatsdMetric statsdMetric : statsdMetrics) timestampAverage += statsdMetric.getMetricReceivedTimestampInMilliseconds();
        timestampAverage = Math.round((double) timestampAverage / (double) statsdMetrics.size());
        
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("40.00000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetrics, new BigDecimal(10000), ".", statsdNthPercentiles, null, false);  
        String nthPctShort = statsdNthPercentiles.getNthPercentiles_CleanStrings_StatsdFormatted().get(0);

        int matchCount = 0;

        for (StatsdMetricAggregated statsdMetricAggregated : statsdMetricsAggregated) {
            //System.out.println(statsdMetricAggregated.getGraphiteFormatString());
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.mean ")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "558.25"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.median ")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "524.5"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.upper ")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "994"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.lower ")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "120"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.count ")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "21"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.count_ps ")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "2.1"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum ")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "4466"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_squares ")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "3036278"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.std ")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "260.5603337"); matchCount++;}
            
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.mean_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "301.3333333"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.count_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "3"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "904"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.sum_squares_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "328456"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.lower_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "0"); matchCount++;}
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.upper_" + nthPctShort)) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "450"); matchCount++;}

            assertEquals(timestampAverage, statsdMetricAggregated.getMetricTimestampInMilliseconds());
            assertEquals(timestampAverage, statsdMetricAggregated.getMetricReceivedTimestampInMilliseconds());
            assertEquals(timestampAverage / 1000, statsdMetricAggregated.getMetricTimestampInSeconds());
        }
        
        assertEquals(14, statsdMetricsAggregated.size());
        assertEquals(14, matchCount);
    }
    
    public void testAggregateTimer_NoNth_Histogram_EmptyBins() {
        String unparsedStatsdHistogramConfigurations = "[{metric:'',bins:[]}]" ;
        List<StatsdHistogramConfiguration> statsdHistogramConfigurations = StatsdHistogramConfiguration.getStatsdHistogramConfigurations(unparsedStatsdHistogramConfigurations);
        
        List<StatsdMetric> statsdMetrics = new ArrayList<>();
        StatsdMetric statsdMetric1 = StatsdMetric.parseStatsdMetric("timerMetric:0|ms");
        StatsdMetric statsdMetric2 = StatsdMetric.parseStatsdMetric("timerMetric:0.1|ms");
        StatsdMetric statsdMetric3 = StatsdMetric.parseStatsdMetric("timerMetric:1|ms");
        StatsdMetric statsdMetric4 = StatsdMetric.parseStatsdMetric("timerMetric:1.1|ms");
        StatsdMetric statsdMetric5 = StatsdMetric.parseStatsdMetric("timerMetric:1.2|ms");
        StatsdMetric statsdMetric6 = StatsdMetric.parseStatsdMetric("timerMetric:10|ms");
        StatsdMetric statsdMetric7 = StatsdMetric.parseStatsdMetric("timerMetric:10.1|ms");
        StatsdMetric statsdMetric8 = StatsdMetric.parseStatsdMetric("timerMetric:15|ms");
        statsdMetrics.add(statsdMetric1); statsdMetrics.add(statsdMetric2); statsdMetrics.add(statsdMetric3); statsdMetrics.add(statsdMetric4);
        statsdMetrics.add(statsdMetric5); statsdMetrics.add(statsdMetric6); statsdMetrics.add(statsdMetric7); statsdMetrics.add(statsdMetric8);

        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetrics, new BigDecimal(10000), ".", null, statsdHistogramConfigurations, false);  

        int matchCount = 0;

        for (StatsdMetricAggregated statsdMetricAggregated : statsdMetricsAggregated) {
            //System.out.println(statsdMetricAggregated.getGraphiteFormatString());
            if (statsdMetricAggregated.getGraphiteFormatString().contains("timerMetric.histogram")) matchCount++;
        }
        
        assertEquals(0, matchCount);
    }
    
    public void testAggregateTimer_NoNth_Histogram_Inf() {
        String unparsedStatsdHistogramConfigurations = "[{metric:'',bins:[0,0.1,1,1.21,'inf']}]" ;
        List<StatsdHistogramConfiguration> statsdHistogramConfigurations = StatsdHistogramConfiguration.getStatsdHistogramConfigurations(unparsedStatsdHistogramConfigurations);
        
        List<StatsdMetric> statsdMetrics = new ArrayList<>();
        StatsdMetric statsdMetric1 = StatsdMetric.parseStatsdMetric("timerMetric:0|ms");
        StatsdMetric statsdMetric2 = StatsdMetric.parseStatsdMetric("timerMetric:0.1|ms");
        StatsdMetric statsdMetric3 = StatsdMetric.parseStatsdMetric("timerMetric:1|ms");
        StatsdMetric statsdMetric4 = StatsdMetric.parseStatsdMetric("timerMetric:1.1|ms");
        StatsdMetric statsdMetric5 = StatsdMetric.parseStatsdMetric("timerMetric:1.2|ms");
        StatsdMetric statsdMetric6 = StatsdMetric.parseStatsdMetric("timerMetric:10|ms");
        StatsdMetric statsdMetric7 = StatsdMetric.parseStatsdMetric("timerMetric:10.1|ms");
        StatsdMetric statsdMetric8 = StatsdMetric.parseStatsdMetric("timerMetric:15|ms");
        statsdMetrics.add(statsdMetric1); statsdMetrics.add(statsdMetric2); statsdMetrics.add(statsdMetric3); statsdMetrics.add(statsdMetric4);
        statsdMetrics.add(statsdMetric5); statsdMetrics.add(statsdMetric6); statsdMetrics.add(statsdMetric7); statsdMetrics.add(statsdMetric8);

        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetrics, new BigDecimal(10000), ".", null, statsdHistogramConfigurations, false);  

        int matchCount = 0;

        for (StatsdMetricAggregated statsdMetricAggregated : statsdMetricsAggregated) {
            //System.out.println(statsdMetricAggregated.getGraphiteFormatString());
            if (statsdMetricAggregated.getBucket().endsWith("timerMetric.histogram.bin_0_1")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "1"); matchCount++;}
            if (statsdMetricAggregated.getBucket().endsWith("timerMetric.histogram.bin_1")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "1"); matchCount++;}
            if (statsdMetricAggregated.getBucket().endsWith("timerMetric.histogram.bin_1_21")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "3"); matchCount++;}
            if (statsdMetricAggregated.getBucket().endsWith("timerMetric.histogram.bin_inf")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "3"); matchCount++;}
        }
        
        assertEquals(4, matchCount);
    }
    
    public void testAggregateTimer_NoNth_Histogram_NoInf() {
        String unparsedStatsdHistogramConfigurations = "[{metric:'',bins:[0,0.1,1,1.21,10.2]}]" ;
        List<StatsdHistogramConfiguration> statsdHistogramConfigurations = StatsdHistogramConfiguration.getStatsdHistogramConfigurations(unparsedStatsdHistogramConfigurations);
        
        List<StatsdMetric> statsdMetrics = new ArrayList<>();
        StatsdMetric statsdMetric1 = StatsdMetric.parseStatsdMetric("timerMetric:0|ms");
        StatsdMetric statsdMetric2 = StatsdMetric.parseStatsdMetric("timerMetric:0.1|ms");
        StatsdMetric statsdMetric3 = StatsdMetric.parseStatsdMetric("timerMetric:1|ms");
        StatsdMetric statsdMetric4 = StatsdMetric.parseStatsdMetric("timerMetric:1.1|ms");
        StatsdMetric statsdMetric5 = StatsdMetric.parseStatsdMetric("timerMetric:1.2|ms");
        StatsdMetric statsdMetric6 = StatsdMetric.parseStatsdMetric("timerMetric:10|ms");
        StatsdMetric statsdMetric7 = StatsdMetric.parseStatsdMetric("timerMetric:10.1|ms");
        StatsdMetric statsdMetric8 = StatsdMetric.parseStatsdMetric("timerMetric:15|ms");
        statsdMetrics.add(statsdMetric1); statsdMetrics.add(statsdMetric2); statsdMetrics.add(statsdMetric3); statsdMetrics.add(statsdMetric4);
        statsdMetrics.add(statsdMetric5); statsdMetrics.add(statsdMetric6); statsdMetrics.add(statsdMetric7); statsdMetrics.add(statsdMetric8);

        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetrics, new BigDecimal(10000), ".", null, statsdHistogramConfigurations, false);  

        int matchCount = 0;

        for (StatsdMetricAggregated statsdMetricAggregated : statsdMetricsAggregated) {
            //System.out.println(statsdMetricAggregated.getGraphiteFormatString());
            if (statsdMetricAggregated.getBucket().endsWith("timerMetric.histogram.bin_0_1")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "1"); matchCount++;}
            if (statsdMetricAggregated.getBucket().endsWith("timerMetric.histogram.bin_1")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "1"); matchCount++;}
            if (statsdMetricAggregated.getBucket().endsWith("timerMetric.histogram.bin_1_21")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "3"); matchCount++;}
            if (statsdMetricAggregated.getBucket().endsWith("timerMetric.histogram.bin_10_2")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "2"); matchCount++;}
        }
        
        assertEquals(4, matchCount);
    }
    
    public void testAggregateTimer_NoNth_Histogram_OnlyInf() {
        String unparsedStatsdHistogramConfigurations = "[{metric:'',bins:['inf']}]" ;
        List<StatsdHistogramConfiguration> statsdHistogramConfigurations = StatsdHistogramConfiguration.getStatsdHistogramConfigurations(unparsedStatsdHistogramConfigurations);
        
        List<StatsdMetric> statsdMetrics = new ArrayList<>();
        StatsdMetric statsdMetric1 = StatsdMetric.parseStatsdMetric("timerMetric:0|ms");
        StatsdMetric statsdMetric2 = StatsdMetric.parseStatsdMetric("timerMetric:0.1|ms");
        StatsdMetric statsdMetric3 = StatsdMetric.parseStatsdMetric("timerMetric:1|ms");
        StatsdMetric statsdMetric4 = StatsdMetric.parseStatsdMetric("timerMetric:1.1|ms");
        StatsdMetric statsdMetric5 = StatsdMetric.parseStatsdMetric("timerMetric:1.2|ms");
        StatsdMetric statsdMetric6 = StatsdMetric.parseStatsdMetric("timerMetric:10|ms");
        StatsdMetric statsdMetric7 = StatsdMetric.parseStatsdMetric("timerMetric:10.1|ms");
        StatsdMetric statsdMetric8 = StatsdMetric.parseStatsdMetric("timerMetric:15|ms");
        statsdMetrics.add(statsdMetric1); statsdMetrics.add(statsdMetric2); statsdMetrics.add(statsdMetric3); statsdMetrics.add(statsdMetric4);
        statsdMetrics.add(statsdMetric5); statsdMetrics.add(statsdMetric6); statsdMetrics.add(statsdMetric7); statsdMetrics.add(statsdMetric8);

        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetrics, new BigDecimal(10000), ".", null, statsdHistogramConfigurations, false);  

        int matchCount = 0;

        for (StatsdMetricAggregated statsdMetricAggregated : statsdMetricsAggregated) {
            //System.out.println(statsdMetricAggregated.getGraphiteFormatString());
            if (statsdMetricAggregated.getBucket().endsWith("timerMetric.histogram.bin_inf")) {assertEquals(statsdMetricAggregated.getMetricValue().toString(), "8"); matchCount++;}
        }
        
        assertEquals(1, matchCount);
    }
    
    public void testAggregateTimer_NoNth(List<StatsdMetric> statsdMetrics, long timestampAverage) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles(null);
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetrics, new BigDecimal(10000), ".", statsdNthPercentiles, null, false);  
        
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

    public void testAggregateTimer_Negative100thPct(List<StatsdMetric> statsdMetrics) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("-100.000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetrics, new BigDecimal(10000), ".", statsdNthPercentiles, null, false);  
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
    
    public void testAggregateTimer_Negative35thPct(List<StatsdMetric> statsdMetrics) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("-35.000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetrics, new BigDecimal(10000), ".", statsdNthPercentiles, null, false);  
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
    
    public void testAggregateTimer_Negative3rdPct(List<StatsdMetric> statsdMetrics) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("-3.000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetrics, new BigDecimal(10000), ".", statsdNthPercentiles, null, false);  
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
    
    public void testAggregateTimer_0thPct(List<StatsdMetric> statsdMetrics) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("0.00000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetrics, new BigDecimal(10000), ".", statsdNthPercentiles, null, false);  
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
    
    public void testAggregateTimer_5thPct(List<StatsdMetric> statsdMetrics) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("5.00000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetrics, new BigDecimal(10000), ".", statsdNthPercentiles, null, false);  
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
    
    public void testAggregateTimer_12_5thPct(List<StatsdMetric> statsdMetrics) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("12.50000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetrics, new BigDecimal(10000), ".", statsdNthPercentiles, null, false);  
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
    
    public void testAggregateTimer_35thPct(List<StatsdMetric> statsdMetrics) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("35.00000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetrics, new BigDecimal(10000), ".", statsdNthPercentiles, null, false);  
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
    
    public void testAggregateTimer_40thPct(List<StatsdMetric> statsdMetrics) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("40.00000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetrics, new BigDecimal(10000), ".", statsdNthPercentiles, null, false);  
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
    
    public void testAggregateTimer_50thPct(List<StatsdMetric> statsdMetrics) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("50.00000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetrics, new BigDecimal(10000), ".", statsdNthPercentiles, null, false);  
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
    
    public void testAggregateTimer_90thPct(List<StatsdMetric> statsdMetrics) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("90.00000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetrics, new BigDecimal(10000), ".", statsdNthPercentiles, null, false);  
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
    
    public void testAggregateTimer_100thPct(List<StatsdMetric> statsdMetrics) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("100.00000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetrics, new BigDecimal(10000), ".", statsdNthPercentiles, null, false);  
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
    
    public void testAggregateTimer_150thPct(List<StatsdMetric> statsdMetrics) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("150.00000000");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetrics, new BigDecimal(10000), ".", statsdNthPercentiles, null, false);  
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

    public void testAggregateTimer_Multi(List<StatsdMetric> statsdMetrics) {
        StatsdNthPercentiles statsdNthPercentiles = new StatsdNthPercentiles("12.5,5,40");
        List<StatsdMetricAggregated> statsdMetricsAggregated = StatsdMetricAggregator.aggregateTimer(statsdMetrics, new BigDecimal(10000), ".", statsdNthPercentiles, null, false);  
                
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