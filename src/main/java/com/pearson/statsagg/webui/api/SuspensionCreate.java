package com.pearson.statsagg.webui.api;

import com.google.gson.JsonObject;
import com.pearson.statsagg.utilities.StackTrace;
import java.io.IOException;
import java.io.PrintWriter;
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
@WebServlet(name = "API_Suspension_Create", urlPatterns = {"/api/suspension-create"})
public class SuspensionCreate extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(SuspensionCreate.class.getName());
    
    public static final String PAGE_NAME = "API_Suspension_Create";
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        
        PrintWriter out = null;

        try {
            response.setContentType("application/json");
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
        com.pearson.statsagg.webui.CreateSuspension createSuspension = new com.pearson.statsagg.webui.CreateSuspension();
        JsonObject suspensionJsonObject = Helper.getJsonObjectFromRequestBody(request);
        String result = createSuspension.parseAndAlterSuspension(suspensionJsonObject);
        return Helper.createSimpleJsonResponse(result);
    }
    
}
