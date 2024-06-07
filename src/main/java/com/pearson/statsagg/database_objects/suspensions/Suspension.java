package com.pearson.statsagg.database_objects.suspensions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import com.pearson.statsagg.database_objects.DatabaseObjectValidation;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.utilities.time_utils.DateAndTime;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseObject;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import java.util.Date;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class Suspension implements DatabaseObject<Suspension> {
    
    private static final Logger logger = LoggerFactory.getLogger(Suspension.class.getName());

    public static final int SUSPEND_BY_ALERT_ID = 1;
    public static final int SUSPEND_BY_METRIC_GROUP_TAGS = 2;
    public static final int SUSPEND_BY_EVERYTHING = 3;
    public static final int SUSPEND_BY_METRICS = 4;
    
    @SerializedName("id") private Integer id_ = null;
    @SerializedName("name") private String name_ = null;
    private transient String uppercaseName_ = null;
    @SerializedName("description") private String description_ = null;
    @SerializedName("enabled") private Boolean isEnabled_ = null;
    @SerializedName("suspend_by") private Integer suspendBy_ = null;
    @SerializedName("alert_id") private Integer alertId_ = null;
    @SerializedName("metric_group_tags_inclusive") private String metricGroupTagsInclusive_ = null;
    @SerializedName("metric_group_tags_exclusive") private String metricGroupTagsExclusive_ = null;
    @SerializedName("metric_suspension_regexes") private String metricSuspensionRegexes_ = null;
    
    @SerializedName("one_time") private Boolean isOneTime_ = null;
    @SerializedName("suspend_notification_only") private Boolean isSuspendNotificationOnly_ = null;
    @SerializedName("recur_sunday") private Boolean isRecurSunday_ = null;
    @SerializedName("recur_monday") private Boolean isRecurMonday_ = null;
    @SerializedName("recur_tuesday") private Boolean isRecurTuesday_ = null;
    @SerializedName("recur_wednesday") private Boolean isRecurWednesday_ = null;
    @SerializedName("recur_thursday") private Boolean isRecurThursday_ = null;
    @SerializedName("recur_friday") private Boolean isRecurFriday_ = null;
    @SerializedName("recur_saturday") private Boolean isRecurSaturday_ = null;
    @SerializedName("start_date") private Timestamp startDate_ = null;
    @SerializedName("start_time") private Timestamp startTime_ = null;
    @SerializedName("duration") private Long duration_ = null;  // native timeunit is milliseconds
    @SerializedName("duration_time_unit") private Integer durationTimeUnit_ = null; 
    private transient Timestamp deleteAtTimestamp_ = null;    

    public Suspension() {
        this.id_ = -1;
    }
    
    public Suspension(Integer id, String name, String description, Boolean isEnabled, 
            Integer suspendBy, Integer alertId, String metricGroupTagsInclusive, String metricGroupTagsExclusive, String metricSuspensionRegexes, 
            Boolean isOneTime, Boolean isSuspendNotificationOnly,
            Boolean isRecurSunday, Boolean isRecurMonday, Boolean isRecurTuesday, Boolean isRecurWednesday,  
            Boolean isRecurThursday, Boolean isRecurFriday, Boolean isRecurSaturday, 
            Timestamp startDate, Timestamp startTime, Long duration, Integer durationTimeUnit, Timestamp deleteAtTimestamp) {
        
        this(id, name, ((name == null) ? null : name.toUpperCase()), description, isEnabled, 
             suspendBy, alertId, metricGroupTagsInclusive, metricGroupTagsExclusive, metricSuspensionRegexes, 
             isOneTime, isSuspendNotificationOnly,
             isRecurSunday, isRecurMonday, isRecurTuesday, isRecurWednesday,  
             isRecurThursday, isRecurFriday, isRecurSaturday, 
             startDate, startTime, duration, durationTimeUnit, deleteAtTimestamp);
    }
    
    public Suspension(Integer id, String name, String uppercaseName, String description, Boolean isEnabled, 
            Integer suspendBy, Integer alertId, String metricGroupTagsInclusive, String metricGroupTagsExclusive, String metricSuspensionRegexes, 
            Boolean isOneTime, Boolean isSuspendNotificationOnly,
            Boolean isRecurSunday, Boolean isRecurMonday, Boolean isRecurTuesday, Boolean isRecurWednesday,  
            Boolean isRecurThursday, Boolean isRecurFriday, Boolean isRecurSaturday, 
            Timestamp startDate, Timestamp startTime, Long duration, Integer durationTimeUnit, Timestamp deleteAtTimestamp) {
        
        this.id_ = id;
        this.name_ = name;
        this.uppercaseName_ = uppercaseName;
        this.description_ = description;
        this.isEnabled_ = isEnabled;
        this.suspendBy_ = suspendBy;
        this.alertId_ = alertId;
        this.metricGroupTagsInclusive_ = metricGroupTagsInclusive;
        this.metricGroupTagsExclusive_ = metricGroupTagsExclusive;
        this.metricSuspensionRegexes_ = metricSuspensionRegexes;
        
        this.isOneTime_ = isOneTime;
        this.isSuspendNotificationOnly_ = isSuspendNotificationOnly;
        this.isRecurSunday_ = isRecurSunday;
        this.isRecurMonday_ = isRecurMonday;
        this.isRecurTuesday_ = isRecurTuesday;
        this.isRecurWednesday_ = isRecurWednesday;
        this.isRecurThursday_ = isRecurThursday;
        this.isRecurFriday_ = isRecurFriday;
        this.isRecurSaturday_ = isRecurSaturday;
        
        if (startDate == null) this.startDate_ = null;
        else this.startDate_ = (Timestamp) startDate.clone();
        
        if (startTime == null) this.startTime_ = null;
        else this.startTime_ = (Timestamp) startTime.clone();
        
        this.duration_ = duration;
        this.durationTimeUnit_ = durationTimeUnit;
        
        if (deleteAtTimestamp == null) this.deleteAtTimestamp_ = null;
        else this.deleteAtTimestamp_ = (Timestamp) deleteAtTimestamp.clone();
    }

    public static Suspension copy(Suspension suspension) {
        
        if (suspension == null) {
            return null;
        }
        
        Suspension suspensionCopy = new Suspension();
        
        suspensionCopy.setId(suspension.getId());
        suspensionCopy.setName(suspension.getName());
        suspensionCopy.setDescription(suspension.getDescription());
        suspensionCopy.setIsEnabled(suspension.isEnabled());
        suspensionCopy.setSuspendBy(suspension.getSuspendBy());
        suspensionCopy.setAlertId(suspension.getAlertId());
        suspensionCopy.setMetricGroupTagsInclusive(suspension.getMetricGroupTagsInclusive());
        suspensionCopy.setMetricGroupTagsExclusive(suspension.getMetricGroupTagsExclusive());
        suspensionCopy.setMetricSuspensionRegexes(suspension.getMetricSuspensionRegexes());

        suspensionCopy.setIsOneTime(suspension.isOneTime());
        suspensionCopy.setIsSuspendNotificationOnly(suspension.isSuspendNotificationOnly());
        suspensionCopy.setIsRecurSunday(suspension.isRecurSunday());
        suspensionCopy.setIsRecurMonday(suspension.isRecurMonday());
        suspensionCopy.setIsRecurTuesday(suspension.isRecurTuesday());
        suspensionCopy.setIsRecurWednesday(suspension.isRecurWednesday());
        suspensionCopy.setIsRecurThursday(suspension.isRecurThursday());
        suspensionCopy.setIsRecurFriday(suspension.isRecurFriday());
        suspensionCopy.setIsRecurSaturday(suspension.isRecurSaturday());
        
        suspensionCopy.setStartDate(suspension.getStartDate());
        suspensionCopy.setStartTime(suspension.getStartTime());
        suspensionCopy.setDuration(suspension.getDuration());
        suspensionCopy.setDurationTimeUnit(suspension.getDurationTimeUnit());
        suspensionCopy.setDeleteAtTimestamp(suspension.getDeleteAtTimestamp());
        
        return suspensionCopy;
    }
    
    @Override
    public boolean isEqual(Suspension suspension) {
       
        if (suspension == null) return false;
        if (suspension == this) return true;
        if (suspension.getClass() != getClass()) return false;
        
        return new EqualsBuilder()
                .append(id_, suspension.getId())
                .append(name_, suspension.getName())
                .append(uppercaseName_, suspension.getUppercaseName())
                .append(description_, suspension.getDescription())
                .append(isEnabled_, suspension.isEnabled())
                .append(suspendBy_, suspension.getSuspendBy())
                .append(alertId_, suspension.getAlertId())
                .append(metricGroupTagsInclusive_, suspension.getMetricGroupTagsInclusive())
                .append(metricGroupTagsExclusive_, suspension.getMetricGroupTagsExclusive())
                .append(metricSuspensionRegexes_, suspension.getMetricSuspensionRegexes())
                .append(isOneTime_, suspension.isOneTime())
                .append(isSuspendNotificationOnly_, suspension.isSuspendNotificationOnly())
                .append(isRecurSunday_, suspension.isRecurSunday())
                .append(isRecurMonday_, suspension.isRecurMonday())
                .append(isRecurTuesday_, suspension.isRecurTuesday())
                .append(isRecurWednesday_, suspension.isRecurWednesday())
                .append(isRecurThursday_, suspension.isRecurThursday())
                .append(isRecurFriday_, suspension.isRecurFriday())
                .append(isRecurSaturday_, suspension.isRecurSaturday())
                .append(startDate_, suspension.getStartDate())
                .append(startTime_, suspension.getStartTime())
                .append(duration_, suspension.getDuration())
                .append(durationTimeUnit_, suspension.getDurationTimeUnit())
                .append(deleteAtTimestamp_, suspension.getDeleteAtTimestamp())
                .isEquals();
    }
    
    public static DatabaseObjectValidation isValid(Suspension suspension) {
        
        if (suspension == null) return new DatabaseObjectValidation(false, "Invalid suspension");

        DatabaseObjectValidation databaseObjectValidation_CheckOptions = isValid_CheckOptions(suspension);
        if (!databaseObjectValidation_CheckOptions.isValid()) return databaseObjectValidation_CheckOptions;

        DatabaseObjectValidation databaseObjectValidation_CheckSuspendBy = isValid_CheckSuspendBy(suspension);
        if (!databaseObjectValidation_CheckSuspendBy.isValid()) return databaseObjectValidation_CheckSuspendBy;
        
        DatabaseObjectValidation databaseObjectValidation_SuspensionType = isValid_SuspensionType(suspension);
        if (!databaseObjectValidation_SuspensionType.isValid()) return databaseObjectValidation_SuspensionType;
        
        return new DatabaseObjectValidation(true);
    }

    /*
    Checks to make sure that the 'options' criteria are valid
    */
    public static DatabaseObjectValidation isValid_CheckOptions(Suspension suspension) {
        DatabaseObjectValidation databaseObjectValidation_Fail = new DatabaseObjectValidation(false, "Invalid 'options' setting");
        
        if (suspension == null) return databaseObjectValidation_Fail;
        if (suspension.getId() == null) return databaseObjectValidation_Fail;

        DatabaseObjectValidation databaseObjectValidation_Name = new DatabaseObjectValidation(false, "Invalid suspension name");
        if ((suspension.getName() == null) || (suspension.getName().isEmpty())) return databaseObjectValidation_Name;
        
        DatabaseObjectValidation databaseObjectValidation_NotificationOnly = new DatabaseObjectValidation(false, "Invalid suspension 'notification only' setting");
        if (suspension.isSuspendNotificationOnly() == null) return databaseObjectValidation_NotificationOnly;

        return new DatabaseObjectValidation(true);
    }
    
    /*
    Checks to make sure that the 'suspend by' criteria are valid
    */
    public static DatabaseObjectValidation isValid_CheckSuspendBy(Suspension suspension) {
        
        DatabaseObjectValidation databaseObjectValidation_Fail = new DatabaseObjectValidation(false, "Invalid 'suspension by' setting");

        if ((suspension == null) || (suspension.getSuspendBy() == null)) {
            return databaseObjectValidation_Fail;
        }
        
        if ((suspension.getSuspendBy() == Suspension.SUSPEND_BY_ALERT_ID) && (suspension.getAlertId() != null)) return new DatabaseObjectValidation(true);
        else if ((suspension.getSuspendBy() == Suspension.SUSPEND_BY_ALERT_ID) && (suspension.getAlertId() == null)) {
            return new DatabaseObjectValidation(false, "Invalid 'suspension by' setting. Trying to 'suspend by' an alert, but the alert does not exist");
        }
        
        if (suspension.getSuspendBy() == Suspension.SUSPEND_BY_METRIC_GROUP_TAGS) return new DatabaseObjectValidation(true);
        if (suspension.getSuspendBy() == Suspension.SUSPEND_BY_EVERYTHING) return new DatabaseObjectValidation(true);
        if (suspension.getSuspendBy() == Suspension.SUSPEND_BY_METRICS) return new DatabaseObjectValidation(true);
        
        return databaseObjectValidation_Fail;
    }
    
    /*
    Checks to make sure that the 'suspension type' variables are valid
    */
    public static DatabaseObjectValidation isValid_SuspensionType(Suspension suspension) {
        
        if ((suspension == null) || (suspension.isOneTime() == null)) {
            DatabaseObjectValidation databaseObjectValidation_Fail = new DatabaseObjectValidation(false, "Invalid 'suspension type' setting");
            return databaseObjectValidation_Fail;
        }
        
        if (!suspension.isOneTime()) {
            // isRecured days of the week can't be null if this is a 'recurring' suspension
            DatabaseObjectValidation databaseObjectValidation_FailRecurringDays = new DatabaseObjectValidation(false, "Invalid 'recurring days' setting");
            if (suspension.isRecurSunday() == null) return databaseObjectValidation_FailRecurringDays;
            if (suspension.isRecurMonday() == null) return databaseObjectValidation_FailRecurringDays;
            if (suspension.isRecurTuesday() == null) return databaseObjectValidation_FailRecurringDays;
            if (suspension.isRecurWednesday() == null) return databaseObjectValidation_FailRecurringDays;
            if (suspension.isRecurThursday() == null) return databaseObjectValidation_FailRecurringDays;
            if (suspension.isRecurFriday() == null) return databaseObjectValidation_FailRecurringDays;
            if (suspension.isRecurSaturday() == null) return databaseObjectValidation_FailRecurringDays;
        
            // can't suspend for more than 24hrs if alert is recurring
            DatabaseObjectValidation databaseObjectValidation_FailRecurringDaysDurationTooBig = new DatabaseObjectValidation(false, "Duration can't be more than 24 hours when using a recurring suspension");
            if (suspension.getDuration() > 86400000) return databaseObjectValidation_FailRecurringDaysDurationTooBig; // 86400000ms = 1day
            
            DatabaseObjectValidation databaseObjectValidation_FailRecurringDaysDurationTooSmall = new DatabaseObjectValidation(false, "Duration must be greater than 0");
            if (suspension.getDuration() <= 0) return databaseObjectValidation_FailRecurringDaysDurationTooSmall; 
        }
        
        // start dates/times can't be null
        DatabaseObjectValidation databaseObjectValidation_FailDateAndTime = new DatabaseObjectValidation(false, "Date/time must be set");
        if (suspension.getStartDate() == null) return databaseObjectValidation_FailDateAndTime;
        if (suspension.getStartTime() == null) return databaseObjectValidation_FailDateAndTime;
        if (suspension.getDuration() == null) return databaseObjectValidation_FailDateAndTime;

        // a one-time alert can't have a 'delete at' date that is in the past
        if (suspension.isOneTime() && suspension.getDeleteAtTimestamp() == null) {
            return databaseObjectValidation_FailDateAndTime;
        }
        else if (suspension.isOneTime() && suspension.getDeleteAtTimestamp() != null) {
            if (System.currentTimeMillis() >= suspension.getDeleteAtTimestamp().getTime()) {
                DatabaseObjectValidation databaseObjectValidation_OneTimeInPast = new DatabaseObjectValidation(false, "Date/time/duration for a 'one time' suspension cannot be in the past");
                return databaseObjectValidation_OneTimeInPast;
            }
        }

        return new DatabaseObjectValidation(true);
    }
    
    public static boolean isSuspensionActive(Suspension suspension) {
        if (suspension == null || (suspension.isEnabled() == null)) {
            return false;
        }
        
        return suspension.isEnabled() && Suspension.isSuspensionInSuspensionTimeWindow(suspension);
    }
    
    public static boolean isSuspensionActive(Suspension suspension, Calendar dateAndTime) {
        if (suspension == null || (suspension.isEnabled() == null)) {
            return false;
        }
        
        return suspension.isEnabled() && isSuspensionInSuspensionTimeWindow(suspension, dateAndTime);
    }
    
    public static boolean isSuspensionInSuspensionTimeWindow(Suspension suspension) {
        if (suspension == null) {
            return false;
        }
        
        Calendar currentDateAndTime = Calendar.getInstance();
        return isSuspensionInSuspensionTimeWindow(suspension, currentDateAndTime);
    }
    
    public static boolean isSuspensionInSuspensionTimeWindow(Suspension suspension, Calendar dateAndTime) {
        if ((suspension == null) || (dateAndTime == null)) {
            return false;
        }
        
        boolean isDateAndTimeInSuspensionWindow = Suspension.isDateAndTimeInSuspensionWindow(suspension, dateAndTime);
        return isDateAndTimeInSuspensionWindow;
    }

    // specifiedDateAndTime will usually refer to 'current date/time'
    public static boolean isDateAndTimeInSuspensionWindow(Suspension suspension, Calendar specifiedDateAndTime) {

        if ((suspension == null) || (suspension.getStartTime() == null) || (suspension.getStartDate() == null) || 
                (suspension.getDuration() == null) || (specifiedDateAndTime == null)) {
            return false;
        }

        long suspensionDuration_Milliseconds = suspension.getDuration();
        int suspensionStartTime_HourOfDay = suspension.getStartTime().getHours();
        int suspensionStartTime_Minute = suspension.getStartTime().getMinutes();
        int suspensionStartTime_Second = suspension.getStartTime().getSeconds();
        int suspensionStartTime_Millisecond = (int) (suspension.getStartTime().getTime() % 1000);

        // gets a calendar with suspension's date & time. used for checking if specifiedDateAndTime is before the start date/time. 
        Calendar suspensionStartDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(suspension.getStartDate(), suspensionStartTime_HourOfDay, 
                    suspensionStartTime_Minute, suspensionStartTime_Second, suspensionStartTime_Millisecond);
        
        if (suspension.isOneTime()) { // handles the 'one time' use-case
            return isDateAndTimeInSuspensionWindow_OneTime(suspension, suspensionStartDateAndTime, specifiedDateAndTime, suspensionDuration_Milliseconds);
        }
        else { // handles the 'recurring' use-case
            return isDateAndTimeInSuspensionWindow_Recurring(suspension, suspensionStartDateAndTime, specifiedDateAndTime, suspensionDuration_Milliseconds);
        }
    }
    
    private static boolean isDateAndTimeInSuspensionWindow_OneTime(Suspension suspension, Calendar suspensionStartDateAndTime, 
            Calendar specifiedDateAndTime, long suspensionDuration_Milliseconds) {
        
        if ((suspension == null) || (suspension.getStartTime() == null) || (suspension.getStartDate() == null) || 
                (suspension.getDuration() == null) || (specifiedDateAndTime == null)) {
            return false;
        }
        
        long suspensionStartDateAndTime_Milliseconds = suspensionStartDateAndTime.getTimeInMillis();
        long specifiedDateAndTime_Milliseconds = specifiedDateAndTime.getTimeInMillis();
        long suspensionStartDateAndTime_PlusDuration_Milliseconds = suspensionDuration_Milliseconds + suspensionStartDateAndTime_Milliseconds;
        
        if ((specifiedDateAndTime_Milliseconds >= suspensionStartDateAndTime_Milliseconds) && 
                (specifiedDateAndTime_Milliseconds < suspensionStartDateAndTime_PlusDuration_Milliseconds)) {
            return true;
        }
        
        return false;
    }
    
    private static boolean isDateAndTimeInSuspensionWindow_Recurring(Suspension suspension, Calendar suspensionStartDateAndTime, 
            Calendar specifiedDateAndTime, long suspensionDuration_Milliseconds) {
        
        /* Note -- this method may seem to have a contrived implementation, but the majority of the coding choices for this method were made to  
        achieve maximum performance. A cleaner, Calendar-only, model was originally used, but it performed 2x slower, so it was replaced with this structure. */
        
        if ((suspension == null) || (suspension.getStartTime() == null) || (suspension.getStartDate() == null) || 
                (suspension.getDuration() == null) || (specifiedDateAndTime == null)) {
            return false;
        }
        
        long specifiedDateAndTime_Milliseconds = specifiedDateAndTime.getTimeInMillis();        
        Date specifiedDateAndTime_Date = new Date(specifiedDateAndTime_Milliseconds);
        Date specifiedDateAndTime_MinusDuration = new Date(specifiedDateAndTime_Milliseconds - suspensionDuration_Milliseconds);
        
        /* covers the cicumstance of specifiedDateAndTime being in an active suspension time window, and the time window started the day before specifiedDateAndTime */
        if (specifiedDateAndTime_Date.getDate() != specifiedDateAndTime_MinusDuration.getDate()) {
            // gets a calendar with a date of "one day before specifiedDateAndTime's date" & suspension's start-time
            Calendar startTime_DayBeforeSpecifiedDay = (Calendar) specifiedDateAndTime.clone();
            startTime_DayBeforeSpecifiedDay.add(Calendar.DATE, -1);
            startTime_DayBeforeSpecifiedDay = DateAndTime.getCalendarWithSameDateAtDifferentTime(startTime_DayBeforeSpecifiedDay, suspensionStartDateAndTime.get(Calendar.HOUR_OF_DAY), 
                    suspensionStartDateAndTime.get(Calendar.MINUTE), suspensionStartDateAndTime.get(Calendar.SECOND), suspensionStartDateAndTime.get(Calendar.MILLISECOND));
            long startTime_DayBeforeSpecifiedDay_Milliseconds = startTime_DayBeforeSpecifiedDay.getTimeInMillis();
            
            // gets unix time (in ms) with startTime_DayBeforeSpecifiedDay's date & suspension's end-time
            long endTime_SpecifiedDay_Milliseconds = startTime_DayBeforeSpecifiedDay_Milliseconds + suspensionDuration_Milliseconds;

            if (((specifiedDateAndTime_Milliseconds > startTime_DayBeforeSpecifiedDay_Milliseconds) || (specifiedDateAndTime_Milliseconds == startTime_DayBeforeSpecifiedDay_Milliseconds)) && 
                    (specifiedDateAndTime_Milliseconds < endTime_SpecifiedDay_Milliseconds)) {  
            
                // specifiedDateAndTime's start date & time is before suspension's start date & time
                if (startTime_DayBeforeSpecifiedDay_Milliseconds < suspensionStartDateAndTime.getTimeInMillis()) {
                    return false;
                }
                else {
                    return isSuspensionAllowed_DayOfWeek(suspension, startTime_DayBeforeSpecifiedDay);
                }
            }
        }

        /* covers the cicumstance of dealing with an suspension that doesn't involve an suspension time window that started the day before specifiedDateAndTime */

        // gets a calendar with specifiedDateAndTime's date & suspension's start-time
        Calendar startTime_SpecifiedDay = DateAndTime.getCalendarWithSameDateAtDifferentTime((Calendar) specifiedDateAndTime.clone(), suspensionStartDateAndTime.get(Calendar.HOUR_OF_DAY), 
                    suspensionStartDateAndTime.get(Calendar.MINUTE), suspensionStartDateAndTime.get(Calendar.SECOND), suspensionStartDateAndTime.get(Calendar.MILLISECOND));
        long startTime_SpecifiedDay_Milliseconds = startTime_SpecifiedDay.getTimeInMillis();
        
        // specifiedDateAndTime's start date & time is before a suspension's start date & time
        if (startTime_SpecifiedDay_Milliseconds < suspensionStartDateAndTime.getTimeInMillis()) {
            return false;
        }

        // gets unix time (in ms) with specifiedDateAndTime's date & suspension's end-time
        long endTime_SpecifiedDay_Milliseconds = startTime_SpecifiedDay_Milliseconds + suspensionDuration_Milliseconds;
        
        if (((specifiedDateAndTime_Milliseconds > startTime_SpecifiedDay_Milliseconds) || (specifiedDateAndTime_Milliseconds == startTime_SpecifiedDay_Milliseconds)) && 
                (specifiedDateAndTime_Milliseconds < endTime_SpecifiedDay_Milliseconds)) {  
            return isSuspensionAllowed_DayOfWeek(suspension, startTime_SpecifiedDay);
        }
        else {
            return false;
        }
        
    }
    
    public static boolean isSuspensionAllowed_DayOfWeek(Suspension suspension, Calendar specifiedDateAndTime) {
        
        if ((suspension == null) || (suspension.isOneTime() == null)) {
            return false;
        }
        
        // suspensions are allowed on all days if it is a 'one time' suspension 
        if (suspension.isOneTime()) return true;
        
        // isRecured days of the week can't be null if this is a 'recurring' suspension
        if (suspension.isRecurSunday() == null) return false;
        if (suspension.isRecurMonday() == null) return false;
        if (suspension.isRecurTuesday() == null) return false;
        if (suspension.isRecurWednesday() == null) return false;
        if (suspension.isRecurThursday() == null) return false;
        if (suspension.isRecurFriday() == null) return false;
        if (suspension.isRecurSaturday() == null) return false;
        
        // checks to see if the alert is allowed to run based on the current day of the week
        if (Calendar.SUNDAY == specifiedDateAndTime.get(Calendar.DAY_OF_WEEK)) return suspension.isRecurSunday();
        if (Calendar.MONDAY == specifiedDateAndTime.get(Calendar.DAY_OF_WEEK)) return suspension.isRecurMonday();
        if (Calendar.TUESDAY == specifiedDateAndTime.get(Calendar.DAY_OF_WEEK)) return suspension.isRecurTuesday();
        if (Calendar.WEDNESDAY == specifiedDateAndTime.get(Calendar.DAY_OF_WEEK)) return suspension.isRecurWednesday();
        if (Calendar.THURSDAY == specifiedDateAndTime.get(Calendar.DAY_OF_WEEK)) return suspension.isRecurThursday();
        if (Calendar.FRIDAY == specifiedDateAndTime.get(Calendar.DAY_OF_WEEK)) return suspension.isRecurFriday();
        if (Calendar.SATURDAY == specifiedDateAndTime.get(Calendar.DAY_OF_WEEK)) return suspension.isRecurSaturday();

        return false;
    }

    public static String trimNewLineDelimitedTags(String newLineDelimitedTags) {
        return DatabaseObjectCommon.trimNewLineDelimitedString(newLineDelimitedTags);
    }
    
    public static String getSuspendByStringFromCode(Integer suspendByCode) {
        
        if ((suspendByCode == null)) {
            return null;
        }

        if (suspendByCode == SUSPEND_BY_ALERT_ID) return "AlertName";
        else if (suspendByCode == SUSPEND_BY_METRIC_GROUP_TAGS) return "Tags";
        else if (suspendByCode == SUSPEND_BY_EVERYTHING) return "Everything";
        else if (suspendByCode == SUSPEND_BY_METRICS) return "Metrics";
        else logger.warn("Unrecognized suspend-by code");
         
        return null;
    }
    
    public static JsonObject getJsonObject_ApiFriendly(Suspension suspension) {
        return getJsonObject_ApiFriendly(suspension, null);
    }
    
    public static JsonObject getJsonObject_ApiFriendly(Suspension suspension, Alert alert) {
        
        if (suspension == null) {
            return null;
        }
        
        try {
            Gson suspension_Gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();   
            JsonElement suspension_JsonElement = suspension_Gson.toJsonTree(suspension);
            JsonObject jsonObject = new Gson().toJsonTree(suspension_JsonElement).getAsJsonObject();
            String currentFieldToAlter;
            JsonElement currentField_JsonElement;

            if ((alert != null) && (suspension.getAlertId() != null) && (alert.getId() != null) && 
                    (suspension.getAlertId().intValue() == alert.getId().intValue())) {
                jsonObject.addProperty("alert_name", alert.getName());
            }
            else if ((alert != null) && (suspension.getAlertId() != null) && (alert.getId() != null)) {
                logger.error("'Alert Id' from the 'suspension' object must match the Alert's 'Id'");
            }
            
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
                else {
                    List<String> fieldArrayValues = StringUtilities.getListOfStringsFromDelimitedString(currentField_JsonElement_String.trim(), '\n');
                    JsonArray jsonArray = new JsonArray();
                    if (fieldArrayValues != null) for (String metricGroupTag : fieldArrayValues) jsonArray.add(metricGroupTag);
                    jsonObject.add(currentFieldToAlter, jsonArray);
                }
            }

            currentFieldToAlter = "metric_group_tags_exclusive";
            currentField_JsonElement = jsonObject.get(currentFieldToAlter);
            if (currentField_JsonElement != null) {
                String currentField_JsonElement_String = currentField_JsonElement.getAsString();
                if (currentField_JsonElement_String.trim().isEmpty()) jsonObject.remove(currentFieldToAlter);
                else {
                    List<String> fieldArrayValues = StringUtilities.getListOfStringsFromDelimitedString(currentField_JsonElement_String.trim(), '\n');
                    JsonArray jsonArray = new JsonArray();
                    if (fieldArrayValues != null) for (String metricGroupTag : fieldArrayValues) jsonArray.add(metricGroupTag);
                    jsonObject.add(currentFieldToAlter, jsonArray);
                }
            }

            currentFieldToAlter = "metric_suspension_regexes";
            currentField_JsonElement = jsonObject.get(currentFieldToAlter);
            if (currentField_JsonElement != null) {
                String currentField_JsonElement_String = currentField_JsonElement.getAsString();
                if (currentField_JsonElement_String.trim().isEmpty()) jsonObject.remove(currentFieldToAlter);
                else {
                    List<String> fieldArrayValues = StringUtilities.getListOfStringsFromDelimitedString(currentField_JsonElement_String.trim(), '\n');
                    JsonArray jsonArray = new JsonArray();
                    if (fieldArrayValues != null) for (String metricGroupTag : fieldArrayValues) jsonArray.add(metricGroupTag);
                    jsonObject.add(currentFieldToAlter, jsonArray);
                }
            }

            DatabaseObjectCommon.getApiFriendlyJsonObject_CorrectTimesAndTimeUnits(jsonObject, "duration", "duration_time_unit");

            currentFieldToAlter = "start_date";
            currentField_JsonElement = jsonObject.get(currentFieldToAlter);
            if (currentField_JsonElement != null) {
                String startDateString = DateAndTime.getFormattedDateAndTime(suspension.getStartDate(), "MM/dd/yyyy");
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
                jsonObject.remove("alert_name");
                jsonObject.remove("metric_group_tags_exclusive");
                jsonObject.remove("metric_suspension_regexes");
            }

            if ((suspension.getSuspendBy() != null) && suspension.getSuspendBy().equals(Suspension.SUSPEND_BY_EVERYTHING)) {
                jsonObject.remove("alert_id");
                jsonObject.remove("alert_name");
                jsonObject.remove("metric_group_tags_inclusive");
                jsonObject.remove("metric_suspension_regexes");
            }

            if ((suspension.getSuspendBy() != null) && suspension.getSuspendBy().equals(Suspension.SUSPEND_BY_METRICS)) {
                jsonObject.remove("alert_id");
                jsonObject.remove("alert_name");
                jsonObject.remove("metric_group_tags_inclusive");
                jsonObject.remove("metric_group_tags_exclusive");
                jsonObject.remove("suspend_notification_only");
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

            return jsonObject;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        
    }
    
    public static String getJsonString_ApiFriendly(Suspension suspension) {
        return getJsonString_ApiFriendly(suspension, null);
    }
    
    public static String getJsonString_ApiFriendly(Suspension suspension, Alert alert) {
        
        if (suspension == null) {
            return null;
        }
        
        try {
            JsonObject jsonObject = getJsonObject_ApiFriendly(suspension, alert);
            if (jsonObject == null) return null;

            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();   
            return gson.toJson(jsonObject);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        
    }

    public Integer getId() {
        return id_;
    }

    public void setId(Integer id) {
        this.id_ = id;
    }

    public String getName() {
        return name_;
    }

    public void setName(String name) {
        this.name_ = name;
        if (name != null) this.uppercaseName_ = name.toUpperCase();
    }
    
    public String getUppercaseName() {
        return uppercaseName_;
    }

    public String getDescription() {
        return description_;
    }

    public void setDescription(String description) {
        this.description_ = description;
    }

    public Boolean isEnabled() {
        return isEnabled_;
    }

    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled_ = isEnabled;
    }

    public Integer getSuspendBy() {
        return suspendBy_;
    }

    public void setSuspendBy(Integer suspendBy) {
        this.suspendBy_ = suspendBy;
    }
    
    public Integer getAlertId() {
        return alertId_;
    }

    public void setAlertId(Integer alertId) {
        this.alertId_ = alertId;
    }

    public String getMetricGroupTagsInclusive() {
        return metricGroupTagsInclusive_;
    }

    public void setMetricGroupTagsInclusive(String metricGroupTagsInclusive) {
        this.metricGroupTagsInclusive_ = metricGroupTagsInclusive;
    }
    
    public String getMetricGroupTagsExclusive() {
        return metricGroupTagsExclusive_;
    }

    public void setMetricGroupTagsExclusive(String metricGroupTagsExclusive) {
        this.metricGroupTagsExclusive_ = metricGroupTagsExclusive;
    }
    
    public String getMetricSuspensionRegexes() {
        return metricSuspensionRegexes_;
    }

    public void setMetricSuspensionRegexes(String metricSuspensionRegexes) {
        this.metricSuspensionRegexes_ = metricSuspensionRegexes;
    }
    
    public Boolean isOneTime() {
        return isOneTime_;
    }

    public void setIsOneTime(Boolean isOneTime) {
        this.isOneTime_ = isOneTime;
    }

    public Boolean isSuspendNotificationOnly() {
        return isSuspendNotificationOnly_;
    }

    public void setIsSuspendNotificationOnly(Boolean isSuspendNotificationOnly) {
        this.isSuspendNotificationOnly_ = isSuspendNotificationOnly;
    }

    public Boolean isRecurSunday() {
        return isRecurSunday_;
    }

    public void setIsRecurSunday(Boolean isRecurSunday) {
        this.isRecurSunday_ = isRecurSunday;
    }

    public Boolean isRecurMonday() {
        return isRecurMonday_;
    }

    public void setIsRecurMonday(Boolean isRecurMonday) {
        this.isRecurMonday_ = isRecurMonday;
    }

    public Boolean isRecurTuesday() {
        return isRecurTuesday_;
    }

    public void setIsRecurTuesday(Boolean isRecurTuesday) {
        this.isRecurTuesday_ = isRecurTuesday;
    }

    public Boolean isRecurWednesday() {
        return isRecurWednesday_;
    }

    public void setIsRecurWednesday(Boolean isRecurWednesday) {
        this.isRecurWednesday_ = isRecurWednesday;
    }

    public Boolean isRecurThursday() {
        return isRecurThursday_;
    }

    public void setIsRecurThursday(Boolean isRecurThursday) {
        this.isRecurThursday_ = isRecurThursday;
    }

    public Boolean isRecurFriday() {
        return isRecurFriday_;
    }

    public void setIsRecurFriday(Boolean isRecurFriday) {
        this.isRecurFriday_ = isRecurFriday;
    }

    public Boolean isRecurSaturday() {
        return isRecurSaturday_;
    }

    public void setIsRecurSaturday(Boolean isRecurSaturday) {
        this.isRecurSaturday_ = isRecurSaturday;
    }

    public Timestamp getStartDate() {
        if (startDate_ == null) return null;
        else return (Timestamp) startDate_.clone();
    }

    public void setStartDate(Timestamp startDate) {
        if (startDate == null) this.startDate_ = null;
        else this.startDate_ = (Timestamp) startDate.clone();
    }

    public Timestamp getStartTime() {
        if (startTime_ == null) return null;
        else return (Timestamp) startTime_.clone();
    }

    public void setStartTime(Timestamp startTime) {
        if (startTime == null) this.startTime_ = null;
        else this.startTime_ = (Timestamp) startTime.clone();
    }

    public Long getDuration() {
        return duration_;
    }

    public void setDuration(Long duration) {
        this.duration_ = duration;
    }
    
    public Integer getDurationTimeUnit() {
        return durationTimeUnit_;
    }

    public void setDurationTimeUnit(Integer durationTimeUnit) {
        this.durationTimeUnit_ = durationTimeUnit;
    }
    
    public Timestamp getDeleteAtTimestamp() {
        if (deleteAtTimestamp_ == null) return null;
        else return (Timestamp) deleteAtTimestamp_.clone();
    }

    public void setDeleteAtTimestamp(Timestamp deleteAtTimestamp) {
        if (deleteAtTimestamp == null) this.deleteAtTimestamp_ = null;
        else this.deleteAtTimestamp_ = (Timestamp) deleteAtTimestamp.clone();
    }

}
