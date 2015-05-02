package com.pearson.statsagg.database.alerts;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;
import com.pearson.statsagg.database.DatabaseObject;
import com.pearson.statsagg.utilities.MathUtilities;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class Alert extends DatabaseObject<Alert> {
    
    private static final Logger logger = LoggerFactory.getLogger(Alert.class.getName());
    
    public static final int TYPE_AVAILABILITY = 1001;
    public static final int TYPE_THRESHOLD = 1002;
    
    public static final Integer OPERATOR_GREATER = 1;
    public static final Integer OPERATOR_GREATER_EQUALS = 2;
    public static final Integer OPERATOR_LESS = 3;
    public static final Integer OPERATOR_LESS_EQUALS = 4;
    public static final Integer OPERATOR_EQUALS = 5;
    
    public static final Integer COMBINATION_ANY = 101;
    public static final Integer COMBINATION_ALL = 102;
    public static final Integer COMBINATION_AVERAGE = 103;
    public static final Integer COMBINATION_AT_MOST_COUNT = 105;
    public static final Integer COMBINATION_AT_LEAST_COUNT = 106;
     
    private Integer id_;
    private String name_ = null;
    private String uppercaseName_ = null;
    private String description_ = null;
    private Integer metricGroupId_ = null;
    private Boolean isEnabled_ = null;
    private Boolean isCautionEnabled_ = null;
    private Boolean isDangerEnabled_ = null;
    private Integer alertType_ = null;

    private Boolean alertOnPositive_ = null;
    private Boolean allowResendAlert_ = null;
    private Integer sendAlertEveryNumMilliseconds_ = null;

    private Integer cautionNotificationGroupId_ = null;
    private Integer cautionOperator_ = null;
    private Integer cautionCombination_ = null; 
    private Integer cautionCombinationCount_ = null;
    private BigDecimal cautionThreshold_ = null; 
    private Long cautionWindowDuration_ = null;
    private Long cautionStopTrackingAfter_ = null;
    private Integer cautionMinimumSampleCount_ = null;
    private Boolean isCautionAlertActive_ = null;
    private Timestamp cautionAlertLastSentTimestamp_ = null;
    private Boolean isCautionAcknowledged_ = null;
    private String cautionActiveAlertsSet_ = null;
    private Timestamp cautionFirstActiveAt_ = null;
    
    private Integer dangerNotificationGroupId_ = null;
    private Integer dangerOperator_ = null; 
    private Integer dangerCombination_ = null; 
    private Integer dangerCombinationCount_ = null;
    private BigDecimal dangerThreshold_ = null; 
    private Long dangerWindowDuration_ = null;
    private Long dangerStopTrackingAfter_ = null;
    private Integer dangerMinimumSampleCount_ = null;
    private Boolean isDangerAlertActive_ = null;
    private Timestamp dangerAlertLastSentTimestamp_ = null;
    private Boolean isDangerAcknowledged_ = null;
    private String dangerActiveAlertsSet_ = null;
    private Timestamp dangerFirstActiveAt_ = null;
    
    public Alert() {
        this.id_ = -1;
    }
    
    public Alert(Integer id, String name, String description, Integer metricGroupId, Boolean isEnabled, Boolean isCautionEnabled, 
            Boolean isDangerEnabled, Integer alertType, Boolean alertOnPositive, Boolean allowResendAlert, Integer sendAlertEveryNumMilliseconds, 
            Integer cautionNotificationGroupId, Integer cautionOperator, Integer cautionCombination, Integer cautionCombinationCount, BigDecimal cautionThreshold, 
            Long cautionWindowDuration, Long cautionStopTrackingAfter, Integer cautionMinimumSampleCount, Boolean isCautionAlertActive, Timestamp cautionAlertLastSentTimestamp, 
            Boolean isCautionAcknowledged, String cautionActiveAlertsSet, Timestamp cautionFirstActiveAt, 
            Integer dangerNotificationGroupId, Integer dangerOperator, Integer dangerCombination, Integer dangerCombinationCount, BigDecimal dangerThreshold, 
            Long dangerWindowDuration, Long dangerStopTrackingAfter, Integer dangerMinimumSampleCount, Boolean isDangerAlertActive, Timestamp dangerAlertLastSentTimestamp, 
            Boolean isDangerAcknowledged, String dangerActiveAlertsSet, Timestamp dangerFirstActiveAt) {
        
        this(id, name, ((name == null) ? null : name.toUpperCase()), description, metricGroupId, isEnabled, isCautionEnabled, 
             isDangerEnabled, alertType, alertOnPositive, allowResendAlert, sendAlertEveryNumMilliseconds, 
             cautionNotificationGroupId, cautionOperator, cautionCombination, cautionCombinationCount, cautionThreshold, 
             cautionWindowDuration, cautionStopTrackingAfter, cautionMinimumSampleCount, isCautionAlertActive, cautionAlertLastSentTimestamp, 
             isCautionAcknowledged, cautionActiveAlertsSet, cautionFirstActiveAt, 
             dangerNotificationGroupId, dangerOperator, dangerCombination, dangerCombinationCount, dangerThreshold, 
             dangerWindowDuration, dangerStopTrackingAfter, dangerMinimumSampleCount, isDangerAlertActive, dangerAlertLastSentTimestamp, 
             isDangerAcknowledged, dangerActiveAlertsSet, dangerFirstActiveAt);
    }

    public Alert(Integer id, String name, String uppercaseName, String description, Integer metricGroupId, Boolean isEnabled, Boolean isCautionEnabled, 
            Boolean isDangerEnabled, Integer alertType, Boolean alertOnPositive, Boolean allowResendAlert, Integer sendAlertEveryNumMilliseconds, 
            Integer cautionNotificationGroupId, Integer cautionOperator, Integer cautionCombination, Integer cautionCombinationCount, BigDecimal cautionThreshold, 
            Long cautionWindowDuration, Long cautionStopTrackingAfter, Integer cautionMinimumSampleCount, Boolean isCautionAlertActive, Timestamp cautionAlertLastSentTimestamp, 
            Boolean isCautionAcknowledged, String cautionActiveAlertsSet, Timestamp cautionFirstActiveAt, 
            Integer dangerNotificationGroupId, Integer dangerOperator, Integer dangerCombination, Integer dangerCombinationCount, BigDecimal dangerThreshold, 
            Long dangerWindowDuration, Long dangerStopTrackingAfter, Integer dangerMinimumSampleCount, Boolean isDangerAlertActive, Timestamp dangerAlertLastSentTimestamp, 
            Boolean isDangerAcknowledged, String dangerActiveAlertsSet, Timestamp dangerFirstActiveAt) {
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
        this.sendAlertEveryNumMilliseconds_ = sendAlertEveryNumMilliseconds;
        
        this.cautionNotificationGroupId_ = cautionNotificationGroupId;
        this.cautionOperator_ = cautionOperator;
        this.cautionCombination_ = cautionCombination;
        this.cautionCombinationCount_ = cautionCombinationCount;
        this.cautionThreshold_ = cautionThreshold;
        this.cautionWindowDuration_ = cautionWindowDuration;
        this.cautionStopTrackingAfter_ = cautionStopTrackingAfter;
        this.cautionMinimumSampleCount_ = cautionMinimumSampleCount;
        this.isCautionAlertActive_ = isCautionAlertActive;
        if (cautionAlertLastSentTimestamp == null) this.cautionAlertLastSentTimestamp_ = null;
        else this.cautionAlertLastSentTimestamp_ = (Timestamp) cautionAlertLastSentTimestamp.clone();
        this.isCautionAcknowledged_ = isCautionAcknowledged;
        this.cautionActiveAlertsSet_ = cautionActiveAlertsSet;
        this.cautionFirstActiveAt_ = cautionFirstActiveAt;

        this.dangerNotificationGroupId_ = dangerNotificationGroupId;
        this.dangerOperator_ = dangerOperator;
        this.dangerCombination_ = dangerCombination;
        this.dangerCombinationCount_ = dangerCombinationCount;
        this.dangerThreshold_ = dangerThreshold;
        this.dangerWindowDuration_ = dangerWindowDuration;
        this.dangerStopTrackingAfter_ = dangerStopTrackingAfter;
        this.dangerMinimumSampleCount_ = dangerMinimumSampleCount;
        this.isDangerAlertActive_ = isDangerAlertActive;
        if (dangerAlertLastSentTimestamp == null) this.dangerAlertLastSentTimestamp_ = null;
        else this.dangerAlertLastSentTimestamp_ = (Timestamp) dangerAlertLastSentTimestamp.clone();
        this.isDangerAcknowledged_ = isDangerAcknowledged;
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
        alertCopy.setSendAlertEveryNumMilliseconds(alert.getSendAlertEveryNumMilliseconds());

        alertCopy.setCautionNotificationGroupId(alert.getCautionNotificationGroupId());
        alertCopy.setCautionOperator(alert.getCautionOperator());
        alertCopy.setCautionCombination(alert.getCautionCombination());
        alertCopy.setCautionCombinationCount(alert.getCautionCombinationCount());
        alertCopy.setCautionThreshold(alert.getCautionThreshold());
        alertCopy.setCautionWindowDuration(alert.getCautionWindowDuration());
        alertCopy.setCautionStopTrackingAfter(alert.getCautionStopTrackingAfter());
        alertCopy.setCautionMinimumSampleCount(alert.getCautionMinimumSampleCount());
        alertCopy.setIsCautionAlertActive(alert.isCautionAlertActive());
        if (alert.getCautionAlertLastSentTimestamp() == null) alertCopy.setCautionAlertLastSentTimestamp(null);
        else alertCopy.setCautionAlertLastSentTimestamp(new Timestamp(alert.getCautionAlertLastSentTimestamp().getTime()));
        alertCopy.setIsCautionAcknowledged(alert.isCautionAcknowledged());
        alertCopy.setCautionActiveAlertsSet(alert.getCautionActiveAlertsSet());
        alertCopy.setCautionFirstActiveAt(alert.getCautionFirstActiveAt());
        
        alertCopy.setDangerNotificationGroupId(alert.getDangerNotificationGroupId());
        alertCopy.setDangerOperator(alert.getDangerOperator());
        alertCopy.setDangerCombination(alert.getDangerCombination());
        alertCopy.setDangerCombinationCount(alert.getDangerCombinationCount());
        alertCopy.setDangerThreshold(alert.getDangerThreshold());
        alertCopy.setDangerWindowDuration(alert.getDangerWindowDuration());
        alertCopy.setDangerStopTrackingAfter(alert.getDangerStopTrackingAfter());
        alertCopy.setDangerMinimumSampleCount(alert.getDangerMinimumSampleCount());
        alertCopy.setIsDangerAlertActive(alert.isDangerAlertActive());
        if (alert.getDangerAlertLastSentTimestamp() == null) alertCopy.setDangerAlertLastSentTimestamp(null);
        else alertCopy.setDangerAlertLastSentTimestamp(new Timestamp(alert.getDangerAlertLastSentTimestamp().getTime()));
        alertCopy.setIsDangerAcknowledged(alert.isDangerAcknowledged());
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
                .append(sendAlertEveryNumMilliseconds_, alert.getSendAlertEveryNumMilliseconds())
                .append(cautionNotificationGroupId_, alert.getCautionNotificationGroupId())
                .append(cautionOperator_, alert.getCautionOperator())
                .append(cautionCombination_, alert.getCautionCombination())
                .append(cautionCombinationCount_, alert.getCautionCombinationCount())
                .append(isCautionThresholdValueEqual, true)
                .append(cautionWindowDuration_, alert.getCautionWindowDuration())
                .append(cautionStopTrackingAfter_, alert.getCautionStopTrackingAfter())
                .append(cautionMinimumSampleCount_, alert.getCautionMinimumSampleCount())
                .append(isCautionAlertActive_, alert.isCautionAlertActive())
                .append(cautionAlertLastSentTimestamp_, alert.getCautionAlertLastSentTimestamp())
                .append(isCautionAcknowledged_, alert.isCautionAcknowledged())
                .append(cautionActiveAlertsSet_, alert.getCautionActiveAlertsSet())
                .append(cautionFirstActiveAt_, alert.getCautionFirstActiveAt())
                .append(dangerNotificationGroupId_, alert.getDangerNotificationGroupId())
                .append(dangerOperator_, alert.getDangerOperator())
                .append(dangerCombination_, alert.getDangerCombination())
                .append(dangerCombinationCount_, alert.getDangerCombinationCount())
                .append(isDangerThresholdValueEqual, true)
                .append(dangerWindowDuration_, alert.getDangerWindowDuration())
                .append(dangerStopTrackingAfter_, alert.getDangerStopTrackingAfter())
                .append(dangerMinimumSampleCount_, alert.getDangerMinimumSampleCount())
                .append(isDangerAlertActive_, alert.isDangerAlertActive())
                .append(dangerAlertLastSentTimestamp_, alert.getDangerAlertLastSentTimestamp())
                .append(isDangerAcknowledged_, alert.isDangerAcknowledged())
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
                .append(cautionStopTrackingAfter_, alert.getCautionStopTrackingAfter())
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
                .append(dangerStopTrackingAfter_, alert.getDangerStopTrackingAfter())
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
    
    public String getCautionOperatorString(boolean includeSymbol, boolean includeEnglish) {
        
        if (cautionOperator_ == null) {
            return null;
        }

        if (includeSymbol && includeEnglish) {
            if (cautionOperator_.intValue() == OPERATOR_GREATER) return "> (greater than)";
            else if (cautionOperator_.intValue() == OPERATOR_GREATER_EQUALS) return ">= (greater than or equal to)";
            else if (cautionOperator_.intValue() == OPERATOR_LESS) return "< (less than)";
            else if (cautionOperator_.intValue() == OPERATOR_LESS_EQUALS) return "<= (less than or equal to)";
            else if (cautionOperator_.intValue() == OPERATOR_EQUALS) return "= (equal to)";
            else {
                logger.warn("Unrecognized caution operator code");
            }
        }
        else if (includeSymbol) {
            if (cautionOperator_.intValue() == OPERATOR_GREATER) return ">";
            else if (cautionOperator_.intValue() == OPERATOR_GREATER_EQUALS) return ">=";
            else if (cautionOperator_.intValue() == OPERATOR_LESS) return "<";
            else if (cautionOperator_.intValue() == OPERATOR_LESS_EQUALS) return "<=";
            else if (cautionOperator_.intValue() == OPERATOR_EQUALS) return "=";
            else {
                logger.warn("Unrecognized caution operator code");
            }
        }
        else if (includeEnglish) {
            if (cautionOperator_.intValue() == OPERATOR_GREATER) return "greater than";
            else if (cautionOperator_.intValue() == OPERATOR_GREATER_EQUALS) return "greater than or equal to";
            else if (cautionOperator_.intValue() == OPERATOR_LESS) return "less than";
            else if (cautionOperator_.intValue() == OPERATOR_LESS_EQUALS) return "less than or equal to";
            else if (cautionOperator_.intValue() == OPERATOR_EQUALS) return "equal to";
            else {
                logger.warn("Unrecognized caution operator code");
            }
        }
        
        return null;
    }
            
    public String getDangerOperatorString(boolean includeSymbol, boolean includeEnglish) {
        
        if (dangerOperator_ == null) {
            return null;
        }

        if (includeSymbol && includeEnglish) {
            if (dangerOperator_.intValue() == OPERATOR_GREATER) return "> (greater than)";
            else if (dangerOperator_.intValue() == OPERATOR_GREATER_EQUALS) return ">= (greater than or equal to)";
            else if (dangerOperator_.intValue() == OPERATOR_LESS) return "< (less than)";
            else if (dangerOperator_.intValue() == OPERATOR_LESS_EQUALS) return "<= (less than or equal to)";
            else if (dangerOperator_.intValue() == OPERATOR_EQUALS) return "= (equal to)";
            else {
                logger.warn("Unrecognized danger operator code");
            }
        }
        else if (includeSymbol) {
            if (dangerOperator_.intValue() == OPERATOR_GREATER) return ">";
            else if (dangerOperator_.intValue() == OPERATOR_GREATER_EQUALS) return ">=";
            else if (dangerOperator_.intValue() == OPERATOR_LESS) return "<";
            else if (dangerOperator_.intValue() == OPERATOR_LESS_EQUALS) return "<=";
            else if (dangerOperator_.intValue() == OPERATOR_EQUALS) return "=";
            else {
                logger.warn("Unrecognized danger operator code");
            }
        }
        else if (includeEnglish) {
            if (dangerOperator_.intValue() == OPERATOR_GREATER) return "greater than";
            else if (dangerOperator_.intValue() == OPERATOR_GREATER_EQUALS) return "greater than or equal to";
            else if (dangerOperator_.intValue() == OPERATOR_LESS) return "less than";
            else if (dangerOperator_.intValue() == OPERATOR_LESS_EQUALS) return "less than or equal to";
            else if (dangerOperator_.intValue() == OPERATOR_EQUALS) return "equal to";
            else {
                logger.warn("Unrecognized danger operator code");
            }
        }
        
        return null;
    }
    
    public static Integer getOperatorCodeFromOperatorString(String operator) {
        
        if ((operator == null) || operator.isEmpty()) {
            return null;
        }
        
        if (operator.equals(">") || operator.contains("(greater than)")) {
            return OPERATOR_GREATER;
        }
        else if (operator.equals(">=") || operator.contains("(greater than or equal to)")) { 
            return OPERATOR_GREATER_EQUALS;
        }
        else if (operator.equals("<") || operator.contains("(less than)")) { 
            return OPERATOR_LESS;
        }
        else if (operator.equals("<=") || operator.contains("(less than or equal to)")) { 
            return OPERATOR_LESS_EQUALS;
        }
        else if (operator.equals("=") || operator.contains("(equal to)")) { 
            return OPERATOR_EQUALS;
        }
        else {
            logger.warn("Unrecognized operator string");
        }
        
        return null;
    }
    
    public String getCautionCombinationString() {
        
        if (cautionCombination_ == null) {
            return null;
        }

         if (cautionCombination_.intValue() == COMBINATION_ANY) {
            return "Any";
        }
        else if (cautionCombination_.intValue() == COMBINATION_ALL) {
            return "All";
        }
        else if (cautionCombination_.intValue() == COMBINATION_AVERAGE) {
            return "Average";
        }
        else if (cautionCombination_.intValue() == COMBINATION_AT_MOST_COUNT) {
            return "At most";
        }
        else if (cautionCombination_.intValue() == COMBINATION_AT_LEAST_COUNT) {
            return "At least";
        }
        else {
            logger.warn("Unrecognized caution combination code");
        }
         
        return null;
    }

    public String getDangerCombinationString() {
        
        if (dangerCombination_ == null) {
            return null;
        }

         if (dangerCombination_.intValue() == COMBINATION_ANY) {
            return "Any";
        }
        else if (dangerCombination_.intValue() == COMBINATION_ALL) {
            return "All";
        }
        else if (dangerCombination_.intValue() == COMBINATION_AVERAGE) {
            return "Average";
        }
        else if (dangerCombination_.intValue() == COMBINATION_AT_MOST_COUNT) {
            return "At most";
        }
        else if (dangerCombination_.intValue() == COMBINATION_AT_LEAST_COUNT) {
            return "At least";
        }
        else {
            logger.warn("Unrecognized danger combination code");
        }
         
        return null;
    }
    
    public static Integer getCombinationCodeFromString(String combination) {
        
        if ((combination == null) || combination.isEmpty()) {
            return null;
        }
        
        if (combination.equalsIgnoreCase("Any")) {
            return COMBINATION_ANY;
        }
        else if (combination.equalsIgnoreCase("All")) {
            return COMBINATION_ALL;
        }
        else if (combination.equalsIgnoreCase("Average")) {
            return COMBINATION_AVERAGE;
        }
        else if (combination.equalsIgnoreCase("At most")) {
            return COMBINATION_AT_MOST_COUNT;
        }
        else if (combination.equalsIgnoreCase("At least")) {
            return COMBINATION_AT_LEAST_COUNT;
        }
        else {
            logger.warn("Unrecognized combination string");
        }
        
        return null;
    }
    
    public static String getCautionMetricValueString_WithLabel(Alert alert, BigDecimal metricValue) {
        
        if ((alert == null) || (metricValue == null) || (alert.getAlertType() == null)) {
            return null;
        }
        
        String outputString = null;

        if (alert.getAlertType() == Alert.TYPE_THRESHOLD) {
            String metricValueString = metricValue.stripTrailingZeros().toPlainString();
            
            if (Objects.equals(Alert.COMBINATION_ALL, alert.getCautionCombination())) outputString = metricValueString + " (recent value)";
            if (Objects.equals(Alert.COMBINATION_ANY, alert.getCautionCombination())) outputString = metricValueString + " (recent value)";
            if (Objects.equals(Alert.COMBINATION_AVERAGE, alert.getCautionCombination())) outputString = metricValueString + " (avg value)";
            if (Objects.equals(Alert.COMBINATION_AT_LEAST_COUNT, alert.getCautionCombination())) outputString = metricValueString + " (count)";
            if (Objects.equals(Alert.COMBINATION_AT_MOST_COUNT, alert.getCautionCombination())) outputString = metricValueString + " (count)";
        }
        else if (alert.getAlertType() == Alert.TYPE_AVAILABILITY) {
            BigDecimal metricValue_Seconds = metricValue.divide(new BigDecimal(1000));
            String metricValueString = metricValue_Seconds.stripTrailingZeros().toPlainString();
            outputString = metricValueString + " (seconds since last data point received)";
        }
        
        return outputString;
    }
    
    public static String getDangerMetricValueString_WithLabel(Alert alert, BigDecimal metricValue) {
        
        if ((alert == null) || (metricValue == null) || (alert.getAlertType() == null)) {
            return null;
        }
        
        String outputString = null;

        if (alert.getAlertType() == Alert.TYPE_THRESHOLD) {
            String metricValueString = metricValue.stripTrailingZeros().toPlainString();
            
            if (Objects.equals(Alert.COMBINATION_ALL, alert.getDangerCombination())) outputString = metricValueString + " (recent value)";
            if (Objects.equals(Alert.COMBINATION_ANY, alert.getDangerCombination())) outputString = metricValueString + " (recent value)";
            if (Objects.equals(Alert.COMBINATION_AVERAGE, alert.getDangerCombination())) outputString = metricValueString + " (avg value)";
            if (Objects.equals(Alert.COMBINATION_AT_LEAST_COUNT, alert.getDangerCombination())) outputString = metricValueString + " (count)";
            if (Objects.equals(Alert.COMBINATION_AT_MOST_COUNT, alert.getDangerCombination())) outputString = metricValueString + " (count)";
        }
        else if (alert.getAlertType() == Alert.TYPE_AVAILABILITY) {
            BigDecimal metricValue_Seconds = metricValue.divide(new BigDecimal(1000));
            String metricValueString = metricValue_Seconds.stripTrailingZeros().toPlainString();
            outputString = metricValueString + " (seconds since last data point received)";
        }
        
        return outputString;
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
    
    public Integer getSendAlertEveryNumMilliseconds() {
        return sendAlertEveryNumMilliseconds_;
    }

    public void setSendAlertEveryNumMilliseconds(Integer sendAlertEveryNumMilliseconds) {
        this.sendAlertEveryNumMilliseconds_ = sendAlertEveryNumMilliseconds;
    }

    public Integer getCautionNotificationGroupId() {
        return cautionNotificationGroupId_;
    }

    public void setCautionNotificationGroupId(Integer cautionNotificationGroupId) {
        this.cautionNotificationGroupId_ = cautionNotificationGroupId;
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
    
    public Long getCautionStopTrackingAfter() {
        return cautionStopTrackingAfter_;
    }

    public void setCautionStopTrackingAfter(Long cautionStopTrackingAfter) {
        this.cautionStopTrackingAfter_ = cautionStopTrackingAfter;
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

    public Boolean isCautionAcknowledged() {
        return isCautionAcknowledged_;
    }

    public void setIsCautionAcknowledged(Boolean isCautionAcknowledged) {
        this.isCautionAcknowledged_ = isCautionAcknowledged;
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
    
    public Long getDangerStopTrackingAfter() {
        return dangerStopTrackingAfter_;
    }

    public void setDangerStopTrackingAfter(Long dangerStopTrackingAfter) {
        this.dangerStopTrackingAfter_ = dangerStopTrackingAfter;
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

    public Boolean isDangerAcknowledged() {
        return isDangerAcknowledged_;
    }

    public void setIsDangerAcknowledged(Boolean isDangerAcknowledged) {
        this.isDangerAcknowledged_ = isDangerAcknowledged;
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
