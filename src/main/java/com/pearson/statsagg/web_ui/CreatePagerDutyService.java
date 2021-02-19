package com.pearson.statsagg.web_ui;

import com.pearson.statsagg.database_objects.pagerduty_services.PagerdutyService;
import com.pearson.statsagg.database_objects.pagerduty_services.PagerdutyServicesDao;
import com.pearson.statsagg.database_objects.pagerduty_services.PagerdutyServicesDaoWrapper;
import com.pearson.statsagg.globals.DatabaseConnections;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class CreatePagerDutyService extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CreatePagerDutyService.class.getName());
    
    public static final String PAGE_NAME = "Create PagerDuty Service";
    
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
            
            PagerdutyService pagerdutyService = null;
            String name = request.getParameter("Name");
            if (name != null) pagerdutyService = PagerdutyServicesDao.getPagerdutyService(DatabaseConnections.getConnection(), true, name.trim()); 

            String htmlBodyContents = buildCreatePagerdutyServiceHtml(pagerdutyService);
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
            String result = parseAndAlterPagerdutyService(request);

            StringBuilder htmlBuilder = new StringBuilder();
            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            String htmlBodyContent = statsAggHtmlFramework.buildHtmlBodyForPostResult(PAGE_NAME, StatsAggHtmlFramework.htmlEncode(result), "PagerDutyServices", PagerDutyServices.PAGE_NAME);
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
    
    private String buildCreatePagerdutyServiceHtml(PagerdutyService pagerdutyService) {

        StringBuilder htmlBody = new StringBuilder();

        htmlBody.append(
            "<div id=\"page-content-wrapper\">\n" +
            " <!-- Keep all page content within the page-content inset div! -->\n" +
            "   <div class=\"page-content inset statsagg_page_content_font\">\n" +
            "     <div class=\"content-header\"> \n" +
            "       <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "     </div> " +
            "     <form action=\"CreatePagerDutyService\" method=\"POST\">\n");
        
        if ((pagerdutyService != null) && (pagerdutyService.getName() != null) && !pagerdutyService.getName().isEmpty()) {
            htmlBody.append("<input type=\"hidden\" name=\"Old_Name\" value=\"").append(StatsAggHtmlFramework.htmlEncode(pagerdutyService.getName(), true)).append("\">");
        }
        
        
        // name
        htmlBody.append(
            "       <div class=\"form-group\">\n" +
            "         <label class=\"label_small_margin\">PagerDuty Service Name</label>\n" +
            "         <input class=\"form-control-statsagg\" placeholder=\"Enter a unique name for this PagerDuty service.\" name=\"Name\" id=\"Name\" ");

        if ((pagerdutyService != null) && (pagerdutyService.getName() != null)) {
            htmlBody.append("value=\"").append(StatsAggHtmlFramework.htmlEncode(pagerdutyService.getName(), true)).append("\"");
        }

        htmlBody.append(">\n</div>\n");
        
                   
        // description
        htmlBody.append(      
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Description</label>\n" +
            "  <textarea class=\"form-control-statsagg\" rows=\"3\" name=\"Description\" id=\"Description\" >");

        if ((pagerdutyService != null) && (pagerdutyService.getDescription() != null)) {
            htmlBody.append(StatsAggHtmlFramework.htmlEncode(pagerdutyService.getDescription(), true));
        }

        htmlBody.append("</textarea>\n" + "</div>\n");
        
        
        // pagerduty service routing key
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Routing Key</label>\n" +
            "  <input class=\"form-control-statsagg\" placeholder=\"Enter a PagerDuty service routing key\" name=\"RoutingKey\" id=\"RoutingKey\" ");

        if ((pagerdutyService != null) && (pagerdutyService.getRoutingKey() != null)) {
            htmlBody.append("value=\"").append(StatsAggHtmlFramework.htmlEncode(pagerdutyService.getRoutingKey(), true)).append("\"");
        }
        
        htmlBody.append(">\n</div>\n");

        
        htmlBody.append(
            "       <button type=\"submit\" class=\"btn btn-default btn-primary statsagg_button_no_shadow statsagg_page_content_font\">Submit</button>" +
            "&nbsp;&nbsp;&nbsp;" +
            "       <a href=\"PagerDutyServices\" class=\"btn btn-default statsagg_page_content_font\" role=\"button\">Cancel</a>" +
            "     </form>\n"       +          
            "   </div>\n" +
            "</div>\n");
            
        return htmlBody.toString();
    }
    
    public String parseAndAlterPagerdutyService(Object request) {
        
        if (request == null) {
            return null;
        }
        
        String returnString;
        
        PagerdutyService pagerdutyService = getPagerdutyServiceFromPagerdutyServiceParameters(request);
        String oldName = Common.getSingleParameterAsString(request, "Old_Name");
        if (oldName == null) oldName = Common.getSingleParameterAsString(request, "old_name");
        if (oldName == null) {
            String id = Common.getSingleParameterAsString(request, "Id");
            if (id == null) id = Common.getSingleParameterAsString(request, "id");
            
            if (id != null) {
                try {
                    Integer id_Integer = Integer.parseInt(id.trim());
                    PagerdutyService oldPagerdutyService = PagerdutyServicesDao.getPagerdutyService(DatabaseConnections.getConnection(), true, id_Integer);
                    oldName = oldPagerdutyService.getName();
                }
                catch (Exception e){}
            }
        }
        
        // insert/update/delete records in the database
        if ((pagerdutyService != null) && (pagerdutyService.getName() != null)) {
            returnString = PagerdutyServicesDaoWrapper.alterRecordInDatabase(pagerdutyService, oldName).getReturnString();
        }
        else {
            returnString = "Failed to create or alter PagerDuty service. Reason=\"Field validation failed.\"";
            logger.warn(returnString);
        }
        
        return returnString;
    }
    
    private PagerdutyService getPagerdutyServiceFromPagerdutyServiceParameters(Object request) {
        
        if (request == null) {
            return null;
        }
        
        boolean didEncounterError = false;
        
        PagerdutyService pagerdutyService = new PagerdutyService();

        try {
            String parameter;

            parameter = Common.getSingleParameterAsString(request, "Name");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "name");
            String trimmedName = parameter.trim();
            pagerdutyService.setName(trimmedName);
            if ((pagerdutyService.getName() == null) || pagerdutyService.getName().isEmpty()) didEncounterError = true;

            parameter = Common.getSingleParameterAsString(request, "Description");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "description");
            if (parameter != null) {
                String trimmedParameter = parameter.trim();
                String description;
                if (trimmedParameter.length() > 100000) description = trimmedParameter.substring(0, 99999);
                else description = trimmedParameter;
                pagerdutyService.setDescription(description);
            }
            else pagerdutyService.setDescription("");
            
            parameter = Common.getSingleParameterAsString(request, "RoutingKey");
            if (parameter == null) parameter = Common.getSingleParameterAsString(request, "routing_key");
            String trimmedRoutingKey = parameter.trim();
            pagerdutyService.setRoutingKey(trimmedRoutingKey);
            if ((pagerdutyService.getRoutingKey() == null) || pagerdutyService.getRoutingKey().isEmpty()) didEncounterError = true;
        }
        catch (Exception e) {
            didEncounterError = true;
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
            
        if (didEncounterError) {
            pagerdutyService = null;
        }
        
        return pagerdutyService;
    }

}
