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
public class StatsdMetricTest {
    
    public StatsdMetricTest() {
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
     * Test of parseStatsdMetrics method, of class StatsdMetric.
     */
    @Test
    public void testParseStatsdMetrics_Counter() {
        long currentTime = System.currentTimeMillis();
        
        StatsdMetric statsdMetric1 = StatsdMetric.parseStatsdMetric("countMetric_1:100|c", currentTime);
        StatsdMetric statsdMetric2 = StatsdMetric.parseStatsdMetric("countMetric_2:-800.3|c|@.1\n", currentTime);
        StatsdMetric statsdMetric3 = StatsdMetric.parseStatsdMetric("countMetric_3:10000000000000|c|@1.11111\n", currentTime);

        assertEquals(statsdMetric1.getBucket(), "countMetric_1");
        assertEquals(0, statsdMetric1.getMetricValue().compareTo(new BigDecimal("100")));
        assertEquals(statsdMetric1.getMetricType(), "c");
        assertEquals(statsdMetric1.getMetricTypeKey(), StatsdMetric.COUNTER_TYPE);
        assertEquals(statsdMetric1.doesContainOperator(), false);
        assertEquals(statsdMetric1.getSampleRate(), null);
        assertEquals(statsdMetric1.getMetricReceivedTimestampInMilliseconds(), currentTime);
        assertEquals(statsdMetric1.getStatsdMetricFormatString(), "countMetric_1:100|c");
        
        assertEquals(statsdMetric2.getBucket(), "countMetric_2");
        assertEquals(0, statsdMetric2.getMetricValue().compareTo(new BigDecimal("-800.3")));
        assertEquals(statsdMetric2.getMetricType(), "c");
        assertEquals(statsdMetric2.getMetricTypeKey(), StatsdMetric.COUNTER_TYPE);
        assertEquals(statsdMetric2.doesContainOperator(), true);
        assertEquals(0, statsdMetric2.getSampleRate().compareTo(new BigDecimal("0.1")));
        assertEquals(statsdMetric2.getMetricReceivedTimestampInMilliseconds(), currentTime);
        assertEquals(statsdMetric2.getStatsdMetricFormatString(), "countMetric_2:-800.3|c|@0.1");

        assertEquals(statsdMetric3.getBucket(), "countMetric_3");
        assertEquals(0, statsdMetric3.getMetricValue().compareTo(new BigDecimal("10000000000000")));
        assertEquals(statsdMetric3.getMetricType(), "c");
        assertEquals(statsdMetric3.getMetricTypeKey(), StatsdMetric.COUNTER_TYPE);
        assertEquals(statsdMetric3.doesContainOperator(), false);
        assertEquals(0, statsdMetric3.getSampleRate().compareTo(new BigDecimal("1.11111")));
        assertEquals(statsdMetric3.getMetricReceivedTimestampInMilliseconds(), currentTime);
        assertEquals(statsdMetric3.getStatsdMetricFormatString(), "countMetric_3:10000000000000|c|@1.11111");
    }
    
    /**
     * Test of parseStatsdMetrics method, of class StatsdMetric.
     */
    @Test
    public void testParseStatsdMetrics_Timer() {
        long currentTime = System.currentTimeMillis();
        
        StatsdMetric statsdMetric1 = StatsdMetric.parseStatsdMetric("timerMetric_1:100|ms", currentTime);
        StatsdMetric statsdMetric2 = StatsdMetric.parseStatsdMetric("timerMetric_2:+800.3|ms|@.1\n", currentTime);
        StatsdMetric statsdMetric3 = StatsdMetric.parseStatsdMetric("timerMetric_3:10000000000000|ms|@1.11111\n", currentTime);

        assertEquals(statsdMetric1.getBucket(), "timerMetric_1");
        assertEquals(0, statsdMetric1.getMetricValue().compareTo(new BigDecimal("100")));
        assertEquals(statsdMetric1.getMetricType(), "ms");
        assertEquals(statsdMetric1.getMetricTypeKey(), StatsdMetric.TIMER_TYPE);
        assertEquals(statsdMetric1.doesContainOperator(), false);
        assertEquals(statsdMetric1.getSampleRate(), null);
        assertEquals(statsdMetric1.getMetricReceivedTimestampInMilliseconds(), currentTime);
        assertEquals(statsdMetric1.getStatsdMetricFormatString(), "timerMetric_1:100|ms");
        
        assertEquals(statsdMetric2.getBucket(), "timerMetric_2");
        assertEquals(0, statsdMetric2.getMetricValue().compareTo(new BigDecimal("800.3")));
        assertEquals(statsdMetric2.getMetricType(), "ms");
        assertEquals(statsdMetric2.getMetricTypeKey(), StatsdMetric.TIMER_TYPE);
        assertEquals(statsdMetric2.doesContainOperator(), true);
        assertEquals(0, statsdMetric2.getSampleRate().compareTo(new BigDecimal("0.1")));
        assertEquals(statsdMetric2.getMetricReceivedTimestampInMilliseconds(), currentTime);
        assertEquals(statsdMetric2.getStatsdMetricFormatString(), "timerMetric_2:800.3|ms|@0.1");

        assertEquals(statsdMetric3.getBucket(), "timerMetric_3");
        assertEquals(0, statsdMetric3.getMetricValue().compareTo(new BigDecimal("10000000000000")));
        assertEquals(statsdMetric3.getMetricType(), "ms");
        assertEquals(statsdMetric3.getMetricTypeKey(), StatsdMetric.TIMER_TYPE);
        assertEquals(statsdMetric3.doesContainOperator(), false);
        assertEquals(0, statsdMetric3.getSampleRate().compareTo(new BigDecimal("1.11111")));
        assertEquals(statsdMetric3.getMetricReceivedTimestampInMilliseconds(), currentTime);
        assertEquals(statsdMetric3.getStatsdMetricFormatString(), "timerMetric_3:10000000000000|ms|@1.11111");
    }
    
    /**
     * Test of parseStatsdMetrics method, of class StatsdMetric.
     */
    @Test
    public void testParseStatsdMetrics_Gauge() {
        long currentTime = System.currentTimeMillis();

        StatsdMetric statsdMetric1 = StatsdMetric.parseStatsdMetric("gaugeMetric_1:100|g", currentTime);
        StatsdMetric statsdMetric2 = StatsdMetric.parseStatsdMetric("gaugeMetric_2:+800.3|g\n", currentTime);
        StatsdMetric statsdMetric3 = StatsdMetric.parseStatsdMetric("gaugeMetric_3:-215.1|g\r\n", currentTime);
        
        assertEquals(statsdMetric1.getBucket(), "gaugeMetric_1");
        assertEquals(0, statsdMetric1.getMetricValue().compareTo(new BigDecimal("100")));
        assertEquals(statsdMetric1.getMetricType(), "g");
        assertEquals(statsdMetric1.getMetricTypeKey(), StatsdMetric.GAUGE_TYPE);
        assertEquals(statsdMetric1.doesContainOperator(), false);
        assertEquals(statsdMetric1.getSampleRate(), null);
        assertEquals(statsdMetric1.getMetricReceivedTimestampInMilliseconds(), currentTime);
        assertEquals(statsdMetric1.getStatsdMetricFormatString(), "gaugeMetric_1:100|g");

        assertEquals(statsdMetric2.getBucket(), "gaugeMetric_2");
        assertEquals(0, statsdMetric2.getMetricValue().compareTo(new BigDecimal("800.3")));
        assertEquals(statsdMetric2.getMetricType(), "g");
        assertEquals(statsdMetric2.getMetricTypeKey(), StatsdMetric.GAUGE_TYPE);
        assertEquals(statsdMetric2.doesContainOperator(), true);
        assertEquals(statsdMetric2.getSampleRate(), null);
        assertEquals(statsdMetric2.getMetricReceivedTimestampInMilliseconds(), currentTime);
        assertEquals(statsdMetric2.getStatsdMetricFormatString(), "gaugeMetric_2:+800.3|g");

        assertEquals(statsdMetric3.getBucket(), "gaugeMetric_3");
        assertEquals(0, statsdMetric3.getMetricValue().compareTo(new BigDecimal("-215.1")));
        assertEquals(statsdMetric3.getMetricType(), "g");
        assertEquals(statsdMetric3.getMetricTypeKey(), StatsdMetric.GAUGE_TYPE);
        assertEquals(statsdMetric3.doesContainOperator(), true);
        assertEquals(statsdMetric3.getSampleRate(), null);
        assertEquals(statsdMetric3.getMetricReceivedTimestampInMilliseconds(), currentTime);
        assertEquals(statsdMetric3.getStatsdMetricFormatString(), "gaugeMetric_3:-215.1|g");
    }
    
    /**
     * Test of parseStatsdMetrics method, of class StatsdMetric.
     */
    @Test
    public void testParseStatsdMetrics_Set() {
        long currentTime = System.currentTimeMillis();

        StatsdMetric statsdMetric1 = StatsdMetric.parseStatsdMetric("setMetric_1:100|s", currentTime);
        StatsdMetric statsdMetric2 = StatsdMetric.parseStatsdMetric("setMetric_2:-800.3|s\n", currentTime);
        StatsdMetric statsdMetric3 = StatsdMetric.parseStatsdMetric("setMetric_3:10000000000000|s\r\n", currentTime);
        
        assertEquals(statsdMetric1.getBucket(), "setMetric_1");
        assertEquals(0, statsdMetric1.getMetricValue().compareTo(new BigDecimal("100")));
        assertEquals(statsdMetric1.getMetricType(), "s");
        assertEquals(statsdMetric1.getMetricTypeKey(), StatsdMetric.SET_TYPE);
        assertEquals(statsdMetric1.doesContainOperator(), false);
        assertEquals(statsdMetric1.getSampleRate(), null);
        assertEquals(statsdMetric1.getMetricReceivedTimestampInMilliseconds(), currentTime);
        assertEquals(statsdMetric1.getStatsdMetricFormatString(), "setMetric_1:100|s");

        assertEquals(statsdMetric2.getBucket(), "setMetric_2");
        assertEquals(0, statsdMetric2.getMetricValue().compareTo(new BigDecimal("-800.3")));
        assertEquals(statsdMetric2.getMetricType(), "s");
        assertEquals(statsdMetric2.getMetricTypeKey(), StatsdMetric.SET_TYPE);
        assertEquals(statsdMetric2.doesContainOperator(), true);
        assertEquals(statsdMetric2.getSampleRate(), null);
        assertEquals(statsdMetric2.getMetricReceivedTimestampInMilliseconds(), currentTime);
        assertEquals(statsdMetric2.getStatsdMetricFormatString(), "setMetric_2:-800.3|s");

        assertEquals(statsdMetric3.getBucket(), "setMetric_3");
        assertEquals(0, statsdMetric3.getMetricValue().compareTo(new BigDecimal("10000000000000")));
        assertEquals(statsdMetric3.getMetricType(), "s");
        assertEquals(statsdMetric3.getMetricTypeKey(), StatsdMetric.SET_TYPE);
        assertEquals(statsdMetric3.doesContainOperator(), false);
        assertEquals(statsdMetric3.getSampleRate(), null);
        assertEquals(statsdMetric3.getMetricReceivedTimestampInMilliseconds(), currentTime);
        assertEquals(statsdMetric3.getStatsdMetricFormatString(), "setMetric_3:10000000000000|s");
    }
    
}
