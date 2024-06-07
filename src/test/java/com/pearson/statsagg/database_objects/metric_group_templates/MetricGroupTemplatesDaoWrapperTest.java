package com.pearson.statsagg.database_objects.metric_group_templates;

import com.pearson.statsagg.database_objects.variable_set.VariableSet;
import com.pearson.statsagg.database_objects.variable_set.VariableSetsDao;
import com.pearson.statsagg.database_objects.variable_set.VariableSetsDaoWrapper;
import com.pearson.statsagg.database_objects.variable_set_list.VariableSetList;
import com.pearson.statsagg.database_objects.variable_set_list.VariableSetListsDao;
import com.pearson.statsagg.database_objects.variable_set_list.VariableSetListsDaoWrapper;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.drivers.Driver;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.Connection;
import java.util.TreeSet;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroupTemplatesDaoWrapperTest {
    
    private VariableSet variableSet_ = null;
    private static final String variableSetName_ = "JUnit - VariableSet for MetricGroupTemplatesDaoWrapperTest";
    private VariableSetList variableSetList_ = null;
    private static final String variableSetListName_ = "JUnit - VariableSetList for MetricGroupTemplatesDaoWrapperTest";
    
    public MetricGroupTemplatesDaoWrapperTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        Driver.initializeApplication_Logger();
        Driver.initializeApplication_DatabaseConfiguration(true);
        Driver.connectToDatabase();
        Driver.setupDatabaseSchema();
    }
    
    @AfterClass
    public static void tearDownClass() {
        DatabaseConnections.disconnectAndShutdown();
    }
    
    @Before
    public void setUp() {
        // delete an metric group template that was inserted into the database from a previous test. verify that it was deleted.
        String result = MetricGroupTemplatesDaoWrapper.deleteRecordInDatabase("metric group template junit 1").getReturnString();
        assertTrue(result.contains("success") || result.contains("The metric group template was not found"));
        
        // delete an metric group template that was inserted into the database from a previous test. verify that it was deleted.
        result = MetricGroupTemplatesDaoWrapper.deleteRecordInDatabase("metric group template junit 1_1").getReturnString();
        assertTrue(result.contains("success") || result.contains("The metric group template was not found"));

        // delete previous variable set list
        result = VariableSetListsDaoWrapper.deleteRecordInDatabase(variableSetListName_).getReturnString();
        assertTrue(result.contains("success") || result.contains("The variable set list was not found"));
        
        // delete previous variable set
        result = VariableSetsDaoWrapper.deleteRecordInDatabase(variableSetName_).getReturnString();
        assertTrue(result.contains("success") || result.contains("The variable set was not found"));
        
        // create a new variable set, and retrieve it from the db. this variable set exists solely for this unit test.
        VariableSet variableSet = new VariableSet(-1, variableSetName_, variableSetName_ + " decription", "taco=food");   
        result = VariableSetsDaoWrapper.createRecordInDatabase(variableSet).getReturnString();
        assertTrue(result.contains("Success"));
        variableSet_ = VariableSetsDao.getVariableSet(DatabaseConnections.getConnection(), true, variableSetName_);
        
        // create a new variable set list, and retrieve it from the db. this variable set list exists solely for this unit test.
        VariableSetList variableSetList = new VariableSetList(-1, variableSetListName_, variableSetListName_ + " decription", "taco=food");   
        TreeSet<String> variableSets = new TreeSet<>();
        variableSets.add(variableSet_.getName());
        result = VariableSetListsDaoWrapper.createRecordInDatabase(variableSetList, variableSets).getReturnString();
        assertTrue(result.contains("Success"));
        variableSetList_ = VariableSetListsDao.getVariableSetList(DatabaseConnections.getConnection(), true, variableSetListName_);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of alterRecordInDatabase method, of class MetricGroupTemplatesDaoWrapperTest.
     */
    @Test
    public void testAlterRecordInDatabase() {
        // cleanup from previous unit test
        String result = MetricGroupTemplatesDaoWrapper.deleteRecordInDatabase("metric group template junit 1").getReturnString();
        assertTrue(result.contains("success") || result.contains("The metric group template was not found"));
        
        // create & insert an metric group template, insert it into the db, retrieve it from the db, & check for correctness of the retrieved records
        MetricGroupTemplate metricGroupTemplate1 = new MetricGroupTemplate(1, "metric group template junit 1", variableSetList_.getId(), 
            "metric group template junit 1 metric group name", "metric group template junit 1 desc", "match.*", "blacklist.*", "taco-tag", false);
        result = MetricGroupTemplatesDaoWrapper.createRecordInDatabase(metricGroupTemplate1).getReturnString();
        assertTrue(result.contains("Success"));
        MetricGroupTemplate metricGroupTemplate1FromDb = MetricGroupTemplatesDao.getMetricGroupTemplate(DatabaseConnections.getConnection(), true, "metric group template junit 1");
        assertTrue(metricGroupTemplate1FromDb.getName().contains("metric group template junit 1"));
        assertTrue(metricGroupTemplate1FromDb.getDescriptionVariable().contains("metric group template junit 1 desc"));
        
        // alter the metric group template description. update the record in the db, retrieve it from the db, & check for correctness of the retrieved records
        metricGroupTemplate1FromDb.setDescriptionVariable("metric group template junit 1_2 desc");
        result = MetricGroupTemplatesDaoWrapper.createRecordInDatabase(metricGroupTemplate1FromDb).getReturnString();
        assertTrue(result.contains("Failed"));
        result = MetricGroupTemplatesDaoWrapper.alterRecordInDatabase(metricGroupTemplate1FromDb, metricGroupTemplate1FromDb.getName()).getReturnString();
        assertTrue(result.contains("Success"));
        MetricGroupTemplate metricGroupTemplate2FromDb = MetricGroupTemplatesDao.getMetricGroupTemplate(DatabaseConnections.getConnection(), true, "metric group template junit 1");
        assertTrue(metricGroupTemplate2FromDb.getName().contains("metric group template junit 1"));
        assertTrue(metricGroupTemplate2FromDb.getDescriptionVariable().contains("metric group template junit 1_2 desc"));
        
        // attempt to delete an metric group template in the database that doesn't exist. make sure it didn't delete the record that was inserted into the db earlier.
        result = MetricGroupTemplatesDaoWrapper.deleteRecordInDatabase("metric group template junit fake 1").getReturnString();
        assertTrue(result.contains("Cancelling"));
        MetricGroupTemplate metricGroupTemplate3FromDb = MetricGroupTemplatesDao.getMetricGroupTemplate(DatabaseConnections.getConnection(), true, "metric group template junit 1");
        assertTrue(metricGroupTemplate3FromDb.getName().contains("metric group template junit 1"));
        
        // alters an metric group template's name, make sure it got properly updated in the db, then changes the metric group template name back to what it was originally named
        Connection connection = DatabaseConnections.getConnection();
        DatabaseUtils.setAutoCommit(connection, false);
        MetricGroupTemplate metricGroupTemplateFromDbOriginalName = MetricGroupTemplatesDao.getMetricGroupTemplate(connection, false, "metric group template junit 1"); //pt 1
        assertTrue(metricGroupTemplateFromDbOriginalName.getName().contains("metric group template junit 1"));
        MetricGroupTemplate metricGroupTemplateFromDbNewName = MetricGroupTemplate.copy(metricGroupTemplateFromDbOriginalName);
        metricGroupTemplateFromDbNewName.setName("metric group template junit 1_1");
        result = MetricGroupTemplatesDaoWrapper.alterRecordInDatabase(metricGroupTemplateFromDbNewName, metricGroupTemplateFromDbOriginalName.getName()).getReturnString();
        assertTrue(result.contains("Successful"));
        MetricGroupTemplate metricGroupTemplateFromDbNewNameVerify = MetricGroupTemplatesDao.getMetricGroupTemplate(connection, false, metricGroupTemplateFromDbNewName.getName()); //pt2
        assertFalse(metricGroupTemplateFromDbNewNameVerify.getName().equals(metricGroupTemplateFromDbOriginalName.getName()));
        assertTrue(metricGroupTemplateFromDbNewNameVerify.getName().contains(metricGroupTemplateFromDbNewName.getName()));
        assertEquals(metricGroupTemplateFromDbOriginalName.getId(), metricGroupTemplateFromDbNewNameVerify.getId());
        MetricGroupTemplate metricGroupTemplateFromDbOriginalName_NoResult = MetricGroupTemplatesDao.getMetricGroupTemplate(connection, false, metricGroupTemplateFromDbOriginalName.getName()); //pt3
        assertEquals(metricGroupTemplateFromDbOriginalName_NoResult, null);
        MetricGroupTemplate metricGroupTemplateFromDbOriginalName_Reset = MetricGroupTemplate.copy(metricGroupTemplateFromDbOriginalName); // pt4
        metricGroupTemplateFromDbOriginalName_Reset.setName(metricGroupTemplateFromDbOriginalName.getName());  
        result = MetricGroupTemplatesDaoWrapper.alterRecordInDatabase(metricGroupTemplateFromDbOriginalName_Reset, metricGroupTemplateFromDbOriginalName.getName()).getReturnString();
        assertTrue(result.contains("Successful"));
        DatabaseUtils.commit(connection);
        DatabaseUtils.cleanup(connection);
        
        // delete the metric group template that was inserted into the database earlier. verify that it was deleted.
        result = MetricGroupTemplatesDaoWrapper.deleteRecordInDatabase("metric group template junit 1").getReturnString();
        assertTrue(result.contains("success"));
        MetricGroupTemplate metricGroupTemplate7FromDb = MetricGroupTemplatesDao.getMetricGroupTemplate(DatabaseConnections.getConnection(), true, "metric group template junit 1");
        assertEquals(null, metricGroupTemplate7FromDb);
        
        // alter existing metric group template, no name change.  delete when done.
        MetricGroupTemplate metricGroupTemplate8FromDb = MetricGroupTemplatesDao.getMetricGroupTemplate(DatabaseConnections.getConnection(), true, "metric group template junit 1_1");
        metricGroupTemplate8FromDb.setDescriptionVariable("taco taco taco");
        result = MetricGroupTemplatesDaoWrapper.alterRecordInDatabase(metricGroupTemplate8FromDb).getReturnString();
        assertTrue(result.contains("Successful"));
        MetricGroupTemplate metricGroupTemplate9FromDb = MetricGroupTemplatesDao.getMetricGroupTemplate(DatabaseConnections.getConnection(), true, "metric group template junit 1_1");
        assertTrue(metricGroupTemplate9FromDb.getDescriptionVariable().equals("taco taco taco"));
        result = MetricGroupTemplatesDaoWrapper.deleteRecordInDatabase("metric group template junit 1_1").getReturnString();
        assertTrue(result.contains("success"));
    }

    /**
     * Test of deleteRecordInDatabase method, of class MetricGroupTemplatesDaoWrapperTest.
     */
    @Test
    public void testDeleteRecordInDatabase() {
        // cleanup from previous unit test
        String result = MetricGroupTemplatesDaoWrapper.deleteRecordInDatabase("metric group template junit name 1").getReturnString();
        assertTrue(result.contains("success") || result.contains("The metric group template was not found"));
        
        MetricGroupTemplate metricGroupTemplate1 = new MetricGroupTemplate(1, "metric group template junit 1", variableSetList_.getId(),
            "metric group template junit 1 metric group name", "metric group template junit 1 desc", "match.*", "blacklist.*", "taco-tag", false);
        result = MetricGroupTemplatesDaoWrapper.createRecordInDatabase(metricGroupTemplate1).getReturnString();
        assertTrue(result.contains("Success"));
        MetricGroupTemplate metricGroupTemplate1FromDb = MetricGroupTemplatesDao.getMetricGroupTemplate(DatabaseConnections.getConnection(), true, "metric group template junit 1");
        assertTrue(metricGroupTemplate1FromDb.getName().contains("metric group template junit 1"));
        assertTrue(metricGroupTemplate1FromDb.getDescriptionVariable().contains("metric group template junit 1"));
        
        result = MetricGroupTemplatesDaoWrapper.deleteRecordInDatabase("metric group template junit 1").getReturnString();
        assertTrue(result.contains("success"));
        MetricGroupTemplate metricGroupTemplate2FromDb = MetricGroupTemplatesDao.getMetricGroupTemplate(DatabaseConnections.getConnection(), true, "metric group template junit 1");
        assertEquals(null, metricGroupTemplate2FromDb);
        
        result = MetricGroupTemplatesDaoWrapper.deleteRecordInDatabase("metric group template junit fake 1").getReturnString();
        assertTrue(result.contains("Cancelling"));
    }
    
}
