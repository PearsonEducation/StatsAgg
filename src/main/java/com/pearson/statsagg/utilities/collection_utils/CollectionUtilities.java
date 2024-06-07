package com.pearson.statsagg.utilities.collection_utils;

import java.util.Set;

/**
 * @author Jeffrey Schmidt
 */
public class CollectionUtilities {
    
    public static <T> boolean areSetContentsEqual(Set<T> set1, Set<T> set2) {
        
        if ((set1 == null) && (set2 != null)) return false;
        if ((set1 != null) && (set2 == null)) return false;
        if ((set1 == null) && (set2 == null)) return true;
        if ((set1 != null) && (set2 != null) && (set1.size() != set2.size())) return false;
        
        if ((set1 != null) && (set2 != null)) {
            for (T set1Value : set1) {
                if (!set2.contains(set1Value)) return false;
            }
        }

        return true;
    }
    
}
