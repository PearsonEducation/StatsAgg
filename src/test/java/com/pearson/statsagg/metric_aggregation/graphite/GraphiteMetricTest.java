package com.pearson.statsagg.metric_aggregation.graphite;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
public class GraphiteMetricTest {
    
    private GraphiteMetric graphiteMetric1_;
    private GraphiteMetric graphiteMetric2_;
    private GraphiteMetric graphiteMetric3_;
    
    public GraphiteMetricTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        graphiteMetric1_ = new GraphiteMetric("test.metric.path", new BigDecimal("12345.1230"), 1382848111, 1382848222222L);
        graphiteMetric2_ = new GraphiteMetric("test.metric.path?><!@#$!", new BigDecimal("12345.000"), 123, 1234L);
        graphiteMetric3_ = new GraphiteMetric("test.metric.path", new BigDecimal("12345.123"), 1382848111, 1382848222222L); // same as Raw1
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of equals method, of class GraphiteMetric.
     */
    @Test
    public void testEquals() {
        assertEquals(graphiteMetric1_, graphiteMetric3_);
        assertNotEquals(graphiteMetric1_, graphiteMetric2_);
        
        GraphiteMetric graphiteMetric = new GraphiteMetric("test.metric.path.", new BigDecimal("12345.123"), 1382848111, 1382848222222L);
        assertNotEquals(graphiteMetric1_, graphiteMetric);
        
        graphiteMetric = new GraphiteMetric("test.metric.path", new BigDecimal("12345.1234"), 1382848111, 1382848222222L);
        assertNotEquals(graphiteMetric1_, graphiteMetric);
        
        graphiteMetric = new GraphiteMetric("test.metric.path", new BigDecimal("12345.123"), 1382848112, 1382848222222L);
        assertNotEquals(graphiteMetric1_, graphiteMetric);
        
        graphiteMetric = new GraphiteMetric("test.metric.path", new BigDecimal("12345.123"), 1382848111, 1382848222221L);
        assertNotEquals(graphiteMetric1_, graphiteMetric);
    }
    
    /**
     * Test of getGraphiteFormatString method, of class GraphiteMetric.
     */
    @Test
    public void testGetGraphiteFormatString() {
        String graphiteFormatString = "test.metric.path 12345.123 1382848111";
        System.out.println(graphiteMetric1_.getGraphiteFormatString());
        assertEquals(graphiteMetric1_.getGraphiteFormatString(), graphiteFormatString);
        
        graphiteFormatString = "test.metric.path?><!@#$! 12345 123";
        System.out.println(graphiteMetric2_.getGraphiteFormatString());
        assertEquals(graphiteMetric2_.getGraphiteFormatString(), graphiteFormatString);
    }

    /**
     * Test of getOpenTsdbFormatString method, of class GraphiteMetric.
     */
    @Test
    public void testGetOpenTsdbFormatString() {
        String openTsdbFormatString1 = "test.metric.path 1382848111 12345.123 Format=Graphite";
        assertEquals(graphiteMetric1_.getOpenTsdbFormatString(), openTsdbFormatString1);     
        
        String openTsdbFormatString2 = "test.metric.path?><!@#$! 123 12345 Format=Graphite";
        assertEquals(graphiteMetric2_.getOpenTsdbFormatString(), openTsdbFormatString2);        
    }
    
    /**
     * Test of parseGraphiteMetric method, of class GraphiteMetric.
     */
    @Test
    public void testParseGraphiteMetric() {
        String unparsedMetric = "test.metric.path 12345.123 1382848111";
        long metricReceivedTimestampInMilliseconds = Long.valueOf("1382848222222");

        GraphiteMetric expResult = new GraphiteMetric("global.graphite.test.metric.path", new BigDecimal("12345.123"), 1382848111, 1382848222222L);
        GraphiteMetric result = GraphiteMetric.parseGraphiteMetric(unparsedMetric, "global.graphite.", metricReceivedTimestampInMilliseconds);
        assertEquals(expResult, result);
    }

