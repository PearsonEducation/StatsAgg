package com.pearson.statsagg.webui;

import com.pearson.statsagg.database_objects.alert_suspensions.AlertSuspension;
import com.pearson.statsagg.database_objects.alert_suspensions.AlertSuspensionsDao;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.KeyValue;
import com.pearson.statsagg.utilities.StackTrace;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "AlertSuspensions", urlPatterns = {"/AlertSuspensions"})
public class AlertSuspensions extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AlertSuspensions.class.getName());
    
    public static final String PAGE_NAME = "Alert Suspensions";
    
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
        
        response.setContentType("text/html");
        PrintWriter out = null;
                
        try {
            String html = buildAlertSuspensionsHtml();
            
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
        
        String operation = request.getParameter("Operation");

        if ((operation != null) && operation.equals("Enable")) {
            String name = request.getParameter("Name");
            Boolean isEnabled = Boolean.parseBoolean(request.getParameter("Enabled"));
            changeAlertSuspensionEnabled(name, isEnabled);
        }
        
        if ((operation != null) && operation.equals("Clone")) {
            String name = request.getParameter("Name");
            cloneAlertSuspension(name);
        }
        
        if ((operation != null) && operation.equals("Remove")) {
            String name = request.getParameter("Name");
            removeAlertSuspension(name);
        }
        
        StatsAggHtmlFramework.redirectAndGet(response, 303, "AlertSuspensions");
    }
        
    private void changeAlertSuspensionEnabled(String alertSuspensionName, Boolean isEnabled) {
        
        if ((alertSuspensionName == null) || (isEnabled == null)) {
            return;
        }
        
        AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao();
        AlertSuspension alertSuspension = alertSuspensionsDao.getAlertSuspensionByName(alertSuspensionName);
        
        if (alertSuspension != null) {
            alertSuspension.setIsEnabled(isEnabled);

            AlertSuspensionsLogic alertSuspensionsLogic = new AlertSuspensionsLogic();
            alertSuspensionsLogic.alterRecordInDatabase(alertSuspension, alertSuspensionName);

            com.pearson.statsagg.alerts.AlertSuspensions alertSuspensions = new com.pearson.statsagg.alerts.AlertSuspensions();
            alertSuspensions.runAlertSuspensionRoutine();
        }
    }

    private void cloneAlertSuspension(String alertSuspensionName) {
        
        if (alertSuspensionName == null) {
            return;
        }
        
        try {
            AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao(false);
            AlertSuspension alertSuspension = alertSuspensionsDao.getAlertSuspensionByName(alertSuspensionName);
            List<AlertSuspension> allAlertSuspensions = alertSuspensionsDao.getAllDatabaseObjectsInTable();
            alertSuspensionsDao.close();

            if ((alertSuspension != null) && (alertSuspension.getName() != null)) {
                Set<String> allAlertSuspensionsNames = new HashSet<>();
                for (AlertSuspension currentAlertSuspension : allAlertSuspensions) {
                    if (currentAlertSuspension.getName() != null) {
                        allAlertSuspensionsNames.add(currentAlertSuspension.getName());
                    }
                }
                
                AlertSuspension clonedAlertSuspension = AlertSuspension.copy(alertSuspension);
                clonedAlertSuspension.setId(-1);
                String clonedAlertSuspensionName = StatsAggHtmlFramework.createCloneName(alertSuspension.getName(), allAlertSuspensionsNames);
                clonedAlertSuspension.setName(clonedAlertSuspensionName);
                clonedAlertSuspension.setUppercaseName(clonedAlertSuspensionName.toUpperCase());

                AlertSuspensionsLogic alertSuspensionsLogic = new AlertSuspensionsLogic();
                alertSuspensionsLogic.alterRecordInDatabase(clonedAlertSuspension);

                com.pearson.statsagg.alerts.AlertSuspensions alertSuspensions = new com.pearson.statsagg.alerts.AlertSuspensions();
                alertSuspensions.runAlertSuspensionRoutine();
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
    private void removeAlertSuspension(String alertSuspensionName) {
        
        if (alertSuspensionName == null) {
            return;
        }
        
        AlertSuspensionsLogic alertSuspensionsLogic = new AlertSuspensionsLogic();
        alertSuspensionsLogic.deleteRecordInDatabase(alertSuspensionName);
        
        com.pearson.statsagg.alerts.AlertSuspensions alertSuspensions = new com.pearson.statsagg.alerts.AlertSuspensions();
        alertSuspensions.runAlertSuspensionRoutine();
    }
    
    private String buildAlertSuspensionsHtml() {
        
        StringBuilder html = new StringBuilder();

        StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
        String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");

        StringBuilder htmlBodyStringBuilder = new StringBuilder();
        htmlBodyStringBuilder.append(
            "<div id=\"page-content-wrapper\">\n" +
            "<!-- Keep all page content within the page-content inset div! -->\n" +
            "<div class=\"page-content inset statsagg_page_content_font\">\n" +
            "  <div class=\"content-header\"> \n" +
            "    <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "    <div class=\"pull-right \">\n" +
            "     <a href=\"CreateAlertSuspension\" class=\"btn btn-primary statsagg_page_content_font\">Create New Alert Suspension <i class=\"fa fa-long-arrow-right\"></i></a> \n" +
            "    </div>\n" + 
            "  </div>\n" +   
            "  <table id=\"AlertSuspensionsTable\" style=\"display:none\" class=\"table table-bordered table-hover\">\n" +
            "     <thead>\n" +
            "       <tr>\n" +
            "         <th>Alert Suspension Name</th>\n" +
            "         <th>Suspend By</th>\n" +
            "         <th>Suspend By Details</th>\n" +
            "         <th># Alert Associations</th>\n" +
            "         <th>Enabled?</th>\n" +
            "         <th>Operations</th>\n" +
            "       </tr>\n" +
            "     </thead>\n" +
            "     <tbody>\n");

        AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao();
        List<AlertSuspension> alertSuspensions = alertSuspensionsDao.getAllDatabaseObjectsInTable();
        
        for (AlertSuspension alertSuspension : alertSuspensions) {
            
            if (alertSuspension.isOneTime() && alertSuspension.getDeleteAtTimestamp() != null) {
                if (System.currentTimeMillis() >= alertSuspension.getDeleteAtTimestamp().getTime()) continue;
            }
            
            String rowAlertStatusContext = "";
            if (AlertSuspension.isAlertSuspensionActive(alertSuspension)) rowAlertStatusContext = "class=\"info\"";
            
            String alertSuspensionDetails = "<a href=\"AlertSuspensionDetails?Name=" + 
                    StatsAggHtmlFramework.urlEncode(alertSuspension.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(alertSuspension.getName()) + "</a>";
            
            String suspendBy;
            StringBuilder suspendByDetails = new StringBuilder();
            
            if (alertSuspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_ALERT_ID) {
                suspendBy = "Alert Name";
                
                if (alertSuspension.getAlertId() != null) {
                    AlertsDao alertsDao = new AlertsDao();
                    Alert alert = alertsDao.getAlert(alertSuspension.getAlertId());
                    if ((alert != null) && (alert.getName() != null)) suspendByDetails.append(StatsAggHtmlFramework.htmlEncode(alert.getName()));
                }
            }
            else if (alertSuspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS) {
                suspendBy = "Metric Group Tags";
                List<String> tags = AlertSuspension.getMetricGroupTagStringsFromNewlineDelimitedString(alertSuspension.getMetricGroupTagsInclusive());
                
                if (tags != null) {
                    for (int i = 0; i < tags.size(); i++) {
                        suspendByDetails.append("<u>").append(StatsAggHtmlFramework.htmlEncode(tags.get(i))).append("</u>");
                        if ((i + 1) < tags.size()) suspendByDetails.append("&nbsp;&nbsp;");
                    }
                }
            }
            else if (alertSuspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_EVERYTHING) {
                suspendBy = "Everything";
                List<String> tags = AlertSuspension.getMetricGroupTagStringsFromNewlineDelimitedString(alertSuspension.getMetricGroupTagsExclusive());
                
                if (tags != null) {
                    for (int i = 0; i < tags.size(); i++) {
                        suspendByDetails.append("<u>").append(StatsAggHtmlFramework.htmlEncode(tags.get(i))).append("</u>");
                        if ((i + 1) < tags.size()) suspendByDetails.append("&nbsp;&nbsp;");
                    }
                }
            }
            else {
                suspendBy = "?";
            }
            
            Map<Integer, Set<Integer>> alertIdAssociationsByAlertSuspensionId;
            synchronized(GlobalVariables.alertSuspensionIdAssociationsByAlertId) {
                alertIdAssociationsByAlertSuspensionId = com.pearson.statsagg.alerts.AlertSuspensions.getAlertIdAssociationsByAlertSuspensionId(GlobalVariables.alertSuspensionIdAssociationsByAlertId);
            }
            
            Set<Integer> alertIdAssociations = alertIdAssociationsByAlertSuspensionId.get(alertSuspension.getId());
            int alertSuspensionAssociationCount;
            if (alertIdAssociations == null) alertSuspensionAssociationCount = 0;
            else alertSuspensionAssociationCount = alertIdAssociations.size(); 
            String associatedAlertsLink = "<a href=\"AlertSuspensionAlertAssociations?Name=" + StatsAggHtmlFramework.urlEncode(alertSuspension.getName()) + "\">" + alertSuspensionAssociationCount + "</a>";
            
            String isAlertEnabled = "No";
            if ((alertSuspension.isEnabled() != null) && alertSuspension.isEnabled()) isAlertEnabled = "Yes";
            
            String enable; 
            if (alertSuspension.isEnabled()) {
                List<KeyValue> keysAndValues = new ArrayList<>();
                keysAndValues.add(new KeyValue("Operation", "Enable"));
                keysAndValues.add(new KeyValue("Name", Encode.forHtmlAttribute(alertSuspension.getName())));
                keysAndValues.add(new KeyValue("Enabled", "false"));
                enable = StatsAggHtmlFramework.buildJavaScriptPostLink("Enable_" + alertSuspension.getName(), "AlertSuspensions", "disable", keysAndValues);
            }
            else {
                List<KeyValue> keysAndValues = new ArrayList<>();
                keysAndValues.add(new KeyValue("Operation", "Enable"));
                keysAndValues.add(new KeyValue("Name", Encode.forHtmlAttribute(alertSuspension.getName())));
                keysAndValues.add(new KeyValue("Enabled", "true"));
                enable = StatsAggHtmlFramework.buildJavaScriptPostLink("Enable_" + alertSuspension.getName(), "AlertSuspensions", "enable", keysAndValues);
            }
            
            String alter = "<a href=\"CreateAlertSuspension?Operation=Alter&amp;Name=" + StatsAggHtmlFramework.urlEncode(alertSuspension.getName()) + "\">alter</a>";
            
            List<KeyValue> cloneKeysAndValues = new ArrayList<>();
            cloneKeysAndValues.add(new KeyValue("Operation", "Clone"));
            cloneKeysAndValues.add(new KeyValue("Name", Encode.forHtmlAttribute(alertSuspension.getName())));
            String clone = StatsAggHtmlFramework.buildJavaScriptPostLink("Clone_" + alertSuspension.getName(), "AlertSuspensions", "clone", cloneKeysAndValues);
                    
            List<KeyValue> removeKeysAndValues = new ArrayList<>();
            removeKeysAndValues.add(new KeyValue("Operation", "Remove"));
            removeKeysAndValues.add(new KeyValue("Name", Encode.forHtmlAttribute(alertSuspension.getName())));
            String remove = StatsAggHtmlFramework.buildJavaScriptPostLink("Remove_" + alertSuspension.getName(), "AlertSuspensions", "remove", 
                    removeKeysAndValues, true, "Are you sure you want to remove this alert suspension?");

            htmlBodyStringBuilder
                    .append("<tr ").append(rowAlertStatusContext).append(">\n")
                    .append("<td class=\"statsagg_force_word_break\">").append(alertSuspensionDetails).append("</td>\n")
                    .append("<td>").append(suspendBy).append("</td>\n")
                    .append("<td class=\"statsagg_force_word_break\">").append(suspendByDetails).append("</td>\n")
                    .append("<td>").append(associatedAlertsLink).append("</td>\n")
                    .append("<td>").append(isAlertEnabled).append("</td>\n")
                    .append("<td>").append(enable).append(", ").append(alter).append(", ").append(clone).append(", ").append(remove).append("</td>\n")
                    .append("</tr>\n");
        }

        htmlBodyStringBuilder.append(""
                + "</tbody>\n"
                + "<tfoot> \n"
                + "  <tr>\n" 
                + "    <th></th>\n"
                + "    <th></th>\n" 
                + "    <th></th>\n" 
                + "    <th></th>\n" 
                + "    <th></th>\n" 
                + "    <th></th>\n" 
                + "  </tr>\n" 
                + "</tfoot>" 
                + "</table>\n"
                + "</div>\n"
                + "</div>\n");
        
        String htmlBody = (statsAggHtmlFramework.createHtmlBody(htmlBodyStringBuilder.toString()));

        html.append(""
                + "<!DOCTYPE html>\n"
                + "<html>\n")
                .append(htmlHeader)
                .append(htmlBody)
                .append("</html>");
        
        return html.toString();
    }

}
