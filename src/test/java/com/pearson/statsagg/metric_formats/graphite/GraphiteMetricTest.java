package com.pearson.statsagg.metric_formats.graphite;

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
public class GraphiteMetricTest {
    
    private GraphiteMetric graphiteMetric1_;
    private GraphiteMetric graphiteMetric2_;
    private GraphiteMetric graphiteMetric3_;
    private GraphiteMetric graphiteMetric4_;
    
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
        graphiteMetric3_ = new GraphiteMetric("test.metric.path", new BigDecimal("12345.123"), 1382848111, 1382848222222L); // same as metric1
        graphiteMetric4_ = new GraphiteMetric("test.metric.path@-\\/#$%^_123AaZz09...", new BigDecimal("12345.000"), 123, 1234L);
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
        // simple test -- includes decimals in value
        assertEquals(("test.metric.path 12345.123 1382848111"), graphiteMetric1_.getGraphiteFormatString(false, false));
        
        // unsanitized, without substitution
        assertEquals(("test.metric.path@-\\/#$%^_123AaZz09... 12345 123"), graphiteMetric4_.getGraphiteFormatString(false, false));

        // sanitized, without substitution
        assertEquals(("test.metric.path@-\\/#$%^_123AaZz09. 12345 123"), graphiteMetric4_.getGraphiteFormatString(true, false));
  
        // sanitized, with substitution
        assertEquals(("test.metric.path@-||#$Pct^_123AaZz09. 12345 123"), graphiteMetric4_.getGraphiteFormatString(true, true));
        
        // unsanitized, with substitution
        assertEquals(("test.metric.path@-||#$Pct^_123AaZz09... 12345 123"), graphiteMetric4_.getGraphiteFormatString(false, true));
    }

    /**
     * Test of getOpenTsdbTelnetFormatString method, of class GraphiteMetric.
     */
    @Test
    public void testGetOpenTsdbTelnetFormatString() {
        String openTsdbFormatString1 = "test.metric.path 1382848111 12345.123 Format=Graphite";
        assertEquals(graphiteMetric1_.getOpenTsdbTelnetFormatString(false), openTsdbFormatString1);     
        
        String openTsdbFormatString2 = "test.metric.path?><!@#$! 123 12345 Format=Graphite";
        assertEquals(graphiteMetric2_.getOpenTsdbTelnetFormatString(false), openTsdbFormatString2);   
        
        String openTsdbFormatString3 = "test.metric.path?><!@#$! 123 12345 Taco=Bell";
        assertEquals(graphiteMetric2_.getOpenTsdbTelnetFormatString(false, "Taco", "Bell"), openTsdbFormatString3);  
    }
    
    /**
     * Test of getOpenTsdbJsonFormatString method, of class GraphiteMetric.
     */
    @Test
    public void testGetOpenTsdbJsonFormatString() {
        String openTsdbFormatString1 = "{\"metric\":\"test.metric.path\",\"timestamp\":1382848111,\"value\":12345.123,\"tags\":{\"Format\":\"Graphite\"}}";
        assertEquals(graphiteMetric1_.getOpenTsdbJsonFormatString(false), openTsdbFormatString1);     
        
        String openTsdbFormatString2 = "{\"metric\":\"test.metric.path?><!@#$!\",\"timestamp\":123,\"value\":12345,\"tags\":{\"Format\":\"Graphite\"}}";
        assertEquals(graphiteMetric2_.getOpenTsdbJsonFormatString(false), openTsdbFormatString2);      
        
        String openTsdbFormatString3 = "{\"metric\":\"test.metric.path\",\"timestamp\":123,\"value\":12345,\"tags\":{\"Format\":\"Graphite\"}}";
        assertEquals(graphiteMetric2_.getOpenTsdbJsonFormatString(true), openTsdbFormatString3);    
        
        String openTsdbFormatString4 = "{\"metric\":\"test.metric.path\",\"timestamp\":123,\"value\":12345,\"tags\":{\"Taco\":\"Bell\"}}";
        assertEquals(graphiteMetric2_.getOpenTsdbJsonFormatString(true, "Taco", "Bell"), openTsdbFormatString4);    
    }
    
    /**
     * Test of getInfluxdbV1JsonFormatString method, of class GraphiteMetric.
     */
    @Test
    public void testgetInfluxdbV1JsonFormatString() {
        String influxdbFormatString1 = "{\"name\":\"test.metric.path\",\"columns\":[\"value\",\"time\"],\"points\":[[12345.123,1382848111000]]}";
        assertEquals(graphiteMetric1_.getInfluxdbV1JsonFormatString(), influxdbFormatString1);     
        
        String influxdbFormatString2 = "{\"name\":\"test.metric.path?><!@#$!\",\"columns\":[\"value\",\"time\"],\"points\":[[12345,123000]]}";
        assertEquals(graphiteMetric2_.getInfluxdbV1JsonFormatString(), influxdbFormatString2);        
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
