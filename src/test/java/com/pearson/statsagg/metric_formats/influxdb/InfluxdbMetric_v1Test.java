package com.pearson.statsagg.metric_formats.influxdb;

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
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
                "    \"columns\" : [\"time\", \"time_precision\", \"sequence_number\", \"column1\", \"column2\", \"column3\", \"column4\"],\n" +
                "    \"points\" : [\n" +
                "      [999991, \"s\", 1, 1, 2, \"meta  1\", \"meta  2\"],\n" +
                "      [999992, \"ms\", 2, 11.11, 22.22, \"meta  11\", \"meta  22\"],\n" +
                "      [999993, \"u\", 3, 111.111, 222.222, \"meta  222\", \"meta  222\"],\n" +
                "      [999994, \"lol\", \"3\", \"111.11\", \"222.22\", \"meta  1111\", \"meta  2222\"]\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"metric_name_2\",\n" +
                "    \"columns\": [\"column1\", \"column2\"],\n" +
                "    \"points\": [\n" +
                "      [\"meta\"],\n" +
                "      [1234],\n" +
                "      [true]\n" +
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
                "ms", "global-local-", "global.local.", currentTimeInMs);
        
        String influxdbJson = InfluxdbMetric_v1.getInfluxdbJson(influxdbMetrics);
        System.out.println(influxdbJson);
    }

}
