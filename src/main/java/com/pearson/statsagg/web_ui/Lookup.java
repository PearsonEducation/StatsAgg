package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupsDao;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroupsDao;
import com.pearson.statsagg.database_objects.pagerduty_services.PagerdutyServicesDao;
import com.pearson.statsagg.database_objects.variable_set_list.VariableSetListsDao;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class Lookup extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(Lookup.class.getName());
    
    public static final String PAGE_NAME = "Lookup";
    
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        processGetRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        processGetRequest(request, response);
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
    
    protected void processGetRequest(HttpServletRequest request, HttpServletResponse response) {
        
        if ((request == null) || (response == null)) {
            return;
        }
        
        try {  
            request.setCharacterEncoding("UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        } 
        
        PrintWriter out = null;
        
        String type = request.getParameter("Type");
        String query = request.getParameter("Query");
        
        try {  
            String results = "[]";
            
            if (type != null) {
                if (type.equals("AlertName")) results = createAlertNamesJson(query);
                else if (type.equals("MetricGroupName")) results = createMetricGroupNamesJson(query);
                else if (type.equals("NotificationGroupName")) results = createNotificationGroupNamesJson(query);
                else if (type.equals("PagerDutyServiceName")) results = createPagerdutyServiceNamesJson(query);
                else if (type.equals("VariableSetListName")) results = createVariableSetListNamesJson(query);
            }
            
            out = response.getWriter();
            out.println(results);
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
    
    protected String createAlertNamesJson(String alertNamesQuery) {
        
        if (alertNamesQuery == null) {
            return "";
        }
        
        List<String> alertNames = AlertsDao.getAlertNames(DatabaseConnections.getConnection(), true, alertNamesQuery, 10);
        
        StringBuilder json = new StringBuilder();
        
        json.append("[");
        
        int i = 1;
        for (String name : alertNames) {
            json.append("{");
            
            json.append("\"HtmlValue\":\"").append(StringEscapeUtils.escapeJson(StatsAggHtmlFramework.htmlEncode(name))).append("\",");
            json.append("\"Value\":\"").append(StringEscapeUtils.escapeJson(name)).append("\"");
            
            json.append("}");
            
            if (i < alertNames.size()) json.append(",");
            i++;
        }
        
        json.append("]");
    
        return json.toString();
    }
    
    protected String createMetricGroupNamesJson(String metricGroupNamesQuery) {
        
        if (metricGroupNamesQuery == null) {
            return "";
        }
        
        List<String> metricGroupsNames = MetricGroupsDao.getMetricGroupNames(DatabaseConnections.getConnection(), true, metricGroupNamesQuery, 10);
        
        StringBuilder json = new StringBuilder();
        
        json.append("[");
        
        int i = 1;
        for (String name : metricGroupsNames) {
            json.append("{");
            
            json.append("\"HtmlValue\":\"").append(StringEscapeUtils.escapeJson(StatsAggHtmlFramework.htmlEncode(name))).append("\",");
            json.append("\"Value\":\"").append(StringEscapeUtils.escapeJson(name)).append("\"");
            
            json.append("}");
            
            if (i < metricGroupsNames.size()) json.append(",");
            i++;
        }
        
        json.append("]");
    
        return json.toString();
    }

    protected String createNotificationGroupNamesJson(String notificationGroupNamesQuery) {
        
        if (notificationGroupNamesQuery == null) {
            return "";
        }
        
        List<String> notificationGroupsNames = NotificationGroupsDao.getNotificationGroupNames(DatabaseConnections.getConnection(), true, notificationGroupNamesQuery, 10);
        
        StringBuilder json = new StringBuilder();
        
        json.append("[");
        
        int i = 1;
        for (String name : notificationGroupsNames) {
            json.append("{");
            
            json.append("\"HtmlValue\":\"").append(StringEscapeUtils.escapeJson(StatsAggHtmlFramework.htmlEncode(name))).append("\",");
            json.append("\"Value\":\"").append(StringEscapeUtils.escapeJson(name)).append("\"");
            
            json.append("}");
            
            if (i < notificationGroupsNames.size()) json.append(",");
            i++;
        }
        
        json.append("]");
    
        return json.toString();
    }
    
    protected String createPagerdutyServiceNamesJson(String pagerdutyServiceNamesQuery) {
        
        if (pagerdutyServiceNamesQuery == null) {
            return "";
        }
        
        List<String> pagerdutyServiceNames = PagerdutyServicesDao.getPagerdutyServiceNames(DatabaseConnections.getConnection(), true, pagerdutyServiceNamesQuery, 10);
        
        StringBuilder json = new StringBuilder();
        
        json.append("[");
        
        int i = 1;
        for (String name : pagerdutyServiceNames) {
            json.append("{");
            
            json.append("\"HtmlValue\":\"").append(StringEscapeUtils.escapeJson(StatsAggHtmlFramework.htmlEncode(name))).append("\",");
            json.append("\"Value\":\"").append(StringEscapeUtils.escapeJson(name)).append("\"");
            
            json.append("}");
            
            if (i < pagerdutyServiceNames.size()) json.append(",");
            i++;
        }
        
        json.append("]");
    
        return json.toString();
    }
    
    protected String createVariableSetListNamesJson(String variableSetListNamesQuery) {
        
        if (variableSetListNamesQuery == null) {
            return "";
        }
        
        List<String> variableSetListNames = VariableSetListsDao.getVariableSetListNames(DatabaseConnections.getConnection(), true, variableSetListNamesQuery, 10);
        
        StringBuilder json = new StringBuilder();
        
        json.append("[");
        
        int i = 1;
        for (String name : variableSetListNames) {
            json.append("{");
            
            json.append("\"HtmlValue\":\"").append(StringEscapeUtils.escapeJson(StatsAggHtmlFramework.htmlEncode(name))).append("\",");
            json.append("\"Value\":\"").append(StringEscapeUtils.escapeJson(name)).append("\"");
            
            json.append("}");
            
            if (i < variableSetListNames.size()) json.append(",");
            i++;
        }
        
        json.append("]");
    
        return json.toString();
    }
    
}