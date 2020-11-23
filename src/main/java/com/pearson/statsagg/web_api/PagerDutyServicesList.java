package com.pearson.statsagg.web_api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pearson.statsagg.database_objects.pagerduty_services.PagerdutyService;
import com.pearson.statsagg.database_objects.pagerduty_services.PagerdutyServicesDao;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Prashant Kumar (prashant4nov)
 * @author Jeffrey Schmidt
 */
public class PagerDutyServicesList extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(PagerDutyServicesList.class.getName());
    
    public static final String PAGE_NAME = "API_PagerDutyServices_List";
 
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
            String json = getPagerDutyServicesList(request);       
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
     * Returns json containing a list of pagerduty services.
     * 
     * @param request servlet request
     * @return list of pagerduty services
     */ 
    protected String getPagerDutyServicesList(HttpServletRequest request) {
        
        if (request == null) {
            return Helper.ERROR_UNKNOWN_JSON;
        }
        
        try {
            List<PagerdutyService> pagerdutyServices = PagerdutyServicesDao.getPagerdutyServices(DatabaseConnections.getConnection(), true);
            if (pagerdutyServices == null) pagerdutyServices = new ArrayList<>();
            
            List<JsonObject> pagerdutyServicesJsonObjects = new ArrayList<>();
            for (PagerdutyService pagerdutyService : pagerdutyServices) {
                JsonObject pagerdutyServiceJsonObject = PagerdutyService.getJsonObject_ApiFriendly(pagerdutyService);
                if (pagerdutyServiceJsonObject != null) pagerdutyServicesJsonObjects.add(pagerdutyServiceJsonObject);
            }
            
            Gson pagerdutyServicesGson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            JsonElement pagerdutyServices_JsonElement = pagerdutyServicesGson.toJsonTree(pagerdutyServicesJsonObjects);
            JsonArray jsonArray = new Gson().toJsonTree(pagerdutyServices_JsonElement).getAsJsonArray();
            String pagerdutyServicesJson = pagerdutyServicesGson.toJson(jsonArray);
            
            return pagerdutyServicesJson;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return Helper.ERROR_UNKNOWN_JSON;
        }
        
    }
    
}