    /**
     * Test of parseGraphiteMetrics method, of class GraphiteMetric.
     */
    @Test
    public void testParseGraphiteMetrics() {
        String unparsedMetrics = "test.metric.path1 12345 1382848111\r\n"
                + "test.metric.path2 12345.2    1382848112\n" // this one should fail because of too many spaces
                + "test.metric.path2 12345.2 1382848119\n" 
                + "test.metric.path3 12345.23 1382848113\n\n"
                + "test.metric.path4 12345.234 1382848114";

        long metricReceivedTimestampInMilliseconds = Long.valueOf("1382848222222");

        List<GraphiteMetric> expectedGraphiteMetrics = new ArrayList<>();
        expectedGraphiteMetrics.add(new GraphiteMetric("test.metric.path1", new BigDecimal("12345"), 1382848111, 1382848222222L));
        expectedGraphiteMetrics.add(new GraphiteMetric("test.metric.path2", new BigDecimal("12345.2"), 1382848119, 1382848222222L));
        expectedGraphiteMetrics.add(new GraphiteMetric("test.metric.path3", new BigDecimal("12345.23"), 1382848113, 1382848222222L));
        expectedGraphiteMetrics.add(new GraphiteMetric("test.metric.path4", new BigDecimal("12345.234"), 1382848114, 1382848222222L));
        
        List<GraphiteMetric> resultGraphiteMetrics = GraphiteMetric.parseGraphiteMetrics(unparsedMetrics, null, metricReceivedTimestampInMilliseconds);
        
        for (GraphiteMetric graphiteMetric : resultGraphiteMetrics) {
            assertTrue(expectedGraphiteMetrics.contains(graphiteMetric));
        }
        
        assertTrue(expectedGraphiteMetrics.size() == resultGraphiteMetrics.size());
    }

    /**
     * Test of getMostRecentGraphiteMetricByMetricPath method, of class GraphiteMetric.
     */
    @Test
    public void testGetMostRecentGraphiteMetricByMetricPath() {

        List<GraphiteMetric> input = new ArrayList<>();
        
        GraphiteMetric graphiteMetric1 = new GraphiteMetric("test.metric.path1", new BigDecimal("12345.1"),  1382848111, Long.valueOf("1382848222222").longValue());
        GraphiteMetric graphiteMetric2 = new GraphiteMetric("test.metric.path1", new BigDecimal("12345"),    1382848113, Long.valueOf("1382848222220").longValue());
        GraphiteMetric graphiteMetric3 = new GraphiteMetric("test.metric.path1", new BigDecimal("12345.23"), 1382848112, Long.valueOf("1382848222223").longValue());
        GraphiteMetric graphiteMetric4 = new GraphiteMetric("test.metric.path2", new BigDecimal("12345.2"),  1382848111, 1382848222222L);   
        GraphiteMetric graphiteMetric5 = new GraphiteMetric("test.metric.path3", new BigDecimal("12345.1"),  1382848111, 1382848222222L);
        GraphiteMetric graphiteMetric6 = new GraphiteMetric("test.metric.path3", new BigDecimal("12345"),    1382848112, 1382848222220L);
        GraphiteMetric graphiteMetric7 = new GraphiteMetric("test.metric.path3", new BigDecimal("12345.23"), 1382848112, 1382848222223L);
        GraphiteMetric graphiteMetric8 = new GraphiteMetric("test.metric.path3", new BigDecimal("12345.23"), 1382848110, 1382848222224L);
        
        input.add(graphiteMetric1); input.add(graphiteMetric2); input.add(graphiteMetric3); input.add(graphiteMetric4); 
        input.add(graphiteMetric5); input.add(graphiteMetric6); input.add(graphiteMetric7); input.add(graphiteMetric8);
        
        Map<String, GraphiteMetric> result = GraphiteMetric.getMostRecentGraphiteMetricByMetricPath(input);
        
        assertTrue(result.values().contains(graphiteMetric2));
        assertTrue(result.values().contains(graphiteMetric4));
        assertTrue(result.values().contains(graphiteMetric7));
    }

    /**
     * Test of getMetricKey method, of class GraphiteMetric.
     */
    @Test
    public void testGetMetricKey() {
        assertEquals(graphiteMetric1_.getMetricPath(), graphiteMetric1_.getMetricKey());
        assertEquals(graphiteMetric2_.getMetricPath(), graphiteMetric2_.getMetricKey());
        
        assertNotEquals(graphiteMetric1_.getMetricPath(), graphiteMetric2_.getMetricKey());
        assertNotEquals(graphiteMetric2_.getMetricPath(), graphiteMetric1_.getMetricKey());
    }

}
