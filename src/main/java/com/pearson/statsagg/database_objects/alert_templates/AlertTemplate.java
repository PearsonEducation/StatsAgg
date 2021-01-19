package com.pearson.statsagg.database_objects.alert_templates;

import com.google.gson.annotations.SerializedName;
import com.pearson.statsagg.database_objects.DatabaseObjectValidation;
import com.pearson.statsagg.database_objects.abstract_objects.AbstractAlert;
import com.pearson.statsagg.utilities.db_utils.DatabaseObject;
import com.pearson.statsagg.utilities.math_utils.MathUtilities;
import java.math.BigDecimal;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class AlertTemplate extends AbstractAlert implements DatabaseObject<AlertTemplate>  {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertTemplate.class.getName());
    
    @SerializedName("variable_set_list_id") private Integer variableSetListId_;
    @SerializedName("description_variable") protected String descriptionVariable_ = null;
    @SerializedName("alert_name_variable") private String alertNameVariable_ = null;
    @SerializedName("metric_group_name_variable") private String metricGroupNameVariable_ = null;

    @SerializedName("caution_notification_group_name_variable") private String cautionNotificationGroupNameVariable_ = null;
    @SerializedName("caution_positive_notification_group_name_variable") private String cautionPositiveNotificationGroupNameVariable_ = null;
    
    @SerializedName("danger_notification_group_name_variable") private String dangerNotificationGroupNameVariable_ = null;
    @SerializedName("danger_positive_notification_group_name_variable") private String dangerPositiveNotificationGroupNameVariable_ = null;
    
    public AlertTemplate() {
        super.id_ = -1;
    }
    
    public AlertTemplate(Integer id, String name, Integer variableSetListId, String descriptionVariable, String alertNameVariable, String metricGroupNameVariable, Boolean isEnabled,
            Boolean isCautionEnabled, Boolean isDangerEnabled, Integer alertType, Boolean alertOnPositive, Boolean allowResendAlert, Long resendAlertEvery, Integer resendAlertEveryTimeUnit,
            String cautionNotificationGroupNameVariable, String cautionPositiveNotificationGroupNameVariable, Integer cautionOperator, Integer cautionCombination, 
            Integer cautionCombinationCount, BigDecimal cautionThreshold, Long cautionWindowDuration, Integer cautionWindowDurationTimeUnit, 
            Long cautionStopTrackingAfter, Integer cautionStopTrackingAfterTimeUnit, Integer cautionMinimumSampleCount, 
            String dangerNotificationGroupNameVariable, String dangerPositiveNotificationGroupNameVariable, Integer dangerOperator, Integer dangerCombination, 
            Integer dangerCombinationCount, BigDecimal dangerThreshold, Long dangerWindowDuration, Integer dangerWindowDurationTimeUnit, 
            Long dangerStopTrackingAfter, Integer dangerStopTrackingAfterTimeUnit, Integer dangerMinimumSampleCount) {
        
        this(id, name, ((name == null) ? null : name.toUpperCase()), variableSetListId, descriptionVariable, alertNameVariable, metricGroupNameVariable, isEnabled, 
             isCautionEnabled, isDangerEnabled, alertType, alertOnPositive, allowResendAlert, resendAlertEvery, 
             resendAlertEveryTimeUnit, cautionNotificationGroupNameVariable, cautionPositiveNotificationGroupNameVariable, cautionOperator, cautionCombination,  
             cautionCombinationCount, cautionThreshold, cautionWindowDuration, cautionWindowDurationTimeUnit, 
             cautionStopTrackingAfter, cautionStopTrackingAfterTimeUnit, cautionMinimumSampleCount, 
             dangerNotificationGroupNameVariable, dangerPositiveNotificationGroupNameVariable, dangerOperator, dangerCombination,  
             dangerCombinationCount, dangerThreshold, dangerWindowDuration, dangerWindowDurationTimeUnit, 
             dangerStopTrackingAfter, dangerStopTrackingAfterTimeUnit, dangerMinimumSampleCount);
    }

    public AlertTemplate(Integer id, String name, String uppercaseName, Integer variableSetListId, String descriptionVariable, String alertNameVariable, String metricGroupNameVariable, Boolean isEnabled,
            Boolean isCautionEnabled, Boolean isDangerEnabled, Integer alertType, Boolean alertOnPositive, Boolean allowResendAlert, Long resendAlertEvery, Integer resendAlertEveryTimeUnit,
            String cautionNotificationGroupNameVariable, String cautionPositiveNotificationGroupNameVariable, Integer cautionOperator, Integer cautionCombination, 
            Integer cautionCombinationCount, BigDecimal cautionThreshold, Long cautionWindowDuration, Integer cautionWindowDurationTimeUnit, 
            Long cautionStopTrackingAfter, Integer cautionStopTrackingAfterTimeUnit, Integer cautionMinimumSampleCount, 
            String dangerNotificationGroupNameVariable, String dangerPositiveNotificationGroupNameVariable, Integer dangerOperator, Integer dangerCombination, 
            Integer dangerCombinationCount, BigDecimal dangerThreshold, Long dangerWindowDuration, Integer dangerWindowDurationTimeUnit, 
            Long dangerStopTrackingAfter, Integer dangerStopTrackingAfterTimeUnit, Integer dangerMinimumSampleCount) {
        super.id_ = id;
        super.name_ = name;
        super.uppercaseName_ = uppercaseName;
        this.variableSetListId_ = variableSetListId;
        this.descriptionVariable_ = descriptionVariable;
        
        this.alertNameVariable_ = alertNameVariable;
        this.metricGroupNameVariable_ = metricGroupNameVariable;
        super.isEnabled_ = isEnabled;
        super.isCautionEnabled_ = isCautionEnabled;
        super.isDangerEnabled_ = isDangerEnabled;
        super.alertType_ = alertType;
        
        super.alertOnPositive_ = alertOnPositive;
        super.allowResendAlert_ = allowResendAlert;
        super.resendAlertEvery_ = resendAlertEvery;
        super.resendAlertEveryTimeUnit_ = resendAlertEveryTimeUnit;
        
        this.cautionNotificationGroupNameVariable_ = cautionNotificationGroupNameVariable;
        this.cautionPositiveNotificationGroupNameVariable_ = cautionPositiveNotificationGroupNameVariable;
        super.cautionOperator_ = cautionOperator;
        super.cautionCombination_ = cautionCombination;
        super.cautionCombinationCount_ = cautionCombinationCount;
        super.cautionThreshold_ = cautionThreshold;
        super.cautionWindowDuration_ = cautionWindowDuration;
        super.cautionWindowDurationTimeUnit_ = cautionWindowDurationTimeUnit;
        super.cautionStopTrackingAfter_ = cautionStopTrackingAfter;
        super.cautionStopTrackingAfterTimeUnit_ = cautionStopTrackingAfterTimeUnit;
        super.cautionMinimumSampleCount_ = cautionMinimumSampleCount;
        
        this.dangerNotificationGroupNameVariable_ = dangerNotificationGroupNameVariable;
        this.dangerPositiveNotificationGroupNameVariable_ = dangerPositiveNotificationGroupNameVariable;
        super.dangerOperator_ = dangerOperator;
        super.dangerCombination_ = dangerCombination;
        super.dangerCombinationCount_ = dangerCombinationCount;
        super.dangerThreshold_ = dangerThreshold;
        super.dangerWindowDuration_ = dangerWindowDuration;
        super.dangerWindowDurationTimeUnit_ = dangerWindowDurationTimeUnit;
        super.dangerStopTrackingAfter_ = dangerStopTrackingAfter;
        super.dangerStopTrackingAfterTimeUnit_ = dangerStopTrackingAfterTimeUnit;
        super.dangerMinimumSampleCount_ = dangerMinimumSampleCount;
    }
    
    public static AlertTemplate copy(AlertTemplate alertTemplate) {
        
        if (alertTemplate == null) {
            return null;
        }
        
        AlertTemplate alertTemplateCopy = new AlertTemplate();
        
        alertTemplateCopy.setId(alertTemplate.getId());
        alertTemplateCopy.setName(alertTemplate.getName());
        alertTemplateCopy.setVariableSetListId(alertTemplate.getVariableSetListId());
        alertTemplateCopy.setDescriptionVariable(alertTemplate.getDescriptionVariable());
        alertTemplateCopy.setAlertNameVariable(alertTemplate.getAlertNameVariable());
        alertTemplateCopy.setMetricGroupNameVariable(alertTemplate.getMetricGroupNameVariable());
        alertTemplateCopy.setIsEnabled(alertTemplate.isEnabled());
        alertTemplateCopy.setIsCautionEnabled(alertTemplate.isCautionEnabled());
        alertTemplateCopy.setIsDangerEnabled(alertTemplate.isDangerEnabled());
        alertTemplateCopy.setAlertType(alertTemplate.getAlertType());

        alertTemplateCopy.setAlertOnPositive(alertTemplate.isAlertOnPositive());
        alertTemplateCopy.setAllowResendAlert(alertTemplate.isAllowResendAlert());
        alertTemplateCopy.setResendAlertEvery(alertTemplate.getResendAlertEvery());
        alertTemplateCopy.setResendAlertEveryTimeUnit(alertTemplate.getResendAlertEveryTimeUnit());

        alertTemplateCopy.setCautionNotificationGroupNameVariable(alertTemplate.getCautionNotificationGroupNameVariable());
        alertTemplateCopy.setCautionPositiveNotificationGroupNameVariable(alertTemplate.getCautionPositiveNotificationGroupNameVariable());
        alertTemplateCopy.setCautionOperator(alertTemplate.getCautionOperator());
        alertTemplateCopy.setCautionCombination(alertTemplate.getCautionCombination());
        alertTemplateCopy.setCautionCombinationCount(alertTemplate.getCautionCombinationCount());
        alertTemplateCopy.setCautionThreshold(alertTemplate.getCautionThreshold());
        alertTemplateCopy.setCautionWindowDuration(alertTemplate.getCautionWindowDuration());
        alertTemplateCopy.setCautionWindowDurationTimeUnit(alertTemplate.getCautionWindowDurationTimeUnit());
        alertTemplateCopy.setCautionStopTrackingAfter(alertTemplate.getCautionStopTrackingAfter());
        alertTemplateCopy.setCautionStopTrackingAfterTimeUnit(alertTemplate.getCautionStopTrackingAfterTimeUnit());
        alertTemplateCopy.setCautionMinimumSampleCount(alertTemplate.getCautionMinimumSampleCount());
        
        alertTemplateCopy.setDangerNotificationGroupNameVariable(alertTemplate.getDangerNotificationGroupNameVariable());
        alertTemplateCopy.setDangerPositiveNotificationGroupNameVariable(alertTemplate.getDangerPositiveNotificationGroupNameVariable());
        alertTemplateCopy.setDangerOperator(alertTemplate.getDangerOperator());
        alertTemplateCopy.setDangerCombination(alertTemplate.getDangerCombination());
        alertTemplateCopy.setDangerCombinationCount(alertTemplate.getDangerCombinationCount());
        alertTemplateCopy.setDangerThreshold(alertTemplate.getDangerThreshold());
        alertTemplateCopy.setDangerWindowDuration(alertTemplate.getDangerWindowDuration());
        alertTemplateCopy.setDangerWindowDurationTimeUnit(alertTemplate.getDangerWindowDurationTimeUnit());
        alertTemplateCopy.setDangerStopTrackingAfter(alertTemplate.getDangerStopTrackingAfter());
        alertTemplateCopy.setDangerStopTrackingAfterTimeUnit(alertTemplate.getDangerStopTrackingAfterTimeUnit());
        alertTemplateCopy.setDangerMinimumSampleCount(alertTemplate.getDangerMinimumSampleCount());

        return alertTemplateCopy;
    }
    
    @Override
    public boolean isEqual(AlertTemplate alertTemplate) {
        
        if (alertTemplate == null) return false;
        if (alertTemplate == this) return true;
        if (alertTemplate.getClass() != getClass()) return false;
        
        boolean isCautionThresholdValueEqual = MathUtilities.areBigDecimalsNumericallyEqual(cautionThreshold_, alertTemplate.getCautionThreshold());
        boolean isDangerThresholdValueEqual = MathUtilities.areBigDecimalsNumericallyEqual(dangerThreshold_, alertTemplate.getDangerThreshold());
        
        return new EqualsBuilder()
                .append(id_, alertTemplate.getId())
                .append(name_, alertTemplate.getName())
                .append(variableSetListId_, alertTemplate.getVariableSetListId())
                .append(uppercaseName_, alertTemplate.getUppercaseName())
                .append(descriptionVariable_, alertTemplate.getDescriptionVariable())
                .append(alertNameVariable_, alertTemplate.getMetricGroupNameVariable())
                .append(metricGroupNameVariable_, alertTemplate.getMetricGroupNameVariable())
                .append(isEnabled_, alertTemplate.isEnabled())
                .append(isCautionEnabled_, alertTemplate.isCautionEnabled())
                .append(isDangerEnabled_, alertTemplate.isDangerEnabled())
                .append(alertType_, alertTemplate.getAlertType())
                .append(alertOnPositive_, alertTemplate.isAlertOnPositive())
                .append(allowResendAlert_, alertTemplate.isAllowResendAlert())
                .append(resendAlertEvery_, alertTemplate.getResendAlertEvery())
                .append(resendAlertEveryTimeUnit_, alertTemplate.getResendAlertEveryTimeUnit())
                .append(cautionNotificationGroupNameVariable_, alertTemplate.getCautionNotificationGroupNameVariable())
                .append(cautionPositiveNotificationGroupNameVariable_, alertTemplate.getCautionPositiveNotificationGroupNameVariable())
                .append(cautionOperator_, alertTemplate.getCautionOperator())
                .append(cautionCombination_, alertTemplate.getCautionCombination())
                .append(cautionCombinationCount_, alertTemplate.getCautionCombinationCount())
                .append(isCautionThresholdValueEqual, true)
                .append(cautionWindowDuration_, alertTemplate.getCautionWindowDuration())
                .append(cautionWindowDurationTimeUnit_, alertTemplate.getCautionWindowDurationTimeUnit())
                .append(cautionStopTrackingAfter_, alertTemplate.getCautionStopTrackingAfter())
                .append(cautionStopTrackingAfterTimeUnit_, alertTemplate.getCautionStopTrackingAfterTimeUnit())
                .append(cautionMinimumSampleCount_, alertTemplate.getCautionMinimumSampleCount())
                .append(dangerNotificationGroupNameVariable_, alertTemplate.getDangerNotificationGroupNameVariable())
                .append(dangerPositiveNotificationGroupNameVariable_, alertTemplate.getDangerPositiveNotificationGroupNameVariable())
                .append(dangerOperator_, alertTemplate.getDangerOperator())
                .append(dangerCombination_, alertTemplate.getDangerCombination())
                .append(dangerCombinationCount_, alertTemplate.getDangerCombinationCount())
                .append(isDangerThresholdValueEqual, true)
                .append(dangerWindowDuration_, alertTemplate.getDangerWindowDuration())
                .append(dangerWindowDurationTimeUnit_, alertTemplate.getDangerWindowDurationTimeUnit())
                .append(dangerStopTrackingAfter_, alertTemplate.getDangerStopTrackingAfter())
                .append(dangerStopTrackingAfterTimeUnit_, alertTemplate.getDangerStopTrackingAfterTimeUnit())
                .append(dangerMinimumSampleCount_, alertTemplate.getDangerMinimumSampleCount())
                .isEquals();
    }

    public static DatabaseObjectValidation isValid(AlertTemplate alertTemplate) {
        if (alertTemplate == null) return new DatabaseObjectValidation(false, "Invalid alert template");
        
        DatabaseObjectValidation databaseObjectValidation_CoreCriteria = alertTemplate.isCoreAlertCriteriaValid();
        if (!databaseObjectValidation_CoreCriteria.isValid()) return databaseObjectValidation_CoreCriteria;
        
        DatabaseObjectValidation databaseObjectValidation_AlertTemplateCriteria = alertTemplate.isAlertTemplateCriteriaValid();
        if (!databaseObjectValidation_AlertTemplateCriteria.isValid()) return databaseObjectValidation_AlertTemplateCriteria;
        
        DatabaseObjectValidation databaseObjectValidation_CautionCriteria = alertTemplate.isCautionAlertCriteriaValid(true);
        if (!databaseObjectValidation_CautionCriteria.isValid()) return databaseObjectValidation_CautionCriteria;
        
        DatabaseObjectValidation databaseObjectValidation_DangerCriteria = alertTemplate.isDangerAlertCriteriaValid(true);
        if (!databaseObjectValidation_DangerCriteria.isValid()) return databaseObjectValidation_DangerCriteria;
        
        return new DatabaseObjectValidation(true);
    }
    
    public DatabaseObjectValidation isAlertTemplateCriteriaValid() {
        if (variableSetListId_ == null) return new DatabaseObjectValidation(false, "Invalid variable set list");
        if ((alertNameVariable_ == null) || alertNameVariable_.isEmpty()) return new DatabaseObjectValidation(false, "Invalid alert name variable");
        if ((metricGroupNameVariable_ == null) || metricGroupNameVariable_.isEmpty()) return new DatabaseObjectValidation(false, "Invalid metric group name variable");

        return new DatabaseObjectValidation(true);
    }
    
    public String getDescriptionVariable() {
        return descriptionVariable_;
    }
    
    public void setDescriptionVariable(String descriptionVariable) {
        this.descriptionVariable_ = descriptionVariable;
    }
    
    public Integer getVariableSetListId() {
        return variableSetListId_;
    }

    public void setVariableSetListId(Integer variableSetListId) {
        this.variableSetListId_ = variableSetListId;
    }

    public String getAlertNameVariable() {
        return alertNameVariable_;
    }
    
    public void setAlertNameVariable(String alertNameVariable) {
        this.alertNameVariable_ = alertNameVariable;
    }

    public String getMetricGroupNameVariable() {
        return metricGroupNameVariable_;
    }
    
    public void setMetricGroupNameVariable(String metricGroupNameVariable) {
        this.metricGroupNameVariable_ = metricGroupNameVariable;
    }

    public String getCautionNotificationGroupNameVariable() {
        return cautionNotificationGroupNameVariable_;
    }

    public void setCautionNotificationGroupNameVariable(String cautionNotificationGroupNameVariable) {
        this.cautionNotificationGroupNameVariable_ = cautionNotificationGroupNameVariable;
    }

    public String getCautionPositiveNotificationGroupNameVariable() {
        return cautionPositiveNotificationGroupNameVariable_;
    }

    public void setCautionPositiveNotificationGroupNameVariable(String cautionPositiveNotificationGroupNameVariable) {
        this.cautionPositiveNotificationGroupNameVariable_ = cautionPositiveNotificationGroupNameVariable;
    }
    
    public String getDangerNotificationGroupNameVariable() {
        return dangerNotificationGroupNameVariable_;
    }

    public void setDangerNotificationGroupNameVariable(String dangerNotificationGroupNameVariable) {
        this.dangerNotificationGroupNameVariable_ = dangerNotificationGroupNameVariable;
    }
    
    public String getDangerPositiveNotificationGroupNameVariable() {
        return dangerPositiveNotificationGroupNameVariable_;
    }

    public void setDangerPositiveNotificationGroupNameVariable(String dangerPositiveNotificationGroupNameVariable) {
        this.dangerPositiveNotificationGroupNameVariable_ = dangerPositiveNotificationGroupNameVariable;
    }
    
}
