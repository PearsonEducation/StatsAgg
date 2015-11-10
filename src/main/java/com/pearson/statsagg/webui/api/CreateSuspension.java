package com.pearson.statsagg.webui.api;

import com.pearson.statsagg.utilities.StackTrace;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prashant kumar (Prashant4nov)
 */
@WebServlet(name = "API_CreateSuspension", urlPatterns = {"/api/create-suspension"})
public class CreateSuspension extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(CreateSuspension.class.getName());
    
    public static final String PAGE_NAME = "API_CreateSuspension";
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            JSONObject responseMsg = new JSONObject();
            response.setContentType("application/json");
            PrintWriter out = null;
            String result = processPostRequest(request, new com.pearson.statsagg.webui.CreateSuspension());
            responseMsg.put("response", result);
            out = response.getWriter();
            out.println(responseMsg);
        } 
        catch (IOException ex) {
            logger.error(ex.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(ex));
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
     * Returns a string with success message if suspension is 
     * successfully created or error message if the request fails to create one.
     * 
     * @param request servlet request
     * @param createSuspension CreateSuspension object
     * @return success or error message
     */
    protected String processPostRequest(HttpServletRequest request, com.pearson.statsagg.webui.CreateSuspension createSuspension) {
        String result = null;
        JSONObject suspensionData = Helper.getRequestData(request);

        try {  
            result = createSuspension.parseAndAlterSuspension(suspensionData);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return result;
    }
    
}
