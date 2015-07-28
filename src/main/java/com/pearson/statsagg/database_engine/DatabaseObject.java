package com.pearson.statsagg.database_engine;

/**
 * @author Jeffrey Schmidt
 * @param <T>
 */
public abstract class DatabaseObject<T> {

    public abstract boolean isEqual(T t);
    
}
