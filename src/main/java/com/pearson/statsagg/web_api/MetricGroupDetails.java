package com.pearson.statsagg.web_api;

import com.google.gson.JsonObject;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.database_objects.metric_group.MetricGroup;
import com.pearson.statsagg.database_objects.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database_objects.metric_group_regex.MetricGroupRegex;
import com.pearson.statsagg.database_objects.metric_group_regex.MetricGroupRegexesDao;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTag;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTagsDao;
import com.pearson.statsagg.configuration.ApplicationConfiguration;
import com.pearson.statsagg.utilities.json_utils.JsonUtils;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prashant kumar (prashant4nov)
 * @author Jeffrey Schmidt
 */
public class MetricGroupDetails extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricGroupDetails.class.getName());
    
    public static final String PAGE_NAME = "API_MetricGroup_Details";
    
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
            request.setCharacterEncoding("UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        try {
            String json = getMetricGroupDetails(request);
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
            Boolean includeMetricAssociations = false;
            
            if (request.getParameter("id") != null) metricGroupId = Integer.parseInt(request.getParameter("id"));
            if (request.getParameter("name") != null) metricGroupName = request.getParameter("name");
            if (request.getParameter("include_metric_associations") != null) includeMetricAssociations = Boolean.parseBoolean(request.getParameter("include_metric_associations"));
            
            if ((metricGroupId == null) && (metricGroupName == null)) {
                JsonObject jsonObject = Helper.getJsonObjectFromRequestBody(request);
                metricGroupId = JsonUtils.getIntegerFieldFromJsonObject(jsonObject, "id");
                metricGroupName = JsonUtils.getStringFieldFromJsonObject(jsonObject, "name");
                includeMetricAssociations = JsonUtils.getBooleanFieldFromJsonObject(jsonObject, "include_metric_associations");
            }

            MetricGroup metricGroup = null;
            List<MetricGroupRegex> metricGroupRegexes = new ArrayList<>();
            List<MetricGroupTag> metricGroupTags = new ArrayList<>();
            Connection connection = DatabaseConnections.getConnection();

            try {
                if (metricGroupId != null) metricGroup = MetricGroupsDao.getMetricGroup(connection, false, metricGroupId);
                else if (metricGroupName != null) metricGroup = MetricGroupsDao.getMetricGroup(connection, false, metricGroupName);

                if (metricGroup != null) {
                    metricGroupRegexes = MetricGroupRegexesDao.getMetricGroupRegexesByMetricGroupId(connection, false, metricGroup.getId());
                    metricGroupTags = MetricGroupTagsDao.getMetricGroupTagsByMetricGroupId(connection, false, metricGroup.getId());
                }
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
            finally {
                DatabaseUtils.cleanup(connection);
            }
            
            if ((metricGroup != null) && (includeMetricAssociations != null) && includeMetricAssociations) {
                return MetricGroup.getJsonString_ApiFriendly(metricGroup, metricGroupRegexes, metricGroupTags, true, ApplicationConfiguration.getMetricGroupApiMaxMetricAssociations());
            }
            else if (metricGroup != null) {
                return MetricGroup.getJsonString_ApiFriendly(metricGroup, metricGroupRegexes, metricGroupTags, false, -1);
            }
            else return Helper.ERROR_NOTFOUND_JSON;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));        
        }
        
        return Helper.ERROR_UNKNOWN_JSON;
    }

}
