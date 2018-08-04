package com.pearson.statsagg.metric_formats.influxdb;

import java.util.List;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Jeffrey Schmidt
 */
public class InfluxdbStandardizedMetric_Test {
    
    private String json1_ = "" +
                "[\n" +
                "  {\n" +
                "    \"name\" : \"metricName..@-\\\\/#$%^_123AaZz09\",\n" +
                "    \"columns\" : [\"time\", \"sequence_number\", \"metricColumn..@-\\\\/#$%^_123AaZz09\", \"metricTagKey..@-\\\\/#$%^_123AaZz09\", \"key2\"],\n" +
                "    \"points\" : [\n" +
                "      [1436135662888123, 1, 123.456, \"metricTagValue..@-\\\\/#$%^_123AaZz09\", \"val2\"]\n" +
                "    ]\n" +
                "  }\n" +
                "]";
    
    private String json2_ = "" +
            "[\n" +
            "  {\n" +
            "    \"name\" : \"metricName\",\n" +
            "    \"columns\" : [\"time\", \"sequence_number\", \"metricColumn\"],\n" +
            "    \"points\" : [\n" +
            "      [1436135662888123, 1, 123.456],\n" +
            "      [1436135662888123, 1, false]\n" +
            "    ]\n" +
            "  }\n" +
            "]";    
    
    private InfluxdbStandardizedMetric influxdbMetric1;
    private InfluxdbStandardizedMetric influxdbMetric2;
    private InfluxdbStandardizedMetric influxdbMetric3;

    public InfluxdbStandardizedMetric_Test() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        List<InfluxdbMetric_v1> influxdbMetrics1 = InfluxdbMetric_v1.parseInfluxdbMetricJson("metricDb..@-\\/#$%^_123AaZz09", json1_, "user", "pass", null, 
                "u", "..metricPrefix..@-\\/#$%^_123AaZz09.", 1436135662888l);
        
        List<InfluxdbMetric_v1> influxdbMetrics2 = InfluxdbMetric_v1.parseInfluxdbMetricJson("metricDb", json2_, "user", "pass", null, "u", "metricPrefix.", 1436135662888l);
        
        assertEquals(influxdbMetrics1.size(), 1);
        assertEquals(influxdbMetrics2.size(), 1);
        assertEquals(influxdbMetrics1.get(0).getInfluxdbStandardizedMetrics().size(), 1);
        assertEquals(influxdbMetrics2.get(0).getInfluxdbStandardizedMetrics().size(), 2);

        influxdbMetric1 = influxdbMetrics1.get(0).getInfluxdbStandardizedMetrics().get(0);
        influxdbMetric2 = influxdbMetrics2.get(0).getInfluxdbStandardizedMetrics().get(0);
        influxdbMetric3 = influxdbMetrics2.get(0).getInfluxdbStandardizedMetrics().get(1);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getGraphiteFormatString method, of class InfluxdbStandardizedMetric.
     */
    @Test
    public void testGetGraphiteFormatString() {
        
        // unsanitized, without substitution
        String expected = "metricDb..@-\\/#$%^_123AaZz09...metricPrefix..@-\\/#$%^_123AaZz09.metricName..@-\\/#$%^_123AaZz09.metricColumn..@-\\/#$%^_123AaZz09 123.456 1436135662";
        assertEquals(expected, influxdbMetric1.getGraphiteFormatString(false, false));
        
        // sanitized, without substitution
        expected = "metricDb.@-\\/#$%^_123AaZz09.metricPrefix.@-\\/#$%^_123AaZz09.metricName.@-\\/#$%^_123AaZz09.metricColumn.@-\\/#$%^_123AaZz09 123.456 1436135662";
        assertEquals(expected, influxdbMetric1.getGraphiteFormatString(true, false));
        
        // unsanitized, with substitution
        expected = "metricDb..@-||#$Pct^_123AaZz09...metricPrefix..@-||#$Pct^_123AaZz09.metricName..@-||#$Pct^_123AaZz09.metricColumn..@-||#$Pct^_123AaZz09 123.456 1436135662";
        assertEquals(expected, influxdbMetric1.getGraphiteFormatString(false, true));
        
        // sanitized, with substitution
        expected = "metricDb.@-||#$Pct^_123AaZz09.metricPrefix.@-||#$Pct^_123AaZz09.metricName.@-||#$Pct^_123AaZz09.metricColumn.@-||#$Pct^_123AaZz09 123.456 1436135662";
        System.out.println(influxdbMetric1.getGraphiteFormatString(true, true));
        assertEquals(expected, influxdbMetric1.getGraphiteFormatString(true, true));
    }

