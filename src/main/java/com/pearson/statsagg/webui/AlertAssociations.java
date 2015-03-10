package com.pearson.statsagg.webui;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database.alerts.Alert;
import com.pearson.statsagg.database.alerts.AlertsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.StackTrace;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "AlertAssociations", urlPatterns = {"/AlertAssociations"})
public class AlertAssociations extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AlertAssociations.class.getName());
    
    public static final String PAGE_NAME = "Alert Associations";
    
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
        String level = request.getParameter("Level");
        String acknowledgeLevel = request.getParameter("AcknowledgeLevel");
        String acknowledgeChange = request.getParameter("AcknowledgeChange");
        
        AlertsDao alertsDao = new AlertsDao();
        Alert alert = alertsDao.getAlertByName(name);  
        
        Boolean acknowledgeChange_Boolean = null;
        try {acknowledgeChange_Boolean = Boolean.parseBoolean(acknowledgeChange);}
        catch (Exception e){}
        
        if ((acknowledgeLevel != null) && (alert != null)) {            
            if ((alert.isCautionAlertActive() != null) && alert.isCautionAlertActive() && (acknowledgeLevel.equalsIgnoreCase("Caution") || acknowledgeLevel.equalsIgnoreCase("Triggered"))) {
                AlertsLogic.changeAlertCautionAcknowledge(name, acknowledgeChange_Boolean);
            }     

            if ((alert.isDangerAlertActive() != null) && alert.isDangerAlertActive() && (acknowledgeLevel.equalsIgnoreCase("Danger") || acknowledgeLevel.equalsIgnoreCase("Triggered"))) {
                AlertsLogic.changeAlertDangerAcknowledge(name, acknowledgeChange_Boolean);
            }    
            
            alertsDao = new AlertsDao();
            alert = alertsDao.getAlertByName(name);  
        }
        
        String alertAssociations = "";
        
        if (level != null) {
            if (level.equalsIgnoreCase("Triggered")) {
                alertAssociations = getTriggeredAlertAssociations(name);
            }
            else if (level.equalsIgnoreCase("Caution")) {
                alertAssociations = getCautionAlertAssociations(name);
            }
            else if (level.equalsIgnoreCase("Danger")) {
                alertAssociations = getDangerAlertAssociations(name);
            }
        }
        
        try {  
            StringBuilder htmlBuilder = new StringBuilder("");

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            
            StringBuilder htmlBodyBuilder = new StringBuilder();
            htmlBodyBuilder.append(
                "<div id=\"page-content-wrapper\">\n" +
                "<!-- Keep all page content within the page-content inset div! -->\n" +
                "  <div class=\"page-content inset\">\n" +
                "    <div class=\"content-header\"> \n" +
                "      <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
                "      <div class=\"pull-right \">\n");
            
            if ((level != null) && (name != null) && (alert != null)) {
                if (level.equalsIgnoreCase("Triggered")) {
                    if (                // caution & danger both acknowledged
                        ((alert.isCautionAlertActive() && (alert.isCautionAcknowledged() != null) && alert.isCautionAcknowledged()) &&
                        (alert.isDangerAlertActive() && (alert.isDangerAcknowledged() != null) && alert.isDangerAcknowledged())) 
                        ||              // danger acknowledged, caution not active (therefore not acknowledged)
                        (!alert.isCautionAlertActive() && (alert.isDangerAlertActive() && (alert.isDangerAcknowledged() != null) && alert.isDangerAcknowledged()))
                        ||              // caution acknowledged, danger not active (therefore not acknowledged)
                        (!alert.isDangerAlertActive() && (alert.isCautionAlertActive() && (alert.isCautionAcknowledged() != null) && alert.isCautionAcknowledged()))
                       )              
                    {
                        htmlBodyBuilder.append("<a href=\"AlertAssociations?AcknowledgeLevel=Triggered&amp;AcknowledgeChange=False&amp;Level=Triggered&amp;Name=").
                                append(StatsAggHtmlFramework.urlEncode(name)).append("\" class=\"btn btn-primary\">Unacknowledge Triggered Alert</a>\n");
                    }
                    else if ((alert.isCautionAlertActive() && ((alert.isCautionAcknowledged() == null) || ((alert.isCautionAcknowledged() != null) && !alert.isCautionAcknowledged()))) || 
                            (alert.isDangerAlertActive() && ((alert.isDangerAcknowledged() == null) || ((alert.isDangerAcknowledged() != null) && !alert.isDangerAcknowledged())))) {
                        htmlBodyBuilder.append("<a href=\"AlertAssociations?AcknowledgeLevel=Triggered&amp;AcknowledgeChange=True&amp;Level=Triggered&amp;Name=").
                                append(StatsAggHtmlFramework.urlEncode(name)).append("\" class=\"btn btn-primary\">Acknowledge Triggered Alert</a>\n");
                    }
                }
                else if (level.equalsIgnoreCase("Caution")) {
                    if ((alert.isCautionAlertActive() != null) && alert.isCautionAlertActive()) {
                        if ((alert.isCautionAcknowledged() == null) || ((alert.isCautionAcknowledged() != null) && !alert.isCautionAcknowledged())) {
                            htmlBodyBuilder.append("<a href=\"AlertAssociations?AcknowledgeLevel=Caution&amp;AcknowledgeChange=True&amp;Level=Caution&amp;Name=").
                                    append(StatsAggHtmlFramework.urlEncode(name)).append("\" class=\"btn btn-primary\">Acknowledge Caution Alert</a>\n");
                        }
                        else if (((alert.isCautionAcknowledged() != null) && alert.isCautionAcknowledged())) {
                            htmlBodyBuilder.append("<a href=\"AlertAssociations?AcknowledgeLevel=Caution&amp;AcknowledgeChange=False&amp;Level=Caution&amp;Name=").
                                    append(StatsAggHtmlFramework.urlEncode(name)).append("\" class=\"btn btn-primary\">Unacknowledge Caution Alert</a>\n");
                        }
                    }
                }
                else if (level.equalsIgnoreCase("Danger")) {
                    if ((alert.isDangerAlertActive() != null) && alert.isDangerAlertActive()) {
                        if ((alert.isDangerAcknowledged() == null) || ((alert.isDangerAcknowledged() != null) && !alert.isDangerAcknowledged())) {
                            htmlBodyBuilder.append("<a href=\"AlertAssociations?AcknowledgeLevel=Danger&amp;AcknowledgeChange=True&amp;Level=Danger&amp;Name=").
                                    append(StatsAggHtmlFramework.urlEncode(name)).append("\" class=\"btn btn-primary\">Acknowledge Danger Alert</a>\n");
                        }
                        else if (((alert.isDangerAcknowledged() != null) && alert.isDangerAcknowledged())) {
                            htmlBodyBuilder.append("<a href=\"AlertAssociations?AcknowledgeLevel=Danger&amp;AcknowledgeChange=False&amp;Level=Danger&amp;Name=").
                                    append(StatsAggHtmlFramework.urlEncode(name)).append("\" class=\"btn btn-primary\">Unacknowledge Danger Alert</a>\n");
                        }
                    }
                }
            }
            
            htmlBodyBuilder.append(
                "      </div>\n" + 
                "    </div>\n " +
                "    <div class=\"statsagg_force_word_wrap\">" +
                alertAssociations +
                "    </div>\n" +
                "  </div>\n" +
                "</div>\n");
            
            String htmlBody = statsAggHtmlFramework.createHtmlBody(htmlBodyBuilder.toString());
 
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

    private String getTriggeredAlertAssociations(String alertName) {
        
        if (alertName == null) {
            return "";
        }
        
        StringBuilder outputString = new StringBuilder("");
        
        AlertsDao altersDao = new AlertsDao();
        Alert alert = altersDao.getAlertByName(alertName);
        
        if (alert != null) {
            outputString.append("<b>Alert Name</b> = ").append(StatsAggHtmlFramework.htmlEncode(alert.getName())).append("<br>");
            
            outputString.append("<b>Caution Acknowledged</b> = ");
            if (alert.isCautionAcknowledged() == null) outputString.append("N/A");
            else if (alert.isCautionAcknowledged()) outputString.append("Yes");   
            else if (!alert.isCautionAcknowledged()) outputString.append("No");    
            outputString.append("<br>");
            
            outputString.append("<b>Danger Acknowledged</b> = ");
            if (alert.isDangerAcknowledged() == null) outputString.append("N/A");
            else if (alert.isDangerAcknowledged()) outputString.append("Yes");   
            else if (!alert.isDangerAcknowledged()) outputString.append("No");    
            outputString.append("<br>");
            
            outputString.append("<hr>");

            String cautionBody = getCautionAlertAssociations_Body(alert);
            outputString.append(cautionBody);
            
            outputString.append("<hr>");
            
            String dangerBody = getDangerAlertAssociations_Body(alert);
            outputString.append(dangerBody);
        }
        
        return outputString.toString();
    }
    
    private String getCautionAlertAssociations(String alertName) {
        
        if (alertName == null) {
            return "";
        }
        
        StringBuilder outputString = new StringBuilder("");
        
        AlertsDao altersDao = new AlertsDao();
        Alert alert = altersDao.getAlertByName(alertName);
        
        if (alert != null) {
            outputString.append("<b>Alert Name</b> = ").append(StatsAggHtmlFramework.htmlEncode(alert.getName())).append("<br>");
            
            outputString.append("<b>Caution Acknowledged</b> = ");
            if (alert.isCautionAcknowledged() == null) outputString.append("N/A");
            else if (alert.isCautionAcknowledged()) outputString.append("Yes");   
            else if (!alert.isCautionAcknowledged()) outputString.append("No");    
            outputString.append("<br>");
            
            String body = getCautionAlertAssociations_Body(alert);
            outputString.append(body);
        }
        
        return outputString.toString();
    }
    
    private String getCautionAlertAssociations_Body(Alert alert) {
        
        if ((alert == null) || (alert.getId() == null)) {
            return "";
        }
        
        StringBuilder outputString = new StringBuilder("");
        
        List<String> activeCautionAlertMetricKeys = null;
        TreeSet<String> activeCautionAlertMetricKeysSorted = new TreeSet<>();
        synchronized(GlobalVariables.activeCautionAlertMetricKeysByAlertId) {
            activeCautionAlertMetricKeys = GlobalVariables.activeCautionAlertMetricKeysByAlertId.get(alert.getId());

            if (activeCautionAlertMetricKeys != null) {
                activeCautionAlertMetricKeysSorted = new TreeSet<>(activeCautionAlertMetricKeys);
            }
        }

        Map<String,BigDecimal> activeCautionAlertMetricValuesLocal = null;
        synchronized(GlobalVariables.activeCautionAlertMetricValues) {
            activeCautionAlertMetricValuesLocal = new HashMap<>(GlobalVariables.activeCautionAlertMetricValues);
        }

        if (activeCautionAlertMetricKeys == null) {
            outputString.append("<b>Total Triggered Caution Metrics</b> = ").append("0");
        }
        else {
            outputString.append("<b>Total Triggered Caution Metrics</b> = ").append(activeCautionAlertMetricKeysSorted.size()).append("<br><br>");

            if (activeCautionAlertMetricKeysSorted.size() > 0) {
                outputString.append("<b>Triggered Metrics...</b>").append("<br>");

                int associationOutputCounter = 0;
                outputString.append("<ul>");

                for (String activeCautionAlertMetricKey : activeCautionAlertMetricKeysSorted) {

                    if (associationOutputCounter < 1000)  {
                        String metricValueString = null;
                        
                        BigDecimal alertMetricValue = activeCautionAlertMetricValuesLocal.get(activeCautionAlertMetricKey + "-" + alert.getId());
                        if (alertMetricValue != null) {
                            metricValueString = Alert.getCautionMetricValueString_WithLabel(alert, alertMetricValue);
                        }

                        outputString.append("<li>");
                        outputString.append("<a href=\"MetricRecentValues?MetricKey=").append(StatsAggHtmlFramework.urlEncode(activeCautionAlertMetricKey)).append("\">");
                        outputString.append(StatsAggHtmlFramework.htmlEncode(activeCautionAlertMetricKey)).append("</a>");
                        outputString.append("&nbsp;=&nbsp;").append(metricValueString);
                        outputString.append("</li>");
                    }
                    else {
                        break;
                    }

                    associationOutputCounter++;
                }

                int numAssociationsNotOutputted = activeCautionAlertMetricKeysSorted.size() - associationOutputCounter;
                if (numAssociationsNotOutputted > 0) {
                    outputString.append("<li>").append(numAssociationsNotOutputted).append(" more...").append("</li>");
                }

                outputString.append("</ul>");
            }
        }

        return outputString.toString();
    }

    private String getDangerAlertAssociations(String alertName) {
        
        if (alertName == null) {
            return "";
        }
        
        StringBuilder outputString = new StringBuilder("");
        
        AlertsDao altersDao = new AlertsDao();
        Alert alert = altersDao.getAlertByName(alertName);
        
        if (alert != null) {
            outputString.append("<b>Name</b> = ").append(StatsAggHtmlFramework.htmlEncode(alert.getName())).append("<br>");
            
            outputString.append("<b>Danger Acknowledged</b> = ");
            if (alert.isDangerAcknowledged() == null) outputString.append("N/A");
            else if (alert.isDangerAcknowledged()) outputString.append("Yes");   
            else if (!alert.isDangerAcknowledged()) outputString.append("No");    
            outputString.append("<br>");
            
            String body = getDangerAlertAssociations_Body(alert);
            outputString.append(body);
        }
        
        return outputString.toString();
    }
    
    private String getDangerAlertAssociations_Body(Alert alert) {
        
        if ((alert == null) || (alert.getId() == null)) {
            return "";
        }
        
        StringBuilder outputString = new StringBuilder("");
        
        List<String> activeDangerAlertMetricKeys = null;
        TreeSet<String> activeDangerAlertMetricKeysSorted = new TreeSet<>();
        synchronized(GlobalVariables.activeDangerAlertMetricKeysByAlertId) {
            activeDangerAlertMetricKeys = GlobalVariables.activeDangerAlertMetricKeysByAlertId.get(alert.getId());

            if (activeDangerAlertMetricKeys != null) {
                activeDangerAlertMetricKeysSorted = new TreeSet<>(activeDangerAlertMetricKeys);
            }
        }

        Map<String,BigDecimal> activeDangerAlertMetricValuesLocal = null;
        synchronized(GlobalVariables.activeDangerAlertMetricValues) {
            activeDangerAlertMetricValuesLocal = new HashMap<>(GlobalVariables.activeDangerAlertMetricValues);
        }

        if (activeDangerAlertMetricKeys == null) {
            outputString.append("<b>Total Triggered Danger Metrics</b> = ").append("0");
        }
        else {
            outputString.append("<b>Total Triggered Danger Metrics</b> = ").append(activeDangerAlertMetricKeysSorted.size()).append("<br><br>");

            if (activeDangerAlertMetricKeysSorted.size() > 0) {
                outputString.append("<b>Triggered Metrics...</b>").append("<br>");

                int associationOutputCounter = 0;
                outputString.append("<ul>");

                for (String activeDangerAlertMetricKey : activeDangerAlertMetricKeysSorted) {

                    if (associationOutputCounter < 1000)  {
                        String metricValueString = null;
                        
                        BigDecimal alertMetricValue = activeDangerAlertMetricValuesLocal.get(activeDangerAlertMetricKey + "-" + alert.getId());
                        if (alertMetricValue != null) {
                            metricValueString = Alert.getDangerMetricValueString_WithLabel(alert, alertMetricValue);
                        }

                        outputString.append("<li>");
                        outputString.append("<a href=\"MetricRecentValues?MetricKey=").append(StatsAggHtmlFramework.urlEncode(activeDangerAlertMetricKey)).append("\">");
                        outputString.append(StatsAggHtmlFramework.htmlEncode(activeDangerAlertMetricKey)).append("</a>");
                        outputString.append("&nbsp;=&nbsp;").append(metricValueString);
                        outputString.append("</li>");
                    }
                    else {
                        break;
                    }

                    associationOutputCounter++;
                }

                int numAssociationsNotOutputted = activeDangerAlertMetricKeysSorted.size() - associationOutputCounter;
                if (numAssociationsNotOutputted > 0) {
                    outputString.append("<li>").append(numAssociationsNotOutputted).append(" more...").append("</li>");
                }

                outputString.append("</ul>");
            }
        }
        
        return outputString.toString();
    }
    
}
