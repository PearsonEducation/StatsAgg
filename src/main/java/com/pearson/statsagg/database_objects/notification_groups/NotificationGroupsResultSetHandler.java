package com.pearson.statsagg.database_objects.notification_groups;

import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseResultSetHandler;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class NotificationGroupsResultSetHandler extends NotificationGroup implements DatabaseResultSetHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupsResultSetHandler.class.getName());
    
    @Override
    public List<NotificationGroup> handleResultSet(ResultSet resultSet) {
        
        List<NotificationGroup> notificationGroups = new ArrayList<>();
        
        try {
           Set<String> lowercaseColumnNames = DatabaseUtils.getResultSetColumnNames_Lowercase(resultSet);

            while ((lowercaseColumnNames != null) && resultSet.next()) {
                try {
                    Integer id = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "id", Integer.class);
                    String name = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "name", String.class);
                    String uppercaseName = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "uppercase_name", String.class);
                    Integer notificationGroupTemplateId = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "notification_group_template_id", Integer.class);
                    Integer variableSetId = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "variable_set_id", Integer.class);
                    String emailAddresses = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "email_addresses", String.class);
                    Integer pagerdutyServiceId = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "pagerduty_service_id", Integer.class);
                    
                    NotificationGroup notificationGroup = new NotificationGroup(id, name, uppercaseName, 
                            notificationGroupTemplateId, variableSetId, emailAddresses, pagerdutyServiceId);
                    notificationGroups.add(notificationGroup);
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
        }
        
        return notificationGroups;
    }

}

