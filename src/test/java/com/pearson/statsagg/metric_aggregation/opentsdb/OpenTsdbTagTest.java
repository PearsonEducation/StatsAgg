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
package com.pearson.statsagg.metric_aggregation.opentsdb;

import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Jeffrey Schmidt
 */
public class OpenTsdbTagTest {
    
    public OpenTsdbTagTest() {
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
     * Test of parseTags method, of class OpenTsdbTag.
     */
    @Test
    public void testParseTags_String_1() {
        String unparsedTags = "tag1=5 tag2=2asdf tag3";
        ArrayList<OpenTsdbTag> result = OpenTsdbTag.parseTags(unparsedTags);

        ArrayList<OpenTsdbTag> expResult = new ArrayList<>();
        expResult.add(new OpenTsdbTag("tag1=5")); 
        expResult.add(new OpenTsdbTag("tag2=2asdf")); 
        
        int matchCount = 0;
        for (OpenTsdbTag tag : expResult) {
            if (tag.getTag().equals("tag1=5")) matchCount++;
            if (tag.getTag().equals("tag2=2asdf")) matchCount++;
        }
        
        assertEquals(result.size(), 2);
        assertEquals(matchCount, 2);
    }

    /**
     * Test of parseTags method, of class OpenTsdbTag.
     */
    @Test
    public void testParseTags_String_2() {
        String unparsedTags = "tag1=5 tag2=2asdf";
        ArrayList<OpenTsdbTag> result = OpenTsdbTag.parseTags(unparsedTags);

        ArrayList<OpenTsdbTag> expResult = new ArrayList<>();
        expResult.add(new OpenTsdbTag("tag1=5")); 
        expResult.add(new OpenTsdbTag("tag2=2asdf")); 
        
        int matchCount = 0;
        for (OpenTsdbTag tag : expResult) {
            if (tag.getTag().equals("tag1=5")) matchCount++;
            if (tag.getTag().equals("tag2=2asdf")) matchCount++;
        }
        
        assertEquals(result.size(), 2);
        assertEquals(matchCount, 2);
    }
    
    /**
     * Test of parseTags method, of class OpenTsdbTag.
     */
    @Test
    public void testParseTags_String_int_1() {
        String metricKey = "asdfkjladsflkaslkdj : tag1=5 tag2=2asdf tag3";
        ArrayList<OpenTsdbTag> result = OpenTsdbTag.parseTags(metricKey, 19);

        ArrayList<OpenTsdbTag> expResult = new ArrayList<>();
        expResult.add(new OpenTsdbTag("tag1=5")); 
        expResult.add(new OpenTsdbTag("tag2=2asdf")); 
        
        int matchCount = 0;
        for (OpenTsdbTag tag : expResult) {
            if (tag.getTag().equals("tag1=5")) matchCount++;
            if (tag.getTag().equals("tag2=2asdf")) matchCount++;
        }
        
        assertEquals(result.size(), 2);
        assertEquals(matchCount, 2);
    }
    
    /**
     * Test of parseTags method, of class OpenTsdbTag.
     */
    @Test
    public void testParseTags_String_int_2() {
        String metricKey = "asdfkjladsflkaslkdj : tag1=5 tag2=2asdf";
        ArrayList<OpenTsdbTag> result = OpenTsdbTag.parseTags(metricKey, 19);

        ArrayList<OpenTsdbTag> expResult = new ArrayList<>();
        expResult.add(new OpenTsdbTag("tag1=5")); 
        expResult.add(new OpenTsdbTag("tag2=2asdf")); 
        
        int matchCount = 0;
        for (OpenTsdbTag tag : expResult) {
            if (tag.getTag().equals("tag1=5")) matchCount++;
            if (tag.getTag().equals("tag2=2asdf")) matchCount++;
        }
        
        assertEquals(result.size(), 2);
        assertEquals(matchCount, 2);
    }
    
    /**
     * Test of getTagKey method, of class OpenTsdbTag.
     */
    @Test
    public void testGetTagKey() {
        String unparsedTags = "tag1=5 tag2=2asdf tag3";
        ArrayList<OpenTsdbTag> result = OpenTsdbTag.parseTags(unparsedTags);
        
        assertTrue(result.get(0).getTagKey().equals("tag1"));
        assertTrue(result.get(1).getTagKey().equals("tag2"));
    }

    /**
     * Test of getTagValue method, of class OpenTsdbTag.
     */
    @Test
    public void testGetTagValue() {
        String unparsedTags = "tag1=5 tag2=2asdf tag3";
        ArrayList<OpenTsdbTag> result = OpenTsdbTag.parseTags(unparsedTags);
        
        assertTrue(result.get(0).getTagValue().equals("5"));
        assertTrue(result.get(1).getTagValue().equals("2asdf"));
    }
    
}
