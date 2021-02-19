package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.database_objects.alert_templates.AlertTemplate;
import com.pearson.statsagg.database_objects.alert_templates.AlertTemplatesDao;
import com.pearson.statsagg.database_objects.metric_group_templates.MetricGroupTemplate;
import com.pearson.statsagg.database_objects.metric_group_templates.MetricGroupTemplatesDao;
import com.pearson.statsagg.database_objects.notification_group_templates.NotificationGroupTemplate;
import com.pearson.statsagg.database_objects.notification_group_templates.NotificationGroupTemplatesDao;
import com.pearson.statsagg.database_objects.variable_set_list.VariableSetList;
import com.pearson.statsagg.database_objects.variable_set_list.VariableSetListsDao;
import com.pearson.statsagg.database_objects.variable_set_list.VariableSetListsDaoWrapper;
import com.pearson.statsagg.database_objects.variable_set_list_entry.VariableSetListEntriesDao;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.utilities.core_utils.KeyValue;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.sql.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class VariableSetLists extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(VariableSetLists.class.getName());

    public static final String PAGE_NAME = "Variable Set Lists";
    
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
            String html = buildVariableSetListsHtml();
            
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

            if ((operation != null) && operation.equals("Clone")) {
                Integer id = Integer.parseInt(request.getParameter("Id"));
                cloneVariableSetList(id);
            }

            if ((operation != null) && operation.equals("Remove")) {
                Integer id = Integer.parseInt(Common.getSingleParameterAsString(request, "Id"));
                removeVariableSetList(id);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        StatsAggHtmlFramework.redirectAndGet(response, 303, "VariableSetLists");
    }
    
    private void cloneVariableSetList(Integer variableSetListId) {
        
        if (variableSetListId == null) {
            return;
        }
        
        Connection connection = null;
        
        try {
            connection = DatabaseConnections.getConnection();
            
            VariableSetList variableSetList = VariableSetListsDao.getVariableSetList(connection, false, variableSetListId);
            Set<String> allVariableSetListNames = VariableSetListsDao.getVariableSetListNames(connection, false);
            List<Integer> variableSetIds = VariableSetListEntriesDao.getVariableSetIds_ForVariableSetListId(connection, false, variableSetListId);
            DatabaseUtils.cleanup(connection);

            if ((variableSetList != null) && (variableSetList.getName() != null)) {
                VariableSetList clonedVariableSetList = VariableSetList.copy(variableSetList);
                clonedVariableSetList.setId(-1);
                String clonedAlterName = StatsAggHtmlFramework.createCloneName(variableSetList.getName(), allVariableSetListNames);
                clonedVariableSetList.setName(clonedAlterName);

                VariableSetListsDaoWrapper.createRecordInDatabase(clonedVariableSetList, variableSetIds);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {
            DatabaseUtils.cleanup(connection);
        }
        
    }
    
    public String removeVariableSetList(Integer variableSetListId) {
        
        String returnString = "Variable Set List ID field can't be null.";
        if (variableSetListId == null) return returnString;
        
        try {
            VariableSetList variableSetList = VariableSetListsDao.getVariableSetList(DatabaseConnections.getConnection(), true, variableSetListId);   
            returnString = VariableSetListsDaoWrapper.deleteRecordInDatabase(variableSetList).getReturnString();
            return returnString;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            returnString = "Error removing variable set list";
            return returnString;
        }

    }
    
    private String buildVariableSetListsHtml() {
        
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
            "     <a href=\"CreateVariableSetList\" class=\"btn btn-primary statsagg_page_content_font\">Create New Variable Set List <i class=\"fa fa-long-arrow-right\"></i></a> \n" +
            "    </div>" +   
            "  </div>" +    
            "  <table id=\"VariableSetListsTable\" style=\"display:none\" class=\"table table-bordered table-hover \">\n" +
            "    <thead>\n" +
            "      <tr>\n" +
            "        <th>Variable Set List Name</th>\n" +
            "        <th>Operations</th>\n" +
            "      </tr>\n" +
            "    </thead>\n" +
            "    <tbody>\n");

        Connection connection = null;

        try {
            connection = DatabaseConnections.getConnection();
            List<AlertTemplate> alertTemplates = AlertTemplatesDao.getAlertTemplates(connection, false);
            if (alertTemplates == null) alertTemplates = new ArrayList<>();
            List<MetricGroupTemplate> metricGroupTemplates = MetricGroupTemplatesDao.getMetricGroupTemplates(connection, false);
            if (metricGroupTemplates == null) metricGroupTemplates = new ArrayList<>();
            List<NotificationGroupTemplate> notificationGroupTemplates = NotificationGroupTemplatesDao.getNotificationGroupTemplates(connection, false);
            if (notificationGroupTemplates == null) notificationGroupTemplates = new ArrayList<>();
            List<VariableSetList> variableSetLists = VariableSetListsDao.getVariableSetLists(connection, false);
            if (variableSetLists == null) variableSetLists = new ArrayList<>();
            DatabaseUtils.cleanup(connection);

            Set<Integer> variableSetListIdsAssociatedWithTemplates = new HashSet<>();
            
            for (AlertTemplate alertTemplate : alertTemplates) {
                if ((alertTemplate != null) && (alertTemplate.getVariableSetListId() != null)) {
                    variableSetListIdsAssociatedWithTemplates.add(alertTemplate.getVariableSetListId());
                }
            }
            
            for (MetricGroupTemplate metricGroupTemplate : metricGroupTemplates) {
                if ((metricGroupTemplate != null) && (metricGroupTemplate.getVariableSetListId() != null)) {
                    variableSetListIdsAssociatedWithTemplates.add(metricGroupTemplate.getVariableSetListId());
                }
            }
            
            for (NotificationGroupTemplate notificationGroupTemplate : notificationGroupTemplates) {
                if ((notificationGroupTemplate != null) && (notificationGroupTemplate.getVariableSetListId() != null)) {
                    variableSetListIdsAssociatedWithTemplates.add(notificationGroupTemplate.getVariableSetListId());
                }
            }
            
            for (VariableSetList variableSetList : variableSetLists) {     
                if ((variableSetList == null) || (variableSetList.getId() == null) || (variableSetList.getName() == null)) continue;

                String variableSetListDetails = "<a class=\"iframe cboxElement\" href=\"VariableSetListDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(variableSetList.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(variableSetList.getName()) + "</a>";

                String alter = "<a href=\"CreateVariableSetList?Operation=Alter&amp;Name=" + StatsAggHtmlFramework.urlEncode(variableSetList.getName()) + "\">alter</a>";

                List<KeyValue<String,String>> cloneKeysAndValues = new ArrayList<>();
                cloneKeysAndValues.add(new KeyValue("Operation", "Clone"));
                cloneKeysAndValues.add(new KeyValue("Id", variableSetList.getId().toString()));
                String clone = StatsAggHtmlFramework.buildJavaScriptPostLink("Clone_" + variableSetList.getName(), "VariableSetLists", "clone", cloneKeysAndValues);

                List<KeyValue<String,String>> removeKeysAndValues = new ArrayList<>();
                removeKeysAndValues.add(new KeyValue("Operation", "Remove"));
                removeKeysAndValues.add(new KeyValue("Id", variableSetList.getId().toString()));
                String remove = StatsAggHtmlFramework.buildJavaScriptPostLink("Remove_" + variableSetList.getName(), "VariableSetLists", "remove", 
                        removeKeysAndValues, true, "Are you sure you want to remove this variable set list?");       

                htmlBodyStringBuilder.append("<tr>\n")
                    .append("<td class=\"statsagg_force_word_break\">").append(variableSetListDetails).append("</td>\n")
                    .append("<td>").append(alter).append(", ").append(clone);

                if (!variableSetListIdsAssociatedWithTemplates.contains(variableSetList.getId())) htmlBodyStringBuilder.append(", ").append(remove);

                htmlBodyStringBuilder.append("</td>\n").append("</tr>\n");
            }

            htmlBodyStringBuilder.append(""
                    + "</tbody>\n"
                    + "<tfoot> \n"
                    + "  <tr>\n" 
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
