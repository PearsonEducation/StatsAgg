package com.pearson.statsagg.database_objects.notification_groups;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Jeffrey Schmidt
 */
public class NotificationGroupTest {
    
    public NotificationGroupTest() {
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
     * Test of isEqual method, of class NotificationGroup.
     */
    @Test
    public void testIsEqual() {
        NotificationGroup notificationGroup1 = new NotificationGroup(1, "NotificationGroup JUnit1 Name", null, null, "NotificationGroup JUnit1 Email", 100);
        NotificationGroup notificationGroup2 = new NotificationGroup(1, "NotificationGroup JUnit1 Name", null, null, "NotificationGroup JUnit1 Email", 100);
        
        assertTrue(notificationGroup1.isEqual(notificationGroup2));
        
        notificationGroup1.setId(-1);
        assertFalse(notificationGroup1.isEqual(notificationGroup2));
        notificationGroup1.setId(1);
        
        notificationGroup1.setName("NotificationGroup JUnit1 Name Bad");
        assertFalse(notificationGroup1.isEqual(notificationGroup2));
        notificationGroup1.setName("NotificationGroup JUnit1 Name");
        
        notificationGroup1.setEmailAddresses("NotificationGroup JUnit1 Email Bad");
        assertFalse(notificationGroup1.isEqual(notificationGroup2));
        notificationGroup1.setEmailAddresses("NotificationGroup JUnit1 Email");
        
        notificationGroup1.setPagerdutyServiceId(99);
        assertFalse(notificationGroup1.isEqual(notificationGroup2));
        notificationGroup1.setPagerdutyServiceId(100);
        
        assertTrue(notificationGroup1.isEqual(notificationGroup2));
    }

    /**
     * Test of copy method, of class NotificationGroup.
     */
    @Test
    public void testCopy() {
        NotificationGroup notificationGroup1 = new NotificationGroup(1, "NotificationGroup JUnit1 Name", null, null, "NotificationGroup JUnit1 Email", 100);
        
        NotificationGroup notificationGroup2 = NotificationGroup.copy(notificationGroup1);
        assertTrue(notificationGroup1.isEqual(notificationGroup2));
        
        notificationGroup1.setId(-1);
        assertFalse(notificationGroup1.isEqual(notificationGroup2));
        assertTrue(notificationGroup2.getId() == 1);
        notificationGroup1.setId(1);
        
        notificationGroup1.setName("NotificationGroup JUnit1 Name Bad");
        assertFalse(notificationGroup1.isEqual(notificationGroup2));
        assertTrue(notificationGroup2.getName().equals("NotificationGroup JUnit1 Name"));
        notificationGroup1.setName("NotificationGroup JUnit1 Name");
        
        notificationGroup1.setEmailAddresses("NotificationGroup JUnit1 Email Bad");
        assertFalse(notificationGroup1.isEqual(notificationGroup2));
        assertTrue(notificationGroup2.getEmailAddresses().equals("NotificationGroup JUnit1 Email"));
        notificationGroup1.setEmailAddresses("NotificationGroup JUnit1 Email");
        
        notificationGroup1.setPagerdutyServiceId(99);
        assertFalse(notificationGroup1.isEqual(notificationGroup2));
        assertTrue(notificationGroup2.getPagerdutyServiceId() == 100);
        notificationGroup1.setPagerdutyServiceId(100);
        
        assertTrue(notificationGroup1.isEqual(notificationGroup2));
    }

}
