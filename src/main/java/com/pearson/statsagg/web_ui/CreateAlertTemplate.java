package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import com.pearson.statsagg.database_objects.DatabaseObjectValidation;
import com.pearson.statsagg.database_objects.alert_templates.AlertTemplate;
import com.pearson.statsagg.database_objects.alert_templates.AlertTemplatesDao;
import com.pearson.statsagg.database_objects.alert_templates.AlertTemplatesDaoWrapper;
import com.pearson.statsagg.globals.DatabaseConnections;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database_objects.variable_set_list.VariableSetList;
import com.pearson.statsagg.database_objects.variable_set_list.VariableSetListsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.math_utils.MathUtilities;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class CreateAlertTemplate extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CreateAlertTemplate.class.getName());
    
    public static final String PAGE_NAME = "Create Alert Template";
    
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
            StringBuilder htmlBuilder = new StringBuilder();

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            
            AlertTemplate alertTemplate = null;
            String name = request.getParameter("Name");
            if (name != null) alertTemplate = AlertTemplatesDao.getAlertTemplate(DatabaseConnections.getConnection(), true, name.trim());      

            String htmlBodyContents = buildCreateAlertTemplateHtml(alertTemplate);
            List<String> additionalJavascript = new ArrayList<>();
            additionalJavascript.add("js/statsagg_create_alert_template.js");
            String htmlBody = statsAggHtmlFramework.createHtmlBody(htmlBodyContents, additionalJavascript, false);
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
            String result = parseAndAlterAlertTemplate(request);

            StringBuilder htmlBuilder = new StringBuilder();
            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            String htmlBodyContent = statsAggHtmlFramework.buildHtmlBodyForPostResult(PAGE_NAME, StatsAggHtmlFramework.htmlEncode(result), "AlertTemplates", AlertTemplates.PAGE_NAME);
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
    
    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    protected static String buildCreateAlertTemplateHtml(AlertTemplate alertTemplate) {
        
        StringBuilder htmlBody = new StringBuilder();
        
        htmlBody.append(
            "<div id=\"page-content-wrapper\">\n" +
            "  <!-- Keep all page content within the page-content inset div! -->\n" +
            "  <div class=\"page-content inset statsagg_page_content_font\">\n" +
            "  <div class=\"content-header\"> \n" +
            "    <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "  </div>\n ");
        
        htmlBody.append("  <form action=\"CreateAlertTemplate\" method=\"POST\">\n");
        htmlBody.append(    "    <div class=\"row create-alert-form-row\">\n");

        if ((alertTemplate != null) && (alertTemplate.getName() != null) && !alertTemplate.getName().isEmpty()) {
            htmlBody.append("<input type=\"hidden\" name=\"Old_Name\" value=\"").append(StatsAggHtmlFramework.htmlEncode(alertTemplate.getName(), true)).append("\">");
        }
        
        
        // start column 1
        htmlBody.append(
            "<div class=\"col-md-4 statsagg_three_panel_first_panel\">\n" +
            "  <div class=\"panel panel-default\">\n" +
            "    <div class=\"panel-heading\"><b>Core Alert Criteria</b></div>\n" +
            "    <div class=\"panel-body\">");
            
        
        // name
        htmlBody.append(      
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Name</label>\n" +
            "  <button type=\"button\" id=\"Name_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"A unique name for this alert template.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <input class=\"form-control-statsagg\" name=\"Name\" id=\"Name\" ");
        
        if ((alertTemplate != null) && (alertTemplate.getName() != null)) {
            htmlBody.append(" value=\"").append(StatsAggHtmlFramework.htmlEncode(alertTemplate.getName(), true)).append("\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // variable set list name
        htmlBody.append(
            "<div class=\"form-group\" id=\"VariableSetListName_Lookup\">\n" +
            "  <label class=\"label_small_margin\">Variable Set List</label>\n" +
            "  <button type=\"button\" id=\"VariableSetListName_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"The exact name of the variable set list to associate with this alert template.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <input class=\"typeahead form-control-statsagg\" autocomplete=\"off\" name=\"VariableSetListName\" id=\"VariableSetListName\" ");

        if ((alertTemplate != null) && (alertTemplate.getVariableSetListId() != null)) {
            VariableSetList variableSetList = VariableSetListsDao.getVariableSetList(DatabaseConnections.getConnection(), true, alertTemplate.getVariableSetListId());

            if ((variableSetList != null) && (variableSetList.getName() != null)) {
                htmlBody.append(" value=\"").append(StatsAggHtmlFramework.htmlEncode(variableSetList.getName(), true)).append("\"");
            }
        }
        
        htmlBody.append(">\n</div>\n");
        
        
        // description
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Description</label>\n" +
            "  <button type=\"button\" id=\"DescriptionVariable_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"A templated description. Variable values are substituted using ```key```.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <textarea class=\"form-control-statsagg\" rows=\"3\" name=\"Description\" id=\"Description\">");

        if ((alertTemplate != null) && (alertTemplate.getDescriptionVariable() != null)) {
            htmlBody.append(StatsAggHtmlFramework.htmlEncode(alertTemplate.getDescriptionVariable(), true));
        }

        htmlBody.append("</textarea>\n");
        htmlBody.append("</div>\n");

        
        // alert name variable
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Alert name variable</label>\n" +
            "  <button type=\"button\" id=\"AlertNameVariable_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"A templated alert name. Variable values are substituted using ```key```.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <input class=\"form-control-statsagg\" name=\"AlertNameVariable\" id=\"AlertNameVariable\" ");

        if ((alertTemplate != null) && (alertTemplate.getAlertNameVariable() != null)) {
            htmlBody.append(" value=\"").append(StatsAggHtmlFramework.htmlEncode(alertTemplate.getAlertNameVariable(), true)).append("\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // metric group name variable
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Metric group name variable</label>\n" +
            "  <button type=\"button\" id=\"MetricGroupNameVariable_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"A templated metric group name. Variable values are substituted using ```key```.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <input class=\"form-control-statsagg\" name=\"MetricGroupNameVariable\" id=\"MetricGroupNameVariable\" ");

        if ((alertTemplate != null) && (alertTemplate.getMetricGroupNameVariable() != null)) {
            htmlBody.append(" value=\"").append(StatsAggHtmlFramework.htmlEncode(alertTemplate.getMetricGroupNameVariable(), true)).append("\"");
        }

        htmlBody.append(">\n</div>\n");


        // alert type
        htmlBody.append("<div class=\"form-group\">\n");
                
        htmlBody.append("<label class=\"label_small_margin\">Alert type:&nbsp;&nbsp;</label>\n");
        
        htmlBody.append("<input type=\"radio\" id=\"Type_Availability\" name=\"Type\" value=\"Availability\" ");
        if ((alertTemplate != null) && (alertTemplate.getAlertType() != null) && (alertTemplate.getAlertType() == AlertTemplate.TYPE_AVAILABILITY)) htmlBody.append(" checked=\"checked\"");
        htmlBody.append("> Availability &nbsp;&nbsp;&nbsp;\n");
        
        htmlBody.append("<input type=\"radio\" id=\"Type_Threshold\" name=\"Type\" value=\"Threshold\" ");
        if ((alertTemplate != null) && (alertTemplate.getAlertType() != null) && (alertTemplate.getAlertType() == AlertTemplate.TYPE_THRESHOLD)) htmlBody.append(" checked=\"checked\"");
        htmlBody.append("> Threshold\n");

        htmlBody.append("</div>");
        
        
        // is enabled?
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Is alert enabled?&nbsp;&nbsp;</label>\n" +
            "  <input name=\"Enabled\" id=\"Enabled\" type=\"checkbox\" ");

        if (((alertTemplate != null) && (alertTemplate.isEnabled() != null) && alertTemplate.isEnabled()) || 
                (alertTemplate == null) || (alertTemplate.isEnabled() == null)) {
            htmlBody.append(" checked=\"checked\"");
        }

        htmlBody.append(">\n</div>\n");

        
        // is caution alerting enabled?
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Is caution alerting enabled?&nbsp;&nbsp;</label>\n" +
            "  <input name=\"CautionEnabled\" id=\"CautionEnabled\" type=\"checkbox\" ");

        if (((alertTemplate != null) && (alertTemplate.isCautionEnabled() != null) && alertTemplate.isCautionEnabled()) || 
                (alertTemplate == null) || (alertTemplate.isCautionEnabled() == null)) {
            htmlBody.append(" checked=\"checked\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // is danger alerting enabled?
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Is danger alerting enabled?&nbsp;&nbsp;</label>\n" +
            "  <input name=\"DangerEnabled\" id=\"DangerEnabled\" type=\"checkbox\" ");

        if (((alertTemplate != null) && (alertTemplate.isDangerEnabled() != null) && alertTemplate.isDangerEnabled()) || 
                (alertTemplate == null) || (alertTemplate.isDangerEnabled() == null)) {
            htmlBody.append(" checked=\"checked\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // alert on positive?
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Alert on positive?&nbsp;&nbsp;</label>\n" +
            "  <input name=\"AlertOnPositive\" id=\"AlertOnPositive\" type=\"checkbox\" ");
        
        if (((alertTemplate != null) && (alertTemplate.isAlertOnPositive() != null) && alertTemplate.isAlertOnPositive()) || 
                (alertTemplate == null) || (alertTemplate.isAlertOnPositive() == null)) {
            htmlBody.append(" checked=\"checked\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // allow resend alert?
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Resend alert?&nbsp;&nbsp;</label>\n" +
            "  <input name=\"AllowResendAlert\" id=\"AllowResendAlert\" type=\"checkbox\" ");
        
        if ((alertTemplate != null) && (alertTemplate.isAllowResendAlert()!= null) && alertTemplate.isAllowResendAlert()) {
            htmlBody.append(" checked=\"checked\"");
        }

        htmlBody.append(">\n</div>\n");
        

        // resend alert every...
        htmlBody.append("<label id=\"ResendAlertEvery_Label\" class=\"label_small_margin\">Resend alert every...</label>\n");
        htmlBody.append("<button type=\"button\" id=\"ResendAlertEvery_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"Specifies how long to wait before resending a notification for a triggered alert.\" style=\"margin-bottom: 1.5px;\">?</button> ");
        htmlBody.append("<div>\n");
        
        htmlBody.append(
            "<div class=\"form-group col-xs-6\">\n" +
            "  <input class=\"form-control-statsagg\" placeholder=\"If 'resend alert' is enabled, how often should the alert be resent?\" name=\"ResendAlertEvery\" id=\"ResendAlertEvery\" ");

        if ((alertTemplate != null) && (alertTemplate.getResendAlertEvery() != null)) {
            BigDecimal sendAlertEvery = DatabaseObjectCommon.getValueForTimeFromMilliseconds(alertTemplate.getResendAlertEvery(), alertTemplate.getResendAlertEveryTimeUnit());
            htmlBody.append(" value=\"").append(sendAlertEvery.stripTrailingZeros().toPlainString()).append("\"");
        }
        
        htmlBody.append(">\n</div>\n");
        
        
        // resend alert every... time unit
        htmlBody.append(
            "<div class=\"form-group col-xs-6\">\n" +
            "  <select class=\"form-control-statsagg\" name=\"ResendAlertEveryTimeUnit\" id=\"ResendAlertEveryTimeUnit\">\n");

        if ((alertTemplate != null) && (DatabaseObjectCommon.getTimeUnitStringFromCode(alertTemplate.getResendAlertEveryTimeUnit(), true) != null)) {
            String timeUnitString = DatabaseObjectCommon.getTimeUnitStringFromCode(alertTemplate.getResendAlertEveryTimeUnit(), true);
            
            if (timeUnitString.equalsIgnoreCase("Seconds")) htmlBody.append("<option selected=\"selected\">Seconds</option>\n");
            else htmlBody.append("<option>Seconds</option>\n");

            if (timeUnitString.equalsIgnoreCase("Minutes")) htmlBody.append("<option selected=\"selected\">Minutes</option>\n");
            else htmlBody.append("<option>Minutes</option>\n");

            if (timeUnitString.equalsIgnoreCase("Hours")) htmlBody.append("<option selected=\"selected\">Hours</option>\n");
            else htmlBody.append("<option>Hours</option>\n");

            if (timeUnitString.equalsIgnoreCase("Days")) htmlBody.append("<option selected=\"selected\">Days</option>\n");
            else htmlBody.append("<option>Days</option>\n");
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
        
        // end column 1
        htmlBody.append(
            "    </div>\n" +
            "  </div>\n" +        
            "</div>\n");
        
        
        // start column 2
        htmlBody.append(
            "<div class=\"col-md-4 statsagg_three_panel_second_panel\" id=\"CautionCriteria\" >\n" +
            "  <div class=\"panel panel-warning\">\n" +
            "    <div class=\"panel-heading\"><b>Caution Criteria</b> " +
            "    <a id=\"CautionPreview\" name=\"CautionPreview\" class=\"iframe cboxElement statsagg_caution_preview pull-right\" href=\"#\" onclick=\"generateAlertPreviewLink('Caution');\">Preview</a>" + 
            "    </div>" +
            "    <div class=\"panel-body\">");
        
        
        // warning for when no alert-type is selected
        htmlBody.append("<label id=\"CautionNoAlertTypeSelected_Label\" class=\"label_small_margin\">Please select an alert type</label>\n");

        
        // caution notification group name
        htmlBody.append(
            "<div id=\"CautionNotificationGroupNameVariable_Div\">\n" +
            "  <div class=\"form-group\">\n" +
            "    <label class=\"label_small_margin\">Notification group name variable</label>\n" +
            "    <button type=\"button\" id=\"CautionNotificationGroupNameVariable_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"A templated notification group name. Variable values are substituted using ```key```.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "    <input class=\"form-control-statsagg\" name=\"CautionNotificationGroupNameVariable\" id=\"CautionNotificationGroupNameVariable\" ");

        if ((alertTemplate != null) && (alertTemplate.getCautionNotificationGroupNameVariable() != null)) {
            htmlBody.append(" value=\"").append(StatsAggHtmlFramework.htmlEncode(alertTemplate.getCautionNotificationGroupNameVariable(), true)).append("\"");
        }
        
        htmlBody.append("></div></div>\n");
        
        
        // caution positive notification group name
        htmlBody.append(
            "<div id=\"CautionPositiveNotificationGroupNameVariable_Div\">\n" +
            "  <div class=\"form-group\">\n" +
            "    <label class=\"label_small_margin\">Positive notification group name variable</label>\n" +
            "    <button type=\"button\" id=\"CautionPositiveNotificationGroupNameVariable_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"A templated notification group name. Variable values are substituted using ```key```.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "    <input class=\"form-control-statsagg\" name=\"CautionPositiveNotificationGroupNameVariable\" id=\"CautionPositiveNotificationGroupNameVariable\" ");

        if ((alertTemplate != null) && (alertTemplate.getCautionPositiveNotificationGroupNameVariable() != null)) {
            htmlBody.append(" value=\"").append(StatsAggHtmlFramework.htmlEncode(alertTemplate.getCautionPositiveNotificationGroupNameVariable(), true)).append("\"");
        }
        
        htmlBody.append(">\n</div></div>\n");
        
        
        // caution window duration
        htmlBody.append("<div id=\"CautionWindowDuration_Div\">\n");
        htmlBody.append("  <label id=\"CautionWindowDuration_Label\" class=\"label_small_margin\">Window duration</label>\n");
        htmlBody.append("  <button type=\"button\" id=\"CautionWindowDuration_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"A rolling time window between 'now' and 'X' time units ago. Values that fall in this window are used in alert evaluation.\" style=\"margin-bottom: 1.5px;\">?</button> ");
        htmlBody.append("  <div>\n");
        
        htmlBody.append(
            "<div class=\"col-xs-6\" style=\"margin-bottom: 11px;\">\n" +
            "  <input class=\"form-control-statsagg\" name=\"CautionWindowDuration\" id=\"CautionWindowDuration\" ");

        if ((alertTemplate != null) && (alertTemplate.getCautionWindowDuration() != null)) {
            BigDecimal cautionWindowDuration = DatabaseObjectCommon.getValueForTimeFromMilliseconds(alertTemplate.getCautionWindowDuration(), alertTemplate.getCautionWindowDurationTimeUnit());
            htmlBody.append(" value=\"").append(cautionWindowDuration.stripTrailingZeros().toPlainString()).append("\"");
        }
        
        htmlBody.append(">\n</div>\n");
        
        
        // caution window duration time unit
        htmlBody.append(
            "<div class=\"col-xs-6\" style=\"margin-bottom: 11px;\">\n" +
            "  <select class=\"form-control-statsagg\" name=\"CautionWindowDurationTimeUnit\" id=\"CautionWindowDurationTimeUnit\">\n");
        
        if ((alertTemplate != null) && (DatabaseObjectCommon.getTimeUnitStringFromCode(alertTemplate.getCautionWindowDurationTimeUnit(), true) != null)) {
            String timeUnitString = DatabaseObjectCommon.getTimeUnitStringFromCode(alertTemplate.getCautionWindowDurationTimeUnit(), true);
            
            if (timeUnitString.equalsIgnoreCase("Seconds")) htmlBody.append("<option selected=\"selected\">Seconds</option>\n");
            else htmlBody.append("<option>Seconds</option>\n");

            if (timeUnitString.equalsIgnoreCase("Minutes")) htmlBody.append("<option selected=\"selected\">Minutes</option>\n");
            else htmlBody.append("<option>Minutes</option>\n");

            if (timeUnitString.equalsIgnoreCase("Hours")) htmlBody.append("<option selected=\"selected\">Hours</option>\n");
            else htmlBody.append("<option>Hours</option>\n");

            if (timeUnitString.equalsIgnoreCase("Days")) htmlBody.append("<option selected=\"selected\">Days</option>\n");
            else htmlBody.append("<option>Days</option>\n");
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
        htmlBody.append("</div></div></div>\n");
        
        
        // caution stop tracking after
        htmlBody.append("<div id=\"CautionStopTrackingAfter_Div\" >\n");
        htmlBody.append("  <label id=\"CautionStopTrackingAfter_Label\" class=\"label_small_margin\">Stop tracking after...</label>\n");
        htmlBody.append("  <button type=\"button\" id=\"CautionStopTrackingAfter_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"After a metric has not been seen for X time-units, stop alerting on it.\" style=\"margin-bottom: 1.5px;\">?</button> ");
        htmlBody.append("  <div>\n");
        
        htmlBody.append(   
            "<div class=\"col-xs-6\">\n" +
            "  <input class=\"form-control-statsagg\" name=\"CautionStopTrackingAfter\" id=\"CautionStopTrackingAfter\"");

        if ((alertTemplate != null) && (alertTemplate.getCautionStopTrackingAfter() != null)) {
            BigDecimal cautionStopTrackingAfter = DatabaseObjectCommon.getValueForTimeFromMilliseconds(alertTemplate.getCautionStopTrackingAfter(), alertTemplate.getCautionStopTrackingAfterTimeUnit());
            htmlBody.append(" value=\"").append(cautionStopTrackingAfter.stripTrailingZeros().toPlainString()).append("\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // caution 'stop tracking after' time unit
        htmlBody.append(
            "<div class=\"col-xs-6\">\n" +
            "  <select class=\"form-control-statsagg\" name=\"CautionStopTrackingAfterTimeUnit\" id=\"CautionStopTrackingAfterTimeUnit\">\n");

        if ((alertTemplate != null) && (DatabaseObjectCommon.getTimeUnitStringFromCode(alertTemplate.getCautionStopTrackingAfterTimeUnit(), true) != null)) {
            String timeUnitString = DatabaseObjectCommon.getTimeUnitStringFromCode(alertTemplate.getCautionStopTrackingAfterTimeUnit(), true);

            if (timeUnitString.equalsIgnoreCase("Seconds")) htmlBody.append("<option selected=\"selected\">Seconds</option>\n");
            else htmlBody.append("<option>Seconds</option>\n");

            if (timeUnitString.equalsIgnoreCase("Minutes")) htmlBody.append("<option selected=\"selected\">Minutes</option>\n");
            else htmlBody.append("<option>Minutes</option>\n");

            if (timeUnitString.equalsIgnoreCase("Hours")) htmlBody.append("<option selected=\"selected\">Hours</option>\n");
            else htmlBody.append("<option>Hours</option>\n");

            if (timeUnitString.equalsIgnoreCase("Days")) htmlBody.append("<option selected=\"selected\">Days</option>\n");
            else htmlBody.append("<option>Days</option>\n");
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
        htmlBody.append("</div></div></div>\n");
        
        
        // caution minimum sample count
        htmlBody.append(
            "<div class=\"form-group statsagg_typeahead_form_margin_correction\" id=\"CautionMinimumSampleCount_Div\" >\n" +
            "  <label id=\"CautionMinimumSampleCount_Label\" class=\"label_small_margin\">Minimum sample count</label>\n" +
            "  <button type=\"button\" id=\"CautionMinimumSampleCount_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"An alert can only be triggered if there are at least 'X' samples within specified the 'alert window duration'.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <input class=\"form-control-statsagg\" name=\"CautionMinimumSampleCount\" id=\"CautionMinimumSampleCount\"");

        if ((alertTemplate != null) && (alertTemplate.getCautionMinimumSampleCount() != null)) {
            htmlBody.append(" value=\"").append(alertTemplate.getCautionMinimumSampleCount()).append("\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // caution operator
        htmlBody.append(
            "<div class=\"form-group statsagg_typeahead_form_margin_correction\" id=\"CautionOperator_Div\" >\n" +
            "  <label id=\"CautionOperator_Label\" class=\"label_small_margin\">Operator</label>\n" +
            "  <button type=\"button\" id=\"CautionOperator_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"The values of a metric-key are considered for threshold-based alerting when they are above/below/equal-to a certain threshold. This value controls the above/below/equal-to aspect of the alert.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <select class=\"form-control-statsagg\" name=\"CautionOperator\" id=\"CautionOperator\">\n");
        
        String cautionOperatorString = (alertTemplate == null) ? null : alertTemplate.getOperatorString(AlertTemplate.CAUTION, true, false);
        
        htmlBody.append("<option");
        if ((cautionOperatorString != null) && cautionOperatorString.equalsIgnoreCase(">")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append(">&nbsp;&nbsp;(greater than)</option>\n");
        
        htmlBody.append("<option");
        if ((cautionOperatorString != null) && cautionOperatorString.equalsIgnoreCase(">=")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append(">=&nbsp;&nbsp;(greater than or equal to)</option>\n");
        
        htmlBody.append("<option");
        if ((cautionOperatorString != null) && cautionOperatorString.equalsIgnoreCase("<")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append("<&nbsp;&nbsp;(less than)</option>\n");
        
        htmlBody.append("<option");
        if ((cautionOperatorString != null) && cautionOperatorString.equalsIgnoreCase("<=")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append("<=&nbsp;&nbsp;(less than or equal to)</option>\n");

        htmlBody.append("<option");
        if ((cautionOperatorString != null) && cautionOperatorString.equalsIgnoreCase("=")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append("=&nbsp;&nbsp;(equal to)</option>\n");

        htmlBody.append("</select>\n");
        htmlBody.append("</div>\n");     
        
        
        // caution combination
        htmlBody.append(
            "<div class=\"form-group statsagg_typeahead_form_margin_correction\" id=\"CautionCombination_Div\" >\n" +
            "  <label id=\"CautionCombination_Label\" class=\"label_small_margin\">Combination</label>\n" +
            "  <button type=\"button\" id=\"CautionCombination_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"For any metric values that fall within the 'alert window duration', what condition will cause the alert to be triggered? Is the average of the metric values above or below the threshold? Are all metrics values above or below the threshold? Is any metric value above or below the threshold? Are 'at least' or 'at most' X metric values above or below the threshold?\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <select class=\"form-control-statsagg\" name=\"CautionCombination\" id=\"CautionCombination\">\n");

        if ((alertTemplate != null) && (alertTemplate.getCombinationString(AlertTemplate.CAUTION) != null)) {
            if (alertTemplate.getCombinationString(AlertTemplate.CAUTION).equalsIgnoreCase("Any")) htmlBody.append("<option selected=\"selected\">Any</option>\n");
            else htmlBody.append("<option>Any</option>\n");

            if (alertTemplate.getCombinationString(AlertTemplate.CAUTION).equalsIgnoreCase("All")) htmlBody.append("<option selected=\"selected\">All</option>\n");
            else htmlBody.append("<option>All</option>\n");

            if (alertTemplate.getCombinationString(AlertTemplate.CAUTION).equalsIgnoreCase("Average")) htmlBody.append("<option selected=\"selected\">Average</option>\n");
            else htmlBody.append("<option>Average</option>\n");

            if (alertTemplate.getCombinationString(AlertTemplate.CAUTION).equalsIgnoreCase("At most")) htmlBody.append("<option selected=\"selected\">At most</option>\n");
            else htmlBody.append("<option>At most</option>\n");

            if (alertTemplate.getCombinationString(AlertTemplate.CAUTION).equalsIgnoreCase("At least")) htmlBody.append("<option selected=\"selected\">At least</option>\n");
            else htmlBody.append("<option>At least</option>\n");
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
            "<div class=\"form-group statsagg_typeahead_form_margin_correction\" id=\"CautionCombinationCount_Div\" >\n" +
            "  <label id=\"CautionCombinationCount_Label\" class=\"label_small_margin\">Combination count</label>\n" +
            "  <button type=\"button\" id=\"CautionCombination_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"If using a combination of 'at most' or 'at least', then you must specify a count. This refers to the number of independent metric values for a single metric-key that fall within the 'alert window duration'.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <input class=\"form-control-statsagg\" name=\"CautionCombinationCount\" id=\"CautionCombinationCount\" ");

        if ((alertTemplate != null) && (alertTemplate.getCautionCombinationCount() != null)) {
            htmlBody.append(" value=\"").append(alertTemplate.getCautionCombinationCount()).append("\"");
        }
        
        htmlBody.append(">\n</div>\n");

        
        // caution threshold
        htmlBody.append(
            "<div class=\"form-group statsagg_typeahead_form_margin_correction\" id=\"CautionThreshold_Div\" >\n" +
            "  <label id=\"CautionThreshold_Label\" class=\"label_small_margin\">Threshold</label>\n" +
            "  <button type=\"button\" id=\"CautionThreshold_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"The numeric threshold that, if crossed, will trigger the alert.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <input class=\"form-control-statsagg\" name=\"CautionThreshold\" id=\"CautionThreshold\" ");

        if ((alertTemplate != null) && (alertTemplate.getCautionThreshold() != null)) {
            htmlBody.append(" value=\"").append(alertTemplate.getCautionThreshold().stripTrailingZeros().toPlainString()).append("\"");
        }
                
        htmlBody.append(">\n</div>\n");
        
        // end column 2
        htmlBody.append("</div>\n</div>\n</div>\n");

               
        // start column 3
        htmlBody.append(
            "<div class=\"col-md-4 statsagg_three_panel_second_panel\" id=\"DangerCriteria\" >\n" +
            "  <div class=\"panel panel-danger\">\n" +
            "    <div class=\"panel-heading\"><b>Danger Criteria</b> " +
            "    <a id=\"DangerPreview\" name=\"DangerPreview\" class=\"iframe cboxElement statsagg_danger_preview pull-right\" href=\"#\" onclick=\"generateAlertPreviewLink('Danger');\">Preview</a>" + 
            "    </div>" +
            "    <div class=\"panel-body\">");
        
        
        // warning for when no alert-type is selected
        htmlBody.append("<label id=\"DangerNoAlertTypeSelected_Label\" class=\"label_small_margin\">Please select an alert type</label>\n");

        
        // danger notification group name
        htmlBody.append(
            "<div id=\"DangerNotificationGroupNameVariable_Div\">\n" +
            "  <div class=\"form-group\">\n" +
            "    <label class=\"label_small_margin\">Notification group name variable</label>\n" +
            "    <button type=\"button\" id=\"DangerNotificationGroupNameVariable_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"The exact name of the notification group to send alerts to.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "    <input class=\"form-control-statsagg\" name=\"DangerNotificationGroupNameVariable\" id=\"DangerNotificationGroupNameVariable\" ");

        if ((alertTemplate != null) && (alertTemplate.getDangerNotificationGroupNameVariable() != null)) {
            htmlBody.append(" value=\"").append(StatsAggHtmlFramework.htmlEncode(alertTemplate.getDangerNotificationGroupNameVariable(), true)).append("\"");
        }
        
        htmlBody.append("></div></div>\n");
        
        
        // danger positive notification group name
        htmlBody.append(
            "<div id=\"DangerPositiveNotificationGroupNameVariable_Div\">\n" +
            "  <div class=\"form-group\">\n" +
            "    <label class=\"label_small_margin\">Positive notification group name variable</label>\n" +
            "    <button type=\"button\" id=\"DangerPositiveNotificationGroupNameVariable_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"The exact name of the notification group to send positive alerts to.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "    <input class=\"form-control-statsagg\" name=\"DangerPositiveNotificationGroupNameVariable\" id=\"DangerPositiveNotificationGroupNameVariable\" ");

        if ((alertTemplate != null) && (alertTemplate.getDangerPositiveNotificationGroupNameVariable() != null)) {
            htmlBody.append(" value=\"").append(StatsAggHtmlFramework.htmlEncode(alertTemplate.getDangerPositiveNotificationGroupNameVariable(), true)).append("\"");
        }
        
        htmlBody.append(">\n</div></div>\n");
        
        
        // danger window duration
        htmlBody.append("<div id=\"DangerWindowDuration_Div\">\n");
        htmlBody.append("  <label id=\"DangerWindowDuration_Label\" class=\"label_small_margin\">Window duration</label>\n");
        htmlBody.append("  <button type=\"button\" id=\"DangerWindowDuration_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"A rolling time window between 'now' and 'X' time units ago. Values that fall in this window are used in alert evaluation.\" style=\"margin-bottom: 1.5px;\">?</button> ");
        htmlBody.append("  <div>\n");
        
        htmlBody.append(
            "<div class=\"col-xs-6\" style=\"margin-bottom: 11px;\">\n" +
            "  <input class=\"form-control-statsagg\" name=\"DangerWindowDuration\" id=\"DangerWindowDuration\" ");

        if ((alertTemplate != null) && (alertTemplate.getDangerWindowDuration() != null)) {
            BigDecimal dangerWindowDuration = DatabaseObjectCommon.getValueForTimeFromMilliseconds(alertTemplate.getDangerWindowDuration(), alertTemplate.getDangerWindowDurationTimeUnit());
            htmlBody.append(" value=\"").append(dangerWindowDuration.stripTrailingZeros().toPlainString()).append("\"");
        }
        
        htmlBody.append(">\n</div>\n");
        
        
        // danger window duration time unit
        htmlBody.append(
            "<div class=\"col-xs-6\" style=\"margin-bottom: 11px;\">\n" +
            "  <select class=\"form-control-statsagg\" name=\"DangerWindowDurationTimeUnit\" id=\"DangerWindowDurationTimeUnit\">\n");
        
        if ((alertTemplate != null) && (DatabaseObjectCommon.getTimeUnitStringFromCode(alertTemplate.getDangerWindowDurationTimeUnit(), true) != null)) {
            String timeUnitString = DatabaseObjectCommon.getTimeUnitStringFromCode(alertTemplate.getDangerWindowDurationTimeUnit(), true);
            
            if (timeUnitString.equalsIgnoreCase("Seconds")) htmlBody.append("<option selected=\"selected\">Seconds</option>\n");
            else htmlBody.append("<option>Seconds</option>\n");

            if (timeUnitString.equalsIgnoreCase("Minutes")) htmlBody.append("<option selected=\"selected\">Minutes</option>\n");
            else htmlBody.append("<option>Minutes</option>\n");

            if (timeUnitString.equalsIgnoreCase("Hours")) htmlBody.append("<option selected=\"selected\">Hours</option>\n");
            else htmlBody.append("<option>Hours</option>\n");

            if (timeUnitString.equalsIgnoreCase("Days")) htmlBody.append("<option selected=\"selected\">Days</option>\n");
            else htmlBody.append("<option>Days</option>\n");
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
        htmlBody.append("</div></div></div>\n");
        
        
        // danger stop tracking after
        htmlBody.append("<div id=\"DangerStopTrackingAfter_Div\" >\n");
        htmlBody.append("  <label id=\"DangerStopTrackingAfter_Label\" class=\"label_small_margin\">Stop tracking after...</label>\n");
        htmlBody.append("  <button type=\"button\" id=\"DangerStopTrackingAfter_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"After a metric has not been seen for X time-units, stop alerting on it.\" style=\"margin-bottom: 1.5px;\">?</button> ");
        htmlBody.append("  <div>\n");
        
        htmlBody.append(   
            "<div class=\"col-xs-6\">\n" +
            "  <input class=\"form-control-statsagg\" name=\"DangerStopTrackingAfter\" id=\"DangerStopTrackingAfter\"");

        if ((alertTemplate != null) && (alertTemplate.getDangerStopTrackingAfter() != null)) {
            BigDecimal dangerStopTrackingAfter = DatabaseObjectCommon.getValueForTimeFromMilliseconds(alertTemplate.getDangerStopTrackingAfter(), alertTemplate.getDangerStopTrackingAfterTimeUnit());
            htmlBody.append(" value=\"").append(dangerStopTrackingAfter.stripTrailingZeros().toPlainString()).append("\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // danger 'stop tracking after' time unit
        htmlBody.append(
            "<div class=\"col-xs-6\">\n" +
            "  <select class=\"form-control-statsagg\" name=\"DangerStopTrackingAfterTimeUnit\" id=\"DangerStopTrackingAfterTimeUnit\">\n");

        if ((alertTemplate != null) && (DatabaseObjectCommon.getTimeUnitStringFromCode(alertTemplate.getDangerStopTrackingAfterTimeUnit(), true) != null)) {
            String timeUnitString = DatabaseObjectCommon.getTimeUnitStringFromCode(alertTemplate.getDangerStopTrackingAfterTimeUnit(), true);

            if (timeUnitString.equalsIgnoreCase("Seconds")) htmlBody.append("<option selected=\"selected\">Seconds</option>\n");
            else htmlBody.append("<option>Seconds</option>\n");

            if (timeUnitString.equalsIgnoreCase("Minutes")) htmlBody.append("<option selected=\"selected\">Minutes</option>\n");
            else htmlBody.append("<option>Minutes</option>\n");

            if (timeUnitString.equalsIgnoreCase("Hours")) htmlBody.append("<option selected=\"selected\">Hours</option>\n");
            else htmlBody.append("<option>Hours</option>\n");

            if (timeUnitString.equalsIgnoreCase("Days")) htmlBody.append("<option selected=\"selected\">Days</option>\n");
            else htmlBody.append("<option>Days</option>\n");
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
        htmlBody.append("</div></div></div>\n");
        
        
        // danger minimum sample count
        htmlBody.append(
            "<div class=\"form-group statsagg_typeahead_form_margin_correction\" id=\"DangerMinimumSampleCount_Div\" >\n" +
            "  <label id=\"DangerMinimumSampleCount_Label\" class=\"label_small_margin\">Minimum sample count</label>\n" +
            "  <button type=\"button\" id=\"DangerMinimumSampleCount_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"An alert can only be triggered if there are at least 'X' samples within specified the 'alert window duration'.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <input class=\"form-control-statsagg\" name=\"DangerMinimumSampleCount\" id=\"DangerMinimumSampleCount\"");

        if ((alertTemplate != null) && (alertTemplate.getDangerMinimumSampleCount() != null)) {
            htmlBody.append(" value=\"").append(alertTemplate.getDangerMinimumSampleCount()).append("\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // danger operator
        htmlBody.append(
            "<div class=\"form-group statsagg_typeahead_form_margin_correction\" id=\"DangerOperator_Div\" >\n" +
            "  <label id=\"DangerOperator_Label\" class=\"label_small_margin\">Operator</label>\n" +
            "  <button type=\"button\" id=\"DangerOperator_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"The values of a metric-key are considered for threshold-based alerting when they are above/below/equal-to a certain threshold. This value controls the above/below/equal-to aspect of the alert.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <select class=\"form-control-statsagg\" name=\"DangerOperator\" id=\"DangerOperator\">\n");
        
        String dangerOperatorString = (alertTemplate == null) ? null : alertTemplate.getOperatorString(AlertTemplate.DANGER, true, false);

        htmlBody.append("<option");
        if ((dangerOperatorString != null) && dangerOperatorString.equalsIgnoreCase(">")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append(">&nbsp;&nbsp;(greater than)</option>\n");
        
        htmlBody.append("<option");
        if ((dangerOperatorString != null) && dangerOperatorString.equalsIgnoreCase(">=")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append(">=&nbsp;&nbsp;(greater than or equal to)</option>\n");
        
        htmlBody.append("<option");
        if ((dangerOperatorString != null) && dangerOperatorString.equalsIgnoreCase("<")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append("<&nbsp;&nbsp;(less than)</option>\n");
        
        htmlBody.append("<option");
        if ((dangerOperatorString != null) && dangerOperatorString.equalsIgnoreCase("<=")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append("<=&nbsp;&nbsp;(less than or equal to)</option>\n");

        htmlBody.append("<option");
        if ((dangerOperatorString != null) && dangerOperatorString.equalsIgnoreCase("=")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append("=&nbsp;&nbsp;(equal to)</option>\n");

        htmlBody.append("</select>\n");
        htmlBody.append("</div>\n");     
        
        
        // danger combination
        htmlBody.append(
            "<div class=\"form-group statsagg_typeahead_form_margin_correction\" id=\"DangerCombination_Div\" >\n" +
            "  <label id=\"DangerCombination_Label\" class=\"label_small_margin\">Combination</label>\n" +
            "  <button type=\"button\" id=\"DangerCombination_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"For any metric values that fall within the 'alert window duration', what condition will cause the alert to be triggered? Is the average of the metric values above or below the threshold? Are all metrics values above or below the threshold? Is any metric value above or below the threshold? Are 'at least' or 'at most' X metric values above or below the threshold?\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <select class=\"form-control-statsagg\" name=\"DangerCombination\" id=\"DangerCombination\">\n");

        if ((alertTemplate != null) && (alertTemplate.getCombinationString(AlertTemplate.DANGER) != null)) {
            if (alertTemplate.getCombinationString(AlertTemplate.DANGER).equalsIgnoreCase("Any")) htmlBody.append("<option selected=\"selected\">Any</option>\n");
            else htmlBody.append("<option>Any</option>\n");

            if (alertTemplate.getCombinationString(AlertTemplate.DANGER).equalsIgnoreCase("All")) htmlBody.append("<option selected=\"selected\">All</option>\n");
            else htmlBody.append("<option>All</option>\n");

            if (alertTemplate.getCombinationString(AlertTemplate.DANGER).equalsIgnoreCase("Average")) htmlBody.append("<option selected=\"selected\">Average</option>\n");
            else htmlBody.append("<option>Average</option>\n");

            if (alertTemplate.getCombinationString(AlertTemplate.DANGER).equalsIgnoreCase("At most")) htmlBody.append("<option selected=\"selected\">At most</option>\n");
            else htmlBody.append("<option>At most</option>\n");

            if (alertTemplate.getCombinationString(AlertTemplate.DANGER).equalsIgnoreCase("At least")) htmlBody.append("<option selected=\"selected\">At least</option>\n");
            else htmlBody.append("<option>At least</option>\n");
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
            "<div class=\"form-group statsagg_typeahead_form_margin_correction\" id=\"DangerCombinationCount_Div\" >\n" +
            "  <label id=\"DangerCombinationCount_Label\" class=\"label_small_margin\">Combination count</label>\n" +
            "  <button type=\"button\" id=\"DangerCombination_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"If using a combination of 'at most' or 'at least', then you must specify a count. This refers to the number of independent metric values for a single metric-key that fall within the 'alert window duration'.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <input class=\"form-control-statsagg\" name=\"DangerCombinationCount\" id=\"DangerCombinationCount\" ");

        if ((alertTemplate != null) && (alertTemplate.getDangerCombinationCount() != null)) {
            htmlBody.append(" value=\"").append(alertTemplate.getDangerCombinationCount()).append("\"");
        }
        
        htmlBody.append(">\n</div>\n");

        
        // danger threshold
        htmlBody.append(
            "<div class=\"form-group statsagg_typeahead_form_margin_correction\" id=\"DangerThreshold_Div\" >\n" +
            "  <label id=\"DangerThreshold_Label\" class=\"label_small_margin\">Threshold</label>\n" +
            "  <button type=\"button\" id=\"DangerThreshold_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"The numeric threshold that, if crossed, will trigger the alert.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <input class=\"form-control-statsagg\" name=\"DangerThreshold\" id=\"DangerThreshold\" ");

        if ((alertTemplate != null) && (alertTemplate.getDangerThreshold() != null)) {
            htmlBody.append(" value=\"").append(alertTemplate.getDangerThreshold().stripTrailingZeros().toPlainString()).append("\"");
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
            "<a href=\"AlertTemplates\" class=\"btn btn-default statsagg_page_content_font\" role=\"button\">Cancel</a>" +
            "</form>\n" +
            "</div>\n" +
            "</div>\n"
            );

        return htmlBody.toString();
    }

    public static String parseAndAlterAlertTemplate(Object request) {
        
        if (request == null) {
            return null;
        }
        
        String returnString;
        
        AlertTemplate alertTemplate = getAlertTemplateFromAlertTemplateParameters(request);
        
        String oldName = Common.getSingleParameterAsString(request, "Old_Name");
        if (oldName == null) oldName = Common.getSingleParameterAsString(request, "old_name");
        if (oldName == null) {
            String id = Common.getSingleParameterAsString(request, "Id");
            if (id == null) id = Common.getSingleParameterAsString(request, "id");
            
            if (id != null) {
                try {
                    Integer id_Integer = Integer.parseInt(id.trim());
                    AlertTemplate oldAlertTemplate = AlertTemplatesDao.getAlertTemplate(DatabaseConnections.getConnection(), true, id_Integer);
                    oldName = oldAlertTemplate.getName();
                }
                catch (Exception e){}
            }
        }
        
        if (alertTemplate == null) {
            returnString = "Failed to create or alter alert template. Reason=\"One or more invalid alert template fields detected\".";
            logger.warn(returnString);
        } 
        else {
            DatabaseObjectValidation databaseObjectValidation = AlertTemplate.isValid(alertTemplate);

            if (!databaseObjectValidation.isValid()) {
                returnString = "Failed to create or alter alert template. Reason=\"" + databaseObjectValidation.getReason() + "\".";
                logger.warn(returnString);
            }
            else {
                AlertTemplatesDaoWrapper alertsTemplatesDaoWrapper = AlertTemplatesDaoWrapper.alterRecordInDatabase(alertTemplate, oldName);
                returnString = alertsTemplatesDaoWrapper.getReturnString();

                if ((GlobalVariables.templateInvokerThread != null) && (AlertTemplatesDaoWrapper.STATUS_CODE_SUCCESS == alertsTemplatesDaoWrapper.getLastAlterRecordStatus())) {
                    logger.info("Running alert template routine due to alert template create or alter operation");
                    GlobalVariables.templateInvokerThread.runTemplateThread();
                }
                else logger.warn(returnString);
            }
        }
        
        return returnString;
    }
    
    private static AlertTemplate getAlertTemplateFromAlertTemplateParameters(Object request) {
        
        if (request == null) {
            return null;
        }

        AlertTemplate alertTemplate = new AlertTemplate();

        try {
            String parameter;

            parameter = Common.getSingleParameterAsString(request, "Name");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "name");
            String trimmedName = (parameter != null) ? parameter.trim() : "";
            alertTemplate.setName(trimmedName);

            parameter = Common.getSingleParameterAsString(request, "Description");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "description");
            if (parameter != null) {
                String trimmedParameter = parameter.trim();
                String description;
                if (trimmedParameter.length() > 100000) description = trimmedParameter.substring(0, 99999);
                else description = trimmedParameter;
                alertTemplate.setDescriptionVariable(description);
            }
            else alertTemplate.setDescriptionVariable("");
            
            parameter = Common.getSingleParameterAsString(request, "VariableSetListName");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "variable_set_list_name");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {    
                    VariableSetList variableSetList = VariableSetListsDao.getVariableSetList(DatabaseConnections.getConnection(), true, parameterTrimmed);
                    if (variableSetList != null) alertTemplate.setVariableSetListId(variableSetList.getId());
                }
            }
            else {
                parameter = Common.getSingleParameterAsString(request, "VariableSetListId");
                if (parameter == null) parameter = Common.getSingleParameterAsString(request, "variable_set_list_id");
                if (parameter != null) {
                    String parameterTrimmed = parameter.trim();
                    if (!parameterTrimmed.isEmpty() && MathUtilities.isStringAnInteger(parameterTrimmed)) alertTemplate.setVariableSetListId(Integer.parseInt(parameterTrimmed));
                }
            }
            
            parameter = Common.getSingleParameterAsString(request, "AlertNameVariable");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "alert_name_variable");
            String trimmedAlertNameVariable = (parameter != null) ? parameter.trim() : "";
            alertTemplate.setAlertNameVariable(trimmedAlertNameVariable);
            
            parameter = Common.getSingleParameterAsString(request, "MetricGroupNameVariable");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "metric_group_name_variable");
            String trimmedMetricGroupNameVariable = (parameter != null) ? parameter.trim() : "";
            alertTemplate.setMetricGroupNameVariable(trimmedMetricGroupNameVariable);

            parameter = Common.getSingleParameterAsString(request, "Enabled");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "enabled");
            if ((parameter != null) && (parameter.contains("on") || parameter.equalsIgnoreCase("true"))) alertTemplate.setIsEnabled(true);
            else alertTemplate.setIsEnabled(false);

            parameter = Common.getSingleParameterAsString(request, "CautionEnabled");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "caution_enabled");
            if ((parameter != null) && (parameter.contains("on") || parameter.equalsIgnoreCase("true"))) alertTemplate.setIsCautionEnabled(true);
            else alertTemplate.setIsCautionEnabled(false);
            
            parameter = Common.getSingleParameterAsString(request, "DangerEnabled");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "danger_enabled");
            if ((parameter != null) && (parameter.contains("on") || parameter.equalsIgnoreCase("true"))) alertTemplate.setIsDangerEnabled(true);
            else alertTemplate.setIsDangerEnabled(false);
            
            parameter = Common.getSingleParameterAsString(request, "Type");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "alert_type");
            if ((parameter != null) && parameter.contains("Availability")) alertTemplate.setAlertType(AlertTemplate.TYPE_AVAILABILITY);
            else if ((parameter != null) && parameter.contains("Threshold")) alertTemplate.setAlertType(AlertTemplate.TYPE_THRESHOLD);
            
            parameter = Common.getSingleParameterAsString(request, "AlertOnPositive");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "alert_on_positive");
            if ((parameter != null) && (parameter.contains("on") || parameter.equalsIgnoreCase("true"))) alertTemplate.setAlertOnPositive(true);
            else alertTemplate.setAlertOnPositive(false);

            parameter = Common.getSingleParameterAsString(request, "AllowResendAlert");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "allow_resend_alert");
            if ((parameter != null) && (parameter.contains("on") || parameter.equalsIgnoreCase("true"))) alertTemplate.setAllowResendAlert(true);
            else alertTemplate.setAllowResendAlert(false);

            parameter = Common.getSingleParameterAsString(request, "ResendAlertEveryTimeUnit");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "resend_alert_every_time_unit");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = DatabaseObjectCommon.getTimeUnitCodeFromString(parameterTrimmed);
                    alertTemplate.setResendAlertEveryTimeUnit(intValue);
                }
            }
            
            parameter = Common.getSingleParameterAsString(request, "ResendAlertEvery");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "resend_alert_every");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty() && MathUtilities.isStringABigDecimal(parameterTrimmed)) {    
                    BigDecimal time = new BigDecimal(parameterTrimmed, DatabaseObjectCommon.TIME_UNIT_MATH_CONTEXT);
                    BigDecimal timeInMs = DatabaseObjectCommon.getMillisecondValueForTime(time, alertTemplate.getResendAlertEveryTimeUnit());
                    if (timeInMs != null) alertTemplate.setResendAlertEvery(timeInMs.longValue());                    
                }
            }
            
            parameter = Common.getSingleParameterAsString(request, "CautionNotificationGroupNameVariable");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "caution_notification_group_name_variable");
            String trimmedCautionNotificationGroupNameVariable = (parameter != null) ? parameter.trim() : "";
            alertTemplate.setCautionNotificationGroupNameVariable(trimmedCautionNotificationGroupNameVariable);
            
            parameter = Common.getSingleParameterAsString(request, "CautionPositiveNotificationGroupNameVariable");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "caution_positive_notification_group_name_variable");
            String trimmedCautionPositiveNotificationGroupNameVariable = (parameter != null) ? parameter.trim() : "";
            alertTemplate.setCautionPositiveNotificationGroupNameVariable(trimmedCautionPositiveNotificationGroupNameVariable);
            
            parameter = Common.getSingleParameterAsString(request, "CautionWindowDurationTimeUnit");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "caution_window_duration_time_unit");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = DatabaseObjectCommon.getTimeUnitCodeFromString(parameterTrimmed);
                    alertTemplate.setCautionWindowDurationTimeUnit(intValue);
                }
            }
            
            parameter = Common.getSingleParameterAsString(request, "CautionWindowDuration");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "caution_window_duration");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty() && MathUtilities.isStringABigDecimal(parameterTrimmed)) {    
                    BigDecimal time = new BigDecimal(parameterTrimmed, DatabaseObjectCommon.TIME_UNIT_MATH_CONTEXT);
                    BigDecimal timeInMs = DatabaseObjectCommon.getMillisecondValueForTime(time, alertTemplate.getCautionWindowDurationTimeUnit());
                    if (timeInMs != null) alertTemplate.setCautionWindowDuration(timeInMs.longValue());                    
                }
            }
            
            parameter = Common.getSingleParameterAsString(request, "CautionStopTrackingAfterTimeUnit");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "caution_stop_tracking_after_time_unit");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = DatabaseObjectCommon.getTimeUnitCodeFromString(parameterTrimmed);
                    alertTemplate.setCautionStopTrackingAfterTimeUnit(intValue);
                }
            }
            
            parameter = Common.getSingleParameterAsString(request, "CautionStopTrackingAfter");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "caution_stop_tracking_after");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty() && MathUtilities.isStringABigDecimal(parameterTrimmed)) {     
                    BigDecimal time = new BigDecimal(parameterTrimmed, DatabaseObjectCommon.TIME_UNIT_MATH_CONTEXT);
                    BigDecimal timeInMs = DatabaseObjectCommon.getMillisecondValueForTime(time, alertTemplate.getCautionStopTrackingAfterTimeUnit());
                    if (timeInMs != null) alertTemplate.setCautionStopTrackingAfter(timeInMs.longValue());
                }
            }
            
            parameter = Common.getSingleParameterAsString(request, "CautionMinimumSampleCount");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "caution_minimum_sample_count");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty() && MathUtilities.isStringAnInteger(parameterTrimmed)) {      
                    Integer intValue = Integer.parseInt(parameterTrimmed);
                    alertTemplate.setCautionMinimumSampleCount(intValue);
                }
            }
            
            parameter = Common.getSingleParameterAsString(request, "CautionOperator");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "caution_operator");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = AlertTemplate.getOperatorCodeFromOperatorString(parameterTrimmed);
                    alertTemplate.setCautionOperator(intValue);
                }
            }

            parameter = Common.getSingleParameterAsString(request, "CautionCombination");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "caution_combination");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = AlertTemplate.getCombinationCodeFromString(parameterTrimmed);
                    alertTemplate.setCautionCombination(intValue);                
                }
            }
            
            parameter = Common.getSingleParameterAsString(request, "CautionCombinationCount");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "caution_combination_count");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty() && MathUtilities.isStringAnInteger(parameterTrimmed)) {                    
                    Integer intValue = Integer.parseInt(parameterTrimmed);
                    alertTemplate.setCautionCombinationCount(intValue);
                }
            }

            parameter = Common.getSingleParameterAsString(request, "CautionThreshold");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "caution_threshold");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty() && MathUtilities.isStringABigDecimal(parameterTrimmed)) {    
                    BigDecimal bigDecimalValue = new BigDecimal(parameterTrimmed);
                    alertTemplate.setCautionThreshold(bigDecimalValue);
                }
            }

            parameter = Common.getSingleParameterAsString(request, "DangerNotificationGroupNameVariable");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "danger_notification_group_name_variable");
            String trimmedDangerNotificationGroupNameVariable = (parameter != null) ? parameter.trim() : "";
            alertTemplate.setDangerNotificationGroupNameVariable(trimmedDangerNotificationGroupNameVariable);
            
            parameter = Common.getSingleParameterAsString(request, "DangerPositiveNotificationGroupNameVariable");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "danger_positive_notification_group_name_variable");
            String trimmedDangerPositiveNotificationGroupNameVariable = (parameter != null) ? parameter.trim() : "";
            alertTemplate.setDangerPositiveNotificationGroupNameVariable(trimmedDangerPositiveNotificationGroupNameVariable);
            
            parameter = Common.getSingleParameterAsString(request, "DangerWindowDurationTimeUnit");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "danger_window_duration_time_unit");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = DatabaseObjectCommon.getTimeUnitCodeFromString(parameterTrimmed);
                    alertTemplate.setDangerWindowDurationTimeUnit(intValue);
                }
            }
            
            parameter = Common.getSingleParameterAsString(request, "DangerWindowDuration");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "danger_window_duration");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty() && MathUtilities.isStringABigDecimal(parameterTrimmed)) {    
                    BigDecimal time = new BigDecimal(parameterTrimmed, DatabaseObjectCommon.TIME_UNIT_MATH_CONTEXT);
                    BigDecimal timeInMs = DatabaseObjectCommon.getMillisecondValueForTime(time, alertTemplate.getDangerWindowDurationTimeUnit());
                    if (timeInMs != null) alertTemplate.setDangerWindowDuration(timeInMs.longValue());
                }
            }
            
            parameter = Common.getSingleParameterAsString(request, "DangerStopTrackingAfterTimeUnit");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "danger_stop_tracking_after_time_unit");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = DatabaseObjectCommon.getTimeUnitCodeFromString(parameterTrimmed);
                    alertTemplate.setDangerStopTrackingAfterTimeUnit(intValue);
                }
            }
            
            parameter = Common.getSingleParameterAsString(request, "DangerStopTrackingAfter");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "danger_stop_tracking_after");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty() && MathUtilities.isStringABigDecimal(parameterTrimmed)) {    
                    BigDecimal time = new BigDecimal(parameterTrimmed, DatabaseObjectCommon.TIME_UNIT_MATH_CONTEXT);
                    BigDecimal timeInMs = DatabaseObjectCommon.getMillisecondValueForTime(time, alertTemplate.getDangerStopTrackingAfterTimeUnit());
                    if (timeInMs != null) alertTemplate.setDangerStopTrackingAfter(timeInMs.longValue());
                }
            }
           
            parameter = Common.getSingleParameterAsString(request, "DangerMinimumSampleCount");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "danger_minimum_sample_count");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty() && MathUtilities.isStringAnInteger(parameterTrimmed)) {    
                    Integer intValue = Integer.parseInt(parameterTrimmed);
                    alertTemplate.setDangerMinimumSampleCount(intValue);
                }
            }
            
            parameter = Common.getSingleParameterAsString(request, "DangerOperator");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "danger_operator");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {    
                    Integer intValue = AlertTemplate.getOperatorCodeFromOperatorString(parameterTrimmed);
                    alertTemplate.setDangerOperator(intValue);
                }
            }

            parameter = Common.getSingleParameterAsString(request, "DangerCombination");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "danger_combination");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {    
                    Integer intValue = AlertTemplate.getCombinationCodeFromString(parameterTrimmed);
                    alertTemplate.setDangerCombination(intValue);
                }
            }
            
            parameter = Common.getSingleParameterAsString(request, "DangerCombinationCount");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "danger_combination_count");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty() && MathUtilities.isStringAnInteger(parameterTrimmed)) {    
                    Integer intValue = Integer.parseInt(parameterTrimmed);
                    alertTemplate.setDangerCombinationCount(intValue);
                }
            }

            parameter = Common.getSingleParameterAsString(request, "DangerThreshold");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "danger_threshold");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty() && MathUtilities.isStringABigDecimal(parameterTrimmed)) {    
                    BigDecimal bigDecimalValue = new BigDecimal(parameterTrimmed);
                    alertTemplate.setDangerThreshold(bigDecimalValue);
                }
            }
        }
        catch (Exception e) {         
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            alertTemplate = null;
        }
        
        return alertTemplate;
    }
    
}
