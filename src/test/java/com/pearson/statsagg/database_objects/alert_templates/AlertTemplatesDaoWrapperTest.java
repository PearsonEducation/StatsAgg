package com.pearson.statsagg.database_objects.alert_templates;

import java.math.BigDecimal;
import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import com.pearson.statsagg.database_objects.alerts.Alert;
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
public class AlertTemplatesDaoWrapperTest {
    
    private VariableSet variableSet_ = null;
    private static final String variableSetName_ = "JUnit - VariableSet for AlertTemplatesDaoWrapperTest";
    private VariableSetList variableSetList_ = null;
    private static final String variableSetListName_ = "JUnit - VariableSetList for AlertTemplatesDaoWrapperTest";
    
    public AlertTemplatesDaoWrapperTest() {
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
        // delete an alert template that was inserted into the database from a previous test. verify that it was deleted.
        String result = AlertTemplatesDaoWrapper.deleteRecordInDatabase("alert template junit 1").getReturnString();
        assertTrue(result.contains("success") || result.contains("The alert template was not found"));
        
        // delete an alert template that was inserted into the database from a previous test. verify that it was deleted.
        result = AlertTemplatesDaoWrapper.deleteRecordInDatabase("alert template junit 1_1").getReturnString();
        assertTrue(result.contains("success") || result.contains("The alert template was not found"));

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
     * Test of alterRecordInDatabase method, of class AlertTemplatesDaoWrapperTest.
     */
    @Test
    public void testAlterRecordInDatabase() {
        // cleanup from previous unit test
        String result = AlertTemplatesDaoWrapper.deleteRecordInDatabase("alert template junit 1").getReturnString();
        assertTrue(result.contains("success") || result.contains("The alert template was not found"));
        
        // create & insert an alert template, insert it into the db, retrieve it from the db, & check for correctness of the retrieved records
        AlertTemplate alertTemplate1 = new AlertTemplate(1, "alert template junit 1", variableSetList_.getId(), 
            "alert template junit 1 desc", "alert template junit 1 alert name", "alert template junit 1 metric group name", 
            false, true, true, Alert.TYPE_THRESHOLD, true, true, 300000l, DatabaseObjectCommon.TIME_UNIT_SECONDS, 
            "alert template junit 1 caution name", "alert template junit 1 caution positive name", Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 1, 
            "alert template junit 1 danger name", "alert template junit 1 danger positive name", Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("200"), 1000L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 2);
        result = AlertTemplatesDaoWrapper.createRecordInDatabase(alertTemplate1).getReturnString();
        assertTrue(result.contains("Success"));
        AlertTemplate alertTemplate1FromDb = AlertTemplatesDao.getAlertTemplate(DatabaseConnections.getConnection(), true, "alert template junit 1");
        assertTrue(alertTemplate1FromDb.getName().contains("alert template junit 1"));
        assertTrue(alertTemplate1FromDb.getDescriptionVariable().contains("alert template junit 1 desc"));
        
        // alter the alert template description. update the record in the db, retrieve it from the db, & check for correctness of the retrieved records
        alertTemplate1FromDb.setDescriptionVariable("alert template junit 1_2 desc");
        result = AlertTemplatesDaoWrapper.createRecordInDatabase(alertTemplate1FromDb).getReturnString();
        assertTrue(result.contains("Failed"));
        result = AlertTemplatesDaoWrapper.alterRecordInDatabase(alertTemplate1FromDb, alertTemplate1FromDb.getName()).getReturnString();
        assertTrue(result.contains("Success"));
        AlertTemplate alertTemplate2FromDb = AlertTemplatesDao.getAlertTemplate(DatabaseConnections.getConnection(), true, "alert template junit 1");
        assertTrue(alertTemplate2FromDb.getName().contains("alert template junit 1"));
        assertTrue(alertTemplate2FromDb.getDescriptionVariable().contains("alert template junit 1_2 desc"));
        
        // attempt to delete an alert template in the database that doesn't exist. make sure it didn't delete the record that was inserted into the db earlier.
        result = AlertTemplatesDaoWrapper.deleteRecordInDatabase("alert template junit fake 1").getReturnString();
        assertTrue(result.contains("Cancelling"));
        AlertTemplate alertTemplate3FromDb = AlertTemplatesDao.getAlertTemplate(DatabaseConnections.getConnection(), true, "alert template junit 1");
        assertTrue(alertTemplate3FromDb.getName().contains("alert template junit 1"));
        
        // alters an alert template's name, make sure it got properly updated in the db, then changes the alert template name back to what it was originally named
        Connection connection = DatabaseConnections.getConnection();
        DatabaseUtils.setAutoCommit(connection, false);
        AlertTemplate alertTemplateFromDbOriginalName = AlertTemplatesDao.getAlertTemplate(connection, false, "alert template junit 1"); //pt 1
        assertTrue(alertTemplateFromDbOriginalName.getName().contains("alert template junit 1"));
        AlertTemplate alertTemplateFromDbNewName = AlertTemplate.copy(alertTemplateFromDbOriginalName);
        alertTemplateFromDbNewName.setName("alert template junit 1_1");
        result = AlertTemplatesDaoWrapper.alterRecordInDatabase(alertTemplateFromDbNewName, alertTemplateFromDbOriginalName.getName()).getReturnString();
        assertTrue(result.contains("Successful"));
        AlertTemplate alertTemplateFromDbNewNameVerify = AlertTemplatesDao.getAlertTemplate(connection, false, alertTemplateFromDbNewName.getName()); //pt2
        assertFalse(alertTemplateFromDbNewNameVerify.getName().equals(alertTemplateFromDbOriginalName.getName()));
        assertTrue(alertTemplateFromDbNewNameVerify.getName().contains(alertTemplateFromDbNewName.getName()));
        assertEquals(alertTemplateFromDbOriginalName.getId(), alertTemplateFromDbNewNameVerify.getId());
        AlertTemplate alertTemplateFromDbOriginalName_NoResult = AlertTemplatesDao.getAlertTemplate(connection, false, alertTemplateFromDbOriginalName.getName()); //pt3
        assertEquals(alertTemplateFromDbOriginalName_NoResult, null);
        AlertTemplate alertTemplateFromDbOriginalName_Reset = AlertTemplate.copy(alertTemplateFromDbOriginalName); // pt4
        alertTemplateFromDbOriginalName_Reset.setName(alertTemplateFromDbOriginalName.getName());  
        result = AlertTemplatesDaoWrapper.alterRecordInDatabase(alertTemplateFromDbOriginalName_Reset, alertTemplateFromDbOriginalName.getName()).getReturnString();
        assertTrue(result.contains("Successful"));
        DatabaseUtils.commit(connection);
        DatabaseUtils.cleanup(connection);
        
        // delete the alert template that was inserted into the database earlier. verify that it was deleted.
        result = AlertTemplatesDaoWrapper.deleteRecordInDatabase("alert template junit 1").getReturnString();
        assertTrue(result.contains("success"));
        AlertTemplate alertTemplate7FromDb = AlertTemplatesDao.getAlertTemplate(DatabaseConnections.getConnection(), true, "alert template junit 1");
        assertEquals(null, alertTemplate7FromDb);
        
        // alter existing alert template, no name change.  delete when done.
        AlertTemplate alertTemplate8FromDb = AlertTemplatesDao.getAlertTemplate(DatabaseConnections.getConnection(), true, "alert template junit 1_1");
        alertTemplate8FromDb.setDescriptionVariable("taco taco taco");
        result = AlertTemplatesDaoWrapper.alterRecordInDatabase(alertTemplate8FromDb).getReturnString();
        assertTrue(result.contains("Successful"));
        AlertTemplate alertTemplate9FromDb = AlertTemplatesDao.getAlertTemplate(DatabaseConnections.getConnection(), true, "alert template junit 1_1");
        assertTrue(alertTemplate9FromDb.getDescriptionVariable().equals("taco taco taco"));
        result = AlertTemplatesDaoWrapper.deleteRecordInDatabase("alert template junit 1_1").getReturnString();
        assertTrue(result.contains("success"));
    }

    /**
     * Test of deleteRecordInDatabase method, of class AlertTemplatesDaoWrapperTest.
     */
    @Test
    public void testDeleteRecordInDatabase() {
        // cleanup from previous unit test
        String result = AlertTemplatesDaoWrapper.deleteRecordInDatabase("alert template junit name 1").getReturnString();
        assertTrue(result.contains("success") || result.contains("The alert template was not found"));
        
        AlertTemplate alertTemplate1 = new AlertTemplate(1, "alert template junit 1", variableSetList_.getId(),
            "alert template junit 1 desc", "alert template junit 1 alert name", "alert template junit 1 metric group name", 
            false, true, true, Alert.TYPE_THRESHOLD, true, true, 300000l, DatabaseObjectCommon.TIME_UNIT_SECONDS, 
            "alert template junit 1 caution name", "alert template junit 1 caution positive name", Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("100"), 900L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 1,
            "alert template junit 1 danger name", "alert template junit 1 danger positive name", Alert.OPERATOR_GREATER, Alert.COMBINATION_ALL, null, new BigDecimal("200"), 1000L, DatabaseObjectCommon.TIME_UNIT_SECONDS, null, DatabaseObjectCommon.TIME_UNIT_SECONDS, 2);
        result = AlertTemplatesDaoWrapper.createRecordInDatabase(alertTemplate1).getReturnString();
        assertTrue(result.contains("Success"));
        AlertTemplate alertTemplate1FromDb = AlertTemplatesDao.getAlertTemplate(DatabaseConnections.getConnection(), true, "alert template junit 1");
        assertTrue(alertTemplate1FromDb.getName().contains("alert template junit 1"));
        assertTrue(alertTemplate1FromDb.getDescriptionVariable().contains("alert template junit 1"));
        
        result = AlertTemplatesDaoWrapper.deleteRecordInDatabase("alert template junit 1").getReturnString();
        assertTrue(result.contains("success"));
        AlertTemplate alertTemplate2FromDb = AlertTemplatesDao.getAlertTemplate(DatabaseConnections.getConnection(), true, "alert template junit 1");
        assertEquals(null, alertTemplate2FromDb);
        
        result = AlertTemplatesDaoWrapper.deleteRecordInDatabase("alert template junit fake 1").getReturnString();
        assertTrue(result.contains("Cancelling"));
    }
    
}
