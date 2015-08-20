package com.pearson.statsagg.webui.api;

import com.pearson.statsagg.database_objects.metric_group.MetricGroup;
import com.pearson.statsagg.database_objects.metric_group.MetricGroupsDao;
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
 * @author prashant kumar (prashant4nov)
 */
@WebServlet(name="API_MetricGroupDetails", urlPatterns={"/api/metric-group-details"})
public class MetricGroupDetails extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricGroupDetails.class.getName());
    
    public static final String PAGE_NAME = "API_MetricGroupDetails";
    
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
            JSONObject json = getMetricGroup(request, new MetricGroupsDao());       
            PrintWriter out = null;
            response.setContentType("application/json");
            out = response.getWriter();
            out.println(json);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }

    /**
     * Returns a json object containing the details of the requested metric group.
     * 
     * @param request servlet request
     * @param metricGroupsDao MetricGroupsDao object
     * @return details of the requested metric group
     */
    protected JSONObject getMetricGroup(HttpServletRequest request, MetricGroupsDao metricGroupsDao) {
        logger.debug("getMetricGroup");
        logger.debug(PAGE_NAME);
        JSONObject metricGroupDetails = new JSONObject();
        int metricGroupId = 0;
        try {
            if (request.getParameter(Helper.id) != null) {
                metricGroupId = Integer.parseInt(request.getParameter(Helper.id));
            }
            
            MetricGroup metricGroup = metricGroupsDao.getMetricGroup(metricGroupId);
            
            if (metricGroup != null) {
                if (metricGroup.getId() != null) {
                    metricGroupDetails.put("id", metricGroup.getId());
                }
                if (metricGroup.getName() != null) {
                    metricGroupDetails.put("name", metricGroup.getName());
                }
                if (metricGroup.getDescription() != null) {
                    metricGroupDetails.put("description", metricGroup.getDescription());
                }
            }
            else {
                metricGroupDetails.put(Helper.error, Helper.noResult);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            metricGroupDetails.put(Helper.error, Helper.errorMsg);
        }
        
        return metricGroupDetails;
    }
    
}
