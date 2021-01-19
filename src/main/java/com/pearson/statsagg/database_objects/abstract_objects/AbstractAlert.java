package com.pearson.statsagg.database_objects.abstract_objects;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;
import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import com.pearson.statsagg.database_objects.DatabaseObjectValidation;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public abstract class AbstractAlert {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractAlert.class.getName());
    
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
    
    @SerializedName("id") protected Integer id_;
    @SerializedName("name") protected String name_ = null;
    protected transient String uppercaseName_ = null;
    @SerializedName("enabled") protected Boolean isEnabled_ = null;
    @SerializedName("caution_enabled") protected Boolean isCautionEnabled_ = null;
    @SerializedName("danger_enabled") protected Boolean isDangerEnabled_ = null;
    @SerializedName("alert_type") protected Integer alertType_ = null;

    @SerializedName("alert_on_positive") protected Boolean alertOnPositive_ = null;
    @SerializedName("allow_resend_alert") protected Boolean allowResendAlert_ = null;
    @SerializedName("resend_alert_every") protected Long resendAlertEvery_ = null;
    @SerializedName("resend_alert_every_time_unit") protected Integer resendAlertEveryTimeUnit_ = null;
    
    @SerializedName("caution_operator") protected Integer cautionOperator_ = null;
    @SerializedName("caution_combination") protected Integer cautionCombination_ = null; 
    @SerializedName("caution_combination_count") protected Integer cautionCombinationCount_ = null;
    @SerializedName("caution_threshold") protected BigDecimal cautionThreshold_ = null; 
    @SerializedName("caution_window_duration") protected Long cautionWindowDuration_ = null;  // native timeunit is milliseconds
    @SerializedName("caution_window_duration_time_unit") protected Integer cautionWindowDurationTimeUnit_ = null;
    @SerializedName("caution_stop_tracking_after") protected Long cautionStopTrackingAfter_ = null;
    @SerializedName("caution_stop_tracking_after_time_unit") protected Integer cautionStopTrackingAfterTimeUnit_ = null;
    @SerializedName("caution_minimum_sample_count") protected Integer cautionMinimumSampleCount_ = null;
    
    @SerializedName("danger_operator") protected Integer dangerOperator_ = null; 
    @SerializedName("danger_combination") protected Integer dangerCombination_ = null; 
    @SerializedName("danger_combination_count") protected Integer dangerCombinationCount_ = null;
    @SerializedName("danger_threshold") protected BigDecimal dangerThreshold_ = null; 
    @SerializedName("danger_window_duration") protected Long dangerWindowDuration_ = null; // native timeunit is milliseconds
    @SerializedName("danger_window_duration_time_unit") protected Integer dangerWindowDurationTimeUnit_ = null;
    @SerializedName("danger_stop_tracking_after") protected Long dangerStopTrackingAfter_ = null;
    @SerializedName("danger_stop_tracking_after_time_unit") protected Integer dangerStopTrackingAfterTimeUnit_ = null;
    @SerializedName("danger_minimum_sample_count") protected Integer dangerMinimumSampleCount_ = null;
    
    public AbstractAlert() {
        this.id_ = -1;
    }
    
    public AbstractAlert(Integer id, String name, Boolean isEnabled, Boolean isCautionEnabled, Boolean isDangerEnabled,
            Integer alertType, Boolean alertOnPositive, Boolean allowResendAlert, Long resendAlertEvery, Integer resendAlertEveryTimeUnit, 
            Integer cautionOperator, Integer cautionCombination, 
            Integer cautionCombinationCount, BigDecimal cautionThreshold, Long cautionWindowDuration, Integer cautionWindowDurationTimeUnit, 
            Long cautionStopTrackingAfter, Integer cautionStopTrackingAfterTimeUnit, Integer cautionMinimumSampleCount, Boolean isCautionAlertActive, 
            Timestamp cautionAlertLastSentTimestamp, Boolean isCautionAlertAcknowledged, String cautionActiveAlertsSet, Timestamp cautionFirstActiveAt, 
            Integer dangerOperator, Integer dangerCombination, 
            Integer dangerCombinationCount, BigDecimal dangerThreshold, Long dangerWindowDuration, Integer dangerWindowDurationTimeUnit, 
            Long dangerStopTrackingAfter, Integer dangerStopTrackingAfterTimeUnit, Integer dangerMinimumSampleCount, Boolean isDangerAlertActive,  
            Timestamp dangerAlertLastSentTimestamp, Boolean isDangerAlertAcknowledged, String dangerActiveAlertsSet, Timestamp dangerFirstActiveAt) {
        
        this(id, name, ((name == null) ? null : name.toUpperCase()), isEnabled, isCautionEnabled, isDangerEnabled,
             alertType, alertOnPositive, allowResendAlert, resendAlertEvery, resendAlertEveryTimeUnit, 
             cautionOperator, cautionCombination,  
             cautionCombinationCount, cautionThreshold, cautionWindowDuration, cautionWindowDurationTimeUnit, 
             cautionStopTrackingAfter, cautionStopTrackingAfterTimeUnit, cautionMinimumSampleCount, isCautionAlertActive,  
             cautionAlertLastSentTimestamp, isCautionAlertAcknowledged, cautionActiveAlertsSet, cautionFirstActiveAt, 
             dangerOperator, dangerCombination,  
             dangerCombinationCount, dangerThreshold, dangerWindowDuration, dangerWindowDurationTimeUnit, 
             dangerStopTrackingAfter, dangerStopTrackingAfterTimeUnit, dangerMinimumSampleCount, isDangerAlertActive,  
             dangerAlertLastSentTimestamp, isDangerAlertAcknowledged, dangerActiveAlertsSet, dangerFirstActiveAt);
    }

    public AbstractAlert(Integer id, String name, String uppercaseName, Boolean isEnabled, Boolean isCautionEnabled, Boolean isDangerEnabled, 
            Integer alertType, Boolean alertOnPositive, Boolean allowResendAlert, Long resendAlertEvery, 
            Integer resendAlertEveryTimeUnit, Integer cautionOperator, Integer cautionCombination, 
            Integer cautionCombinationCount, BigDecimal cautionThreshold, Long cautionWindowDuration, Integer cautionWindowDurationTimeUnit, 
            Long cautionStopTrackingAfter, Integer cautionStopTrackingAfterTimeUnit, Integer cautionMinimumSampleCount, Boolean isCautionAlertActive, 
            Timestamp cautionAlertLastSentTimestamp, Boolean isCautionAlertAcknowledged, String cautionActiveAlertsSet, Timestamp cautionFirstActiveAt, 
            Integer dangerOperator, Integer dangerCombination, 
            Integer dangerCombinationCount, BigDecimal dangerThreshold, Long dangerWindowDuration, Integer dangerWindowDurationTimeUnit, 
            Long dangerStopTrackingAfter, Integer dangerStopTrackingAfterTimeUnit, Integer dangerMinimumSampleCount, Boolean isDangerAlertActive,  
            Timestamp dangerAlertLastSentTimestamp, Boolean isDangerAlertAcknowledged, String dangerActiveAlertsSet, Timestamp dangerFirstActiveAt) {
        this.id_ = id;
        this.name_ = name;
        this.uppercaseName_ = uppercaseName;
        this.isEnabled_ = isEnabled;
        this.isCautionEnabled_ = isCautionEnabled;
        this.isDangerEnabled_ = isDangerEnabled;
        this.alertType_ = alertType;
        
        this.alertOnPositive_ = alertOnPositive;
        this.allowResendAlert_ = allowResendAlert;
        this.resendAlertEvery_ = resendAlertEvery;
        this.resendAlertEveryTimeUnit_ = resendAlertEveryTimeUnit;
        
        this.cautionOperator_ = cautionOperator;
        this.cautionCombination_ = cautionCombination;
        this.cautionCombinationCount_ = cautionCombinationCount;
        this.cautionThreshold_ = cautionThreshold;
        this.cautionWindowDuration_ = cautionWindowDuration;
        this.cautionWindowDurationTimeUnit_ = cautionWindowDurationTimeUnit;
        this.cautionStopTrackingAfter_ = cautionStopTrackingAfter;
        this.cautionStopTrackingAfterTimeUnit_ = cautionStopTrackingAfterTimeUnit;
        this.cautionMinimumSampleCount_ = cautionMinimumSampleCount;

        this.dangerOperator_ = dangerOperator;
        this.dangerCombination_ = dangerCombination;
        this.dangerCombinationCount_ = dangerCombinationCount;
        this.dangerThreshold_ = dangerThreshold;
        this.dangerWindowDuration_ = dangerWindowDuration;
        this.dangerWindowDurationTimeUnit_ = dangerWindowDurationTimeUnit;
        this.dangerStopTrackingAfter_ = dangerStopTrackingAfter;
        this.dangerStopTrackingAfterTimeUnit_ = dangerStopTrackingAfterTimeUnit;
        this.dangerMinimumSampleCount_ = dangerMinimumSampleCount;
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
       
    public DatabaseObjectValidation isCoreAlertCriteriaValid() {
        if ((name_ == null) || name_.isEmpty()) return new DatabaseObjectValidation(false, "Invalid name");
        if (alertType_ == null) return new DatabaseObjectValidation(false, "Invalid 'alert type'");
        if (isEnabled_ == null) return new DatabaseObjectValidation(false, "Invalid 'is alert enabled?' value");
        if (isCautionEnabled_ == null) return new DatabaseObjectValidation(false, "Invalid 'is caution alerting enabled?' value");
        if (isDangerEnabled_ == null) return new DatabaseObjectValidation(false, "Invalid 'is danger alerting enabled?' value");
        if (alertOnPositive_ == null) return new DatabaseObjectValidation(false, "Invalid 'alert on positive?' value");
        if (allowResendAlert_ == null) return new DatabaseObjectValidation(false, "Invalid 'resend alert?' value");
        if (allowResendAlert_ && (resendAlertEvery_ == null)) return new DatabaseObjectValidation(false, "Invalid 'resend alert frequency?' value");
        if (allowResendAlert_ && (resendAlertEveryTimeUnit_ == null)) return new DatabaseObjectValidation(false, "Invalid 'resend alert time unit' value");

        return new DatabaseObjectValidation(true);
    }
    
    public DatabaseObjectValidation isCautionAlertCriteriaValid() {
        return isCautionAlertCriteriaValid(false);
    }
    
    public DatabaseObjectValidation isCautionAlertCriteriaValid(boolean skipCheckIfCautionNotEnabled) {
        if (skipCheckIfCautionNotEnabled && (isCautionEnabled_ != null) && !isCautionEnabled_) return new DatabaseObjectValidation(true);
        
        if (alertType_ == null) return new DatabaseObjectValidation(false, "Invalid 'alert type'");
        
        if (alertType_ == TYPE_AVAILABILITY) {
            if (!isValid_CautionWindowDuration()) return new DatabaseObjectValidation(false, "Invalid caution 'window duration' value");
            if (!isValid_CautionStopTrackingAfter()) return new DatabaseObjectValidation(false, "Invalid caution 'stop tracking after' value");
        }
        else if (alertType_ == TYPE_THRESHOLD) {
            if (!isValid_CautionOperation()) return new DatabaseObjectValidation(false, "Invalid caution operation");
            if (!isValid_CautionCombination()) return new DatabaseObjectValidation(false, "Invalid caution combination");
            if (getCautionThreshold() == null) return new DatabaseObjectValidation(false, "Invalid caution threshold value");
            if (!isValid_CautionWindowDuration()) return new DatabaseObjectValidation(false, "Invalid caution 'window duration' value");
            if (!isValid_CautionMinimumSampleCount()) return new DatabaseObjectValidation(false, "Invalid caution 'minimum sample count' value");
        }
        
        return new DatabaseObjectValidation(true);
    }
    
    public DatabaseObjectValidation isDangerAlertCriteriaValid() {
        return isDangerAlertCriteriaValid(false);
    }
    
    public DatabaseObjectValidation isDangerAlertCriteriaValid(boolean skipCheckIfDangerNotEnabled) {
        if (skipCheckIfDangerNotEnabled && (isDangerEnabled_ != null) && !isDangerEnabled_) return new DatabaseObjectValidation(true);
        
        if (alertType_ == null) return new DatabaseObjectValidation(false, "Invalid 'alert type'");
        
        if (alertType_ == TYPE_AVAILABILITY) {
            if (!isValid_DangerWindowDuration()) return new DatabaseObjectValidation(false, "Invalid danger 'window duration' value");
            if (!isValid_DangerStopTrackingAfter()) return new DatabaseObjectValidation(false, "Invalid danger 'stop tracking after' value");
        }
        else if (alertType_ == TYPE_THRESHOLD) {
            if (!isValid_DangerOperation()) return new DatabaseObjectValidation(false, "Invalid danger operation");
            if (!isValid_DangerCombination()) return new DatabaseObjectValidation(false, "Invalid danger combination");
            if (getDangerThreshold() == null) return new DatabaseObjectValidation(false, "Invalid danger threshold value");
            if (!isValid_DangerWindowDuration()) return new DatabaseObjectValidation(false, "Invalid danger 'window duration' value");
            if (!isValid_DangerMinimumSampleCount()) return new DatabaseObjectValidation(false, "Invalid danger 'minimum sample count' value");
        }
        
        return new DatabaseObjectValidation(true);
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
        return getOperatorString(alertLevel, includeSymbol, includeEnglish, cautionOperator_, dangerOperator_);
    }
    
    public static String getOperatorString(int alertLevel, boolean includeSymbol, boolean includeEnglish, Integer cautionOperator, Integer dangerOperator) {
        
        if ((alertLevel == AbstractAlert.CAUTION) && (cautionOperator == null)) return null;
        if ((alertLevel == AbstractAlert.DANGER) && (dangerOperator == null)) return null;
        
        int operator = -1;
        if (alertLevel == AbstractAlert.CAUTION) operator = cautionOperator;
        else if (alertLevel == AbstractAlert.DANGER) operator = dangerOperator;
        
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
        
        if ((alertLevel == AbstractAlert.CAUTION) && (cautionCombination_ == null)) return null;
        if ((alertLevel == AbstractAlert.DANGER) && (dangerCombination_ == null)) return null;
        
        int combination = -1;
        if (alertLevel == AbstractAlert.CAUTION) combination = cautionCombination_;
        else if (alertLevel == AbstractAlert.DANGER) combination = dangerCombination_;
        
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
    
    public static String getMetricValueString_WithLabel(int alertLevel, AbstractAlert alert, BigDecimal metricValue) {
        
        if ((alert == null) || (metricValue == null) || (alert.getAlertType() == null)) {
            return null;
        }
        
        String outputString = null;

        if (alert.getAlertType() == AbstractAlert.TYPE_THRESHOLD) {
            int combination = -1;
            if (alertLevel == AbstractAlert.CAUTION) combination = alert.getCautionCombination();
            else if (alertLevel == AbstractAlert.DANGER) combination = alert.getDangerCombination();
            
            String metricValueString = metricValue.stripTrailingZeros().toPlainString();
            
            if (AbstractAlert.COMBINATION_ALL == combination) outputString = metricValueString + " (recent value)";
            else if (AbstractAlert.COMBINATION_ANY == combination) outputString = metricValueString + " (recent value)";
            else if (AbstractAlert.COMBINATION_AVERAGE == combination) outputString = metricValueString + " (avg value)";
            else if (AbstractAlert.COMBINATION_AT_LEAST_COUNT == combination) outputString = metricValueString + " (count)";
            else if (AbstractAlert.COMBINATION_AT_MOST_COUNT == combination) outputString = metricValueString + " (count)";
            else logger.warn("Unrecognized combination code");
        }
        else if (alert.getAlertType() == AbstractAlert.TYPE_AVAILABILITY) {
            BigDecimal metricValue_Seconds = metricValue.divide(new BigDecimal(1000));
            String metricValueString = metricValue_Seconds.stripTrailingZeros().toPlainString();
            outputString = metricValueString + " (seconds since last data point received)";
        }
        
        return outputString;
    }
    
    public String getHumanReadable_AlertCriteria_MinimumSampleCount(int alertLevel) {
        
        if ((alertLevel == AbstractAlert.CAUTION) && (getCautionMinimumSampleCount() == null)) return null;
        else if ((alertLevel == AbstractAlert.DANGER) && (getDangerMinimumSampleCount() == null)) return null;
        else if ((alertLevel != AbstractAlert.CAUTION) && (alertLevel != AbstractAlert.DANGER)) return null;
        
        if (alertLevel == AbstractAlert.CAUTION) return "A minimum of " + getCautionMinimumSampleCount() + " sample(s)";
        else if (alertLevel == AbstractAlert.DANGER) return "A minimum of " + getDangerMinimumSampleCount() + " sample(s)";
        else return null;
    }
    
    public String getHumanReadable_AlertCriteria_AvailabilityCriteria(int alertLevel) {
        
        if ((alertLevel != AbstractAlert.CAUTION) && (alertLevel != AbstractAlert.DANGER)) {
            return null;
        }
        
        try {
            if (alertLevel == AbstractAlert.CAUTION) {
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
            else if (alertLevel == AbstractAlert.DANGER) {
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
        
        if ((alertLevel != AbstractAlert.CAUTION) && (alertLevel != AbstractAlert.DANGER)) {
            return null;
        }
        
        try {
            if (alertLevel == AbstractAlert.CAUTION) {
                if ((getCautionWindowDuration() == null) || (getCautionWindowDurationTimeUnit() == null) || (getCautionThreshold() == null) || (getCautionOperator() == null)) return null;
                
                BigDecimal cautionWindowDuration = DatabaseObjectCommon.getValueForTimeFromMilliseconds(getCautionWindowDuration(), getCautionWindowDurationTimeUnit());
                String cautionWindowDurationTimeUnit = "";
                if (getCautionWindowDurationTimeUnit() != null) cautionWindowDurationTimeUnit = DatabaseObjectCommon.getTimeUnitStringFromCode(getCautionWindowDurationTimeUnit(), true);

                StringBuilder humanReadableThresholdCriteria = new StringBuilder();
                humanReadableThresholdCriteria.append(getHumanReadable_ThresholdCriteria_Combination(AbstractAlert.CAUTION)).append(" ").append(getOperatorString(AbstractAlert.CAUTION, false, true))
                    .append(" ").append(getCautionThreshold().stripTrailingZeros().toPlainString())
                    .append(" during the last ").append(cautionWindowDuration.stripTrailingZeros().toPlainString()).append(" ").append(cautionWindowDurationTimeUnit);

                return humanReadableThresholdCriteria.toString();
            }
            else if (alertLevel == AbstractAlert.DANGER) {
                if ((getDangerWindowDuration() == null) || (getDangerWindowDurationTimeUnit() == null) || (getDangerThreshold() == null) || (getDangerOperator() == null)) return null;
                
                BigDecimal dangerWindowDuration = DatabaseObjectCommon.getValueForTimeFromMilliseconds(getDangerWindowDuration(), getDangerWindowDurationTimeUnit());
                String dangerWindowDurationTimeUnit = "";
                if (getDangerWindowDurationTimeUnit() != null) dangerWindowDurationTimeUnit = DatabaseObjectCommon.getTimeUnitStringFromCode(getDangerWindowDurationTimeUnit(), true);

                StringBuilder humanReadableThresholdCriteria = new StringBuilder();
                humanReadableThresholdCriteria.append(getHumanReadable_ThresholdCriteria_Combination(AbstractAlert.DANGER)).append(" ").append(getOperatorString(AbstractAlert.DANGER, false, true))
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
        
        if ((alertLevel != AbstractAlert.CAUTION) && (alertLevel != AbstractAlert.DANGER)) {
            return null;
        }

        Integer combination = null;
        if (alertLevel == AbstractAlert.CAUTION) combination = getCautionCombination();
        else if (alertLevel == AbstractAlert.DANGER) combination = getDangerCombination();
        
        Integer combinationCount = null;
        if (alertLevel == AbstractAlert.CAUTION) combinationCount = getCautionCombinationCount();
        else if (alertLevel == AbstractAlert.DANGER) combinationCount = getDangerCombinationCount();
        
        if (combination != null) {
            if (Objects.equals(combination, AbstractAlert.COMBINATION_ANY)) return "Any metric value was";
            else if (Objects.equals(combination, AbstractAlert.COMBINATION_ALL)) return "All metric values were";
            else if (Objects.equals(combination, AbstractAlert.COMBINATION_AVERAGE)) return "The average metric value was";
            else if (Objects.equals(combination, AbstractAlert.COMBINATION_AT_MOST_COUNT) && (combinationCount != null)) return "At most " + combinationCount + " metric values were";
            else if (Objects.equals(combination, AbstractAlert.COMBINATION_AT_LEAST_COUNT) && (combinationCount != null)) return "At least " + combinationCount + " metric values were";
            else return null;
        }
        else return null;
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

}
