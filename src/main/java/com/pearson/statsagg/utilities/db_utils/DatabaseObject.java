package com.pearson.statsagg.utilities.db_utils;

/**
 * @author Jeffrey Schmidt
 */
public interface DatabaseObject<T> {
    
    public boolean isEqual(T t);
    
}
