package com.pearson.statsagg.webui.api;

import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.utilities.StackTrace;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prashant kumar(prashant4nov)
 */
@WebServlet(name = "API_CreateAlert", urlPatterns = {"/api/create-alert"})
public class CreateAlert extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CreateAlert.class.getName());
    
    public static final String PAGE_NAME = "API_CreateAlert";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            JSONObject responseMsg = new JSONObject();
            response.setContentType("application/json");
            PrintWriter out = null;
            String result = processPostRequest(request, new com.pearson.statsagg.webui.CreateAlert());
            responseMsg.put("response", result);
            out = response.getWriter();
            out.println(responseMsg);
        } 
        catch (Exception ex) {
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
    
    String processPostRequest(HttpServletRequest request, com.pearson.statsagg.webui.CreateAlert createAlert) {
        String result = null;
        
        try {
            JSONObject alertData = Helper.getRequestData(request);
            result = createAlert.parseAndAlterAlert(alertData);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return result;
    }
    
}
