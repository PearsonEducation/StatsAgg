package com.pearson.statsagg.webui.api;

import com.google.gson.JsonObject;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.utilities.StackTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prashant kumar(prashant4nov)
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "API_CreateAlert", urlPatterns = {"/api/create-alert"})
public class CreateAlert extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CreateAlert.class.getName());
    
    public static final String PAGE_NAME = "API_CreateAlert";

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
        
        PrintWriter out = null;
        
        try {
            response.setContentType("application/json");
            String result = processPostRequest(request);
            out = response.getWriter();
            out.println(result);
        } 
        catch (Exception ex) {
            logger.error(ex.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(ex));
        }
        finally {            
            if (out != null) {
                out.close();
            }
        } 
        
    }
    
    /**
     * Returns a string with success message if the alert was successfully created,
     * or an error message if the request fails to create the alert.
     * 
     * @param request servlet request
     * @return success or error message
     */
    protected String processPostRequest(HttpServletRequest request) {
        com.pearson.statsagg.webui.CreateAlert createAlert = new com.pearson.statsagg.webui.CreateAlert();
        JsonObject alertJsonObject = Helper.getJsonObjectFromRequestBody(request);
        String result = createAlert.parseAndAlterAlert(alertJsonObject);
        return Helper.createSimpleJsonResponse(result);
    }
    
}
