/*
 * Copyright 2015 prashant4nov.
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
package com.pearson.statsagg.database_objects.metric_group;

import com.pearson.statsagg.database_objects.metric_group.MetricGroup;
import com.pearson.statsagg.controller.ContextManager;
import com.pearson.statsagg.database_objects.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database_engine.DatabaseConnections;
import com.pearson.statsagg.webui.AlertsLogic;
import com.pearson.statsagg.webui.MetricGroupsLogic;
import java.io.InputStream;
import java.util.TreeSet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author prashant kumar (prashant4nov)
 */
public class MetricGroupsDaoTest {
    
   // private MetricGroupsDao metricGroupsDao; 
    private static final JSONObject mockMetricGroupsJson = new JSONObject();
    private static final Logger logger = LoggerFactory.getLogger(AlertsLogic.class.getName());
    private final MetricGroup metricGroup_ = null;
    private static final String metricGroupName_ = "JUnit - MetricGroup for AlertsLogicTest";
    private final MetricGroupsLogic metricGroupsLogic_ = new MetricGroupsLogic();
    
    @BeforeClass
    public static void setUpClass() {
        ContextManager contextManager = new ContextManager();
        InputStream ephemeralDatabaseConfiguration = contextManager.getEphemeralDatabaseConfiguration();
        contextManager.initializeDatabaseFromInputStream(ephemeralDatabaseConfiguration);
        contextManager.createDatabaseSchemas();
         
        JSONArray mockMetricGroupsList = new JSONArray();
        JSONObject mockMetric = new JSONObject();
        
        mockMetric.put("name", "metricgroup junit_test 1");
        mockMetric.put("id", "1");
        mockMetricGroupsList.add(mockMetric);
        
        mockMetric = new JSONObject();
        
        mockMetric.put("name", "metricgroup junit_test 2");
        mockMetric.put("id", "2");
        mockMetricGroupsList.add(mockMetric);
             
        mockMetricGroupsJson.put("metricgroups", mockMetricGroupsList);
        mockMetricGroupsJson.put("count", 2);
    }
    
    @AfterClass
    public static void tearDownClass() {
        DatabaseConnections.disconnectAndShutdown();
    }
    
    @Before
    public void setUp() {
        
    }
    
    @After
    public void tearDown() {
        // delete a metric group that was inserted into the database from a previous test. verify that it was deleted.
        String result = metricGroupsLogic_.deleteRecordInDatabase("metricgroup junit_test 1");
        assertTrue(result.contains("success") || result.contains("Metric group not found"));
        
        // delete a metric group that was inserted into the database from a previous test. verify that it was deleted.
        result = metricGroupsLogic_.deleteRecordInDatabase("metricgroup junit_test 2");
        assertTrue(result.contains("success") || result.contains("Metric group not found"));
    }

    @Test
    public void testGetMetricGroups() {
        MetricGroup metricGroup1 = new MetricGroup(-1, "metricgroup junit_test 1", "this is a junit test 1");
        TreeSet<String> matchRegexes1 = new TreeSet<>();
        matchRegexes1.add(".*junit_1_1.*");
        matchRegexes1.add(".*junit_1_2.*");
        TreeSet<String> tags1 = new TreeSet<>();
        tags1.add("tag1_1");
        tags1.add("tag1_2");
              
        // create a metric group & insert it into the db
        String result = metricGroupsLogic_.alterRecordInDatabase(metricGroup1, matchRegexes1, null, tags1);
        assertTrue(result.contains("Success"));
       
        // create a second metric group & insert it into the db. also check to see if inserting blacklist regexes works.
        MetricGroup metricGroup2 = new MetricGroup(-1, "metricgroup junit_test 2", "this is a junit test 2");
        TreeSet<String> matchRegexes2 = new TreeSet<>();
        matchRegexes2.add(".*junit_2_2.*");
        matchRegexes2.add(".*junit_2_1.*");
        matchRegexes2.add(".*junit_2_3.*");
        TreeSet<String> blacklistRegexes2 = new TreeSet<>();
        blacklistRegexes2.add(".*blaclist1.*");
        blacklistRegexes2.add(".*blaclist2.*");
        TreeSet<String> tags2 = new TreeSet<>();
        tags2.add("tag2_1");
        tags2.add("tag2_2");
        tags2.add("tag2_3");
        
        result = metricGroupsLogic_.alterRecordInDatabase(metricGroup2, matchRegexes2, blacklistRegexes2, tags2, metricGroup2.getName());
        assertTrue(result.contains("Success"));
        
        MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
        JSONObject resultMetricGroups = metricGroupsDao.getMetricGroups(0, 10);
        assertEquals(mockMetricGroupsJson.get("count"), resultMetricGroups.get("count"));
    }
    
    @Test
    public void testGetMetricGroupsNoMetricGroups() {
        JSONObject mockMetricGroupsJsonNoMetric = new JSONObject();
        mockMetricGroupsJsonNoMetric.put("metricgroups", new JSONArray());
        mockMetricGroupsJsonNoMetric.put("count", 0);

        JSONObject resultMetricGroups = new MetricGroupsDao().getMetricGroups(0, 0);
        assertEquals(mockMetricGroupsJsonNoMetric, resultMetricGroups);

        resultMetricGroups = new MetricGroupsDao().getMetricGroups(0, 100);
        assertEquals(mockMetricGroupsJsonNoMetric, resultMetricGroups);
        
        MetricGroup metricGroup1 = new MetricGroup(-1, "metricgroup junit_test 1", "this is a junit test 1");
        TreeSet<String> matchRegexes1 = new TreeSet<>();
        matchRegexes1.add(".*junit_1_1.*");
        matchRegexes1.add(".*junit_1_2.*");
        TreeSet<String> tags1 = new TreeSet<>();
        tags1.add("tag1_1");
        tags1.add("tag1_2");
              
        // create a metric group & insert it into the db
        String result = metricGroupsLogic_.alterRecordInDatabase(metricGroup1, matchRegexes1, null, tags1);
        assertTrue(result.contains("Success"));
       
        // create a second metric group & insert it into the db. also check to see if inserting blacklist regexes works.
        MetricGroup metricGroup2 = new MetricGroup(-1, "metricgroup junit_test 2", "this is a junit test 2");
        TreeSet<String> matchRegexes2 = new TreeSet<>();
        matchRegexes2.add(".*junit_2_2.*");
        matchRegexes2.add(".*junit_2_1.*");
        matchRegexes2.add(".*junit_2_3.*");
        TreeSet<String> blacklistRegexes2 = new TreeSet<>();
        blacklistRegexes2.add(".*blaclist1.*");
        blacklistRegexes2.add(".*blaclist2.*");
        TreeSet<String> tags2 = new TreeSet<>();
        tags2.add("tag2_1");
        tags2.add("tag2_2");
        tags2.add("tag2_3");
        
        result = metricGroupsLogic_.alterRecordInDatabase(metricGroup2, matchRegexes2, blacklistRegexes2, tags2, metricGroup2.getName());
        assertTrue(result.contains("Success"));
              
        resultMetricGroups = new MetricGroupsDao().getMetricGroups(115600, 34);
        assertEquals(mockMetricGroupsJsonNoMetric, resultMetricGroups);
    }
}
