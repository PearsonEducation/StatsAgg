package com.pearson.statsagg.database;

/**
 * @author Jeffrey Schmidt
 * @param <T>
 */
public abstract class DatabaseObject<T> {

    public abstract boolean isEqual(T t);
    
}
