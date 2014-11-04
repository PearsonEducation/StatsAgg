package com.pearson.statsagg.webui;

import com.pearson.statsagg.controller.ContextManager;
import com.pearson.statsagg.database.notifications.NotificationGroup;
import com.pearson.statsagg.database.notifications.NotificationGroupsDao;
import com.pearson.statsagg.globals.DatabaseConnections;
import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Jeffrey Schmidt
 */
public class NotificationGroupsLogicTest {
    
    private final NotificationGroupsLogic notificationGroupsLogic_ = new NotificationGroupsLogic();

    public NotificationGroupsLogicTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        ContextManager ContextManager = new ContextManager();
        ContextManager.initializeDatabaseFromFile(System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator +
                "webapp" + File.separator + "WEB-INF" + File.separator + "config", "database.properties");
    }
    
    @AfterClass
    public static void tearDownClass() {
        DatabaseConnections.disconnectAndShutdown();
    }
    
    @Before
    public void setUp() {
        // delete notification that was inserted into the database from a previous test. verify that it was deleted.
        String result = notificationGroupsLogic_.deleteRecordInDatabase("notification junit name 1");
        assertTrue(result.contains("success") || result.contains("Notification group not found"));
        
        // delete a notification that was inserted into the database from a previous test. verify that it was deleted.
        result = notificationGroupsLogic_.deleteRecordInDatabase("notification junit name 11");
        assertTrue(result.contains("success") || result.contains("Notification group not found"));
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of alterRecordInDatabase method, of class NotificationGroupsLogic.
     */
    @Test
    public void testAlterRecordInDatabase() {
        // cleanup from previous unit test
        String result = notificationGroupsLogic_.deleteRecordInDatabase("notification junit name 1");
        assertTrue(result.contains("success") || result.contains("Notification group not found"));
        
        // create & insert a NotificationGroup, insert it into the db, retrieve it from the db, & compare the original & retrieved records
        NotificationGroup notification1 = new NotificationGroup(-1, "notification junit name 1", "notification junit email 1");   
        result = notificationGroupsLogic_.alterRecordInDatabase(notification1);
        assertTrue(result.contains("Success"));
        NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
        NotificationGroup notification1FromDb = notificationGroupsDao.getNotificationGroupByName("notification junit name 1");
        assertTrue(notification1FromDb.getName().contains("notification junit name 1"));
        assertTrue(notification1FromDb.getEmailAddresses().contains("notification junit email 1"));
        
        notification1FromDb.setEmailAddresses("notification junit email 1_2");
        result = notificationGroupsLogic_.alterRecordInDatabase(notification1FromDb);
        assertTrue(result.contains("Fail"));
        result = notificationGroupsLogic_.alterRecordInDatabase(notification1FromDb, notification1FromDb.getName());
        assertTrue(result.contains("Success"));
        notificationGroupsDao = new NotificationGroupsDao();
        NotificationGroup notification2FromDb = notificationGroupsDao.getNotificationGroupByName("notification junit name 1");
        assertTrue(notification2FromDb.getName().contains("notification junit name 1"));
        assertTrue(notification2FromDb.getEmailAddresses().contains("notification junit email 1_2"));
        
        result = notificationGroupsLogic_.deleteRecordInDatabase("notification junit fake 1");
        assertTrue(result.contains("Cancelling"));
        notificationGroupsDao = new NotificationGroupsDao();
        NotificationGroup notification3FromDb = notificationGroupsDao.getNotificationGroupByName("notification junit name 1");
        assertTrue(notification3FromDb.getName().contains("notification junit name 1"));
        
        // test altering metric group name
        notificationGroupsDao = new NotificationGroupsDao(false);
        NotificationGroup notificationGroupFromDbOriginalName = notificationGroupsDao.getNotificationGroupByName("notification junit name 1"); // pt1
        assertTrue(notificationGroupFromDbOriginalName.getName().contains("notification junit name 1"));
        NotificationGroup notificationGroupFromDbNewName = NotificationGroup.copy(notificationGroupFromDbOriginalName);
        notificationGroupFromDbNewName.setName("notification junit name 11");
        result = notificationGroupsLogic_.alterRecordInDatabase(notificationGroupFromDbNewName, notificationGroupFromDbNewName.getName());
        assertTrue(result.contains("Successful"));
        NotificationGroup notificationGroupFromDbNewNameVerify = notificationGroupsDao.getNotificationGroupByName(notificationGroupFromDbNewName.getName()); // pt2
        assertTrue(notificationGroupFromDbNewNameVerify.getName().contains(notificationGroupFromDbNewName.getName()));
        assertFalse(notificationGroupFromDbNewNameVerify.getName().equals(notificationGroupFromDbOriginalName.getName()));
        assertEquals(notificationGroupFromDbOriginalName.getId(), notificationGroupFromDbNewNameVerify.getId());
        NotificationGroup notificationGroupFromDbOriginalName_NoResult = notificationGroupsDao.getNotificationGroupByName(notificationGroupFromDbOriginalName.getName()); // pt3
        assertEquals(notificationGroupFromDbOriginalName_NoResult, null);
        NotificationGroup notificationGroupFromDbOriginalName_Reset = NotificationGroup.copy(notificationGroupFromDbOriginalName); // pt4
        notificationGroupFromDbOriginalName_Reset.setName(notificationGroupFromDbOriginalName.getName());  
        result = notificationGroupsLogic_.alterRecordInDatabase(notificationGroupFromDbOriginalName_Reset, notificationGroupFromDbOriginalName.getName());
        assertTrue(result.contains("Successful"));
        notificationGroupsDao.close();
        
        result = notificationGroupsLogic_.deleteRecordInDatabase("notification junit name 1");
        assertTrue(result.contains("success"));
        notificationGroupsDao = new NotificationGroupsDao();
        NotificationGroup notification4FromDb = notificationGroupsDao.getNotificationGroupByName("notification junit name 1");
        assertEquals(null, notification4FromDb);
    }

    /**
     * Test of deleteRecordInDatabase method, of class NotificationGroupsLogic.
     */
    @Test
    public void testDeleteRecordInDatabase() {
        String result = notificationGroupsLogic_.deleteRecordInDatabase("notification junit name 1");
        assertTrue(result.contains("success") || result.contains("Notification group not found"));
        
        NotificationGroup notification1 = new NotificationGroup(-1, "notification junit name 1", "notification junit email 1");   
        result = notificationGroupsLogic_.alterRecordInDatabase(notification1);
        assertTrue(result.contains("Success"));
        NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
        NotificationGroup notification1FromDb = notificationGroupsDao.getNotificationGroupByName("notification junit name 1");
        assertTrue(notification1FromDb.getName().contains("notification junit name 1"));
        assertTrue(notification1FromDb.getEmailAddresses().contains("notification junit email 1"));
        
        result = notificationGroupsLogic_.deleteRecordInDatabase("notification junit name 1");
        assertTrue(result.contains("success"));
        notificationGroupsDao = new NotificationGroupsDao();
        NotificationGroup notification2FromDb = notificationGroupsDao.getNotificationGroupByName("notification junit name 1");
        assertEquals(null, notification2FromDb);
        
        result = notificationGroupsLogic_.deleteRecordInDatabase("notification junit fake 1");
        assertTrue(result.contains("Cancelling"));
    }
    
}
