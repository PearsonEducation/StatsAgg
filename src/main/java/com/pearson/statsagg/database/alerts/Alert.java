package com.pearson.statsagg.database.alerts;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;
import com.pearson.statsagg.database.DatabaseObject;
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
    // public static final Integer CRITERIA_NTH_PERCENTILE = 104;
    public static final Integer COMBINATION_AT_MOST_COUNT = 105;
    public static final Integer COMBINATION_AT_LEAST_COUNT = 106;
     
    private Integer id_;
    private String name_ = null;
    private String uppercaseName_ = null;
    private String description_ = null;
    private Integer metricGroupId_ = null;
    private Integer notificationGroupId_ = null;
    private Boolean isEnabled_ = null;
    
    private Boolean alertOnPositive_ = null;
    private Boolean allowResendAlert_ = null;
    private Integer sendAlertEveryNumMilliseconds_ = null;

    private Integer cautionAlertType_ = null;
    private Integer cautionOperator_ = null;
    private Integer cautionCombination_ = null; 
    private Integer cautionCombinationCount_ = null;
    private BigDecimal cautionThreshold_ = null; 
    private Integer cautionWindowDuration_ = null;
    private Integer cautionMinimumSampleCount_ = null;
    private Boolean isCautionAlertActive_ = null;
    private Timestamp cautionAlertLastSentTimestamp_ = null;
    private String cautionActiveAlertsSet_ = null;
    
    private Integer dangerAlertType_ = null;
    private Integer dangerOperator_ = null; 
    private Integer dangerCombination_ = null; 
    private Integer dangerCombinationCount_ = null;
    private BigDecimal dangerThreshold_ = null; 
    private Integer dangerWindowDuration_ = null;
    private Integer dangerMinimumSampleCount_ = null;
    private Boolean isDangerAlertActive_ = null;
    private Timestamp dangerAlertLastSentTimestamp_ = null;
    private String dangerActiveAlertsSet_ = null;

    public Alert() {
        this.id_ = -1;
    }
    
    public Alert(Integer id, String name, String description, Integer metricGroupId, Integer notificationGroupId, Boolean isEnabled, 
            Boolean alertOnPositive, Boolean allowResendAlert, Integer sendAlertEveryNumMilliseconds, 
            Integer cautionAlertType, Integer cautionOperator, Integer cautionCombination, Integer cautionCombinationCount, BigDecimal cautionThreshold, 
            Integer cautionWindowDuration, Integer cautionMinimumSampleCount, Boolean isCautionAlertActive, Timestamp cautionAlertLastSentTimestamp, String cautionActiveAlertsSet, 
            Integer dangerAlertType, Integer dangerOperator, Integer dangerCombination, Integer dangerCombinationCount, BigDecimal dangerThreshold, 
            Integer dangerWindowDuration, Integer dangerMinimumSampleCount, Boolean isDangerAlertActive, Timestamp dangerAlertLastSentTimestamp, String dangerActiveAlertsSet) {
        
        this(id, name, ((name == null) ? null : name.toUpperCase()), description, metricGroupId, notificationGroupId, isEnabled, 
             alertOnPositive, allowResendAlert, sendAlertEveryNumMilliseconds, 
             cautionAlertType, cautionOperator, cautionCombination, cautionCombinationCount, cautionThreshold, 
             cautionWindowDuration, cautionMinimumSampleCount, isCautionAlertActive, cautionAlertLastSentTimestamp, cautionActiveAlertsSet, 
             dangerAlertType, dangerOperator, dangerCombination, dangerCombinationCount, dangerThreshold, 
             dangerWindowDuration, dangerMinimumSampleCount, isDangerAlertActive, dangerAlertLastSentTimestamp, dangerActiveAlertsSet);
    }

    public Alert(Integer id, String name, String uppercaseName, String description, Integer metricGroupId, Integer notificationGroupId, Boolean isEnabled, 
            Boolean alertOnPositive, Boolean allowResendAlert, Integer sendAlertEveryNumMilliseconds, 
            Integer cautionAlertType, Integer cautionOperator, Integer cautionCombination, Integer cautionCombinationCount, BigDecimal cautionThreshold, 
            Integer cautionWindowDuration, Integer cautionMinimumSampleCount, Boolean isCautionAlertActive, Timestamp cautionAlertLastSentTimestamp, String cautionActiveAlertsSet, 
            Integer dangerAlertType, Integer dangerOperator, Integer dangerCombination, Integer dangerCombinationCount, BigDecimal dangerThreshold, 
            Integer dangerWindowDuration, Integer dangerMinimumSampleCount, Boolean isDangerAlertActive, Timestamp dangerAlertLastSentTimestamp, String dangerActiveAlertsSet) {
        this.id_ = id;
        this.name_ = name;
        this.uppercaseName_ = uppercaseName;
        this.description_ = description;
        this.metricGroupId_ = metricGroupId;
        this.notificationGroupId_ = notificationGroupId;
        this.isEnabled_ = isEnabled;
        
        this.alertOnPositive_ = alertOnPositive;
        this.allowResendAlert_ = allowResendAlert;
        this.sendAlertEveryNumMilliseconds_ = sendAlertEveryNumMilliseconds;
        
        this.cautionAlertType_ = cautionAlertType;
        this.cautionOperator_ = cautionOperator;
        this.cautionCombination_ = cautionCombination;
        this.cautionCombinationCount_ = cautionCombinationCount;
        this.cautionThreshold_ = cautionThreshold;
        this.cautionWindowDuration_ = cautionWindowDuration;
        this.cautionMinimumSampleCount_ = cautionMinimumSampleCount;
        this.isCautionAlertActive_ = isCautionAlertActive;
        if (cautionAlertLastSentTimestamp == null) this.cautionAlertLastSentTimestamp_ = null;
        else this.cautionAlertLastSentTimestamp_ = (Timestamp) cautionAlertLastSentTimestamp.clone();
        this.cautionActiveAlertsSet_ = cautionActiveAlertsSet;

        this.dangerAlertType_ = dangerAlertType;
        this.dangerOperator_ = dangerOperator;
        this.dangerCombination_ = dangerCombination;
        this.dangerCombinationCount_ = dangerCombinationCount;
        this.dangerThreshold_ = dangerThreshold;
        this.dangerWindowDuration_ = dangerWindowDuration;
        this.dangerMinimumSampleCount_ = dangerMinimumSampleCount;
        this.isDangerAlertActive_ = isDangerAlertActive;
        if (dangerAlertLastSentTimestamp == null) this.dangerAlertLastSentTimestamp_ = null;
        else this.dangerAlertLastSentTimestamp_ = (Timestamp) dangerAlertLastSentTimestamp.clone();
        this.dangerActiveAlertsSet_ = dangerActiveAlertsSet;
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
        alertCopy.setNotificationGroupId(alert.getNotificationGroupId());
        alertCopy.setIsEnabled(alert.isEnabled());
        
        alertCopy.setAlertOnPositive(alert.isAlertOnPositive());
        alertCopy.setAllowResendAlert(alert.isAllowResendAlert());
        alertCopy.setSendAlertEveryNumMilliseconds(alert.getSendAlertEveryNumMilliseconds());

        alertCopy.setCautionAlertType(alert.getCautionAlertType());
        alertCopy.setCautionOperator(alert.getCautionOperator());
        alertCopy.setCautionCombination(alert.getCautionCombination());
        alertCopy.setCautionCombinationCount(alert.getCautionCombinationCount());
        alertCopy.setCautionThreshold(alert.getCautionThreshold());
        alertCopy.setCautionWindowDuration(alert.getCautionWindowDuration());
        alertCopy.setCautionMinimumSampleCount(alert.getCautionMinimumSampleCount());
        alertCopy.setIsCautionAlertActive(alert.isCautionAlertActive());
        if (alert.getCautionAlertLastSentTimestamp() == null) alertCopy.setCautionAlertLastSentTimestamp(null);
        else alertCopy.setCautionAlertLastSentTimestamp(new Timestamp(alert.getCautionAlertLastSentTimestamp().getTime()));
        alertCopy.setCautionActiveAlertsSet(alert.getCautionActiveAlertsSet());
        
        alertCopy.setDangerAlertType(alert.getDangerAlertType());
        alertCopy.setDangerOperator(alert.getDangerOperator());
        alertCopy.setDangerCombination(alert.getDangerCombination());
        alertCopy.setDangerCombinationCount(alert.getDangerCombinationCount());
        alertCopy.setDangerThreshold(alert.getDangerThreshold());
        alertCopy.setDangerWindowDuration(alert.getDangerWindowDuration());
        alertCopy.setDangerMinimumSampleCount(alert.getDangerMinimumSampleCount());
        alertCopy.setIsDangerAlertActive(alert.isDangerAlertActive());
        if (alert.getDangerAlertLastSentTimestamp() == null) alertCopy.setDangerAlertLastSentTimestamp(null);
        else alertCopy.setDangerAlertLastSentTimestamp(new Timestamp(alert.getDangerAlertLastSentTimestamp().getTime()));
        alertCopy.setDangerActiveAlertsSet(alert.getDangerActiveAlertsSet());
        
        return alertCopy;
    }
    
    @Override
    public boolean isEqual(Alert alert) {
       
        if (alert == null) return false;
        if (alert == this) return true;
        if (alert.getClass() != getClass()) return false;
        
        boolean isCautionThresholdValueEqual = false;
        if ((cautionThreshold_ != null) && (alert.getCautionThreshold() != null)) {
            isCautionThresholdValueEqual = cautionThreshold_.compareTo(alert.getCautionThreshold()) == 0;
        }
        else if (cautionThreshold_ == null) {
            isCautionThresholdValueEqual = alert.getCautionThreshold() == null;
        }
        
        boolean isDangerThresholdValueEqual = false;
        if ((dangerThreshold_ != null) && (alert.getDangerThreshold() != null)) {
            isDangerThresholdValueEqual = dangerThreshold_.compareTo(alert.getDangerThreshold()) == 0;
        }
        else if (dangerThreshold_ == null) {
            isDangerThresholdValueEqual = alert.getDangerThreshold() == null;
        }
        
        return new EqualsBuilder()
                .append(id_, alert.getId())
                .append(name_, alert.getName())
                .append(uppercaseName_, alert.getUppercaseName())
                .append(description_, alert.getDescription())
                .append(metricGroupId_, alert.getMetricGroupId())
                .append(notificationGroupId_, alert.getNotificationGroupId())
                .append(isEnabled_, alert.isEnabled())
                .append(alertOnPositive_, alert.isAlertOnPositive())
                .append(allowResendAlert_, alert.isAllowResendAlert())
                .append(sendAlertEveryNumMilliseconds_, alert.getSendAlertEveryNumMilliseconds())
                .append(cautionAlertType_, alert.getCautionAlertType())
                .append(cautionOperator_, alert.getCautionOperator())
                .append(cautionCombination_, alert.getCautionCombination())
                .append(cautionCombinationCount_, alert.getCautionCombinationCount())
                .append(isCautionThresholdValueEqual, true)
                .append(cautionWindowDuration_, alert.getCautionWindowDuration())
                .append(cautionMinimumSampleCount_, alert.getCautionMinimumSampleCount())
                .append(isCautionAlertActive_, alert.isCautionAlertActive())
                .append(cautionAlertLastSentTimestamp_, alert.getCautionAlertLastSentTimestamp())
                .append(cautionActiveAlertsSet_, alert.getCautionActiveAlertsSet())
                .append(dangerAlertType_, alert.getDangerAlertType())
                .append(dangerOperator_, alert.getDangerOperator())
                .append(dangerCombination_, alert.getDangerCombination())
                .append(dangerCombinationCount_, alert.getDangerCombinationCount())
                .append(isDangerThresholdValueEqual, true)
                .append(dangerWindowDuration_, alert.getDangerWindowDuration())
                .append(dangerMinimumSampleCount_, alert.getDangerMinimumSampleCount())
                .append(isDangerAlertActive_, alert.isDangerAlertActive())
                .append(dangerAlertLastSentTimestamp_, alert.getDangerAlertLastSentTimestamp())
                .append(dangerActiveAlertsSet_, alert.getDangerActiveAlertsSet())
                .isEquals();
    }
    
    public Integer getLongestWindowDuration() {
        
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
        
        if (cautionAlertType_ == null) return false;
        
        if (cautionAlertType_ == TYPE_AVAILABILITY) {
            if (!isValid_CautionWindowDuration()) return false;
        }
        else if (cautionAlertType_ == TYPE_THRESHOLD) {
            if (!isValid_CautionOperation()) return false;
            if (!isValid_CautionCombination()) return false;
            if (getCautionThreshold() == null) return false;
            if (!isValid_CautionWindowDuration()) return false;
            if (!isValid_CautionMinimumSampleCount()) return false;
        }
        
        return true;
    }
    
    public boolean isDangerAlertCriteriaValid() {
        
        if (dangerAlertType_ == null) return false;
        
        if (dangerAlertType_ == TYPE_AVAILABILITY) {
            if (!isValid_DangerWindowDuration()) return false;
        }
        else if (dangerAlertType_ == TYPE_THRESHOLD) {
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
    
    public boolean isValid_DangerWindowDuration() {
        
        if (dangerWindowDuration_ == null) {
            return false;
        } 
        
        return dangerWindowDuration_ >= 1;
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
        
        if ((alert == null) || (metricValue == null)) {
            return null;
        }
        
        String outputString = null;
        String metricValueString = metricValue.stripTrailingZeros().toPlainString();

        if (Objects.equals(Alert.COMBINATION_ALL, alert.getCautionCombination())) outputString = metricValueString + " (recent value)";
        if (Objects.equals(Alert.COMBINATION_ANY, alert.getCautionCombination())) outputString = metricValueString + " (recent value)";
        if (Objects.equals(Alert.COMBINATION_AVERAGE, alert.getCautionCombination())) outputString = metricValueString + " (avg value)";
        if (Objects.equals(Alert.COMBINATION_AT_LEAST_COUNT, alert.getCautionCombination())) outputString = metricValueString + " (count)";
        if (Objects.equals(Alert.COMBINATION_AT_MOST_COUNT, alert.getCautionCombination())) outputString = metricValueString + " (count)";

        return outputString;
    }
    
    public static String getDangerMetricValueString_WithLabel(Alert alert, BigDecimal alertMetricValue) {
        
        if ((alert == null) || (alertMetricValue == null)) {
            return null;
        }
        
        String outputString = null;
        String metricValueString = alertMetricValue.stripTrailingZeros().toPlainString();

        if (Objects.equals(Alert.COMBINATION_ALL, alert.getDangerCombination())) outputString = metricValueString + " (recent value)";
        if (Objects.equals(Alert.COMBINATION_ANY, alert.getDangerCombination())) outputString = metricValueString + " (recent value)";
        if (Objects.equals(Alert.COMBINATION_AVERAGE, alert.getDangerCombination())) outputString = metricValueString + " (avg value)";
        if (Objects.equals(Alert.COMBINATION_AT_LEAST_COUNT, alert.getDangerCombination())) outputString = metricValueString + " (count)";
        if (Objects.equals(Alert.COMBINATION_AT_MOST_COUNT, alert.getDangerCombination())) outputString = metricValueString + " (count)";

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
    
    public Integer getNotificationGroupId() {
        return notificationGroupId_;
    }
    
    public void setNotificationGroupId(Integer notificationGroupId) {
        this.notificationGroupId_ = notificationGroupId;
    }

    public Boolean isEnabled() {
        return isEnabled_;
    }
    
    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled_ = isEnabled;
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
    
    public Integer getCautionAlertType() {
        return cautionAlertType_;
    }

    public void setCautionAlertType(Integer cautionAlertType) {
        this.cautionAlertType_ = cautionAlertType;
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
    
    public Integer getCautionWindowDuration() {
        return cautionWindowDuration_;
    }
    
    public void setCautionWindowDuration(Integer cautionWindowDuration) {
        this.cautionWindowDuration_ = cautionWindowDuration;
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

    public String getCautionActiveAlertsSet() {
        return cautionActiveAlertsSet_;
    }

    public void setCautionActiveAlertsSet(String cautionActiveAlertsSet) {
        this.cautionActiveAlertsSet_ = cautionActiveAlertsSet;
    }

    public Integer getDangerAlertType() {
        return dangerAlertType_;
    }

    public void setDangerAlertType(Integer dangerAlertType) {
        this.dangerAlertType_ = dangerAlertType;
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
    
    public Integer getDangerWindowDuration() {
        return dangerWindowDuration_;
    }

    public void setDangerWindowDuration(Integer dangerWindowDuration) {
        this.dangerWindowDuration_ = dangerWindowDuration;
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

    public String getDangerActiveAlertsSet() {
        return dangerActiveAlertsSet_;
    }

    public void setDangerActiveAlertsSet(String dangerActiveAlertsSet) {
        this.dangerActiveAlertsSet_ = dangerActiveAlertsSet;
    }

}
