package com.pearson.statsagg.webui.api;

import com.pearson.statsagg.utilities.StackTrace;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author prashant4nov (Prashant Kumar)
 */
@WebServlet(name = "API_Remove_Suspension", urlPatterns = {"/api/suspension-remove"})
public class RemoveSuspension extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(RemoveSuspension.class.getName());
    
    public static final String PAGE_NAME = "API_Remove_Suspension";
 
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("doPost");
        
        try {    
            PrintWriter out = null;
            String returnString = processPostRequest(request, new com.pearson.statsagg.webui.Suspensions());       
            JSONObject responseMsg = new JSONObject();
            responseMsg.put("response", returnString);
            response.setContentType("application/json");
            out = response.getWriter();
            out.println(responseMsg);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }   
    }

    /**
     * Returns a string with success message if suspension is deleted 
     * successfully or error message if the request fails to delete the suspension.
     * 
     * @param request servlet request
     * @param suspensions Suspensions object
     * @return success or error message
     */
    protected String processPostRequest(HttpServletRequest request, com.pearson.statsagg.webui.Suspensions suspensions) {

        String returnString = null;
        String suspensionName = null;

        try {
            logger.debug(request.getParameter(Helper.name));

            if (request.getParameter(Helper.name) != null) {
                suspensionName = request.getParameter(Helper.name);
            }

            returnString = suspensions.removeSuspension(suspensionName);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }

        return returnString;
    }

}
