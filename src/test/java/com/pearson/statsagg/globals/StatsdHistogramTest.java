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
package com.pearson.statsagg.globals;

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
public class StatsdHistogramTest {
    
    public StatsdHistogramTest() {
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
     * Test of getStatsdHistogramConfigurations method, of class StatsdHistogramConfiguration.
     */
    @Test
    public void testGetStatsdHistogramConfigurations() {
        // covers multiple configs, emptry bin, empty metric, bin sorting, bin inf in different cases, bin inf duplicated, 
        // negative bin values, duplicated bin numeric values, bin leading 0s, bin trailing 0s
        String unparsedStatsdHistogramConfigurations = "[{metric:'foo',bins:[]},{metric:'',bins:[-20,100,10,1,0,'inf',00001,.1,'Inf',010,0.1000000]},{metric:'foo',bins:['INF']}]" ;
        List<StatsdHistogramConfiguration> statsdHistogramConfigurations = StatsdHistogramConfiguration.getStatsdHistogramConfigurations(unparsedStatsdHistogramConfigurations);
        
        assertEquals(3, statsdHistogramConfigurations.size());
        
        assertEquals("{metric:'foo',bins:[]}", statsdHistogramConfigurations.get(0).toString());
        assertEquals(false, statsdHistogramConfigurations.get(0).isInfDetected());

        assertEquals("{metric:'',bins:[0.1,1,10,100,'inf']}", statsdHistogramConfigurations.get(1).toString());
        assertEquals(true, statsdHistogramConfigurations.get(1).isInfDetected());
        
        assertEquals("{metric:'foo',bins:['inf']}", statsdHistogramConfigurations.get(2).toString());
        assertEquals(true, statsdHistogramConfigurations.get(2).isInfDetected());
    }
    
}
