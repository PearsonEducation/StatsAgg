package com.pearson.statsagg.database_objects.notification_group_templates;

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
public class NotificationGroupTemplatesResultSetHandler extends NotificationGroupTemplate implements DatabaseResultSetHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupTemplatesResultSetHandler.class.getName());
    
    @Override
    public List<NotificationGroupTemplate> handleResultSet(ResultSet resultSet) {
        
        List<NotificationGroupTemplate> notificationGroupTemplates = new ArrayList<>();
        
        try {
           Set<String> lowercaseColumnNames = DatabaseUtils.getResultSetColumnNames_Lowercase(resultSet);

            while ((lowercaseColumnNames != null) && resultSet.next()) {
                try {
                    Integer id = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "id", Integer.class);
                    String name = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "name", String.class);
                    String uppercaseName = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "uppercase_name", String.class);
                    Integer variableSetListId = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "variable_set_list_id", Integer.class);
                    String notificationGroupNameVariable = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "notification_group_name_variable", String.class);
                    String emailAddressesVariable = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "email_addresses_variable", String.class);
                    String pagerdutyServiceNameVariable = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "pagerduty_service_name_variable", String.class);
                    Boolean isMarkedForDelete = DatabaseUtils.getResultSetValue(resultSet, lowercaseColumnNames, "is_marked_for_delete", Boolean.class);

                    NotificationGroupTemplate notificationGroupTemplate = new NotificationGroupTemplate(id, name, uppercaseName, 
                            variableSetListId, notificationGroupNameVariable, emailAddressesVariable, pagerdutyServiceNameVariable, isMarkedForDelete);
                    notificationGroupTemplates.add(notificationGroupTemplate);
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));  
        }
        
        return notificationGroupTemplates;
    }

}

