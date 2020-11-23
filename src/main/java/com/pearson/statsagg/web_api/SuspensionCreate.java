package com.pearson.statsagg.web_api;

import com.google.gson.JsonObject;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prashant kumar (Prashant4nov)
 * @author Jeffrey Schmidt
 */
public class SuspensionCreate extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(SuspensionCreate.class.getName());
    
    public static final String PAGE_NAME = "API_Suspension_Create";
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        
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
            String result = processPostRequest(request);
            out = response.getWriter();
            out.println(result);
        } 
        catch (IOException ex) {
            logger.error(ex.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(ex));
        }
        finally {            
            if (out != null) {
                out.close();
            }
        } 
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
    
    /**
     * Returns a string with success message if the suspension was successfully created,
     * or an error message if the request fails to create the suspension.
     * 
     * @param request servlet request
     * @return success or error message
     */
    protected String processPostRequest(HttpServletRequest request) {
        com.pearson.statsagg.web_ui.CreateSuspension createSuspension = new com.pearson.statsagg.web_ui.CreateSuspension();
        JsonObject jsonObject = Helper.getJsonObjectFromRequestBody(request);
        String result = createSuspension.parseAndAlterSuspension(jsonObject);
        return Helper.createSimpleJsonResponse(result);
    }
    
}
