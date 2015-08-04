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


package com.pearson.statsagg.webui.api;

import com.pearson.statsagg.database_objects.metric_group.MetricGroup;
import com.pearson.statsagg.database_objects.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database_objects.notifications.NotificationGroup;
import com.pearson.statsagg.database_objects.notifications.NotificationGroupsDao;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONObject;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Prashant Kumar (prashant4nov)
 */
public class NotificationGroupDetailsTest extends Mockito {
    private static NotificationGroupsDao notificationGroupsDao;
    private static final Logger logger = LoggerFactory.getLogger(MetricGroupDetails.class.getName());

    @Before
    public void setUp() {
        NotificationGroup mockNotificationGroup = new NotificationGroup(101, "notification_name", "xyz@gmail.com");
        notificationGroupsDao = mock(NotificationGroupsDao.class);
        when(notificationGroupsDao.getNotificationGroup(101)).thenReturn(mockNotificationGroup);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testGetNotificationGroupDetails() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(Helper.id)).thenReturn("101");

        NotificationGroupDetails notificationGroupDetails = new NotificationGroupDetails();
        
        JSONObject result = notificationGroupDetails.getNotificationGroup(request, notificationGroupsDao);
        System.out.println(result);
        verify(request, atLeast(1)).getParameter(Helper.id);
        assertEquals("xyz@gmail.com", result.get("email_addresses"));
        assertEquals(101, result.get("id"));
        assertEquals("notification_name", result.get("name"));
    }
}
