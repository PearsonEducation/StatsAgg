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
public class GraphiteMetricRawTest {
    
    private GraphiteMetricRaw graphiteMetricRaw1_;
    private GraphiteMetricRaw graphiteMetricRaw2_;
    private GraphiteMetricRaw graphiteMetricRaw3_;
    
    public GraphiteMetricRawTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        graphiteMetricRaw1_ = new GraphiteMetricRaw("test.metric.path", new BigDecimal("12345.123"), 1382848111, 1382848222222L);
        graphiteMetricRaw2_ = new GraphiteMetricRaw("test.metric.path?><!@#$!", new BigDecimal("12345"), 123, 1234L);
        graphiteMetricRaw3_ = new GraphiteMetricRaw("test.metric.path", new BigDecimal("12345.123"), 1382848111, 1382848222222L); // same as Raw1
    }
    
    @After
    public void tearDown() {
    }

//    /**
//     * Test of createAndGetMetricValueBigDecimal method, of class GraphiteMetricRaw.
//     */
//    @Test
//    public void testCreateAndGetMetricValueBigDecimal() {
//        BigDecimal result = graphiteMetricRaw1_.createAndGetMetricValueBigDecimal();
//        assertEquals(new BigDecimal("12345.123"), result);
//        
//        result = graphiteMetricRaw2_.createAndGetMetricValueBigDecimal();
//        assertEquals(new BigDecimal("12345"), result);
//    }

