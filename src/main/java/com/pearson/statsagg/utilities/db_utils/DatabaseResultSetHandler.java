package com.pearson.statsagg.utilities.db_utils;

import java.sql.ResultSet;
import java.util.List;

/**
 * @author Jeffrey Schmidt
 */
public interface DatabaseResultSetHandler<T> {

    public <T> List<T> handleResultSet(ResultSet resultSet);
    
}
