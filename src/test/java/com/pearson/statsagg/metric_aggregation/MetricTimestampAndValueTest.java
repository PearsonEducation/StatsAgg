package com.pearson.statsagg.metric_aggregation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import com.pearson.statsagg.globals.GlobalVariables;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Jeffrey Schmidt
 */
public class MetricTimestampAndValueTest {
    
    private static final AtomicLong hashKeyGen_ = GlobalVariables.aggregatedMetricHashKeyGenerator;
    private static final List<MetricTimestampAndValue> metricTimestampsAndValues_ = new ArrayList<>();
    
    public MetricTimestampAndValueTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        metricTimestampsAndValues_.add(new MetricTimestampAndValue((long) 400, new BigDecimal("73"), hashKeyGen_.incrementAndGet()));
        metricTimestampsAndValues_.add(new MetricTimestampAndValue((long) 500, new BigDecimal("74"), hashKeyGen_.incrementAndGet())); 
        metricTimestampsAndValues_.add(new MetricTimestampAndValue((long) 1000, new BigDecimal("78"), hashKeyGen_.incrementAndGet()));
        metricTimestampsAndValues_.add(new MetricTimestampAndValue((long) 1100, new BigDecimal("79"), hashKeyGen_.incrementAndGet())); 
        metricTimestampsAndValues_.add(new MetricTimestampAndValue((long) 300, new BigDecimal("72"), hashKeyGen_.incrementAndGet())); 
        metricTimestampsAndValues_.add(new MetricTimestampAndValue((long) 1200, new BigDecimal("80.1"), hashKeyGen_.incrementAndGet()));    
        metricTimestampsAndValues_.add(new MetricTimestampAndValue((long) 700, new BigDecimal("75"), hashKeyGen_.incrementAndGet())); 
        metricTimestampsAndValues_.add(new MetricTimestampAndValue((long) 800, new BigDecimal("76"), hashKeyGen_.incrementAndGet()));
        metricTimestampsAndValues_.add(new MetricTimestampAndValue((long) 900, new BigDecimal("77"), hashKeyGen_.incrementAndGet())); 
        metricTimestampsAndValues_.add(new MetricTimestampAndValue((long) 100, new BigDecimal("69.9"), hashKeyGen_.incrementAndGet())); 
        metricTimestampsAndValues_.add(new MetricTimestampAndValue((long) 200, new BigDecimal("71"), hashKeyGen_.incrementAndGet()));
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

    @Test
    public void testCompare() {

        Set<MetricTimestampAndValue> sortedMetricTimestampsAndValuesSet = new TreeSet<>(MetricTimestampAndValue.COMPARE_BY_TIMESTAMP);
        
        for (MetricTimestampAndValue metricTimestampAndValue : metricTimestampsAndValues_) {
            sortedMetricTimestampsAndValuesSet.add(metricTimestampAndValue);
        }
     
        List<MetricTimestampAndValue> sortedMetricTimestampsAndValuesList = new ArrayList<>(sortedMetricTimestampsAndValuesSet);
        
        for (int i = 0; i < sortedMetricTimestampsAndValuesList.size(); i++) {
            if (i > 0) {
                assertTrue(sortedMetricTimestampsAndValuesList.get(i).getTimestamp() > sortedMetricTimestampsAndValuesList.get(i - 1).getTimestamp());
            }
        }
        
    }

}
