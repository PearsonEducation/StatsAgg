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
package com.pearson.statsagg.metric_aggregation.statsd;

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
public class StatsdMetricRawTest {
    
    public StatsdMetricRawTest() {
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
     * Test of parseStatsdMetricsRaw method, of class StatsdMetricRaw.
     */
    @Test
    public void testParseStatsdMetricsRaw_Counter() {
        long currentTime = System.currentTimeMillis();
        
        StatsdMetricRaw statsdMetricRaw1 = StatsdMetricRaw.parseStatsdMetricRaw("countMetric_1:100|c", currentTime);
        StatsdMetricRaw statsdMetricRaw2 = StatsdMetricRaw.parseStatsdMetricRaw("countMetric_2:-800.3|c|@.1\n", currentTime);
        StatsdMetricRaw statsdMetricRaw3 = StatsdMetricRaw.parseStatsdMetricRaw("countMetric_3:10000000000000|c|@1.11111\n", currentTime);

        assertEquals(statsdMetricRaw1.getBucket(), "countMetric_1");
        assertEquals(0, statsdMetricRaw1.getMetricValue().compareTo(new BigDecimal("100")));
        assertEquals(statsdMetricRaw1.getMetricType(), "c");
        assertEquals(statsdMetricRaw1.getMetricTypeKey(), StatsdMetricRaw.COUNTER_TYPE);
        assertEquals(statsdMetricRaw1.doesContainOperator(), false);
        assertEquals(statsdMetricRaw1.getSampleRate(), null);
        assertEquals(statsdMetricRaw1.getMetricReceivedTimestampInMilliseconds(), currentTime);
        assertEquals(statsdMetricRaw1.getStatsdMetricFormatString(), "countMetric_1:100|c");
        
        assertEquals(statsdMetricRaw2.getBucket(), "countMetric_2");
        assertEquals(0, statsdMetricRaw2.getMetricValue().compareTo(new BigDecimal("-800.3")));
        assertEquals(statsdMetricRaw2.getMetricType(), "c");
        assertEquals(statsdMetricRaw2.getMetricTypeKey(), StatsdMetricRaw.COUNTER_TYPE);
        assertEquals(statsdMetricRaw2.doesContainOperator(), true);
        assertEquals(0, statsdMetricRaw2.getSampleRate().compareTo(new BigDecimal("0.1")));
        assertEquals(statsdMetricRaw2.getMetricReceivedTimestampInMilliseconds(), currentTime);
        assertEquals(statsdMetricRaw2.getStatsdMetricFormatString(), "countMetric_2:-800.3|c|@0.1");

        assertEquals(statsdMetricRaw3.getBucket(), "countMetric_3");
        assertEquals(0, statsdMetricRaw3.getMetricValue().compareTo(new BigDecimal("10000000000000")));
        assertEquals(statsdMetricRaw3.getMetricType(), "c");
        assertEquals(statsdMetricRaw3.getMetricTypeKey(), StatsdMetricRaw.COUNTER_TYPE);
        assertEquals(statsdMetricRaw3.doesContainOperator(), false);
        assertEquals(0, statsdMetricRaw3.getSampleRate().compareTo(new BigDecimal("1.11111")));
        assertEquals(statsdMetricRaw3.getMetricReceivedTimestampInMilliseconds(), currentTime);
        assertEquals(statsdMetricRaw3.getStatsdMetricFormatString(), "countMetric_3:10000000000000|c|@1.11111");
    }
    
    /**
     * Test of parseStatsdMetricsRaw method, of class StatsdMetricRaw.
     */
    @Test
    public void testParseStatsdMetricsRaw_Timer() {
        long currentTime = System.currentTimeMillis();
        
        StatsdMetricRaw statsdMetricRaw1 = StatsdMetricRaw.parseStatsdMetricRaw("timerMetric_1:100|ms", currentTime);
        StatsdMetricRaw statsdMetricRaw2 = StatsdMetricRaw.parseStatsdMetricRaw("timerMetric_2:-800.3|ms|@.1\n", currentTime);
        StatsdMetricRaw statsdMetricRaw3 = StatsdMetricRaw.parseStatsdMetricRaw("timerMetric_3:10000000000000|ms|@1.11111\n", currentTime);

        assertEquals(statsdMetricRaw1.getBucket(), "timerMetric_1");
        assertEquals(0, statsdMetricRaw1.getMetricValue().compareTo(new BigDecimal("100")));
        assertEquals(statsdMetricRaw1.getMetricType(), "ms");
        assertEquals(statsdMetricRaw1.getMetricTypeKey(), StatsdMetricRaw.TIMER_TYPE);
        assertEquals(statsdMetricRaw1.doesContainOperator(), false);
        assertEquals(statsdMetricRaw1.getSampleRate(), null);
        assertEquals(statsdMetricRaw1.getMetricReceivedTimestampInMilliseconds(), currentTime);
        assertEquals(statsdMetricRaw1.getStatsdMetricFormatString(), "timerMetric_1:100|ms");
        
        assertEquals(statsdMetricRaw2.getBucket(), "timerMetric_2");
        assertEquals(0, statsdMetricRaw2.getMetricValue().compareTo(new BigDecimal("-800.3")));
        assertEquals(statsdMetricRaw2.getMetricType(), "ms");
        assertEquals(statsdMetricRaw2.getMetricTypeKey(), StatsdMetricRaw.TIMER_TYPE);
        assertEquals(statsdMetricRaw2.doesContainOperator(), true);
        assertEquals(0, statsdMetricRaw2.getSampleRate().compareTo(new BigDecimal("0.1")));
        assertEquals(statsdMetricRaw2.getMetricReceivedTimestampInMilliseconds(), currentTime);
        assertEquals(statsdMetricRaw2.getStatsdMetricFormatString(), "timerMetric_2:-800.3|ms|@0.1");

        assertEquals(statsdMetricRaw3.getBucket(), "timerMetric_3");
        assertEquals(0, statsdMetricRaw3.getMetricValue().compareTo(new BigDecimal("10000000000000")));
        assertEquals(statsdMetricRaw3.getMetricType(), "ms");
        assertEquals(statsdMetricRaw3.getMetricTypeKey(), StatsdMetricRaw.TIMER_TYPE);
        assertEquals(statsdMetricRaw3.doesContainOperator(), false);
        assertEquals(0, statsdMetricRaw3.getSampleRate().compareTo(new BigDecimal("1.11111")));
        assertEquals(statsdMetricRaw3.getMetricReceivedTimestampInMilliseconds(), currentTime);
        assertEquals(statsdMetricRaw3.getStatsdMetricFormatString(), "timerMetric_3:10000000000000|ms|@1.11111");
    }
    
    /**
     * Test of parseStatsdMetricsRaw method, of class StatsdMetricRaw.
     */
    @Test
    public void testParseStatsdMetricsRaw_Gauge() {
        long currentTime = System.currentTimeMillis();

        StatsdMetricRaw statsdMetricRaw1 = StatsdMetricRaw.parseStatsdMetricRaw("gaugeMetric_1:100|g", currentTime);
        StatsdMetricRaw statsdMetricRaw2 = StatsdMetricRaw.parseStatsdMetricRaw("gaugeMetric_2:+800.3|g\n", currentTime);
        StatsdMetricRaw statsdMetricRaw3 = StatsdMetricRaw.parseStatsdMetricRaw("gaugeMetric_3:-215.1|g\r\n", currentTime);
        
        assertEquals(statsdMetricRaw1.getBucket(), "gaugeMetric_1");
        assertEquals(0, statsdMetricRaw1.getMetricValue().compareTo(new BigDecimal("100")));
        assertEquals(statsdMetricRaw1.getMetricType(), "g");
        assertEquals(statsdMetricRaw1.getMetricTypeKey(), StatsdMetricRaw.GAUGE_TYPE);
        assertEquals(statsdMetricRaw1.doesContainOperator(), false);
        assertEquals(statsdMetricRaw1.getSampleRate(), null);
        assertEquals(statsdMetricRaw1.getMetricReceivedTimestampInMilliseconds(), currentTime);
        assertEquals(statsdMetricRaw1.getStatsdMetricFormatString(), "gaugeMetric_1:100|g");

        assertEquals(statsdMetricRaw2.getBucket(), "gaugeMetric_2");
        assertEquals(0, statsdMetricRaw2.getMetricValue().compareTo(new BigDecimal("800.3")));
        assertEquals(statsdMetricRaw2.getMetricType(), "g");
        assertEquals(statsdMetricRaw2.getMetricTypeKey(), StatsdMetricRaw.GAUGE_TYPE);
        assertEquals(statsdMetricRaw2.doesContainOperator(), true);
        assertEquals(statsdMetricRaw2.getSampleRate(), null);
        assertEquals(statsdMetricRaw2.getMetricReceivedTimestampInMilliseconds(), currentTime);
        assertEquals(statsdMetricRaw2.getStatsdMetricFormatString(), "gaugeMetric_2:+800.3|g");

        assertEquals(statsdMetricRaw3.getBucket(), "gaugeMetric_3");
        assertEquals(0, statsdMetricRaw3.getMetricValue().compareTo(new BigDecimal("-215.1")));
        assertEquals(statsdMetricRaw3.getMetricType(), "g");
        assertEquals(statsdMetricRaw3.getMetricTypeKey(), StatsdMetricRaw.GAUGE_TYPE);
        assertEquals(statsdMetricRaw3.doesContainOperator(), true);
        assertEquals(statsdMetricRaw3.getSampleRate(), null);
        assertEquals(statsdMetricRaw3.getMetricReceivedTimestampInMilliseconds(), currentTime);
        assertEquals(statsdMetricRaw3.getStatsdMetricFormatString(), "gaugeMetric_3:-215.1|g");
    }
    
    /**
     * Test of parseStatsdMetricsRaw method, of class StatsdMetricRaw.
     */
    @Test
    public void testParseStatsdMetricsRaw_Set() {
        long currentTime = System.currentTimeMillis();

        StatsdMetricRaw statsdMetricRaw1 = StatsdMetricRaw.parseStatsdMetricRaw("setMetric_1:100|s", currentTime);
        StatsdMetricRaw statsdMetricRaw2 = StatsdMetricRaw.parseStatsdMetricRaw("setMetric_2:-800.3|s\n", currentTime);
        StatsdMetricRaw statsdMetricRaw3 = StatsdMetricRaw.parseStatsdMetricRaw("setMetric_3:10000000000000|s\r\n", currentTime);
        
        assertEquals(statsdMetricRaw1.getBucket(), "setMetric_1");
        assertEquals(0, statsdMetricRaw1.getMetricValue().compareTo(new BigDecimal("100")));
        assertEquals(statsdMetricRaw1.getMetricType(), "s");
        assertEquals(statsdMetricRaw1.getMetricTypeKey(), StatsdMetricRaw.SET_TYPE);
        assertEquals(statsdMetricRaw1.doesContainOperator(), false);
        assertEquals(statsdMetricRaw1.getSampleRate(), null);
        assertEquals(statsdMetricRaw1.getMetricReceivedTimestampInMilliseconds(), currentTime);
        assertEquals(statsdMetricRaw1.getStatsdMetricFormatString(), "setMetric_1:100|s");

        assertEquals(statsdMetricRaw2.getBucket(), "setMetric_2");
        assertEquals(0, statsdMetricRaw2.getMetricValue().compareTo(new BigDecimal("-800.3")));
        assertEquals(statsdMetricRaw2.getMetricType(), "s");
        assertEquals(statsdMetricRaw2.getMetricTypeKey(), StatsdMetricRaw.SET_TYPE);
        assertEquals(statsdMetricRaw2.doesContainOperator(), true);
        assertEquals(statsdMetricRaw2.getSampleRate(), null);
        assertEquals(statsdMetricRaw2.getMetricReceivedTimestampInMilliseconds(), currentTime);
        assertEquals(statsdMetricRaw2.getStatsdMetricFormatString(), "setMetric_2:-800.3|s");

        assertEquals(statsdMetricRaw3.getBucket(), "setMetric_3");
        assertEquals(0, statsdMetricRaw3.getMetricValue().compareTo(new BigDecimal("10000000000000")));
        assertEquals(statsdMetricRaw3.getMetricType(), "s");
        assertEquals(statsdMetricRaw3.getMetricTypeKey(), StatsdMetricRaw.SET_TYPE);
        assertEquals(statsdMetricRaw3.doesContainOperator(), false);
        assertEquals(statsdMetricRaw3.getSampleRate(), null);
        assertEquals(statsdMetricRaw3.getMetricReceivedTimestampInMilliseconds(), currentTime);
        assertEquals(statsdMetricRaw3.getStatsdMetricFormatString(), "setMetric_3:10000000000000|s");
    }
    
}
