package com.pearson.statsagg.metric_formats.opentsdb;

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
public class OpenTsdbTest {
    
    public OpenTsdbTest() {
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
     * Test of getOpenTsdbFormatString method, of class OpenTsdbMetric.
     */
    @Test
    public void testGetGraphiteFormatString() {
        String unparsedMetric1 = "tcollector.reader..lines_collected@-\\/#$%^_123AaZz09... 1424566500 1203.3  tag2=mix  tag1#$%^=meow#$%^";
        OpenTsdbMetric parsedMetric1 = OpenTsdbMetric.parseOpenTsdbTelnetMetric(unparsedMetric1, "", 1366998400999L);
        
        // unsanitized, without substitution
        assertEquals(("tcollector.reader..lines_collected@-\\/#$%^_123AaZz09... 1203.3 1424566500"), parsedMetric1.getGraphiteFormatString(false, false));

        // sanitized, without substitution
        assertEquals(("tcollector.reader.lines_collected@-\\/#$%^_123AaZz09. 1203.3 1424566500"), parsedMetric1.getGraphiteFormatString(true, false));

        // sanitized, with substitution
        assertEquals(("tcollector.reader.lines_collected@-||#$Pct^_123AaZz09. 1203.3 1424566500"), parsedMetric1.getGraphiteFormatString(true,  true));
        
        // unsanitized, with substitution
        assertEquals(("tcollector.reader..lines_collected@-||#$Pct^_123AaZz09... 1203.3 1424566500"), parsedMetric1.getGraphiteFormatString(false,  true));
        
        // millisecond timestamp
        String unparsedMetric2 = "tcollector.reader.lines_collected 1424566500123 12  tag2=mix  tag1=meow";
        OpenTsdbMetric parsedMetric2 = OpenTsdbMetric.parseOpenTsdbTelnetMetric(unparsedMetric2, "", 1366998400999L);
        assertEquals("tcollector.reader.lines_collected 12 1424566500", parsedMetric2.getGraphiteFormatString(false, false));  
    }
    
    /**
     * Test of getOpenTsdbTelnetFormatString method, of class OpenTsdbMetric.
     */
    @Test
    public void testGetOpenTsdbTelnetFormatString() {
        // second timestamp
        String unparsedMetric1 = "tcollector.reader.lines_collected 1424566500 1203.3  tag2=mix  tag1=meow";
        OpenTsdbMetric parsedMetric1 = OpenTsdbMetric.parseOpenTsdbTelnetMetric(unparsedMetric1, "", 1366998400999L);
        assertEquals("tcollector.reader.lines_collected 1424566500 1203.3 tag1=meow tag2=mix", parsedMetric1.getOpenTsdbTelnetFormatString(false));     
        
        // millisecond timestamp
        String unparsedMetric2 = "tcollector.reader.lines_collected 1424566500123 12  tag2=mix  tag1=meow";
        OpenTsdbMetric parsedMetric2 = OpenTsdbMetric.parseOpenTsdbTelnetMetric(unparsedMetric2, "", 1366998400999L);
        assertEquals("tcollector.reader.lines_collected 1424566500123 12 tag1=meow tag2=mix", parsedMetric2.getOpenTsdbTelnetFormatString(false));  
        
        String unparsedMetric3 = "tcollector.reader.lines_collected@#$%^_123AaZz09 1424566500123 12  tag2=mix  tag1$#^=meow$#^";
        OpenTsdbMetric parsedMetric3 = OpenTsdbMetric.parseOpenTsdbTelnetMetric(unparsedMetric3, "", 1366998400999L);
        
        // unsanitized 
        assertEquals("tcollector.reader.lines_collected@#$%^_123AaZz09 1424566500123 12 tag1$#^=meow$#^ tag2=mix", parsedMetric3.getOpenTsdbTelnetFormatString(false));
        
        // sanitized
        assertEquals("tcollector.reader.lines_collected_123AaZz09 1424566500123 12 tag1=meow tag2=mix", parsedMetric3.getOpenTsdbTelnetFormatString(true));  
        
        // with extra, default, tag
        String unparsedMetric4 = "tcollector.reader.lines_collected 1424566500123 12  tag2=mix  tag1=meow";
        OpenTsdbMetric parsedMetric4 = OpenTsdbMetric.parseOpenTsdbTelnetMetric(unparsedMetric4, "", 1366998400999L);
        assertEquals("tcollector.reader.lines_collected 1424566500123 12 tag1=meow tag2=mix Taco=Bell", parsedMetric4.getOpenTsdbTelnetFormatString(true, "Taco", "Bell"));  
    }
    
    /**
     * Test of getOpenTsdbJsonFormatString method, of class OpenTsdbMetric.
     */
    @Test
    public void testGetOpenTsdbJsonFormatString() {
        // second timestamp
        String unparsedMetric1 = "tcollector.reader.lines_collected 1524566500 1203.3  tag2=mix  tag1=meow";
        OpenTsdbMetric parsedMetric1 = OpenTsdbMetric.parseOpenTsdbTelnetMetric(unparsedMetric1, "", 1366998400999L);
        assertEquals("{\"metric\":\"tcollector.reader.lines_collected\",\"timestamp\":1524566500,\"value\":1203.3,\"tags\":{\"tag1\":\"meow\",\"tag2\":\"mix\"}}", parsedMetric1.getOpenTsdbJsonFormatString(false));     
        
        // millisecond timestamp
        String unparsedMetric2 = "tcollector.reader.lines_collected@#$%^_123AaZz09 1424566500123 12  tag2=mix  tag1$#^=meow$#^";
        OpenTsdbMetric parsedMetric2 = OpenTsdbMetric.parseOpenTsdbTelnetMetric(unparsedMetric2, "", 1366998400999L);

        // unsanitized
        assertEquals("{\"metric\":\"tcollector.reader.lines_collected@#$%^_123AaZz09\",\"timestamp\":1424566500123,\"value\":12,\"tags\":{\"tag1$#^\":\"meow$#^\",\"tag2\":\"mix\"}}", parsedMetric2.getOpenTsdbJsonFormatString(false));  
    
        // sanitized
        assertEquals("{\"metric\":\"tcollector.reader.lines_collected_123AaZz09\",\"timestamp\":1424566500123,\"value\":12,\"tags\":{\"tag1\":\"meow\",\"tag2\":\"mix\"}}", parsedMetric2.getOpenTsdbJsonFormatString(true)); 
    
        // with extra, default, tag
        String unparsedMetric4 = "tcollector.reader.lines_collected@#$%^_123AaZz09 1424566500123 12 tag2=mix tag1=meow";
        OpenTsdbMetric parsedMetric4 = OpenTsdbMetric.parseOpenTsdbTelnetMetric(unparsedMetric4, "", 1366998400999L);
        System.out.println(parsedMetric4.getOpenTsdbJsonFormatString(true, "Taco", "Bell"));
        assertEquals("{\"metric\":\"tcollector.reader.lines_collected_123AaZz09\",\"timestamp\":1424566500123,\"value\":12,\"tags\":{\"tag1\":\"meow\",\"tag2\":\"mix\",\"Taco\":\"Bell\"}}", parsedMetric4.getOpenTsdbJsonFormatString(true, "Taco", "Bell"));  
    }
    
    /**
     * Test of getInfluxdbV1JsonFormatString method, of class OpenTsdbMetric.
     */
    @Test
    public void testGetInfluxdbV1JsonFormatString() {
        String unparsedMetric1 = "tcollector.reader.lines_collected 1424566500 1203.3  tag2=mix  tag1=meow";
        OpenTsdbMetric parsedMetric1 = OpenTsdbMetric.parseOpenTsdbTelnetMetric(unparsedMetric1, "", 1366998400999L);
        assertEquals("{\"name\":\"tcollector.reader.lines_collected\",\"columns\":[\"value\",\"time\",\"tag1\",\"tag2\"],\"points\":[[1203.3,1424566500000,\"meow\",\"mix\"]]}", parsedMetric1.getInfluxdbV1JsonFormatString());     
        
        String unparsedMetric2 = "tcollector.reader.lines_collected@#$%^_123AaZz09 1524566500123 12  tag2=mix  tag1@#$%^_123AaZz09=meow@#$%^_123AaZz09";
        OpenTsdbMetric parsedMetric2 = OpenTsdbMetric.parseOpenTsdbTelnetMetric(unparsedMetric2, "", 1366998400999L);
        assertEquals("{\"name\":\"tcollector.reader.lines_collected@#$%^_123AaZz09\",\"columns\":[\"value\",\"time\",\"tag1@#$%^_123AaZz09\",\"tag2\"],\"points\":[[12,1524566500123,\"meow@#$%^_123AaZz09\",\"mix\"]]}", parsedMetric2.getInfluxdbV1JsonFormatString());  
    }
    
    /**
     * Test of parseOpenTsdbTelnetMetric method, of class OpenTsdbMetric.
     */
    @Test
    public void testParseOpenTsdbMetric() {
        String unparsedMetric = "tcollector.reader.lines_collected 1424566500 1203.3  tag2=mix  tag1=meow";
        
        OpenTsdbMetric parsedMetric = OpenTsdbMetric.parseOpenTsdbTelnetMetric(unparsedMetric, "", 1366998400999L);
        
        assertTrue(parsedMetric.getMetric().equals("tcollector.reader.lines_collected"));
        assertTrue(parsedMetric.getMetricValue().equals(new BigDecimal("1203.3")));
        assertEquals(parsedMetric.getMetricValueBigDecimal().compareTo(new BigDecimal("1203.3")), 0);
        assertTrue(parsedMetric.getMetricTimestamp() == 1424566500L);
        assertTrue(parsedMetric.getMetricKey().equals("tcollector.reader.lines_collected : tag1=meow tag2=mix"));
        assertTrue(parsedMetric.getMetricTimestampInMilliseconds() == 1424566500000L);
        assertEquals(parsedMetric.getGraphiteFormatString(false, false), "tcollector.reader.lines_collected 1203.3 1424566500");
        assertEquals(parsedMetric.getOpenTsdbTelnetFormatString(false), "tcollector.reader.lines_collected 1424566500 1203.3 tag1=meow tag2=mix");
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
     * Test of parseOpenTsdbTelnetMetric method, of class OpenTsdbMetric.
     */
    @Test
    public void testParseOpenTsdb_DuplicatedTagKey() {
        String unparsedMetric = "tcollector.reader.lines_collected 1424566500 1203.3  tag=mix  tag=meow";
        OpenTsdbMetric parsedMetric = OpenTsdbMetric.parseOpenTsdbTelnetMetric(unparsedMetric, "", 1366998400999L);
        assertEquals(parsedMetric, null);
    }
    
    /**
     * Test of getOpenTsdbMetricJson method, of class OpenTsdbMetric.
     */
    @Test
    public void testGetOpenTsdbJson() {
        String unparsedMetric1 = "tcollector1.reader.lines_collected2 1424566501 1203.1  tag2=mix  tag1=meow";
        String unparsedMetric2 = "tcollector2.reader.lines_collected2 1424566502000 1203.2 tag3=maow tag4=mox";
        String unparsedMetric3 = "tcollector3.reader.lines_collected2 1424566501 1E6 tag2=mix  tag1=meow";
        
        OpenTsdbMetric parsedMetric1 = OpenTsdbMetric.parseOpenTsdbTelnetMetric(unparsedMetric1, "global.opentsdb.", 1366998400991L);
        OpenTsdbMetric parsedMetric2 = OpenTsdbMetric.parseOpenTsdbTelnetMetric(unparsedMetric2, null, 1366998400992L);
        OpenTsdbMetric parsedMetric3 = OpenTsdbMetric.parseOpenTsdbTelnetMetric(unparsedMetric3, "global.opentsdb.", 1366998400991L);
        
        List<OpenTsdbMetric> openTsdbMetrics = new ArrayList<>();
        openTsdbMetrics.add(parsedMetric1);
        openTsdbMetrics.add(parsedMetric2);
        openTsdbMetrics.add(parsedMetric3);
        
        String json = OpenTsdbMetric.getOpenTsdbJson(openTsdbMetrics, false);
        String expectedJson = "[{\"metric\":\"global.opentsdb.tcollector1.reader.lines_collected2\",\"timestamp\":1424566501,\"value\":1203.1,\"tags\":{\"tag1\":\"meow\",\"tag2\":\"mix\"}},{\"metric\":\"tcollector2.reader.lines_collected2\",\"timestamp\":1424566502000,\"value\":1203.2,\"tags\":{\"tag3\":\"maow\",\"tag4\":\"mox\"}},{\"metric\":\"global.opentsdb.tcollector3.reader.lines_collected2\",\"timestamp\":1424566501,\"value\":1000000,\"tags\":{\"tag1\":\"meow\",\"tag2\":\"mix\"}}]";
        assertEquals(expectedJson, json);
    }

    /**
     * Test of parseOpenTsdbJson method, of class OpenTsdbMetric.
     */
    @Test
    public void testParseOpenTsdbJson_MultiMetric() {
        
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
                + "    },\n"
                + "    {\n"
                + "        \"metric\": \"sys.cpu.nice4\",\n"
                + "        \"timestamp\": 1346846400,\n"
                + "        \"value\": \"1E6\",\n"
                + "        \"tags\": {\n"
                + "           \"host\": \"web03\",\n"
                + "           \"dc\": \"lga\"\n"
                + "        }\n"
                + "    }\n"
                + "]";
        
        List<Integer> successCountAndFailCount = new ArrayList<>();
        List<OpenTsdbMetric> openTsdbMetrics = OpenTsdbMetric.parseOpenTsdbJson(inputJson, "global.opentsdb.", System.currentTimeMillis(), successCountAndFailCount);
        
        assertEquals(openTsdbMetrics.size(), 3);
        assertEquals(successCountAndFailCount.get(0).intValue(), 3);
        assertEquals(successCountAndFailCount.get(1).intValue(), 1);

        int matchCount = 0;
        for (OpenTsdbMetric openTsdbMetric: openTsdbMetrics) {
            if (openTsdbMetric.getMetric().equals("global.opentsdb.sys.cpu.nice1")) {
                assertTrue(openTsdbMetric.getMetricValue().equals(new BigDecimal("11.4")));
                assertTrue(openTsdbMetric.getMetricTimestamp() == 1346846400123L);
                assertTrue(openTsdbMetric.getMetricKey().equals("global.opentsdb.sys.cpu.nice1 : dc=lga host=web01"));
                matchCount++;
            }
            
            if (openTsdbMetric.getMetric().equals("global.opentsdb.sys.cpu.nice2")) {
                assertTrue(openTsdbMetric.getMetricValue().equals(new BigDecimal("9")));
                assertTrue(openTsdbMetric.getMetricTimestamp() == 1346846400L);
                assertTrue(openTsdbMetric.getMetricKey().equals("global.opentsdb.sys.cpu.nice2 : dc=lga host=web02"));
                matchCount++;
            }
            
            
            if (openTsdbMetric.getMetric().equals("global.opentsdb.sys.cpu.nice4")) {
                assertTrue(openTsdbMetric.getMetricValue().compareTo(new BigDecimal("1000000")) == 0);
                assertTrue(openTsdbMetric.getMetricTimestamp() == 1346846400L);
                assertTrue(openTsdbMetric.getMetricKey().equals("global.opentsdb.sys.cpu.nice4 : dc=lga host=web03"));
                matchCount++;
            }
        }
        
        assertEquals(openTsdbMetrics.size(), matchCount);
    }
    /**
     * Test of parseOpenTsdbJson method, of class OpenTsdbMetric.
     */
    
    @Test
    public void testParseOpenTsdbJson_SingleMetric() {
        
        String inputJson = ""
                + "    {\n"
                + "        \"metric\": \"sys.cpu.nice1\",\n"
                + "        \"timestamp\": 1346846400123,\n"
                + "        \"value\": 11.4,\n"
                + "        \"tags\": {\n"
                + "           \"host\": \"web01\",\n"
                + "           \"dc\": \"lga\"\n"
                + "        }\n"
                + "    }\n";
        
        List<Integer> successCountAndFailCount = new ArrayList<>();
        List<OpenTsdbMetric> openTsdbMetrics = OpenTsdbMetric.parseOpenTsdbJson(inputJson, "global.opentsdb.", System.currentTimeMillis(), successCountAndFailCount);
        
        assertEquals(openTsdbMetrics.size(), 1);
        assertEquals(successCountAndFailCount.get(0).intValue(), 1);
        assertEquals(successCountAndFailCount.get(1).intValue(), 0);

        int matchCount = 0;
        for (OpenTsdbMetric openTsdbMetric: openTsdbMetrics) {
            if (openTsdbMetric.getMetric().equals("global.opentsdb.sys.cpu.nice1")) {
                assertTrue(openTsdbMetric.getMetricValue().equals(new BigDecimal("11.4")));
                assertTrue(openTsdbMetric.getMetricTimestamp() == 1346846400123L);
                assertTrue(openTsdbMetric.getMetricKey().equals("global.opentsdb.sys.cpu.nice1 : dc=lga host=web01"));
                matchCount++;
            }
        }
        
        assertEquals(openTsdbMetrics.size(), matchCount);
    }
    
}
