package com.pearson.statsagg.webui.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.suspensions.Suspension;
import com.pearson.statsagg.database_objects.suspensions.SuspensionsDao;
import com.pearson.statsagg.utilities.StackTrace;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prashant kumar (Prashant4nov)
 * @author Jeffrey Schmidt
 */
@WebServlet(name="API_Suspensions_List", urlPatterns={"/api/suspensions-list"})
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
            String json = getSuspensionsList(request);       
            response.setContentType("application/json");
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
        
        try {
            SuspensionsDao suspensionsDao = new SuspensionsDao(false);
            List<Suspension> suspensions = suspensionsDao.getAllDatabaseObjectsInTable();
            if (suspensions == null) suspensions = new ArrayList<>();
            
            List<JsonObject> suspensionsJsonObjects = new ArrayList<>();
            AlertsDao alertsDao = new AlertsDao(suspensionsDao.getDatabaseInterface());
            
            for (Suspension suspension : suspensions) {
                Alert alert = null;
                if ((suspension != null) && suspension.getAlertId() != null) alert = alertsDao.getAlert(suspension.getAlertId());
                JsonObject suspensionJsonObject = Suspension.getJsonObject_ApiFriendly(suspension, alert);
                if (suspensionJsonObject != null) suspensionsJsonObjects.add(suspensionJsonObject);
            }
            
            suspensionsDao.close();
            
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
        
    }
    
}
