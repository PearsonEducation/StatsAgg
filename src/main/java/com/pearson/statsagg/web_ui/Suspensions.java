package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.database_objects.suspensions.SuspensionsDaoWrapper;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.database_objects.suspensions.Suspension;
import com.pearson.statsagg.database_objects.suspensions.SuspensionsDao;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.core_utils.KeyValue;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import java.sql.Connection;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class Suspensions extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(Suspensions.class.getName());
    
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
            String html = buildSuspensionsHtml();
            
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
            String operation = request.getParameter("Operation");

            if ((operation != null) && operation.equals("Enable")) {
                Integer suspensionId = Integer.parseInt(request.getParameter("Id"));
                Boolean isEnabled = Boolean.parseBoolean(request.getParameter("Enabled"));
                changeSuspensionEnabled(suspensionId, isEnabled);
            }

            if ((operation != null) && operation.equals("Clone")) {
                Integer suspensionId = Integer.parseInt(request.getParameter("Id"));
                cloneSuspension(suspensionId);
            }

            if ((operation != null) && operation.equals("Remove")) {
                Integer suspensionId = Integer.parseInt(request.getParameter("Id"));
                removeSuspension(suspensionId);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        StatsAggHtmlFramework.redirectAndGet(response, 303, "Suspensions");
    }
        
    public String changeSuspensionEnabled(Integer suspensionId, Boolean isEnabled) {
        
        if ((suspensionId == null) || (isEnabled == null)) {
            return "Invalid input!";
        }
        
        boolean isSuccess = false;
        
        Suspension suspension = SuspensionsDao.getSuspension(DatabaseConnections.getConnection(), true, suspensionId);
        
        if (suspension != null) {
            suspension.setIsEnabled(isEnabled);

            SuspensionsDaoWrapper suspensionsDaoWrapper = SuspensionsDaoWrapper.alterRecordInDatabase(suspension, suspension.getName());

            if (suspensionsDaoWrapper.getLastAlterRecordStatus() == SuspensionsDaoWrapper.STATUS_CODE_SUCCESS) {
                isSuccess = true;
                com.pearson.statsagg.threads.alert_related.Suspensions suspensions = new com.pearson.statsagg.threads.alert_related.Suspensions();
                suspensions.runSuspensionRoutine();
            }
        }
        
        if (isSuccess && isEnabled) return "Successfully enabled suspension";
        if (isSuccess && !isEnabled) return "Successfully disabled suspension";
        else return "Error -- could not alter suspension";
    }

    private void cloneSuspension(Integer suspensionId) {
        
        if (suspensionId == null) {
            return;
        }
        
        Connection connection = DatabaseConnections.getConnection();
        
        try {
            Suspension suspension = SuspensionsDao.getSuspension(connection, false, suspensionId);
            List<Suspension> allSuspensions = SuspensionsDao.getSuspensions(connection, false);
            DatabaseUtils.cleanup(connection);
            
            if ((suspension != null) && (suspension.getName() != null)) {
                Set<String> allSuspensionsNames = new HashSet<>();
                for (Suspension currentSuspension : allSuspensions) {
                    if (currentSuspension.getName() != null) {
                        allSuspensionsNames.add(currentSuspension.getName());
                    }
                }
                
                Suspension clonedSuspension = Suspension.copy(suspension);
                clonedSuspension.setId(-1);
                String clonedSuspensionName = StatsAggHtmlFramework.createCloneName(suspension.getName(), allSuspensionsNames);
                clonedSuspension.setName(clonedSuspensionName);

                SuspensionsDaoWrapper.createRecordInDatabase(clonedSuspension);

                com.pearson.statsagg.threads.alert_related.Suspensions suspensions = new com.pearson.statsagg.threads.alert_related.Suspensions();
                suspensions.runSuspensionRoutine();
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            DatabaseUtils.cleanup(connection);
        }
    }
    
    public String removeSuspension(Integer suspensionId) {
        
        String returnString = "Suspension id can't be null";
        if (suspensionId == null) return returnString;

        try{
            Suspension suspension = SuspensionsDao.getSuspension(DatabaseConnections.getConnection(), true, suspensionId);
            if (suspension == null) return null;    

            returnString = SuspensionsDaoWrapper.deleteRecordInDatabase(suspension).getReturnString();

            com.pearson.statsagg.threads.alert_related.Suspensions suspensions = new com.pearson.statsagg.threads.alert_related.Suspensions();
            suspensions.runSuspensionRoutine();
            
            return returnString;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            returnString = "Error removing suspension";
            return returnString;
        }
    }
    
    private String buildSuspensionsHtml() {
        
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
            "     <a href=\"CreateSuspension\" class=\"btn btn-primary statsagg_page_content_font\">Create New Suspension <i class=\"fa fa-long-arrow-right\"></i></a> \n" +
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

        List<Suspension> suspensions = SuspensionsDao.getSuspensions(DatabaseConnections.getConnection(), true);
        if (suspensions == null) suspensions = new ArrayList<>();
        
        for (Suspension suspension : suspensions) {
            
            if (suspension == null) continue;
            
            if (suspension.isOneTime() && suspension.getDeleteAtTimestamp() != null) {
                if (System.currentTimeMillis() >= suspension.getDeleteAtTimestamp().getTime()) continue;
            }
            
            String rowAlertStatusContext = "";
            if (Suspension.isSuspensionActive(suspension)) rowAlertStatusContext = "class=\"info\"";
            
            String suspensionDetails = "<a class=\"iframe cboxElement\" href=\"SuspensionDetails?ExcludeNavbar=true&amp;Name=" + 
                    StatsAggHtmlFramework.urlEncode(suspension.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(suspension.getName()) + "</a>";
            
            String suspendBy;
            StringBuilder suspendByDetails = new StringBuilder();
            
            if (suspension.getSuspendBy() == Suspension.SUSPEND_BY_ALERT_ID) {
                suspendBy = "Alert Name";
                
                if (suspension.getAlertId() != null) {
                    Alert alert = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, suspension.getAlertId());
                    if ((alert != null) && (alert.getName() != null)) {
                        String alertDetails = "<a class=\"iframe cboxElement\" href=\"AlertDetails?ExcludeNavbar=true&amp;Name=" + 
                            StatsAggHtmlFramework.urlEncode(alert.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(alert.getName()) + "</a>";
                        suspendByDetails.append(alertDetails);
                    }
                }
            }
            else if (suspension.getSuspendBy() == Suspension.SUSPEND_BY_METRIC_GROUP_TAGS) {
                suspendBy = "Metric Group Tags";
                List<String> tags = StringUtilities.getListOfStringsFromDelimitedString(suspension.getMetricGroupTagsInclusive(), '\n');
                
                if (tags != null) {
                    for (int i = 0; i < tags.size(); i++) {
                        suspendByDetails.append("<u>").append(StatsAggHtmlFramework.htmlEncode(tags.get(i))).append("</u>");
                        if ((i + 1) < tags.size()) suspendByDetails.append("&nbsp;&nbsp;");
                    }
                }
            }
            else if (suspension.getSuspendBy() == Suspension.SUSPEND_BY_EVERYTHING) {
                suspendBy = "Everything";
                List<String> tags = StringUtilities.getListOfStringsFromDelimitedString(suspension.getMetricGroupTagsExclusive(), '\n');
                
                if (tags != null) {
                    for (int i = 0; i < tags.size(); i++) {
                        suspendByDetails.append("<u>").append(StatsAggHtmlFramework.htmlEncode(tags.get(i))).append("</u>");
                        if ((i + 1) < tags.size()) suspendByDetails.append("&nbsp;&nbsp;");
                    }
                }
            }
            else if (suspension.getSuspendBy() == Suspension.SUSPEND_BY_METRICS) {
                suspendBy = "Metrics";
                
                Set<String> matchingMetricKeysAssociatedWithSuspension = GlobalVariables.matchingMetricKeysAssociatedWithSuspension.get(suspension.getId());
                int matchingMetricKeysAssociatedWithSuspension_Count = 0;
                if (matchingMetricKeysAssociatedWithSuspension != null) matchingMetricKeysAssociatedWithSuspension_Count = matchingMetricKeysAssociatedWithSuspension.size();
          
                String associatedAlertsPopup = ("<a class=\"iframe cboxElement\" href=\"Suspension-MetricKeyAssociations?ExcludeNavbar=true&amp;Name=" + 
                        StatsAggHtmlFramework.urlEncode(suspension.getName()) + "\">" + 
                        StatsAggHtmlFramework.htmlEncode("# Metric Associations: " + Integer.toString(matchingMetricKeysAssociatedWithSuspension_Count)) + "</a>");

                suspendByDetails.append(associatedAlertsPopup);
            }
            else {
                suspendBy = "?";
            }
            
            Map<Integer, Set<Integer>> alertIdAssociationsBySuspensionId;
            synchronized(GlobalVariables.suspensionIdAssociationsByAlertId) {
                alertIdAssociationsBySuspensionId = com.pearson.statsagg.threads.alert_related.Suspensions.getAlertIdAssociationsBySuspensionId(GlobalVariables.suspensionIdAssociationsByAlertId);
            }
            
            Set<Integer> alertIdAssociations = alertIdAssociationsBySuspensionId.get(suspension.getId());
            int suspensionAssociationCount;
            if (alertIdAssociations == null) suspensionAssociationCount = 0;
            else suspensionAssociationCount = alertIdAssociations.size(); 

            String alertsAssociationsPopup = ("<a class=\"iframe cboxElement\" href=\"Suspension-AlertAssociations?ExcludeNavbar=true&amp;Name=" + 
                    StatsAggHtmlFramework.urlEncode(suspension.getName()) + "\">" + suspensionAssociationCount + "</a>");
            
            String isAlertEnabled = "No";
            if ((suspension.isEnabled() != null) && suspension.isEnabled()) isAlertEnabled = "Yes";
            
            String enable; 
            if (suspension.isEnabled()) {
                List<KeyValue<String,String>> keysAndValues = new ArrayList<>();
                keysAndValues.add(new KeyValue("Operation", "Enable"));
                keysAndValues.add(new KeyValue("Id", suspension.getId().toString()));
                keysAndValues.add(new KeyValue("Enabled", "false"));
                enable = StatsAggHtmlFramework.buildJavaScriptPostLink("Enable_" + suspension.getName(), "Suspensions", "disable", keysAndValues);
            }
            else {
                List<KeyValue<String,String>> keysAndValues = new ArrayList<>();
                keysAndValues.add(new KeyValue("Operation", "Enable"));
                keysAndValues.add(new KeyValue("Id", suspension.getId().toString()));
                keysAndValues.add(new KeyValue("Enabled", "true"));
                enable = StatsAggHtmlFramework.buildJavaScriptPostLink("Enable_" + suspension.getName(), "Suspensions", "enable", keysAndValues);
            }
            
            String alter = "<a href=\"CreateSuspension?Operation=Alter&amp;Name=" + StatsAggHtmlFramework.urlEncode(suspension.getName()) + "\">alter</a>";
            
            List<KeyValue<String,String>> cloneKeysAndValues = new ArrayList<>();
            cloneKeysAndValues.add(new KeyValue("Operation", "Clone"));
            cloneKeysAndValues.add(new KeyValue("Id", suspension.getId().toString()));
            String clone = StatsAggHtmlFramework.buildJavaScriptPostLink("Clone_" + suspension.getName(), "Suspensions", "clone", cloneKeysAndValues);
                    
            List<KeyValue<String,String>> removeKeysAndValues = new ArrayList<>();
            removeKeysAndValues.add(new KeyValue("Operation", "Remove"));
            removeKeysAndValues.add(new KeyValue("Id", suspension.getId().toString()));
            String remove = StatsAggHtmlFramework.buildJavaScriptPostLink("Remove_" + suspension.getName(), "Suspensions", "remove", 
                    removeKeysAndValues, true, "Are you sure you want to remove this suspension?");

            htmlBodyStringBuilder
                    .append("<tr ").append(rowAlertStatusContext).append(">\n")
                    .append("<td class=\"statsagg_force_word_break\">").append(suspensionDetails).append("</td>\n")
                    .append("<td>").append(suspendBy).append("</td>\n")
                    .append("<td class=\"statsagg_force_word_break\">").append(suspendByDetails).append("</td>\n")
                    .append("<td>").append(alertsAssociationsPopup).append("</td>\n")
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
