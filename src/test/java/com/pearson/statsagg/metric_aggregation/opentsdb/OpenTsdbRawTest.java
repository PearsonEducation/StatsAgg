package com.pearson.statsagg.metric_aggregation.opentsdb;

import java.math.BigDecimal;
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
public class OpenTsdbRawTest {
    
    public OpenTsdbRawTest() {
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
     * Test of parseOpenTsdbMetricRaw method, of class OpenTsdbMetricRaw.
     */
    @Test
    public void testParseOpenTsdbRaw() {
        //String unparsedMetric = "sys.cpu.user 1356998400 42.5 host=webserver01 cpu=0";
        String unparsedMetric = "tcollector.reader.lines_collected 1424566500 1203  host=uschmje-VirtualBox";
        
        OpenTsdbMetricRaw expResult = null;
        OpenTsdbMetricRaw result = OpenTsdbMetricRaw.parseOpenTsdbMetricRaw(unparsedMetric, 1356998400999L);
        
        //assertEquals(expResult, result);
        
    }

}
