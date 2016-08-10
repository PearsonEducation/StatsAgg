package com.pearson.statsagg.webui.api;

import com.google.gson.JsonObject;
import com.pearson.statsagg.utilities.StackTrace;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prashant kumar(prashant4nov)
 */
@WebServlet(name = "API_CreateMetricGroup", urlPatterns = {"/api/create-metric-group"})
public class CreateMetricGroup extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(CreateMetricGroup.class.getName());
    
    public static final String PAGE_NAME = "API_CreateMetricGroup";

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
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("doPost");
        
        PrintWriter out = null;
        
        try {
            response.setContentType("application/json");
            String result = processPostRequest(request);
            out = response.getWriter();
            out.println(result);
        } 
        catch(Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {            
            if (out != null) {
                out.close();
            }
        } 
        
    }

    /**
     * Returns a string with success message if the metric group was successfully created,
     * or an error message if the request fails to create the metric group.
     * 
     * @param request servlet request
     * @return success or error message
     */
    protected String processPostRequest(HttpServletRequest request) {
        com.pearson.statsagg.webui.CreateMetricGroup createMetricGroup = new com.pearson.statsagg.webui.CreateMetricGroup();
        JsonObject suspensionJsonObject = Helper.getJsonObjectFromRequestBody(request);
        String result = createMetricGroup.parseAndAlterMetricGroup(suspensionJsonObject);
        return Helper.createSimpleJsonResponse(result);
    }
    
}
