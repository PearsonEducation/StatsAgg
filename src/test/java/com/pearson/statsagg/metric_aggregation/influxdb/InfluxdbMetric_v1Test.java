package com.pearson.statsagg.metric_aggregation.influxdb;

import java.util.ArrayList;
import java.util.List;
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
public class InfluxdbMetric_v1Test {
    
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
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of parseInfluxdbMetricJson method, of class InfluxdbMetric_v1.
     */
    @Test
    public void testParseInfluxdbMetricJson() {

        String json = ""
                + "[\n"
                + "  {\n"
                + "    \"name\" : \"metric_1\",\n"
                + "    \"columns\" : [\"column  1\", \"column  2\", \"time\", \"sequence_number\", \"host\", \"mount\", \"time_precision\"],\n"
                + "    \"points\" : [\n"
                + "      [1, 2, 909801, 1, \"serverA\", \"/mntA\", \"s\"],\n"
                + "      [11.1, 22.2, 909802, 2, \"serverB\", \"/mntB\", \"ms\"],\n"
                + "      [111.1, 222.2, 909803, 3, \"serverC\", \"/mntC\", \"u\"],\n"
                + "      [\"11.111\", \"22.222\", \"4\", \"909804\", \"serverD\", \"/mntD\", \"u\"]\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"name\" : \"metric_2\",\n"
                + "    \"columns\" : [\"column  3\"],\n"
                + "    \"points\" : [\n"
                + "      [11.11],\n"
                + "      [\"22.22\"]\n"
                + "    ]\n"
                + "  }\n"
                + "]";
        
        
        
    }

}
