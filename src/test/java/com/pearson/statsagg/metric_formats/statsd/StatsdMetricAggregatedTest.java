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
package com.pearson.statsagg.metric_formats.statsd;

import java.math.BigDecimal;
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
public class StatsdMetricAggregatedTest {
    
    public StatsdMetricAggregatedTest() {
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
     * Test of getGraphiteFormatString method, of class StatsdMetricAggregator.
     */
    @Test
    public void testGetGraphiteFormatString() {
        long currentTime = System.currentTimeMillis();
        int currentTimeInSeconds = (int) (currentTime / 1000);
        
        StatsdMetricAggregated statsdMetricAggregated = new StatsdMetricAggregated("aggregated.counterMetric", 
                new BigDecimal("1.2222000000"), currentTime, StatsdMetricAggregated.COUNTER_TYPE);
        
        assertEquals(("aggregated.counterMetric 1.2222 " + currentTimeInSeconds), statsdMetricAggregated.getGraphiteFormatString());
    }

    /**
     * Test of getOpenTsdbTelnetFormatString method, of class StatsdMetricAggregator.
     */
    @Test
    public void testGetOpenTsdbTelnetFormatString() {
        long currentTime = System.currentTimeMillis();
        
        StatsdMetricAggregated statsdMetricAggregated = new StatsdMetricAggregated("aggregated.counterMetric", 
                new BigDecimal("1.2222000000"), currentTime, StatsdMetricAggregated.COUNTER_TYPE);
        
        assertEquals(("aggregated.counterMetric " + currentTime + " 1.2222 " + "Format=StatsD"), statsdMetricAggregated.getOpenTsdbTelnetFormatString());
    }
    
    /**
     * Test of getOpenTsdbJsonFormatString method, of class StatsdMetricAggregator.
     */
    @Test
    public void testGetOpenTsdbJsonFormatString() {
        long currentTime = System.currentTimeMillis();
        
        StatsdMetricAggregated statsdMetricAggregated = new StatsdMetricAggregated("aggregated.counterMetric", 
                new BigDecimal("1.2222000000"), currentTime, StatsdMetricAggregated.COUNTER_TYPE);
                
        assertEquals("{\"metric\":\"aggregated.counterMetric\",\"timestamp\":" + currentTime + ",\"value\":1.2222,\"tags\":{\"Format\":\"StatsD\"}}", 
                statsdMetricAggregated.getOpenTsdbJsonFormatString());
    }
    
    /**
     * Test of getInfluxdbV1JsonFormatString method, of class StatsdMetricAggregator.
     */
    @Test
    public void testGetInfluxdbV1JsonFormatString() {
        long currentTime = System.currentTimeMillis();
        
        StatsdMetricAggregated statsdMetricAggregated = new StatsdMetricAggregated("aggregated.counterMetric", 
                new BigDecimal("1.2222000000"), currentTime, StatsdMetricAggregated.COUNTER_TYPE);
                
        assertEquals("{\"name\":\"aggregated.counterMetric\",\"columns\":[\"value\",\"time\"],\"points\":[[1.2222," + currentTime + "]]}", statsdMetricAggregated.getInfluxdbV1JsonFormatString());
    }
    
}
