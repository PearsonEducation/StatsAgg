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
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.StackTrace;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "AlertAlertSuspensionAssociations", urlPatterns = {"/AlertAlertSuspensionAssociations"})
public class AlertAlertSuspensionAssociations extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AlertAlertSuspensionAssociations.class.getName());
    
    public static final String PAGE_NAME = "Alert - Alert Suspension Associations";
    
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
        String alert_AlertSuspensionAssociations = getAlert_AlertSuspensionAssociations(name);
                
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
            "    <div class=\"statsagg_force_word_wrap\">" +
            alert_AlertSuspensionAssociations +
            "    </div>\n" +
            "  </div>\n" +
            "</div>\n");
            
            htmlBuilder.append("<!DOCTYPE html>\n<html>\n").append(htmlHeader).append(htmlBody).append("</html>");
            
            Document htmlDocument = Jsoup.parse(htmlBuilder.toString());
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

    private String getAlert_AlertSuspensionAssociations(String alertName) {
        
        if (alertName == null) {
            return "";
        }
        
        StringBuilder outputString = new StringBuilder("");
        
        AlertsDao alertsDao = new AlertsDao();
        Alert alert = alertsDao.getAlertByName(alertName);
        if (alert == null) return outputString.toString();
        
        outputString.append("<b>Alert Name</b> = ").append(StatsAggHtmlFramework.htmlEncode(alert.getName())).append("<br>");

        Set<Integer> alertSuspensionIds = GlobalVariables.alertSuspensionIdAssociationsByAlertId.get(alert.getId());

        if (alertSuspensionIds == null) {
            outputString.append("<b>Total Associations</b> = ").append("0");
            return outputString.toString();
        }

        int associationCount = alertSuspensionIds.size();
        outputString.append("<b>Total Associations</b> = ").append(associationCount).append("<br><br>");
        if (associationCount <= 0) return outputString.toString();

        outputString.append("<b>Associations...</b>").append("<br>");

        outputString.append("<ul>");

        Map<String,String> alertSuspensionStrings = new HashMap<>();
        
        AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao(false);
        for (Integer alertSuspensionId : alertSuspensionIds) {
            AlertSuspension alertSuspension = alertSuspensionsDao.getAlertSuspension(alertSuspensionId);
            if ((alertSuspension == null) || (alertSuspension.getName() == null)) continue;

            String alertSuspensionDetailsUrl = "<a href=\"AlertSuspensionDetails?Name=" + 
                    StatsAggHtmlFramework.urlEncode(alertSuspension.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(alertSuspension.getName()) + "</a>";

            boolean isAlertSuspensionActive = AlertSuspension.isAlertSuspensionActive(alertSuspension);

            StringBuilder status = new StringBuilder("");
            if (isAlertSuspensionActive) status.append("(active");
            else status.append("(inactive");
            if ((alertSuspension.isSuspendNotificationOnly() != null) && alertSuspension.isSuspendNotificationOnly()) status.append(", suspend notification only");
            else if ((alertSuspension.isSuspendNotificationOnly() != null) && !alertSuspension.isSuspendNotificationOnly()) status.append(", suspend entire alert");
            status.append(")");

            if (isAlertSuspensionActive) alertSuspensionStrings.put(alertSuspension.getName(), "<li>" + "<b>" + alertSuspensionDetailsUrl + "&nbsp" + status.toString() + "</b>" + "</li>");
            else alertSuspensionStrings.put(alertSuspension.getName(), "<li>" + alertSuspensionDetailsUrl + "&nbsp" + status.toString() + "</li>");
        }
        alertSuspensionsDao.close();

        List<String> sortedAlertSuspensionStrings = new ArrayList<>(alertSuspensionStrings.keySet());
        Collections.sort(sortedAlertSuspensionStrings);
        
        for (String alertSuspensionString : sortedAlertSuspensionStrings) {
            String alertSuspensionOutputString = alertSuspensionStrings.get(alertSuspensionString);
            outputString.append(alertSuspensionOutputString);
        }

        outputString.append("</ul>");
    
        return outputString.toString();
    }

}
