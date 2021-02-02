package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.database_objects.notification_groups.NotificationGroupsDao;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.database_objects.pagerduty_services.PagerdutyService;
import com.pearson.statsagg.database_objects.pagerduty_services.PagerdutyServicesDao;
import com.pearson.statsagg.database_objects.pagerduty_services.PagerdutyServicesDaoWrapper;
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
public class PagerDutyServices extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(PagerDutyServices.class.getName());

    public static final String PAGE_NAME = "PagerDuty Services";
    
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
            String html = buildPagerdutyServicesHtml();
            
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
                clonePagerdutyService(id);
            }

            if ((operation != null) && operation.equals("Remove")) {
                Integer id = Integer.parseInt(Common.getSingleParameterAsString(request, "Id"));
                removePagerdutyService(id);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        StatsAggHtmlFramework.redirectAndGet(response, 303, "PagerDutyServices");
    }
    
    private void clonePagerdutyService(Integer pagerdutyServiceId) {
        
        if (pagerdutyServiceId == null) {
            return;
        }
        
        try {
            Connection connection = DatabaseConnections.getConnection();
            PagerdutyService pagerdutyService = PagerdutyServicesDao.getPagerdutyService(connection, false, pagerdutyServiceId);
            List<PagerdutyService> allPagerdutyServices = PagerdutyServicesDao.getPagerdutyServices(connection, false);
            DatabaseUtils.cleanup(connection);

            if ((pagerdutyService != null) && (pagerdutyService.getName() != null)) {
                Set<String> allPagerdutyServiceNames = new HashSet<>();
                for (PagerdutyService currentPagerdutyService : allPagerdutyServices) {
                    if (currentPagerdutyService.getName() != null) allPagerdutyServiceNames.add(currentPagerdutyService.getName());
                }

                PagerdutyService clonedPagerdutyService = PagerdutyService.copy(pagerdutyService);
                clonedPagerdutyService.setId(-1);
                String clonedAlterName = StatsAggHtmlFramework.createCloneName(pagerdutyService.getName(), allPagerdutyServiceNames);
                clonedPagerdutyService.setName(clonedAlterName);

                PagerdutyServicesDaoWrapper.createRecordInDatabase(clonedPagerdutyService);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
    public String removePagerdutyService(Integer pagerdutyServiceId) {
        
        String returnString = "PagerDuty Service ID field can't be null.";
        if (pagerdutyServiceId == null) return returnString;
        
        try {
            PagerdutyService pagerdutyService = PagerdutyServicesDao.getPagerdutyService(DatabaseConnections.getConnection(), true, pagerdutyServiceId);   
            returnString = PagerdutyServicesDaoWrapper.deleteRecordInDatabase(pagerdutyService).getReturnString();
            return returnString;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            returnString = "Error removing PagerDuty Service";
            return returnString;
        }

    }
    
    private String buildPagerdutyServicesHtml() {
        
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
            "     <a href=\"CreatePagerDutyService\" class=\"btn btn-primary statsagg_page_content_font\">Create New PagerDuty Service <i class=\"fa fa-long-arrow-right\"></i></a> \n" +
            "    </div>" +   
            "  </div>" +    
            "  <table id=\"PagerDutyServicesTable\" style=\"display:none\" class=\"table table-bordered table-hover \">\n" +
            "    <thead>\n" +
            "      <tr>\n" +
            "        <th>PagerDuty Service Name</th>\n" +
            "        <th>Routing Key</th>\n" +
            "        <th>Operations</th>\n" +
            "      </tr>\n" +
            "    </thead>\n" +
            "    <tbody>\n");

        Set<Integer> pagerdutyServiceIdsAssociatedWithNotificationGroups = NotificationGroupsDao.getDistinctPagerdutyServiceIdsAssociatedWithNotificationGroups(DatabaseConnections.getConnection(), true);
        List<PagerdutyService> pagerdutyServices = PagerdutyServicesDao.getPagerdutyServices(DatabaseConnections.getConnection(), true);

        for (PagerdutyService pagerdutyService : pagerdutyServices) {     
            if (pagerdutyService == null) continue;
            
            String pagerdutyServiceDetails = "<a class=\"iframe cboxElement\" href=\"PagerDutyServiceDetails?ExcludeNavbar=true&amp;Name=" + StatsAggHtmlFramework.urlEncode(pagerdutyService.getName()) + "\">" + StatsAggHtmlFramework.htmlEncode(pagerdutyService.getName()) + "</a>";
            
            String routingKey = pagerdutyService.getRoutingKey();

            String alter = "<a href=\"CreatePagerDutyService?Operation=Alter&amp;Name=" + StatsAggHtmlFramework.urlEncode(pagerdutyService.getName()) + "\">alter</a>";

            List<KeyValue<String,String>> cloneKeysAndValues = new ArrayList<>();
            cloneKeysAndValues.add(new KeyValue("Operation", "Clone"));
            cloneKeysAndValues.add(new KeyValue("Id", pagerdutyService.getId().toString()));
            String clone = StatsAggHtmlFramework.buildJavaScriptPostLink("Clone_" + pagerdutyService.getName(), "PagerDutyServices", "clone", cloneKeysAndValues);
            
            List<KeyValue<String,String>> removeKeysAndValues = new ArrayList<>();
            removeKeysAndValues.add(new KeyValue("Operation", "Remove"));
            removeKeysAndValues.add(new KeyValue("Id", pagerdutyService.getId().toString()));
            String remove = StatsAggHtmlFramework.buildJavaScriptPostLink("Remove_" + pagerdutyService.getName(), "PagerDutyServices", "remove", 
                    removeKeysAndValues, true, "Are you sure you want to remove this PagerDuty Service?");       
            
            htmlBodyStringBuilder.append("<tr>\n")
                .append("<td class=\"statsagg_force_word_break\">").append(pagerdutyServiceDetails).append("</td>\n")
                .append("<td class=\"statsagg_force_word_break\">").append(StatsAggHtmlFramework.htmlEncode(routingKey)).append("</td>\n")
                .append("<td>").append(alter).append(", ").append(clone);
            
            if (pagerdutyServiceIdsAssociatedWithNotificationGroups == null) htmlBodyStringBuilder.append(", ").append(remove);
            else if (!pagerdutyServiceIdsAssociatedWithNotificationGroups.contains(pagerdutyService.getId())) htmlBodyStringBuilder.append(", ").append(remove);
 
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
    
}
