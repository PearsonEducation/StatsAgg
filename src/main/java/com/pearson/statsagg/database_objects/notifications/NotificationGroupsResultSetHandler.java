package com.pearson.statsagg.database_objects.notifications;

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
            Set<String> columnNames = DatabaseUtils.getResultSetColumns(resultSet);
            
            while ((columnNames != null) && resultSet.next()) {
                try {
                    String columnName = "ID";
                    Integer id = (columnNames.contains(columnName)) ? resultSet.getInt(columnName) : null;
                    if (resultSet.wasNull()) id = null;
                    
                    columnName = "NAME";
                    String name = (columnNames.contains(columnName)) ? resultSet.getString(columnName) : null;
                    if (resultSet.wasNull()) name = null;

                    columnName = "UPPERCASE_NAME";
                    String uppercaseName = (columnNames.contains(columnName)) ? resultSet.getString(columnName) : null;
                    if (resultSet.wasNull()) uppercaseName = null;

                    columnName = "EMAIL_ADDRESSES";
                    String emailAddresses = (columnNames.contains(columnName)) ? resultSet.getString(columnName) : null;
                    if (resultSet.wasNull()) emailAddresses = null;

                    NotificationGroup notificationGroup = new NotificationGroup(id, name, uppercaseName, emailAddresses);
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

