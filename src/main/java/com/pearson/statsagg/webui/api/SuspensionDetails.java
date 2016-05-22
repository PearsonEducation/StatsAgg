package com.pearson.statsagg.webui.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pearson.statsagg.database_objects.suspensions.Suspension;
import com.pearson.statsagg.database_objects.suspensions.SuspensionsDao;
import com.pearson.statsagg.utilities.DateAndTime;
import com.pearson.statsagg.utilities.StackTrace;
import java.io.PrintWriter;
import java.util.TreeMap;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prashant kumar (prashant4nov)
 * @author Jeffrey Schmidt
 */
@WebServlet(name="API_SuspensionDetails", urlPatterns={"/api/suspension-details"})
public class SuspensionDetails extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(SuspensionDetails.class.getName());
    
    public static final String PAGE_NAME = "API_SuspensionDetails";
    
    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return PAGE_NAME;
    }
    
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        processRequest(request, response);
    }
    
    private void processRequest(HttpServletRequest request, HttpServletResponse response) {
        try {
            String json = getSuspensionDetails(request);
            response.setContentType("application/json");
            PrintWriter out;
            out = response.getWriter();
            out.println(json);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
    /**
     * Returns a json string containing the details of the requested suspension.
     * 
     * @param request servlet request
     * @return details of the requested suspension
     */
    private String getSuspensionDetails(HttpServletRequest request) {
        
        if (request == null) {
            return Helper.ERROR_JSON;
        }
        
        try {
            Integer suspensionId = null;
            String suspensionName = null;

            if (request.getParameter("id") != null) suspensionId = Integer.parseInt(request.getParameter("id"));
            if (request.getParameter("name") != null) suspensionName = request.getParameter("name");

            if ((suspensionId == null) && (suspensionName == null)) {
                JsonObject jsonObject = Helper.getJsonObjectFromRequetBody(request);
                suspensionId = Helper.getIntegerFieldFromJsonObject(jsonObject, "id");
                suspensionName = Helper.getStringFieldFromJsonObject(jsonObject, "name");
            }

            Suspension suspension = null;
            SuspensionsDao suspensionsDao = new SuspensionsDao();
            if (suspensionId != null) suspension = suspensionsDao.getSuspension(suspensionId);
            else if (suspensionName != null) suspension = suspensionsDao.getSuspensionByName(suspensionName);
            else suspensionsDao.close();
            
            if (suspension != null) return getApiFriendlyJsonObject(suspension);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));        
        }
        
        return Helper.ERROR_JSON;
    }
    
    private String getApiFriendlyJsonObject(Suspension suspension) {
        
        if (suspension == null) {
            return null;
        }
        
        Gson suspension_Gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();   
        JsonElement suspension_JsonElement = suspension_Gson.toJsonTree(suspension);
        JsonObject jsonObject = new Gson().toJsonTree(suspension_JsonElement).getAsJsonObject();
        String currentFieldToAlter;
        JsonElement currentField_JsonElement;

        currentFieldToAlter = "suspend_by";
        currentField_JsonElement = jsonObject.get(currentFieldToAlter);
        if (currentField_JsonElement != null) {
            int currentField_JsonElement_Int = currentField_JsonElement.getAsInt();
            jsonObject.remove(currentFieldToAlter);
            jsonObject.addProperty(currentFieldToAlter, Suspension.getSuspendByStringFromCode(currentField_JsonElement_Int));
        }
        
        currentFieldToAlter = "metric_group_tags_inclusive";
        currentField_JsonElement = jsonObject.get(currentFieldToAlter);
        if (currentField_JsonElement != null) {
            String currentField_JsonElement_String = currentField_JsonElement.getAsString();
            if (currentField_JsonElement_String.trim().isEmpty()) jsonObject.remove(currentFieldToAlter);
        }
        
        currentFieldToAlter = "metric_group_tags_exclusive";
        currentField_JsonElement = jsonObject.get(currentFieldToAlter);
        if (currentField_JsonElement != null) {
            String currentField_JsonElement_String = currentField_JsonElement.getAsString();
            if (currentField_JsonElement_String.trim().isEmpty()) jsonObject.remove(currentFieldToAlter);
        }
        
        currentFieldToAlter = "metric_suspension_regexes";
        currentField_JsonElement = jsonObject.get(currentFieldToAlter);
        if (currentField_JsonElement != null) {
            String currentField_JsonElement_String = currentField_JsonElement.getAsString();
            if (currentField_JsonElement_String.trim().isEmpty()) jsonObject.remove(currentFieldToAlter);
        }
        
        Helper.getApiFriendlyJsonObject_CorrectTimesAndTimeUnits(jsonObject, "duration", "duration_time_unit");
        
        currentFieldToAlter = "start_date";
        currentField_JsonElement = jsonObject.get(currentFieldToAlter);
        if (currentField_JsonElement != null) {
            String startDateString = DateAndTime.getFormattedDateAndTime(suspension.getStartDate(), "yyyy-MM-dd");
            jsonObject.remove(currentFieldToAlter);
            jsonObject.addProperty(currentFieldToAlter, startDateString);
        }
        
        currentFieldToAlter = "start_time";
        currentField_JsonElement = jsonObject.get(currentFieldToAlter);
        if (currentField_JsonElement != null) {
            String startTimeString = DateAndTime.getFormattedDateAndTime(suspension.getStartTime(), "h:mm a");
            jsonObject.remove(currentFieldToAlter);
            jsonObject.addProperty(currentFieldToAlter, startTimeString);
        }
        
        if ((suspension.getSuspendBy() != null) && suspension.getSuspendBy().equals(Suspension.SUSPEND_BY_ALERT_ID)) {
            jsonObject.remove("metric_group_tags_inclusive");
            jsonObject.remove("metric_group_tags_exclusive");
            jsonObject.remove("metric_suspension_regexes");
        }
                
        if ((suspension.getSuspendBy() != null) && suspension.getSuspendBy().equals(Suspension.SUSPEND_BY_METRIC_GROUP_TAGS)) {
            jsonObject.remove("alert_id");
            jsonObject.remove("metric_group_tags_exclusive");
            jsonObject.remove("metric_suspension_regexes");
        }
        
        if ((suspension.getSuspendBy() != null) && suspension.getSuspendBy().equals(Suspension.SUSPEND_BY_EVERYTHING)) {
            jsonObject.remove("alert_id");
            jsonObject.remove("metric_group_tags_inclusive");
            jsonObject.remove("metric_suspension_regexes");
        }
        
        if ((suspension.getSuspendBy() != null) && suspension.getSuspendBy().equals(Suspension.SUSPEND_BY_METRICS)) {
            jsonObject.remove("alert_id");
            jsonObject.remove("metric_group_tags_inclusive");
            jsonObject.remove("metric_group_tags_exclusive");
        }
        
        if ((suspension.isOneTime() != null) && suspension.isOneTime()) {
            jsonObject.remove("recur_sunday");
            jsonObject.remove("recur_monday");
            jsonObject.remove("recur_tuesday");
            jsonObject.remove("recur_wednesday");
            jsonObject.remove("recur_thursday");
            jsonObject.remove("recur_friday");
            jsonObject.remove("recur_saturday");
        }
        
        return suspension_Gson.toJson(jsonObject);
    }
    
}
