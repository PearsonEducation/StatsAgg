package com.pearson.statsagg.metric_formats.opentsdb;

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
