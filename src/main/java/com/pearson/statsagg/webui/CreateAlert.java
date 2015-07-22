package com.pearson.statsagg.webui;

import java.io.PrintWriter;
import java.math.BigDecimal;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database.alerts.Alert;
import com.pearson.statsagg.database.alerts.AlertsDao;
import com.pearson.statsagg.database.metric_group.MetricGroup;
import com.pearson.statsagg.database.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database.notifications.NotificationGroup;
import com.pearson.statsagg.database.notifications.NotificationGroupsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.StackTrace;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "CreateAlert", urlPatterns = {"/CreateAlert"})
public class CreateAlert extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CreateAlert.class.getName());
    
    public static final String PAGE_NAME = "Create Alert";
    
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
            StringBuilder htmlBuilder = new StringBuilder();

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            
            Alert alert = null;
            String name = request.getParameter("Name");
            if (name != null) {
                AlertsDao alertsDao = new AlertsDao();
                alert = alertsDao.getAlertByName(name.trim());
            }        

            String htmlBodyContents = buildCreateAlertHtml(alert);
            String htmlBody = statsAggHtmlFramework.createHtmlBody(htmlBodyContents);
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
    
    protected void processPostRequest(HttpServletRequest request, HttpServletResponse response) {
        
        if ((request == null) || (response == null)) {
            return;
        }
        
        PrintWriter out = null;
        
        try {
            String result = parseAndAlterAlert(request);
            
            response.setContentType("text/html");     
            
            StringBuilder htmlBuilder = new StringBuilder();
            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            String htmlBodyContent = statsAggHtmlFramework.buildHtmlBodyForPostResult(PAGE_NAME, StatsAggHtmlFramework.htmlEncode(result), "Alerts", Alerts.PAGE_NAME);
            String htmlBody = statsAggHtmlFramework.createHtmlBody(htmlBodyContent);
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

    private String buildCreateAlertHtml(Alert alert) {
        
        StringBuilder htmlBody = new StringBuilder();
        
        htmlBody.append(
            "<div id=\"page-content-wrapper\">\n" +
            "  <!-- Keep all page content within the page-content inset div! -->\n" +
            "  <div class=\"page-content inset statsagg_page_content_font\">\n" +
            "  <div class=\"content-header\"> \n" +
            "    <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "  </div>\n " +
            "  <form action=\"CreateAlert\" method=\"POST\">\n" +
            "    <div class=\"row create-alert-form-row\">\n");

        if ((alert != null) && (alert.getName() != null) && !alert.getName().isEmpty()) {
            htmlBody.append("<input type=\"hidden\" name=\"Old_Name\" value=\"").append(Encode.forHtmlAttribute(alert.getName())).append("\">");
        }
        
        
        // start column 1
        htmlBody.append(
            "<div class=\"col-md-4\">\n" +
            "  <div class=\"panel panel-default\">\n" +
            "    <div class=\"panel-heading\"><b>Core Alert Criteria</b></div>\n" +
            "    <div class=\"panel-body\">");
            
        
        // name
        htmlBody.append(      
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Name</label>\n" +
            "  <input class=\"form-control-statsagg\" placeholder=\"Enter a unique name for this alert.\" name=\"Name\" id=\"Name\" ");
        
        if ((alert != null) && (alert.getName() != null)) {
            htmlBody.append(" value=\"").append(Encode.forHtmlAttribute(alert.getName())).append("\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // description
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Description</label>\n" +
            "  <textarea class=\"form-control-statsagg\" rows=\"3\" name=\"Description\" id=\"Description\">");

        if ((alert != null) && (alert.getDescription() != null)) {
            htmlBody.append(Encode.forHtmlAttribute(alert.getDescription()));
        }

        htmlBody.append("</textarea>\n");
        htmlBody.append("</div>\n");

        
        // metric group name
        htmlBody.append(
            "<div class=\"form-group\" id=\"MetricGroupNameLookup\">\n" +
            "  <label class=\"label_small_margin\">Metric group name</label>\n" +
            "  <input class=\"typeahead form-control-statsagg\" placeholder=\"Enter the exact name of the metric group that is associated with this alert.\" autocomplete=\"off\" name=\"MetricGroupName\" id=\"MetricGroupName\" ");

        if ((alert != null) && (alert.getMetricGroupId() != null)) {
            MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
            MetricGroup metricGroup = metricGroupsDao.getMetricGroup(alert.getMetricGroupId());

            if ((metricGroup != null) && (metricGroup.getName() != null)) {
                htmlBody.append(" value=\"").append(Encode.forHtmlAttribute(metricGroup.getName())).append("\"");
            }
        }

        htmlBody.append(">\n</div>\n");


        // alert type
        htmlBody.append("<div class=\"form-group\">\n");
                
        htmlBody.append("<label class=\"label_small_margin\">Alert type:&nbsp;&nbsp;</label>\n");
        
        htmlBody.append("<input type=\"radio\" id=\"CreateAlert_Type_Availability\" name=\"CreateAlert_Type\" value=\"Availability\" ");
        if ((alert != null) && (alert.getAlertType() != null) && (alert.getAlertType() == Alert.TYPE_AVAILABILITY)) htmlBody.append(" checked=\"checked\"");
        htmlBody.append("> Availability &nbsp;&nbsp;&nbsp;\n");
        
        htmlBody.append("<input type=\"radio\" id=\"CreateAlert_Type_Threshold\" name=\"CreateAlert_Type\" value=\"Threshold\" ");
        if ((alert != null) && (alert.getAlertType() != null) && (alert.getAlertType() == Alert.TYPE_THRESHOLD)) htmlBody.append(" checked=\"checked\"");
        htmlBody.append("> Threshold\n");

        htmlBody.append("</div>");
        
        
        // is enabled?
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Is alert enabled?&nbsp;&nbsp;</label>\n" +
            "  <input name=\"Enabled\" id=\"Enabled\" type=\"checkbox\" ");

        if (((alert != null) && (alert.isEnabled() != null) && alert.isEnabled()) || 
                (alert == null) || (alert.isEnabled() == null)) {
            htmlBody.append(" checked=\"checked\"");
        }

        htmlBody.append(">\n</div>\n");

        
        // is caution alerting enabled?
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Is caution alerting enabled?&nbsp;&nbsp;</label>\n" +
            "  <input name=\"CautionEnabled\" id=\"CautionEnabled\" type=\"checkbox\" ");

        if (((alert != null) && (alert.isCautionEnabled() != null) && alert.isCautionEnabled()) || 
                (alert == null) || (alert.isCautionEnabled() == null)) {
            htmlBody.append(" checked=\"checked\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // is danger alerting enabled?
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Is danger alerting enabled?&nbsp;&nbsp;</label>\n" +
            "  <input name=\"DangerEnabled\" id=\"DangerEnabled\" type=\"checkbox\" ");

        if (((alert != null) && (alert.isDangerEnabled() != null) && alert.isDangerEnabled()) || 
                (alert == null) || (alert.isDangerEnabled() == null)) {
            htmlBody.append(" checked=\"checked\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // alert on positive?
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Alert on positive?&nbsp;&nbsp;</label>\n" +
            "  <input name=\"AlertOnPositive\" id=\"AlertOnPositive\" type=\"checkbox\" ");
        
        if (((alert != null) && (alert.isAlertOnPositive() != null) && alert.isAlertOnPositive()) || 
                (alert == null) || (alert.isAlertOnPositive() == null)) {
            htmlBody.append(" checked=\"checked\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // allow resend alert?
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Resend alert?&nbsp;&nbsp;</label>\n" +
            "  <input name=\"AllowResendAlert\" id=\"AllowResendAlert\" type=\"checkbox\" ");
        
        if ((alert != null) && (alert.isAllowResendAlert()!= null) && alert.isAllowResendAlert()) {
            htmlBody.append(" checked=\"checked\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // send alert every num milliseconds
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Resent alert every... (in seconds)</label>\n" +
            "  <input class=\"form-control-statsagg\" placeholder=\"If 'resend alert' is enabled, how often should the alert be resent?\" name=\"SendAlertEveryNumMilliseconds\" id=\"SendAlertEveryNumMilliseconds\"");

        if ((alert != null) && (alert.getSendAlertEveryNumMilliseconds() != null)) {
            BigDecimal sendAlertEveryNumMilliseconds = new BigDecimal(alert.getSendAlertEveryNumMilliseconds());
            BigDecimal sendAlertEveryNumSeconds = sendAlertEveryNumMilliseconds.divide(new BigDecimal(1000));
            htmlBody.append(" value=\"").append(sendAlertEveryNumSeconds.stripTrailingZeros().toPlainString()).append("\"");
        }
        
        htmlBody.append(">\n</div>\n");

        // end column 1
        htmlBody.append(
            "    </div>\n" +
            "  </div>\n" +        
            "</div>\n");
        
        
        // start column 2
        htmlBody.append(
            "<div class=\"col-md-4\" id=\"CautionCriteria\" >\n" +
            "  <div class=\"panel panel-warning\">\n" +
            "    <div class=\"panel-heading\"><b>Caution Criteria</b> " +
            "    <a id=\"CautionPreview\" name=\"CautionPreview\" class=\"iframe cboxElement statsagg_caution_preview pull-right\" href=\"#\" onclick=\"generateAlertPreviewLink('Caution');\">Preview</a>" + 
            "    </div>" +
            "    <div class=\"panel-body\">");
        
        
        // warning for when no alert-type is selected
        htmlBody.append("<label id=\"CautionNoAlertTypeSelected_Label\" class=\"label_small_margin\">Please select an alert type</label>\n");

        
        // caution notification group name
        htmlBody.append(
            "<div class=\"form-group\" id=\"CautionNotificationGroupNameLookup\">\n" +
            "  <label id=\"CautionNotificationGroupName_Label\" class=\"label_small_margin\">Notification group name</label>\n" +
            "  <input class=\"typeahead form-control-statsagg\" placeholder=\"Enter the exact name of the notification group that is associated with this alert.\" autocomplete=\"off\" name=\"CautionNotificationGroupName\" id=\"CautionNotificationGroupName\" ");

        if ((alert != null) && (alert.getCautionNotificationGroupId() != null)) {
            NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
            NotificationGroup notificationGroup = notificationGroupsDao.getNotificationGroup(alert.getCautionNotificationGroupId());

            if ((notificationGroup != null) && (notificationGroup.getName() != null)) {
                htmlBody.append(" value=\"").append(Encode.forHtmlAttribute(notificationGroup.getName())).append("\"");
            }
        }
        
        htmlBody.append(">\n</div>\n");
        
        
        // caution window duration
        htmlBody.append("<label id=\"CautionWindowDuration_Label\" class=\"label_small_margin\">Window duration</label>\n");
        htmlBody.append("<div>\n");
        
        htmlBody.append(
            "<div class=\"form-group col-xs-6\">\n" +
            "  <input class=\"form-control-statsagg\" placeholder=\"A rolling time window between 'now' and 'X'  time units ago. Values that fall in this window are used in alert evaluation.\" name=\"CautionWindowDuration\" id=\"CautionWindowDuration\" ");

        if ((alert != null) && (alert.getCautionWindowDuration() != null)) {
            BigDecimal cautionWindowDurationMs = new BigDecimal(alert.getCautionWindowDuration());
            BigDecimal cautionWindowDurationSeconds = cautionWindowDurationMs.divide(new BigDecimal(1000));
            htmlBody.append(" value=\"").append(cautionWindowDurationSeconds.stripTrailingZeros().toPlainString()).append("\"");
        }
        
        htmlBody.append(">\n</div>\n");
        
        
        // caution window duration time unit
        htmlBody.append(
            "<div class=\"form-group col-xs-6\">\n" +
            "  <select class=\"form-control-statsagg\" name=\"CautionWindowDurationTimeUnit\" id=\"CautionWindowDurationTimeUnit\">\n");

        
        if ((alert != null) && (Alert.getTimeUnitStringFromCode(alert.getCautionWindowDurationTimeUnit()) != null)) {
            String whiteSpace = "							  ";
            String cautionTimeUnitString = Alert.getTimeUnitStringFromCode(alert.getCautionWindowDurationTimeUnit());
            
            if (cautionTimeUnitString.equalsIgnoreCase("Seconds")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">Seconds</option>\n");
            else htmlBody.append(whiteSpace).append("<option>Seconds</option>\n");

            if (cautionTimeUnitString.equalsIgnoreCase("Minutes")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">Minutes</option>\n");
            else htmlBody.append(whiteSpace).append("<option>Minutes</option>\n");

            if (cautionTimeUnitString.equalsIgnoreCase("Hours")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">Hours</option>\n");
            else htmlBody.append(whiteSpace).append("<option>Hours</option>\n");

            if (cautionTimeUnitString.equalsIgnoreCase("Days")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">Days</option>\n");
            else htmlBody.append(whiteSpace).append("<option>Days</option>\n");
        }
        else {
            htmlBody.append(
                "<option>Seconds</option>\n" +
                "<option>Minutes</option>\n" +
                "<option>Hours</option>\n" +
                "<option>Days</option>\n"
            );
        }
        
        htmlBody.append("</select>\n");
        htmlBody.append("</div>\n");
        htmlBody.append("</div>\n");
        
        
        // caution stop tracking after
        htmlBody.append(   
            "<div class=\"form-group\"> \n" +
            "  <label id=\"CautionStopTrackingAfter_Label\" class=\"label_small_margin\">Stop tracking after... (in seconds)</label>\n" +
            "  <input class=\"form-control-statsagg\" placeholder=\"After a metric has not been seen for X seconds, stop alerting on it.\" name=\"CautionStopTrackingAfter\" id=\"CautionStopTrackingAfter\"");

        if ((alert != null) && (alert.getCautionStopTrackingAfter() != null)) {
            BigDecimal cautionStopTrackingAfterMs = new BigDecimal(alert.getCautionStopTrackingAfter());
            BigDecimal cautionStopTrackingAfterSeconds = cautionStopTrackingAfterMs.divide(new BigDecimal(1000));
            htmlBody.append(" value=\"").append(cautionStopTrackingAfterSeconds.stripTrailingZeros().toPlainString()).append("\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // caution minimum sample count
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label id=\"CautionMinimumSampleCount_Label\" class=\"label_small_margin\">Minimum sample count</label>\n" +
            "  <input class=\"form-control-statsagg\" placeholder=\"An alert can only be triggered if there are at least X samples within specified the window duration.\" name=\"CautionMinimumSampleCount\" id=\"CautionMinimumSampleCount\"");

        if ((alert != null) && (alert.getCautionMinimumSampleCount() != null)) {
            htmlBody.append(" value=\"").append(alert.getCautionMinimumSampleCount()).append("\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // caution operator
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label id=\"CautionOperator_Label\" class=\"label_small_margin\">Operator</label>\n" +
            "  <select class=\"form-control-statsagg\" name=\"CautionOperator\" id=\"CautionOperator\">\n");

        htmlBody.append("<option");
        if ((alert != null) && (alert.getOperatorString(Alert.CAUTION, true, false) != null) && alert.getOperatorString(Alert.CAUTION, true, false).equalsIgnoreCase(">")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append(">&nbsp;&nbsp;(greater than)</option>\n");
        
        htmlBody.append("<option");
        if ((alert != null) && (alert.getOperatorString(Alert.CAUTION, true, false) != null) && alert.getOperatorString(Alert.CAUTION, true, false).equalsIgnoreCase(">=")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append(">=&nbsp;&nbsp;(greater than or equal to)</option>\n");
        
        htmlBody.append("<option");
        if ((alert != null) && (alert.getOperatorString(Alert.CAUTION, true, false) != null) && alert.getOperatorString(Alert.CAUTION, true, false).equalsIgnoreCase("<")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append("<&nbsp;&nbsp;(less than)</option>\n");
        
        htmlBody.append("<option");
        if ((alert != null) && (alert.getOperatorString(Alert.CAUTION, true, false) != null) && alert.getOperatorString(Alert.CAUTION, true, false).equalsIgnoreCase("<=")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append("<=&nbsp;&nbsp;(less than or equal to)</option>\n");

        htmlBody.append("<option");
        if ((alert != null) && (alert.getOperatorString(Alert.CAUTION, true, false) != null) && alert.getOperatorString(Alert.CAUTION, true, false).equalsIgnoreCase("=")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append("=&nbsp;&nbsp;(equal to)</option>\n");

        htmlBody.append("</select>\n");
        htmlBody.append("</div>\n");     
        
        
        // caution combination
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label id=\"CautionCombination_Label\" class=\"label_small_margin\">Combination</label>\n" +
            "  <select class=\"form-control-statsagg\" name=\"CautionCombination\" id=\"CautionCombination\">\n");

        if ((alert != null) && (alert.getCombinationString(Alert.CAUTION) != null)) {
            String whiteSpace = "							  ";

            if (alert.getCombinationString(Alert.CAUTION).equalsIgnoreCase("Any")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">Any</option>\n");
            else htmlBody.append(whiteSpace).append("<option>Any</option>\n");

            if (alert.getCombinationString(Alert.CAUTION).equalsIgnoreCase("All")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">All</option>\n");
            else htmlBody.append(whiteSpace).append("<option>All</option>\n");

            if (alert.getCombinationString(Alert.CAUTION).equalsIgnoreCase("Average")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">Average</option>\n");
            else htmlBody.append(whiteSpace).append("<option>Average</option>\n");

            if (alert.getCombinationString(Alert.CAUTION).equalsIgnoreCase("At most")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">At most</option>\n");
            else htmlBody.append(whiteSpace).append("<option>At most</option>\n");

            if (alert.getCombinationString(Alert.CAUTION).equalsIgnoreCase("At least")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">At least</option>\n");
            else htmlBody.append(whiteSpace).append("<option>At least</option>\n");
        }
        else {
            htmlBody.append(
                "<option>Any</option>\n" +
                "<option>All</option>\n" +
                "<option>Average</option>\n" +
                "<option>At most</option>\n" +
                "<option>At least</option>\n"
            );
        }

        htmlBody.append("</select>\n");
        htmlBody.append("</div>\n");        
        
        
        // caution combination count
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label id=\"CautionCombinationCount_Label\" class=\"label_small_margin\">Combination count</label>\n" +
            "  <input class=\"form-control-statsagg\" placeholder=\"If using a caution combination of 'at most' or 'at least', then you must specify a count.\" name=\"CautionCombinationCount\" id=\"CautionCombinationCount\" ");

        if ((alert != null) && (alert.getCautionCombinationCount() != null)) {
            htmlBody.append(" value=\"").append(alert.getCautionCombinationCount()).append("\"");
        }
        
        htmlBody.append(">\n</div>\n");

        
        // caution threshold
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label id=\"CautionThreshold_Label\" class=\"label_small_margin\">Threshold</label>\n" +
            "  <input class=\"form-control-statsagg\" placeholder=\"The numeric threshold that, if crossed, will trigger the caution alert.\" name=\"CautionThreshold\" id=\"CautionThreshold\" ");

        if ((alert != null) && (alert.getCautionThreshold() != null)) {
            htmlBody.append(" value=\"").append(alert.getCautionThreshold().stripTrailingZeros().toPlainString()).append("\"");
        }
                
        htmlBody.append(">\n</div>\n");
        
        // end column 2
        htmlBody.append("</div>\n</div>\n</div>\n");

               
        // start column 3
        htmlBody.append(     
            "<div class=\"col-md-4\" id=\"DangerCriteria\" >\n" +
            "  <div class=\"panel panel-danger\">\n" +
            "    <div class=\"panel-heading\"><b>Danger Criteria</b>" +
            "      <a id=\"DangerPreview\" name=\"DangerPreview\" class=\"iframe cboxElement statsagg_danger_preview pull-right\" href=\"#\" onclick=\"generateAlertPreviewLink('Danger');\">Preview</a>" + 
            "    </div>" +
            "    <div class=\"panel-body\">");
        
        
        // warning for when no alert-type is selected
        htmlBody.append("<label id=\"DangerNoAlertTypeSelected_Label\" class=\"label_small_margin\">Please select an alert type</label>\n");
        
        
        // danger notification group name
        htmlBody.append(
            "<div class=\"form-group\" id=\"DangerNotificationGroupNameLookup\">\n" +
            "  <label id=\"DangerNotificationGroupName_Label\" class=\"label_small_margin\">Notification group name</label>\n" +
            "  <input class=\"typeahead form-control-statsagg\" placeholder=\"Enter the exact name of the notification group that is associated with this alert.\" autocomplete=\"off\" name=\"DangerNotificationGroupName\" id=\"DangerNotificationGroupName\" ");

        if ((alert != null) && (alert.getDangerNotificationGroupId() != null)) {
            NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
            NotificationGroup notificationGroup = notificationGroupsDao.getNotificationGroup(alert.getDangerNotificationGroupId());

            if ((notificationGroup != null) && (notificationGroup.getName() != null)) {
                htmlBody.append(" value=\"").append(Encode.forHtmlAttribute(notificationGroup.getName())).append("\"");
            }
        }
        
        htmlBody.append(">\n</div>\n");
        
        
        // danger window duration
        htmlBody.append("<label id=\"DangerWindowDuration_Label\" class=\"label_small_margin\">Window duration</label>\n");
        htmlBody.append("<div>\n");
        
        htmlBody.append(   
            "<div class=\"form-group col-xs-6\"> \n" +
            "  <input class=\"form-control-statsagg\" placeholder=\"A rolling time window between 'now' and 'X' time units ago. Values that fall in this window are used in alert evaluation.\" name=\"DangerWindowDuration\" id=\"DangerWindowDuration\"");

        if ((alert != null) && (alert.getDangerWindowDuration() != null)) {
            BigDecimal dangerWindowDurationMs = new BigDecimal(alert.getDangerWindowDuration());
            BigDecimal dangerWindowDurationSeconds = dangerWindowDurationMs.divide(new BigDecimal(1000));
            htmlBody.append(" value=\"").append(dangerWindowDurationSeconds.stripTrailingZeros().toPlainString()).append("\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // danger window duration time unit
        htmlBody.append(
            "<div class=\"form-group col-xs-6\">\n" +
            "  <select class=\"form-control-statsagg\" name=\"DangerWindowDurationTimeUnit\" id=\"DangerWindowDurationTimeUnit\">\n");

        if ((alert != null) && (Alert.getTimeUnitStringFromCode(alert.getDangerWindowDurationTimeUnit()) != null)) {
            String whiteSpace = "							  ";
            String dangerTimeUnitString = Alert.getTimeUnitStringFromCode(alert.getDangerWindowDurationTimeUnit());

            if (dangerTimeUnitString.equalsIgnoreCase("Seconds")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">Seconds</option>\n");
            else htmlBody.append(whiteSpace).append("<option>Seconds</option>\n");

            if (dangerTimeUnitString.equalsIgnoreCase("Minutes")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">Minutes</option>\n");
            else htmlBody.append(whiteSpace).append("<option>Minutes</option>\n");

            if (dangerTimeUnitString.equalsIgnoreCase("Hours")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">Hours</option>\n");
            else htmlBody.append(whiteSpace).append("<option>Hours</option>\n");

            if (dangerTimeUnitString.equalsIgnoreCase("Days")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">Days</option>\n");
            else htmlBody.append(whiteSpace).append("<option>Days</option>\n");
        }
        else {
            htmlBody.append(
                "<option>Seconds</option>\n" +
                "<option>Minutes</option>\n" +
                "<option>Hours</option>\n" +
                "<option>Days</option>\n"
            );
        }
        
        htmlBody.append("</select>\n");
        htmlBody.append("</div>\n");
        htmlBody.append("</div>\n");
        
        
        // danger stop tracking after
        htmlBody.append("<label id=\"DangerStopTrackingAfter_Label\" class=\"label_small_margin\">Stop tracking after...</label>\n");
        htmlBody.append("<div>\n");
        
        htmlBody.append(   
            "<div class=\"form-group col-xs-6\"> \n" +
            "  <input class=\"form-control-statsagg\" placeholder=\"After a metric has not been seen for X time units, stop alerting on it.\" name=\"DangerStopTrackingAfter\" id=\"DangerStopTrackingAfter\"");

        if ((alert != null) && (alert.getDangerStopTrackingAfter() != null)) {
            BigDecimal dangerStopTrackingAfterMs = new BigDecimal(alert.getDangerStopTrackingAfter());
            BigDecimal dangerStopTrackingAfterSeconds = dangerStopTrackingAfterMs.divide(new BigDecimal(1000));
            htmlBody.append(" value=\"").append(dangerStopTrackingAfterSeconds.stripTrailingZeros().toPlainString()).append("\"");
        }

        htmlBody.append(">\n</div>\n");
        

        // danger 'stop tracking after' time unit
        htmlBody.append(
            "<div class=\"form-group col-xs-6\">\n" +
            "  <select class=\"form-control-statsagg\" name=\"DangerStopTrackingAfterTimeUnit\" id=\"DangerStopTrackingAfterTimeUnit\">\n");

        if ((alert != null) && (Alert.getTimeUnitStringFromCode(alert.getDangerStopTrackingAfterTimeUnit()) != null)) {
            String whiteSpace = "							  ";
            String dangerTimeUnitString = Alert.getTimeUnitStringFromCode(alert.getDangerStopTrackingAfterTimeUnit());

            if (dangerTimeUnitString.equalsIgnoreCase("Seconds")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">Seconds</option>\n");
            else htmlBody.append(whiteSpace).append("<option>Seconds</option>\n");

            if (dangerTimeUnitString.equalsIgnoreCase("Minutes")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">Minutes</option>\n");
            else htmlBody.append(whiteSpace).append("<option>Minutes</option>\n");

            if (dangerTimeUnitString.equalsIgnoreCase("Hours")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">Hours</option>\n");
            else htmlBody.append(whiteSpace).append("<option>Hours</option>\n");

            if (dangerTimeUnitString.equalsIgnoreCase("Days")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">Days</option>\n");
            else htmlBody.append(whiteSpace).append("<option>Days</option>\n");
        }
        else {
            htmlBody.append(
                "<option>Seconds</option>\n" +
                "<option>Minutes</option>\n" +
                "<option>Hours</option>\n" +
                "<option>Days</option>\n"
            );
        }
        
        htmlBody.append("</select>\n");
        htmlBody.append("</div>\n");
        htmlBody.append("</div>\n");
        
        
        // danger minimum sample count
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label id=\"DangerMinimumSampleCount_Label\" class=\"label_small_margin\">Minimum sample count</label>\n" +
            "  <input class=\"form-control-statsagg\" placeholder=\"An alert can only be triggered if there are at least X samples within specified the window duration.\" name=\"DangerMinimumSampleCount\" id=\"DangerMinimumSampleCount\"");

        if ((alert != null) && (alert.getDangerMinimumSampleCount() != null)) {
            htmlBody.append(" value=\"").append(alert.getDangerMinimumSampleCount()).append("\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // danger operator 
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label id=\"DangerOperator_Label\" class=\"label_small_margin\">Operator</label>\n" +
            "  <select class=\"form-control-statsagg\" name=\"DangerOperator\" id=\"DangerOperator\">\n");
        
        htmlBody.append("<option");
        if ((alert != null) && (alert.getOperatorString(Alert.DANGER, true, false) != null) && alert.getOperatorString(Alert.DANGER, true, false).equalsIgnoreCase(">")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append(">&nbsp;&nbsp;(greater than)</option>\n");
        
        htmlBody.append("<option");
        if ((alert != null) && (alert.getOperatorString(Alert.DANGER, true, false) != null) && alert.getOperatorString(Alert.DANGER, true, false).equalsIgnoreCase(">=")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append(">=&nbsp;&nbsp;(greater than or equal to)</option>\n");
        
        htmlBody.append("<option");
        if ((alert != null) && (alert.getOperatorString(Alert.DANGER, true, false) != null) && alert.getOperatorString(Alert.DANGER, true, false).equalsIgnoreCase("<")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append("<&nbsp;&nbsp;(less than)</option>\n");
        
        htmlBody.append("<option");
        if ((alert != null) && (alert.getOperatorString(Alert.DANGER, true, false) != null) && alert.getOperatorString(Alert.DANGER, true, false).equalsIgnoreCase("<=")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append("<=&nbsp;&nbsp;(less than or equal to)</option>\n");

        htmlBody.append("<option");
        if ((alert != null) && (alert.getOperatorString(Alert.DANGER, true, false) != null) && alert.getOperatorString(Alert.DANGER, true, false).equalsIgnoreCase("=")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append("=&nbsp;&nbsp;(equal to)</option>\n");

        htmlBody.append("</select>\n");
        htmlBody.append("</div>\n");

        
        // danger combination
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label id=\"DangerCombination_Label\" class=\"label_small_margin\">Combination</label>\n" +
            "  <select class=\"form-control-statsagg\" name=\"DangerCombination\" id=\"DangerCombination\">\n");

        if ((alert != null) && (alert.getCombinationString(Alert.DANGER) != null)) {
            String whiteSpace = "							  ";

            if (alert.getCombinationString(Alert.DANGER).equalsIgnoreCase("Any")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">Any</option>\n");
            else htmlBody.append(whiteSpace).append("<option>Any</option>\n");

            if (alert.getCombinationString(Alert.DANGER).equalsIgnoreCase("All")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">All</option>\n");
            else htmlBody.append(whiteSpace).append("<option>All</option>\n");

            if (alert.getCombinationString(Alert.DANGER).equalsIgnoreCase("Average")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">Average</option>\n");
            else htmlBody.append(whiteSpace).append("<option>Average</option>\n");

            if (alert.getCombinationString(Alert.DANGER).equalsIgnoreCase("At most")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">At most</option>\n");
            else htmlBody.append(whiteSpace).append("<option>At most</option>\n");

            if (alert.getCombinationString(Alert.DANGER).equalsIgnoreCase("At least")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">At least</option>\n");
            else htmlBody.append(whiteSpace).append("<option>At least</option>\n");
        }
        else {
            htmlBody.append(
                "<option>Any</option>\n" +
                "<option>All</option>\n" +
                "<option>Average</option>\n" +
                "<option>At most</option>\n" +
                "<option>At least</option>\n"
            );
        }
        
        htmlBody.append("</select>\n");
        htmlBody.append("</div>\n");

        
        // danger combination count
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label id=\"DangerCombinationCount_Label\" class=\"label_small_margin\">Combination count</label>\n" +
            "  <input class=\"form-control-statsagg\" placeholder=\"If using a danger combination of 'at most' or 'at least', then you must specify a count.\" name=\"DangerCombinationCount\" id=\"DangerCombinationCount\"");

        if ((alert != null) && (alert.getDangerCombinationCount() != null)) {
            htmlBody.append(" value=\"").append(alert.getDangerCombinationCount()).append("\"");
        }

        htmlBody.append(">\n</div>\n");

        
        // danger threshold
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label id=\"DangerThreshold_Label\" class=\"label_small_margin\">Threshold</label>\n" +
            "  <input class=\"form-control-statsagg\" placeholder=\"The numeric threshold that, if crossed, will trigger the danger alert.\" name=\"DangerThreshold\" id=\"DangerThreshold\"");

        if ((alert != null) && (alert.getDangerThreshold() != null)) {
            htmlBody.append(" value=\"").append(alert.getDangerThreshold().stripTrailingZeros().toPlainString()).append("\"");
        }
        
        htmlBody.append(">\n</div>\n");

        
        // end column 3 & form
        htmlBody.append(             
            "      </div>\n" +
            "    </div>\n" +
            "  </div>\n" + 
            "</div>\n" +
            "<button type=\"submit\" class=\"btn btn-default btn-primary statsagg_button_no_shadow statsagg_page_content_font\">Submit</button>" +
            "&nbsp;&nbsp;&nbsp;" +
            "<a href=\"Alerts\" class=\"btn btn-default statsagg_page_content_font\" role=\"button\">Cancel</a>" +
            "</form>\n" +
            "</div>\n" +
            "</div>\n"
            );

        return htmlBody.toString();
    }

    public String parseAndAlterAlert(Object request) {
        
        if (request == null) {
            return null;
        }
        
        String returnString;
        
        Alert alert = getAlertFromAlertParameters(request);
        String oldName = Common.getObjectParameter(request, "Old_Name");

        // insert/update/delete records in the database
        if ((alert != null) && (alert.getName() != null)) {
            AlertsLogic alertsLogic = new AlertsLogic();
            returnString = alertsLogic.alterRecordInDatabase(alert, oldName, false);
            
            if ((GlobalVariables.alertInvokerThread != null) && (AlertsLogic.STATUS_CODE_SUCCESS == alertsLogic.getLastAlterRecordStatus())) {
                GlobalVariables.alertInvokerThread.runAlertThread(false, true);
            }
        }
        else {
            returnString = "Failed to add alert. Reason=\"Field validation failed.\"";
            logger.warn(returnString);
        }
        
        return returnString;
    }
    
    public static Alert getAlertFromAlertParameters(Object request) {
        
        if (request == null) {
            return null;
        }
        
        boolean didEncounterError = false;
        
        Alert alert = new Alert();

        try {
            String parameter;

            parameter = Common.getObjectParameter(request, "Name");
            String trimmedName = parameter.trim();
            alert.setName(trimmedName);
            alert.setUppercaseName(trimmedName.toUpperCase());
            if ((alert.getName() == null) || alert.getName().isEmpty()) didEncounterError = true;
            
            parameter = Common.getObjectParameter(request, "Description");
            if (parameter != null) {
                String trimmedParameter = parameter.trim();
                String description;
                if (trimmedParameter.length() > 100000) description = trimmedParameter.substring(0, 99999);
                else description = trimmedParameter;
                alert.setDescription(description);
            }
            else alert.setDescription("");
                
            parameter = Common.getObjectParameter(request, "MetricGroupName");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
                    MetricGroup metricGroup = metricGroupsDao.getMetricGroupByName(parameterTrimmed);
                    if (metricGroup != null) alert.setMetricGroupId(metricGroup.getId());
                }
            }

            parameter = Common.getObjectParameter(request, "Enabled");
            if ((parameter != null) && parameter.contains("on")) alert.setIsEnabled(true);
            else alert.setIsEnabled(false);

            parameter = Common.getObjectParameter(request, "CautionEnabled");
            if ((parameter != null) && parameter.contains("on")) alert.setIsCautionEnabled(true);
            else alert.setIsCautionEnabled(false);
            
            parameter = Common.getObjectParameter(request, "DangerEnabled");
            if ((parameter != null) && parameter.contains("on")) alert.setIsDangerEnabled(true);
            else alert.setIsDangerEnabled(false);
            
            parameter = Common.getObjectParameter(request, "CreateAlert_Type");
            if ((parameter != null) && parameter.contains("Availability")) alert.setAlertType(Alert.TYPE_AVAILABILITY);
            else if ((parameter != null) && parameter.contains("Threshold")) alert.setAlertType(Alert.TYPE_THRESHOLD);
            
            parameter = Common.getObjectParameter(request, "AlertOnPositive");
            if ((parameter != null) && parameter.contains("on")) alert.setAlertOnPositive(true);
            else alert.setAlertOnPositive(false);

            parameter = Common.getObjectParameter(request, "AllowResendAlert");
            if ((parameter != null) && parameter.contains("on")) alert.setAllowResendAlert(true);
            else alert.setAllowResendAlert(false);

            parameter = Common.getObjectParameter(request, "SendAlertEveryNumMilliseconds");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {                
                    BigDecimal bigDecimalValueMs = new BigDecimal(parameterTrimmed);
                    BigDecimal bigDecimalValueInSeconds = bigDecimalValueMs.multiply(new BigDecimal(1000));
                    alert.setSendAlertEveryNumMilliseconds(bigDecimalValueInSeconds.intValue());
                }
            }
            
            parameter = Common.getObjectParameter(request, "CautionNotificationGroupName");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {
                    NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
                    NotificationGroup notificationGroup = notificationGroupsDao.getNotificationGroupByName(parameterTrimmed);
                    if ((notificationGroup != null) && (notificationGroup.getId() != null)) alert.setCautionNotificationGroupId(notificationGroup.getId());
                }
            }
            
            parameter = Common.getObjectParameter(request, "CautionWindowDuration");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    BigDecimal bigDecimalValueMs = new BigDecimal(parameterTrimmed);
                    BigDecimal bigDecimalValueInSeconds = bigDecimalValueMs.multiply(new BigDecimal(1000));
                    alert.setCautionWindowDuration(bigDecimalValueInSeconds.longValue());
                }
            }
            
            parameter = Common.getObjectParameter(request, "CautionWindowDurationTimeUnit");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = Alert.getTimeUnitCodeFromString(parameterTrimmed);
                    alert.setCautionWindowDurationTimeUnit(intValue);
                }
            }
            
            parameter = Common.getObjectParameter(request, "CautionStopTrackingAfter");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {      
                    BigDecimal bigDecimalValueMs = new BigDecimal(parameterTrimmed);
                    BigDecimal bigDecimalValueInSeconds = bigDecimalValueMs.multiply(new BigDecimal(1000));
                    alert.setCautionStopTrackingAfter(bigDecimalValueInSeconds.longValue());
                }
            }
            
            parameter = Common.getObjectParameter(request, "CautionStopTrackingAfterTimeUnit");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = Alert.getTimeUnitCodeFromString(parameterTrimmed);
                    alert.setCautionStopTrackingAfterTimeUnit(intValue);
                }
            }
            
            parameter = Common.getObjectParameter(request, "CautionMinimumSampleCount");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = Integer.parseInt(parameterTrimmed);
                    alert.setCautionMinimumSampleCount(intValue);
                }
            }
            
            parameter = Common.getObjectParameter(request, "CautionOperator");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = Alert.getOperatorCodeFromOperatorString(parameterTrimmed);
                    alert.setCautionOperator(intValue);
                }
            }

            parameter = Common.getObjectParameter(request, "CautionCombination");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = Alert.getCombinationCodeFromString(parameterTrimmed);
                    alert.setCautionCombination(intValue);                
                }
            }
            
            parameter = Common.getObjectParameter(request, "CautionCombinationCount");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {                    
                    Integer intValue = Integer.parseInt(parameterTrimmed);
                    alert.setCautionCombinationCount(intValue);
                }
            }

            parameter = Common.getObjectParameter(request, "CautionThreshold");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    BigDecimal bigDecimalValue = new BigDecimal(parameterTrimmed);
                    alert.setCautionThreshold(bigDecimalValue);
                }
            }

            parameter = Common.getObjectParameter(request, "DangerNotificationGroupName");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
                    NotificationGroup notificationGroup = notificationGroupsDao.getNotificationGroupByName(parameterTrimmed);
                    if ((notificationGroup != null) && (notificationGroup.getId() != null)) alert.setDangerNotificationGroupId(notificationGroup.getId());
                }
            }
            
            parameter = Common.getObjectParameter(request, "DangerWindowDuration");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    BigDecimal bigDecimalValueMs = new BigDecimal(parameterTrimmed);
                    BigDecimal bigDecimalValueInSeconds = bigDecimalValueMs.multiply(new BigDecimal(1000));
                    alert.setDangerWindowDuration(bigDecimalValueInSeconds.longValue());
                }
            }
            
            parameter = Common.getObjectParameter(request, "DangerWindowDurationTimeUnit");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = Alert.getTimeUnitCodeFromString(parameterTrimmed);
                    alert.setDangerWindowDurationTimeUnit(intValue);
                }
            }

            parameter = Common.getObjectParameter(request, "DangerStopTrackingAfter");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    BigDecimal bigDecimalValueMs = new BigDecimal(parameterTrimmed);
                    BigDecimal bigDecimalValueInSeconds = bigDecimalValueMs.multiply(new BigDecimal(1000));
                    alert.setDangerStopTrackingAfter(bigDecimalValueInSeconds.longValue());
                }
            }
            
            parameter = Common.getObjectParameter(request, "DangerStopTrackingAfterTimeUnit");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = Alert.getTimeUnitCodeFromString(parameterTrimmed);
                    alert.setDangerStopTrackingAfterTimeUnit(intValue);
                }
            }

            parameter = Common.getObjectParameter(request, "DangerMinimumSampleCount");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    Integer intValue = Integer.parseInt(parameterTrimmed);
                    alert.setDangerMinimumSampleCount(intValue);
                }
            }
            
            parameter = Common.getObjectParameter(request, "DangerOperator");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    Integer intValue = Alert.getOperatorCodeFromOperatorString(parameterTrimmed);
                    alert.setDangerOperator(intValue);
                }
            }

            parameter = Common.getObjectParameter(request, "DangerCombination");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    Integer intValue = Alert.getCombinationCodeFromString(parameterTrimmed);
                    alert.setDangerCombination(intValue);
                }
            }
            
            parameter = Common.getObjectParameter(request, "DangerCombinationCount");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    Integer intValue = Integer.parseInt(parameterTrimmed);
                    alert.setDangerCombinationCount(intValue);
                }
            }

            parameter = Common.getObjectParameter(request, "DangerThreshold");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                
                if (!parameterTrimmed.isEmpty()) {    
                    BigDecimal bigDecimalValue = new BigDecimal(parameterTrimmed);
                    alert.setDangerThreshold(bigDecimalValue);
                }
            }
        }
        catch (Exception e) {
            didEncounterError = true;
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
            
        if (!didEncounterError) {
            alert.setIsCautionAlertActive(false);
            alert.setCautionFirstActiveAt(null);
            alert.setIsCautionAcknowledged(null);
            alert.setCautionAlertLastSentTimestamp(null);
            alert.setCautionActiveAlertsSet(null);
            alert.setIsDangerAlertActive(false);
            alert.setDangerFirstActiveAt(null);
            alert.setIsDangerAcknowledged(null);
            alert.setDangerAlertLastSentTimestamp(null);
            alert.setDangerActiveAlertsSet(null);
        }
        else {
            alert = null;
        }
        
        return alert;
    }
    
}
