package com.pearson.statsagg.utilities.db_utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Jeffrey Schmidt
 */
public class PreparedStatementAndResultSet {
    
    private final PreparedStatement preparedStatement_;
    private final ResultSet resultSet_;
    
    public PreparedStatementAndResultSet(PreparedStatement preparedStatement, ResultSet resultSet) {
        this.preparedStatement_ = preparedStatement;
        this.resultSet_ = resultSet;
    }

    public void close() {
        DatabaseUtils.cleanup(preparedStatement_, resultSet_);
    }
    
    public PreparedStatement getPreparedStatement() {
        return preparedStatement_;
    }

    public ResultSet getResultSet() {
        return resultSet_;
    }

}