//    /**
//     * Test of createAndGetMetricTimestampInt method, of class GraphiteMetricRaw.
//     */
//    @Test
//    public void testCreateAndGetMetricTimestampInt() {
//        Integer result = graphiteMetricRaw1_.createAndGetMetricTimestampInt();
//        assertEquals(new Integer(1382848111), result);
//        
//        result = graphiteMetricRaw2_.createAndGetMetricTimestampInt();
//        assertEquals(new Integer(123), result);
//    }

    /**
     * Test of createAndGetMetricTimestampInMilliseconds method, of class GraphiteMetricRaw.
     */
    @Test
    public void testCreateAndGetMetricTimestampInMilliseconds() {
        Long result = graphiteMetricRaw1_.createAndGetMetricTimestampInMilliseconds();
        assertEquals(Long.valueOf("1382848111000"), result);
        
        result = graphiteMetricRaw2_.createAndGetMetricTimestampInMilliseconds();
        assertEquals(Long.valueOf("123000"), result);
    }

    /**
     * Test of equals method, of class GraphiteMetricRaw.
     */
    @Test
    public void testEquals() {
        assertEquals(graphiteMetricRaw1_, graphiteMetricRaw3_);
        assertNotEquals(graphiteMetricRaw1_, graphiteMetricRaw2_);
        
        GraphiteMetricRaw graphiteMetricRaw = new GraphiteMetricRaw("test.metric.path.", new BigDecimal("12345.123"), 1382848111, 1382848222222L);
        assertNotEquals(graphiteMetricRaw1_, graphiteMetricRaw);
        
        graphiteMetricRaw = new GraphiteMetricRaw("test.metric.path", new BigDecimal("12345.1234"), 1382848111, 1382848222222L);
        assertNotEquals(graphiteMetricRaw1_, graphiteMetricRaw);
        
        graphiteMetricRaw = new GraphiteMetricRaw("test.metric.path", new BigDecimal("12345.123"), 1382848112, 1382848222222L);
        assertNotEquals(graphiteMetricRaw1_, graphiteMetricRaw);
        
        graphiteMetricRaw = new GraphiteMetricRaw("test.metric.path", new BigDecimal("12345.123"), 1382848111, 1382848222221L);
        assertNotEquals(graphiteMetricRaw1_, graphiteMetricRaw);
    }
    
    /**
     * Test of getGraphiteFormatString method, of class GraphiteMetricRaw.
     */
    @Test
    public void testGetGraphiteFormatString() {
        String graphiteFormatString = "test.metric.path 12345.123 1382848111";
        assertEquals(graphiteMetricRaw1_.getGraphiteFormatString(), graphiteFormatString);
        
        graphiteFormatString = "test.metric.path?><!@#$! 12345 123";
        assertEquals(graphiteMetricRaw2_.getGraphiteFormatString(), graphiteFormatString);
    }

    /**
     * Test of parseGraphiteMetricRaw method, of class GraphiteMetricRaw.
     */
    @Test
    public void testParseGraphiteMetricRaw() {
        String unparsedMetric = "test.metric.path 12345.123 1382848111";
        long metricReceivedTimestampInMilliseconds = Long.valueOf("1382848222222");

        GraphiteMetricRaw expResult = new GraphiteMetricRaw("global.graphite.test.metric.path", new BigDecimal("12345.123"), 1382848111, 1382848222222L);
        GraphiteMetricRaw result = GraphiteMetricRaw.parseGraphiteMetricRaw(unparsedMetric, "global.graphite.", metricReceivedTimestampInMilliseconds);
        assertEquals(expResult, result);
    }

    /**
     * Test of parseGraphiteMetricsRaw method, of class GraphiteMetricRaw.
     */
    @Test
    public void testParseGraphiteMetricsRaw() {
        String unparsedMetrics = "test.metric.path1 12345 1382848111\r\n"
                + "test.metric.path2 12345.2    1382848112\n" // this one should fail because of too many spaces
                + "test.metric.path2 12345.2 1382848119\n" 
                + "test.metric.path3 12345.23 1382848113\n\n"
                + "test.metric.path4 12345.234 1382848114";

        long metricReceivedTimestampInMilliseconds = Long.valueOf("1382848222222");

        List<GraphiteMetricRaw> expectedGraphiteMetricsRaw = new ArrayList<>();
        expectedGraphiteMetricsRaw.add(new GraphiteMetricRaw("test.metric.path1", new BigDecimal("12345"), 1382848111, 1382848222222L));
        expectedGraphiteMetricsRaw.add(new GraphiteMetricRaw("test.metric.path2", new BigDecimal("12345.2"), 1382848119, 1382848222222L));
        expectedGraphiteMetricsRaw.add(new GraphiteMetricRaw("test.metric.path3", new BigDecimal("12345.23"), 1382848113, 1382848222222L));
        expectedGraphiteMetricsRaw.add(new GraphiteMetricRaw("test.metric.path4", new BigDecimal("12345.234"), 1382848114, 1382848222222L));
        
        List<GraphiteMetricRaw> resultGraphiteMetricsRaw = GraphiteMetricRaw.parseGraphiteMetricsRaw(unparsedMetrics, null, metricReceivedTimestampInMilliseconds);
        
        for (GraphiteMetricRaw graphiteMetricRaw : resultGraphiteMetricsRaw) {
            assertTrue(expectedGraphiteMetricsRaw.contains(graphiteMetricRaw));
        }
        
        assertTrue(expectedGraphiteMetricsRaw.size() == resultGraphiteMetricsRaw.size());
    }

    /**
     * Test of getMostRecentGraphiteMetricRawByMetricPath method, of class GraphiteMetricRaw.
     */
    @Test
    public void testGetMostRecentGraphiteMetricRawByMetricPath() {

        List<GraphiteMetricRaw> input = new ArrayList<>();
        
        GraphiteMetricRaw graphiteMetricRaw1 = new GraphiteMetricRaw("test.metric.path1", new BigDecimal("12345.1"),  1382848111, Long.valueOf("1382848222222").longValue());
        GraphiteMetricRaw graphiteMetricRaw2 = new GraphiteMetricRaw("test.metric.path1", new BigDecimal("12345"),    1382848113, Long.valueOf("1382848222220").longValue());
        GraphiteMetricRaw graphiteMetricRaw3 = new GraphiteMetricRaw("test.metric.path1", new BigDecimal("12345.23"), 1382848112, Long.valueOf("1382848222223").longValue());
        
        GraphiteMetricRaw graphiteMetricRaw4 = new GraphiteMetricRaw("test.metric.path2", new BigDecimal("12345.2"),  1382848111, 1382848222222L);   
        
        GraphiteMetricRaw graphiteMetricRaw5 = new GraphiteMetricRaw("test.metric.path3", new BigDecimal("12345.1"),  1382848111, 1382848222222L);
        GraphiteMetricRaw graphiteMetricRaw6 = new GraphiteMetricRaw("test.metric.path3", new BigDecimal("12345"),    1382848112, 1382848222220L);
        GraphiteMetricRaw graphiteMetricRaw7 = new GraphiteMetricRaw("test.metric.path3", new BigDecimal("12345.23"), 1382848112, 1382848222223L);
        GraphiteMetricRaw graphiteMetricRaw8 = new GraphiteMetricRaw("test.metric.path3", new BigDecimal("12345.23"), 1382848110, 1382848222224L);
        
        input.add(graphiteMetricRaw1);
        input.add(graphiteMetricRaw2);
        input.add(graphiteMetricRaw3);
        input.add(graphiteMetricRaw4);
        input.add(graphiteMetricRaw5);
        input.add(graphiteMetricRaw6);
        input.add(graphiteMetricRaw7);
        input.add(graphiteMetricRaw8);
        
        Map<String, GraphiteMetricRaw> result = GraphiteMetricRaw.getMostRecentGraphiteMetricRawByMetricPath(input);
        
        assertTrue(result.values().contains(graphiteMetricRaw2));
        assertTrue(result.values().contains(graphiteMetricRaw4));
        assertTrue(result.values().contains(graphiteMetricRaw7));
    }

    /**
     * Test of getMetricKey method, of class GraphiteMetricRaw.
     */
    @Test
    public void testGetMetricKey() {
        assertEquals(graphiteMetricRaw1_.getMetricPath(), graphiteMetricRaw1_.getMetricKey());
        assertEquals(graphiteMetricRaw2_.getMetricPath(), graphiteMetricRaw2_.getMetricKey());
        
        assertNotEquals(graphiteMetricRaw1_.getMetricPath(), graphiteMetricRaw2_.getMetricKey());
        assertNotEquals(graphiteMetricRaw2_.getMetricPath(), graphiteMetricRaw1_.getMetricKey());
    }

