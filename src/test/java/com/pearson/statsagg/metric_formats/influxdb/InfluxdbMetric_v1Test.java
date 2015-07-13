package com.pearson.statsagg.metric_formats.influxdb;

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jeffrey Schmidt
 */
public class InfluxdbMetric_v1Test {
    
    private String json_;
    
    public InfluxdbMetric_v1Test() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
        json_ = "" +
                "[\n" +
                "  {\n" +
                "    \"name\" : \"metric_name_1\",\n" +
                "    \"columns\" : [\"time\", \"sequence_number\", \"column1\", \"column2\", \"column3\", \"column4\"],\n" +
                "    \"points\" : [\n" +
                "      [999991, 1, 1, 2, \"meta  1\", \"meta  2\"],\n" +
                "      [999992, 2, 11.11, 22.22, \"meta  11\", \"meta  22\"],\n" +
                "      [999993, 3, 111.111, 222.222, \"meta  111\", \"meta  222\"],\n" +
                "      [999994, 3, \"111.11\", \"222.22\", \"meta  1111\", \"meta  2222\"]\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"metric_name_2\",\n" +
                "    \"columns\": [\"column1\", \"column2\"],\n" +
                "    \"points\": [\n" +
                "      [\"meta\", false],\n" +
                "      [1234, \"   \"],\n" +
                "      [true, 123.456]\n" +
                "    ]\n" +
                "  }\n" +
                "]";
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of parseInfluxdbMetricJson method, of class InfluxdbMetric_v1.
     */
    @Test
    public void testParseInfluxdbMetricJson() {

        long currentTimeInMs = System.currentTimeMillis();
        List<InfluxdbMetric_v1> influxdbMetrics = InfluxdbMetric_v1.parseInfluxdbMetricJson("statsagg_db", json_, "user", "pass", null, 
                "ms", "global.local.", currentTimeInMs);
        
        String influxdbJson = InfluxdbMetric_v1.getInfluxdbJson(influxdbMetrics);
        String expectedResult = "[{\"name\":\"global.local.metric_name_1\",\"columns\":[\"time\",\"sequence_number\",\"column1\",\"column2\",\"column3\",\"column4\"],\"points\":[[999991,1,1,2,\"meta  1\",\"meta  2\"],[999992,2,11.11,22.22,\"meta  11\",\"meta  22\"],[999993,3,111.111,222.222,\"meta  111\",\"meta  222\"],[999994,3,\"111.11\",\"222.22\",\"meta  1111\",\"meta  2222\"]]},{\"name\":\"global.local.metric_name_2\",\"columns\":[\"column1\",\"column2\"],\"points\":[[\"meta\",false],[1234,\"   \"],[true,123.456]]}]";
        System.out.println(influxdbJson);
        assertEquals(influxdbJson, expectedResult);
        
        InfluxdbStandardizedMetric influxdbStandardizedMetric_v1 = influxdbMetrics.get(0).getInfluxdbStandardizedMetrics().get(0);
        assertEquals(influxdbStandardizedMetric_v1.getMetricKey(), "statsagg_db : global.local.metric_name_1 : column1 : \"column3\"=\"meta  1\" \"column4\"=\"meta  2\"");
        assertEquals(influxdbStandardizedMetric_v1.getMetricReceivedTimestampInMilliseconds(), currentTimeInMs);
        assertEquals(influxdbStandardizedMetric_v1.getMetricTimestampPrecision(), Common.TIMESTAMP_PRECISION_MILLISECONDS);
        assertEquals(influxdbStandardizedMetric_v1.getMetricTimestamp(), 999991);
        assertEquals(influxdbStandardizedMetric_v1.getMetricTimestampInSeconds(), 999);
        assertEquals(influxdbStandardizedMetric_v1.getMetricPrefix(), "global.local.");
        assertEquals(influxdbStandardizedMetric_v1.getMetricName(), "metric_name_1");
        assertEquals(influxdbStandardizedMetric_v1.getMetricDatabase(), "statsagg_db");
        assertEquals(influxdbStandardizedMetric_v1.getMetricValueBigDecimal().stripTrailingZeros().toPlainString(), "1");

        System.out.println(influxdbStandardizedMetric_v1.getMetricKey());
        
        
    }

}
