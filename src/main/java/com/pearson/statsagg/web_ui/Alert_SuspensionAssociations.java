package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.database_objects.suspensions.Suspension;
import com.pearson.statsagg.database_objects.suspensions.SuspensionsDao;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import java.io.PrintWriter;
import java.util.Set;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import java.sql.Connection;
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
public class Alert_SuspensionAssociations extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(Alert_SuspensionAssociations.class.getName());
    
    public static final String PAGE_NAME = "Alert - Suspension Associations";
    
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
    
        String name = request.getParameter("Name");
        boolean excludeNavbar = StringUtilities.isStringValueBooleanTrue(request.getParameter("ExcludeNavbar"));
        String alert_SuspensionAssociations = getAlert_SuspensionAssociations(name, excludeNavbar);
                
        try {  
            StringBuilder htmlBuilder = new StringBuilder();

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            
            String htmlBody = statsAggHtmlFramework.createHtmlBody(
            "<div id=\"page-content-wrapper\">\n" +
            "<!-- Keep all page content within the page-content inset div! -->\n" +
            "  <div class=\"page-content inset statsagg_page_content_font\">\n" +
            "    <div class=\"content-header\"> \n" +
            "      <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "    </div> " +
            "    <div class=\"statsagg_force_word_wrap\">" +
            alert_SuspensionAssociations +
            "    </div>\n" +
            "  </div>\n" +
            "</div>\n",
            excludeNavbar);
            
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

    private String getAlert_SuspensionAssociations(String alertName, boolean excludeNavbar) {
        
        if (alertName == null) {
            return "<b>No alert specified</b>";
        }
 
        Alert alert = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, alertName);
        if (alert == null) return "<b>Alert not found</b>";
        
        StringBuilder outputString = new StringBuilder();
        
        outputString.append("<b>Alert Name</b> = ").append(StatsAggHtmlFramework.htmlEncode(alert.getName())).append("<br>");

        Set<Integer> suspensionIds;
        synchronized(GlobalVariables.suspensionIdAssociationsByAlertId) {
            suspensionIds = GlobalVariables.suspensionIdAssociationsByAlertId.get(alert.getId());
        }
        
        if (suspensionIds == null) {
            outputString.append("<b>Total Associations</b> = ").append("0");
            return outputString.toString();
        }

        int associationCount = suspensionIds.size();
        outputString.append("<b>Total Associations</b> = ").append(associationCount).append("<br><br>");
        if (associationCount <= 0) return outputString.toString();

        outputString.append("<b>Associations...</b>").append("<br>");

        outputString.append("<ul>");

        Map<String,String> suspensionStrings = new HashMap<>();
        
        Connection connection = DatabaseConnections.getConnection();
        
        try {
            for (Integer suspensionId : suspensionIds) {
                if (suspensionId == null) continue;

                Suspension suspension = SuspensionsDao.getSuspension(connection, false, suspensionId);
                if ((suspension == null) || (suspension.getName() == null)) continue;

                String suspensionDetailsUrl = "<a href=\"SuspensionDetails?ExcludeNavbar=true&amp;Name=" + 
                        StatsAggHtmlFramework.urlEncode(suspension.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(suspension.getName()) + "</a>";

                boolean isSuspensionActive = Suspension.isSuspensionActive(suspension);

                StringBuilder status = new StringBuilder();
                if (isSuspensionActive) status.append("(active");
                else status.append("(inactive");
                if ((suspension.isSuspendNotificationOnly() != null) && suspension.isSuspendNotificationOnly()) status.append(", suspend notification only");
                else if ((suspension.isSuspendNotificationOnly() != null) && !suspension.isSuspendNotificationOnly()) status.append(", suspend entire alert");
                status.append(")");

                if (isSuspensionActive) suspensionStrings.put(suspension.getName(), "<li>" + "<b>" + suspensionDetailsUrl + "&nbsp" + status.toString() + "</b>" + "</li>");
                else suspensionStrings.put(suspension.getName(), "<li>" + suspensionDetailsUrl + "&nbsp" + status.toString() + "</li>");
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            DatabaseUtils.cleanup(connection);
        }
        
        List<String> sortedSuspensionStrings = new ArrayList<>(suspensionStrings.keySet());
        Collections.sort(sortedSuspensionStrings);
        
        for (String suspensionString : sortedSuspensionStrings) {
            String suspensionOutputString = suspensionStrings.get(suspensionString);
            outputString.append(suspensionOutputString);
        }

        outputString.append("</ul>");
    
        return outputString.toString();
    }

}
