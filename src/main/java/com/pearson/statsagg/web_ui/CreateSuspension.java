package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.database_objects.suspensions.SuspensionsDaoWrapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import com.pearson.statsagg.database_objects.DatabaseObjectValidation;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database_objects.suspensions.Suspension;
import com.pearson.statsagg.database_objects.suspensions.SuspensionsDao;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.utilities.time_utils.DateAndTime;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.math_utils.MathUtilities;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class CreateSuspension extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CreateSuspension.class.getName());
    
    public static final String PAGE_NAME = "Create Suspension";
    
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

            Suspension suspension = null;
            String name = request.getParameter("Name");
            if (name != null) {
                suspension = SuspensionsDao.getSuspension(DatabaseConnections.getConnection(), true, name.trim());
            }        
            
            String htmlBodyContents = buildCreateSuspensionHtml(suspension);
            List<String> additionalJavascript = new ArrayList<>();
            additionalJavascript.add("js/statsagg_create_suspension.js");
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
            String result = parseAndAlterSuspension(request);   
            
            StringBuilder htmlBuilder = new StringBuilder();
            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            String htmlBodyContent = statsAggHtmlFramework.buildHtmlBodyForPostResult(PAGE_NAME, StatsAggHtmlFramework.htmlEncode(result), "Suspensions", Suspensions.PAGE_NAME);
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
    
    private String buildCreateSuspensionHtml(Suspension suspension) {

        StringBuilder htmlBody = new StringBuilder();

        htmlBody.append(
            "<div id=\"page-content-wrapper\">\n" +
            "  <!-- Keep all page content within the page-content inset div! -->\n" +
            "  <div class=\"page-content inset statsagg_page_content_font\">\n" +
            "    <div class=\"content-header\"> \n" +
            "      <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "    </div> \n" +
            "    <form action=\"CreateSuspension\" method=\"POST\">\n");
        
        htmlBody.append("<div class=\"row create-alert-form-row\">"); 

        if ((suspension != null) && (suspension.getName() != null) && !suspension.getName().isEmpty()) {
            htmlBody.append("<input type=\"hidden\" name=\"Old_Name\" value=\"").append(StatsAggHtmlFramework.htmlEncode(suspension.getName(), true)).append("\">");
        }
        
        
        // column #1
        htmlBody.append("" +
            "<div class=\"col-md-4 statsagg_three_panel_first_panel\" > \n" +
            "  <div class=\"panel panel-info\"> \n" +
            "    <div class=\"panel-heading\"><b>Options</b></div> \n" +
            "    <div class=\"panel-body\"> \n");
            
        // name
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Name</label>\n" +
            "  <input class=\"form-control-statsagg\" placeholder=\".\" name=\"Name\" id=\"Name\"");
        
        if ((suspension != null) && (suspension.getName() != null)) {
            htmlBody.append(" value=\"").append(StatsAggHtmlFramework.htmlEncode(suspension.getName(), true)).append("\"");
        }

        htmlBody.append(">\n" + "</div>\n");
        
        
        // description
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Description</label>\n" +
            "  <textarea class=\"form-control-statsagg\" rows=\"3\" name=\"Description\" id=\"Description\">");

        if ((suspension != null) && (suspension.getDescription() != null)) {
            htmlBody.append(StatsAggHtmlFramework.htmlEncode(suspension.getDescription(), true));
        }

        htmlBody.append("</textarea>\n");
        htmlBody.append("</div>\n");
        
        
        // is enabled?
        htmlBody.append("" +
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Is Enabled?&nbsp;&nbsp;</label>\n" +
            "  <input name=\"Enabled\" id=\"Enabled\" type=\"checkbox\" ");

        if (((suspension != null) && (suspension.isEnabled() != null) && suspension.isEnabled()) || 
                (suspension == null) || (suspension.isEnabled() == null)) {
            htmlBody.append(" checked=\"checked\"");
        }

        htmlBody.append(">\n</div>\n");
       

        // suspend notification only?
        htmlBody.append("" +
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Suspend Notification Only?&nbsp;&nbsp;</label>\n" +
            "  <input name=\"SuspendNotificationOnly\" id=\"SuspendNotificationOnly\" type=\"checkbox\" ");

        if ((suspension != null) && (suspension.isSuspendNotificationOnly() != null) && suspension.isSuspendNotificationOnly() ||
                (suspension == null) || (suspension.isSuspendNotificationOnly() == null)) {
            htmlBody.append(" checked=\"checked\"");
        }

        htmlBody.append(">\n</div>\n");

        // end column 1
        htmlBody.append("</div></div></div>");

        
        
        // column #2
        htmlBody.append("" +
            "<div class=\"col-md-4 statsagg_three_panel_second_panel\" > \n" +
            "  <div class=\"panel panel-info\"> \n" +
            "    <div class=\"panel-heading\"><b>Suspend By...</b>" +
            "         <a id=\"SuspensionAssociationsPreview\" name=\"SuspensionAssociationsPreview\" class=\"iframe cboxElement statsagg_suspension_alert_associations_preview pull-right\" href=\"#\" onclick=\"generateSuspensionAssociationsPreviewLink();\">Preview Suspension Associations</a>" + 
            "    </div>" + 
            "    <div class=\"panel-body\"> \n");
            
        
        // type selection
        htmlBody.append("<input type=\"radio\" id=\"SuspendBy_AlertName_Radio\" name=\"SuspendBy\" value=\"AlertName\" ");
        if ((suspension != null) && (suspension.getSuspendBy() == Suspension.SUSPEND_BY_ALERT_ID)) htmlBody.append(" checked=\"checked\"");
        htmlBody.append("> Alert Name &nbsp;&nbsp;&nbsp;\n");
        
        htmlBody.append("<input type=\"radio\" id=\"SuspendBy_Tags_Radio\" name=\"SuspendBy\" value=\"Tags\" ");
        if ((suspension != null) && (suspension.getSuspendBy() == Suspension.SUSPEND_BY_METRIC_GROUP_TAGS)) htmlBody.append(" checked=\"checked\"");
        htmlBody.append("> Tags &nbsp;&nbsp;&nbsp;\n");
        
        htmlBody.append("<input type=\"radio\" id=\"SuspendBy_Everything_Radio\" name=\"SuspendBy\" value=\"Everything\" ");
        if ((suspension != null) && (suspension.getSuspendBy() == Suspension.SUSPEND_BY_EVERYTHING)) htmlBody.append(" checked=\"checked\" ");
        htmlBody.append("> Everything &nbsp;&nbsp;&nbsp;\n");
        
        htmlBody.append("<input type=\"radio\" id=\"SuspendBy_Metrics_Radio\" name=\"SuspendBy\" value=\"Metrics\" ");
        if ((suspension != null) && (suspension.getSuspendBy() == Suspension.SUSPEND_BY_METRICS)) htmlBody.append(" checked=\"checked\" ");
        htmlBody.append("> Metrics\n");
        
        htmlBody.append("<br><br>\n");
        
        
        // alert name
        htmlBody.append("" +
            "<div id=\"SuspendBy_AlertName_Div\">\n" +
            "  <div class=\"form-group\" id=\"AlertNameLookup\"> \n" +
            "    <input class=\"typeahead form-control-statsagg\" placeholder=\"Enter the name of the alert that you want to suspend.\" autocomplete=\"off\" name=\"AlertName\" id=\"AlertName\" ");
        
        if ((suspension != null) && (suspension.getAlertId() != null)) {
            Alert alert = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, suspension.getAlertId());
            
            if ((alert != null) && (alert.getName() != null)) htmlBody.append("value=\"").append(StatsAggHtmlFramework.htmlEncode(alert.getName(), true)).append("\"");
        }
 
        htmlBody.append("" +
            "> \n" +
            "  </div>\n" +
            "</div>\n");
        
        
        // metric group tags (inclusive)
        htmlBody.append("" +
            "<div id=\"SuspendBy_Tags_Div\">\n" +
            "  <div class=\"form-group\"> \n" +
            "    <textarea class=\"form-control-statsagg\" placeholder=\"For an alert to be suspended, it must be tagged with ALL of the tags listed here. " +
            "List one tag per line.\" rows=\"5\" name=\"MetricGroupTagsInclusive\" id=\"MetricGroupTagsInclusive\" >");
        
        if ((suspension != null) && (suspension.getMetricGroupTagsInclusive() != null)) {
            htmlBody.append(StatsAggHtmlFramework.htmlEncode(suspension.getMetricGroupTagsInclusive(), true));
        }
 
        htmlBody.append("" +
            "</textarea> \n" +
            "  </div>\n" +
            "</div>\n");
        
        
        // metric group tags (exclusive)
        htmlBody.append("" +
            "<div id=\"SuspendBy_Everything_Div\">\n" +
            "  <div class=\"form-group\"> \n" +
            "    <textarea class=\"form-control-statsagg\" placeholder=\"For an alert to be excluded from suspension, it must be tagged with ANY of the tags listed here. " +
            "List one tag per line.\" rows=\"5\" name=\"MetricGroupTagsExclusive\" id=\"MetricGroupTagsExclusive\" >");
        
        if ((suspension != null) && (suspension.getMetricGroupTagsExclusive() != null)) {
            htmlBody.append(StatsAggHtmlFramework.htmlEncode(suspension.getMetricGroupTagsExclusive(), true));
        }
 
        htmlBody.append("" +
            "</textarea> \n" +
            "  </div>\n" +
            "</div>\n");
        
        
        // metric suspension
        htmlBody.append("" +
            "<div id=\"SuspendBy_Metrics_Div\">\n" +
            "  <div class=\"form-group\"> \n" +
            "    <textarea class=\"form-control-statsagg\" placeholder=\" " +
            "List one regex per line.\" rows=\"5\" name=\"MetricSuspensionRegexes\" id=\"MetricSuspensionRegexes\" >");
        
        if ((suspension != null) && (suspension.getMetricSuspensionRegexes() != null)) {
            htmlBody.append(StatsAggHtmlFramework.htmlEncode(suspension.getMetricSuspensionRegexes(), true));
        }
 
        htmlBody.append("" +
            "</textarea> \n" +
            "  </div>\n" +
            "</div>\n");
        
        // end column 2
        htmlBody.append("</div></div></div>");
        
  
        
        // column #3
        htmlBody.append("" +
            "<div class=\"col-md-4 statsagg_three_panel_third_panel\" > \n" +
            "   <div class=\"panel panel-info\"> \n" +
            "       <div class=\"panel-heading\"><b>Suspension Type</b></div> \n" +
            "       <div class=\"panel-body\"> \n");
        
        
        // one time or recurring?
        String startSuspensionTypeRecurring = "<input type=\"radio\" id=\"Type_Recurring\" name=\"Type\" value=\"Recurring\" ";
        String endSuspensionTypeRecurring = " > Recurring (daily)&nbsp;&nbsp;&nbsp; \n";
        String startSuspensionTypeOneTime = "<input type=\"radio\" id=\"Type_OneTime\" name=\"Type\" value=\"OneTime\" ";
        String endSuspensionTypeOneTime = " > One Time \n";

        if ((suspension != null) && (suspension.isOneTime() != null)) {
            if (suspension.isOneTime()) {
                htmlBody.append(startSuspensionTypeRecurring).append(endSuspensionTypeRecurring)
                        .append(startSuspensionTypeOneTime).append(" checked=\"checked\" ").append(endSuspensionTypeOneTime);
            }
            else {
                htmlBody.append(startSuspensionTypeRecurring).append(" checked=\"checked\" ").append(endSuspensionTypeRecurring)
                        .append(startSuspensionTypeOneTime).append(endSuspensionTypeOneTime);
            }
        }
        else {
                htmlBody.append(startSuspensionTypeRecurring).append(endSuspensionTypeRecurring)
                        .append(startSuspensionTypeOneTime).append(endSuspensionTypeOneTime);
        }
        
        htmlBody.append("" +
            "<br><br>\n" +
            "<table style=\"border-spacing:0px 2px; width:100%;\">\n" +
            "<tbody>\n");
        
        
        // start date
        htmlBody.append(        
            "<tr>\n" +
            "  <th style=\"width:1%;\"><div class=\"create-suspension-th\" id=\"DateTimePicker_StartDate_Label_Div\">Start Date:</div></th>\n" +
            "  <td>\n" +
            "    <div class=\"input-group\" id=\"DateTimePicker_StartDate_Div\" style=\"width:100%;\"> \n" +
            "      <input class=\"form-control-datetime\" name=\"StartDate\" id=\"StartDate\" style=\"width:100%;\" type=\"text\" ");
        
        if ((suspension != null) && (suspension.getStartDate() != null)) {
            String startDateString = DateAndTime.getFormattedDateAndTime(suspension.getStartDate(), "MM/dd/yyyy");
            htmlBody.append(" value=\"").append(startDateString).append("\"");
        }
        
        htmlBody.append(       
            " > \n" +
            "      <span class=\"input-group-addon\" style=\"font-size:10px;\" ><span class=\"glyphicon-calendar glyphicon\"></span></span> \n" +
            "    </div> \n" +
            "  </td>\n" +
            "</tr>\n");
         
        
        // recur on days of week
        htmlBody.append(        
            "<tr>\n" +
            "  <th><div class=\"create-suspension-th\" id=\"RecurOnDays_Label_Div\">Recurs on:</div></th>\n" +
            "  <td>\n" +
            "    <div id=\"RecurOnDays_Div\">\n");

        htmlBody.append("<label class=\"checkbox-inline\"><input name=\"RecurSunday\" id=\"RecurSunday\" type=\"checkbox\" ");
        if ((suspension == null) || ((suspension.isRecurSunday() == null) || suspension.isRecurSunday())) htmlBody.append("checked=\"checked\"");
        htmlBody.append(">S</label>\n");      
                
        htmlBody.append("<label class=\"checkbox-inline\"><input name=\"RecurMonday\" id=\"RecurMonday\" type=\"checkbox\" ");
        if ((suspension == null) || ((suspension.isRecurMonday() == null) || suspension.isRecurMonday())) htmlBody.append("checked=\"checked\"");
        htmlBody.append(">M</label>\n");      
        
        htmlBody.append("<label class=\"checkbox-inline\"><input name=\"RecurTuesday\" id=\"RecurTuesday\" type=\"checkbox\" ");
        if ((suspension == null) || ((suspension.isRecurTuesday() == null) || suspension.isRecurTuesday())) htmlBody.append("checked=\"checked\"");
        htmlBody.append(">T</label>\n");      
     
        htmlBody.append("<label class=\"checkbox-inline\"><input name=\"RecurWednesday\" id=\"RecurWednesday\" type=\"checkbox\" ");
        if ((suspension == null) || ((suspension.isRecurWednesday() == null) || suspension.isRecurWednesday())) htmlBody.append("checked=\"checked\"");
        htmlBody.append(">W</label>\n");   
        
        htmlBody.append("<label class=\"checkbox-inline\"><input name=\"RecurThursday\" id=\"RecurThursday\" type=\"checkbox\" ");
        if ((suspension == null) || ((suspension.isRecurThursday() == null) || suspension.isRecurThursday())) htmlBody.append("checked=\"checked\"");
        htmlBody.append(">T</label>\n");   
        
        htmlBody.append("<label class=\"checkbox-inline\"><input name=\"RecurFriday\" id=\"RecurFriday\" type=\"checkbox\" ");
        if ((suspension == null) || ((suspension.isRecurFriday() == null) || suspension.isRecurFriday())) htmlBody.append("checked=\"checked\"");
        htmlBody.append(">F</label>\n");   
        
        htmlBody.append("<label class=\"checkbox-inline\"><input name=\"RecurSaturday\" id=\"RecurSaturday\" type=\"checkbox\" ");
        if ((suspension == null) || ((suspension.isRecurSaturday() == null) || suspension.isRecurSaturday())) htmlBody.append("checked=\"checked\"");
        htmlBody.append(">S</label>\n");   
        
        htmlBody.append("</div>\n" + "</td>\n" + "</tr>\n");
        
        htmlBody.append("<tr id=\"Type_Spacer1\"><th>&nbsp;</th><td>&nbsp;</td></tr>\n");
        
        
        // start time
        htmlBody.append(
            "<tr>\n" +
            "  <th><div class=\"create-suspension-th\" id=\"DateTimePicker_StartTime_Label_Div\">Start Time:</div></th>\n" +
            "    <td>\n" +
            "      <div class=\"input-group\" id=\"DateTimePicker_StartTime_Div\" style=\"width:100%;\" > \n" +
            "        <input class=\"form-control-datetime\" name=\"StartTime\" id=\"StartTime\" style=\"width:100%;\" type=\"text\" ");
        
        if ((suspension != null) && (suspension.getStartTime() != null)) {
            String startTimeString = DateAndTime.getFormattedDateAndTime(suspension.getStartTime(), "h:mm a");
            htmlBody.append(" value=\"").append(startTimeString).append("\"");
        }
  
        htmlBody.append("" +
            "> \n" +
            "        <span class=\"input-group-addon\" style=\"font-size:10px;\"><span class=\"glyphicon-time glyphicon\"></span></span> \n" +
            "      </div>\n" +
            "    </td>\n" +
            "</tr>\n");
        
        
        // duration
        htmlBody.append(
            "<tr>\n" +
            "  <th><div class=\"create-suspension-th\" id=\"Duration_Label_Div\">Duration:</div></th>\n" +
            "  <td>\n" +
            "    <div style=\" padding-top: 3px;\" id=\"Duration_Div\">\n" +
            "      <div class=\"col-xs-6\"> <input class=\"form-control-statsagg\" name=\"Duration\" id=\"Duration\" ");
                    
        if ((suspension != null) && (suspension.getDuration() != null)) {
            BigDecimal duration = DatabaseObjectCommon.getValueForTimeFromMilliseconds(suspension.getDuration(), suspension.getDurationTimeUnit());
            htmlBody.append(" value=\"").append(duration.stripTrailingZeros().toPlainString()).append("\"");
        }
        htmlBody.append("></div>\n");

        
        // duration time unit
        htmlBody.append("<div class=\"col-xs-6\"> <select class=\"form-control-statsagg col-xs-6\" name=\"DurationTimeUnit\" id=\"DurationTimeUnit\">\n");

        if ((suspension != null) && (DatabaseObjectCommon.getTimeUnitStringFromCode(suspension.getDurationTimeUnit(), true) != null)) {
            String whiteSpace = "							  ";
            String timeUnitString = DatabaseObjectCommon.getTimeUnitStringFromCode(suspension.getDurationTimeUnit(), true);

            if (timeUnitString.equalsIgnoreCase("Minutes")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">Minutes</option>\n");
            else htmlBody.append(whiteSpace).append("<option>Minutes</option>\n");

            if (timeUnitString.equalsIgnoreCase("Hours")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">Hours</option>\n");
            else htmlBody.append(whiteSpace).append("<option>Hours</option>\n");

            if (timeUnitString.equalsIgnoreCase("Days")) htmlBody.append(whiteSpace).append("<option selected=\"selected\">Days</option>\n");
            else htmlBody.append(whiteSpace).append("<option>Days</option>\n");
        }
        else {
            htmlBody.append(
                "<option>Minutes</option>\n" +
                "<option>Hours</option>\n" +
                "<option>Days</option>\n"
            );
        }
        
        htmlBody.append("</select></div>\n");
        htmlBody.append("</div>\n </td>\n </tr>\n");

        // end column 3
        htmlBody.append("</tbody>\n </table>\n </div>\n </div>\n </div>\n");
        
      

        // end form & body content
        htmlBody.append(        
            "</div>" +
            "<button type=\"submit\" class=\"btn btn-default btn-primary statsagg_button_no_shadow statsagg_page_content_font\">Submit</button>\n" +
            "&nbsp;&nbsp;&nbsp;" +
            "<a href=\"Suspensions\" class=\"btn btn-default statsagg_page_content_font\" role=\"button\">Cancel</a>");
        
        htmlBody.append("</form></div></div>");
        
        return htmlBody.toString();
    }
    
    public String parseAndAlterSuspension(Object request) {
        
        if (request == null) {
            return null;
        }
        
        String returnString;
        
        // create a new suspension based on the input parameters of the request
        Suspension suspension = getSuspensionFromRequestParameters(request);
        
        // help determine if the suspension is being renamed by getting the previous name of the suspension (if it exists)
        String oldName = getOldSuspensionName(request);
        
        // insert/update/delete records in the database
        DatabaseObjectValidation databaseObjectValidation = Suspension.isValid(suspension);
        
        if (suspension == null) {
            returnString = "Failed to create or alter suspension. Reason=\"One or more invalid suspension fields detected\".";
            logger.warn(returnString);
        } 
        else if (!databaseObjectValidation.isValid()) {
            returnString = "Failed to create or alter suspension. Reason=\"" + databaseObjectValidation.getReason() + "\".";
        }
        else {
            SuspensionsDaoWrapper suspensionsDaoWrapper = SuspensionsDaoWrapper.alterRecordInDatabase(suspension, oldName);
            returnString = suspensionsDaoWrapper.getReturnString();

            if (suspensionsDaoWrapper.getLastAlterRecordStatus() == SuspensionsDaoWrapper.STATUS_CODE_SUCCESS) {
                logger.info("Running suspension routine");
                com.pearson.statsagg.threads.alert_related.Suspensions suspensions = new com.pearson.statsagg.threads.alert_related.Suspensions();
                suspensions.runSuspensionRoutine();
            }
        }

        return returnString;
    }
    
    protected static String getOldSuspensionName(Object request) {
        
        try {
            if (request == null) return null;

            String oldName = Common.getSingleParameterAsString(request, "Old_Name");
            if (oldName == null) oldName = Common.getSingleParameterAsString(request, "old_name");

            if (oldName == null) {
                String id = Common.getSingleParameterAsString(request, "Id");
                if (id == null) id = Common.getSingleParameterAsString(request, "id");

                if (id != null) {
                    try {
                        Integer id_Integer = Integer.parseInt(id.trim());
                        Suspension oldSuspension = SuspensionsDao.getSuspension(DatabaseConnections.getConnection(), true, id_Integer);
                        oldName = oldSuspension.getName();
                    }
                    catch (Exception e){}
                }
            }

            return oldName;
        }
        catch (Exception e){
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        
    }
    
    private Suspension getSuspensionFromRequestParameters(Object request) {
        
        if (request == null) {
            return null;
        }
                
        Suspension suspension = new Suspension();

        try {
            String parameter;

            // column #1 parameters
            parameter = Common.getSingleParameterAsString(request, "Name");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "name");
            String trimmedName = (parameter != null) ? parameter.trim() : "";
            suspension.setName(trimmedName);
            
            parameter = Common.getSingleParameterAsString(request, "Description");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "description");
            if (parameter != null) {
                String trimmedParameter = parameter.trim();
                String description;
                if (trimmedParameter.length() > 100000) description = trimmedParameter.substring(0, 99999);
                else description = trimmedParameter;
                suspension.setDescription(description);
            }
            else suspension.setDescription("");
            
            parameter = Common.getSingleParameterAsString(request, "Enabled");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "enabled");
            if ((parameter != null) && (parameter.contains("on") || parameter.contains("true"))) suspension.setIsEnabled(true);
            else suspension.setIsEnabled(false);

            parameter = Common.getSingleParameterAsString(request, "SuspendNotificationOnly");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "suspend_notification_only");
            if ((parameter != null) && (parameter.contains("on") || parameter.contains("true")) ) suspension.setIsSuspendNotificationOnly(true);
            else suspension.setIsSuspendNotificationOnly(false);
 
            
            // column #2 parameters
            parameter = Common.getSingleParameterAsString(request, "SuspendBy");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "suspend_by");
            if ((parameter != null) && parameter.contains("AlertName")) suspension.setSuspendBy(Suspension.SUSPEND_BY_ALERT_ID);
            else if ((parameter != null) && parameter.contains("Tags")) suspension.setSuspendBy(Suspension.SUSPEND_BY_METRIC_GROUP_TAGS);
            else if ((parameter != null) && parameter.contains("Everything")) suspension.setSuspendBy(Suspension.SUSPEND_BY_EVERYTHING);
            else if ((parameter != null) && parameter.contains("Metrics")) suspension.setSuspendBy(Suspension.SUSPEND_BY_METRICS);

            parameter = Common.getSingleParameterAsString(request, "AlertName");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "alert_name");
            Alert alert = AlertsDao.getAlert(DatabaseConnections.getConnection(), true, parameter);
            if (alert != null) suspension.setAlertId(alert.getId());

            parameter = Common.getSingleParameterAsString(request, "MetricGroupTagsInclusive");
            if (parameter != null) {
                String trimmedTags = Suspension.trimNewLineDelimitedTags(parameter);
                suspension.setMetricGroupTagsInclusive(trimmedTags);
            }
            else if (request instanceof JsonObject) {
                JsonObject jsonObject = (JsonObject) request;
                JsonArray jsonArray = jsonObject.getAsJsonArray("metric_group_tags_inclusive");
                if (jsonArray != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (JsonElement jsonElement : jsonArray) stringBuilder.append(jsonElement.getAsString()).append("\n");
                    suspension.setMetricGroupTagsInclusive(stringBuilder.toString().trim()); 
                }
            }
            
            parameter = Common.getSingleParameterAsString(request, "MetricGroupTagsExclusive");
            if (parameter != null) {
                String trimmedTags = Suspension.trimNewLineDelimitedTags(parameter);
                suspension.setMetricGroupTagsExclusive(trimmedTags);
            }
            else if (request instanceof JsonObject) {
                JsonObject jsonObject = (JsonObject) request;
                JsonArray jsonArray = jsonObject.getAsJsonArray("metric_group_tags_exclusive");
                if (jsonArray != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (JsonElement jsonElement : jsonArray) stringBuilder.append(jsonElement.getAsString()).append("\n");
                    suspension.setMetricGroupTagsExclusive(stringBuilder.toString().trim()); 
                }
            }

            parameter = Common.getSingleParameterAsString(request, "MetricSuspensionRegexes");
            if (parameter != null) {
                String metricSuspensionRegexes = Suspension.trimNewLineDelimitedTags(parameter);
                suspension.setMetricSuspensionRegexes(metricSuspensionRegexes);
            }
            else if (request instanceof JsonObject) {
                JsonObject jsonObject = (JsonObject) request;
                JsonArray jsonArray = jsonObject.getAsJsonArray("metric_suspension_regexes");
                if (jsonArray != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (JsonElement jsonElement : jsonArray) stringBuilder.append(jsonElement.getAsString()).append("\n");
                    suspension.setMetricSuspensionRegexes(stringBuilder.toString().trim()); 
                }
            }
            
            
            // column #3 parameters
            parameter = Common.getSingleParameterAsString(request, "Type");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "one_time");
            if ((parameter != null) && parameter.contains("Recurring")) suspension.setIsOneTime(false);
            else if ((parameter != null) && parameter.contains("OneTime")) suspension.setIsOneTime(true);
            else if ((parameter != null) && parameter.contains("true")) suspension.setIsOneTime(true);
            else if ((parameter != null) && parameter.contains("false")) suspension.setIsOneTime(false);
            
            parameter = Common.getSingleParameterAsString(request, "StartDate");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "start_date");
            if (parameter != null) {
                String startDateStringTrimmed = parameter.trim();
                Calendar startDateCalendar = DateAndTime.getCalendarFromFormattedString(startDateStringTrimmed, "MM/dd/yyyy");
                Timestamp startDateTimestamp = new Timestamp(startDateCalendar.getTimeInMillis());
                suspension.setStartDate(startDateTimestamp);
            }
                        
            parameter = Common.getSingleParameterAsString(request, "RecurSunday");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "recur_sunday");
            if ((parameter != null) && (parameter.contains("on") || parameter.equalsIgnoreCase("true"))) suspension.setIsRecurSunday(true);
            else suspension.setIsRecurSunday(false);
            
            parameter = Common.getSingleParameterAsString(request, "RecurMonday");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "recur_monday");
            if ((parameter != null) && (parameter.contains("on") || parameter.equalsIgnoreCase("true"))) suspension.setIsRecurMonday(true);
            else suspension.setIsRecurMonday(false);
            
            parameter = Common.getSingleParameterAsString(request, "RecurTuesday");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "recur_tuesday");
            if ((parameter != null) && (parameter.contains("on") || parameter.equalsIgnoreCase("true"))) suspension.setIsRecurTuesday(true);
            else suspension.setIsRecurTuesday(false);
            
            parameter = Common.getSingleParameterAsString(request, "RecurWednesday");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "recur_wednesday");
            if ((parameter != null) && (parameter.contains("on") || parameter.equalsIgnoreCase("true"))) suspension.setIsRecurWednesday(true);
            else suspension.setIsRecurWednesday(false);
            
            parameter = Common.getSingleParameterAsString(request, "RecurThursday");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "recur_thursday");
            if ((parameter != null) && (parameter.contains("on") || parameter.equalsIgnoreCase("true"))) suspension.setIsRecurThursday(true);
            else suspension.setIsRecurThursday(false);

            parameter = Common.getSingleParameterAsString(request, "RecurFriday");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "recur_friday");
            if ((parameter != null) && (parameter.contains("on") || parameter.equalsIgnoreCase("true"))) suspension.setIsRecurFriday(true);
            else suspension.setIsRecurFriday(false);
            
            parameter = Common.getSingleParameterAsString(request, "RecurSaturday");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "recur_saturday");
            if ((parameter != null) && (parameter.contains("on") || parameter.equalsIgnoreCase("true"))) suspension.setIsRecurSaturday(true);
            else suspension.setIsRecurSaturday(false);

            parameter = Common.getSingleParameterAsString(request, "StartTime");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "start_time");
            if (parameter != null) {
                String startTimeStringTrimmed = parameter.trim();
                String startTimeStringTrimmedWithDate = "01/01/1970" + " " + startTimeStringTrimmed;
                Calendar startTimeCalendar = DateAndTime.getCalendarFromFormattedString(startTimeStringTrimmedWithDate, "MM/dd/yyyy h:mm a");
                Timestamp startTimeTimestamp = new Timestamp(startTimeCalendar.getTimeInMillis());
                suspension.setStartTime(startTimeTimestamp);
            }
            
            parameter = Common.getSingleParameterAsString(request, "DurationTimeUnit");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "duration_time_unit");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = DatabaseObjectCommon.getTimeUnitCodeFromString(parameterTrimmed);
                    suspension.setDurationTimeUnit(intValue);
                }
            }
            
            parameter = Common.getSingleParameterAsString(request, "Duration");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "duration");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty() && MathUtilities.isStringABigDecimal(parameterTrimmed)) {    
                    BigDecimal time = new BigDecimal(parameterTrimmed);
                    BigDecimal timeInMs = DatabaseObjectCommon.getMillisecondValueForTime(time, suspension.getDurationTimeUnit());
                    if (timeInMs != null) suspension.setDuration(timeInMs.longValue());
                }
            }

            if ((suspension.isOneTime() != null) && suspension.isOneTime() && (suspension.getDuration() != null) &&
                    (suspension.getStartDate() != null) && (suspension.getStartTime() != null)) {
                Calendar startTime = Calendar.getInstance();
                startTime.setTimeInMillis(suspension.getStartTime().getTime());
               
                Calendar startDateAndTime = Calendar.getInstance();
                startDateAndTime.setTimeInMillis(suspension.getStartDate().getTime());
                startDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(startDateAndTime, 
                        startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE), startTime.get(Calendar.SECOND), startTime.get(Calendar.MILLISECOND));
                
                Calendar deleteDateAndTime = Calendar.getInstance();
                deleteDateAndTime.setTimeInMillis(startDateAndTime.getTimeInMillis());
                deleteDateAndTime.add(Calendar.SECOND, (int) (suspension.getDuration() / 1000));
                
                Timestamp deleteAtTimestamp = new Timestamp(deleteDateAndTime.getTimeInMillis());
                suspension.setDeleteAtTimestamp(deleteAtTimestamp);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            suspension = null;
        }
            
        return suspension;
    }
    
}
