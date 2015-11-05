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
import com.pearson.statsagg.utilities.StringUtilities;
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
    
    public static final String PAGE_NAME = "Suspensions";
    
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
            changeSuspensionEnabled(name, isEnabled);
        }
        
        if ((operation != null) && operation.equals("Clone")) {
            String name = request.getParameter("Name");
            cloneSuspension(name);
        }
        
        if ((operation != null) && operation.equals("Remove")) {
            String name = request.getParameter("Name");
            removeSuspension(name);
        }
        
        StatsAggHtmlFramework.redirectAndGet(response, 303, "AlertSuspensions");
    }
        
    private void changeSuspensionEnabled(String suspensionName, Boolean isEnabled) {
        
        if ((suspensionName == null) || (isEnabled == null)) {
            return;
        }
        
        AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao();
        AlertSuspension suspension = alertSuspensionsDao.getSuspensionByName(suspensionName);
        
        if (suspension != null) {
            suspension.setIsEnabled(isEnabled);

            AlertSuspensionsLogic alertSuspensionsLogic = new AlertSuspensionsLogic();
            alertSuspensionsLogic.alterRecordInDatabase(suspension, suspensionName);

            com.pearson.statsagg.alerts.AlertSuspensions alertSuspensions = new com.pearson.statsagg.alerts.AlertSuspensions();
            alertSuspensions.runAlertSuspensionRoutine();
        }
    }

    private void cloneSuspension(String suspensionName) {
        
        if (suspensionName == null) {
            return;
        }
        
        try {
            AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao(false);
            AlertSuspension suspension = alertSuspensionsDao.getSuspensionByName(suspensionName);
            List<AlertSuspension> allSuspensions = alertSuspensionsDao.getAllDatabaseObjectsInTable();
            alertSuspensionsDao.close();

            if ((suspension != null) && (suspension.getName() != null)) {
                Set<String> allSuspensionsNames = new HashSet<>();
                for (AlertSuspension currentSuspension : allSuspensions) {
                    if (currentSuspension.getName() != null) {
                        allSuspensionsNames.add(currentSuspension.getName());
                    }
                }
                
                AlertSuspension clonedSuspension = AlertSuspension.copy(suspension);
                clonedSuspension.setId(-1);
                String clonedSuspensionName = StatsAggHtmlFramework.createCloneName(suspension.getName(), allSuspensionsNames);
                clonedSuspension.setName(clonedSuspensionName);
                clonedSuspension.setUppercaseName(clonedSuspensionName.toUpperCase());

                AlertSuspensionsLogic suspensionsLogic = new AlertSuspensionsLogic();
                suspensionsLogic.alterRecordInDatabase(clonedSuspension);

                com.pearson.statsagg.alerts.AlertSuspensions suspensions = new com.pearson.statsagg.alerts.AlertSuspensions();
                suspensions.runAlertSuspensionRoutine();
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
    public String removeSuspension(String suspensionName) {
        
        if (suspensionName == null) {
            return null;
        }
        
        String returnString = null;
        AlertSuspensionsLogic suspensionsLogic = new AlertSuspensionsLogic();
        returnString = suspensionsLogic.deleteRecordInDatabase(suspensionName);
        
        com.pearson.statsagg.alerts.AlertSuspensions suspensions = new com.pearson.statsagg.alerts.AlertSuspensions();
        suspensions.runAlertSuspensionRoutine();
        
        return returnString;
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
            "     <a href=\"CreateAlertSuspension\" class=\"btn btn-primary statsagg_page_content_font\">Create New Suspension <i class=\"fa fa-long-arrow-right\"></i></a> \n" +
            "    </div>\n" + 
            "  </div>\n" +   
            "  <table id=\"SuspensionsTable\" style=\"display:none\" class=\"table table-bordered table-hover\">\n" +
            "     <thead>\n" +
            "       <tr>\n" +
            "         <th>Suspension Name</th>\n" +
            "         <th>Suspend By</th>\n" +
            "         <th>Suspend By Details</th>\n" +
            "         <th># Alert Associations</th>\n" +
            "         <th>Enabled?</th>\n" +
            "         <th>Operations</th>\n" +
            "       </tr>\n" +
            "     </thead>\n" +
            "     <tbody>\n");

        AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao();
        List<AlertSuspension> suspensions = alertSuspensionsDao.getAllDatabaseObjectsInTable();
        
        for (AlertSuspension suspension : suspensions) {
            
            if (suspension.isOneTime() && suspension.getDeleteAtTimestamp() != null) {
                if (System.currentTimeMillis() >= suspension.getDeleteAtTimestamp().getTime()) continue;
            }
            
            String rowAlertStatusContext = "";
            if (AlertSuspension.isSuspensionActive(suspension)) rowAlertStatusContext = "class=\"info\"";
            
            String suspensionDetails = "<a href=\"AlertSuspensionDetails?Name=" + 
                    StatsAggHtmlFramework.urlEncode(suspension.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(suspension.getName()) + "</a>";
            
            String suspendBy;
            StringBuilder suspendByDetails = new StringBuilder();
            
            if (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_ALERT_ID) {
                suspendBy = "Alert Name";
                
                if (suspension.getAlertId() != null) {
                    AlertsDao alertsDao = new AlertsDao();
                    Alert alert = alertsDao.getAlert(suspension.getAlertId());
                    if ((alert != null) && (alert.getName() != null)) suspendByDetails.append(StatsAggHtmlFramework.htmlEncode(alert.getName()));
                }
            }
            else if (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS) {
                suspendBy = "Metric Group Tags";
                List<String> tags = StringUtilities.getListOfStringsFromDelimitedString(suspension.getMetricGroupTagsInclusive(), '\n');
                
                if (tags != null) {
                    for (int i = 0; i < tags.size(); i++) {
                        suspendByDetails.append("<u>").append(StatsAggHtmlFramework.htmlEncode(tags.get(i))).append("</u>");
                        if ((i + 1) < tags.size()) suspendByDetails.append("&nbsp;&nbsp;");
                    }
                }
            }
            else if (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_EVERYTHING) {
                suspendBy = "Everything";
                List<String> tags = StringUtilities.getListOfStringsFromDelimitedString(suspension.getMetricGroupTagsExclusive(), '\n');
                
                if (tags != null) {
                    for (int i = 0; i < tags.size(); i++) {
                        suspendByDetails.append("<u>").append(StatsAggHtmlFramework.htmlEncode(tags.get(i))).append("</u>");
                        if ((i + 1) < tags.size()) suspendByDetails.append("&nbsp;&nbsp;");
                    }
                }
            }
            else if (suspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_METRICS) {
                suspendBy = "Metrics";
                
                Set<String> matchingMetricKeysAssociatedWithSuspension = GlobalVariables.matchingMetricKeysAssociatedWithSuspension.get(suspension.getId());
                int matchingMetricKeysAssociatedWithSuspension_Count = 0;
                if (matchingMetricKeysAssociatedWithSuspension != null) matchingMetricKeysAssociatedWithSuspension_Count = matchingMetricKeysAssociatedWithSuspension.size();
            
                String metricAssociationsLink = "<a href=\"SuspensionMetricKeyAssociations?Name=" + 
                        StatsAggHtmlFramework.urlEncode(suspension.getName()) + "\">" + 
                        StatsAggHtmlFramework.htmlEncode("# Metric Associations: " + Integer.toString(matchingMetricKeysAssociatedWithSuspension_Count)) + "</a>";
                
                suspendByDetails.append(metricAssociationsLink);
            }
            else {
                suspendBy = "?";
            }
            
            Map<Integer, Set<Integer>> alertIdAssociationsBySuspensionId;
            synchronized(GlobalVariables.suspensionIdAssociationsByAlertId) {
                alertIdAssociationsBySuspensionId = com.pearson.statsagg.alerts.AlertSuspensions.getAlertIdAssociationsBySuspensionId(GlobalVariables.suspensionIdAssociationsByAlertId);
            }
            
            Set<Integer> alertIdAssociations = alertIdAssociationsBySuspensionId.get(suspension.getId());
            int suspensionAssociationCount;
            if (alertIdAssociations == null) suspensionAssociationCount = 0;
            else suspensionAssociationCount = alertIdAssociations.size(); 
            String associatedAlertsLink = "<a href=\"AlertSuspensionAlertAssociations?Name=" + StatsAggHtmlFramework.urlEncode(suspension.getName()) + "\">" + suspensionAssociationCount + "</a>";
            
            String isAlertEnabled = "No";
            if ((suspension.isEnabled() != null) && suspension.isEnabled()) isAlertEnabled = "Yes";
            
            String enable; 
            if (suspension.isEnabled()) {
                List<KeyValue> keysAndValues = new ArrayList<>();
                keysAndValues.add(new KeyValue("Operation", "Enable"));
                keysAndValues.add(new KeyValue("Name", Encode.forHtmlAttribute(suspension.getName())));
                keysAndValues.add(new KeyValue("Enabled", "false"));
                enable = StatsAggHtmlFramework.buildJavaScriptPostLink("Enable_" + suspension.getName(), "AlertSuspensions", "disable", keysAndValues);
            }
            else {
                List<KeyValue> keysAndValues = new ArrayList<>();
                keysAndValues.add(new KeyValue("Operation", "Enable"));
                keysAndValues.add(new KeyValue("Name", Encode.forHtmlAttribute(suspension.getName())));
                keysAndValues.add(new KeyValue("Enabled", "true"));
                enable = StatsAggHtmlFramework.buildJavaScriptPostLink("Enable_" + suspension.getName(), "AlertSuspensions", "enable", keysAndValues);
            }
            
            String alter = "<a href=\"CreateAlertSuspension?Operation=Alter&amp;Name=" + StatsAggHtmlFramework.urlEncode(suspension.getName()) + "\">alter</a>";
            
            List<KeyValue> cloneKeysAndValues = new ArrayList<>();
            cloneKeysAndValues.add(new KeyValue("Operation", "Clone"));
            cloneKeysAndValues.add(new KeyValue("Name", Encode.forHtmlAttribute(suspension.getName())));
            String clone = StatsAggHtmlFramework.buildJavaScriptPostLink("Clone_" + suspension.getName(), "AlertSuspensions", "clone", cloneKeysAndValues);
                    
            List<KeyValue> removeKeysAndValues = new ArrayList<>();
            removeKeysAndValues.add(new KeyValue("Operation", "Remove"));
            removeKeysAndValues.add(new KeyValue("Name", Encode.forHtmlAttribute(suspension.getName())));
            String remove = StatsAggHtmlFramework.buildJavaScriptPostLink("Remove_" + suspension.getName(), "AlertSuspensions", "remove", 
                    removeKeysAndValues, true, "Are you sure you want to remove this suspension?");

            htmlBodyStringBuilder
                    .append("<tr ").append(rowAlertStatusContext).append(">\n")
                    .append("<td class=\"statsagg_force_word_break\">").append(suspensionDetails).append("</td>\n")
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
