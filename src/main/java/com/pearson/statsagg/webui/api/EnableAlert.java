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
@WebServlet(name = "API_Enable_Alert", urlPatterns = {"/api/alert-enable"})
public class EnableAlert extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(EnableAlert.class.getName());
    
    public static final String PAGE_NAME = "API_Enable_Alert";
 
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
        processPostRequest(request, response);
  
    }

    protected void processPostRequest(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("Enable/Disable alert request");

        if ((request == null) || (response == null)) {
            return;
        }
            
        try {
            String alertName = null;
            logger.debug(request.getParameter(Helper.name));
            
            if (request.getParameter(Helper.name) != null) {
                alertName = request.getParameter(Helper.name);
            }
            
            Boolean isEnabled = Boolean.parseBoolean(request.getParameter("Enabled"));
            com.pearson.statsagg.webui.Alerts alert = new com.pearson.statsagg.webui.Alerts();
            String returnString = alert.changeAlertEnabled(alertName, isEnabled);
            
            JSONObject responseMsg = new JSONObject();
            responseMsg.put("response", returnString);
            response.setContentType("application/json");
            PrintWriter out = null;
            out = response.getWriter();
            out.println(responseMsg);
        } 
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
}
