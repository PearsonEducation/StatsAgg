package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.database_objects.metric_group_templates.MetricGroupTemplate;
import com.pearson.statsagg.database_objects.metric_group_templates.MetricGroupTemplatesDao;
import com.pearson.statsagg.database_objects.metric_group_templates.MetricGroupTemplatesDaoWrapper;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroup;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupsDao;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.core_utils.KeyValue;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.core_utils.Threads;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class MetricGroupTemplates extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(MetricGroupTemplates.class.getName());
    
    public static final String PAGE_NAME = "Metric Group Templates";
    
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
            String html = buildMetricGroupTemplatesHtml();
            
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
            String operation = Common.getSingleParameterAsString(request, "Operation");

            if ((operation != null) && operation.equals("Clone")) {
                Integer metricGroupTemplateId = Integer.parseInt(request.getParameter("Id"));
                MetricGroupTemplates.cloneMetricGroupTemplate(metricGroupTemplateId);
            }

            if ((operation != null) && operation.equals("Remove")) {
                Integer metricGroupTemplateId = Integer.parseInt(Common.getSingleParameterAsString(request, "Id"));
                MetricGroupTemplates.removeMetricGroupTemplate(metricGroupTemplateId);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        StatsAggHtmlFramework.redirectAndGet(response, 303, "MetricGroupTemplates");
    }
    
    protected static void cloneMetricGroupTemplate(Integer metricGroupTemplateId) {
        
        if (metricGroupTemplateId == null) {
            return;
        }
        
        Connection connection = null;
        
        try {
            connection = DatabaseConnections.getConnection(); 
            MetricGroupTemplate metricGroupTemplate = MetricGroupTemplatesDao.getMetricGroupTemplate(connection, false, metricGroupTemplateId);

            if ((metricGroupTemplate != null) && (metricGroupTemplate.getName() != null)) {
                Set<String> allMetricGroupTemplateNames = MetricGroupTemplatesDao.getMetricGroupTemplateNames(connection, true);

                MetricGroupTemplate clonedMetricGroupTemplate = MetricGroupTemplate.copy(metricGroupTemplate);
                clonedMetricGroupTemplate.setId(-1);
                String clonedMetricGroupTemplateName = StatsAggHtmlFramework.createCloneName(metricGroupTemplate.getName(), allMetricGroupTemplateNames);

                clonedMetricGroupTemplate.setName(clonedMetricGroupTemplateName);
                
                MetricGroupTemplatesDaoWrapper metricGroupTemplatesDaoWrapper = MetricGroupTemplatesDaoWrapper.createRecordInDatabase(clonedMetricGroupTemplate);
                
                if ((GlobalVariables.templateInvokerThread != null) && (MetricGroupTemplatesDaoWrapper.STATUS_CODE_SUCCESS == metricGroupTemplatesDaoWrapper.getLastAlterRecordStatus())) {
                    logger.info("Running metric group template routine due to metric group template clone operation");
                    GlobalVariables.templateInvokerThread.runTemplateThread();
                }
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public static String removeMetricGroupTemplate(Integer metricGroupTemplateId) {
        
        if (metricGroupTemplateId == null) {
            return null;
        }
        
        String returnString = null;
        
        try {
            MetricGroupTemplate metricGroupTemplate = MetricGroupTemplatesDao.getMetricGroupTemplate(DatabaseConnections.getConnection(), true, metricGroupTemplateId);
            if (metricGroupTemplate != null) metricGroupTemplate.setIsMarkedForDelete(true);
            
            MetricGroupTemplatesDaoWrapper metricGroupTemplatesDaoWrapper = MetricGroupTemplatesDaoWrapper.alterRecordInDatabase(metricGroupTemplate);
            
            if ((GlobalVariables.templateInvokerThread != null) && (MetricGroupTemplatesDaoWrapper.STATUS_CODE_SUCCESS == metricGroupTemplatesDaoWrapper.getLastAlterRecordStatus())) {
                logger.info("Running metric group template routine due to metric group template remove operation");
                GlobalVariables.templateInvokerThread.runTemplateThread();
                Threads.sleepMilliseconds(300); // sleep for 300ms to give the template thread a change to run before re-rendering the page
            }
            
            returnString = metricGroupTemplatesDaoWrapper.getReturnString();
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnString;
    }
    
    public static String buildMetricGroupTemplatesHtml() {
        
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
            "     <a href=\"CreateMetricGroupTemplate\" class=\"btn btn-primary statsagg_page_content_font\">Create New Metric Group Template <i class=\"fa fa-long-arrow-right\"></i></a> \n" +
            "    </div>\n" + 
            "  </div>\n" +   
            "  <table id=\"MetricGroupTemplatesTable\" style=\"display:none\" class=\"table table-bordered table-hover compact\">\n" +
            "     <thead>\n" +
            "       <tr>\n" +
            "         <th>Metric Group Template Name</th>\n" +
            "         <th>Derived Metric Groups</th>\n" +
            "         <th>Operations</th>\n" +
            "       </tr>\n" +
            "     </thead>\n" +
            "     <tbody>\n");

        Connection connection = null;
        
        try {
            connection = DatabaseConnections.getConnection();
            List<MetricGroupTemplate> metricGroupTemplates = MetricGroupTemplatesDao.getMetricGroupTemplates(connection, false);
            if (metricGroupTemplates == null) metricGroupTemplates = new ArrayList<>();
            DatabaseUtils.cleanup(connection);

            for (MetricGroupTemplate metricGroupTemplate : metricGroupTemplates) {
                if (metricGroupTemplate == null) continue;

                List<MetricGroup> metricGroups = MetricGroupsDao.getMetricGroups_FilterByMetricGroupTemplateId(DatabaseConnections.getConnection(), true, metricGroupTemplate.getId());
                Set<String> metricGroupNamesThatMetricGroupTemplateWantsToCreate = com.pearson.statsagg.threads.template_related.Common.getNamesThatTemplateWantsToCreate(metricGroupTemplate.getVariableSetListId(), metricGroupTemplate.getMetricGroupNameVariable());
                String numberOfDerivedMetricGroups = (metricGroups == null) ? "0" : (metricGroups.size() + "");

                String rowStatusContext = "";
                if ((metricGroupNamesThatMetricGroupTemplateWantsToCreate != null) && (metricGroups != null) && (metricGroups.size() != metricGroupNamesThatMetricGroupTemplateWantsToCreate.size())) {
                    rowStatusContext = "class=\"danger\"";
                }

                String metricGroupTemplateDetails = "<a class=\"iframe cboxElement\" href=\"MetricGroupTemplateDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(metricGroupTemplate.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(metricGroupTemplate.getName()) + "</a>";

                String derivedMetricGroupDetails = "<a class=\"iframe cboxElement\" href=\"MetricGroupTemplate-DerivedMetricGroups?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(metricGroupTemplate.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(numberOfDerivedMetricGroups) + "</a>";

                String alter = "<a href=\"CreateMetricGroupTemplate?Operation=Alter&amp;Name=" + StatsAggHtmlFramework.urlEncode(metricGroupTemplate.getName()) + "\">alter</a>";

                List<KeyValue<String,String>> cloneKeysAndValues = new ArrayList<>();
                cloneKeysAndValues.add(new KeyValue("Operation", "Clone"));
                cloneKeysAndValues.add(new KeyValue("Id", metricGroupTemplate.getId().toString()));
                String clone = StatsAggHtmlFramework.buildJavaScriptPostLink("Clone_" + metricGroupTemplate.getName(), "MetricGroupTemplates", "clone", cloneKeysAndValues);

                List<KeyValue<String,String>> removeKeysAndValues = new ArrayList<>();
                removeKeysAndValues.add(new KeyValue("Operation", "Remove"));
                removeKeysAndValues.add(new KeyValue("Id", metricGroupTemplate.getId().toString()));
                String remove = StatsAggHtmlFramework.buildJavaScriptPostLink("Remove_" + metricGroupTemplate.getName(), "MetricGroupTemplates", "remove", 
                        removeKeysAndValues, true, "Are you sure you want to remove this metric group template?");

                htmlBodyStringBuilder
                        .append("<tr ").append(rowStatusContext).append(">\n")
                        .append("<td class=\"statsagg_force_word_break\">").append(metricGroupTemplateDetails).append("</td>\n")
                        .append("<td class=\"statsagg_force_word_break\">").append(derivedMetricGroupDetails).append("</td>\n")
                        .append("<td>").append(alter).append(", ").append(clone).append(", ").append(remove);

                htmlBodyStringBuilder.append("</td>\n").append("</tr>\n");
            }

            htmlBodyStringBuilder.append(""
                    + "</tbody>\n"
                    + "<tfoot> \n"
                    + "  <tr>\n" 
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
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return "Fatal error encountered";
        }
        finally {
            DatabaseUtils.cleanup(connection);
        }
        
    }

}
