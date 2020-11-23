package com.pearson.statsagg.web_api;

import com.google.gson.JsonObject;
import com.pearson.statsagg.database_objects.pagerduty_services.PagerdutyService;
import com.pearson.statsagg.database_objects.pagerduty_services.PagerdutyServicesDao;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.utilities.json_utils.JsonUtils;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prashant kumar (prashant4nov)
 * @author Jeffrey Schmidt
 */
public class PagerDutyServiceDetails extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(PagerDutyServiceDetails.class.getName());
    
    public static final String PAGE_NAME = "API_PagerDutyService_Details";
    
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
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        processRequest(request, response);
    }
    
    private void processRequest(HttpServletRequest request, HttpServletResponse response) {
        
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
            String json = getPagerdutyServiceDetails(request);
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
     * Returns a json string containing the details of the requested pagerduty service.
     * 
     * @param request servlet request
     * @return details of the requested pagerduty service
     */
    protected String getPagerdutyServiceDetails(HttpServletRequest request) {
        
        if (request == null) {
            return Helper.ERROR_UNKNOWN_JSON;
        }
        
        try {
            Integer pagerdutyServiceId = null;
            String pagerdutyServiceName = null;

            if (request.getParameter("id") != null) pagerdutyServiceId = Integer.parseInt(request.getParameter("id"));
            if (request.getParameter("name") != null) pagerdutyServiceName = request.getParameter("name");

            if ((pagerdutyServiceId == null) && (pagerdutyServiceName == null)) {
                JsonObject jsonObject = Helper.getJsonObjectFromRequestBody(request);
                pagerdutyServiceId = JsonUtils.getIntegerFieldFromJsonObject(jsonObject, "id");
                pagerdutyServiceName = JsonUtils.getStringFieldFromJsonObject(jsonObject, "name");
            }

            PagerdutyService pagerdutyService = null;
            if (pagerdutyServiceId != null) pagerdutyService = PagerdutyServicesDao.getPagerdutyService(DatabaseConnections.getConnection(), true, pagerdutyServiceId);
            else if (pagerdutyServiceName != null) pagerdutyService = PagerdutyServicesDao.getPagerdutyService(DatabaseConnections.getConnection(), true, pagerdutyServiceName);
            
            if (pagerdutyService != null) return PagerdutyService.getJsonString_ApiFriendly(pagerdutyService);
            else return Helper.ERROR_NOTFOUND_JSON;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));        
        }
        
        return Helper.ERROR_UNKNOWN_JSON;
    }

}
