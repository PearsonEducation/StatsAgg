/*
 * Copyright 2015 Jeffrey Schmidt.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pearson.statsagg.metric_formats.influxdb;

import java.util.List;
import org.apache.commons.lang3.StringEscapeUtils;
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
public class InfluxdbStandardizedMetric_v1Test {
    
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
    
    private InfluxdbStandardizedMetric_v1 influxdbMetric1;
    private InfluxdbStandardizedMetric_v1 influxdbMetric2;
    private InfluxdbStandardizedMetric_v1 influxdbMetric3;

    public InfluxdbStandardizedMetric_v1Test() {
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
     * Test of getGraphiteFormatString method, of class InfluxdbStandardizedMetric_v1.
     */
    @Test
    public void testGetGraphiteFormatString() {
        
        // unsanitized, without substitution
        String expected = "..metricPrefix..@-\\/#$%^_123AaZz09.metricDb..@-\\/#$%^_123AaZz09.metricName..@-\\/#$%^_123AaZz09.metricColumn..@-\\/#$%^_123AaZz09 123.456 1436135662";
        assertEquals(expected, influxdbMetric1.getGraphiteFormatString(false, false));
        
        // sanitized, without substitution
        expected = ".metricPrefix.@-\\/#$%^_123AaZz09.metricDb.@-\\/#$%^_123AaZz09.metricName.@-\\/#$%^_123AaZz09.metricColumn.@-\\/#$%^_123AaZz09 123.456 1436135662";
        assertEquals(expected, influxdbMetric1.getGraphiteFormatString(true, false));
        
        // unsanitized, with substitution
        expected = "..metricPrefix..@-||#$Pct^_123AaZz09.metricDb..@-||#$Pct^_123AaZz09.metricName..@-||#$Pct^_123AaZz09.metricColumn..@-||#$Pct^_123AaZz09 123.456 1436135662";
        assertEquals(expected, influxdbMetric1.getGraphiteFormatString(false, true));
        
        // sanitized, with substitution
        expected = ".metricPrefix.@-||#$Pct^_123AaZz09.metricDb.@-||#$Pct^_123AaZz09.metricName.@-||#$Pct^_123AaZz09.metricColumn.@-||#$Pct^_123AaZz09 123.456 1436135662";
        System.out.println(influxdbMetric1.getGraphiteFormatString(true, true));
        assertEquals(expected, influxdbMetric1.getGraphiteFormatString(true, true));
    }

    /**
     * Test of getOpenTsdbTelnetFormatString method, of class InfluxdbStandardizedMetric_v1.
     */
    @Test
    public void testGetOpenTsdbTelnetFormatString() {
        // unsanitized
        String expected = "..metricPrefix..@-\\/#$%^_123AaZz09.metricDb..@-\\/#$%^_123AaZz09.metricName..@-\\/#$%^_123AaZz09.metricColumn..@-\\/#$%^_123AaZz09 1436135662888 123.456 metricTagKey..@-\\/#$%^_123AaZz09=metricTagValue..@-\\/#$%^_123AaZz09 key2=val2";
        assertEquals(expected, influxdbMetric1.getOpenTsdbTelnetFormatString(false));
        
        // sanitized
        expected = "..metricPrefix..-/_123AaZz09.metricDb..-/_123AaZz09.metricName..-/_123AaZz09.metricColumn..-/_123AaZz09 1436135662888 123.456 metricTagKey..-/_123AaZz09=metricTagValue..-/_123AaZz09 key2=val2";
        assertEquals(expected, influxdbMetric1.getOpenTsdbTelnetFormatString(true));
        
        // no tags
        expected = "metricPrefix.metricDb.metricName.metricColumn 1436135662888 123.456 Format=InfluxDB";
        assertEquals(expected, influxdbMetric2.getOpenTsdbTelnetFormatString(true));
        
        // no tags, boolean value
        expected = "metricPrefix.metricDb.metricName.metricColumn 1436135662888 0 Format=InfluxDB";
        assertEquals(expected, influxdbMetric3.getOpenTsdbTelnetFormatString(true));
    }

    /**
     * Test of getOpenTsdbJsonFormatString method, of class InfluxdbStandardizedMetric_v1.
     */
    @Test
    public void testGetOpenTsdbJsonFormatString() {
        
        // unsanitized
        String expected = "{\"metric\":\"..metricPrefix..@-\\/#$%^_123AaZz09.metricDb..@-\\/#$%^_123AaZz09.metricName..@-\\/#$%^_123AaZz09.metricColumn..@-\\/#$%^_123AaZz09\",\"timestamp\":1436135662888,\"value\":123.456,\"tags\":{\"metricTagKey..@-\\/#$%^_123AaZz09\":\"metricTagValue..@-\\/#$%^_123AaZz09\",\"key2\":\"val2\"}}";
        System.out.println(StringEscapeUtils.unescapeJson(influxdbMetric1.getOpenTsdbJsonFormatString(false)));
        assertEquals(expected, StringEscapeUtils.unescapeJson(influxdbMetric1.getOpenTsdbJsonFormatString(false)));
        
        // sanitized
        expected = "{\"metric\":\"..metricPrefix..-/_123AaZz09.metricDb..-/_123AaZz09.metricName..-/_123AaZz09.metricColumn..-/_123AaZz09\",\"timestamp\":1436135662888,\"value\":123.456,\"tags\":{\"metricTagKey..-/_123AaZz09\":\"metricTagValue..-/_123AaZz09\",\"key2\":\"val2\"}}";
        System.out.println(StringEscapeUtils.unescapeJson(influxdbMetric1.getOpenTsdbJsonFormatString(true)));
        assertEquals(expected, StringEscapeUtils.unescapeJson(influxdbMetric1.getOpenTsdbJsonFormatString(true)));    
        
        // no tags
        expected = "{\"metric\":\"metricPrefix.metricDb.metricName.metricColumn\",\"timestamp\":1436135662888,\"value\":123.456,\"tags\":{\"Format\":\"InfluxDB\"}}";
        System.out.println(StringEscapeUtils.unescapeJson(influxdbMetric2.getOpenTsdbJsonFormatString(true)));
        assertEquals(expected, StringEscapeUtils.unescapeJson(influxdbMetric2.getOpenTsdbJsonFormatString(true)));    
        
        // no tags
        expected = "{\"metric\":\"metricPrefix.metricDb.metricName.metricColumn\",\"timestamp\":1436135662888,\"value\":0,\"tags\":{\"Format\":\"InfluxDB\"}}";
        System.out.println(StringEscapeUtils.unescapeJson(influxdbMetric2.getOpenTsdbJsonFormatString(true)));
        assertEquals(expected, StringEscapeUtils.unescapeJson(influxdbMetric3.getOpenTsdbJsonFormatString(true)));   
    }

}
