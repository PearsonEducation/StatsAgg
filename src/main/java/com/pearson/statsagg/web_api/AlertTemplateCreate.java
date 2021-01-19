package com.pearson.statsagg.web_api;

import com.google.gson.JsonObject;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.web_ui.CreateAlertTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prashant kumar(prashant4nov)
 * @author Jeffrey Schmidt
 */
public class AlertTemplateCreate extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AlertTemplateCreate.class.getName());
    
    public static final String PAGE_NAME = "API_Alert_Template_Create";

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
     * Returns a string with success message if the alert template was successfully created,
     * or an error message if the request fails to create the alert template.
     * 
     * @param request servlet request
     * @return success or error message
     */
    protected String processPostRequest(HttpServletRequest request) {
        JsonObject jsonObject = Helper.getJsonObjectFromRequestBody(request);
        String result = CreateAlertTemplate.parseAndAlterAlertTemplate(jsonObject);
        return Helper.createSimpleJsonResponse(result);
    }
    
}
