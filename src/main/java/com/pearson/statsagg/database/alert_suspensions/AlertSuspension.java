package com.pearson.statsagg.database.alert_suspensions;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import com.pearson.statsagg.database.DatabaseObject;
import com.pearson.statsagg.utilities.DateAndTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class AlertSuspension extends DatabaseObject<AlertSuspension> {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertSuspension.class.getName());

    public static final int SUSPEND_BY_ALERT_ID = 1;
    public static final int SUSPEND_BY_METRIC_GROUP_TAGS = 2;
    public static final int SUSPEND_BY_EVERYTHING = 3;
    
    private Integer id_ = null;
    private String name_ = null;
    private String uppercaseName_ = null;
    private Boolean isEnabled_ = null;
    private Integer suspendBy_ = null;
    private Integer alertId_ = null;
    private String metricGroupTagsInclusive_ = null;
    private String metricGroupTagsExclusive_ = null;
    private Boolean isOneTime_ = null;
    private Boolean isSuspendNotificationOnly_ = null;
    
    private Boolean isRecurSunday_ = null;
    private Boolean isRecurMonday_ = null;
    private Boolean isRecurTuesday_ = null;
    private Boolean isRecurWednesday_ = null;
    private Boolean isRecurThursday_ = null;
    private Boolean isRecurFriday_ = null;
    private Boolean isRecurSaturday_ = null;
    
    private Timestamp startDate_ = null;
    private Timestamp startTime_ = null;
    private Integer duration_ = null;
    private Timestamp deleteAtTimestamp_ = null;    
    
    public AlertSuspension() {
        this.id_ = -1;
    }
    
    public AlertSuspension(Integer id, String name, Boolean isEnabled, 
            Integer suspendBy, Integer alertId, String metricGroupTagsInclusive, String metricGroupTagsExclusive, Boolean isOneTime, Boolean isSuspendNotificationOnly,
            Boolean isRecurSunday, Boolean isRecurMonday, Boolean isRecurTuesday, Boolean isRecurWednesday,  
            Boolean isRecurThursday, Boolean isRecurFriday, Boolean isRecurSaturday, 
            Timestamp startDate, Timestamp startTime, Integer duration, Timestamp deleteAtTimestamp) {
        
        this(id, name, ((name == null) ? null : name.toUpperCase()), isEnabled, 
             suspendBy, alertId, metricGroupTagsInclusive, metricGroupTagsExclusive, isOneTime, isSuspendNotificationOnly,
             isRecurSunday, isRecurMonday, isRecurTuesday, isRecurWednesday,  
             isRecurThursday, isRecurFriday, isRecurSaturday, 
             startDate, startTime, duration, deleteAtTimestamp);
    }
    
    public AlertSuspension(Integer id, String name, String uppercaseName, Boolean isEnabled, 
            Integer suspendBy, Integer alertId, String metricGroupTagsInclusive, String metricGroupTagsExclusive, Boolean isOneTime, Boolean isSuspendNotificationOnly,
            Boolean isRecurSunday, Boolean isRecurMonday, Boolean isRecurTuesday, Boolean isRecurWednesday,  
            Boolean isRecurThursday, Boolean isRecurFriday, Boolean isRecurSaturday, 
            Timestamp startDate, Timestamp startTime, Integer duration, Timestamp deleteAtTimestamp) {
        
        this.id_ = id;
        this.name_ = name;
        this.uppercaseName_ = uppercaseName;
        this.isEnabled_ = isEnabled;
        this.suspendBy_ = suspendBy;
        this.alertId_ = alertId;
        this.metricGroupTagsInclusive_ = metricGroupTagsInclusive;
        this.metricGroupTagsExclusive_ = metricGroupTagsExclusive;
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
        
        if (deleteAtTimestamp == null) this.deleteAtTimestamp_ = null;
        else this.deleteAtTimestamp_ = (Timestamp) deleteAtTimestamp.clone();
    }

    public static AlertSuspension copy(AlertSuspension alertSuspension) {
        
        if (alertSuspension == null) {
            return null;
        }
        
        AlertSuspension alertSuspensionCopy = new AlertSuspension();
        
        alertSuspensionCopy.setId(alertSuspension.getId());
        alertSuspensionCopy.setName(alertSuspension.getName());
        alertSuspensionCopy.setUppercaseName(alertSuspension.getUppercaseName());
        alertSuspensionCopy.setIsEnabled(alertSuspension.isEnabled());
        alertSuspensionCopy.setSuspendBy(alertSuspension.getSuspendBy());
        alertSuspensionCopy.setAlertId(alertSuspension.getAlertId());
        alertSuspensionCopy.setMetricGroupTagsInclusive(alertSuspension.getMetricGroupTagsInclusive());
        alertSuspensionCopy.setMetricGroupTagsExclusive(alertSuspension.getMetricGroupTagsExclusive());
        alertSuspensionCopy.setIsOneTime(alertSuspension.isOneTime());
        alertSuspensionCopy.setIsSuspendNotificationOnly(alertSuspension.isSuspendNotificationOnly());
        
        alertSuspensionCopy.setIsRecurSunday(alertSuspension.isRecurSunday());
        alertSuspensionCopy.setIsRecurMonday(alertSuspension.isRecurMonday());
        alertSuspensionCopy.setIsRecurTuesday(alertSuspension.isRecurTuesday());
        alertSuspensionCopy.setIsRecurWednesday(alertSuspension.isRecurWednesday());
        alertSuspensionCopy.setIsRecurThursday(alertSuspension.isRecurThursday());
        alertSuspensionCopy.setIsRecurFriday(alertSuspension.isRecurFriday());
        alertSuspensionCopy.setIsRecurSaturday(alertSuspension.isRecurSaturday());
        
        alertSuspensionCopy.setStartDate(alertSuspension.getStartDate());
        alertSuspensionCopy.setStartTime(alertSuspension.getStartTime());
        alertSuspensionCopy.setDuration(alertSuspension.getDuration());
        alertSuspensionCopy.setDeleteAtTimestamp(alertSuspension.getDeleteAtTimestamp());
        
        return alertSuspensionCopy;
    }
    
    @Override
    public boolean isEqual(AlertSuspension alertSuspension) {
       
        if (alertSuspension == null) return false;
        if (alertSuspension == this) return true;
        if (alertSuspension.getClass() != getClass()) return false;
        
        return new EqualsBuilder()
                .append(id_, alertSuspension.getId())
                .append(name_, alertSuspension.getName())
                .append(uppercaseName_, alertSuspension.getUppercaseName())
                .append(isEnabled_, alertSuspension.isEnabled())
                .append(suspendBy_, alertSuspension.getSuspendBy())
                .append(alertId_, alertSuspension.getAlertId())
                .append(metricGroupTagsInclusive_, alertSuspension.getMetricGroupTagsInclusive())
                .append(metricGroupTagsExclusive_, alertSuspension.getMetricGroupTagsExclusive())
                .append(isOneTime_, alertSuspension.isOneTime())
                .append(isSuspendNotificationOnly_, alertSuspension.isSuspendNotificationOnly())
                .append(isRecurSunday_, alertSuspension.isRecurSunday())
                .append(isRecurMonday_, alertSuspension.isRecurMonday())
                .append(isRecurTuesday_, alertSuspension.isRecurTuesday())
                .append(isRecurWednesday_, alertSuspension.isRecurWednesday())
                .append(isRecurThursday_, alertSuspension.isRecurThursday())
                .append(isRecurFriday_, alertSuspension.isRecurFriday())
                .append(isRecurSaturday_, alertSuspension.isRecurSaturday())
                .append(startDate_, alertSuspension.getStartDate())
                .append(startTime_, alertSuspension.getStartTime())
                .append(duration_, alertSuspension.getDuration())
                .append(deleteAtTimestamp_, alertSuspension.getDeleteAtTimestamp())
                .isEquals();
    }
     
    public static boolean isValid(AlertSuspension alertSuspension) {
        
        if (alertSuspension == null) return false;

        boolean isValid_CheckOptions = isValid_CheckOptions(alertSuspension);
        if (!isValid_CheckOptions) return false;

        boolean isValid_CheckSuspendBy = isValid_CheckSuspendBy(alertSuspension);
        if (!isValid_CheckSuspendBy) return false;
        
        boolean isValid_SuspensionType = isValid_SuspensionType(alertSuspension);
        return isValid_SuspensionType;
    }

    /*
    Checks to make sure that the 'options' criteria are valid
    */
    public static boolean isValid_CheckOptions(AlertSuspension alertSuspension) {
        if (alertSuspension == null) return false;
        if (alertSuspension.getId() == null) return false;
        if ((alertSuspension.getName() == null) || (alertSuspension.getName().isEmpty())) return false;
        if (alertSuspension.isSuspendNotificationOnly() == null) return false;
        
        return true;
    }
    
    /*
    Checks to make sure that the 'suspend by' criteria are valid
    */
    public static boolean isValid_CheckSuspendBy(AlertSuspension alertSuspension) {
        
        if ((alertSuspension == null) || (alertSuspension.getSuspendBy() == null)) {
            return false;
        }
        
        if (alertSuspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_ALERT_ID) {
            if (alertSuspension.getAlertId() != null) {
                return true;
            }
        } 
        
        if (alertSuspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_METRIC_GROUP_TAGS) {
            return true;
        } 
        
        if (alertSuspension.getSuspendBy() == AlertSuspension.SUSPEND_BY_EVERYTHING) {
            return true;
        } 
        
        return false;
    }
    
    /*
    Checks to make sure that the 'suspension type' variables are valid
    */
    public static boolean isValid_SuspensionType(AlertSuspension alertSuspension) {
        
        if ((alertSuspension == null) || (alertSuspension.isOneTime() == null)) {
            return false;
        }
        
        if (!alertSuspension.isOneTime()) {
            // isRecured days of the week can't be null if this is a 'recurring' alert suspension
            if (alertSuspension.isRecurSunday() == null) return false;
            if (alertSuspension.isRecurMonday() == null) return false;
            if (alertSuspension.isRecurTuesday() == null) return false;
            if (alertSuspension.isRecurWednesday() == null) return false;
            if (alertSuspension.isRecurThursday() == null) return false;
            if (alertSuspension.isRecurFriday() == null) return false;
            if (alertSuspension.isRecurSaturday() == null) return false;
        }
        
        // start dates/times can't be null
        if (alertSuspension.getStartDate() == null) return false;
        if (alertSuspension.getStartTime() == null) return false;
        if (alertSuspension.getDuration() == null) return false;
        
        // can't suspend for more than 24hrs
        if (alertSuspension.getDuration() > 1440) return false; 
        if (alertSuspension.getDuration() <= 0) return false; 
        
        // a one-time alert have a 'delete at' date that is in the past
        if (alertSuspension.isOneTime() && alertSuspension.getDeleteAtTimestamp() == null) return false;
        else if (alertSuspension.isOneTime() && alertSuspension.getDeleteAtTimestamp() != null) {
            if (System.currentTimeMillis() >= alertSuspension.getDeleteAtTimestamp().getTime()) {
                return false;
            }
        }

        return true;
    }
    
    public static boolean isAlertSuspensionActive(AlertSuspension alertSuspension) {
        if (alertSuspension == null || (alertSuspension.isEnabled() == null)) {
            return false;
        }
        
        return alertSuspension.isEnabled() && isAlertSuspensionInSuspensionTimeWindow(alertSuspension);
    }
    
    public static boolean isAlertSuspensionActive(AlertSuspension alertSuspension, Calendar dateAndTime) {
        if (alertSuspension == null || (alertSuspension.isEnabled() == null)) {
            return false;
        }
        
        return alertSuspension.isEnabled() && isAlertSuspensionInSuspensionTimeWindow(alertSuspension, dateAndTime);
    }
    
    public static boolean isAlertSuspensionInSuspensionTimeWindow(AlertSuspension alertSuspension) {
        if (alertSuspension == null) {
            return false;
        }
        
        Calendar currentDateAndTime = Calendar.getInstance();
        return isAlertSuspensionInSuspensionTimeWindow(alertSuspension, currentDateAndTime);
    }
    
    public static boolean isAlertSuspensionInSuspensionTimeWindow(AlertSuspension alertSuspension, Calendar dateAndTime) {
        if ((alertSuspension == null) || (dateAndTime == null)) {
            return false;
        }
        
        boolean isDateAndTimeInSuspensionWindow = AlertSuspension.isDateAndTimeInSuspensionWindow(alertSuspension, dateAndTime);
        return isDateAndTimeInSuspensionWindow;
    }

    public static boolean isDateAndTimeInSuspensionWindow(AlertSuspension alertSuspension, Calendar specifiedDateAndTime) {

        /* Note -- this method may seem to have a contrived implementation, but the majority of the coding choices for this method were made to  
        achieve maximum performance. A cleaner, Calendar-only, model was originally used, but it performed 2x slower, so it was replaced with this structure. */
        
        if ((alertSuspension == null) || (alertSuspension.getStartTime() == null) || (alertSuspension.getStartDate() == null) || 
                (alertSuspension.getDuration() == null) || (specifiedDateAndTime == null)) {
            return false;
        }

        long alertSuspensionDuration_Milliseconds = (long) (60000 * (long) alertSuspension.getDuration());
        long specifiedDateAndTime_Milliseconds = specifiedDateAndTime.getTimeInMillis();
        int suspensionStartTime_HourOfDay = alertSuspension.getStartTime().getHours();
        int suspensionStartTime_Minute = alertSuspension.getStartTime().getMinutes();
        int suspensionStartTime_Second = alertSuspension.getStartTime().getSeconds();
        int suspensionStartTime_Millisecond = (int) ((long) alertSuspension.getStartTime().getTime() % 1000);
        
        Date specifiedDateAndTime_Date = new Date(specifiedDateAndTime_Milliseconds);
        Date specifiedDateAndTime_MinusDuration = new Date(specifiedDateAndTime_Milliseconds - alertSuspensionDuration_Milliseconds);

        // gets a calendar with alertSuspension's date & time. used for checking if specifiedDateAndTime is before the start date/time. 
        Calendar suspensionStartDateAndTime = DateAndTime.getCalendarWithSameDateAtDifferentTime(alertSuspension.getStartDate(), suspensionStartTime_HourOfDay, 
                    suspensionStartTime_Minute, suspensionStartTime_Second, suspensionStartTime_Millisecond);

        /* covers the cicumstance of specifiedDateAndTime being in an active alert suspension time window, and the time window started the day before specifiedDateAndTime */
        if (specifiedDateAndTime_Date.getDate() != specifiedDateAndTime_MinusDuration.getDate()) {
            // gets a calendar with a date of "one day before specifiedDateAndTime's date" & alertSuspension's start-time
            Calendar startTime_DayBeforeSpecifiedDay = (Calendar) specifiedDateAndTime.clone();
            startTime_DayBeforeSpecifiedDay.add(Calendar.DATE, -1);
            startTime_DayBeforeSpecifiedDay = DateAndTime.getCalendarWithSameDateAtDifferentTime(startTime_DayBeforeSpecifiedDay, suspensionStartDateAndTime.get(Calendar.HOUR_OF_DAY), 
                    suspensionStartDateAndTime.get(Calendar.MINUTE), suspensionStartDateAndTime.get(Calendar.SECOND), suspensionStartDateAndTime.get(Calendar.MILLISECOND));
            long startTime_DayBeforeSpecifiedDay_Milliseconds = startTime_DayBeforeSpecifiedDay.getTimeInMillis();
            
            // gets unix time (in ms) with startTime_DayBeforeSpecifiedDay's date & alertSuspension's end-time
            long endTime_SpecifiedDay_Milliseconds = startTime_DayBeforeSpecifiedDay_Milliseconds + alertSuspensionDuration_Milliseconds;

            if (((specifiedDateAndTime_Milliseconds > startTime_DayBeforeSpecifiedDay_Milliseconds) || (specifiedDateAndTime_Milliseconds == startTime_DayBeforeSpecifiedDay_Milliseconds)) && 
                    (specifiedDateAndTime_Milliseconds < endTime_SpecifiedDay_Milliseconds)) {  
            
                // specifiedDateAndTime's start date & time is before alertSuspension's start date & time
                if (startTime_DayBeforeSpecifiedDay_Milliseconds < suspensionStartDateAndTime.getTimeInMillis()) {
                    return false;
                }
                else {
                    return isAlertSuspensionAllowed_DayOfWeek(alertSuspension, startTime_DayBeforeSpecifiedDay);
                }
            }
        }

        /* covers the cicumstance of dealing with an alert suspension that doesn't involve an alert suspension time window that started the day before specifiedDateAndTime */

        // gets a calendar with specifiedDateAndTime's date & alertSuspension's start-time
        Calendar startTime_SpecifiedDay = DateAndTime.getCalendarWithSameDateAtDifferentTime((Calendar) specifiedDateAndTime.clone(), suspensionStartTime_HourOfDay, 
					suspensionStartTime_Minute, suspensionStartTime_Second, suspensionStartTime_Millisecond);
        long startTime_SpecifiedDay_Milliseconds = startTime_SpecifiedDay.getTimeInMillis();
        
        // specifiedDateAndTime's start date & time is before alertSuspension's start date & time
        if (startTime_SpecifiedDay_Milliseconds < suspensionStartDateAndTime.getTimeInMillis()) {
            return false;
        }

        // gets unix time (in ms) with specifiedDateAndTime's date & alertSuspension's end-time
        long endTime_SpecifiedDay_Milliseconds = startTime_SpecifiedDay_Milliseconds + alertSuspensionDuration_Milliseconds;
        
        if (((specifiedDateAndTime_Milliseconds > startTime_SpecifiedDay_Milliseconds) || (specifiedDateAndTime_Milliseconds == startTime_SpecifiedDay_Milliseconds)) && 
                (specifiedDateAndTime_Milliseconds < endTime_SpecifiedDay_Milliseconds)) {  
            return isAlertSuspensionAllowed_DayOfWeek(alertSuspension, startTime_SpecifiedDay);
        }
        else {
            return false;
        }

    }
    
    public static boolean isAlertSuspensionAllowed_DayOfWeek(AlertSuspension alertSuspension, Calendar specifiedDateAndTime) {
        
        if ((alertSuspension == null) || (alertSuspension.isOneTime() == null)) {
            return false;
        }
        
        // alert suspensions are isRecured on all days if it is a 'one time' alert suspension 
        if (alertSuspension.isOneTime()) return true;
        
        // isRecured days of the week can't be null if this is a 'recurring' alert suspension
        if (alertSuspension.isRecurSunday() == null) return false;
        if (alertSuspension.isRecurMonday() == null) return false;
        if (alertSuspension.isRecurTuesday() == null) return false;
        if (alertSuspension.isRecurWednesday() == null) return false;
        if (alertSuspension.isRecurThursday() == null) return false;
        if (alertSuspension.isRecurFriday() == null) return false;
        if (alertSuspension.isRecurSaturday() == null) return false;
        
        // checks to see if the alert is allowed to run based on the current day of the week
        if (Calendar.SUNDAY == specifiedDateAndTime.get(Calendar.DAY_OF_WEEK)) return alertSuspension.isRecurSunday();
        if (Calendar.MONDAY == specifiedDateAndTime.get(Calendar.DAY_OF_WEEK)) return alertSuspension.isRecurMonday();
        if (Calendar.TUESDAY == specifiedDateAndTime.get(Calendar.DAY_OF_WEEK)) return alertSuspension.isRecurTuesday();
        if (Calendar.WEDNESDAY == specifiedDateAndTime.get(Calendar.DAY_OF_WEEK)) return alertSuspension.isRecurWednesday();
        if (Calendar.THURSDAY == specifiedDateAndTime.get(Calendar.DAY_OF_WEEK)) return alertSuspension.isRecurThursday();
        if (Calendar.FRIDAY == specifiedDateAndTime.get(Calendar.DAY_OF_WEEK)) return alertSuspension.isRecurFriday();
        if (Calendar.SATURDAY == specifiedDateAndTime.get(Calendar.DAY_OF_WEEK)) return alertSuspension.isRecurSaturday();

        return false;
    }

    public static List<String> getMetricGroupTagStringsFromNewlineDelimitedString(String metricGroupTagDelimitedString) {
        
        if ((metricGroupTagDelimitedString == null) || metricGroupTagDelimitedString.isEmpty()) {
            return new ArrayList<>();
        }
        
        String[] metricGroupTagStringArray = StringUtils.split(metricGroupTagDelimitedString, '\n');
        
        List<String> metricGroupTagsList = new ArrayList<>();
        
        metricGroupTagsList.addAll(Arrays.asList(metricGroupTagStringArray));
        
        return metricGroupTagsList;
    }
    
    public static Set<String> getMetricGroupTagsSetFromNewlineDelimitedString(String metricGroupTagDelimitedString) {
        
        if ((metricGroupTagDelimitedString == null) || metricGroupTagDelimitedString.isEmpty()) {
            return new HashSet<>();
        }
        
        String[] metricGroupTagStringArray = StringUtils.split(metricGroupTagDelimitedString, '\n');
        
        if (metricGroupTagStringArray.length == 0) {
            return new HashSet<>();
        }
        
        Set<String> metricGroupTagsSet = new HashSet<>();
        
        metricGroupTagsSet.addAll(Arrays.asList(metricGroupTagStringArray));
        
        return metricGroupTagsSet;
    }

    public static String trimNewLineDelimitedTags(String newLineDelimitedTags) {
        
        if (newLineDelimitedTags == null || newLineDelimitedTags.isEmpty()) {
            return newLineDelimitedTags;
        }
        
        StringBuilder tagStringBuilder = new StringBuilder("");

        List<String> tags = AlertSuspension.getMetricGroupTagStringsFromNewlineDelimitedString(newLineDelimitedTags);
        if ((tags != null) && !tags.isEmpty()) {
            for (String tag : tags) {
                String trimmedTag = tag.trim();
                if (!trimmedTag.isEmpty()) tagStringBuilder.append(trimmedTag).append("\n");
            }
        }
        
        return tagStringBuilder.toString().trim();
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
    }
    
    public String getUppercaseName() {
        return uppercaseName_;
    }

    public void setUppercaseName(String uppercaseName) {
        this.uppercaseName_ = uppercaseName;
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

    public Integer getDuration() {
        return duration_;
    }

    public void setDuration(Integer duration) {
        this.duration_ = duration;
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
