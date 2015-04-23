package com.pearson.statsagg.webui;

import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database.alert_suspensions.AlertSuspension;
import com.pearson.statsagg.database.alert_suspensions.AlertSuspensionsDao;
import com.pearson.statsagg.database.alerts.Alert;
import com.pearson.statsagg.database.alerts.AlertsDao;
import com.pearson.statsagg.utilities.DateAndTime;
import com.pearson.statsagg.utilities.StackTrace;
import java.sql.Timestamp;
import java.util.Calendar;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "CreateAlertSuspension", urlPatterns = {"/CreateAlertSuspension"})
public class CreateAlertSuspension extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CreateAlertSuspension.class.getName());
    
    public static final String PAGE_NAME = "Create Alert Suspension";
    
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

            AlertSuspension alertSuspension = null;
            String name = request.getParameter("Name");
            if (name != null) {
                AlertSuspensionsDao alertSuspensionsDao = new AlertSuspensionsDao();
                alertSuspension = alertSuspensionsDao.getAlertSuspensionByName(name.trim());
            }        
            
            String htmlBodyContents = buildCreateAlertSuspensionHtml(alertSuspension);
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
            String result = parseAndAlterAlertSuspension(request);
            
            response.setContentType("text/html");     
            
            StringBuilder htmlBuilder = new StringBuilder();
            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            String htmlBodyContent = statsAggHtmlFramework.buildHtmlBodyForPostResult(PAGE_NAME, StatsAggHtmlFramework.htmlEncode(result), "AlertSuspensions", AlertSuspensions.PAGE_NAME);
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
    
    private String buildCreateAlertSuspensionHtml(AlertSuspension alertSuspension) {

        StringBuilder htmlBody = new StringBuilder();

        htmlBody.append(
            "<div id=\"page-content-wrapper\">\n" +
            "  <!-- Keep all page content within the page-content inset div! -->\n" +
            "  <div class=\"page-content inset\" style=\"font-size:12px;\">\n" +
            "    <div class=\"content-header\"> \n" +
            "      <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "    </div> \n" +
            "    <form action=\"CreateAlertSuspension\" method=\"POST\">\n");
        
        htmlBody.append("<div class=\"row create-alert-form-row\">"); 

        if ((alertSuspension != null) && (alertSuspension.getName() != null) && !alertSuspension.getName().isEmpty()) {
            htmlBody.append("<input type=\"hidden\" name=\"Old_Name\" value=\"").append(Encode.forHtmlAttribute(alertSuspension.getName())).append("\">");
        }
        
        
        // column #1
        htmlBody.append("" +
            "<div class=\"col-md-4 col-md-4-min-width-statsagg\" > \n" +
            "  <div class=\"panel panel-info\"> \n" +
            "    <div class=\"panel-heading\"><b>Options</b></div> \n" +
            "    <div class=\"panel-body\"> \n");
            
        // name
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Name</label>\n" +
            "  <input class=\"form-control-statsagg\" placeholder=\".\" name=\"Name\" id=\"Name\"");
        
        if ((alertSuspension != null) && (alertSuspension.getName() != null)) {
            htmlBody.append(" value=\"").append(Encode.forHtmlAttribute(alertSuspension.getName())).append("\"");
        }

        htmlBody.append(">\n" + "</div>\n");
        
        
        // is enabled?
        htmlBody.append("" +
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Is enabled?&nbsp;&nbsp;</label>\n" +
            "  <input name=\"Enabled\" id=\"Enabled\" type=\"checkbox\" ");

        if (((alertSuspension != null) && (alertSuspension.isEnabled() != null) && alertSuspension.isEnabled()) || 
                (alertSuspension == null) || (alertSuspension.isEnabled() == null)) {
            htmlBody.append(" checked=\"checked\"");
        }

        htmlBody.append(">\n</div>\n");
       

        // suspend notification only?
        htmlBody.append("" +
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Suspend notification only?&nbsp;&nbsp;</label>\n" +
            "  <input name=\"SuspendNotificationOnly\" id=\"SuspendNotificationOnly\" type=\"checkbox\" ");

        if ((alertSuspension != null) && (alertSuspension.isSuspendNotificationOnly() != null) && alertSuspension.isSuspendNotificationOnly() ||
                (alertSuspension == null) || (alertSuspension.isSuspendNotificationOnly() == null)) {
            htmlBody.append(" checked=\"checked\"");
        }

        htmlBody.append(">\n</div>\n");

        // end column 1
        htmlBody.append("</div></div></div>");

        
        
        // column #2
        htmlBody.append("" +
            "<div class=\"col-md-4 col-md-4-min-width-statsagg\" > \n" +
            "  <div class=\"panel panel-info\"> \n" +
            "    <div class=\"panel-heading\"><b>Suspend by...</b>" +
            "         <a id=\"AlertSuspensionAlertAssociationsPreview\" name=\"AlertSuspensionAlertAssociationsPreview\" class=\"iframe cboxElement statsagg_alert_suspension_alert_associations_preview pull-right\" href=\"#\" onclick=\"generateAlertSuspensionAssociationsPreviewLink();\">Preview Alert Associations</a>" + 
            "    </div>" + 
            "    <div class=\"panel-body\"> \n");
            
        
        // type selection
        htmlBody.append("<input type=\"radio\" id=\"CreateAlertSuspension_SuspendBy_AlertName_Radio\" name=\"CreateAlertSuspension_SuspendBy\" value=\"AlertName\" ");
        if ((alertSuspension != null) && (alertSuspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_ALERT_ID)) htmlBody.append(" checked=\"checked\"");
        htmlBody.append("> Alert Name &nbsp;&nbsp;&nbsp;\n");
        
        htmlBody.append("<input type=\"radio\" id=\"CreateAlertSuspension_SuspendBy_Tags_Radio\" name=\"CreateAlertSuspension_SuspendBy\" value=\"Tags\" ");
        if ((alertSuspension != null) && (alertSuspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS)) htmlBody.append(" checked=\"checked\"");
        htmlBody.append("> Tags &nbsp;&nbsp;&nbsp;\n");
        
        htmlBody.append("<input type=\"radio\" id=\"CreateAlertSuspension_SuspendBy_Everything_Radio\" name=\"CreateAlertSuspension_SuspendBy\" value=\"Everything\" ");
        if ((alertSuspension != null) && (alertSuspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_EVERYTHING)) htmlBody.append(" checked=\"checked\" ");
        htmlBody.append("> Everything\n");
        
        htmlBody.append("<br><br>\n");
        
        
        // alert name
        htmlBody.append("" +
            "<div id=\"CreateAlertSuspension_SuspendBy_AlertName_Div\">\n" +
            "  <div class=\"form-group\" id=\"AlertNameLookup\"> \n" +
            "    <input class=\"typeahead form-control-statsagg\" placeholder=\"Enter the name of the alert that you want to suspend.\" autocomplete=\"off\" name=\"AlertName\" id=\"AlertName\" ");
        
        if ((alertSuspension != null) && (alertSuspension.getAlertId() != null)) {
            AlertsDao alertsDao = new AlertsDao();
            Alert alert = alertsDao.getAlert(alertSuspension.getAlertId());
            
            if ((alert != null) && (alert.getName() != null)) htmlBody.append("value=\"").append(Encode.forHtmlAttribute(alert.getName())).append("\"");
        }
 
        htmlBody.append("" +
            "> \n" +
            "  </div>\n" +
            "</div>\n");
        
        
        // metric group tags (inclusive)
        htmlBody.append("" +
            "<div id=\"CreateAlertSuspension_SuspendBy_Tags_Div\">\n" +
            "  <div class=\"form-group\"> \n" +
            "    <textarea class=\"form-control-statsagg\" placeholder=\"For an alert to be suspended, it must be tagged with ALL of the tags listed here. " +
            "List one tag per line.\" rows=\"5\" name=\"MetricGroupTagsInclusive\" id=\"MetricGroupTagsInclusive\" >");
        
        if ((alertSuspension != null) && (alertSuspension.getMetricGroupTagsInclusive() != null)) {
            htmlBody.append(Encode.forHtmlAttribute(alertSuspension.getMetricGroupTagsInclusive()));
        }
 
        htmlBody.append("" +
            "</textarea> \n" +
            "  </div>\n" +
            "</div>\n");
        
        
        // metric group tags (exclusive)
        htmlBody.append("" +
            "<div id=\"CreateAlertSuspension_SuspendBy_Everything_Div\">\n" +
            "  <div class=\"form-group\"> \n" +
            "    <textarea class=\"form-control-statsagg\" placeholder=\"For an alert to be excluded from suspension, it must be tagged with ANY of the tags listed here. " +
            "List one tag per line.\" rows=\"5\" name=\"MetricGroupTagsExclusive\" id=\"MetricGroupTagsExclusive\" >");
        
        if ((alertSuspension != null) && (alertSuspension.getMetricGroupTagsExclusive() != null)) {
            htmlBody.append(Encode.forHtmlAttribute(alertSuspension.getMetricGroupTagsExclusive()));
        }
 
        htmlBody.append("" +
            "</textarea> \n" +
            "  </div>\n" +
            "</div>\n");
        
        // end column 2
        htmlBody.append("</div></div></div>");
        
  
        
        // column #3
        htmlBody.append("" +
            "<div class=\"col-md-4 col-md-4-min-width-statsagg\" > \n" +
            "   <div class=\"panel panel-info\"> \n" +
            "       <div class=\"panel-heading\"><b>Suspension Type</b></div> \n" +
            "       <div class=\"panel-body\"> \n");
        
        
        // one time or recurring?
        String startSuspensionTypeRecurring = "<input type=\"radio\" id=\"CreateAlertSuspension_Type_Recurring\" name=\"CreateAlertSuspension_Type\" value=\"Recurring\" ";
        String endSuspensionTypeRecurring = " > Recurring (daily)&nbsp;&nbsp;&nbsp; \n";
        String startSuspensionTypeOneTime = "<input type=\"radio\" id=\"CreateAlertSuspension_Type_OneTime\" name=\"CreateAlertSuspension_Type\" value=\"OneTime\" ";
        String endSuspensionTypeOneTime = " > One Time \n";

        if ((alertSuspension != null) && (alertSuspension.isOneTime() != null)) {
            if (alertSuspension.isOneTime()) {
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
            "  <th style=\"width:1%;\"><div class=\"create-alert-suspension-th\" id=\"CreateAlertSuspension_DateTimePicker_StartDate_Label_Div\">Start Date:</div></th>\n" +
            "  <td>\n" +
            "    <div class=\"input-group\" id=\"CreateAlertSuspension_DateTimePicker_StartDate_Div\" style=\"width:100%;\"> \n" +
            "      <input class=\"form-control-datetime\" name=\"StartDate\" id=\"StartDate\" style=\"width:100%;\" type=\"text\" ");
        
        if ((alertSuspension != null) && (alertSuspension.getStartDate() != null)) {
            String startDateString = DateAndTime.getFormattedDateAndTime(alertSuspension.getStartDate(), "MM/dd/yyyy");
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
            "  <th><div class=\"create-alert-suspension-th\" id=\"CreateAlertSuspension_RecurOnDays_Label_Div\">Recurs on:</div></th>\n" +
            "  <td>\n" +
            "    <div id=\"CreateAlertSuspension_RecurOnDays_Div\">\n");

        htmlBody.append("<label class=\"checkbox-inline\"><input name=\"RecurSunday\" id=\"RecurSunday\" type=\"checkbox\" ");
        if ((alertSuspension == null) || ((alertSuspension.isRecurSunday() == null) || alertSuspension.isRecurSunday())) htmlBody.append("checked=\"checked\"");
        htmlBody.append(">S</label>\n");      
                
        htmlBody.append("<label class=\"checkbox-inline\"><input name=\"RecurMonday\" id=\"RecurMonday\" type=\"checkbox\" ");
        if ((alertSuspension == null) || ((alertSuspension.isRecurMonday() == null) || alertSuspension.isRecurMonday())) htmlBody.append("checked=\"checked\"");
        htmlBody.append(">M</label>\n");      
        
        htmlBody.append("<label class=\"checkbox-inline\"><input name=\"RecurTuesday\" id=\"RecurTuesday\" type=\"checkbox\" ");
        if ((alertSuspension == null) || ((alertSuspension.isRecurTuesday() == null) || alertSuspension.isRecurTuesday())) htmlBody.append("checked=\"checked\"");
        htmlBody.append(">T</label>\n");      
     
        htmlBody.append("<label class=\"checkbox-inline\"><input name=\"RecurWednesday\" id=\"RecurWednesday\" type=\"checkbox\" ");
        if ((alertSuspension == null) || ((alertSuspension.isRecurWednesday() == null) || alertSuspension.isRecurWednesday())) htmlBody.append("checked=\"checked\"");
        htmlBody.append(">W</label>\n");   
        
        htmlBody.append("<label class=\"checkbox-inline\"><input name=\"RecurThursday\" id=\"RecurThursday\" type=\"checkbox\" ");
        if ((alertSuspension == null) || ((alertSuspension.isRecurThursday() == null) || alertSuspension.isRecurThursday())) htmlBody.append("checked=\"checked\"");
        htmlBody.append(">T</label>\n");   
        
        htmlBody.append("<label class=\"checkbox-inline\"><input name=\"RecurFriday\" id=\"RecurFriday\" type=\"checkbox\" ");
        if ((alertSuspension == null) || ((alertSuspension.isRecurFriday() == null) || alertSuspension.isRecurFriday())) htmlBody.append("checked=\"checked\"");
        htmlBody.append(">F</label>\n");   
        
        htmlBody.append("<label class=\"checkbox-inline\"><input name=\"RecurSaturday\" id=\"RecurSaturday\" type=\"checkbox\" ");
        if ((alertSuspension == null) || ((alertSuspension.isRecurSaturday() == null) || alertSuspension.isRecurSaturday())) htmlBody.append("checked=\"checked\"");
        htmlBody.append(">S</label>\n");   
        
        htmlBody.append("</div>\n" + "</td>\n" + "</tr>\n");
        
        htmlBody.append("<tr><th>&nbsp;</th><td>&nbsp;</td></tr>\n");
        
        
        // start time
        htmlBody.append(
            "<tr>\n" +
            "  <th><div class=\"create-alert-suspension-th\" id=\"CreateAlertSuspension_DateTimePicker_StartTime_Label_Div\">Start Time:</div></th>\n" +
            "    <td>\n" +
            "      <div class=\"input-group\" id=\"CreateAlertSuspension_DateTimePicker_StartTime_Div\" style=\"width:100%;\" > \n" +
            "        <input class=\"form-control-datetime\" name=\"StartTime\" id=\"StartTime\" style=\"width:100%;\" type=\"text\" ");
        
        if ((alertSuspension != null) && (alertSuspension.getStartTime() != null)) {
            String startTimeString = DateAndTime.getFormattedDateAndTime(alertSuspension.getStartTime(), "h:mm a");
            htmlBody.append(" value=\"").append(startTimeString).append("\"");
        }
  
        htmlBody.append("" +
            "> \n" +
            "        <span class=\"input-group-addon\" style=\"font-size:10px;\"><span class=\"glyphicon-calendar glyphicon\"></span></span> \n" +
            "      </div>\n" +
            "    </td>\n" +
            "</tr>\n");
       
        
        // duration
        htmlBody.append(
            "<tr>\n" +
            "  <th><div class=\"create-alert-suspension-th\" id=\"CreateAlertSuspension_Duration_Label_Div\">Duration (mins):</div></th>\n" +
            "  <td>\n" +
            "    <div id=\"CreateAlertSuspension_Duration_Div\">\n" +
            "      <input class=\"form-control-statsagg\" name=\"Duration\" id=\"Duration\" ");
                    
        if ((alertSuspension != null) && (alertSuspension.getDuration() != null)) {
            htmlBody.append("value=\"").append(alertSuspension.getDuration()).append("\"");
        }
        
        htmlBody.append(">\n </div>\n </td>\n </tr>\n");
        
        // end column 3
        htmlBody.append("</tbody>\n </table>\n </div>\n </div>\n </div>\n");
        
      

        // end form & body content
        htmlBody.append(        
            "</div>" +
            "<button type=\"submit\" class=\"btn btn-default btn-primary statsagg_button_no_shadow\">Submit</button>\n" +
            "&nbsp;&nbsp;&nbsp;" +
            "<a href=\"AlertSuspensions\" class=\"btn btn-default\" role=\"button\">Cancel</a>");
        
        htmlBody.append("</form></div></div>");
        
        return htmlBody.toString();
    }
    
    private String parseAndAlterAlertSuspension(HttpServletRequest request) {
        
        if (request == null) {
            return null;
        }
        
        String returnString;
        
        AlertSuspension alertSuspension = getAlertSuspensionFromRequestParameters(request);
        String oldName = request.getParameter("Old_Name");
        
        // insert/update/delete records in the database
        if (alertSuspension != null) {
            AlertSuspensionsLogic alertSuspensionsLogic = new AlertSuspensionsLogic();
            returnString = alertSuspensionsLogic.alterRecordInDatabase(alertSuspension, oldName);
            
            if (alertSuspensionsLogic.getLastAlterRecordStatus() == AlertSuspensionsLogic.STATUS_CODE_SUCCESS) {
                logger.info("Running alert suspension routine");
                com.pearson.statsagg.alerts.AlertSuspensions alertSuspensions = new com.pearson.statsagg.alerts.AlertSuspensions();
                alertSuspensions.runAlertSuspensionRoutine();
            }
        }
        else {
            returnString = "Failed to add alert suspension. Reason=\"Field validation failed.\"";
            logger.warn(returnString);
        }
        
        return returnString;
    }
    
    private AlertSuspension getAlertSuspensionFromRequestParameters(HttpServletRequest request) {
        
        if (request == null) {
            return null;
        }
        
        boolean didEncounterError = false;
        
        AlertSuspension alertSuspension = new AlertSuspension();

        try {
            String parameter;

            // column #1 parameters
            parameter = request.getParameter("Name");
            String trimmedName = parameter.trim();
            alertSuspension.setName(trimmedName);
            alertSuspension.setUppercaseName(trimmedName.toUpperCase());
            if ((alertSuspension.getName() == null) || alertSuspension.getName().isEmpty()) didEncounterError = true;
            
            parameter = request.getParameter("Enabled");
            if ((parameter != null) && parameter.contains("on")) alertSuspension.setIsEnabled(true);
            else alertSuspension.setIsEnabled(false);

            parameter = request.getParameter("SuspendNotificationOnly");
            if ((parameter != null) && parameter.contains("on")) alertSuspension.setIsSuspendNotificationOnly(true);
            else alertSuspension.setIsSuspendNotificationOnly(false);
 
            
            // column #2 parameters
            parameter = request.getParameter("CreateAlertSuspension_SuspendBy");
            if ((parameter != null) && parameter.contains("AlertName")) alertSuspension.setSuspendBy(AlertSuspension.SUSPEND_BY_ALERT_ID);
            else if ((parameter != null) && parameter.contains("Tags")) alertSuspension.setSuspendBy(AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS);
            else if ((parameter != null) && parameter.contains("Everything")) alertSuspension.setSuspendBy(AlertSuspension.SUSPEND_BY_EVERYTHING);
            
            parameter = request.getParameter("AlertName");
            AlertsDao alertsDao = new AlertsDao();
            Alert alert = alertsDao.getAlertByName(parameter);
            if (alert != null) alertSuspension.setAlertId(alert.getId());

            parameter = request.getParameter("MetricGroupTagsInclusive");
            if (parameter != null) {
                String trimmedTags = AlertSuspension.trimNewLineDelimitedTags(parameter);
                alertSuspension.setMetricGroupTagsInclusive(trimmedTags);
            }
            
            parameter = request.getParameter("MetricGroupTagsExclusive");
            if (parameter != null) {
                String trimmedTags = AlertSuspension.trimNewLineDelimitedTags(parameter);
                alertSuspension.setMetricGroupTagsExclusive(trimmedTags);
            }
            
            
            // column #3 parameters
            parameter = request.getParameter("CreateAlertSuspension_Type");
            if ((parameter != null) && parameter.contains("Recurring")) alertSuspension.setIsOneTime(false);
            else if ((parameter != null) && parameter.contains("OneTime")) alertSuspension.setIsOneTime(true);
            
            parameter = request.getParameter("StartDate");
            if (parameter != null) {
                String startDateStringTrimmed = parameter.trim();
                Calendar startDateCalendar = DateAndTime.getCalendarFromFormattedString(startDateStringTrimmed, "MM/dd/yyyy");
                Timestamp startDateTimestamp = new Timestamp(startDateCalendar.getTimeInMillis());
                alertSuspension.setStartDate(startDateTimestamp);
            }
            
            parameter = request.getParameter("RecurSunday");
            if ((parameter != null) && parameter.contains("on")) alertSuspension.setIsRecurSunday(true);
            else alertSuspension.setIsRecurSunday(false);
            
            parameter = request.getParameter("RecurMonday");
            if ((parameter != null) && parameter.contains("on")) alertSuspension.setIsRecurMonday(true);
            else alertSuspension.setIsRecurMonday(false);
            
            parameter = request.getParameter("RecurTuesday");
            if ((parameter != null) && parameter.contains("on")) alertSuspension.setIsRecurTuesday(true);
            else alertSuspension.setIsRecurTuesday(false);
            
            parameter = request.getParameter("RecurWednesday");
            if ((parameter != null) && parameter.contains("on")) alertSuspension.setIsRecurWednesday(true);
            else alertSuspension.setIsRecurWednesday(false);
            
            parameter = request.getParameter("RecurThursday");
            if ((parameter != null) && parameter.contains("on")) alertSuspension.setIsRecurThursday(true);
            else alertSuspension.setIsRecurThursday(false);

            parameter = request.getParameter("RecurFriday");
            if ((parameter != null) && parameter.contains("on")) alertSuspension.setIsRecurFriday(true);
            else alertSuspension.setIsRecurFriday(false);
            
            parameter = request.getParameter("RecurSaturday");
            if ((parameter != null) && parameter.contains("on")) alertSuspension.setIsRecurSaturday(true);
            else alertSuspension.setIsRecurSaturday(false);

            parameter = request.getParameter("StartTime");
            if (parameter != null) {
                String startTimeStringTrimmed = parameter.trim();
                String startTimeStringTrimmedWithDate = "01/01/1970" + " " + startTimeStringTrimmed;
                Calendar startTimeCalendar = DateAndTime.getCalendarFromFormattedString(startTimeStringTrimmedWithDate, "MM/dd/yyyy h:mm a");
                Timestamp startTimeTimestamp = new Timestamp(startTimeCalendar.getTimeInMillis());
                alertSuspension.setStartTime(startTimeTimestamp);
            }
            
            parameter = request.getParameter("Duration");
            if ((parameter != null) && !parameter.isEmpty()) {
                Integer intValue = Integer.parseInt(parameter.trim());
                alertSuspension.setDuration(intValue);
            }
            
            if ((alertSuspension.isOneTime() != null) && alertSuspension.isOneTime() && (alertSuspension.getDuration() != null) &&
                    (alertSuspension.getStartDate() != null) && (alertSuspension.getStartTime() != null)) {
                Calendar startTime = Calendar.getInstance();
                startTime.setTimeInMillis(alertSuspension.getStartTime().getTime());
               
                Calendar startDateAndTime = Calendar.getInstance();
                startDateAndTime.setTimeInMillis(alertSuspension.getStartDate().getTime());
                startDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(startDateAndTime, 
                        startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE), startTime.get(Calendar.SECOND), startTime.get(Calendar.MILLISECOND));
                
                Calendar deleteDateAndTime = Calendar.getInstance();
                deleteDateAndTime.setTimeInMillis(startDateAndTime.getTimeInMillis());
                deleteDateAndTime.add(Calendar.MINUTE, alertSuspension.getDuration());
                
                Timestamp deleteAtTimestamp = new Timestamp(deleteDateAndTime.getTimeInMillis());
                alertSuspension.setDeleteAtTimestamp(deleteAtTimestamp);
            }
        }
        catch (Exception e) {
            didEncounterError = true;
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
            
        if (didEncounterError) alertSuspension = null;
        boolean isValid = AlertSuspension.isValid(alertSuspension);
        if (!isValid) alertSuspension = null;
        
        return alertSuspension;
    }
    
}