//    /**
//     * Test of getMetricValueBigDecimal method, of class GraphiteMetricRaw.
//     */
//    @Test
//    public void testGetMetricValueBigDecimal() {
//        assertEquals(graphiteMetricRaw1_.getMetricValueBigDecimal(), graphiteMetricRaw1_.createAndGetMetricValueBigDecimal());
//        assertEquals(graphiteMetricRaw2_.getMetricValueBigDecimal(), graphiteMetricRaw2_.createAndGetMetricValueBigDecimal());
//        
//        assertEquals(graphiteMetricRaw1_.getMetricValueBigDecimal(), new BigDecimal(graphiteMetricRaw1_.getMetricValue()));
//        assertEquals(graphiteMetricRaw2_.getMetricValueBigDecimal(), new BigDecimal(graphiteMetricRaw2_.getMetricValue()));
//        
//        assertNotEquals(graphiteMetricRaw1_.getMetricValueBigDecimal(), graphiteMetricRaw2_.createAndGetMetricValueBigDecimal());
//        assertNotEquals(graphiteMetricRaw2_.getMetricValueBigDecimal(), graphiteMetricRaw1_.createAndGetMetricValueBigDecimal());
//    }

//    /**
//     * Test of getMetricTimestampInt method, of class GraphiteMetricRaw.
//     */
//    @Test
//    public void testGetMetricTimestampInt() {
//        assertEquals(graphiteMetricRaw1_.getMetricTimestampInt(), graphiteMetricRaw1_.createAndGetMetricTimestampInt());
//        assertEquals(graphiteMetricRaw2_.getMetricTimestampInt(), graphiteMetricRaw2_.createAndGetMetricTimestampInt());
//        
//        assertEquals(Integer.toString(graphiteMetricRaw1_.getMetricTimestampInt()), graphiteMetricRaw1_.getMetricTimestamp());
//        assertEquals(Integer.toString(graphiteMetricRaw2_.getMetricTimestampInt()), graphiteMetricRaw2_.getMetricTimestamp());
//        
//        assertNotEquals(graphiteMetricRaw1_.getMetricTimestampInt(), graphiteMetricRaw2_.createAndGetMetricTimestampInt());
//        assertNotEquals(graphiteMetricRaw2_.getMetricTimestampInt(), graphiteMetricRaw1_.createAndGetMetricTimestampInt());
//    }

    /**
     * Test of getMetricTimestampInMilliseconds method, of class GraphiteMetricRaw.
     */
    @Test
    public void testGetMetricTimestampInMilliseconds() {
        assertEquals(graphiteMetricRaw1_.getMetricTimestampInMilliseconds(), graphiteMetricRaw1_.createAndGetMetricTimestampInMilliseconds());
        assertEquals(graphiteMetricRaw2_.getMetricTimestampInMilliseconds(), graphiteMetricRaw2_.createAndGetMetricTimestampInMilliseconds());
        
        assertEquals(Long.valueOf(graphiteMetricRaw1_.getMetricTimestampInMilliseconds() / 1000), new Long(graphiteMetricRaw1_.getMetricTimestamp()));
        assertEquals(Long.valueOf(graphiteMetricRaw1_.getMetricTimestampInMilliseconds() / 1000), new Long(graphiteMetricRaw1_.getMetricTimestamp()));
        
        assertNotEquals(graphiteMetricRaw1_.getMetricTimestampInMilliseconds(), graphiteMetricRaw2_.createAndGetMetricTimestampInMilliseconds());
        assertNotEquals(graphiteMetricRaw2_.getMetricTimestampInMilliseconds(), graphiteMetricRaw1_.createAndGetMetricTimestampInMilliseconds());
    }
    
}
