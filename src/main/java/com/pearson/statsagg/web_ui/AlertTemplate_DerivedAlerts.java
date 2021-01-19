package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.database_objects.alert_templates.AlertTemplate;
import com.pearson.statsagg.database_objects.alert_templates.AlertTemplatesDao;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class AlertTemplate_DerivedAlerts extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AlertTemplate_DerivedAlerts.class.getName());
    
    public static final String PAGE_NAME = "Alert Template - Derived Alerts";
    
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
        String alertTemplate_DerivedAlerts = getAlertTemplate_DerivedAlerts(name, excludeNavbar);
                
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
            alertTemplate_DerivedAlerts +
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

    private String getAlertTemplate_DerivedAlerts(String alertTemplateName, boolean excludeNavbar) {
        
        if (alertTemplateName == null) {
            return "<b>No alert template specified</b>";
        }
 
        AlertTemplate alertTemplate = AlertTemplatesDao.getAlertTemplate(DatabaseConnections.getConnection(), true, alertTemplateName);
        if ((alertTemplate == null) || (alertTemplate.getId() == null)) return "<b>Alert template not found</b>";
        
        Map<String,Alert> alerts_ByUppercaseName = AlertsDao.getAlerts_ByUppercaseName(DatabaseConnections.getConnection(), true);
        if (alerts_ByUppercaseName == null) return "<b>Error retrieving alerts</b>";

        Set<String> alertNamesThatAlertTemplateWantsToCreate = com.pearson.statsagg.threads.template_related.Common.getNamesThatTemplateWantsToCreate(alertTemplate.getVariableSetListId(), alertTemplate.getAlertNameVariable());
        if (alertNamesThatAlertTemplateWantsToCreate == null) return "<b>Error retrieving desired derived alerts</b>";
        
        StringBuilder outputString = new StringBuilder();
        
        outputString.append("<b>Alert Template Name</b> = ").append(StatsAggHtmlFramework.htmlEncode(alertTemplate.getName())).append("<br>");

        int desiredDerivedAlertCount = alertNamesThatAlertTemplateWantsToCreate.size();
        outputString.append("<b>Total Desired Derived Alerts</b> = ").append(desiredDerivedAlertCount).append("<br><br>");
        if (desiredDerivedAlertCount <= 0) return outputString.toString();

        outputString.append("<b>Desired Derived Alerts...</b>").append("<br>");

        outputString.append("<ul>");

        Map<String,String> alertStrings = new HashMap<>();
        
        Connection connection = null;
        
        try {
            connection = DatabaseConnections.getConnection();
            
            for (String alertNameThatAlertTemplateWantsToCreate : alertNamesThatAlertTemplateWantsToCreate) {
                if (alertNameThatAlertTemplateWantsToCreate == null) continue;

                Alert alert = alerts_ByUppercaseName.get(alertNameThatAlertTemplateWantsToCreate.toUpperCase());

                String alertStatus = "";
                
                if (alert == null) {
                    alertStatus = "(alert does not exist - issue creating alert)";
                }
                else if (alert.getAlertTemplateId() == null) {
                    alertStatus = "(alert name conflict with another not-templated alert)";
                }
                else if (!alertTemplate.getId().equals(alert.getAlertTemplateId())) {
                    AlertTemplate alertTemplateFromDb = AlertTemplatesDao.getAlertTemplate(DatabaseConnections.getConnection(), true, alert.getAlertTemplateId());
                    
                    if (alertTemplateFromDb == null) {
                        alertStatus = "(alert name conflict with another templated alert)";
                    }
                    else {
                        String alertTemplateUrl = "<a href=\"AlertTemplateDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(alertTemplateFromDb.getName()) + "\">" + "templated" + "</a>";
                        alertStatus = "(alert name conflict with another " + alertTemplateUrl + " alert)";
                    }
                }
                
                String alertDetailsUrl = "<a href=\"AlertDetails?ExcludeNavbar=true&amp;Name=" + 
                        StatsAggHtmlFramework.urlEncode(alertNameThatAlertTemplateWantsToCreate) + "\">" + 
                        StatsAggHtmlFramework.htmlEncode(alertNameThatAlertTemplateWantsToCreate) + "</a>";

                if (alertStatus.isEmpty()) alertStrings.put(alertNameThatAlertTemplateWantsToCreate, "<li>" + alertDetailsUrl + "</li>");
                else alertStrings.put(alertNameThatAlertTemplateWantsToCreate, "<li><b>" + alertDetailsUrl + "&nbsp" + alertStatus + "</b></li>");
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            DatabaseUtils.cleanup(connection);
        }
        
        List<String> sortedAlertStrings = new ArrayList<>(alertStrings.keySet());
        Collections.sort(sortedAlertStrings);
        
        for (String alertString : sortedAlertStrings) {
            String alertOutputString = alertStrings.get(alertString);
            outputString.append(alertOutputString);
        }

        outputString.append("</ul>");
    
        return outputString.toString();
    }

}