    /**
     * Test of getOpenTsdbTelnetFormatString method, of class InfluxdbStandardizedMetric.
     */
    @Test
    public void testGetOpenTsdbTelnetFormatString() {
        // unsanitized
        String expected = "metricDb..@-\\/#$%^_123AaZz09...metricPrefix..@-\\/#$%^_123AaZz09.metricName..@-\\/#$%^_123AaZz09.metricColumn..@-\\/#$%^_123AaZz09 1436135662888 123.456 metricTagKey..@-\\/#$%^_123AaZz09=metricTagValue..@-\\/#$%^_123AaZz09 key2=val2";
        assertEquals(expected, influxdbMetric1.getOpenTsdbTelnetFormatString(false));
        
        // sanitized
        expected = "metricDb..-/_123AaZz09...metricPrefix..-/_123AaZz09.metricName..-/_123AaZz09.metricColumn..-/_123AaZz09 1436135662888 123.456 metricTagKey..-/_123AaZz09=metricTagValue..-/_123AaZz09 key2=val2";
        assertEquals(expected, influxdbMetric1.getOpenTsdbTelnetFormatString(true));
        
        // no tags
        expected = "metricDb.metricPrefix.metricName.metricColumn 1436135662888 123.456 Format=InfluxDB";
        assertEquals(expected, influxdbMetric2.getOpenTsdbTelnetFormatString(true));
        
        // no tags, but manual setting of opentsdb tag
        expected = "metricDb.metricPrefix.metricName.metricColumn 1436135662888 123.456 Taco=Bell";
        assertEquals(expected, influxdbMetric2.getOpenTsdbTelnetFormatString(true, "Taco", "Bell"));
        
        // no tags, boolean value
        expected = "metricDb.metricPrefix.metricName.metricColumn 1436135662888 0 Format=InfluxDB";
        assertEquals(expected, influxdbMetric3.getOpenTsdbTelnetFormatString(true));
    }

    /**
     * Test of getOpenTsdbJsonFormatString method, of class InfluxdbStandardizedMetric.
     */
    @Test
    public void testGetOpenTsdbJsonFormatString() {
        
        // unsanitized
        String expected = "{\"metric\":\"metricDb..@-\\/#$%^_123AaZz09...metricPrefix..@-\\/#$%^_123AaZz09.metricName..@-\\/#$%^_123AaZz09.metricColumn..@-\\/#$%^_123AaZz09\",\"timestamp\":1436135662888,\"value\":123.456,\"tags\":{\"metricTagKey..@-\\/#$%^_123AaZz09\":\"metricTagValue..@-\\/#$%^_123AaZz09\",\"key2\":\"val2\"}}";
        System.out.println(StringEscapeUtils.unescapeJson(influxdbMetric1.getOpenTsdbJsonFormatString(false)));
        assertEquals(expected, StringEscapeUtils.unescapeJson(influxdbMetric1.getOpenTsdbJsonFormatString(false)));
        
        // sanitized
        expected = "{\"metric\":\"metricDb..-/_123AaZz09...metricPrefix..-/_123AaZz09.metricName..-/_123AaZz09.metricColumn..-/_123AaZz09\",\"timestamp\":1436135662888,\"value\":123.456,\"tags\":{\"metricTagKey..-/_123AaZz09\":\"metricTagValue..-/_123AaZz09\",\"key2\":\"val2\"}}";
        System.out.println(StringEscapeUtils.unescapeJson(influxdbMetric1.getOpenTsdbJsonFormatString(true)));
        assertEquals(expected, StringEscapeUtils.unescapeJson(influxdbMetric1.getOpenTsdbJsonFormatString(true)));    
        
        // no tags
        expected = "{\"metric\":\"metricDb.metricPrefix.metricName.metricColumn\",\"timestamp\":1436135662888,\"value\":123.456,\"tags\":{\"Format\":\"InfluxDB\"}}";
        System.out.println(StringEscapeUtils.unescapeJson(influxdbMetric2.getOpenTsdbJsonFormatString(true)));
        assertEquals(expected, StringEscapeUtils.unescapeJson(influxdbMetric2.getOpenTsdbJsonFormatString(true)));    
        
        // no tags, but manual setting of opentsdb tag
        expected = "{\"metric\":\"metricDb.metricPrefix.metricName.metricColumn\",\"timestamp\":1436135662888,\"value\":123.456,\"tags\":{\"Taco\":\"Bell\"}}";
        System.out.println(StringEscapeUtils.unescapeJson(influxdbMetric2.getOpenTsdbJsonFormatString(true, "Taco", "Bell")));
        assertEquals(expected, StringEscapeUtils.unescapeJson(influxdbMetric2.getOpenTsdbJsonFormatString(true, "Taco", "Bell")));    
        
        // no tags
        expected = "{\"metric\":\"metricDb.metricPrefix.metricName.metricColumn\",\"timestamp\":1436135662888,\"value\":0,\"tags\":{\"Format\":\"InfluxDB\"}}";
        System.out.println(StringEscapeUtils.unescapeJson(influxdbMetric2.getOpenTsdbJsonFormatString(true)));
        assertEquals(expected, StringEscapeUtils.unescapeJson(influxdbMetric3.getOpenTsdbJsonFormatString(true)));   
    }

    @Test
    public void testInfluxdbMetricCreateNoTimePrecisionSpecified() {
        List<InfluxdbMetric_v1> influxdbMetrics1 = InfluxdbMetric_v1.parseInfluxdbMetricJson("metricDb..@-\\/#$%^_123AaZz09", json1_, "user", "pass", null,
                null, "..metricPrefix..@-\\/#$%^_123AaZz09.", 1437135662888l);
        
        assertEquals(influxdbMetrics1.get(0).getInfluxdbStandardizedMetrics().get(0).getMetricTimestamp(), 1436135662888123l);
    }
    
}
