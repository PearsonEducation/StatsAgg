package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import com.pearson.statsagg.database_objects.alert_templates.AlertTemplate;
import com.pearson.statsagg.database_objects.alert_templates.AlertTemplatesDao;
import com.pearson.statsagg.database_objects.variable_set_list.VariableSetList;
import com.pearson.statsagg.database_objects.variable_set_list.VariableSetListsDao;
import com.pearson.statsagg.globals.DatabaseConnections;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import java.math.BigDecimal;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class AlertTemplateDetails extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AlertTemplateDetails.class.getName());
    
    public static final String PAGE_NAME = "Alert Template Details";
    
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
    
        String alertTemplateName = request.getParameter("Name");
        boolean excludeNavbar = StringUtilities.isStringValueBooleanTrue(request.getParameter("ExcludeNavbar"));
        String alertTemplateDetails = getAlertTemplateDetailsString(alertTemplateName);
                
        try {  
            StringBuilder htmlBuilder = new StringBuilder();

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            
            String htmlBody = statsAggHtmlFramework.createHtmlBody(
            "<div id=\"page-content-wrapper\">\n" +
            "  <!-- Keep all page content within the page-content inset div! -->\n" +
            "  <div class=\"page-content inset statsagg_page_content_font\">\n" +
            "    <div class=\"content-header\"> \n" +
            "      <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "    </div>\n " +
            "    <div class=\"row create-alert-form-row\">\n" +
            alertTemplateDetails +
            "  </div></div>\n" +
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
    
    protected static String getAlertTemplateDetailsString(String alertTemplateName) {
        
        if (alertTemplateName == null) return "<div class=\"col-md-4\"><b>No alert template specified</b></div>";   
        
        AlertTemplate alertTemplate = AlertTemplatesDao.getAlertTemplate(DatabaseConnections.getConnection(), true, alertTemplateName);
        
        if (alertTemplate == null) {
            return "<div class=\"col-md-4\"><b>Alert template not found</b></div>";
        }

        else {
            StringBuilder outputString = new StringBuilder();

            VariableSetList variableSetList = null;
            if (alertTemplate.getVariableSetListId() != null) variableSetList = VariableSetListsDao.getVariableSetList(DatabaseConnections.getConnection(), true, alertTemplate.getVariableSetListId());

            // core panel
            outputString.append("<div class=\"col-md-4 statsagg_three_panel_first_panel\">\n");
            outputString.append("<div class=\"panel panel-default\"> <div class=\"panel-heading\"><b>Core Details</b></div> <div class=\"panel-body statsagg_force_word_wrap\">");
            
            outputString.append("<b>Name</b> = ").append(StatsAggHtmlFramework.htmlEncode(alertTemplate.getName())).append("<br>");
            
            outputString.append("<b>ID</b> = ").append(alertTemplate.getId()).append("<br>");
            
            outputString.append("<b>Variable set list</b> = ");
            if (variableSetList != null) {
                String variableSetListDetailsPopup = "<a class=\"iframe cboxElement\" href=\"VariableSetListDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(variableSetList.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(variableSetList.getName()) + "</a>";
                outputString.append(variableSetListDetailsPopup).append("<br>");
            }
            else outputString.append("<br>");
            
            outputString.append("<br>");

            outputString.append("<b>Description</b> = ");
            if (alertTemplate.getDescriptionVariable() != null) {
                String encodedAlertDescription = StatsAggHtmlFramework.htmlEncode(alertTemplate.getDescriptionVariable());
                outputString.append(encodedAlertDescription.replaceAll("\n", "<br>")).append("<br><br>");
            }
            else outputString.append("<br><br>");

            outputString.append("<b>Alert name variable</b> = ").append(StatsAggHtmlFramework.htmlEncode(alertTemplate.getAlertNameVariable())).append("<br>");
            
            outputString.append("<b>Metric group name variable</b> = ").append(StatsAggHtmlFramework.htmlEncode(alertTemplate.getMetricGroupNameVariable())).append("<br>");

            outputString.append("<br>");
            
            String isEnabled = "No";
            if ((alertTemplate.isEnabled() != null) && alertTemplate.isEnabled()) isEnabled = "Yes";
            outputString.append("<b>Is alert enabled?</b> = ").append(isEnabled).append("<br>");
            
            String isCautionEnabled = "No";
            if ((alertTemplate.isCautionEnabled() != null) && alertTemplate.isCautionEnabled()) isCautionEnabled = "Yes";
            outputString.append("<b>Is caution alerting enabled?</b> = ").append(isCautionEnabled).append("<br>");
            
            String isDangerEnabled = "No";
            if ((alertTemplate.isDangerEnabled() != null) && alertTemplate.isDangerEnabled()) isDangerEnabled = "Yes";
            outputString.append("<b>Is danger alerting enabled?</b> = ").append(isDangerEnabled).append("<br>");
            
            outputString.append("<b>Alert type</b> = ");
            if (alertTemplate.getAlertType() != null) {
                if (alertTemplate.getAlertType() == AlertTemplate.TYPE_AVAILABILITY) outputString.append("Availability").append("<br>");
                else if (alertTemplate.getAlertType() == AlertTemplate.TYPE_THRESHOLD) outputString.append("Threshold").append("<br>");
                else outputString.append("N/A").append("<br>");
            }
            else outputString.append("N/A <br>");
            
            String isAlertOnPositive = "No";
            if ((alertTemplate.isAlertOnPositive() != null) && alertTemplate.isAlertOnPositive()) isAlertOnPositive = "Yes";
            outputString.append("<b>Alert on positive?</b> = ").append(isAlertOnPositive).append("<br>");
            
            String isAllowResendAlert = "No";
            if ((alertTemplate.isAllowResendAlert() != null) && alertTemplate.isAllowResendAlert()) isAllowResendAlert = "Yes";
            outputString.append("<b>Resend Alert?</b> = ").append(isAllowResendAlert).append("<br>");

            outputString.append("<b>Resend alert every</b> = ");
            if (alertTemplate.getResendAlertEvery() != null) {
                BigDecimal sentAlertEvery = DatabaseObjectCommon.getValueForTimeFromMilliseconds(alertTemplate.getResendAlertEvery(), alertTemplate.getResendAlertEveryTimeUnit());
                if (sentAlertEvery != null) outputString.append(sentAlertEvery.stripTrailingZeros().toPlainString());
                
                if (alertTemplate.getResendAlertEveryTimeUnit() != null) {
                    String timeUnitString = DatabaseObjectCommon.getTimeUnitStringFromCode(alertTemplate.getResendAlertEveryTimeUnit(), true);
                    if (timeUnitString != null) outputString.append(" ").append(timeUnitString);
                }
                outputString.append("<br>");
            }
            else outputString.append("N/A <br>");
            
            outputString.append("<br>");
            
            String isCoreAlertTemplateCriteriaValid = "No";
            if (alertTemplate.isCoreAlertCriteriaValid().isValid() && alertTemplate.isAlertTemplateCriteriaValid().isValid()) isCoreAlertTemplateCriteriaValid = "Yes";
            outputString.append("<b>Is the core criteria valid?</b> = ").append(isCoreAlertTemplateCriteriaValid).append("<br>");
            
            outputString.append("</div></div></div>").append("<div class=\"col-md-4 statsagg_three_panel_second_panel\">\n");
            
            
            // caution panel
            outputString.append("<div class=\"panel panel-warning\"> <div class=\"panel-heading\"><b>Caution Details</b></div> <div class=\"panel-body statsagg_force_word_wrap\">");
                        
            if ((alertTemplate.isCautionEnabled() != null) && !alertTemplate.isCautionEnabled()) outputString.append("<del>");
            
            outputString.append("<b>Caution notification group name variable</b> = ").append(StatsAggHtmlFramework.htmlEncode(alertTemplate.getCautionNotificationGroupNameVariable())).append("<br>");

            outputString.append("<b>Caution positive notification group name variable</b> = ").append(StatsAggHtmlFramework.htmlEncode(alertTemplate.getCautionPositiveNotificationGroupNameVariable())).append("<br>");
            
            outputString.append("<br>");
            
            outputString.append("<b>Caution window duration</b> = ");
            if (alertTemplate.getCautionWindowDuration() != null) {
                BigDecimal cautionWindowDuration = DatabaseObjectCommon.getValueForTimeFromMilliseconds(alertTemplate.getCautionWindowDuration(), alertTemplate.getCautionWindowDurationTimeUnit());
                if (cautionWindowDuration != null) outputString.append(cautionWindowDuration.stripTrailingZeros().toPlainString());
                
                if (alertTemplate.getCautionWindowDurationTimeUnit() != null) {
                    String timeUnitString = DatabaseObjectCommon.getTimeUnitStringFromCode(alertTemplate.getCautionWindowDurationTimeUnit(), true);
                    if (timeUnitString != null) outputString.append(" ").append(timeUnitString);
                }
                outputString.append("<br>");
            }
            else outputString.append("N/A <br>");
            
            if ((alertTemplate.getAlertType() != null) && (alertTemplate.getAlertType() == AlertTemplate.TYPE_AVAILABILITY)) {
                outputString.append("<b>Caution stop tracking after...</b> = ");
                if (alertTemplate.getCautionStopTrackingAfter() != null) {
                    BigDecimal cautionStopTrackingAfter = DatabaseObjectCommon.getValueForTimeFromMilliseconds(alertTemplate.getCautionStopTrackingAfter(), alertTemplate.getCautionStopTrackingAfterTimeUnit());
                    if (cautionStopTrackingAfter != null) outputString.append(cautionStopTrackingAfter.stripTrailingZeros().toPlainString());
                    
                    if (alertTemplate.getCautionStopTrackingAfterTimeUnit() != null) {
                        String timeUnitString = DatabaseObjectCommon.getTimeUnitStringFromCode(alertTemplate.getCautionStopTrackingAfterTimeUnit(), true);
                        if (timeUnitString != null) outputString.append(" ").append(timeUnitString);
                    }
                    outputString.append("<br>");
                }
                else outputString.append("N/A <br>");
            }          
            
            if ((alertTemplate.getAlertType() != null) && (alertTemplate.getAlertType() == AlertTemplate.TYPE_THRESHOLD)) {
                outputString.append("<b>Caution minimum sample count</b> = ");
                if (alertTemplate.getCautionMinimumSampleCount() != null) outputString.append(alertTemplate.getCautionMinimumSampleCount()).append("<br>");
                else outputString.append("N/A <br>");

                outputString.append("<b>Caution operator</b> = ");
                if (alertTemplate.getOperatorString(AlertTemplate.CAUTION, true, true) != null) outputString.append("'").append(alertTemplate.getOperatorString(AlertTemplate.CAUTION, true, true)).append("'<br>");
                else outputString.append("N/A <br>");

                outputString.append("<b>Caution combination</b> = ");
                if (alertTemplate.getCombinationString(AlertTemplate.CAUTION) != null) outputString.append(alertTemplate.getCombinationString(AlertTemplate.CAUTION)).append("<br>");
                else outputString.append("N/A <br>");

                outputString.append("<b>Caution combination count</b> = ");
                if (alertTemplate.getCautionCombinationCount() != null) outputString.append(alertTemplate.getCautionCombinationCount()).append("<br>");
                else outputString.append("N/A <br>");

                outputString.append("<b>Caution threshold</b> = ");
                if (alertTemplate.getCautionThreshold() != null) outputString.append(alertTemplate.getCautionThreshold().stripTrailingZeros().toPlainString()).append("<br>");
                else outputString.append("N/A <br>");
            }
            
            outputString.append("<br>");
            
            String isCautionAlertCriteriaValid = "No";
            if (alertTemplate.isCautionAlertCriteriaValid().isValid()) isCautionAlertCriteriaValid = "Yes";
            outputString.append("<b>Is the caution criteria valid?</b> = ").append(isCautionAlertCriteriaValid).append("<br>");

            if ((alertTemplate.isCautionEnabled() != null) && !alertTemplate.isCautionEnabled()) outputString.append("</del>");

            outputString.append("</div></div></div>").append("<div class=\"col-md-4 statsagg_three_panel_third_panel\">\n");
            
            
            // danger panel
            outputString.append("<div class=\"panel panel-danger\"> <div class=\"panel-heading\"><b>Danger Details</b></div> <div class=\"panel-body statsagg_force_word_wrap\">");

            if ((alertTemplate.isDangerEnabled() != null) && !alertTemplate.isDangerEnabled()) outputString.append("<del>");
            
            outputString.append("<b>Danger notification group name variable</b> = ").append(StatsAggHtmlFramework.htmlEncode(alertTemplate.getDangerNotificationGroupNameVariable())).append("<br>");

            outputString.append("<b>Danger positive notification group name variable</b> = ").append(StatsAggHtmlFramework.htmlEncode(alertTemplate.getDangerPositiveNotificationGroupNameVariable())).append("<br>");
            
            outputString.append("<br>");
            
            outputString.append("<b>Danger window duration</b> = ");
            if (alertTemplate.getDangerWindowDuration() != null) {
                BigDecimal dangerWindowDuration = DatabaseObjectCommon.getValueForTimeFromMilliseconds(alertTemplate.getDangerWindowDuration(), alertTemplate.getDangerWindowDurationTimeUnit());
                if (dangerWindowDuration != null) outputString.append(dangerWindowDuration.stripTrailingZeros().toPlainString());
                
                if (alertTemplate.getDangerWindowDurationTimeUnit() != null) {
                    String timeUnitString = DatabaseObjectCommon.getTimeUnitStringFromCode(alertTemplate.getDangerWindowDurationTimeUnit(), true);
                    if (timeUnitString != null) outputString.append(" ").append(timeUnitString);
                }
                outputString.append("<br>");
            }
            else outputString.append("N/A <br>");
            
            if ((alertTemplate.getAlertType() != null) && (alertTemplate.getAlertType() == AlertTemplate.TYPE_AVAILABILITY)) {
                outputString.append("<b>Danger stop tracking after...</b> = ");
                if (alertTemplate.getDangerStopTrackingAfter() != null) {
                    BigDecimal dangerStopTrackingAfter = DatabaseObjectCommon.getValueForTimeFromMilliseconds(alertTemplate.getDangerStopTrackingAfter(), alertTemplate.getDangerStopTrackingAfterTimeUnit());
                    if (dangerStopTrackingAfter != null) outputString.append(dangerStopTrackingAfter.stripTrailingZeros().toPlainString());
                    
                    if (alertTemplate.getDangerStopTrackingAfterTimeUnit() != null) {
                        String timeUnitString = DatabaseObjectCommon.getTimeUnitStringFromCode(alertTemplate.getDangerStopTrackingAfterTimeUnit(), true);
                        if (timeUnitString != null) outputString.append(" ").append(timeUnitString);
                    }
                    outputString.append("<br>");
                }
                else outputString.append("N/A <br>");
            }
            
            if ((alertTemplate.getAlertType() != null) && (alertTemplate.getAlertType() == AlertTemplate.TYPE_THRESHOLD)) {
                outputString.append("<b>Danger minimum sample count</b> = ");
                if (alertTemplate.getDangerMinimumSampleCount() != null) outputString.append(alertTemplate.getDangerMinimumSampleCount()).append("<br>");
                else outputString.append("N/A <br>");

                outputString.append("<b>Danger operator</b> = ");
                if (alertTemplate.getOperatorString(AlertTemplate.DANGER, true, true) != null) outputString.append("'").append(alertTemplate.getOperatorString(AlertTemplate.DANGER, true, true)).append("'<br>");
                else outputString.append("N/A <br>");

                outputString.append("<b>Danger combination</b> = ");
                if (alertTemplate.getCombinationString(AlertTemplate.DANGER) != null) outputString.append(alertTemplate.getCombinationString(AlertTemplate.DANGER)).append("<br>");
                else outputString.append("N/A <br>");

                outputString.append("<b>Danger combination count</b> = ");
                if (alertTemplate.getDangerCombinationCount() != null) outputString.append(alertTemplate.getDangerCombinationCount()).append("<br>");
                else outputString.append("N/A <br>");

                outputString.append("<b>Danger threshold</b> = ");
                if (alertTemplate.getDangerThreshold() != null) outputString.append(alertTemplate.getDangerThreshold().stripTrailingZeros().toPlainString()).append("<br>");
                else outputString.append("N/A <br>");
            }
            
            outputString.append("<br>");

            String isDangerAlertCriteriaValid = "No";
            if (alertTemplate.isDangerAlertCriteriaValid().isValid()) isDangerAlertCriteriaValid = "Yes";
            outputString.append("<b>Is the danger criteria valid?</b> = ").append(isDangerAlertCriteriaValid).append("<br>");
            
            if ((alertTemplate.isDangerEnabled() != null) && !alertTemplate.isDangerEnabled()) outputString.append("</del>");
            
            outputString.append("</div></div></div>");
            
            return outputString.toString();
        }
    }

}
