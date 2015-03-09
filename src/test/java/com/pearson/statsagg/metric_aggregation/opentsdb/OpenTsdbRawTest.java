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
      
    /**
     * Test of getOpenTsdbJson method, of class OpenTsdbMetricRaw.
     */
    @Test
    public void testGetOpenTsdbJson() {
        
        String unparsedMetric1 = "tcollector1.reader.lines_collected2 1424566501 1203.1  tag2=mix  tag1=meow";
        String unparsedMetric2 = "tcollector2.reader.lines_collected2 1424566502 1203.2  tag3=maow tag4=mox";
        
        OpenTsdbMetricRaw parsedMetric1 = OpenTsdbMetricRaw.parseOpenTsdbMetricRaw(unparsedMetric1, 1366998400991L);
        OpenTsdbMetricRaw parsedMetric2 = OpenTsdbMetricRaw.parseOpenTsdbMetricRaw(unparsedMetric2, 1366998400992L);

        List<OpenTsdbMetricRaw> openTsdbMetricsRaw = new ArrayList<>();
        openTsdbMetricsRaw.add(parsedMetric1);
        openTsdbMetricsRaw.add(parsedMetric2);
        
        String json = OpenTsdbMetricRaw.getOpenTsdbJson(openTsdbMetricsRaw);
        assertEquals(json, "[{\"metric\":\"tcollector1.reader.lines_collected2\",\"timestamp\":1424566501,\"value\":1424566501,\"tags\":{\"tag2\":\"mix\",\"tag1\":\"meow\"}},{\"metric\":\"tcollector2.reader.lines_collected2\",\"timestamp\":1424566502,\"value\":1424566502,\"tags\":{\"tag3\":\"maow\",\"tag4\":\"mox\"}}]");
    }

    /**
     * Test of parseOpenTsdbJson method, of class OpenTsdbMetricRaw.
     */
    @Test
    public void testParseOpenTsdbJson() {
        
        String inputJson = "[\n"
                + "    {\n"
                + "        \"metric\": \"sys.cpu.nice1\",\n"
                + "        \"timestamp\": 1346846400123,\n"
                + "        \"value\": 11.4,\n"
                + "        \"tags\": {\n"
                + "           \"host\": \"web01\",\n"
                + "           \"dc\": \"lga\"\n"
                + "        }\n"
                + "    },\n"
                + "    {\n"
                + "        \"metric\": \"sys.cpu.nice2\",\n"
                + "        \"timestamp\": 1346846400,\n"
                + "        \"value\": 9,\n"
                + "        \"tags\": {\n"
                + "           \"host\": \"web02\",\n"
                + "           \"dc\": \"lga\"\n"
                + "        }\n"
                + "    }\n"
                + "]";
        
        List<OpenTsdbMetricRaw> openTsdbMetricsRaw = OpenTsdbMetricRaw.parseOpenTsdbJson(inputJson, System.currentTimeMillis());
        
        assertEquals(openTsdbMetricsRaw.size(), 2);
        
        int matchCount = 0;
        for (OpenTsdbMetricRaw openTsdbMetricRaw: openTsdbMetricsRaw) {
            if (openTsdbMetricRaw.getMetric().equals("sys.cpu.nice1")) {
                assertTrue(openTsdbMetricRaw.getMetricValue().equals("11.4"));
                assertTrue(openTsdbMetricRaw.getMetricTimestamp().equals("1346846400123"));
                assertTrue(openTsdbMetricRaw.getMetricKey().equals("sys.cpu.nice1 : dc=lga host=web01"));
                matchCount++;
            }
            
            if (openTsdbMetricRaw.getMetric().equals("sys.cpu.nice2")) {
                assertTrue(openTsdbMetricRaw.getMetricValue().equals("9"));
                assertTrue(openTsdbMetricRaw.getMetricTimestamp().equals("1346846400"));
                assertTrue(openTsdbMetricRaw.getMetricKey().equals("sys.cpu.nice2 : dc=lga host=web02"));
                matchCount++;
            }
        }
        
        assertEquals(openTsdbMetricsRaw.size(), matchCount);
    }
    
}
