package com.pearson.statsagg.web_api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.suspensions.Suspension;
import com.pearson.statsagg.database_objects.suspensions.SuspensionsDao;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prashant kumar (Prashant4nov)
 * @author Jeffrey Schmidt
 */
public class SuspensionsList extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(SuspensionsList.class.getName());
    
    public static final String PAGE_NAME = "API_Suspensions_List";
    
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
            String json = getSuspensionsList(request);       
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
     * Returns json containing a list of suspensions.
     * 
     * @param request servlet request
     * @return list of the suspensions
     */
    protected String getSuspensionsList(HttpServletRequest request) {
        
        if (request == null) {
            return Helper.ERROR_UNKNOWN_JSON;
        }
        
        Connection connection = DatabaseConnections.getConnection();
        
        try {
            List<Suspension> suspensions = SuspensionsDao.getSuspensions(connection, false);
            if (suspensions == null) suspensions = new ArrayList<>();
            
            List<JsonObject> suspensionsJsonObjects = new ArrayList<>();
            
            for (Suspension suspension : suspensions) {
                Alert alert = null;
                if ((suspension != null) && suspension.getAlertId() != null) alert = AlertsDao.getAlert(connection, false, suspension.getAlertId());
                JsonObject suspensionJsonObject = Suspension.getJsonObject_ApiFriendly(suspension, alert);
                if (suspensionJsonObject != null) suspensionsJsonObjects.add(suspensionJsonObject);
            }
            
            DatabaseUtils.cleanup(connection);
            
            Gson suspensionsGson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            JsonElement suspensions_JsonElement = suspensionsGson.toJsonTree(suspensionsJsonObjects);
            JsonArray jsonArray = new Gson().toJsonTree(suspensions_JsonElement).getAsJsonArray();
            String suspensionsJson = suspensionsGson.toJson(jsonArray);
            
            return suspensionsJson;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return Helper.ERROR_UNKNOWN_JSON;
        }
        finally {  
            DatabaseUtils.cleanup(connection);
        }
        
    }
    
}
