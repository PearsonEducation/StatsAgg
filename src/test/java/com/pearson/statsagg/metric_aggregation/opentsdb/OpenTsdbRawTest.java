package com.pearson.statsagg.metric_aggregation.opentsdb;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
     * Test of getOpenTsdbFormatString method, of class OpenTsdbMetricRaw.
     */
    @Test
    public void testGetGraphiteFormatString() {
        String unparsedMetric1 = "tcollector.reader.lines_collected 1424566500 1203.3  tag2=mix  tag1=meow";
        OpenTsdbMetricRaw parsedMetric1 = OpenTsdbMetricRaw.parseOpenTsdbMetricRaw(unparsedMetric1, "", 1366998400999L);
        assertEquals("tcollector.reader.lines_collected 1203.3 1424566500", parsedMetric1.getGraphiteFormatString());     
        
        String unparsedMetric2 = "tcollector.reader.lines_collected 1424566500123 12  tag2=mix  tag1=meow";
        OpenTsdbMetricRaw parsedMetric2 = OpenTsdbMetricRaw.parseOpenTsdbMetricRaw(unparsedMetric2, "", 1366998400999L);
        assertEquals("tcollector.reader.lines_collected 12 1424566500", parsedMetric2.getGraphiteFormatString());  
    }
    
    /**
     * Test of getOpenTsdbFormatString method, of class OpenTsdbMetricRaw.
     */
    @Test
    public void testGetOpenTsdbFormatString() {
        String unparsedMetric1 = "tcollector.reader.lines_collected 1424566500 1203.3  tag2=mix  tag1=meow";
        OpenTsdbMetricRaw parsedMetric1 = OpenTsdbMetricRaw.parseOpenTsdbMetricRaw(unparsedMetric1, "", 1366998400999L);
        assertEquals("tcollector.reader.lines_collected 1424566500 1203.3 tag1=meow tag2=mix", parsedMetric1.getOpenTsdbFormatString());     
        
        String unparsedMetric2 = "tcollector.reader.lines_collected 1424566500123 12  tag2=mix  tag1=meow";
        OpenTsdbMetricRaw parsedMetric2 = OpenTsdbMetricRaw.parseOpenTsdbMetricRaw(unparsedMetric2, "", 1366998400999L);
        assertEquals("tcollector.reader.lines_collected 1424566500123 12 tag1=meow tag2=mix", parsedMetric2.getOpenTsdbFormatString());  
    }
    
    /**
     * Test of parseOpenTsdbMetricRaw method, of class OpenTsdbMetricRaw.
     */
    @Test
    public void testParseOpenTsdbRaw() {
        String unparsedMetric = "tcollector.reader.lines_collected 1424566500 1203.3  tag2=mix  tag1=meow";
        
        OpenTsdbMetricRaw parsedMetric = OpenTsdbMetricRaw.parseOpenTsdbMetricRaw(unparsedMetric, "", 1366998400999L);
        
        assertTrue(parsedMetric.getMetric().equals("tcollector.reader.lines_collected"));
        assertTrue(parsedMetric.getMetricValue().equals(new BigDecimal("1203.3")));
        assertEquals(parsedMetric.getMetricValueBigDecimal().compareTo(new BigDecimal("1203.3")), 0);
        assertTrue(parsedMetric.getMetricTimestamp() == 1424566500L);
        assertTrue(parsedMetric.getMetricKey().equals("tcollector.reader.lines_collected : tag1=meow tag2=mix"));
        assertTrue(parsedMetric.getMetricTimestampInMilliseconds() == 1424566500000L);
        assertEquals(parsedMetric.getGraphiteFormatString(), "tcollector.reader.lines_collected 1203.3 1424566500");
        assertEquals(parsedMetric.getOpenTsdbFormatString(), "tcollector.reader.lines_collected 1424566500 1203.3 tag1=meow tag2=mix");
        assertEquals(parsedMetric.getTags().size(), 2);
        
        int tagCount = 0;
        for (OpenTsdbTag tag : parsedMetric.getTags()) {
            if (tag.getTag().equals("tag1=meow")) {
                assertTrue(tag.getTagKey().equals("tag1"));
                assertTrue(tag.getTagValue().equals("meow"));
                tagCount++;
            }
            
            if (tag.getTag().equals("tag2=mix")) {
                assertTrue(tag.getTagKey().equals("tag2"));
                assertTrue(tag.getTagValue().equals("mix"));
                tagCount++;
            }
        }
        
        assertEquals(tagCount, 2);
    }
      
    /**
     * Test of parseOpenTsdbMetricRaw method, of class OpenTsdbMetricRaw.
     */
    @Test
    public void testParseOpenTsdbRaw_DuplicatedTagKey() {
        String unparsedMetric = "tcollector.reader.lines_collected 1424566500 1203.3  tag=mix  tag=meow";
        OpenTsdbMetricRaw parsedMetric = OpenTsdbMetricRaw.parseOpenTsdbMetricRaw(unparsedMetric, "", 1366998400999L);
        assertEquals(parsedMetric, null);
    }
    
    /**
     * Test of getOpenTsdbJson method, of class OpenTsdbMetricRaw.
     */
    @Test
    public void testGetOpenTsdbJson() {
        String unparsedMetric1 = "tcollector1.reader.lines_collected2 1424566501 1203.1  tag2=mix  tag1=meow";
        String unparsedMetric2 = "tcollector2.reader.lines_collected2 1424566502000 1203.2 tag3=maow tag4=mox";
        
        OpenTsdbMetricRaw parsedMetric1 = OpenTsdbMetricRaw.parseOpenTsdbMetricRaw(unparsedMetric1, "global.opentsdb.", 1366998400991L);
        OpenTsdbMetricRaw parsedMetric2 = OpenTsdbMetricRaw.parseOpenTsdbMetricRaw(unparsedMetric2, null, 1366998400992L);

        List<OpenTsdbMetricRaw> openTsdbMetricsRaw = new ArrayList<>();
        openTsdbMetricsRaw.add(parsedMetric1);
        openTsdbMetricsRaw.add(parsedMetric2);
        
        String json = OpenTsdbMetricRaw.getOpenTsdbJson(openTsdbMetricsRaw);
        System.out.println(json);
        String expectedJson = "[{\"metric\":\"global.opentsdb.tcollector1.reader.lines_collected2\",\"timestamp\":1424566501,\"value\":1203.1,\"tags\":{\"tag1\":\"meow\",\"tag2\":\"mix\"}},{\"metric\":\"tcollector2.reader.lines_collected2\",\"timestamp\":1424566502000,\"value\":1203.2,\"tags\":{\"tag3\":\"maow\",\"tag4\":\"mox\"}}]";
        assertEquals(expectedJson, json);
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
                + "    },\n"
                + "    {\n"
                + "        \"metric\": \"sys.cpu.nice3\",\n"
                + "        \"timestamp\": 1346846400,\n"
                + "        \"value\": taco,\n"
                + "        \"tags\": {\n"
                + "           \"host\": \"web03\",\n"
                + "           \"dc\": \"lga\"\n"
                + "        }\n"
                + "    }\n"
                + "]";
        
        List<Integer> successCountAndFailCount = new ArrayList<>();
        List<OpenTsdbMetricRaw> openTsdbMetricsRaw = OpenTsdbMetricRaw.parseOpenTsdbJson(inputJson, "global.opentsdb.", System.currentTimeMillis(), successCountAndFailCount);
        
        assertEquals(openTsdbMetricsRaw.size(), 2);
        assertEquals(successCountAndFailCount.get(0).intValue(), 2);
        assertEquals(successCountAndFailCount.get(1).intValue(), 1);

        int matchCount = 0;
        for (OpenTsdbMetricRaw openTsdbMetricRaw: openTsdbMetricsRaw) {
            if (openTsdbMetricRaw.getMetric().equals("global.opentsdb.sys.cpu.nice1")) {
                assertTrue(openTsdbMetricRaw.getMetricValue().equals(new BigDecimal("11.4")));
                assertTrue(openTsdbMetricRaw.getMetricTimestamp() == 1346846400123L);
                assertTrue(openTsdbMetricRaw.getMetricKey().equals("global.opentsdb.sys.cpu.nice1 : dc=lga host=web01"));
                matchCount++;
            }
            
            if (openTsdbMetricRaw.getMetric().equals("global.opentsdb.sys.cpu.nice2")) {
                assertTrue(openTsdbMetricRaw.getMetricValue().equals(new BigDecimal("9")));
                assertTrue(openTsdbMetricRaw.getMetricTimestamp() == 1346846400L);
                assertTrue(openTsdbMetricRaw.getMetricKey().equals("global.opentsdb.sys.cpu.nice2 : dc=lga host=web02"));
                matchCount++;
            }
        }
        
        assertEquals(openTsdbMetricsRaw.size(), matchCount);
    }
    
}
