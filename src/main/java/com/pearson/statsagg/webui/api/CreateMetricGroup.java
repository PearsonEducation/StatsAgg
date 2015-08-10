package com.pearson.statsagg.webui.api;

import com.pearson.statsagg.utilities.StackTrace;
import java.io.BufferedReader;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
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
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("doPost");
        try {
          String responseMsg = processPostRequest(request, new com.pearson.statsagg.webui.CreateMetricGroup());
          PrintWriter out = null;
          response.setContentType("application/json");
          out = response.getWriter();
          out.println(responseMsg);
        } catch(Exception e) {
              logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
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
    
    String processPostRequest(HttpServletRequest request, com.pearson.statsagg.webui.CreateMetricGroup createMetricGroup) {
        JSONObject metricData = Helper.getRequestData(request);
        JSONObject responseMsg = new JSONObject();
        response.setContentType("application/json");
        PrintWriter out = null;
        
        try {
            result = createMetricGroup.parseMetricGroup(metricData);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        return result;
    }    
}
