package com.pearson.statsagg.utilities.db_utils;

import java.sql.Statement;
import java.sql.ResultSet;

/**
 * @author Jeffrey Schmidt
 */
public class StatementAndResultSet {
    
    private final Statement statement_;
    private final ResultSet resultSet_;
    
    public StatementAndResultSet(Statement statement, ResultSet resultSet) {
        this.statement_ = statement;
        this.resultSet_ = resultSet;
    }

    public void close() {
        DatabaseUtils.cleanup(statement_, resultSet_);
    }
    
    public Statement getStatement() {
        return statement_;
    }

    public ResultSet getResultSet() {
        return resultSet_;
    }

}
