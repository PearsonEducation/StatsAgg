package com.pearson.statsagg.metric_aggregation;

import com.pearson.statsagg.metric_formats.graphite.GraphiteMetric;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
public class GraphiteMetricAggregatorTest {
    
    public GraphiteMetricAggregatorTest() {
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
     * Test of aggregateGraphiteMetrics method, of class GraphiteMetricAggregator.
     */
    @Test
    public void testAggregateGraphiteMetrics() {
        
        List<GraphiteMetric> graphiteMetrics = new ArrayList<>();

        GraphiteMetric graphiteMetric1 = new GraphiteMetric("test.metric.path", new BigDecimal("-5"),  1, 11);
        GraphiteMetric graphiteMetric2 = new GraphiteMetric("test.metric.path", new BigDecimal("11.25"),    2, 21);
        GraphiteMetric graphiteMetric3 = new GraphiteMetric("test.metric.path", new BigDecimal("99.99"), 3, 31);
        GraphiteMetric graphiteMetric4 = new GraphiteMetric("test.metric.path", new BigDecimal("-69.1"),  4, 41);   
        GraphiteMetric graphiteMetric5 = new GraphiteMetric("test.metric.path", new BigDecimal("0"),  5, 51);
        GraphiteMetric graphiteMetric6 = new GraphiteMetric("test.metric.path", new BigDecimal("1.0"),    6, 61);
        GraphiteMetric graphiteMetric7 = new GraphiteMetric("test.metric.path", new BigDecimal("88"), 7, 71);
        
        graphiteMetrics.add(graphiteMetric1); graphiteMetrics.add(graphiteMetric2); graphiteMetrics.add(graphiteMetric3); graphiteMetrics.add(graphiteMetric4); 
        graphiteMetrics.add(graphiteMetric5); graphiteMetrics.add(graphiteMetric6); graphiteMetrics.add(graphiteMetric7); 
        
        List<GraphiteMetric> aggregatedGraphiteMetrics = GraphiteMetricAggregator.aggregate(graphiteMetrics, new BigDecimal(10000), ".");

        int matchCount = 0;
        for (GraphiteMetric aggregatedGraphiteMetric : aggregatedGraphiteMetrics) {
            if (aggregatedGraphiteMetric.getMetricPath().contains("Avg")) {
                assertEquals(aggregatedGraphiteMetric.getMetricValueString(), "18.02");
                matchCount++;
            }
            if (aggregatedGraphiteMetric.getMetricPath().contains("Median")) {
                assertEquals(aggregatedGraphiteMetric.getMetricValueString(), "1");
                matchCount++;
            }
            if (aggregatedGraphiteMetric.getMetricPath().contains("Count")) {
                assertEquals(aggregatedGraphiteMetric.getMetricValueString(), "7");
                matchCount++;
            }
            if (aggregatedGraphiteMetric.getMetricPath().contains("Sum")) {
                assertEquals(aggregatedGraphiteMetric.getMetricValueString(), "126.14");
                matchCount++;
            }
            if (aggregatedGraphiteMetric.getMetricPath().contains("Min")) {
                assertEquals(aggregatedGraphiteMetric.getMetricValueString(), "-69.1");
                matchCount++;
            }
            if (aggregatedGraphiteMetric.getMetricPath().contains("Max")) {
                assertEquals(aggregatedGraphiteMetric.getMetricValueString(), "99.99");
                matchCount++;
            }
            if (aggregatedGraphiteMetric.getMetricPath().contains("Rate-Sec")) {
                assertEquals(aggregatedGraphiteMetric.getMetricValueString(), "0.7");
                matchCount++;
            }
        }
        
        assertEquals(7, matchCount);
        assertEquals(4000, aggregatedGraphiteMetrics.get(0).getMetricTimestamp());
        assertEquals(41, aggregatedGraphiteMetrics.get(0).getMetricReceivedTimestampInMilliseconds());
    }
    
}
