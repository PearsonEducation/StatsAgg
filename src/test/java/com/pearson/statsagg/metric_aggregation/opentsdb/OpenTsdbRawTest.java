package com.pearson.statsagg.metric_aggregation.opentsdb;

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
 * @author Jeffrey Schmidt
 */
public class OpenTsdbRawTest {
    
    public OpenTsdbRawTest() {
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
     * Test of parseOpenTsdbMetricRaw method, of class OpenTsdbMetricRaw.
     */
    @Test
    public void testParseOpenTsdbRaw() {
        String unparsedMetric = "tcollector.reader.lines_collected 1424566500 1203.3  tag2=mix  tag1=meow";
        
        OpenTsdbMetricRaw parsedMetric = OpenTsdbMetricRaw.parseOpenTsdbMetricRaw(unparsedMetric, 1366998400999L);
        parsedMetric.createAndGetMetricTimestamp();
        parsedMetric.createAndGetMetricValueBigDecimal();
        
        assertTrue(parsedMetric.getMetric().equals("tcollector.reader.lines_collected"));
        assertTrue(parsedMetric.getMetricValue().equals("1203.3"));
        assertEquals(parsedMetric.getMetricValueBigDecimal().compareTo(new BigDecimal("1203.3")), 0);
        assertTrue(parsedMetric.getMetricTimestamp().equals("1424566500"));
        assertTrue(parsedMetric.getMetricKey().equals("tcollector.reader.lines_collected : tag1=meow tag2=mix"));
        assertTrue(parsedMetric.getMetricTimestampInMilliseconds().equals(1424566500000L));
        assertEquals(parsedMetric.getGraphiteFormatString(), "tcollector.reader.lines_collected 1203.3 1424566500");
        assertEquals(parsedMetric.toString(), "tcollector.reader.lines_collected 1424566500 1203.3 tag2=mix tag1=meow");

        assertEquals(parsedMetric.getTags().size(), 2);
        
        int tagCount = 0;
        for (OpenTsdbTag tag : parsedMetric.getTags()) {
            if (tag.getUnparsedTag().equals("tag1=meow")) {
                assertTrue(tag.getKey().equals("tag1"));
                assertTrue(tag.getValue().equals("meow"));
                tagCount++;
            }
            
            if (tag.getUnparsedTag().equals("tag2=mix")) {
                assertTrue(tag.getKey().equals("tag2"));
                assertTrue(tag.getValue().equals("mix"));
                tagCount++;
            }
        }
        
        assertEquals(tagCount, 2);
    }

    
    
    /**
     * Test of createPrefixedOpenTsdbMetricsRaw method, of class OpenTsdbMetricRaw.
     */
    @Test
    public void testCreatePrefixedOpenTsdbMetricsRaw() {
        
        String unparsedMetric = "tcollector.reader.lines_collected 1424566500654 1203.3  tag1=meow tag2=mix";
        OpenTsdbMetricRaw parsedMetric = OpenTsdbMetricRaw.parseOpenTsdbMetricRaw(unparsedMetric, 1366998400999L);
        List<OpenTsdbMetricRaw> openTsdbMetricsRaw = new ArrayList<>();
        openTsdbMetricsRaw.add(parsedMetric);
        
        List<OpenTsdbMetricRaw> prefixedOpenTsdbMetricsRaw = OpenTsdbMetricRaw.createPrefixedOpenTsdbMetricsRaw(openTsdbMetricsRaw, true, "myGlobalPrefix", true, "myOpenTsdbPrefix");
        assertEquals(prefixedOpenTsdbMetricsRaw.size(), 1);
        
        assertTrue(prefixedOpenTsdbMetricsRaw.get(0).getMetric().equals("myGlobalPrefix.myOpenTsdbPrefix.tcollector.reader.lines_collected"));
    }
      
}
