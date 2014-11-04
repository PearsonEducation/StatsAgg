package com.pearson.statsagg.webui;

import com.pearson.statsagg.database.alert_suspensions.AlertSuspension;
import com.pearson.statsagg.database.alert_suspensions.AlertSuspensionsDao;
import com.pearson.statsagg.database.alerts.Alert;
import com.pearson.statsagg.database.alerts.AlertsDao;
import java.io.PrintWriter;
import java.util.Set;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.StackTrace;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "AlertSuspensionAlertAssociations", urlPatterns = {"/AlertSuspensionAlertAssociations"})
public class AlertSuspensionAlertAssociations extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AlertSuspensionAlertAssociations.class.getName());
    
    public static final String PAGE_NAME = "Alert Suspension - Alert Associations";
    
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
        
        response.setContentType("text/html");
        PrintWriter out = null;
    
        String name = request.getParameter("Name");
        String alertSuspensionAlertAssociations = getAlertSuspension_AlertAssociations(name);
                
        try {  
            StringBuilder htmlBuilder = new StringBuilder("");

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            
            String htmlBody = statsAggHtmlFramework.createHtmlBody(
            "<div id=\"page-content-wrapper\">\n" +
            "<!-- Keep all page content within the page-content inset div! -->\n" +
            "  <div class=\"page-content inset\">\n" +
            "    <div class=\"content-header\"> \n" +
            "      <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "    </div> " +
            alertSuspensionAlertAssociations +
            "  </div>\n" +
            "</div>\n");
            
            htmlBuilder.append("<!DOCTYPE html>\n<html>\n").append(htmlHeader).append(htmlBody).append("</html>");
            
            Document htmlDocument = Jsoup.parse(htmlBuilder.toString());
            String htmlFormatted  = htmlDocument.toString();
            out = response.getWriter();
            if (ApplicationConfiguration.isDebugModeEnabled()) out.println(htmlBuilder.toString());
            else out.println(htmlFormatted);
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

    protected String getAlertSuspension_AlertAssociations(String alertSuspensionName) {
        
        if (alertSuspensionName == null) {
            return "";
        }
        
        StringBuilder outputString = new StringBuilder("");
        
        AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao();
        AlertSuspension alertSuspension = alertSuspensionsDao.getAlertSuspensionByName(alertSuspensionName);
        if (alertSuspension == null) return outputString.toString();
        
        outputString.append("<b>Alert Suspension Name</b> = ").append(StatsAggHtmlFramework.htmlEncode(alertSuspension.getName())).append("<br>");

        Map<Integer, Set<Integer>> alertIdAssociationsByAlertSuspensionId = null;
        synchronized(GlobalVariables.alertSuspensionIdAssociationsByAlertId) {
            alertIdAssociationsByAlertSuspensionId = com.pearson.statsagg.alerts.AlertSuspensions.getAlertIdAssociationsByAlertSuspensionId(GlobalVariables.alertSuspensionIdAssociationsByAlertId);
        }
        
        if (alertIdAssociationsByAlertSuspensionId == null) {
            outputString.append("<b>Total Alert Associations</b> = ").append("0");
            return outputString.toString();
        }

        Set<Integer> alertIdAssociations = alertIdAssociationsByAlertSuspensionId.get(alertSuspension.getId());
        if (alertIdAssociations == null) alertIdAssociations = new HashSet<>();
        
        int associationCount = alertIdAssociations.size();
        outputString.append("<b>Total Alert Associations</b> = ").append(associationCount).append("<br><br>");
        if (associationCount <= 0) return outputString.toString();

        List<String> alertNames = new ArrayList<>();

        AlertsDao alertsDao = new AlertsDao(false);
        for (Integer alertId : alertIdAssociations) {
            Alert alert = alertsDao.getAlert(alertId);
            if ((alert == null) || (alert.getName() == null)) continue;
            alertNames.add(alert.getName());
        }
        alertsDao.close();
        
        Collections.sort(alertNames);

        outputString.append("<b>Alert Associations...</b>").append("<br>");
        
        outputString.append("<ul>");
        for (String alertName : alertNames) {
            String alertSuspensionDetailsUrl = "<a href=\"AlertDetails?Name=" + 
                    StatsAggHtmlFramework.urlEncode(alertName) + "\">" + 
                    StatsAggHtmlFramework.htmlEncode(alertName) + "</a>";

            outputString.append("<li>").append(alertSuspensionDetailsUrl).append("</li>");
        }
        outputString.append("</ul>");

        return outputString.toString();
    }

}
