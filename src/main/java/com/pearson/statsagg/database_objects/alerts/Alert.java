package com.pearson.statsagg.database_objects.alerts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.sql.Timestamp;
import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import com.pearson.statsagg.database_objects.DatabaseObjectValidation;
import com.pearson.statsagg.database_objects.abstract_objects.AbstractAlert;
import com.pearson.statsagg.database_objects.alert_templates.AlertTemplate;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroup;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroup;
import com.pearson.statsagg.utilities.math_utils.MathUtilities;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseObject;
import com.pearson.statsagg.utilities.json_utils.JsonBigDecimal;
import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class Alert extends AbstractAlert implements DatabaseObject<Alert> {
    
    private static final Logger logger = LoggerFactory.getLogger(Alert.class.getName());
     
    @SerializedName("description") protected String description_ = null;

    @SerializedName("alert_template_id") private Integer alertTemplateId_ = null;
    @SerializedName("variable_set_id") private Integer variableSetId_ = null;
    
    @SerializedName("metric_group_id") private Integer metricGroupId_ = null;

    @SerializedName("caution_notification_group_id") private Integer cautionNotificationGroupId_ = null;
    @SerializedName("caution_positive_notification_group_id") private Integer cautionPositiveNotificationGroupId_ = null;
    @SerializedName("caution_alert_active") private Boolean isCautionAlertActive_ = null;
    @SerializedName("caution_alert_last_sent_timestamp") private Timestamp cautionAlertLastSentTimestamp_ = null;
    @SerializedName("caution_alert_acknowledged_status") private Boolean isCautionAlertAcknowledged_ = null;
    private transient String cautionActiveAlertsSet_ = null;
    @SerializedName("caution_first_active_at") private Timestamp cautionFirstActiveAt_ = null;
    
    @SerializedName("danger_notification_group_id") private Integer dangerNotificationGroupId_ = null;
    @SerializedName("danger_positive_notification_group_id") private Integer dangerPositiveNotificationGroupId_ = null;
    @SerializedName("danger_alert_active") private Boolean isDangerAlertActive_ = null;
    @SerializedName("danger_alert_last_sent_timestamp") private Timestamp dangerAlertLastSentTimestamp_ = null;
    @SerializedName("danger_alert_acknowledged_status") private Boolean isDangerAlertAcknowledged_ = null;
    private transient String dangerActiveAlertsSet_ = null;
    @SerializedName("danger_first_active_at") private Timestamp dangerFirstActiveAt_ = null;
    
    public Alert() {
        super.id_ = -1;
    }
    
    public Alert(Integer id, String name, String description, Integer alertTemplateId, Integer variableSetId, Integer metricGroupId, Boolean isEnabled, 
            Boolean isCautionEnabled, Boolean isDangerEnabled, Integer alertType, Boolean alertOnPositive, Boolean allowResendAlert, Long resendAlertEvery, 
            Integer resendAlertEveryTimeUnit, Integer cautionNotificationGroupId, Integer cautionPositiveNotificationGroupId, Integer cautionOperator, Integer cautionCombination, 
            Integer cautionCombinationCount, BigDecimal cautionThreshold, Long cautionWindowDuration, Integer cautionWindowDurationTimeUnit, 
            Long cautionStopTrackingAfter, Integer cautionStopTrackingAfterTimeUnit, Integer cautionMinimumSampleCount, Boolean isCautionAlertActive, 
            Timestamp cautionAlertLastSentTimestamp, Boolean isCautionAlertAcknowledged, String cautionActiveAlertsSet, Timestamp cautionFirstActiveAt, 
            Integer dangerNotificationGroupId, Integer dangerPositiveNotificationGroupId, Integer dangerOperator, Integer dangerCombination, 
            Integer dangerCombinationCount, BigDecimal dangerThreshold, Long dangerWindowDuration, Integer dangerWindowDurationTimeUnit, 
            Long dangerStopTrackingAfter, Integer dangerStopTrackingAfterTimeUnit, Integer dangerMinimumSampleCount, Boolean isDangerAlertActive,  
            Timestamp dangerAlertLastSentTimestamp, Boolean isDangerAlertAcknowledged, String dangerActiveAlertsSet, Timestamp dangerFirstActiveAt) {
        
        this(id, name, ((name == null) ? null : name.toUpperCase()), description, alertTemplateId, variableSetId, metricGroupId, isEnabled, 
             isCautionEnabled, isDangerEnabled, alertType, alertOnPositive, allowResendAlert, resendAlertEvery, 
             resendAlertEveryTimeUnit, cautionNotificationGroupId, cautionPositiveNotificationGroupId, cautionOperator, cautionCombination,  
             cautionCombinationCount, cautionThreshold, cautionWindowDuration, cautionWindowDurationTimeUnit, 
             cautionStopTrackingAfter, cautionStopTrackingAfterTimeUnit, cautionMinimumSampleCount, isCautionAlertActive,  
             cautionAlertLastSentTimestamp, isCautionAlertAcknowledged, cautionActiveAlertsSet, cautionFirstActiveAt, 
             dangerNotificationGroupId, dangerPositiveNotificationGroupId, dangerOperator, dangerCombination,  
             dangerCombinationCount, dangerThreshold, dangerWindowDuration, dangerWindowDurationTimeUnit, 
             dangerStopTrackingAfter, dangerStopTrackingAfterTimeUnit, dangerMinimumSampleCount, isDangerAlertActive,  
             dangerAlertLastSentTimestamp, isDangerAlertAcknowledged, dangerActiveAlertsSet, dangerFirstActiveAt);
    }

    public Alert(Integer id, String name, String uppercaseName, String description, Integer alertTemplateId, Integer variableSetId, Integer metricGroupId, Boolean isEnabled,
            Boolean isCautionEnabled, Boolean isDangerEnabled, Integer alertType, Boolean alertOnPositive, Boolean allowResendAlert, Long resendAlertEvery, 
            Integer resendAlertEveryTimeUnit, Integer cautionNotificationGroupId, Integer cautionPositiveNotificationGroupId, Integer cautionOperator, Integer cautionCombination, 
            Integer cautionCombinationCount, BigDecimal cautionThreshold, Long cautionWindowDuration, Integer cautionWindowDurationTimeUnit, 
            Long cautionStopTrackingAfter, Integer cautionStopTrackingAfterTimeUnit, Integer cautionMinimumSampleCount, Boolean isCautionAlertActive, 
            Timestamp cautionAlertLastSentTimestamp, Boolean isCautionAlertAcknowledged, String cautionActiveAlertsSet, Timestamp cautionFirstActiveAt, 
            Integer dangerNotificationGroupId, Integer dangerPositiveNotificationGroupId, Integer dangerOperator, Integer dangerCombination, 
            Integer dangerCombinationCount, BigDecimal dangerThreshold, Long dangerWindowDuration, Integer dangerWindowDurationTimeUnit, 
            Long dangerStopTrackingAfter, Integer dangerStopTrackingAfterTimeUnit, Integer dangerMinimumSampleCount, Boolean isDangerAlertActive,  
            Timestamp dangerAlertLastSentTimestamp, Boolean isDangerAlertAcknowledged, String dangerActiveAlertsSet, Timestamp dangerFirstActiveAt) {
        super.id_ = id;
        super.name_ = name;
        super.uppercaseName_ = uppercaseName;
        this.description_ = description;

        this.alertTemplateId_ = alertTemplateId;
        this.variableSetId_ = variableSetId;
        this.metricGroupId_ = metricGroupId;
        super.isEnabled_ = isEnabled;
        super.isCautionEnabled_ = isCautionEnabled;
        super.isDangerEnabled_ = isDangerEnabled;
        super.alertType_ = alertType;
        
        super.alertOnPositive_ = alertOnPositive;
        super.allowResendAlert_ = allowResendAlert;
        super.resendAlertEvery_ = resendAlertEvery;
        super.resendAlertEveryTimeUnit_ = resendAlertEveryTimeUnit;
        
        this.cautionNotificationGroupId_ = cautionNotificationGroupId;
        this.cautionPositiveNotificationGroupId_ = cautionPositiveNotificationGroupId;
        super.cautionOperator_ = cautionOperator;
        super.cautionCombination_ = cautionCombination;
        super.cautionCombinationCount_ = cautionCombinationCount;
        super.cautionThreshold_ = cautionThreshold;
        super.cautionWindowDuration_ = cautionWindowDuration;
        super.cautionWindowDurationTimeUnit_ = cautionWindowDurationTimeUnit;
        super.cautionStopTrackingAfter_ = cautionStopTrackingAfter;
        super.cautionStopTrackingAfterTimeUnit_ = cautionStopTrackingAfterTimeUnit;
        super.cautionMinimumSampleCount_ = cautionMinimumSampleCount;
        this.isCautionAlertActive_ = isCautionAlertActive;
        if (cautionAlertLastSentTimestamp == null) this.cautionAlertLastSentTimestamp_ = null;
        else this.cautionAlertLastSentTimestamp_ = (Timestamp) cautionAlertLastSentTimestamp.clone();
        this.isCautionAlertAcknowledged_ = isCautionAlertAcknowledged;
        this.cautionActiveAlertsSet_ = cautionActiveAlertsSet;
        this.cautionFirstActiveAt_ = cautionFirstActiveAt;

        this.dangerNotificationGroupId_ = dangerNotificationGroupId;
        this.dangerPositiveNotificationGroupId_ = dangerPositiveNotificationGroupId;
        super.dangerOperator_ = dangerOperator;
        super.dangerCombination_ = dangerCombination;
        super.dangerCombinationCount_ = dangerCombinationCount;
        super.dangerThreshold_ = dangerThreshold;
        super.dangerWindowDuration_ = dangerWindowDuration;
        super.dangerWindowDurationTimeUnit_ = dangerWindowDurationTimeUnit;
        super.dangerStopTrackingAfter_ = dangerStopTrackingAfter;
        super.dangerStopTrackingAfterTimeUnit_ = dangerStopTrackingAfterTimeUnit;
        super.dangerMinimumSampleCount_ = dangerMinimumSampleCount;
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
        alertCopy.setDescription(alert.getDescription());
        alertCopy.setAlertTemplateId(alert.getAlertTemplateId());
        alertCopy.setVariableSetId(alert.getVariableSetId());
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
        return isEqual(alert, true);
    }
    
    public boolean isEqual(Alert alert, boolean includeAlertStatus) {
       
        if (alert == null) return false;
        if (alert == this) return true;
        if (alert.getClass() != getClass()) return false;
        
        boolean isCautionThresholdValueEqual = MathUtilities.areBigDecimalsNumericallyEqual(cautionThreshold_, alert.getCautionThreshold());
        boolean isDangerThresholdValueEqual = MathUtilities.areBigDecimalsNumericallyEqual(dangerThreshold_, alert.getDangerThreshold());
        
        boolean isCautionAlertStatusEqual = true;
        if (includeAlertStatus) isCautionAlertStatusEqual = new EqualsBuilder()
                .append(isCautionAlertActive_, alert.isCautionAlertActive())
                .append(cautionAlertLastSentTimestamp_, alert.getCautionAlertLastSentTimestamp())
                .append(isCautionAlertAcknowledged_, alert.isCautionAlertAcknowledged())
                .append(cautionActiveAlertsSet_, alert.getCautionActiveAlertsSet())
                .append(cautionFirstActiveAt_, alert.getCautionFirstActiveAt())
                .isEquals();
        
        boolean isDangerAlertStatusEqual = true;
        if (includeAlertStatus) isDangerAlertStatusEqual = new EqualsBuilder()
                .append(isDangerAlertActive_, alert.isDangerAlertActive())
                .append(dangerAlertLastSentTimestamp_, alert.getDangerAlertLastSentTimestamp())
                .append(isDangerAlertAcknowledged_, alert.isDangerAlertAcknowledged())
                .append(dangerActiveAlertsSet_, alert.getDangerActiveAlertsSet())
                .append(dangerFirstActiveAt_, alert.getDangerFirstActiveAt())
                .isEquals();
        
        return new EqualsBuilder()
                .append(id_, alert.getId())
                .append(name_, alert.getName())
                .append(uppercaseName_, alert.getUppercaseName())
                .append(description_, alert.getDescription())
                .append(alertTemplateId_, alert.getAlertTemplateId())
                .append(variableSetId_, alert.getVariableSetId())
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
                .append(isCautionAlertStatusEqual, true)
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
                .append(isDangerAlertStatusEqual, true)
                .isEquals();
    }
    
    /*
    If the caution criteria 'core' fields in 'this' alert are same as the comparison alert, then return true. 
    Caution criteria 'core' fields are considered to be any field that would be worth resetting an alert's status if the field changed.
    For example, the triggered status of an alert is no longer valid if the danger-operator changes. This makes threshold a 'core' criteria field.
    */
    public boolean isCautionCriteriaEqual(Alert alert, boolean includeNotitificationGroups) {
       
        if (alert == null) return false;
        if (alert == this) return true;
        if (alert.getClass() != getClass()) return false;
        
        boolean isCautionNotificationGroupIdEqual = (includeNotitificationGroups) ? Objects.equals(this.cautionNotificationGroupId_, alert.getCautionNotificationGroupId()) : true;
        boolean isCautionPositiveNotificationGroupIdEqual = (includeNotitificationGroups) ? Objects.equals(this.cautionPositiveNotificationGroupId_, alert.getCautionPositiveNotificationGroupId()) : true;

        boolean isCautionThresholdValueEqual = MathUtilities.areBigDecimalsNumericallyEqual(cautionThreshold_, alert.getCautionThreshold());
        
        return new EqualsBuilder()
                .append(id_, alert.getId())
                .append(metricGroupId_, alert.getMetricGroupId())
                .append(isCautionNotificationGroupIdEqual, true)
                .append(isCautionPositiveNotificationGroupIdEqual, true)
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
    public boolean isDangerCriteriaEqual(Alert alert, boolean includeNotitificationGroups) {
       
        if (alert == null) return false;
        if (alert == this) return true;
        if (alert.getClass() != getClass()) return false;
        
        boolean isDangerNotificationGroupIdEqual = (includeNotitificationGroups) ? Objects.equals(this.dangerNotificationGroupId_, alert.getDangerNotificationGroupId()) : true;
        boolean isDangerPositiveNotificationGroupIdEqual = (includeNotitificationGroups) ? Objects.equals(this.dangerPositiveNotificationGroupId_, alert.getDangerPositiveNotificationGroupId()) : true;
        
        boolean isDangerThresholdValueEqual = MathUtilities.areBigDecimalsNumericallyEqual(dangerThreshold_, alert.getDangerThreshold());

        return new EqualsBuilder()
                .append(id_, alert.getId())
                .append(metricGroupId_, alert.getMetricGroupId())
                .append(isDangerNotificationGroupIdEqual, true)
                .append(isDangerPositiveNotificationGroupIdEqual, true)
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
    
    public void disableAndNullifyAlertStatusFields() {
        this.setIsCautionAlertActive(false);
        this.setCautionFirstActiveAt(null);
        this.setIsCautionAlertAcknowledged(null);
        this.setCautionAlertLastSentTimestamp(null);
        this.setCautionActiveAlertsSet(null);
        this.setIsDangerAlertActive(false);
        this.setDangerFirstActiveAt(null);
        this.setIsDangerAlertAcknowledged(null);
        this.setDangerAlertLastSentTimestamp(null);
        this.setDangerActiveAlertsSet(null);
    }
    
    public static DatabaseObjectValidation isValid(Alert alert) {
        if (alert == null) return new DatabaseObjectValidation(false, "Invalid alert");
        
        DatabaseObjectValidation databaseObjectValidation_CoreCriteria = alert.isCoreAlertCriteriaValid();
        if (!databaseObjectValidation_CoreCriteria.isValid()) return databaseObjectValidation_CoreCriteria;
        
        DatabaseObjectValidation databaseObjectValidation_AlertTemplateCriteria = alert.isAlertTemplateCriteriaValid();
        if (!databaseObjectValidation_AlertTemplateCriteria.isValid()) return databaseObjectValidation_AlertTemplateCriteria;
        
        DatabaseObjectValidation databaseObjectValidation_MetricGroupCriteria = alert.isMetricGroupCriteriaValid();
        if (!databaseObjectValidation_MetricGroupCriteria.isValid()) return databaseObjectValidation_MetricGroupCriteria;
        
        DatabaseObjectValidation databaseObjectValidation_CautionCriteria = alert.isCautionAlertCriteriaValid(true);
        if (!databaseObjectValidation_CautionCriteria.isValid()) return databaseObjectValidation_CautionCriteria;
        
        DatabaseObjectValidation databaseObjectValidation_DangerCriteria = alert.isDangerAlertCriteriaValid(true);
        if (!databaseObjectValidation_DangerCriteria.isValid()) return databaseObjectValidation_DangerCriteria;
        
        return new DatabaseObjectValidation(true);
    }
    
    public DatabaseObjectValidation isMetricGroupCriteriaValid() {
        if (metricGroupId_ == null) return new DatabaseObjectValidation(false, "Invalid metric group");
        return new DatabaseObjectValidation(true);
    }
    
    public DatabaseObjectValidation isAlertTemplateCriteriaValid() {
        if ((alertTemplateId_ == null) && (variableSetId_ != null)) return new DatabaseObjectValidation(false, "Invalid alert template");
        if ((alertTemplateId_ != null) && (variableSetId_ == null)) return new DatabaseObjectValidation(false, "Invalid alert template variable set");
        return new DatabaseObjectValidation(true);
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
        return getJsonObject_ApiFriendly(alert, null, null, null, null, null);
    }
    
    public static JsonObject getJsonObject_ApiFriendly(Alert alert, MetricGroup metricGroup,
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
                
                JsonArray metricGroupTags_JsonArray = new JsonArray();
                if ((metricGroup.getTags() != null) && !metricGroup.getTags().isEmpty()) {
                    for (String metricGroupTag : metricGroup.getTags()) {
                        if ((metricGroupTag == null) || metricGroupTag.isEmpty()) continue;
                        metricGroupTags_JsonArray.add(metricGroupTag);
                    }
                }
                jsonObject.add("metric_group_tags", metricGroupTags_JsonArray);
            }
            else if ((metricGroup != null) && (metricGroup.getId() != null) && (alert_Local.getMetricGroupId() != null)) {
                logger.error("'Metric Group Id' from the 'metricGroup' object must match the Alert's 'Metric Group Id'");
            }
            
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
        return getJsonString_ApiFriendly(alert, null, null, null, null, null);
    }
    
    public static String getJsonString_ApiFriendly(Alert alert, MetricGroup metricGroup, 
            NotificationGroup cautionNotificationGroup, NotificationGroup cautionPositiveNotificationGroup,
            NotificationGroup dangerNotificationGroup, NotificationGroup  dangerPositiveNotificationGroup) {
        
        if (alert == null) {
            return null;
        }
        
        try {
            JsonObject jsonObject = getJsonObject_ApiFriendly(alert, metricGroup, cautionNotificationGroup, cautionPositiveNotificationGroup, dangerNotificationGroup, dangerPositiveNotificationGroup);
            if (jsonObject == null) return null;

            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();   
            return gson.toJson(jsonObject);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return null;
        }
        
    }

    public static Alert createAlertFromAlertTemplate(AlertTemplate alertTemplate, 
            Integer variableSetId, String description, Integer alertId, String alertName, Integer metricGroupId,
            Integer cautionNotificationGroupId, Integer cautionPositiveNotificationGroupId,
            Integer dangerNotificationGroupId, Integer dangerPositiveNotificationGroupId) {

        if (alertTemplate == null) {
            return null;
        }
        
        Alert alert = new Alert();

        if (alertId == null) alert.setId(-1);
        else alert.setId(alertId);
        alert.setVariableSetId(variableSetId);
        alert.setMetricGroupId(metricGroupId);
        alert.setCautionNotificationGroupId(cautionNotificationGroupId);
        alert.setCautionPositiveNotificationGroupId(cautionPositiveNotificationGroupId);
        alert.setDangerNotificationGroupId(dangerNotificationGroupId);
        alert.setDangerPositiveNotificationGroupId(dangerPositiveNotificationGroupId);
        
        alert.setName(alertName);
        alert.setDescription(description);
        alert.setAlertTemplateId(alertTemplate.getId());

        alert.setIsEnabled(alertTemplate.isEnabled());
        alert.setIsCautionEnabled(alertTemplate.isCautionEnabled());
        alert.setIsDangerEnabled(alertTemplate.isDangerEnabled());
        alert.setAlertType(alertTemplate.getAlertType());

        alert.setAlertOnPositive(alertTemplate.isAlertOnPositive());
        alert.setAllowResendAlert(alertTemplate.isAllowResendAlert());
        alert.setResendAlertEvery(alertTemplate.getResendAlertEvery());
        alert.setResendAlertEveryTimeUnit(alertTemplate.getResendAlertEveryTimeUnit());

        alert.setCautionOperator(alertTemplate.getCautionOperator());
        alert.setCautionCombination(alertTemplate.getCautionCombination());
        alert.setCautionCombinationCount(alertTemplate.getCautionCombinationCount());
        alert.setCautionThreshold(alertTemplate.getCautionThreshold());
        alert.setCautionWindowDuration(alertTemplate.getCautionWindowDuration());
        alert.setCautionWindowDurationTimeUnit(alertTemplate.getCautionWindowDurationTimeUnit());
        alert.setCautionStopTrackingAfter(alertTemplate.getCautionStopTrackingAfter());
        alert.setCautionStopTrackingAfterTimeUnit(alertTemplate.getCautionStopTrackingAfterTimeUnit());
        alert.setCautionMinimumSampleCount(alertTemplate.getCautionMinimumSampleCount());

        alert.setDangerOperator(alertTemplate.getDangerOperator());
        alert.setDangerCombination(alertTemplate.getDangerCombination());
        alert.setDangerCombinationCount(alertTemplate.getDangerCombinationCount());
        alert.setDangerThreshold(alertTemplate.getDangerThreshold());
        alert.setDangerWindowDuration(alertTemplate.getDangerWindowDuration());
        alert.setDangerWindowDurationTimeUnit(alertTemplate.getDangerWindowDurationTimeUnit());
        alert.setDangerStopTrackingAfter(alertTemplate.getDangerStopTrackingAfter());
        alert.setDangerStopTrackingAfterTimeUnit(alertTemplate.getDangerStopTrackingAfterTimeUnit());
        alert.setDangerMinimumSampleCount(alertTemplate.getDangerMinimumSampleCount());

        alert.setIsCautionAlertActive(false);
        alert.setIsDangerAlertActive(false);
        
        return alert;
    }
    
    public static boolean areAlertTemplateIdsInConflict(Alert alert1, Alert alert2) {
        if (alert2 == null) return false;
        if (alert1 == null) return false;
        
        if ((alert1.getAlertTemplateId() == null) && (alert2.getAlertTemplateId() == null)) return false;
        if ((alert1.getAlertTemplateId() == null) && (alert2.getAlertTemplateId() != null)) return true;
        if ((alert1.getAlertTemplateId() != null) && (alert2.getAlertTemplateId() == null)) return true;

        return !alert1.getAlertTemplateId().equals(alert2.getAlertTemplateId());
    }
    
    public String getDescription() {
        return description_;
    }
    
    public void setDescription(String description) {
        this.description_ = description;
    }
    
    public Integer getAlertTemplateId() {
        return alertTemplateId_;
    }

    public void setAlertTemplateId(Integer alertTemplateId) {
        this.alertTemplateId_ = alertTemplateId;
    }

    public Integer getVariableSetId() {
        return variableSetId_;
    }

    public void setVariableSetId(Integer variableSetId) {
        this.variableSetId_ = variableSetId;
    }
    
    public Integer getMetricGroupId() {
        return metricGroupId_;
    }
    
    public void setMetricGroupId(Integer metricGroupId) {
        this.metricGroupId_ = metricGroupId;
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
