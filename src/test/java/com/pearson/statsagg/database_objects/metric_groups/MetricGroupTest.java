package com.pearson.statsagg.database_objects.metric_groups;

import java.util.TreeSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroupTest {
    
    public MetricGroupTest() {
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
     * Test of isEqual method, of class MetricGroup.
     */
    @Test
    public void testIsEqual() {
        TreeSet<String> matchRegexes = new TreeSet<>();
        matchRegexes.add(".*match1.*");
        matchRegexes.add(".*match2.*");
        TreeSet<String> blacklistRegexes = new TreeSet<>();
        blacklistRegexes.add(".*blacklist1.*");
        blacklistRegexes.add(".*blacklist2.*");
        TreeSet<String> tags = new TreeSet<>();
        tags.add("tag1");
        tags.add("tag2");
        
        MetricGroup metricGroup1 = new MetricGroup(1, "MetricGroup JUnit1 Name", "MetricGroup JUnit1 Desc", null, null, matchRegexes, blacklistRegexes, tags);
        MetricGroup metricGroup2 = new MetricGroup(1, "MetricGroup JUnit1 Name", "MetricGroup JUnit1 Desc", null, null, matchRegexes, blacklistRegexes, tags);
        assertTrue(metricGroup1.isEqual(metricGroup2));
        
        metricGroup1.setId(-1);
        assertFalse(metricGroup1.isEqual(metricGroup2));
        metricGroup1.setId(1);
        
        metricGroup1.setName("MetricGroup JUnit1 Name Fake");
        assertFalse(metricGroup1.isEqual(metricGroup2));
        metricGroup1.setName("MetricGroup JUnit1 Name");
        
        metricGroup1.setDescription("MetricGroup JUnit1 Desc Fake");
        assertFalse(metricGroup1.isEqual(metricGroup2));
        metricGroup1.setDescription("MetricGroup JUnit1 Desc");
        
        assertTrue(metricGroup1.isEqual(metricGroup2));
    }

    /**
     * Test of copy method, of class MetricGroup.
     */
    @Test
    public void testCopy() {
        TreeSet<String> matchRegexes = new TreeSet<>();
        matchRegexes.add(".*match1.*");
        matchRegexes.add(".*match2.*");
        TreeSet<String> blacklistRegexes = new TreeSet<>();
        blacklistRegexes.add(".*blacklist1.*");
        blacklistRegexes.add(".*blacklist2.*");
        TreeSet<String> tags = new TreeSet<>();
        tags.add("tag1");
        tags.add("tag2");
        MetricGroup metricGroup1 = new MetricGroup(1, "MetricGroup JUnit1 Name", "MetricGroup JUnit1 Desc", null, null, matchRegexes, blacklistRegexes, tags);
        
        MetricGroup metricGroup2 = MetricGroup.copy(metricGroup1);
        assertTrue(metricGroup1.isEqual(metricGroup2));
        
        metricGroup1.setId(-1);
        assertFalse(metricGroup1.isEqual(metricGroup2));
        assertTrue(metricGroup2.getId() == 1);
        metricGroup1.setId(1);
        
        metricGroup1.setName("MetricGroup JUnit1 Name Bad");
        assertFalse(metricGroup1.isEqual(metricGroup2));
        assertTrue(metricGroup2.getName().equals("MetricGroup JUnit1 Name"));
        metricGroup1.setName("MetricGroup JUnit1 Name");
        
        metricGroup1.setDescription("MetricGroup JUnit1 Desc Bad");
        assertFalse(metricGroup1.isEqual(metricGroup2));
        assertTrue(metricGroup2.getDescription().equals("MetricGroup JUnit1 Desc"));
        metricGroup1.setDescription("MetricGroup JUnit1 Desc");
        
        TreeSet<String> matchRegexes_bad = new TreeSet<>();
        matchRegexes_bad.add(".*match1.*");
        metricGroup1.setMatchRegexes(matchRegexes_bad);
        assertFalse(metricGroup1.isEqual(metricGroup2));
        assertTrue(metricGroup2.getMatchRegexes().size() == 2);
        assertTrue(metricGroup2.getMatchRegexes().contains(".*match1.*"));
        assertTrue(metricGroup2.getMatchRegexes().contains(".*match2.*"));
        metricGroup1.setMatchRegexes(matchRegexes);
        
        TreeSet<String> blacklistRegexes_bad = new TreeSet<>();
        blacklistRegexes_bad.add(".*blacklist1.*");
        metricGroup1.setBlacklistRegexes(blacklistRegexes_bad);
        assertFalse(metricGroup1.isEqual(metricGroup2));
        assertTrue(metricGroup2.getBlacklistRegexes().size() == 2);
        assertTrue(metricGroup2.getBlacklistRegexes().contains(".*blacklist1.*"));
        assertTrue(metricGroup2.getBlacklistRegexes().contains(".*blacklist2.*"));
        metricGroup1.setBlacklistRegexes(blacklistRegexes);
        
        TreeSet<String> tags_bad = new TreeSet<>();
        tags_bad.add("tag1");
        metricGroup1.setTags(tags_bad);
        assertFalse(metricGroup1.isEqual(metricGroup2));
        assertTrue(metricGroup2.getTags().size() == 2);
        assertTrue(metricGroup2.getTags().contains("tag1"));
        assertTrue(metricGroup2.getTags().contains("tag2"));
        metricGroup1.setTags(tags);
        
        assertTrue(metricGroup1.isEqual(metricGroup2));
    }

}
