package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.configuration.DatabaseConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.io.PrintWriter;
import java.sql.Connection;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class HealthCheck extends HttpServlet {
   
    private static final Logger logger = LoggerFactory.getLogger(HealthCheck.class.getName());
    
    public static final String PAGE_NAME = "Health Check";
    
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        processGetRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        processGetRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return PAGE_NAME;
    }
    
    protected void processGetRequest(HttpServletRequest request, HttpServletResponse response) {
        
        if ((request == null) || (response == null)) {
            return;
        }
        
        try {  
            request.setCharacterEncoding("UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/plain");
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        } 
        
        PrintWriter out = null;

        try {
            Connection connection = DatabaseConnections.getConnection();
            boolean isDatabaseConnected = connection.isValid(5);
            DatabaseUtils.cleanup(connection);
            
            if (!isDatabaseConnected) response.setStatus(500);
            if (!GlobalVariables.isApplicationInitializeSuccess.get()) response.setStatus(500);
            
            String product = "Product: StatsAgg" + "\n";
            String version = "Version : " + com.pearson.statsagg.controller.Version.getProjectVersion() + "-" + com.pearson.statsagg.controller.Version.getBuildTimestamp() + "\n";
            String databaseEngine = "Database engine : " + DatabaseConfiguration.getTypeString() + "\n";
            String databaseConnected = "Is database connected? : " + isDatabaseConnected + "\n";
            String databaseInMemory = "Is database ephemeral? : " + GlobalVariables.isStatsaggUsingInMemoryDatabase + "\n";
            String wasApplicationInitializationSuccessful = "Was application initialization successful? : " + GlobalVariables.isApplicationInitializeSuccess + "\n";
            
            String outputString = product + version + databaseEngine + databaseConnected + databaseInMemory + wasApplicationInitializationSuccessful;
                    
            out = response.getWriter();
            out.println(outputString);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            
            try {
                if (out != null) out.close();
                out = response.getWriter();
                response.setStatus(500);
                out.println("StatsAgg -- an error occurred while outputting the HealthCheck information");
            }
            catch (Exception e2) {
                logger.error(e2.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e2));
            }
        }
        finally {            
            if (out != null) {
                out.close();
            }
        }
        
    }
    
}
