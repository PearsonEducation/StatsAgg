package com.pearson.statsagg.database_objects.notification_group_templates;

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
public class NotificationGroupTemplatesDaoWrapperTest {
    
    private VariableSet variableSet_ = null;
    private static final String variableSetName_ = "JUnit - VariableSet for NotificationGroupTemplatesDaoWrapperTest";
    private VariableSetList variableSetList_ = null;
    private static final String variableSetListName_ = "JUnit - VariableSetList for NotificationGroupTemplatesDaoWrapperTest";
    
    public NotificationGroupTemplatesDaoWrapperTest() {
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
        // delete an notification group template that was inserted into the database from a previous test. verify that it was deleted.
        String result = NotificationGroupTemplatesDaoWrapper.deleteRecordInDatabase("notification group template junit 1").getReturnString();
        assertTrue(result.contains("success") || result.contains("The notification group template was not found"));
        
        // delete an notification group template that was inserted into the database from a previous test. verify that it was deleted.
        result = NotificationGroupTemplatesDaoWrapper.deleteRecordInDatabase("notification group template junit 1_1").getReturnString();
        assertTrue(result.contains("success") || result.contains("The notification group template was not found"));

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
     * Test of alterRecordInDatabase method, of class NotificationGroupTemplatesDaoWrapperTest.
     */
    @Test
    public void testAlterRecordInDatabase() {
        // cleanup from previous unit test
        String result = NotificationGroupTemplatesDaoWrapper.deleteRecordInDatabase("notification group template junit 1").getReturnString();
        assertTrue(result.contains("success") || result.contains("The notification group template was not found"));
        
        // create & insert an notification group template, insert it into the db, retrieve it from the db, & check for correctness of the retrieved records
        NotificationGroupTemplate notificationGroupTemplate1 = new NotificationGroupTemplate(1, "notification group template junit 1", variableSetList_.getId(), 
            "notification group template junit 1 notification group name", "email_111@taco.com", "ngt pds variable name", false);
        result = NotificationGroupTemplatesDaoWrapper.createRecordInDatabase(notificationGroupTemplate1).getReturnString();
        assertTrue(result.contains("Success"));
        NotificationGroupTemplate notificationGroupTemplate1FromDb = NotificationGroupTemplatesDao.getNotificationGroupTemplate(DatabaseConnections.getConnection(), true, "notification group template junit 1");
        assertTrue(notificationGroupTemplate1FromDb.getName().contains("notification group template junit 1"));
        assertTrue(notificationGroupTemplate1FromDb.getEmailAddressesVariable().contains("email_111@taco.com"));
        
        // alter the notification group template description. update the record in the db, retrieve it from the db, & check for correctness of the retrieved records
        notificationGroupTemplate1FromDb.setEmailAddressesVariable("email_112@taco.com");
        result = NotificationGroupTemplatesDaoWrapper.createRecordInDatabase(notificationGroupTemplate1FromDb).getReturnString();
        assertTrue(result.contains("Failed"));
        result = NotificationGroupTemplatesDaoWrapper.alterRecordInDatabase(notificationGroupTemplate1FromDb, notificationGroupTemplate1FromDb.getName()).getReturnString();
        assertTrue(result.contains("Success"));
        NotificationGroupTemplate notificationGroupTemplate2FromDb = NotificationGroupTemplatesDao.getNotificationGroupTemplate(DatabaseConnections.getConnection(), true, "notification group template junit 1");
        assertTrue(notificationGroupTemplate2FromDb.getName().contains("notification group template junit 1"));
        assertTrue(notificationGroupTemplate2FromDb.getEmailAddressesVariable().contains("email_112@taco.com"));
        
        // attempt to delete an notification group template in the database that doesn't exist. make sure it didn't delete the record that was inserted into the db earlier.
        result = NotificationGroupTemplatesDaoWrapper.deleteRecordInDatabase("notification group template junit fake 1").getReturnString();
        assertTrue(result.contains("Cancelling"));
        NotificationGroupTemplate notificationGroupTemplate3FromDb = NotificationGroupTemplatesDao.getNotificationGroupTemplate(DatabaseConnections.getConnection(), true, "notification group template junit 1");
        assertTrue(notificationGroupTemplate3FromDb.getName().contains("notification group template junit 1"));
        
        // alters an notification group template's name, make sure it got properly updated in the db, then changes the notification group template name back to what it was originally named
        Connection connection = DatabaseConnections.getConnection();
        DatabaseUtils.setAutoCommit(connection, false);
        NotificationGroupTemplate notificationGroupTemplateFromDbOriginalName = NotificationGroupTemplatesDao.getNotificationGroupTemplate(connection, false, "notification group template junit 1"); //pt 1
        assertTrue(notificationGroupTemplateFromDbOriginalName.getName().contains("notification group template junit 1"));
        NotificationGroupTemplate notificationGroupTemplateFromDbNewName = NotificationGroupTemplate.copy(notificationGroupTemplateFromDbOriginalName);
        notificationGroupTemplateFromDbNewName.setName("notification group template junit 1_1");
        result = NotificationGroupTemplatesDaoWrapper.alterRecordInDatabase(notificationGroupTemplateFromDbNewName, notificationGroupTemplateFromDbOriginalName.getName()).getReturnString();
        assertTrue(result.contains("Successful"));
        NotificationGroupTemplate notificationGroupTemplateFromDbNewNameVerify = NotificationGroupTemplatesDao.getNotificationGroupTemplate(connection, false, notificationGroupTemplateFromDbNewName.getName()); //pt2
        assertFalse(notificationGroupTemplateFromDbNewNameVerify.getName().equals(notificationGroupTemplateFromDbOriginalName.getName()));
        assertTrue(notificationGroupTemplateFromDbNewNameVerify.getName().contains(notificationGroupTemplateFromDbNewName.getName()));
        assertEquals(notificationGroupTemplateFromDbOriginalName.getId(), notificationGroupTemplateFromDbNewNameVerify.getId());
        NotificationGroupTemplate notificationGroupTemplateFromDbOriginalName_NoResult = NotificationGroupTemplatesDao.getNotificationGroupTemplate(connection, false, notificationGroupTemplateFromDbOriginalName.getName()); //pt3
        assertEquals(notificationGroupTemplateFromDbOriginalName_NoResult, null);
        NotificationGroupTemplate notificationGroupTemplateFromDbOriginalName_Reset = NotificationGroupTemplate.copy(notificationGroupTemplateFromDbOriginalName); // pt4
        notificationGroupTemplateFromDbOriginalName_Reset.setName(notificationGroupTemplateFromDbOriginalName.getName());  
        result = NotificationGroupTemplatesDaoWrapper.alterRecordInDatabase(notificationGroupTemplateFromDbOriginalName_Reset, notificationGroupTemplateFromDbOriginalName.getName()).getReturnString();
        assertTrue(result.contains("Successful"));
        DatabaseUtils.commit(connection);
        DatabaseUtils.cleanup(connection);
        
        // delete the notification group template that was inserted into the database earlier. verify that it was deleted.
        result = NotificationGroupTemplatesDaoWrapper.deleteRecordInDatabase("notification group template junit 1").getReturnString();
        assertTrue(result.contains("success"));
        NotificationGroupTemplate notificationGroupTemplate7FromDb = NotificationGroupTemplatesDao.getNotificationGroupTemplate(DatabaseConnections.getConnection(), true, "notification group template junit 1");
        assertEquals(null, notificationGroupTemplate7FromDb);
        
        // alter existing notification group template, no name change.  delete when done.
        NotificationGroupTemplate notificationGroupTemplate8FromDb = NotificationGroupTemplatesDao.getNotificationGroupTemplate(DatabaseConnections.getConnection(), true, "notification group template junit 1_1");
        notificationGroupTemplate8FromDb.setEmailAddressesVariable("email_113@taco.com");
        result = NotificationGroupTemplatesDaoWrapper.alterRecordInDatabase(notificationGroupTemplate8FromDb).getReturnString();
        assertTrue(result.contains("Successful"));
        NotificationGroupTemplate notificationGroupTemplate9FromDb = NotificationGroupTemplatesDao.getNotificationGroupTemplate(DatabaseConnections.getConnection(), true, "notification group template junit 1_1");
        assertTrue(notificationGroupTemplate9FromDb.getEmailAddressesVariable().equals("email_113@taco.com"));
        result = NotificationGroupTemplatesDaoWrapper.deleteRecordInDatabase("notification group template junit 1_1").getReturnString();
        assertTrue(result.contains("success"));
    }

    /**
     * Test of deleteRecordInDatabase method, of class NotificationGroupTemplatesDaoWrapperTest.
     */
    @Test
    public void testDeleteRecordInDatabase() {
        // cleanup from previous unit test
        String result = NotificationGroupTemplatesDaoWrapper.deleteRecordInDatabase("notification group template junit name 1").getReturnString();
        assertTrue(result.contains("success") || result.contains("The notification group template was not found"));
        
        NotificationGroupTemplate notificationGroupTemplate1 = new NotificationGroupTemplate(1, "notification group template junit 1", variableSetList_.getId(),
            "notification group template junit 1 notification group name", "email_111@taco.com", "ngt pds variable name", false);
        result = NotificationGroupTemplatesDaoWrapper.createRecordInDatabase(notificationGroupTemplate1).getReturnString();
        assertTrue(result.contains("Success"));
        NotificationGroupTemplate notificationGroupTemplate1FromDb = NotificationGroupTemplatesDao.getNotificationGroupTemplate(DatabaseConnections.getConnection(), true, "notification group template junit 1");
        assertTrue(notificationGroupTemplate1FromDb.getName().contains("notification group template junit 1"));
        assertTrue(notificationGroupTemplate1FromDb.getEmailAddressesVariable().contains("email_111@taco.com"));
        
        result = NotificationGroupTemplatesDaoWrapper.deleteRecordInDatabase("notification group template junit 1").getReturnString();
        assertTrue(result.contains("success"));
        NotificationGroupTemplate notificationGroupTemplate2FromDb = NotificationGroupTemplatesDao.getNotificationGroupTemplate(DatabaseConnections.getConnection(), true, "notification group template junit 1");
        assertEquals(null, notificationGroupTemplate2FromDb);
        
        result = NotificationGroupTemplatesDaoWrapper.deleteRecordInDatabase("notification group template junit fake 1").getReturnString();
        assertTrue(result.contains("Cancelling"));
    }
    
}
