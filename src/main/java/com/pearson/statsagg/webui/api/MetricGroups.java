package com.pearson.statsagg.webui.api;

import com.pearson.statsagg.database.metric_group.MetricGroupsDao;
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
 * @author Prashant Kumar
 */
@WebServlet(name="MetricGroupsJson", urlPatterns={"/api/metricgroups"})
public class MetricGroups extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroups.class.getName());
    
    public static final String PAGE_NAME = "AlertsList";
 
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("doGet");
        try {    
            JSONObject json = getMetricGroupsJson(request, new MetricGroupsDao());       
            PrintWriter out = null;
            response.setContentType("application/json");
            out = response.getWriter();
            out.println(json);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }     
    }

     public JSONObject getMetricGroupsJson(HttpServletRequest request, MetricGroupsDao metricGroupsDao) {
        logger.debug("getMetricGroupsJson");
        JSONObject errorMsg = null;
        JSONObject metricGroupsJson = null;
        int pageNumber = 0, pageSize = 0;
        
        try {
            if (request.getParameter(Common.pageNumber) != null) {
                pageNumber = Integer.parseInt(request.getParameter(Common.pageNumber));
            }

            if (request.getParameter(Common.pageSize) != null) {
                pageSize = Integer.parseInt(request.getParameter(Common.pageSize));
            }
            
            metricGroupsJson = metricGroupsDao.getMetricGroups(pageNumber*pageSize, pageSize);
        } catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            errorMsg = new JSONObject();
            errorMsg.put(Common.error, Common.errorMsg);
        }
        if (metricGroupsJson != null) return metricGroupsJson;
        else if (errorMsg != null) return errorMsg;
        else return null;
    }
}
