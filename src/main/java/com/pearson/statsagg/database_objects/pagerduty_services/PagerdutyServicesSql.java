package com.pearson.statsagg.database_objects.pagerduty_services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class PagerdutyServicesSql {
    
    private static final Logger logger = LoggerFactory.getLogger(PagerdutyServicesSql.class.getName());
    
    protected final static String Insert_PagerdutyService =
                    "INSERT INTO PAGERDUTY_SERVICES " +
                    "(NAME, UPPERCASE_NAME, DESCRIPTION, ROUTING_KEY) " +
                    "VALUES(?,?,?,?)";
    
    protected final static String Update_PagerdutyService_ByPrimaryKey =
                    "UPDATE PAGERDUTY_SERVICES " +
                    "SET NAME = ?, UPPERCASE_NAME = ?, DESCRIPTION = ?, ROUTING_KEY = ? " +
                    "WHERE ID = ?";
    
    protected final static String Delete_PagerdutyService_ByPrimaryKey =
                    "DELETE FROM PAGERDUTY_SERVICES " +
                    "WHERE ID = ?";
      
    protected final static String Select_AllPagerdutyServices = 
                    "SELECT * FROM PAGERDUTY_SERVICES";

    protected final static String Select_PagerdutyService_ByPrimaryKey = 
                    "SELECT * FROM PAGERDUTY_SERVICES " +
                    "WHERE ID = ?";
    
    protected final static String Select_PagerdutyService_ByName = 
                    "SELECT * FROM PAGERDUTY_SERVICES " +
                    "WHERE NAME = ?";
    
    protected final static String Select_PagerdutyService_ByUppercaseName = 
                    "SELECT * FROM PAGERDUTY_SERVICES " +
                    "WHERE UPPERCASE_NAME = ?";
    
    protected final static String Select_PagerdutyService_Names_OrderByName = 
                    "SELECT NAME FROM PAGERDUTY_SERVICES " +
                    "WHERE NAME LIKE ? " +
                    "ORDER BY NAME";

}
