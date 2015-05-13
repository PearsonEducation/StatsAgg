package com.pearson.statsagg.alerts;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.pearson.statsagg.database.alerts.Alert;
import com.pearson.statsagg.database.metric_group.MetricGroup;
import com.pearson.statsagg.database.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database.notifications.NotificationGroup;
import com.pearson.statsagg.database.notifications.NotificationGroupsDao;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.webui.StatsAggHtmlFramework;
import com.pearson.statsagg.utilities.EmailUtils;
import com.pearson.statsagg.utilities.StackTrace;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class EmailThread implements Runnable  {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailThread.class.getName());
    
    public static final int WARNING_LEVEL_CAUTION = 1;
    public static final int WARNING_LEVEL_DANGER = 2;
    
    private final Alert alert_;
    private final int warningLevel_;
    private final List<String> metricKeys_;
    private final Map<String,BigDecimal> alertMetricValues_;  // k="{MetricKey}-{AlertId}"   example: "someMetric.example-55"
    private final Map<String,String> positiveAlertReasons_; // k=MetricMey, v=a reason why the alert was positive (ex "new data point detected")
    private final boolean isPositiveAlert_;
    private final boolean isResend_;
    private final String statsAggLocation_;
    
    private String subject_ = "";
    private String body_ = "";
    
    public EmailThread(Alert alert, int warningLevel, List<String> metricKeys, Map<String,BigDecimal> alertMetricValues, 
            Map<String,String> positiveAlertReasons, boolean isPositiveAlert, boolean isResend, String statsAggLocation) {
        this.alert_ = alert;
        this.warningLevel_ = warningLevel;
        this.metricKeys_ = metricKeys;
        this.alertMetricValues_ = alertMetricValues;
        this.positiveAlertReasons_ = positiveAlertReasons;
        this.isPositiveAlert_ = isPositiveAlert;
        this.isResend_ = isResend;
        this.statsAggLocation_ = statsAggLocation;
    }
    
    @Override
    public void run() {
        
        if (alert_ == null) {
            logger.warn("Alert cannot be null");
            return;
        }
        
        MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
        MetricGroup metricGroup = metricGroupsDao.getMetricGroup(alert_.getMetricGroupId());
        
        buildAlertEmail(ApplicationConfiguration.getAlertMaxMetricsInEmail(), metricGroup);
        
        List<String> emailAddesses;
        if (warningLevel_ == WARNING_LEVEL_CAUTION) emailAddesses = getToEmailsAddressesForAlert(alert_.getCautionNotificationGroupId());
        else if (warningLevel_ == WARNING_LEVEL_DANGER) emailAddesses = getToEmailsAddressesForAlert(alert_.getDangerNotificationGroupId());
        else emailAddesses = new ArrayList();
        
        if (ApplicationConfiguration.isAlertSendEmailEnabled() && !emailAddesses.isEmpty()) {
            sendEmail(emailAddesses, subject_, body_);
        }
    }
    
    public void buildAlertEmail(int numMetricKeysPerEmail, MetricGroup metricGroup) {
    
        if ((alert_ == null) || (warningLevel_ < 1) || (warningLevel_ > 2) || (metricGroup == null)) {
            logger.error("Failed to create email alert message.");
            return;
        }
        
        String warningLevelString = null;
        if (warningLevel_ == WARNING_LEVEL_CAUTION) warningLevelString = "Caution";
        else if (warningLevel_ == WARNING_LEVEL_DANGER) warningLevelString = "Danger";    
                
        if (isPositiveAlert_) subject_ = "StatsAgg Positive Alert, " + warningLevelString + ", Name=\"" + alert_.getName() + "\"";
        else subject_ = "StatsAgg Alert, " + warningLevelString + ", Name=\"" + alert_.getName() + "\"";

        Calendar currentDateAndTime = Calendar.getInstance();
        SimpleDateFormat dateAndTimeFormat = new SimpleDateFormat("yyyy-MM-dd  h:mm:ss a  z");
        String formattedDateAndTime = dateAndTimeFormat.format(currentDateAndTime.getTime());
        
        String alertTriggeredAt = null;
        if (isResend_ && (warningLevel_ == WARNING_LEVEL_CAUTION) && (alert_.getCautionFirstActiveAt() != null)) {
            long secondsBetweenNowAndFirstAlerted = (long) ((currentDateAndTime.getTimeInMillis() - alert_.getCautionFirstActiveAt().getTime()) / 1000);
            alertTriggeredAt = getAmountOfTimeAlertIsTriggered(secondsBetweenNowAndFirstAlerted);
        }
        if (isResend_ && (warningLevel_ == WARNING_LEVEL_DANGER) && (alert_.getDangerFirstActiveAt() != null)) {
            long secondsBetweenNowAndFirstAlerted = (long) ((currentDateAndTime.getTimeInMillis() - alert_.getDangerFirstActiveAt().getTime()) / 1000);
            alertTriggeredAt = getAmountOfTimeAlertIsTriggered(secondsBetweenNowAndFirstAlerted);
        }
        
        StringBuilder body = new StringBuilder("<html>");
        
        if ((statsAggLocation_ != null) && !statsAggLocation_.isEmpty()) {
            body.append("<b>StatsAgg Server</b> = ").append("<a href=\"").append(statsAggLocation_).append("\">").append(statsAggLocation_).append("</a>").append("<br>");
        }
        
        if ((warningLevel_ == WARNING_LEVEL_CAUTION) && (statsAggLocation_ != null) && !statsAggLocation_.isEmpty() && (alert_ != null) && (alert_.getName() != null)) {
            body.append("<b>Currently triggered metrics</b> = ").append("<a href=\"").append(statsAggLocation_).append("/AlertAssociations?Name=")
                    .append(StatsAggHtmlFramework.urlEncode(alert_.getName())).append("&Level=" + "Caution").append("\">");
            body.append("Triggered caution metrics").append("</a>").append("<br>");
        }
        
        if ((warningLevel_ == WARNING_LEVEL_DANGER) && (statsAggLocation_ != null) && !statsAggLocation_.isEmpty() && (alert_ != null) && (alert_.getName() != null)) {
            body.append("<b>Currently triggered metrics</b> = ").append("<a href=\"").append(statsAggLocation_).append("/AlertAssociations?Name=")
                    .append(StatsAggHtmlFramework.urlEncode(alert_.getName())).append("&Level=" + "Danger").append("\">");
            body.append("Triggered danger metrics").append("</a>").append("<br>");
        }
        
        body.append("<b>Current Time</b> = ").append(formattedDateAndTime).append("<br>");
        if (alertTriggeredAt != null) body.append("<b>Alert triggered time</b> = ").append(alertTriggeredAt).append("<br>");
        body.append("<b>Alert Name</b> = ").append(StatsAggHtmlFramework.htmlEncode(alert_.getName())).append("<br>");
        
        body.append("<b>Alert Description</b> = ");
        if (alert_.getDescription() != null) body.append(StatsAggHtmlFramework.htmlEncode(alert_.getDescription()).replaceAll("\n", "<br>&nbsp;&nbsp;&nbsp;")).append("<br>");
        else body.append("<br>");
        
        body.append("<b>Metric Group Name</b> = ");
        if (metricGroup.getName() != null) body.append(StatsAggHtmlFramework.htmlEncode(metricGroup.getName())).append("<br>");
        else body.append("<br>");
        
        body.append("<b>Metric Group Description</b> = ");
        if (metricGroup.getDescription() != null) body.append(StatsAggHtmlFramework.htmlEncode(metricGroup.getDescription()).replaceAll("\n", "<br>&nbsp;&nbsp;&nbsp;")).append("<br><br>");
        else body.append("<br>");
        
        if (isPositiveAlert_) {
            body.append("<b>No metrics associated with the metric group \"").append(StatsAggHtmlFramework.htmlEncode(metricGroup.getName()))
                    .append("\" meet the following criteria:</b>").append("<br>");
        }
        else {
            body.append("<b>One or more metrics associated with the metric group \"").append(StatsAggHtmlFramework.htmlEncode(metricGroup.getName()))
                    .append("\" meet the following criteria:</b>").append("<br>");
        }
        
        if (warningLevel_ == WARNING_LEVEL_CAUTION) {
            BigDecimal cautionWindowDurationMs = new BigDecimal(alert_.getCautionWindowDuration());
            BigDecimal cautionWindowDurationSeconds = cautionWindowDurationMs.divide(new BigDecimal(1000));
            
            if (alert_.getAlertType() == Alert.TYPE_AVAILABILITY) {
                body.append("<ul><li>No new data points were received during the last ").append(cautionWindowDurationSeconds.stripTrailingZeros().toPlainString()).append(" seconds").append("</li></ul><br>");
            }
            else if (alert_.getAlertType() == Alert.TYPE_THRESHOLD) {
                body.append("<ul><li>A minimum of ").append(alert_.getCautionMinimumSampleCount()).append(" sample(s)").append("</li>");
                body.append("<li>").append(getCautionCombinationString(alert_)).append(" ").append(alert_.getCautionOperatorString(false, true))
                        .append(" ").append(alert_.getCautionThreshold().stripTrailingZeros().toPlainString())
                        .append(" during the last ").append(cautionWindowDurationSeconds.stripTrailingZeros().toPlainString()).append(" seconds").append("</li></ul><br>");
            }
        }
        else if (warningLevel_ == WARNING_LEVEL_DANGER) {
            BigDecimal dangerWindowDurationMs = new BigDecimal(alert_.getDangerWindowDuration());
            BigDecimal dangerWindowDurationSeconds = dangerWindowDurationMs.divide(new BigDecimal(1000));

            if (alert_.getAlertType() == Alert.TYPE_AVAILABILITY) {
                body.append("<ul><li>No new data points were received during the last ").append(dangerWindowDurationSeconds.stripTrailingZeros().toPlainString()).append(" seconds").append("</li></ul><br>");
            }
            else if (alert_.getAlertType() == Alert.TYPE_THRESHOLD) {
                body.append("<ul><li>A minimum of ").append(alert_.getDangerMinimumSampleCount()).append(" sample(s)").append("</li>");
                body.append("<li>").append(getDangerCombinationString(alert_)).append(" ").append(alert_.getDangerOperatorString(false, true))
                        .append(" ").append(alert_.getDangerThreshold().stripTrailingZeros().toPlainString())
                        .append(" during the last ").append(dangerWindowDurationSeconds.stripTrailingZeros().toPlainString()).append(" seconds").append("</li></ul><br>"); 
            }
        }
        
        if (!isPositiveAlert_) {
            List<String> sortedMetricKeys = sortAndLimitMetricsForEmail(metricKeys_, numMetricKeysPerEmail);
            int counter = 0;
            
            body.append("<b>The following metric(s) are in an alerted state:</b>").append("<br>");
            body.append("<ul>");

            for (String metricKey : sortedMetricKeys) {
                body.append("<li>").append(StatsAggHtmlFramework.htmlEncode(metricKey));
                
                BigDecimal alertMetricValue = alertMetricValues_.get(metricKey + "-" + alert_.getId());
                if (alertMetricValue != null) {
                    String metricValueString_WithLabel = null;
                    if (warningLevel_ == WARNING_LEVEL_CAUTION) {
                            body.append(" = ");
                            metricValueString_WithLabel = Alert.getCautionMetricValueString_WithLabel(alert_, alertMetricValue);
                    }
                    else if (warningLevel_ == WARNING_LEVEL_DANGER) {
                            body.append(" = ");
                            metricValueString_WithLabel = Alert.getDangerMetricValueString_WithLabel(alert_, alertMetricValue);
                    }
                        
                    if (metricValueString_WithLabel != null) {
                        body.append(StatsAggHtmlFramework.htmlEncode(metricValueString_WithLabel));
                    }
                }
                
                body.append("</li>");
                
                counter++;
            }
            
            if (counter == numMetricKeysPerEmail) { 
                body.append("<li>... ").append(metricKeys_.size() - numMetricKeysPerEmail).append(" more").append("</li>");
            }
            
            body.append("</ul>");
        }
        else {
            List<String> unsortedMetricKeys = new ArrayList<>();
            if ((warningLevel_ == WARNING_LEVEL_CAUTION) && (alert_.getCautionActiveAlertsSet() != null)) {
                String[] cautionActiveAlertsSet = StringUtils.split(alert_.getCautionActiveAlertsSet(), '\n');
                if ((cautionActiveAlertsSet != null) && (cautionActiveAlertsSet.length > 0)) unsortedMetricKeys.addAll(Arrays.asList(cautionActiveAlertsSet));
            }
            else if ((warningLevel_ == WARNING_LEVEL_DANGER) && (alert_.getDangerActiveAlertsSet() != null)) {
                String[] dangerActiveAlertsSet = StringUtils.split(alert_.getDangerActiveAlertsSet(), '\n');
                if ((dangerActiveAlertsSet != null) && (dangerActiveAlertsSet.length > 0)) unsortedMetricKeys.addAll(Arrays.asList(dangerActiveAlertsSet));
            }
            
            List<String> sortedMetricKeys = sortAndLimitMetricsForEmail(unsortedMetricKeys, numMetricKeysPerEmail);
            
            body.append("<b>The following metric(s) are no longer in an alerted state:</b>").append("<br>");
            body.append("<ul>");

            int counter = 0;
            
            for (String metricKey : sortedMetricKeys) {
                body.append("<li>");
                body.append(StatsAggHtmlFramework.htmlEncode(metricKey));
                
                if ((positiveAlertReasons_ != null) && positiveAlertReasons_.containsKey(metricKey)) {
                    body.append(" = ").append(StatsAggHtmlFramework.htmlEncode(positiveAlertReasons_.get(metricKey)));
                }
                
                body.append("</li>");
                
                counter++;
            }

            if ((counter == numMetricKeysPerEmail) && (counter < unsortedMetricKeys.size())) { 
                body.append("<li>... ").append("and more").append("</li>");
            }
                
            body.append("</ul>");
        }
        
        body.append("</html>");
        
        body_ = body.toString();
        
        String cleanSubject = StatsAggHtmlFramework.removeNewlinesFromString(subject_, ' ');
        String cleanBody = StatsAggHtmlFramework.removeNewlinesFromString(body_, ' ');
        logger.debug(cleanSubject + "\n" + cleanBody);
    }
    
    public void sendEmail(List<String> emailAddesses, String emailSubject, String emailBody) {
        
        sendEmail(ApplicationConfiguration.getAlertSmtpHost(), ApplicationConfiguration.getAlertSmtpPort(),
                ApplicationConfiguration.getAlertSmtpUsername(), ApplicationConfiguration.getAlertSmtpPassword(),
                ApplicationConfiguration.getAlertSmtpConnectionTimeout(),
                ApplicationConfiguration.isAlertSmtpUseSslTls(), ApplicationConfiguration.isAlertSmtpUseStartTls(),
                ApplicationConfiguration.getAlertSmtpFromAddress(), ApplicationConfiguration.getAlertSmtpFromName(), 
                emailAddesses, emailSubject, emailBody);
        
    }
    
    private void sendEmail(String smtpHost, int smtpPort, String username, String password, int connectionTimeout, boolean useSslTls, boolean useStartTls, 
            String fromAddress, String fromName, List<String> toAddresses, String emailSubject, String emailBody) {
        
        if (toAddresses.isEmpty()) {
            String cleanSubject = StatsAggHtmlFramework.removeNewlinesFromString(emailSubject, ' ');
            logger.debug("Message=\"Failed to send email alert. No valid recipients.\", EmailSubject=\"" + cleanSubject + "\"");
            return;
        }
        
        try {
            HtmlEmail email = new HtmlEmail();

            email.setHostName(smtpHost);
            email.setSmtpPort(smtpPort);
            email.setAuthenticator(new DefaultAuthenticator(username, password));
            email.setSocketTimeout(connectionTimeout);
            email.setSSLOnConnect(useSslTls);
            email.setStartTLSEnabled(useStartTls);
            email.setFrom(fromAddress, fromName);
            
            for (String toAddress : toAddresses) {
                email.addTo(toAddress);
            }
            
            email.setSubject(emailSubject);
            email.setHtmlMsg(emailBody);
            email.send();
            
            String cleanSubject = StatsAggHtmlFramework.removeNewlinesFromString(emailSubject, ' ');
            String cleanBody = StatsAggHtmlFramework.removeNewlinesFromString(emailBody, ' ');
            logger.info("Message=\"Send email alert\", EmailSubject=\"" + cleanSubject + "\"" + ", EmailBody=\"" + cleanBody + "\"");
        }
        catch (Exception e) {
            String cleanSubject = StatsAggHtmlFramework.removeNewlinesFromString(emailSubject, ' ');
            logger.error("Message=\"Failed to send email alert. SMTP failure.\", " + "EmailSubject=\"" + cleanSubject + "\", " +
                    e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }

    public static List<String> sortAndLimitMetricsForEmail(List<String> metricKeys, int maxAllowedMetrics) {
        
        if ((metricKeys == null) || metricKeys.isEmpty() || (maxAllowedMetrics < 0)) {
            return new ArrayList<>(); 
        }
        
        List<String> sortedMetricKeys = new ArrayList<>();
        int numMetricsToReturn = metricKeys.size();
        if (maxAllowedMetrics < numMetricsToReturn) numMetricsToReturn = maxAllowedMetrics;
        
        for (int i = 0; i < numMetricsToReturn; i++) {
            sortedMetricKeys.add(metricKeys.get(i));
        }
        
        Collections.sort(sortedMetricKeys);
        
        return sortedMetricKeys;
    }
    
    public static List<String> getToEmailsAddressesForAlert(Integer notificationGroupId) {
        
        if (notificationGroupId == null) {
            return new ArrayList<>();
        }
        
        ArrayList emailAddessesList = new ArrayList<>();
        
        NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
        NotificationGroup notificationGroup = notificationGroupsDao.getNotificationGroup(notificationGroupId);

        if ((notificationGroup != null) &&  (notificationGroup.getEmailAddresses() != null)) {
            String[] emailAddresses = StringUtils.split(notificationGroup.getEmailAddresses(), ",");
            
            for (String emailAddress : emailAddresses) {
                String trimmedEmailAddress = emailAddress.trim();
                boolean isValidEmailAddress = EmailUtils.isValidEmailAddress(trimmedEmailAddress);
                
                if (isValidEmailAddress) {
                    emailAddessesList.add(trimmedEmailAddress);
                }
            }
        }
        
        return emailAddessesList;
    }
    
    public static String getCautionCombinationString(Alert alert) {
        
        if ((alert == null) || (alert.getCautionCombination() == null)) {
            return null;
        }
        
        if (Objects.equals(alert.getCautionCombination(), Alert.COMBINATION_ANY)) {
            return "Any metric value was";
        }
        else if (Objects.equals(alert.getCautionCombination(), Alert.COMBINATION_ALL)) {
            return "All metric values were";
        }
        else if (Objects.equals(alert.getCautionCombination(), Alert.COMBINATION_AVERAGE)) {
            return "The average metric value was";
        }
        else if (Objects.equals(alert.getCautionCombination(), Alert.COMBINATION_AT_MOST_COUNT)) {
            return "At most " + alert.getCautionCombinationCount() + " metric values were";
        }
        else if (Objects.equals(alert.getCautionCombination(), Alert.COMBINATION_AT_LEAST_COUNT)) {
            return "At least " + alert.getCautionCombinationCount() + " metric values were";
        }
        
        return null;
    }
    
    public static String getDangerCombinationString(Alert alert) {
        
        if ((alert == null) || (alert.getDangerCombination() == null)) {
            return null;
        }
        
        if (Objects.equals(alert.getDangerCombination(), Alert.COMBINATION_ANY)) {
            return "Any metric value was";
        }
        else if (Objects.equals(alert.getDangerCombination(), Alert.COMBINATION_ALL)) {
            return "All metric values were";
        }
        else if (Objects.equals(alert.getDangerCombination(), Alert.COMBINATION_AVERAGE)) {
            return "The average metric value was";
        }
        else if (Objects.equals(alert.getDangerCombination(), Alert.COMBINATION_AT_MOST_COUNT)) {
            return "At most " + alert.getDangerCombinationCount() + " metric values were";
        }
        else if (Objects.equals(alert.getDangerCombination(), Alert.COMBINATION_AT_LEAST_COUNT)) {
            return "At least " + alert.getDangerCombinationCount() + " metric values were";
        }
        
        return null;
    }

    public static String getAmountOfTimeAlertIsTriggered(long totalSeconds) {
        
        if (totalSeconds < 0) {
            return null;
        }
        
        StringBuilder output = new StringBuilder();
        
        long days = TimeUnit.SECONDS.toDays(totalSeconds);        
        long hours = TimeUnit.SECONDS.toHours(totalSeconds) - (days * 24);
        long minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) - (TimeUnit.SECONDS.toHours(totalSeconds) * 60);
        long seconds = TimeUnit.SECONDS.toSeconds(totalSeconds) - (TimeUnit.SECONDS.toMinutes(totalSeconds) * 60);
        
        String daysString = "";
        if (days == 1) daysString = days + " day, ";
        else if (days > 1) daysString = days + " days, ";
        output.append(daysString);
        
        String hoursString = "";
        if (hours == 1) hoursString = hours + " hour, ";
        else if ((hours > 1) || ((output.length() > 0) && (hours == 0))) hoursString = hours + " hours, ";
        output.append(hoursString);

        String minutesString = "";
        if (minutes == 1) minutesString = minutes + " minute, ";
        else if ((minutes > 1) || ((output.length() > 0) && (minutes == 0))) minutesString = minutes + " minutes, ";
        output.append(minutesString);
        
        String secondsString = "";
        if (seconds == 1) secondsString = seconds + " second";
        else if ((seconds > 1) || (seconds == 0)) secondsString = seconds + " seconds";
        output.append(secondsString);

        return output.toString();
    }
    
    public String getSubject() {
        return subject_;
    }

    public String getBody() {
        return body_;
    }
    
}
