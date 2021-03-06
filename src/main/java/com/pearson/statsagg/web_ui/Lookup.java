package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database_objects.notifications.NotificationGroupsDao;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
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
@WebServlet(name = "Lookup", urlPatterns = {"/Lookup"})
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
            response.setContentType("text/html");
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
        
        AlertsDao alertsDao = new AlertsDao();
        List<String> alertNames = alertsDao.getAlertNames(alertNamesQuery, 10);
        
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
        
        MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
        List<String> metricGroupsNames = metricGroupsDao.getMetricGroupNames(metricGroupNamesQuery, 10);
        
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
        
        NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
        List<String> notificationGroupsNames = notificationGroupsDao.getNotificationGroupNames(notificationGroupNamesQuery, 10);
        
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
    
}