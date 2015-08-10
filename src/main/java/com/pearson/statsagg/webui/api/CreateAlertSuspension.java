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
 * @author prashant kumar (Prashant4nov)
 */
@WebServlet(name = "API_CreateAlertSuspension", urlPatterns = {"/api/create-alertsuspension"})
public class CreateAlertSuspension extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(CreateAlertSuspension.class.getName());
    
    public static final String PAGE_NAME = "API_CreateAlertSuspension";
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        processPostRequest(request, response);
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

    protected void processPostRequest(HttpServletRequest request, HttpServletResponse response) {

        if ((request == null) || (response == null)) {
            return;
        }
        
        JSONObject alertSuspensionData = Helper.getRequestData(request);
        JSONObject responseMsg = new JSONObject();
        response.setContentType("application/json");
        PrintWriter out = null;
        
        try {
            com.pearson.statsagg.webui.CreateAlertSuspension createAlertSuspension = new com.pearson.statsagg.webui.CreateAlertSuspension();
            String result = createAlertSuspension.parseAndAlterAlertSuspension(alertSuspensionData);
            responseMsg.put("response", result);
            out = response.getWriter();
            out.println(responseMsg);
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

}
