package com.pearson.statsagg.webui;

import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database.notifications.NotificationGroup;
import com.pearson.statsagg.database.notifications.NotificationGroupsDao;
import com.pearson.statsagg.utilities.EmailUtils;
import com.pearson.statsagg.utilities.StackTrace;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "NotificationGroupDetails", urlPatterns = {"/NotificationGroupDetails"})
public class NotificationGroupDetails extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupDetails.class.getName());
    
    public static final String PAGE_NAME = "Notification Group Details";
    
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
        String notificationGroupDetails = getNotificationDetailsString(name);
                
        try {  
            StringBuilder htmlBuilder = new StringBuilder("");

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            
            String htmlBody = statsAggHtmlFramework.createHtmlBody(
            "<div id=\"page-content-wrapper\">\n" +
            "  <!-- Keep all page content within the page-content inset div! -->\n" +
            "  <div class=\"page-content inset\">\n" +
            "    <div class=\"content-header\"> \n" +
            "      <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "    </div> " +
            "    <div class=\"statsagg_force_word_wrap\">" +
            notificationGroupDetails +
            "    </div>\n" +
            "  </div>\n" +
            "</div>\n");
            
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

    private String getNotificationDetailsString(String notificationGroupName) {
        
        if (notificationGroupName == null) {
            return "";
        }
        
        StringBuilder outputString = new StringBuilder("");
        
        NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
        NotificationGroup notificationGroup = notificationGroupsDao.getNotificationGroupByName(notificationGroupName);
        
        if (notificationGroup != null) {                  
            outputString.append("<b>Name</b> = ").append(StatsAggHtmlFramework.htmlEncode(notificationGroup.getName())).append("<br>");
            
            outputString.append("<b>Email addresses</b> = ");
            if (notificationGroup.getEmailAddresses() != null) outputString.append(StatsAggHtmlFramework.htmlEncode(notificationGroup.getEmailAddresses())).append("<br>");
            else outputString.append("N/A <br>");
            
            outputString.append("<br>");
            
            String[] emailAddresses = StringUtils.split(notificationGroup.getEmailAddresses(), ",");
            
            for (String emailAddress : emailAddresses) {
                String trimmedEmailAddress = emailAddress.trim();
                boolean isValidEmailAddress = EmailUtils.isValidEmailAddress(trimmedEmailAddress);
                outputString.append("<b>Is \"").append(StatsAggHtmlFramework.htmlEncode(trimmedEmailAddress)).
                        append("\" a valid email address?</b> = ").append(isValidEmailAddress).append("<br>");
            }
            
        }
        
        return outputString.toString();
    }

}
