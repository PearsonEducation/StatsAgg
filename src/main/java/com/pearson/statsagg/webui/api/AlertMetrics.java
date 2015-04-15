package com.pearson.statsagg.webui.api;

import com.pearson.statsagg.database.alerts.Alert;
import com.pearson.statsagg.database.alerts.AlertsDao;
import com.pearson.statsagg.database.metric_group.MetricGroup;
import com.pearson.statsagg.database.metric_group.MetricGroupsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.StackTrace;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name="AlertMetrics", urlPatterns={"/api/AlertMetrics"})
public class AlertMetrics extends HttpServlet {
   
    private static final Logger logger = LoggerFactory.getLogger(AlertMetrics.class.getName());
    
    public static final String PAGE_NAME = "AlertMetrics";
    
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
        
        response.setContentType("application/json");
        PrintWriter out = null;

        try {  
            Alert alert = getAlert(request);
            MetricGroup metricGroup = getMetricGroup(alert);
            String json = createJson(alert, metricGroup);            

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
    
    private Alert getAlert(HttpServletRequest request) {
        
        if (request == null) {
            return null;
        }
        
        AlertsDao alertsDao = new AlertsDao(false);
        Alert alert = null;

        try {
            // should be equal to either "AlertName" or "AlertId"
            String queryField = request.getParameter("QueryField");

            if ((queryField != null) && queryField.equalsIgnoreCase("AlertName")) {
                String alertName = request.getParameter("AlertName");
                alert = alertsDao.getAlertByName(alertName);
            }
            else if ((queryField != null) && queryField.equalsIgnoreCase("AlertId")) {
                String alertId = request.getParameter("AlertId");
                alert = alertsDao.getAlert(Integer.valueOf(alertId));
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    
        alertsDao.close();
        
        return alert;
    }
    
    private MetricGroup getMetricGroup(Alert alert) {

        if ((alert == null) || (alert.getMetricGroupId() == null)) {
            return null;
        }
        
        MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
        MetricGroup metricGroup = metricGroupsDao.getMetricGroup(alert.getMetricGroupId());
                
        return metricGroup;
    }
    
    private String createJson(Alert alert, MetricGroup metricGroup) {
        
        if ((alert == null) || (alert.getName() == null) || (alert.getMetricGroupId() == null) ||
                (metricGroup == null) || (metricGroup.getId() == null) || (metricGroup.getName() == null)) {
            return "[]";
        }

        StringBuilder json = new StringBuilder();
        
        json.append("[{");
        json.append("\"Alert_Name\":\"").append(alert.getName()).append("\",");
        json.append("\"Alert_Id\":").append(alert.getId()).append(",");
        json.append("\"Metric_Group_Name\":\"").append(metricGroup.getName()).append("\",");
        json.append("\"Metric_Group_Id\":").append(alert.getMetricGroupId()).append(",");
        
        
        json.append("\"Triggered_Caution_Metrics\":[");
        Set<String> cautionTriggeredMetricKeys = GlobalVariables.activeCautionAlertMetricValues.keySet();
        if (cautionTriggeredMetricKeys != null) {
            List<String> cautionTriggeredMetricKeys_NoSuffix_ScopedToAlertId = new ArrayList<>();
            
            for (String metricKey : cautionTriggeredMetricKeys) {
                String suffix = ("-" + alert.getId());
                if (metricKey.endsWith(suffix)) cautionTriggeredMetricKeys_NoSuffix_ScopedToAlertId.add(StringUtils.removeEnd(metricKey,suffix));
            }
            
            int i = 1;
            for (String metricKey : cautionTriggeredMetricKeys_NoSuffix_ScopedToAlertId) {
                json.append("\"").append(metricKey).append("\"");
                if (i < cautionTriggeredMetricKeys_NoSuffix_ScopedToAlertId.size()) json.append(",");
                i++;
            }
        }
        json.append("],");
        
        
        json.append("\"Triggered_Danger_Metrics\":[");
        Set<String> dangerTriggeredMetricKeys = GlobalVariables.activeDangerAlertMetricValues.keySet();
        if (dangerTriggeredMetricKeys != null) {
            List<String> dangerTriggeredMetricKeys_NoSuffix_ScopedToAlertId = new ArrayList<>();
            
            for (String metricKey : dangerTriggeredMetricKeys) {
                String suffix = ("-" + alert.getId());
                if (metricKey.endsWith(suffix)) dangerTriggeredMetricKeys_NoSuffix_ScopedToAlertId.add(StringUtils.removeEnd(metricKey,suffix));
            }
            
            int i = 1;
            for (String metricKey : dangerTriggeredMetricKeys_NoSuffix_ScopedToAlertId) {
                json.append("\"").append(metricKey).append("\"");
                if (i < dangerTriggeredMetricKeys_NoSuffix_ScopedToAlertId.size()) json.append(",");
                i++;
            }
        }
        json.append("],");
        
        
        json.append("\"Metric_Group_Metrics\":[");
        Set<String> metricGroupMetricKeys = GlobalVariables.matchingMetricKeysAssociatedWithMetricGroup.get(alert.getMetricGroupId());
        if (metricGroupMetricKeys != null) {
            int i = 1;
            for (String metricKey : metricGroupMetricKeys) {
                json.append("\"").append(metricKey).append("\"");
                if (i < metricGroupMetricKeys.size()) json.append(",");
                i++;
            }
        }
        json.append("]");
    
        json.append("}]");
        
        return json.toString();
    }
    
}
