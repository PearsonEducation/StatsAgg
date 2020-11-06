package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.database_objects.alerts.AlertsLogic;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class AlertTemplates extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AlertTemplates.class.getName());
    
    public static final String PAGE_NAME = "Alerts";
    
        /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        processGetRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
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
                
        try {
            String html = Alerts.buildAlertsHtml(true);
            
            Document htmlDocument = Jsoup.parse(html);
            String htmlFormatted  = htmlDocument.toString();
            out = response.getWriter();
            out.println(htmlFormatted);
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
    
    protected void processPostRequest(HttpServletRequest request, HttpServletResponse response) {
        
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
        
        try {
            String operation = Common.getSingleParameterAsString(request, "Operation");

            if ((operation != null) && operation.equals("Enable")) {
                Integer alertId = Integer.parseInt(Common.getSingleParameterAsString(request, "Id"));
                Boolean isEnabled = Boolean.parseBoolean(Common.getSingleParameterAsString(request, "Enabled"));
                Alerts.changeAlertEnabled(alertId, isEnabled);
            }

            if ((operation != null) && operation.equals("Clone")) {
                Integer alertId = Integer.parseInt(request.getParameter("Id"));
                Alerts.cloneAlert(alertId);
            }

            if ((operation != null) && operation.equals("Remove")) {
                Integer alertId = Integer.parseInt(Common.getSingleParameterAsString(request, "Id"));
                Alerts.removeAlert(alertId);
            }

            if ((operation != null) && operation.equals("Acknowledge")) {
                String isAcknowledged_String = request.getParameter("IsAcknowledged");
                Integer alertId = Integer.parseInt(Common.getSingleParameterAsString(request, "Id"));

                try {
                    Boolean isAcknowledged_Boolean = Boolean.parseBoolean(isAcknowledged_String);
                    AlertsLogic.changeAlertAcknowledge(alertId, isAcknowledged_Boolean);
                }
                catch (Exception e) {
                    logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        StatsAggHtmlFramework.redirectAndGet(response, 303, "AlertTemplates");
    }

}
