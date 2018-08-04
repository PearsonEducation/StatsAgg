package com.pearson.statsagg.alerts;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.metric_group.MetricGroup;
import com.pearson.statsagg.database_objects.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTag;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTagsDao;
import com.pearson.statsagg.database_objects.notifications.NotificationGroup;
import com.pearson.statsagg.database_objects.notifications.NotificationGroupsDao;
import com.pearson.statsagg.globals.ApplicationConfiguration;
import com.pearson.statsagg.webui.StatsAggHtmlFramework;
import com.pearson.statsagg.utilities.web_utils.EmailUtils;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import java.util.Arrays;
import java.util.Collections;
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
    
    private final Alert alert_;
    private final int alertLevel_;
    private final List<String> metricKeys_;
    private final Map<String,BigDecimal> alertMetricValues_;  // k="{MetricKey}-{AlertId}"   example: "someMetric.example-55"
    private final Map<String,String> positiveAlertReasons_; // k=MetricMey, v=a reason why the alert was positive (ex "new data point detected")
    private final boolean isPositiveAlert_;
    private final boolean isResend_;
    private final String statsAggLocation_;
    
    private String subject_ = "";
    private String body_ = "";
    
    public EmailThread(Alert alert, int alertLevel, List<String> metricKeys, Map<String,BigDecimal> alertMetricValues, 
            Map<String,String> positiveAlertReasons, boolean isPositiveAlert, boolean isResend, String statsAggLocation) {
        this.alert_ = alert;
        this.alertLevel_ = alertLevel;
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
        
        MetricGroupTagsDao metricGroupTagsDao = new MetricGroupTagsDao();
        List<MetricGroupTag> metricGroupTags = metricGroupTagsDao.getMetricGroupTagsByMetricGroupId(alert_.getMetricGroupId());
        
        buildAlertEmail(ApplicationConfiguration.getAlertMaxMetricsInEmail(), metricGroup, metricGroupTags);
        
        List<String> emailAddesses;
        if ((alertLevel_ == Alert.CAUTION) && !isPositiveAlert_) emailAddesses = getToEmailsAddressesForAlert(alert_.getCautionNotificationGroupId());
        else if ((alertLevel_ == Alert.CAUTION) && isPositiveAlert_) emailAddesses = getToEmailsAddressesForAlert(alert_.getCautionPositiveNotificationGroupId());
        else if ((alertLevel_ == Alert.DANGER) && !isPositiveAlert_) emailAddesses = getToEmailsAddressesForAlert(alert_.getDangerNotificationGroupId());
        else if ((alertLevel_ == Alert.DANGER) && isPositiveAlert_) emailAddesses = getToEmailsAddressesForAlert(alert_.getDangerPositiveNotificationGroupId());
        else emailAddesses = new ArrayList();
        
        if (ApplicationConfiguration.isAlertSendEmailEnabled() && !emailAddesses.isEmpty()) {
            sendEmail(emailAddesses, subject_, body_);
        }
    }
    
    public void buildAlertEmail(Integer numMetricKeysPerEmail, MetricGroup metricGroup, List<MetricGroupTag> metricGroupTags) {
    
        if ((alert_ == null) || ((alertLevel_ != Alert.CAUTION) && (alertLevel_ != Alert.DANGER)) || (metricGroup == null) || (numMetricKeysPerEmail == null)) {
            logger.error("Failed to create email alert message.");
            return;
        }
        
        String warningLevelString = null;
        if (alertLevel_ == Alert.CAUTION) warningLevelString = "Caution";
        else if (alertLevel_ == Alert.DANGER) warningLevelString = "Danger";    
                
        if (isPositiveAlert_) subject_ = "StatsAgg Positive Alert, " + warningLevelString + ", Name=\"" + alert_.getName() + "\"";
        else subject_ = "StatsAgg Alert, " + warningLevelString + ", Name=\"" + alert_.getName() + "\"";

        Calendar currentDateAndTime = Calendar.getInstance();
        SimpleDateFormat dateAndTimeFormat = new SimpleDateFormat("yyyy-MM-dd  h:mm:ss a  z");
        String formattedDateAndTime = dateAndTimeFormat.format(currentDateAndTime.getTime());
        
        String alertTriggeredAt = null;
        if (isResend_) alertTriggeredAt = alert_.getHumanReadable_AmountOfTimeAlertIsTriggered(alertLevel_, currentDateAndTime);
        
        StringBuilder body = new StringBuilder("<html>");
        
        if ((statsAggLocation_ != null) && !statsAggLocation_.isEmpty()) {
            body.append("<b>StatsAgg Server</b> = ").append("<a href=\"").append(statsAggLocation_).append("\">").append(statsAggLocation_).append("</a>").append("<br>");
        }
        
        if ((alertLevel_ == Alert.CAUTION) && (statsAggLocation_ != null) && !statsAggLocation_.isEmpty() && (alert_ != null) && (alert_.getName() != null)) {
            body.append("<b>Currently Triggered Metrics</b> = ").append("<a href=\"").append(statsAggLocation_).append("/AlertAssociations?Name=")
                    .append(StatsAggHtmlFramework.urlEncode(alert_.getName())).append("&Level=" + "Caution").append("\">");
            body.append("Triggered caution metrics").append("</a>").append("<br>");
        }
        
        if ((alertLevel_ == Alert.DANGER) && (statsAggLocation_ != null) && !statsAggLocation_.isEmpty() && (alert_ != null) && (alert_.getName() != null)) {
            body.append("<b>Currently Triggered Metrics</b> = ").append("<a href=\"").append(statsAggLocation_).append("/AlertAssociations?Name=")
                    .append(StatsAggHtmlFramework.urlEncode(alert_.getName())).append("&Level=" + "Danger").append("\">");
            body.append("Triggered danger metrics").append("</a>").append("<br>");
        }
        
        body.append("<b>Current Time</b> = ").append(formattedDateAndTime).append("<br>");
        if (alertTriggeredAt != null) body.append("<b>Alert Triggered Time</b> = ").append(alertTriggeredAt).append("<br>");
        body.append("<b>Alert Name</b> = ").append(StatsAggHtmlFramework.htmlEncode(alert_.getName())).append("<br>");
        
        body.append("<b>Alert Description</b> = ");
        if (alert_.getDescription() != null) body.append(StatsAggHtmlFramework.htmlEncode(alert_.getDescription()).replaceAll("\n", "<br>&nbsp;&nbsp;&nbsp;")).append("<br>");
        else body.append("<br>");
        
        body.append("<b>Metric Group Name</b> = ");
        if (metricGroup.getName() != null) body.append(StatsAggHtmlFramework.htmlEncode(metricGroup.getName())).append("<br>");
        else body.append("<br>");
        
        body.append("<b>Metric Group Description</b> = ");
        if (metricGroup.getDescription() != null) body.append(StatsAggHtmlFramework.htmlEncode(metricGroup.getDescription()).replaceAll("\n", "<br>&nbsp;&nbsp;&nbsp;"));
        
        if ((metricGroupTags == null) || metricGroupTags.isEmpty()) body.append("<br><br>");
        else body.append("<br>");
        
        if ((metricGroupTags != null) && !metricGroupTags.isEmpty()) {
            body.append("<b>Tags</b> = ");
            
            for (MetricGroupTag metricGroupTag : metricGroupTags) {
                if ((metricGroupTag != null) && (metricGroupTag.getTag() != null) && !metricGroupTag.getTag().trim().isEmpty()) {
                    body.append("<u>").append(StatsAggHtmlFramework.htmlEncode(metricGroupTag.getTag().trim())).append("</u>&nbsp;");
                }
            }
            
            body.append("<br><br>");
        }
        
        if (isPositiveAlert_) {
            body.append("<b>No metrics associated with the metric group \"").append(StatsAggHtmlFramework.htmlEncode(metricGroup.getName()))
                    .append("\" meet the following criteria:</b>").append("<br>");
        }
        else {
            body.append("<b>One or more metrics associated with the metric group \"").append(StatsAggHtmlFramework.htmlEncode(metricGroup.getName()))
                    .append("\" meet the following criteria:</b>").append("<br>");
        }

        if (alert_.getAlertType() == Alert.TYPE_AVAILABILITY) {
            body.append("<ul><li>").append(alert_.getHumanReadable_AlertCriteria_AvailabilityCriteria(alertLevel_)).append("</li></ul><br>");
        }
        else if (alert_.getAlertType() == Alert.TYPE_THRESHOLD) {
            body.append("<ul><li>").append(alert_.getHumanReadable_AlertCriteria_MinimumSampleCount(alertLevel_)).append("</li>");
            body.append("<li>").append(alert_.getHumanReadable_AlertCriteria_ThresholdCriteria(alertLevel_)).append("</li></ul><br>");
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
                    body.append(" = ");
                    String metricValueString_WithLabel = Alert.getMetricValueString_WithLabel(alertLevel_, alert_, alertMetricValue);
                    if (metricValueString_WithLabel != null) body.append(StatsAggHtmlFramework.htmlEncode(metricValueString_WithLabel));
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
            if ((alertLevel_ == Alert.CAUTION) && (alert_.getCautionActiveAlertsSet() != null)) {
                String[] cautionActiveAlertsSet = StringUtils.split(alert_.getCautionActiveAlertsSet(), '\n');
                if ((cautionActiveAlertsSet != null) && (cautionActiveAlertsSet.length > 0)) unsortedMetricKeys.addAll(Arrays.asList(cautionActiveAlertsSet));
            }
            else if ((alertLevel_ == Alert.DANGER) && (alert_.getDangerActiveAlertsSet() != null)) {
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
        
        String cleanSubject = StringUtilities.removeNewlinesFromString(subject_, ' ');
        String cleanBody = StringUtilities.removeNewlinesFromString(body_, ' ');
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
            String cleanSubject = StringUtilities.removeNewlinesFromString(emailSubject, ' ');
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
            
            String cleanSubject = StringUtilities.removeNewlinesFromString(emailSubject, ' ');
            String cleanBody = StringUtilities.removeNewlinesFromString(emailBody, ' ');
            logger.info("Message=\"Send email alert\", EmailSubject=\"" + cleanSubject + "\"" + ", EmailBody=\"" + cleanBody + "\"");
        }
        catch (Exception e) {
            String cleanSubject = StringUtilities.removeNewlinesFromString(emailSubject, ' ');
            logger.error("Message=\"Failed to send email alert. SMTP failure.\", " + "EmailSubject=\"" + cleanSubject + "\", " +
                    e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }

    public static List<String> sortAndLimitMetricsForEmail(List<String> metricKeys, Integer maxAllowedMetrics) {
        
        if ((metricKeys == null) || metricKeys.isEmpty() || (maxAllowedMetrics == null) || (maxAllowedMetrics < 0)) {
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

        if ((notificationGroup != null) && (notificationGroup.getEmailAddresses() != null)) {
            String[] emailAddresses = StringUtils.split(notificationGroup.getEmailAddresses(), ",");
            
            for (String emailAddress : emailAddresses) {
                String trimmedEmailAddress = emailAddress.trim();
                boolean isValidEmailAddress = EmailUtils.isValidEmailAddress(trimmedEmailAddress);
                if (isValidEmailAddress) emailAddessesList.add(trimmedEmailAddress);
            }
        }
        
        return emailAddessesList;
    }
    
    public String getSubject() {
        return subject_;
    }

    public String getBody() {
        return body_;
    }
    
}
