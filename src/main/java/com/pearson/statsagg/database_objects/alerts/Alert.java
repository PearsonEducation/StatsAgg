package com.pearson.statsagg.database_objects.alerts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;
import com.pearson.statsagg.database_engine.DatabaseObject;
import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import com.pearson.statsagg.database_objects.metric_group.MetricGroup;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTag;
import com.pearson.statsagg.database_objects.notifications.NotificationGroup;
import com.pearson.statsagg.utilities.math_utils.MathUtilities;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.json_utils.JsonBigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class Alert extends DatabaseObject<Alert> {
    
    private static final Logger logger = LoggerFactory.getLogger(Alert.class.getName());
    
    public static final int CAUTION = 61;
    public static final int DANGER = 62;
    
    public static final int TYPE_AVAILABILITY = 1001;
    public static final int TYPE_THRESHOLD = 1002;
    
    public static final int OPERATOR_GREATER = 1;
    public static final int OPERATOR_GREATER_EQUALS = 2;
    public static final int OPERATOR_LESS = 3;
    public static final int OPERATOR_LESS_EQUALS = 4;
    public static final int OPERATOR_EQUALS = 5;
    
    public static final int COMBINATION_ANY = 101;
    public static final int COMBINATION_ALL = 102;
    public static final int COMBINATION_AVERAGE = 103;
    public static final int COMBINATION_AT_MOST_COUNT = 105;
    public static final int COMBINATION_AT_LEAST_COUNT = 106;
     
    @SerializedName("id") private Integer id_;
    @SerializedName("name") private String name_ = null;
    private transient String uppercaseName_ = null;
    @SerializedName("description") private String description_ = null;
    @SerializedName("metric_group_id") private Integer metricGroupId_ = null;
    @SerializedName("enabled") private Boolean isEnabled_ = null;
    @SerializedName("caution_enabled") private Boolean isCautionEnabled_ = null;
    @SerializedName("danger_enabled") private Boolean isDangerEnabled_ = null;
    @SerializedName("alert_type") private Integer alertType_ = null;

    @SerializedName("alert_on_positive") private Boolean alertOnPositive_ = null;
    @SerializedName("allow_resend_alert") private Boolean allowResendAlert_ = null;
    @SerializedName("resend_alert_every") private Long resendAlertEvery_ = null;
    @SerializedName("resend_alert_every_time_unit") private Integer resendAlertEveryTimeUnit_ = null;
    
    @SerializedName("caution_notification_group_id") private Integer cautionNotificationGroupId_ = null;
    @SerializedName("caution_positive_notification_group_id") private Integer cautionPositiveNotificationGroupId_ = null;
    @SerializedName("caution_operator") private Integer cautionOperator_ = null;
    @SerializedName("caution_combination") private Integer cautionCombination_ = null; 
    @SerializedName("caution_combination_count") private Integer cautionCombinationCount_ = null;
    @SerializedName("caution_threshold") private BigDecimal cautionThreshold_ = null; 
    @SerializedName("caution_window_duration") private Long cautionWindowDuration_ = null;  // native timeunit is milliseconds
    @SerializedName("caution_window_duration_time_unit") private Integer cautionWindowDurationTimeUnit_ = null;
    @SerializedName("caution_stop_tracking_after") private Long cautionStopTrackingAfter_ = null;
    @SerializedName("caution_stop_tracking_after_time_unit") private Integer cautionStopTrackingAfterTimeUnit_ = null;
    @SerializedName("caution_minimum_sample_count") private Integer cautionMinimumSampleCount_ = null;
    @SerializedName("caution_alert_active") private Boolean isCautionAlertActive_ = null;
    @SerializedName("caution_alert_last_sent_timestamp") private Timestamp cautionAlertLastSentTimestamp_ = null;
    @SerializedName("caution_alert_acknowledged_status") private Boolean isCautionAlertAcknowledged_ = null;
    private transient String cautionActiveAlertsSet_ = null;
    @SerializedName("caution_first_active_at") private Timestamp cautionFirstActiveAt_ = null;
    
    @SerializedName("danger_notification_group_id") private Integer dangerNotificationGroupId_ = null;
    @SerializedName("danger_positive_notification_group_id") private Integer dangerPositiveNotificationGroupId_ = null;
    @SerializedName("danger_operator") private Integer dangerOperator_ = null; 
    @SerializedName("danger_combination") private Integer dangerCombination_ = null; 
    @SerializedName("danger_combination_count") private Integer dangerCombinationCount_ = null;
    @SerializedName("danger_threshold") private BigDecimal dangerThreshold_ = null; 
    @SerializedName("danger_window_duration") private Long dangerWindowDuration_ = null; // native timeunit is milliseconds
    @SerializedName("danger_window_duration_time_unit") private Integer dangerWindowDurationTimeUnit_ = null;
    @SerializedName("danger_stop_tracking_after") private Long dangerStopTrackingAfter_ = null;
    @SerializedName("danger_stop_tracking_after_time_unit") private Integer dangerStopTrackingAfterTimeUnit_ = null;
    @SerializedName("danger_minimum_sample_count") private Integer dangerMinimumSampleCount_ = null;
    @SerializedName("danger_alert_active") private Boolean isDangerAlertActive_ = null;
    @SerializedName("danger_alert_last_sent_timestamp") private Timestamp dangerAlertLastSentTimestamp_ = null;
    @SerializedName("danger_alert_acknowledged_status") private Boolean isDangerAlertAcknowledged_ = null;
    private transient String dangerActiveAlertsSet_ = null;
    @SerializedName("danger_first_active_at") private Timestamp dangerFirstActiveAt_ = null;
    
    public Alert() {
        this.id_ = -1;
    }
    
    public Alert(Integer id, String name, String description, Integer metricGroupId, Boolean isEnabled, Boolean isCautionEnabled, 
            Boolean isDangerEnabled, Integer alertType, Boolean alertOnPositive, Boolean allowResendAlert, Long resendAlertEvery, Integer resendAlertEveryTimeUnit, 
            Integer cautionNotificationGroupId, Integer cautionPositiveNotificationGroupId, Integer cautionOperator, Integer cautionCombination, 
            Integer cautionCombinationCount, BigDecimal cautionThreshold, Long cautionWindowDuration, Integer cautionWindowDurationTimeUnit, 
            Long cautionStopTrackingAfter, Integer cautionStopTrackingAfterTimeUnit, Integer cautionMinimumSampleCount, Boolean isCautionAlertActive, 
            Timestamp cautionAlertLastSentTimestamp, Boolean isCautionAlertAcknowledged, String cautionActiveAlertsSet, Timestamp cautionFirstActiveAt, 
            Integer dangerNotificationGroupId, Integer dangerPositiveNotificationGroupId, Integer dangerOperator, Integer dangerCombination, 
            Integer dangerCombinationCount, BigDecimal dangerThreshold, Long dangerWindowDuration, Integer dangerWindowDurationTimeUnit, 
            Long dangerStopTrackingAfter, Integer dangerStopTrackingAfterTimeUnit, Integer dangerMinimumSampleCount, Boolean isDangerAlertActive,  
            Timestamp dangerAlertLastSentTimestamp, Boolean isDangerAlertAcknowledged, String dangerActiveAlertsSet, Timestamp dangerFirstActiveAt) {
        
        this(id, name, ((name == null) ? null : name.toUpperCase()), description, metricGroupId, isEnabled, isCautionEnabled, 
             isDangerEnabled, alertType, alertOnPositive, allowResendAlert, resendAlertEvery, resendAlertEveryTimeUnit,
             cautionNotificationGroupId, cautionPositiveNotificationGroupId, cautionOperator, cautionCombination,  
             cautionCombinationCount, cautionThreshold, cautionWindowDuration, cautionWindowDurationTimeUnit, 
             cautionStopTrackingAfter, cautionStopTrackingAfterTimeUnit, cautionMinimumSampleCount, isCautionAlertActive,  
             cautionAlertLastSentTimestamp, isCautionAlertAcknowledged, cautionActiveAlertsSet, cautionFirstActiveAt, 
             dangerNotificationGroupId, dangerPositiveNotificationGroupId, dangerOperator, dangerCombination,  
             dangerCombinationCount, dangerThreshold, dangerWindowDuration, dangerWindowDurationTimeUnit, 
             dangerStopTrackingAfter, dangerStopTrackingAfterTimeUnit, dangerMinimumSampleCount, isDangerAlertActive,  
             dangerAlertLastSentTimestamp, isDangerAlertAcknowledged, dangerActiveAlertsSet, dangerFirstActiveAt);
    }

    public Alert(Integer id, String name, String uppercaseName, String description, Integer metricGroupId, Boolean isEnabled, Boolean isCautionEnabled, 
            Boolean isDangerEnabled, Integer alertType, Boolean alertOnPositive, Boolean allowResendAlert, Long resendAlertEvery, Integer resendAlertEveryTimeUnit, 
            Integer cautionNotificationGroupId, Integer cautionPositiveNotificationGroupId, Integer cautionOperator, Integer cautionCombination, 
            Integer cautionCombinationCount, BigDecimal cautionThreshold, Long cautionWindowDuration, Integer cautionWindowDurationTimeUnit, 
            Long cautionStopTrackingAfter, Integer cautionStopTrackingAfterTimeUnit, Integer cautionMinimumSampleCount, Boolean isCautionAlertActive, 
            Timestamp cautionAlertLastSentTimestamp, Boolean isCautionAlertAcknowledged, String cautionActiveAlertsSet, Timestamp cautionFirstActiveAt, 
            Integer dangerNotificationGroupId, Integer dangerPositiveNotificationGroupId, Integer dangerOperator, Integer dangerCombination, 
            Integer dangerCombinationCount, BigDecimal dangerThreshold, Long dangerWindowDuration, Integer dangerWindowDurationTimeUnit, 
            Long dangerStopTrackingAfter, Integer dangerStopTrackingAfterTimeUnit, Integer dangerMinimumSampleCount, Boolean isDangerAlertActive,  
            Timestamp dangerAlertLastSentTimestamp, Boolean isDangerAlertAcknowledged, String dangerActiveAlertsSet, Timestamp dangerFirstActiveAt) {
        this.id_ = id;
        this.name_ = name;
        this.uppercaseName_ = uppercaseName;
        this.description_ = description;
        this.metricGroupId_ = metricGroupId;
        this.isEnabled_ = isEnabled;
        this.isCautionEnabled_ = isCautionEnabled;
        this.isDangerEnabled_ = isDangerEnabled;
        this.alertType_ = alertType;
        
        this.alertOnPositive_ = alertOnPositive;
        this.allowResendAlert_ = allowResendAlert;
        this.resendAlertEvery_ = resendAlertEvery;
        this.resendAlertEveryTimeUnit_ = resendAlertEveryTimeUnit;
        
        this.cautionNotificationGroupId_ = cautionNotificationGroupId;
        this.cautionPositiveNotificationGroupId_ = cautionPositiveNotificationGroupId;
        this.cautionOperator_ = cautionOperator;
        this.cautionCombination_ = cautionCombination;
        this.cautionCombinationCount_ = cautionCombinationCount;
        this.cautionThreshold_ = cautionThreshold;
        this.cautionWindowDuration_ = cautionWindowDuration;
        this.cautionWindowDurationTimeUnit_ = cautionWindowDurationTimeUnit;
        this.cautionStopTrackingAfter_ = cautionStopTrackingAfter;
        this.cautionStopTrackingAfterTimeUnit_ = cautionStopTrackingAfterTimeUnit;
        this.cautionMinimumSampleCount_ = cautionMinimumSampleCount;
        this.isCautionAlertActive_ = isCautionAlertActive;
        if (cautionAlertLastSentTimestamp == null) this.cautionAlertLastSentTimestamp_ = null;
        else this.cautionAlertLastSentTimestamp_ = (Timestamp) cautionAlertLastSentTimestamp.clone();
        this.isCautionAlertAcknowledged_ = isCautionAlertAcknowledged;
        this.cautionActiveAlertsSet_ = cautionActiveAlertsSet;
        this.cautionFirstActiveAt_ = cautionFirstActiveAt;

        this.dangerNotificationGroupId_ = dangerNotificationGroupId;
        this.dangerPositiveNotificationGroupId_ = dangerPositiveNotificationGroupId;
        this.dangerOperator_ = dangerOperator;
        this.dangerCombination_ = dangerCombination;
        this.dangerCombinationCount_ = dangerCombinationCount;
        this.dangerThreshold_ = dangerThreshold;
        this.dangerWindowDuration_ = dangerWindowDuration;
        this.dangerWindowDurationTimeUnit_ = dangerWindowDurationTimeUnit;
        this.dangerStopTrackingAfter_ = dangerStopTrackingAfter;
        this.dangerStopTrackingAfterTimeUnit_ = dangerStopTrackingAfterTimeUnit;
        this.dangerMinimumSampleCount_ = dangerMinimumSampleCount;
        this.isDangerAlertActive_ = isDangerAlertActive;
        if (dangerAlertLastSentTimestamp == null) this.dangerAlertLastSentTimestamp_ = null;
        else this.dangerAlertLastSentTimestamp_ = (Timestamp) dangerAlertLastSentTimestamp.clone();
        this.isDangerAlertAcknowledged_ = isDangerAlertAcknowledged;
        this.dangerActiveAlertsSet_ = dangerActiveAlertsSet;
        this.dangerFirstActiveAt_ = dangerFirstActiveAt;
    }
    
    public static Alert copy(Alert alert) {
        
        if (alert == null) {
            return null;
        }
        
        Alert alertCopy = new Alert();
        
        alertCopy.setId(alert.getId());
        alertCopy.setName(alert.getName());
        alertCopy.setUppercaseName(alert.getUppercaseName());
        alertCopy.setDescription(alert.getDescription());
        alertCopy.setMetricGroupId(alert.getMetricGroupId());
        alertCopy.setIsEnabled(alert.isEnabled());
        alertCopy.setIsCautionEnabled(alert.isCautionEnabled());
        alertCopy.setIsDangerEnabled(alert.isDangerEnabled());
        alertCopy.setAlertType(alert.getAlertType());

        alertCopy.setAlertOnPositive(alert.isAlertOnPositive());
        alertCopy.setAllowResendAlert(alert.isAllowResendAlert());
        alertCopy.setResendAlertEvery(alert.getResendAlertEvery());
        alertCopy.setResendAlertEveryTimeUnit(alert.getResendAlertEveryTimeUnit());

        alertCopy.setCautionNotificationGroupId(alert.getCautionNotificationGroupId());
        alertCopy.setCautionPositiveNotificationGroupId(alert.getCautionPositiveNotificationGroupId());
        alertCopy.setCautionOperator(alert.getCautionOperator());
        alertCopy.setCautionCombination(alert.getCautionCombination());
        alertCopy.setCautionCombinationCount(alert.getCautionCombinationCount());
        alertCopy.setCautionThreshold(alert.getCautionThreshold());
        alertCopy.setCautionWindowDuration(alert.getCautionWindowDuration());
        alertCopy.setCautionWindowDurationTimeUnit(alert.getCautionWindowDurationTimeUnit());
        alertCopy.setCautionStopTrackingAfter(alert.getCautionStopTrackingAfter());
        alertCopy.setCautionStopTrackingAfterTimeUnit(alert.getCautionStopTrackingAfterTimeUnit());
        alertCopy.setCautionMinimumSampleCount(alert.getCautionMinimumSampleCount());
        alertCopy.setIsCautionAlertActive(alert.isCautionAlertActive());
        if (alert.getCautionAlertLastSentTimestamp() == null) alertCopy.setCautionAlertLastSentTimestamp(null);
        else alertCopy.setCautionAlertLastSentTimestamp(new Timestamp(alert.getCautionAlertLastSentTimestamp().getTime()));
        alertCopy.setIsCautionAlertAcknowledged(alert.isCautionAlertAcknowledged());
        alertCopy.setCautionActiveAlertsSet(alert.getCautionActiveAlertsSet());
        alertCopy.setCautionFirstActiveAt(alert.getCautionFirstActiveAt());
        
        alertCopy.setDangerNotificationGroupId(alert.getDangerNotificationGroupId());
        alertCopy.setDangerPositiveNotificationGroupId(alert.getDangerPositiveNotificationGroupId());
        alertCopy.setDangerOperator(alert.getDangerOperator());
        alertCopy.setDangerCombination(alert.getDangerCombination());
        alertCopy.setDangerCombinationCount(alert.getDangerCombinationCount());
        alertCopy.setDangerThreshold(alert.getDangerThreshold());
        alertCopy.setDangerWindowDuration(alert.getDangerWindowDuration());
        alertCopy.setDangerWindowDurationTimeUnit(alert.getDangerWindowDurationTimeUnit());
        alertCopy.setDangerStopTrackingAfter(alert.getDangerStopTrackingAfter());
        alertCopy.setDangerStopTrackingAfterTimeUnit(alert.getDangerStopTrackingAfterTimeUnit());
        alertCopy.setDangerMinimumSampleCount(alert.getDangerMinimumSampleCount());
        alertCopy.setIsDangerAlertActive(alert.isDangerAlertActive());
        if (alert.getDangerAlertLastSentTimestamp() == null) alertCopy.setDangerAlertLastSentTimestamp(null);
        else alertCopy.setDangerAlertLastSentTimestamp(new Timestamp(alert.getDangerAlertLastSentTimestamp().getTime()));
        alertCopy.setIsDangerAlertAcknowledged(alert.isDangerAlertAcknowledged());
        alertCopy.setDangerActiveAlertsSet(alert.getDangerActiveAlertsSet());
        alertCopy.setDangerFirstActiveAt(alert.getDangerFirstActiveAt());

        return alertCopy;
    }
    
    @Override
    public boolean isEqual(Alert alert) {
       
        if (alert == null) return false;
        if (alert == this) return true;
        if (alert.getClass() != getClass()) return false;
        
        boolean isCautionThresholdValueEqual = MathUtilities.areBigDecimalsNumericallyEqual(cautionThreshold_, alert.getCautionThreshold());
        boolean isDangerThresholdValueEqual = MathUtilities.areBigDecimalsNumericallyEqual(dangerThreshold_, alert.getDangerThreshold());
        
        return new EqualsBuilder()
                .append(id_, alert.getId())
                .append(name_, alert.getName())
                .append(uppercaseName_, alert.getUppercaseName())
                .append(description_, alert.getDescription())
                .append(metricGroupId_, alert.getMetricGroupId())
                .append(isEnabled_, alert.isEnabled())
                .append(isCautionEnabled_, alert.isCautionEnabled())
                .append(isDangerEnabled_, alert.isDangerEnabled())
                .append(alertType_, alert.getAlertType())
                .append(alertOnPositive_, alert.isAlertOnPositive())
                .append(allowResendAlert_, alert.isAllowResendAlert())
                .append(resendAlertEvery_, alert.getResendAlertEvery())
                .append(resendAlertEveryTimeUnit_, alert.getResendAlertEveryTimeUnit())
                .append(cautionNotificationGroupId_, alert.getCautionNotificationGroupId())
                .append(cautionPositiveNotificationGroupId_, alert.getCautionPositiveNotificationGroupId())
                .append(cautionOperator_, alert.getCautionOperator())
                .append(cautionCombination_, alert.getCautionCombination())
                .append(cautionCombinationCount_, alert.getCautionCombinationCount())
                .append(isCautionThresholdValueEqual, true)
                .append(cautionWindowDuration_, alert.getCautionWindowDuration())
                .append(cautionWindowDurationTimeUnit_, alert.getCautionWindowDurationTimeUnit())
                .append(cautionStopTrackingAfter_, alert.getCautionStopTrackingAfter())
                .append(cautionStopTrackingAfterTimeUnit_, alert.getCautionStopTrackingAfterTimeUnit())
                .append(cautionMinimumSampleCount_, alert.getCautionMinimumSampleCount())
                .append(isCautionAlertActive_, alert.isCautionAlertActive())
                .append(cautionAlertLastSentTimestamp_, alert.getCautionAlertLastSentTimestamp())
                .append(isCautionAlertAcknowledged_, alert.isCautionAlertAcknowledged())
                .append(cautionActiveAlertsSet_, alert.getCautionActiveAlertsSet())
                .append(cautionFirstActiveAt_, alert.getCautionFirstActiveAt())
                .append(dangerNotificationGroupId_, alert.getDangerNotificationGroupId())
                .append(dangerPositiveNotificationGroupId_, alert.getDangerPositiveNotificationGroupId())
                .append(dangerOperator_, alert.getDangerOperator())
                .append(dangerCombination_, alert.getDangerCombination())
                .append(dangerCombinationCount_, alert.getDangerCombinationCount())
                .append(isDangerThresholdValueEqual, true)
                .append(dangerWindowDuration_, alert.getDangerWindowDuration())
                .append(dangerWindowDurationTimeUnit_, alert.getDangerWindowDurationTimeUnit())
                .append(dangerStopTrackingAfter_, alert.getDangerStopTrackingAfter())
                .append(dangerStopTrackingAfterTimeUnit_, alert.getDangerStopTrackingAfterTimeUnit())
                .append(dangerMinimumSampleCount_, alert.getDangerMinimumSampleCount())
                .append(isDangerAlertActive_, alert.isDangerAlertActive())
                .append(dangerAlertLastSentTimestamp_, alert.getDangerAlertLastSentTimestamp())
                .append(isDangerAlertAcknowledged_, alert.isDangerAlertAcknowledged())
                .append(dangerActiveAlertsSet_, alert.getDangerActiveAlertsSet())
                .append(dangerFirstActiveAt_, alert.getDangerFirstActiveAt())
                .isEquals();
    }
    
    /*
    If the caution criteria 'core' fields in 'this' alert are same as the comparison alert, then return true. 
    Caution criteria 'core' fields are considered to be any field that would be worth resetting an alert's status if the field changed.
    For example, the triggered status of an alert is no longer valid if the danger-operator changes. This makes threshold a 'core' criteria field.
    */
    public boolean isCautionCriteriaEqual(Alert alert) {
       
        if (alert == null) return false;
        if (alert == this) return true;
        if (alert.getClass() != getClass()) return false;
        
        boolean isCautionThresholdValueEqual = MathUtilities.areBigDecimalsNumericallyEqual(cautionThreshold_, alert.getCautionThreshold());
        
        return new EqualsBuilder()
                .append(id_, alert.getId())
                .append(metricGroupId_, alert.getMetricGroupId())
                .append(isEnabled_, alert.isEnabled())
                .append(isCautionEnabled_, alert.isCautionEnabled())
                .append(alertType_, alert.getAlertType())
                .append(cautionOperator_, alert.getCautionOperator())
                .append(cautionCombination_, alert.getCautionCombination())
                .append(cautionCombinationCount_, alert.getCautionCombinationCount())
                .append(isCautionThresholdValueEqual, true)
                .append(cautionWindowDuration_, alert.getCautionWindowDuration())
                .append(cautionWindowDurationTimeUnit_, alert.getCautionWindowDurationTimeUnit())
                .append(cautionStopTrackingAfter_, alert.getCautionStopTrackingAfter())
                .append(cautionStopTrackingAfterTimeUnit_, alert.getCautionStopTrackingAfterTimeUnit())
                .append(cautionMinimumSampleCount_, alert.getCautionMinimumSampleCount())
                .isEquals();
    }
    
    /*
    If the danger criteria 'core' fields in 'this' alert are same as the comparison alert, then return true. 
    Danger criteria 'core' fields are considered to be any field that would be worth resetting an alert's status if the field changed.
    For example, the triggered status of an alert is no longer valid if the danger-operator changes. This makes threshold a 'core' criteria field.
    */
    public boolean isDangerCriteriaEqual(Alert alert) {
       
        if (alert == null) return false;
        if (alert == this) return true;
        if (alert.getClass() != getClass()) return false;
        
        boolean isDangerThresholdValueEqual = MathUtilities.areBigDecimalsNumericallyEqual(dangerThreshold_, alert.getDangerThreshold());
        
        return new EqualsBuilder()
                .append(id_, alert.getId())
                .append(metricGroupId_, alert.getMetricGroupId())
                .append(isEnabled_, alert.isEnabled())
                .append(isDangerEnabled_, alert.isDangerEnabled())
                .append(alertType_, alert.getAlertType())
                .append(dangerOperator_, alert.getDangerOperator())
                .append(dangerCombination_, alert.getDangerCombination())
                .append(dangerCombinationCount_, alert.getDangerCombinationCount())
                .append(isDangerThresholdValueEqual, true)
                .append(dangerWindowDuration_, alert.getDangerWindowDuration())
                .append(dangerWindowDurationTimeUnit_, alert.getDangerWindowDurationTimeUnit())
                .append(dangerStopTrackingAfter_, alert.getDangerStopTrackingAfter())
                .append(dangerStopTrackingAfterTimeUnit_, alert.getDangerStopTrackingAfterTimeUnit())
                .append(dangerMinimumSampleCount_, alert.getDangerMinimumSampleCount())
                .isEquals();
    }
    
    /*
    Copies all caution 'metadata' fields from 'this' alert into 'alertToModify'.
    'Metadata' fields are fields are not visable/settable directly via user-input
    */
    public Alert copyCautionMetadataFields(Alert alertToModify) {
        
        if (alertToModify == null) {
            return null;
        }
        
        alertToModify.setIsCautionAlertActive(isCautionAlertActive_);       
        alertToModify.setCautionAlertLastSentTimestamp(getCautionAlertLastSentTimestamp()); 
        alertToModify.setCautionActiveAlertsSet(cautionActiveAlertsSet_); 
        alertToModify.setCautionFirstActiveAt(getCautionFirstActiveAt()); 

        return alertToModify;
    }
    
    /*
    Copies all danger 'metadata' fields from 'this' alert into 'alertToModify'.
    'Metadata' fields are fields do not have a direct effect on the criteria of the alert, and aren't set via the user-interface
    */
    public Alert copyDangerMetadataFields(Alert alertToModify) {
        
        if (alertToModify == null) {
            return null;
        }

        alertToModify.setIsDangerAlertActive(isDangerAlertActive_);       
        alertToModify.setDangerAlertLastSentTimestamp(getDangerAlertLastSentTimestamp()); 
        alertToModify.setDangerActiveAlertsSet(dangerActiveAlertsSet_); 
        alertToModify.setDangerFirstActiveAt(getDangerFirstActiveAt()); 

        return alertToModify;
    }
    
    public Long getLongestWindowDuration() {
        
        if ((cautionWindowDuration_ == null) && (dangerWindowDuration_ == null)) return null;
        if ((cautionWindowDuration_ != null) && (dangerWindowDuration_ == null)) return cautionWindowDuration_;
        if ((cautionWindowDuration_ == null) && (dangerWindowDuration_ != null)) return dangerWindowDuration_;
 
        if (cautionWindowDuration_ > dangerWindowDuration_) {
            return cautionWindowDuration_;
        }
        else {
            return dangerWindowDuration_;
        }
        
    }
       
    public boolean isCautionAlertCriteriaValid() {
        
        if (alertType_ == null) return false;
        
        if (alertType_ == TYPE_AVAILABILITY) {
            if (!isValid_CautionWindowDuration()) return false;
            if (!isValid_CautionStopTrackingAfter()) return false;
        }
        else if (alertType_ == TYPE_THRESHOLD) {
            if (!isValid_CautionOperation()) return false;
            if (!isValid_CautionCombination()) return false;
            if (getCautionThreshold() == null) return false;
            if (!isValid_CautionWindowDuration()) return false;
            if (!isValid_CautionMinimumSampleCount()) return false;
        }
        
        return true;
    }
    
    public boolean isDangerAlertCriteriaValid() {
        
        if (alertType_ == null) return false;
        
        if (alertType_ == TYPE_AVAILABILITY) {
            if (!isValid_DangerWindowDuration()) return false;
            if (!isValid_DangerStopTrackingAfter()) return false;
        }
        else if (alertType_ == TYPE_THRESHOLD) {
            if (!isValid_DangerOperation()) return false;
            if (!isValid_DangerCombination()) return false;
            if (getDangerThreshold() == null) return false;
            if (!isValid_DangerWindowDuration()) return false;
            if (!isValid_DangerMinimumSampleCount()) return false;
        }
        
        return true;
    }
    
    public boolean isValid_CautionOperation() {
        
        if (cautionOperator_ == null) {
            return false;
        } 
        
        return (cautionOperator_ >= 1) && (cautionOperator_ <= 5);
    }
    
    public boolean isValid_DangerOperation() {
        
        if (dangerOperator_ == null) {
            return false;
        } 
        
        return (dangerOperator_ >= 1) && (dangerOperator_ <= 5);
    }
    
    public boolean isValid_CautionCombination() {
        
        if (cautionCombination_ == null) {
            return false;
        } 
        
        if ((cautionCombination_ >= 101) && (cautionCombination_ <= 106) && (cautionCombination_ != 104)) {
            
            if ((Objects.equals(cautionCombination_, COMBINATION_AT_LEAST_COUNT)) || (Objects.equals(cautionCombination_, COMBINATION_AT_MOST_COUNT))) {
                if ((cautionCombinationCount_ == null) || (cautionCombinationCount_ < 0)) {
                    return false;
                }
            }
            
            return true;
        }
        
        return false;
    }
    
    public boolean isValid_DangerCombination() {
        
        if (dangerCombination_ == null) {
            return false;
        } 
        
        if ((dangerCombination_ >= 101) && (dangerCombination_ <= 106) && (dangerCombination_ != 104)) {
            
            if ((Objects.equals(dangerCombination_, COMBINATION_AT_LEAST_COUNT)) || (Objects.equals(dangerCombination_, COMBINATION_AT_MOST_COUNT))) {
                if ((dangerCombinationCount_ == null) || (dangerCombinationCount_ < 0)) {
                    return false;
                }
            }
            
            return true;
        }
        
        return false;
    }
    
    public boolean isValid_CautionWindowDuration() {
        
        if (cautionWindowDuration_ == null) {
            return false;
        } 
        
        return cautionWindowDuration_ >= 1;
    }
    
    public boolean isValid_CautionStopTrackingAfter() {
        
        if (cautionStopTrackingAfter_ == null) {
            return false;
        } 
        
        return cautionStopTrackingAfter_ >= 1;
    }
    
    public boolean isValid_DangerWindowDuration() {
        
        if (dangerWindowDuration_ == null) {
            return false;
        } 
        
        return dangerWindowDuration_ >= 1;
    }
    
    public boolean isValid_DangerStopTrackingAfter() {
        
        if (dangerStopTrackingAfter_ == null) {
            return false;
        } 
        
        return dangerStopTrackingAfter_ >= 1;
    }
    
    public boolean isValid_CautionMinimumSampleCount() {
        
        if (cautionMinimumSampleCount_ == null) {
            return false;
        } 
        
        return cautionMinimumSampleCount_ >= 1;
    }
    
    public boolean isValid_DangerMinimumSampleCount() {
        
        if (dangerMinimumSampleCount_ == null) {
            return false;
        } 
        
        return dangerMinimumSampleCount_ >= 1;
    }
    
    public String getOperatorString(int alertLevel, boolean includeSymbol, boolean includeEnglish) {
        
        if ((alertLevel == Alert.CAUTION) && (cautionOperator_ == null)) return null;
        if ((alertLevel == Alert.DANGER) && (dangerOperator_ == null)) return null;
        
        int operator = -1;
        if (alertLevel == Alert.CAUTION) operator = cautionOperator_;
        else if (alertLevel == Alert.DANGER) operator = dangerOperator_;
        
        if (includeSymbol && includeEnglish) {
            if (operator == OPERATOR_GREATER) return "> (greater than)";
            else if (operator == OPERATOR_GREATER_EQUALS) return ">= (greater than or equal to)";
            else if (operator == OPERATOR_LESS) return "< (less than)";
            else if (operator == OPERATOR_LESS_EQUALS) return "<= (less than or equal to)";
            else if (operator == OPERATOR_EQUALS) return "= (equal to)";
            else logger.warn("Unrecognized operator code");
        }
        else if (includeSymbol) {
            if (operator == OPERATOR_GREATER) return ">";
            else if (operator == OPERATOR_GREATER_EQUALS) return ">=";
            else if (operator == OPERATOR_LESS) return "<";
            else if (operator == OPERATOR_LESS_EQUALS) return "<=";
            else if (operator == OPERATOR_EQUALS) return "=";
            else logger.warn("Unrecognized operator code");
        }
        else if (includeEnglish) {
            if (operator == OPERATOR_GREATER) return "greater than";
            else if (operator == OPERATOR_GREATER_EQUALS) return "greater than or equal to";
            else if (operator == OPERATOR_LESS) return "less than";
            else if (operator == OPERATOR_LESS_EQUALS) return "less than or equal to";
            else if (operator == OPERATOR_EQUALS) return "equal to";
            else logger.warn("Unrecognized operator code");
        }
        
        return null;
    }
    
    public static Integer getOperatorCodeFromOperatorString(String operator) {
        
        if ((operator == null) || operator.isEmpty()) {
            return null;
        }
                
        if (operator.equals(">") || operator.contains("(greater than)")) return OPERATOR_GREATER;
        else if (operator.equals(">=") || operator.contains("(greater than or equal to)")) return OPERATOR_GREATER_EQUALS;
        else if (operator.equals("<") || operator.contains("(less than)")) return OPERATOR_LESS;
        else if (operator.equals("<=") || operator.contains("(less than or equal to)")) return OPERATOR_LESS_EQUALS;
        else if (operator.equals("=") || operator.contains("(equal to)")) return OPERATOR_EQUALS;
        else logger.warn("Unrecognized operator string");
        
        return null;
    }
    
    public String getCombinationString(int alertLevel) {
        
        if ((alertLevel == Alert.CAUTION) && (cautionCombination_ == null)) return null;
        if ((alertLevel == Alert.DANGER) && (dangerCombination_ == null)) return null;
        
        int combination = -1;
        if (alertLevel == Alert.CAUTION) combination = cautionCombination_;
        else if (alertLevel == Alert.DANGER) combination = dangerCombination_;
        
        if (combination == COMBINATION_ANY) return "Any";
        else if (combination == COMBINATION_ALL) return "All";
        else if (combination == COMBINATION_AVERAGE) return "Average";
        else if (combination == COMBINATION_AT_MOST_COUNT) return "At most";
        else if (combination == COMBINATION_AT_LEAST_COUNT) return "At least";
        else logger.warn("Unrecognized combination code");
         
        return null;
    }
    
    public static Integer getCombinationCodeFromString(String combination) {
        
        if ((combination == null) || combination.isEmpty()) {
            return null;
        }
        
        if (combination.equalsIgnoreCase("Any")) return COMBINATION_ANY;
        else if (combination.equalsIgnoreCase("All")) return COMBINATION_ALL;
        else if (combination.equalsIgnoreCase("Average")) return COMBINATION_AVERAGE;
        else if (combination.equalsIgnoreCase("At most")) return COMBINATION_AT_MOST_COUNT;
        else if (combination.equalsIgnoreCase("At least")) return COMBINATION_AT_LEAST_COUNT;
        else logger.warn("Unrecognized combination string");
        
        return null;
    }
    
    public static String getMetricValueString_WithLabel(int alertLevel, Alert alert, BigDecimal metricValue) {
        
        if ((alert == null) || (metricValue == null) || (alert.getAlertType() == null)) {
            return null;
        }
        
        String outputString = null;

        if (alert.getAlertType() == Alert.TYPE_THRESHOLD) {
            int combination = -1;
            if (alertLevel == Alert.CAUTION) combination = alert.getCautionCombination();
            else if (alertLevel == Alert.DANGER) combination = alert.getDangerCombination();
            
            String metricValueString = metricValue.stripTrailingZeros().toPlainString();
            
            if (Alert.COMBINATION_ALL == combination) outputString = metricValueString + " (recent value)";
            else if (Alert.COMBINATION_ANY == combination) outputString = metricValueString + " (recent value)";
            else if (Alert.COMBINATION_AVERAGE == combination) outputString = metricValueString + " (avg value)";
            else if (Alert.COMBINATION_AT_LEAST_COUNT == combination) outputString = metricValueString + " (count)";
            else if (Alert.COMBINATION_AT_MOST_COUNT == combination) outputString = metricValueString + " (count)";
            else logger.warn("Unrecognized combination code");
        }
        else if (alert.getAlertType() == Alert.TYPE_AVAILABILITY) {
            BigDecimal metricValue_Seconds = metricValue.divide(new BigDecimal(1000));
            String metricValueString = metricValue_Seconds.stripTrailingZeros().toPlainString();
            outputString = metricValueString + " (seconds since last data point received)";
        }
        
        return outputString;
    }
    
    public String getHumanReadable_AlertCriteria_MinimumSampleCount(int alertLevel) {
        
        if ((alertLevel == Alert.CAUTION) && (getCautionMinimumSampleCount() == null)) return null;
        else if ((alertLevel == Alert.DANGER) && (getDangerMinimumSampleCount() == null)) return null;
        else if ((alertLevel != Alert.CAUTION) && (alertLevel != Alert.DANGER)) return null;
        
        if (alertLevel == Alert.CAUTION) return "A minimum of " + getCautionMinimumSampleCount() + " sample(s)";
        else if (alertLevel == Alert.DANGER) return "A minimum of " + getDangerMinimumSampleCount() + " sample(s)";
        else return null;
    }
    
    public String getHumanReadable_AlertCriteria_AvailabilityCriteria(int alertLevel) {
        
        if ((alertLevel != Alert.CAUTION) && (alertLevel != Alert.DANGER)) {
            return null;
        }
        
        try {
            if (alertLevel == Alert.CAUTION) {
                if ((getCautionWindowDuration() == null) || (getCautionWindowDurationTimeUnit() == null)) return null;
                
                BigDecimal cautionWindowDuration = DatabaseObjectCommon.getValueForTimeFromMilliseconds(getCautionWindowDuration(), getCautionWindowDurationTimeUnit());
                String cautionWindowDurationTimeUnit = "";
                if (getCautionWindowDurationTimeUnit() != null) cautionWindowDurationTimeUnit = DatabaseObjectCommon.getTimeUnitStringFromCode(getCautionWindowDurationTimeUnit(), true);
                
                StringBuilder humanReadableAvailabilityCriteria = new StringBuilder();
                humanReadableAvailabilityCriteria.append("No new data points were received during the last ")
                        .append(cautionWindowDuration.stripTrailingZeros().toPlainString())
                        .append(" ").append(cautionWindowDurationTimeUnit);
                
                return humanReadableAvailabilityCriteria.toString();
            }
            else if (alertLevel == Alert.DANGER) {
                if ((getDangerWindowDuration() == null) || (getDangerWindowDurationTimeUnit() == null)) return null;
                
                BigDecimal dangerWindowDuration = DatabaseObjectCommon.getValueForTimeFromMilliseconds(getDangerWindowDuration(), getDangerWindowDurationTimeUnit());
                String dangerWindowDurationTimeUnit = "";
                if (getDangerWindowDurationTimeUnit() != null) dangerWindowDurationTimeUnit = DatabaseObjectCommon.getTimeUnitStringFromCode(getDangerWindowDurationTimeUnit(), true);
                
                StringBuilder humanReadableAvailabilityCriteria = new StringBuilder();
                humanReadableAvailabilityCriteria.append("No new data points were received during the last ")
                        .append(dangerWindowDuration.stripTrailingZeros().toPlainString())
                        .append(" ").append(dangerWindowDurationTimeUnit);
                
                return humanReadableAvailabilityCriteria.toString();
            }
            else return null;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        
    }
    
    public String getHumanReadable_AlertCriteria_ThresholdCriteria(int alertLevel) {
        
        if ((alertLevel != Alert.CAUTION) && (alertLevel != Alert.DANGER)) {
            return null;
        }
        
        try {
            if (alertLevel == Alert.CAUTION) {
                if ((getCautionWindowDuration() == null) || (getCautionWindowDurationTimeUnit() == null) || (getCautionThreshold() == null) || (getCautionOperator() == null)) return null;
                
                BigDecimal cautionWindowDuration = DatabaseObjectCommon.getValueForTimeFromMilliseconds(getCautionWindowDuration(), getCautionWindowDurationTimeUnit());
                String cautionWindowDurationTimeUnit = "";
                if (getCautionWindowDurationTimeUnit() != null) cautionWindowDurationTimeUnit = DatabaseObjectCommon.getTimeUnitStringFromCode(getCautionWindowDurationTimeUnit(), true);

                StringBuilder humanReadableThresholdCriteria = new StringBuilder();
                humanReadableThresholdCriteria.append(getHumanReadable_ThresholdCriteria_Combination(Alert.CAUTION)).append(" ").append(getOperatorString(Alert.CAUTION, false, true))
                    .append(" ").append(getCautionThreshold().stripTrailingZeros().toPlainString())
                    .append(" during the last ").append(cautionWindowDuration.stripTrailingZeros().toPlainString()).append(" ").append(cautionWindowDurationTimeUnit);

                return humanReadableThresholdCriteria.toString();
            }
            else if (alertLevel == Alert.DANGER) {
                if ((getDangerWindowDuration() == null) || (getDangerWindowDurationTimeUnit() == null) || (getDangerThreshold() == null) || (getDangerOperator() == null)) return null;
                
                BigDecimal dangerWindowDuration = DatabaseObjectCommon.getValueForTimeFromMilliseconds(getDangerWindowDuration(), getDangerWindowDurationTimeUnit());
                String dangerWindowDurationTimeUnit = "";
                if (getDangerWindowDurationTimeUnit() != null) dangerWindowDurationTimeUnit = DatabaseObjectCommon.getTimeUnitStringFromCode(getDangerWindowDurationTimeUnit(), true);

                StringBuilder humanReadableThresholdCriteria = new StringBuilder();
                humanReadableThresholdCriteria.append(getHumanReadable_ThresholdCriteria_Combination(Alert.DANGER)).append(" ").append(getOperatorString(Alert.DANGER, false, true))
                    .append(" ").append(getDangerThreshold().stripTrailingZeros().toPlainString())
                    .append(" during the last ").append(dangerWindowDuration.stripTrailingZeros().toPlainString()).append(" ").append(dangerWindowDurationTimeUnit);

                return humanReadableThresholdCriteria.toString();
            }
            else return null;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        
    }

    private String getHumanReadable_ThresholdCriteria_Combination(int alertLevel) {
        
        if ((alertLevel != Alert.CAUTION) && (alertLevel != Alert.DANGER)) {
            return null;
        }

        Integer combination = null;
        if (alertLevel == Alert.CAUTION) combination = getCautionCombination();
        else if (alertLevel == Alert.DANGER) combination = getDangerCombination();
        
        Integer combinationCount = null;
        if (alertLevel == Alert.CAUTION) combinationCount = getCautionCombinationCount();
        else if (alertLevel == Alert.DANGER) combinationCount = getDangerCombinationCount();
        
        if (combination != null) {
            if (Objects.equals(combination, Alert.COMBINATION_ANY)) return "Any metric value was";
            else if (Objects.equals(combination, Alert.COMBINATION_ALL)) return "All metric values were";
            else if (Objects.equals(combination, Alert.COMBINATION_AVERAGE)) return "The average metric value was";
            else if (Objects.equals(combination, Alert.COMBINATION_AT_MOST_COUNT) && (combinationCount != null)) return "At most " + combinationCount + " metric values were";
            else if (Objects.equals(combination, Alert.COMBINATION_AT_LEAST_COUNT) && (combinationCount != null)) return "At least " + combinationCount + " metric values were";
            else return null;
        }
        else return null;
    }

    public String getHumanReadable_AmountOfTimeAlertIsTriggered(int alertLevel, Calendar currentDateAndTime) {
        
        if ((alertLevel != Alert.CAUTION) && (alertLevel != Alert.DANGER)) return null;
        if (currentDateAndTime == null) return null;
        
        Long secondsBetweenNowAndFirstAlerted = null;
        if ((alertLevel == Alert.CAUTION) && (getCautionFirstActiveAt() != null)) secondsBetweenNowAndFirstAlerted = (long) ((currentDateAndTime.getTimeInMillis() - getCautionFirstActiveAt().getTime()) / 1000);
        else if ((alertLevel == Alert.DANGER) && (getDangerFirstActiveAt() != null)) secondsBetweenNowAndFirstAlerted = (long) ((currentDateAndTime.getTimeInMillis() - getDangerFirstActiveAt().getTime()) / 1000);

        if (secondsBetweenNowAndFirstAlerted != null) {
            StringBuilder alertTriggeredAt = new StringBuilder();

            long days = TimeUnit.SECONDS.toDays(secondsBetweenNowAndFirstAlerted);        
            long hours = TimeUnit.SECONDS.toHours(secondsBetweenNowAndFirstAlerted) - (days * 24);
            long minutes = TimeUnit.SECONDS.toMinutes(secondsBetweenNowAndFirstAlerted) - (TimeUnit.SECONDS.toHours(secondsBetweenNowAndFirstAlerted) * 60);
            long seconds = TimeUnit.SECONDS.toSeconds(secondsBetweenNowAndFirstAlerted) - (TimeUnit.SECONDS.toMinutes(secondsBetweenNowAndFirstAlerted) * 60);

            String daysString = "";
            if (days == 1) daysString = days + " day, ";
            else if (days > 1) daysString = days + " days, ";
            alertTriggeredAt.append(daysString);

            String hoursString = "";
            if (hours == 1) hoursString = hours + " hour, ";
            else if ((hours > 1) || ((alertTriggeredAt.length() > 0) && (hours == 0))) hoursString = hours + " hours, ";
            alertTriggeredAt.append(hoursString);

            String minutesString = "";
            if (minutes == 1) minutesString = minutes + " minute, ";
            else if ((minutes > 1) || ((alertTriggeredAt.length() > 0) && (minutes == 0))) minutesString = minutes + " minutes, ";
            alertTriggeredAt.append(minutesString);

            String secondsString = "";
            if (seconds == 1) secondsString = seconds + " second";
            else if ((seconds > 1) || (seconds == 0)) secondsString = seconds + " seconds";
            alertTriggeredAt.append(secondsString);

            return alertTriggeredAt.toString();
        }
        else {
            return null;
        }
    }
    
    public static JsonObject getJsonObject_ApiFriendly(Alert alert) {
        return getJsonObject_ApiFriendly(alert, null, null, null, null, null, null);
    }
    
    public static JsonObject getJsonObject_ApiFriendly(Alert alert, MetricGroup metricGroup, List<MetricGroupTag> metricGroupTags,
            NotificationGroup cautionNotificationGroup, NotificationGroup cautionPositiveNotificationGroup,
            NotificationGroup dangerNotificationGroup, NotificationGroup  dangerPositiveNotificationGroup) {
        
        if (alert == null) {
            return null;
        }

        try {
            Alert alert_Local = Alert.copy(alert);
            
            // ensures that alert acknowledgement statuses are output
            if ((alert_Local.isDangerAlertActive() != null) && alert_Local.isDangerAlertActive() && (alert_Local.isDangerAlertAcknowledged() == null)) alert_Local.setIsDangerAlertAcknowledged(false);
            if ((alert_Local.isCautionAlertActive() != null) && alert_Local.isCautionAlertActive() && (alert_Local.isCautionAlertAcknowledged() == null)) alert_Local.setIsCautionAlertAcknowledged(false);
            
            Gson alert_Gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();   
            JsonElement alert_JsonElement = alert_Gson.toJsonTree(alert_Local);
            JsonObject jsonObject = new Gson().toJsonTree(alert_JsonElement).getAsJsonObject();
            String currentFieldToAlter;
            JsonElement currentField_JsonElement;

            if ((metricGroup != null) && (metricGroup.getId() != null) && (alert_Local.getMetricGroupId() != null) && (metricGroup.getId().intValue() == alert_Local.getMetricGroupId().intValue())) {
                jsonObject.addProperty("metric_group_name", metricGroup.getName());
            }
            else if ((metricGroup != null) && (metricGroup.getId() != null) && (alert_Local.getMetricGroupId() != null)) {
                logger.error("'Metric Group Id' from the 'metricGroup' object must match the Alert's 'Metric Group Id'");
            }
            
            JsonArray metricGroupTags_JsonArray = new JsonArray();
            if ((metricGroupTags != null) && !metricGroupTags.isEmpty()) {
                for (MetricGroupTag metricGroupTag : metricGroupTags) {
                    if ((metricGroupTag.getTag() != null) && (metricGroupTag.getMetricGroupId() != null) && (alert_Local.getMetricGroupId() != null) && 
                            (metricGroupTag.getMetricGroupId().intValue() == alert_Local.getMetricGroupId().intValue())) {
                        metricGroupTags_JsonArray.add(metricGroupTag.getTag());
                    }
                }
            }
            jsonObject.add("metric_group_tags", metricGroupTags_JsonArray);
            
            if ((cautionNotificationGroup != null) && (cautionNotificationGroup.getId() != null) && (alert_Local.getCautionNotificationGroupId() != null) 
                    && (cautionNotificationGroup.getId().intValue() == alert_Local.getCautionNotificationGroupId().intValue())) {
                jsonObject.addProperty("caution_notification_group_name", cautionNotificationGroup.getName());
            }
            else if ((cautionNotificationGroup != null) && (cautionNotificationGroup.getId() != null) && (alert_Local.getCautionNotificationGroupId() != null)) {
                logger.error("'Caution Notification Group Id' from the 'cautionNotificationGroup' object must match the Alert's 'Caution Notification Group Id'");
            }
            
            if ((cautionPositiveNotificationGroup != null) && (cautionPositiveNotificationGroup.getId() != null) && (alert_Local.getCautionPositiveNotificationGroupId() != null) 
                    && (cautionPositiveNotificationGroup.getId().intValue() == alert_Local.getCautionPositiveNotificationGroupId().intValue())) {
                jsonObject.addProperty("caution_positive_notification_group_name", cautionPositiveNotificationGroup.getName());
            }
            else if ((cautionPositiveNotificationGroup != null) && (cautionPositiveNotificationGroup.getId() != null) && (alert_Local.getCautionPositiveNotificationGroupId() != null)) {
                logger.error("'Caution Positive Notification Group Id' from the 'cautionPositiveNotificationGroup' object must match the Alert's 'Caution Positive Notification Group Id'");
            }
            
            if ((dangerNotificationGroup != null) && (dangerNotificationGroup.getId() != null) && (alert_Local.getDangerNotificationGroupId() != null) 
                    && (dangerNotificationGroup.getId().intValue() == alert_Local.getDangerNotificationGroupId().intValue())) {
                jsonObject.addProperty("danger_notification_group_name", dangerNotificationGroup.getName());
            }
            else if ((dangerNotificationGroup != null) && (dangerNotificationGroup.getId() != null) && (alert_Local.getDangerNotificationGroupId() != null)) {
                logger.error("'Danger Notification Group Id' from the 'dangerNotificationGroup' object must match the Alert's 'Danger Notification Group Id'");
            }
            
            if ((dangerPositiveNotificationGroup != null) && (dangerPositiveNotificationGroup.getId() != null) && (alert_Local.getDangerPositiveNotificationGroupId() != null) 
                    && (dangerPositiveNotificationGroup.getId().intValue() == alert_Local.getDangerPositiveNotificationGroupId().intValue())) {
                jsonObject.addProperty("danger_positive_notification_group_name", dangerPositiveNotificationGroup.getName());
            }
            else if ((dangerPositiveNotificationGroup != null) && (dangerPositiveNotificationGroup.getId() != null) && (alert_Local.getDangerPositiveNotificationGroupId() != null)) {
                logger.error("'Danger Positive Notification Group Id' from the 'dangerPositiveNotificationGroup' object must match the Alert's 'Danger Positive Notification Group Id'");
            }
            
            currentFieldToAlter = "alert_type";
            if (alert_Local.getAlertType() == Alert.TYPE_THRESHOLD) {
                jsonObject.remove(currentFieldToAlter);
                jsonObject.addProperty(currentFieldToAlter, "Threshold");
            }
            else if (alert_Local.getAlertType() == Alert.TYPE_AVAILABILITY) {
                jsonObject.remove(currentFieldToAlter);
                jsonObject.addProperty(currentFieldToAlter, "Availability");
            }
            else jsonObject.remove(currentFieldToAlter);        

            DatabaseObjectCommon.getApiFriendlyJsonObject_CorrectTimesAndTimeUnits(jsonObject, "resend_alert_every", "resend_alert_every_time_unit");

            currentFieldToAlter = "caution_operator";
            currentField_JsonElement = jsonObject.get(currentFieldToAlter);
            if (currentField_JsonElement != null) {
                String operatorString = alert_Local.getOperatorString(Alert.CAUTION, true, false);
                jsonObject.remove(currentFieldToAlter);
                jsonObject.addProperty(currentFieldToAlter, operatorString);
            }

            currentFieldToAlter = "caution_combination";
            currentField_JsonElement = jsonObject.get(currentFieldToAlter);
            if (currentField_JsonElement != null) {
                String combinationString = alert_Local.getCombinationString(Alert.CAUTION);
                jsonObject.remove(currentFieldToAlter);
                jsonObject.addProperty(currentFieldToAlter, combinationString);
            }

            currentFieldToAlter = "caution_threshold";
            currentField_JsonElement = jsonObject.get(currentFieldToAlter);
            if (currentField_JsonElement != null) {
                jsonObject.remove(currentFieldToAlter);
                JsonBigDecimal jsonBigDecimal = new JsonBigDecimal(alert_Local.getCautionThreshold());
                jsonObject.addProperty(currentFieldToAlter, jsonBigDecimal);
            }

            DatabaseObjectCommon.getApiFriendlyJsonObject_CorrectTimesAndTimeUnits(jsonObject, "caution_window_duration", "caution_window_duration_time_unit");
            DatabaseObjectCommon.getApiFriendlyJsonObject_CorrectTimesAndTimeUnits(jsonObject, "caution_stop_tracking_after", "caution_stop_tracking_after_time_unit");

            currentFieldToAlter = "danger_operator";
            currentField_JsonElement = jsonObject.get(currentFieldToAlter);
            if (currentField_JsonElement != null) {
                String operatorString = alert_Local.getOperatorString(Alert.DANGER, true, false);
                jsonObject.remove(currentFieldToAlter);
                jsonObject.addProperty(currentFieldToAlter, operatorString);
            }

            currentFieldToAlter = "danger_combination";
            currentField_JsonElement = jsonObject.get(currentFieldToAlter);
            if (currentField_JsonElement != null) {
                String combinationString = alert_Local.getCombinationString(Alert.DANGER);
                jsonObject.remove(currentFieldToAlter);
                jsonObject.addProperty(currentFieldToAlter, combinationString);
            }

            currentFieldToAlter = "danger_threshold";
            currentField_JsonElement = jsonObject.get(currentFieldToAlter);
            if (currentField_JsonElement != null) {
                jsonObject.remove(currentFieldToAlter);
                JsonBigDecimal jsonBigDecimal = new JsonBigDecimal(alert_Local.getDangerThreshold());
                jsonObject.addProperty(currentFieldToAlter, jsonBigDecimal);
            }

            DatabaseObjectCommon.getApiFriendlyJsonObject_CorrectTimesAndTimeUnits(jsonObject, "danger_window_duration", "danger_window_duration_time_unit");
            DatabaseObjectCommon.getApiFriendlyJsonObject_CorrectTimesAndTimeUnits(jsonObject, "danger_stop_tracking_after", "danger_stop_tracking_after_time_unit");

            currentFieldToAlter = "allow_resend_alert";
            currentField_JsonElement = jsonObject.get(currentFieldToAlter);
            if ((currentField_JsonElement == null) || (alert_Local.isAllowResendAlert() == null) || !alert_Local.isAllowResendAlert()) {
                jsonObject.remove("resend_alert_every");
                jsonObject.remove("resend_alert_every_time_unit");
            }

            if ((alert_Local.isCautionEnabled() == null) || !alert_Local.isCautionEnabled()) {
                jsonObject.remove("caution_notification_group_id");
                jsonObject.remove("caution_positive_notification_group_id");
                jsonObject.remove("caution_minimum_sample_count");
                jsonObject.remove("caution_combination");
                jsonObject.remove("caution_window_duration");
                jsonObject.remove("caution_window_duration_time_unit");
                jsonObject.remove("caution_stop_tracking_after");
                jsonObject.remove("caution_stop_tracking_after_time_unit");
                jsonObject.remove("caution_operator");
                jsonObject.remove("caution_threshold");
                jsonObject.remove("caution_alert_active");
                jsonObject.remove("caution_alert_acknowledged_status");
                jsonObject.remove("caution_alert_last_sent_timestamp");
                jsonObject.remove("caution_first_active_at");
            }

            if ((alert_Local.isDangerEnabled() == null) || !alert_Local.isDangerEnabled()) {
                jsonObject.remove("danger_notification_group_id");
                jsonObject.remove("danger_positive_notification_group_id");
                jsonObject.remove("danger_minimum_sample_count");
                jsonObject.remove("danger_combination");
                jsonObject.remove("danger_window_duration");
                jsonObject.remove("danger_window_duration_time_unit");
                jsonObject.remove("danger_stop_tracking_after");
                jsonObject.remove("danger_stop_tracking_after_time_unit");
                jsonObject.remove("danger_operator");
                jsonObject.remove("danger_threshold");
                jsonObject.remove("danger_alert_active");
                jsonObject.remove("danger_alert_acknowledged_status");
                jsonObject.remove("danger_alert_last_sent_timestamp");
                jsonObject.remove("danger_first_active_at");
            }

            if (alert_Local.getAlertType() == Alert.TYPE_AVAILABILITY) {
                jsonObject.remove("caution_minimum_sample_count");
                jsonObject.remove("caution_combination");
                jsonObject.remove("caution_operator");
                jsonObject.remove("caution_threshold");

                jsonObject.remove("danger_minimum_sample_count");
                jsonObject.remove("danger_combination");
                jsonObject.remove("danger_operator");
                jsonObject.remove("danger_threshold");
            }

            if (alert_Local.getAlertType() == Alert.TYPE_THRESHOLD) {
                jsonObject.remove("caution_stop_tracking_after");
                jsonObject.remove("caution_stop_tracking_after_time_unit");

                jsonObject.remove("danger_stop_tracking_after");
                jsonObject.remove("danger_stop_tracking_after_time_unit");
            }        
            
            if ((alert_Local.isAlertOnPositive() != null) && !alert_Local.isAlertOnPositive()) {
                jsonObject.remove("caution_notification_group_name");
                jsonObject.remove("caution_notification_group_id");
                jsonObject.remove("caution_positive_notification_group_name");
                jsonObject.remove("caution_positive_notification_group_id");
                jsonObject.remove("danger_notification_group_name");
                jsonObject.remove("danger_notification_group_id");
                jsonObject.remove("danger_positive_notification_group_name");
                jsonObject.remove("danger_positive_notification_group_id");        
            }

            return jsonObject;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        
    }
    
    public static String getJsonString_ApiFriendly(Alert alert) {
        return getJsonString_ApiFriendly(alert, null, null, null, null, null, null);
    }
    
    public static String getJsonString_ApiFriendly(Alert alert, MetricGroup metricGroup, List<MetricGroupTag> metricGroupTags,
            NotificationGroup cautionNotificationGroup, NotificationGroup cautionPositiveNotificationGroup,
            NotificationGroup dangerNotificationGroup, NotificationGroup  dangerPositiveNotificationGroup) {
        
        if (alert == null) {
            return null;
        }
        
        try {
            JsonObject jsonObject = getJsonObject_ApiFriendly(alert, metricGroup, metricGroupTags, cautionNotificationGroup, cautionPositiveNotificationGroup, dangerNotificationGroup, dangerPositiveNotificationGroup);
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
    }
    
    public String getUppercaseName() {
        return uppercaseName_;
    }

    public void setUppercaseName(String uppercaseName) {
        this.uppercaseName_ = uppercaseName;
    }
    
    public String getDescription() {
        return description_;
    }
    
    public void setDescription(String description) {
        this.description_ = description;
    }
    
    public Integer getMetricGroupId() {
        return metricGroupId_;
    }
    
    public void setMetricGroupId(Integer metricGroupId) {
        this.metricGroupId_ = metricGroupId;
    }
    
    public Boolean isEnabled() {
        return isEnabled_;
    }
    
    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled_ = isEnabled;
    }
    
    public Boolean isCautionEnabled() {
        return isCautionEnabled_;
    }
    
    public void setIsCautionEnabled(Boolean isCautionEnabled) {
        this.isCautionEnabled_ = isCautionEnabled;
    }
    
    public Boolean isDangerEnabled() {
        return isDangerEnabled_;
    }
    
    public void setIsDangerEnabled(Boolean isDangerEnabled) {
        this.isDangerEnabled_ = isDangerEnabled;
    }
    
    public Integer getAlertType() {
        return alertType_;
    }

    public void setAlertType(Integer alertType) {
        this.alertType_ = alertType;
    }
    
    public Boolean isAlertOnPositive() {
        return alertOnPositive_;
    }
    
    public void setAlertOnPositive(Boolean alertOnPositive) {
        this.alertOnPositive_ = alertOnPositive;
    }

    public Boolean isAllowResendAlert() {
        return allowResendAlert_;
    }
    
    public void setAllowResendAlert(Boolean allowResendAlert) {
        this.allowResendAlert_ = allowResendAlert;
    }
    
    public Long getResendAlertEvery() {
        return resendAlertEvery_;
    }

    public void setResendAlertEvery(Long resendAlertEvery) {
        this.resendAlertEvery_ = resendAlertEvery;
    }

    public Integer getResendAlertEveryTimeUnit() {
        return resendAlertEveryTimeUnit_;
    }

    public void setResendAlertEveryTimeUnit(Integer resendAlertEveryTimeUnit) {
        this.resendAlertEveryTimeUnit_ = resendAlertEveryTimeUnit;
    }

    public Integer getCautionNotificationGroupId() {
        return cautionNotificationGroupId_;
    }

    public void setCautionNotificationGroupId(Integer cautionNotificationGroupId) {
        this.cautionNotificationGroupId_ = cautionNotificationGroupId;
    }

    public Integer getCautionPositiveNotificationGroupId() {
        return cautionPositiveNotificationGroupId_;
    }

    public void setCautionPositiveNotificationGroupId(Integer cautionPositiveNotificationGroupId) {
        this.cautionPositiveNotificationGroupId_ = cautionPositiveNotificationGroupId;
    }
    
    public Integer getCautionOperator() {
        return cautionOperator_;
    }
    
    public void setCautionOperator(Integer cautionOperator) {
        this.cautionOperator_ = cautionOperator;
    }
    
    public Integer getCautionCombination() {
        return cautionCombination_;
    }
    
    public void setCautionCombination(Integer cautionCombination) {
        this.cautionCombination_ = cautionCombination;
    }
    
    public Integer getCautionCombinationCount() {
        return cautionCombinationCount_;
    }

    public void setCautionCombinationCount(Integer cautionCombinationCount) {
        this.cautionCombinationCount_ = cautionCombinationCount;
    }
    
    public BigDecimal getCautionThreshold() {
        return cautionThreshold_;
    }

    public void setCautionThreshold(BigDecimal cautionThreshold) {
        this.cautionThreshold_ = cautionThreshold;
    }
    
    public Long getCautionWindowDuration() {
        return cautionWindowDuration_;
    }
    
    public void setCautionWindowDuration(Long cautionWindowDuration) {
        this.cautionWindowDuration_ = cautionWindowDuration;
    }

    public Integer getCautionWindowDurationTimeUnit() {
        return cautionWindowDurationTimeUnit_;
    }

    public void setCautionWindowDurationTimeUnit(Integer cautionWindowDurationTimeUnit) {
        this.cautionWindowDurationTimeUnit_ = cautionWindowDurationTimeUnit;
    }

    public Long getCautionStopTrackingAfter() {
        return cautionStopTrackingAfter_;
    }

    public void setCautionStopTrackingAfter(Long cautionStopTrackingAfter) {
        this.cautionStopTrackingAfter_ = cautionStopTrackingAfter;
    }

    public Integer getCautionStopTrackingAfterTimeUnit() {
        return cautionStopTrackingAfterTimeUnit_;
    }

    public void setCautionStopTrackingAfterTimeUnit(Integer cautionStopTrackingAfterTimeUnit) {
        this.cautionStopTrackingAfterTimeUnit_ = cautionStopTrackingAfterTimeUnit;
    }
    
    public Integer getCautionMinimumSampleCount() {
        return cautionMinimumSampleCount_;
    }

    public void setCautionMinimumSampleCount(Integer cautionMinimumSampleCount) {
        this.cautionMinimumSampleCount_ = cautionMinimumSampleCount;
    }
    
    public Boolean isCautionAlertActive() {
        return isCautionAlertActive_;
    }
    
    public void setIsCautionAlertActive(Boolean isCautionAlertActive) {
        this.isCautionAlertActive_ = isCautionAlertActive;
    }
    
    public Timestamp getCautionAlertLastSentTimestamp() {
        if (cautionAlertLastSentTimestamp_ == null) return null;
        else return (Timestamp) cautionAlertLastSentTimestamp_.clone();
    }

    public void setCautionAlertLastSentTimestamp(Timestamp cautionAlertLastSentTimestamp) {
        if (cautionAlertLastSentTimestamp == null) this.cautionAlertLastSentTimestamp_ = null;
        else this.cautionAlertLastSentTimestamp_ = (Timestamp) cautionAlertLastSentTimestamp.clone();
    }

    public Boolean isCautionAlertAcknowledged() {
        return isCautionAlertAcknowledged_;
    }

    public void setIsCautionAlertAcknowledged(Boolean isCautionAlertAcknowledged) {
        this.isCautionAlertAcknowledged_ = isCautionAlertAcknowledged;
    }
    
    public String getCautionActiveAlertsSet() {
        return cautionActiveAlertsSet_;
    }

    public void setCautionActiveAlertsSet(String cautionActiveAlertsSet) {
        this.cautionActiveAlertsSet_ = cautionActiveAlertsSet;
    }

    public Timestamp getCautionFirstActiveAt() {       
        if (cautionFirstActiveAt_ == null) return null;
        else return (Timestamp) cautionFirstActiveAt_.clone();
    }

    public void setCautionFirstActiveAt(Timestamp cautionFirstActiveAt) {       
        if (cautionFirstActiveAt == null) this.cautionFirstActiveAt_ = null;
        else this.cautionFirstActiveAt_ = (Timestamp) cautionFirstActiveAt.clone();
    }
    
    public Integer getDangerNotificationGroupId() {
        return dangerNotificationGroupId_;
    }

    public void setDangerNotificationGroupId(Integer dangerNotificationGroupId) {
        this.dangerNotificationGroupId_ = dangerNotificationGroupId;
    }
    
    public Integer getDangerPositiveNotificationGroupId() {
        return dangerPositiveNotificationGroupId_;
    }

    public void setDangerPositiveNotificationGroupId(Integer dangerPositiveNotificationGroupId) {
        this.dangerPositiveNotificationGroupId_ = dangerPositiveNotificationGroupId;
    }
    
    public Integer getDangerOperator() {
        return dangerOperator_;
    }
    
    public void setDangerOperator(Integer dangerOperator) {
        this.dangerOperator_ = dangerOperator;
    }
    
    public Integer getDangerCombination() {
        return dangerCombination_;
    }
    
    public void setDangerCombination(Integer dangerCombination) {
        this.dangerCombination_ = dangerCombination;
    }
    
    public Integer getDangerCombinationCount() {
        return dangerCombinationCount_;
    }

    public void setDangerCombinationCount(Integer dangerCombinationCount) {
        this.dangerCombinationCount_ = dangerCombinationCount;
    }
    
    public BigDecimal getDangerThreshold() {
        return dangerThreshold_;
    }

    public void setDangerThreshold(BigDecimal dangerThreshold) {
        this.dangerThreshold_ = dangerThreshold;
    }
    
    public Long getDangerWindowDuration() {
        return dangerWindowDuration_;
    }

    public void setDangerWindowDuration(Long dangerWindowDuration) {
        this.dangerWindowDuration_ = dangerWindowDuration;
    }

    public Integer getDangerWindowDurationTimeUnit() {
        return dangerWindowDurationTimeUnit_;
    }

    public void setDangerWindowDurationTimeUnit(Integer dangerWindowDurationTimeUnit) {
        this.dangerWindowDurationTimeUnit_ = dangerWindowDurationTimeUnit;
    }

    public Long getDangerStopTrackingAfter() {
        return dangerStopTrackingAfter_;
    }

    public void setDangerStopTrackingAfter(Long dangerStopTrackingAfter) {
        this.dangerStopTrackingAfter_ = dangerStopTrackingAfter;
    }

    public Integer getDangerStopTrackingAfterTimeUnit() {
        return dangerStopTrackingAfterTimeUnit_;
    }

    public void setDangerStopTrackingAfterTimeUnit(Integer dangerStopTrackingAfterTimeUnit) {
        this.dangerStopTrackingAfterTimeUnit_ = dangerStopTrackingAfterTimeUnit;
    }
    
    public Integer getDangerMinimumSampleCount() {
        return dangerMinimumSampleCount_;
    }

    public void setDangerMinimumSampleCount(Integer dangerMinimumSampleCount) {
        this.dangerMinimumSampleCount_ = dangerMinimumSampleCount;
    }
    
    public Boolean isDangerAlertActive() {
        return isDangerAlertActive_;
    }

    public void setIsDangerAlertActive(Boolean isDangerAlertActive) {
        this.isDangerAlertActive_ = isDangerAlertActive;
    }

    public Timestamp getDangerAlertLastSentTimestamp() {
        if (dangerAlertLastSentTimestamp_ == null) return null;
        else return (Timestamp) dangerAlertLastSentTimestamp_.clone();
    }

    public void setDangerAlertLastSentTimestamp(Timestamp dangerAlertLastSentTimestamp) {
        if (dangerAlertLastSentTimestamp == null) this.dangerAlertLastSentTimestamp_ = null;
        else this.dangerAlertLastSentTimestamp_ = (Timestamp) dangerAlertLastSentTimestamp.clone();
    }

    public Boolean isDangerAlertAcknowledged() {
        return isDangerAlertAcknowledged_;
    }

    public void setIsDangerAlertAcknowledged(Boolean isDangerAlertAcknowledged) {
        this.isDangerAlertAcknowledged_ = isDangerAlertAcknowledged;
    }

    public String getDangerActiveAlertsSet() {
        return dangerActiveAlertsSet_;
    }

    public void setDangerActiveAlertsSet(String dangerActiveAlertsSet) {
        this.dangerActiveAlertsSet_ = dangerActiveAlertsSet;
    }

    public Timestamp getDangerFirstActiveAt() {
        if (dangerFirstActiveAt_ == null) return null;
        else return (Timestamp) dangerFirstActiveAt_.clone();
    }

    public void setDangerFirstActiveAt(Timestamp dangerFirstActiveAt) {
        if (dangerFirstActiveAt == null) this.dangerFirstActiveAt_ = null;
        else this.dangerFirstActiveAt_ = (Timestamp) dangerFirstActiveAt.clone();
    }

}
