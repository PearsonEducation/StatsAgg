package com.pearson.statsagg.webui.api;

import com.pearson.statsagg.database_objects.suspensions.Suspension;
import com.pearson.statsagg.database_objects.suspensions.SuspensionsDao;
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
@WebServlet(name="API_SuspensionDetails", urlPatterns={"/api/suspension-details"})
public class SuspensionDetails extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(SuspensionDetails.class.getName());
    
    public static final String PAGE_NAME = "API_SuspensionDetails";
    
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
            JSONObject json = getSuspensionDetails(request, new SuspensionsDao());
            response.setContentType("application/json");
            PrintWriter out;
            out = response.getWriter();
            out.println(json);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }

    /**
     * Returns a json object containing the details of the requested suspension.
     * 
     * @param request servlet request
     * @param suspensionsDao SuspensionsDao object
     * @return details of the requested suspension
     */
    protected JSONObject getSuspensionDetails(HttpServletRequest request, SuspensionsDao suspensionsDao) {
        
        JSONObject suspensionDetails = new JSONObject();
        int suspensionId = 0;
        
        try {
            if (request.getParameter(Helper.id) != null) {
                suspensionId = Integer.parseInt(request.getParameter(Helper.id));
            }
            
            Suspension suspension = suspensionsDao.getSuspension(suspensionId);
            
            if (suspension != null) {
                if (suspension.getAlertId() != null) suspensionDetails.put("AlertId", suspension.getAlertId());
                if (suspension.getId() != null) suspensionDetails.put("Id", suspension.getId());
                if (suspension.getDescription() != null) suspensionDetails.put("Description", suspension.getDescription());
                if (suspension.getDuration() != null) suspensionDetails.put("Duration", suspension.getDuration());
                if (suspension.getMetricGroupTagsExclusive() != null) suspensionDetails.put("MetricGroupTagsExclusive", suspension.getMetricGroupTagsExclusive());
                if (suspension.getMetricGroupTagsInclusive() != null) suspensionDetails.put("MetricGroupTagsInclusive", suspension.getMetricGroupTagsInclusive());
                if (suspension.getMetricGroupTagsInclusive() != null) suspensionDetails.put("MetricSuspensionRegexes", suspension.getMetricSuspensionRegexes());
                if (suspension.getName() != null) suspensionDetails.put("Name", suspension.getName());
                if (suspension.getStartDate() != null) suspensionDetails.put("StartDate", suspension.getStartDate());
                if (suspension.getStartTime() != null) suspensionDetails.put("StartTime", suspension.getStartTime());
                if (suspension.getDeleteAtTimestamp() != null) suspensionDetails.put("DeleteAtTimestamp", suspension.getDeleteAtTimestamp());
                if (suspension.getDurationTimeUnit() != null) suspensionDetails.put("Duration", suspension.getDuration());
                
                if (suspension.getSuspendBy() != null) {
                    suspensionDetails.put("SuspendBy", suspension.getSuspendBy());
                }
            }
            else {
                suspensionDetails.put(Helper.error, Helper.noResult);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            suspensionDetails.put(Helper.error, Helper.errorMsg);
        }
        
        return suspensionDetails;
    }
    
}
