package com.pearson.statsagg.webui.api;

import com.google.gson.JsonObject;
import com.pearson.statsagg.database_objects.metric_group.MetricGroup;
import com.pearson.statsagg.database_objects.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database_objects.metric_group_regex.MetricGroupRegex;
import com.pearson.statsagg.database_objects.metric_group_regex.MetricGroupRegexesDao;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTag;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTagsDao;
import com.pearson.statsagg.utilities.JsonUtils;
import com.pearson.statsagg.utilities.StackTrace;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prashant kumar (prashant4nov)
 * @author Jeffrey Schmidt
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
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        processRequest(request, response);
    }
    
    private void processRequest(HttpServletRequest request, HttpServletResponse response) {
        
        PrintWriter out = null;

        try {
            String json = getMetricGroupDetails(request);
            response.setContentType("application/json");
            out = response.getWriter();
            out.println(json);
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

    /**
     * Returns a json string containing the details of the requested metric group.
     * 
     * @param request servlet request
     * @return details of the requested metric group
     */
    protected String getMetricGroupDetails(HttpServletRequest request) {
        
        if (request == null) {
            return Helper.ERROR_UNKNOWN_JSON;
        }
        
        try {
            Integer metricGroupId = null;
            String metricGroupName = null;

            if (request.getParameter("id") != null) metricGroupId = Integer.parseInt(request.getParameter("id"));
            if (request.getParameter("name") != null) metricGroupName = request.getParameter("name");

            if ((metricGroupId == null) && (metricGroupName == null)) {
                JsonObject jsonObject = Helper.getJsonObjectFromRequestBody(request);
                metricGroupId = JsonUtils.getIntegerFieldFromJsonObject(jsonObject, "id");
                metricGroupName = JsonUtils.getStringFieldFromJsonObject(jsonObject, "name");
            }

            MetricGroup metricGroup = null;
            MetricGroupsDao metricGroupsDao = new MetricGroupsDao(false);
            if (metricGroupId != null) metricGroup = metricGroupsDao.getMetricGroup(metricGroupId);
            else if (metricGroupName != null) metricGroup = metricGroupsDao.getMetricGroupByName(metricGroupName);
            
            List<MetricGroupRegex> metricGroupRegexes = new ArrayList<>();
            List<MetricGroupTag> metricGroupTags = new ArrayList<>();
            if (metricGroup != null) {
                MetricGroupRegexesDao metricGroupRegexesDao = new MetricGroupRegexesDao(metricGroupsDao.getDatabaseInterface());
                metricGroupRegexes = metricGroupRegexesDao.getMetricGroupRegexesByMetricGroupId(metricGroup.getId());
                MetricGroupTagsDao metricGroupTagsDao = new MetricGroupTagsDao(metricGroupsDao.getDatabaseInterface());
                metricGroupTags = metricGroupTagsDao.getMetricGroupTagsByMetricGroupId(metricGroup.getId());
            }
            
            metricGroupsDao.close();
            
            if (metricGroup != null) return MetricGroup.getJsonString_ApiFriendly(metricGroup, metricGroupRegexes, metricGroupTags);
            else return Helper.ERROR_NOTFOUND_JSON;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));        
        }
        
        return Helper.ERROR_UNKNOWN_JSON;
    }

}
