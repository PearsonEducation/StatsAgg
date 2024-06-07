package com.pearson.statsagg.web_api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pearson.statsagg.configuration.DatabaseConfiguration;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.io.PrintWriter;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class HealthCheck extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheck.class.getName());
    
    public static final String PAGE_NAME = "API_HealthCheck";
 
    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return PAGE_NAME;
    }
    
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        
        PrintWriter out = null;
        
        try {  
            request.setCharacterEncoding("UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        try {    
            String json = getHealthCheck(request, response);       
            out = response.getWriter();
            out.println(json);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }     
        finally {            
            if (out != null) {
                out.close();
            }
        } 
        
    }

    /**
     * Returns json containing health check information.
     * 
     * @param request servlet request
     * @return health check status
     */ 
    protected String getHealthCheck(HttpServletRequest request, HttpServletResponse response) {
        
        if (request == null) {
            return Helper.ERROR_UNKNOWN_JSON;
        }
        
        try {
            Connection connection = DatabaseConnections.getConnection();
            boolean isDatabaseConnected = connection.isValid(5);
            DatabaseUtils.cleanup(connection);
            
            if (!isDatabaseConnected) response.setStatus(500);
            if (!GlobalVariables.isApplicationInitializeSuccess.get()) response.setStatus(500);
            
            Gson healthCheck_Gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
			JsonObject healthCheck_JsonObject = new JsonObject();
			healthCheck_JsonObject.addProperty("Product", "StatsAgg");
            healthCheck_JsonObject.addProperty("Version", (com.pearson.statsagg.controller.Version.getProjectVersion() + "-" + com.pearson.statsagg.controller.Version.getBuildTimestamp()));
			healthCheck_JsonObject.addProperty("Init-Success", GlobalVariables.isApplicationInitializeSuccess.get());
			healthCheck_JsonObject.addProperty("Database-Engine", DatabaseConfiguration.getTypeString());
			healthCheck_JsonObject.addProperty("Database-Ephemeral", GlobalVariables.isStatsaggUsingInMemoryDatabase.get());
			healthCheck_JsonObject.addProperty("Database-Connected", isDatabaseConnected);
			
			String healthcheckJson = healthCheck_Gson.toJson(healthCheck_JsonObject);

            return healthcheckJson;
        }
        catch (Exception e) {
			try {
				response.setStatus(500);
			}
			catch (Exception e2) {}
			
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return Helper.ERROR_UNKNOWN_JSON;
        }
        
    }
    
}
