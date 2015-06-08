package com.pearson.statsagg.webui;

import com.pearson.statsagg.webui.MetricGroupsLogic;
import java.util.List;
import java.util.TreeSet;
import com.pearson.statsagg.controller.ContextManager;
import com.pearson.statsagg.database.metric_group.MetricGroup;
import com.pearson.statsagg.database.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database.metric_group_regex.MetricGroupRegex;
import com.pearson.statsagg.database.metric_group_regex.MetricGroupRegexsDao;
import com.pearson.statsagg.database.metric_group_tags.MetricGroupTag;
import com.pearson.statsagg.database.metric_group_tags.MetricGroupTagsDao;
import com.pearson.statsagg.globals.DatabaseConnections;
import java.io.File;
import java.io.InputStream;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jeffrey Schmidt
 */
public class MetricGroupsLogicTest {
    
    private final MetricGroupsLogic metricGroupsLogic_ = new MetricGroupsLogic();

    public MetricGroupsLogicTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        ContextManager contextManager = new ContextManager();
        InputStream ephemeralDatabaseConfiguration = contextManager.getEphemeralDatabaseConfiguration();
        contextManager.initializeDatabaseFromInputStream(ephemeralDatabaseConfiguration);
        contextManager.createDatabaseSchemas();
    }
    
    @AfterClass
    public static void tearDownClass() {
        DatabaseConnections.disconnectAndShutdown();
    }
    
    @Before
    public void setUp() {
        // delete a metric group that was inserted into the database from a previous test. verify that it was deleted.
        String result = metricGroupsLogic_.deleteRecordInDatabase("metricgroup junit_test");
        assertTrue(result.contains("success") || result.contains("Metric group not found"));
        
        // delete a metric group that was inserted into the database from a previous test. verify that it was deleted.
        result = metricGroupsLogic_.deleteRecordInDatabase("metricgroup junit_test_11");
        assertTrue(result.contains("success") || result.contains("Metric group not found"));
    }
    
    @After
    public void tearDown() {

    }

    /**
     * Test of alterRecordInDatabase method, of class MetricGroupsLogic.
     */
    @Test
    public void testAlterRecordInDatabase() {
        MetricGroup metricGroup1 = new MetricGroup(-1, "metricgroup junit_test", "this is a junit test 1");
        TreeSet<String> matchRegexs1 = new TreeSet<>();
        matchRegexs1.add(".*junit_1_1.*");
        matchRegexs1.add(".*junit_1_2.*");
        TreeSet<String> tags1 = new TreeSet<>();
        tags1.add("tag1_1");
        tags1.add("tag1_2");
        
        String result = metricGroupsLogic_.alterRecordInDatabase(metricGroup1, null, null, null);
        assertTrue(result.contains("Fail"));
        
        result = metricGroupsLogic_.alterRecordInDatabase(null, matchRegexs1, null, tags1);
        assertTrue(result.contains("Fail"));
        
        // create a metric group & insert it into the db
        result = metricGroupsLogic_.alterRecordInDatabase(metricGroup1, matchRegexs1, null, tags1);
        assertTrue(result.contains("Success"));

        // check to see that the metric group is in the database & has correct values
        MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
        MetricGroup metricGroupFromDb = metricGroupsDao.getMetricGroupByName("metricgroup junit_test");
        assertTrue(metricGroupFromDb.getDescription().equals("this is a junit test 1"));

        // check to see that the metric group's regexs made it into the db
        MetricGroupRegexsDao metricGroupRegexsDao = new MetricGroupRegexsDao();
        List<MetricGroupRegex> metricGroupRegexs = metricGroupRegexsDao.getMetricGroupRegexsByMetricGroupId(metricGroupFromDb.getId());
        for (MetricGroupRegex metricGroupRegex : metricGroupRegexs) assertTrue(matchRegexs1.contains(metricGroupRegex.getPattern()));
        assertEquals(matchRegexs1.size(), metricGroupRegexs.size());
        
        // check to see that the metric group's tags made it into the db
        MetricGroupTagsDao metricGroupTagsDao = new MetricGroupTagsDao();
        List<MetricGroupTag> metricGroupTags = metricGroupTagsDao.getMetricGroupTagsByMetricGroupId(metricGroupFromDb.getId());
        for (MetricGroupTag metricGroupTag : metricGroupTags) assertTrue(tags1.contains(metricGroupTag.getTag()));
        assertEquals(tags1.size(), metricGroupTags.size());
 
        // test altering metric group name
        metricGroupsDao = new MetricGroupsDao(false);
        MetricGroup metricGroupFromDbOriginalName = metricGroupsDao.getMetricGroupByName("metricgroup junit_test"); // pt1
        assertTrue(metricGroupFromDbOriginalName.getName().contains("metricgroup junit_test"));
        MetricGroup metricGroupFromDbNewName = MetricGroup.copy(metricGroupFromDbOriginalName);
        metricGroupFromDbNewName.setName("metricgroup junit_test_11");
        result = metricGroupsLogic_.alterRecordInDatabase(metricGroupFromDbNewName, matchRegexs1, null, tags1, metricGroupFromDbNewName.getName());
        assertTrue(result.contains("Successful"));
        MetricGroup metricGroupFromDbNewNameVerify = metricGroupsDao.getMetricGroupByName(metricGroupFromDbNewName.getName()); // pt2
        assertTrue(metricGroupFromDbNewNameVerify.getName().contains(metricGroupFromDbNewName.getName()));
        assertFalse(metricGroupFromDbNewNameVerify.getName().equals(metricGroupFromDbOriginalName.getName()));
        assertEquals(metricGroupFromDbOriginalName.getId(), metricGroupFromDbNewNameVerify.getId());
        MetricGroup metricGroupFromDbOriginalName_NoResult = metricGroupsDao.getMetricGroupByName(metricGroupFromDbOriginalName.getName()); // pt3
        assertEquals(metricGroupFromDbOriginalName_NoResult, null);
        MetricGroup metricGroupFromDbOriginalName_Reset = MetricGroup.copy(metricGroupFromDbOriginalName); // pt4
        metricGroupFromDbOriginalName_Reset.setName(metricGroupFromDbOriginalName.getName());  
        result = metricGroupsLogic_.alterRecordInDatabase(metricGroupFromDbOriginalName_Reset, matchRegexs1, null, tags1, metricGroupFromDbOriginalName.getName());
        assertTrue(result.contains("Successful"));
        metricGroupsDao.close();
        
        // create a second metric group & insert it into the db. also check to see if inserting blacklist regexes works.
        MetricGroup metricGroup2 = new MetricGroup(-1, "metricgroup junit_test", "this is a junit test 2");
        TreeSet<String> matchRegexs2 = new TreeSet<>();
        matchRegexs2.add(".*junit_2_2.*");
        matchRegexs2.add(".*junit_2_1.*");
        matchRegexs2.add(".*junit_2_3.*");
        TreeSet<String> blacklistRegexs2 = new TreeSet<>();
        blacklistRegexs2.add(".*blaclist1.*");
        blacklistRegexs2.add(".*blaclist2.*");
        TreeSet<String> tags2 = new TreeSet<>();
        tags2.add("tag2_1");
        tags2.add("tag2_2");
        tags2.add("tag2_3");
        result = metricGroupsLogic_.alterRecordInDatabase(metricGroup2, matchRegexs2, blacklistRegexs2, tags2);
        assertTrue(result.contains("Fail"));
        result = metricGroupsLogic_.alterRecordInDatabase(metricGroup2, matchRegexs2, blacklistRegexs2, tags2, metricGroup2.getName());
        assertTrue(result.contains("Success"));
        
        // check to see that the second metric group is in the database & has correct values
        metricGroupsDao = new MetricGroupsDao();
        metricGroupFromDb = metricGroupsDao.getMetricGroupByName("metricgroup junit_test");
        Integer metricGroupFromDb2 = metricGroupFromDb.getId();
        assertTrue(metricGroupFromDb.getDescription().equals("this is a junit test 2"));
        
        // check to see that the second metric group's regexs made it into the db & that we didn't accidentially associate with other regexs 
        metricGroupRegexsDao = new MetricGroupRegexsDao();
        metricGroupRegexs = metricGroupRegexsDao.getMetricGroupRegexsByMetricGroupId(metricGroupFromDb.getId());
        for (MetricGroupRegex metricGroupRegex : metricGroupRegexs) {
            if (!metricGroupRegex.isBlacklistRegex()) {
                assertTrue(matchRegexs2.contains(metricGroupRegex.getPattern()));
                assertFalse(matchRegexs1.contains(metricGroupRegex.getPattern()));
            }
            else {
                assertTrue(blacklistRegexs2.contains(metricGroupRegex.getPattern()));
            }
        }
        assertEquals(matchRegexs2.size() + blacklistRegexs2.size(), metricGroupRegexs.size());
        assertEquals(matchRegexs2.first(), ".*junit_2_1.*");
        
        // check to see that the second metric group's tags made it into the db & that we didn't accidentially associate with other tags 
        metricGroupTagsDao = new MetricGroupTagsDao();
        metricGroupTags = metricGroupTagsDao.getMetricGroupTagsByMetricGroupId(metricGroupFromDb.getId());
        for (MetricGroupTag metricGroupTag : metricGroupTags) {
            assertTrue(tags2.contains(metricGroupTag.getTag()));
            assertFalse(tags1.contains(metricGroupTag.getTag()));
        }
        assertEquals(tags2.size(), metricGroupTags.size());
        assertEquals(tags2.first(),"tag2_1");
        
        // cleanup
        metricGroupsLogic_.deleteRecordInDatabase("metricgroup junit_test");
        metricGroupsDao = new MetricGroupsDao();
        metricGroupFromDb = metricGroupsDao.getMetricGroupByName("metricgroup junit_test");
        assertEquals(null, metricGroupFromDb);
        metricGroupRegexsDao = new MetricGroupRegexsDao();
        metricGroupRegexs = metricGroupRegexsDao.getMetricGroupRegexsByMetricGroupId(metricGroupFromDb2);
        assertEquals(0, metricGroupRegexs.size());
    }
    
    /**
     * Test of deleteRecordInDatabase method, of class MetricGroupsLogic.
     */
    @Test
    public void testDeleteRecordInDatabase() {
        // create a metric group & insert it into the db
        MetricGroup metricGroup1 = new MetricGroup(-1, "metricgroup junit_test_delete", "this is a junit test of delete");
        TreeSet<String> matchRegexs1 = new TreeSet<>();
        matchRegexs1.add(".*junit_delete_1_1.*");
        matchRegexs1.add(".*junit_delete_1_2.*");
        TreeSet<String> blacklistRegexs1 = new TreeSet<>();
        blacklistRegexs1.add(".*blaclist1.*");
        blacklistRegexs1.add(".*blaclist2.*");
        TreeSet<String> tags1 = new TreeSet<>();
        tags1.add("tag1_1");
        tags1.add("tag1_2");
        String result = metricGroupsLogic_.alterRecordInDatabase(metricGroup1, matchRegexs1, blacklistRegexs1, tags1);
        assertTrue(result.contains("Success"));
        
        // check to see that the metric group is in the database & has correct values
        MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
        MetricGroup metricGroupFromDb = metricGroupsDao.getMetricGroupByName("metricgroup junit_test_delete");
        Integer metricGroupIdFromDb = metricGroupFromDb.getId();
        assertTrue(metricGroupFromDb.getDescription().equals("this is a junit test of delete"));
        
        // check to see that the metric group's regexs made it into the db
        MetricGroupRegexsDao metricGroupRegexsDao = new MetricGroupRegexsDao();
        List<MetricGroupRegex> metricGroupRegexs = metricGroupRegexsDao.getMetricGroupRegexsByMetricGroupId(metricGroupFromDb.getId());
        assertEquals(matchRegexs1.size() + blacklistRegexs1.size(), metricGroupRegexs.size());
        
        // check to see that the metric group's tags made it into the db
        MetricGroupTagsDao metricGroupTagsDao = new MetricGroupTagsDao();
        List<MetricGroupTag> metricGroupTags = metricGroupTagsDao.getMetricGroupTagsByMetricGroupId(metricGroupFromDb.getId());
        assertEquals(tags1.size(), metricGroupTags.size());
        
        // delete the metric group & verify that everything is deleted (metric group, regexs, tags)
        result = metricGroupsLogic_.deleteRecordInDatabase("metricgroup junit_test_delete");
        assertTrue(result.contains("success"));
        metricGroupsDao = new MetricGroupsDao();
        metricGroupFromDb = metricGroupsDao.getMetricGroupByName("metricgroup junit_test_delete");
        assertEquals(null, metricGroupFromDb);
        metricGroupRegexsDao = new MetricGroupRegexsDao();
        metricGroupRegexs = metricGroupRegexsDao.getMetricGroupRegexsByMetricGroupId(metricGroupIdFromDb);
        assertEquals(0, metricGroupRegexs.size());
        metricGroupTagsDao = new MetricGroupTagsDao();
        metricGroupTags = metricGroupTagsDao.getMetricGroupTagsByMetricGroupId(metricGroupIdFromDb);
        assertEquals(0, metricGroupTags.size());
        
        result = metricGroupsLogic_.deleteRecordInDatabase("metricgroup junit_test_delete");
        assertTrue(result.contains("not found"));
        
        result = metricGroupsLogic_.deleteRecordInDatabase("metricgroup junit_test_this_doesnt_exist");
        assertTrue(result.contains("not found"));
    }

    
}
