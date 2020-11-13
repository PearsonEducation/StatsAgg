package com.pearson.statsagg.threads.alert_related;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pearson.statsagg.globals.DatabaseConnections;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroup;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupsDao;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTag;
import com.pearson.statsagg.database_objects.metric_group_tags.MetricGroupTagsDao;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroup;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroupsDao;
import com.pearson.statsagg.configuration.ApplicationConfiguration;
import com.pearson.statsagg.database_objects.pagerduty_services.PagerdutyService;
import com.pearson.statsagg.database_objects.pagerduty_services.PagerdutyServicesDao;
import com.pearson.statsagg.web_ui.StatsAggHtmlFramework;
import com.pearson.statsagg.utilities.web_utils.EmailUtils;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.core_utils.Threads;
import com.pearson.statsagg.utilities.string_utils.StringUtilities;
import java.util.Arrays;
import java.util.Collections;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * @author Jeffrey Schmidt
 */
public class NotificationThread implements Runnable  {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationThread.class.getName());
    
    private final Alert alert_;
    private final int alertLevel_;
    private final List<String> metricKeys_;
    private final Map<String,BigDecimal> alertMetricValues_;  // k="{MetricKey}-{AlertId}"   example: "someMetric.example-55"
    private final Map<String,String> positiveAlertReasons_; // k=MetricMey, v=a reason why the alert was positive (ex "new data point detected")
    private final boolean isPositiveAlert_;
    private final boolean isResend_;
    private final String statsAggLocation_;
    
    // Email variables
    private String emailSubject_ = "";
    private String emailBody_ = "";
    
     // Pager Duty variable
    private JsonObject pagerdutyPayload_ = null;
    
    public NotificationThread(Alert alert, int alertLevel, List<String> metricKeys, Map<String,BigDecimal> alertMetricValues, 
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
        
        MetricGroup metricGroup = MetricGroupsDao.getMetricGroup(DatabaseConnections.getConnection(), true, alert_.getMetricGroupId());
        List<MetricGroupTag> metricGroupTags = MetricGroupTagsDao.getMetricGroupTagsByMetricGroupId(DatabaseConnections.getConnection(), true, alert_.getMetricGroupId());
        
        buildAlertEmail(ApplicationConfiguration.getAlertMaxMetricsInEmail(), metricGroup, metricGroupTags);
        
        List<String> emailAddesses;
        if ((alertLevel_ == Alert.CAUTION) && !isPositiveAlert_) emailAddesses = getToEmailsAddressesForAlert(alert_.getCautionNotificationGroupId());
        else if ((alertLevel_ == Alert.CAUTION) && isPositiveAlert_) emailAddesses = getToEmailsAddressesForAlert(alert_.getCautionPositiveNotificationGroupId());
        else if ((alertLevel_ == Alert.DANGER) && !isPositiveAlert_) emailAddesses = getToEmailsAddressesForAlert(alert_.getDangerNotificationGroupId());
        else if ((alertLevel_ == Alert.DANGER) && isPositiveAlert_) emailAddesses = getToEmailsAddressesForAlert(alert_.getDangerPositiveNotificationGroupId());
        else emailAddesses = new ArrayList();
        
        if (ApplicationConfiguration.isAlertSendEmailEnabled() && !emailAddesses.isEmpty()) {
            sendEmail(emailAddesses, emailSubject_, emailBody_);
        }
        
        if (ApplicationConfiguration.isPagerdutyIntegrationEnabled()){
            buildPagerdutyEvent(ApplicationConfiguration.getAlertMaxMetricsInEmail(), metricGroup, metricGroupTags);
            String routingKey;
            if ((alertLevel_ == Alert.CAUTION) && !isPositiveAlert_) routingKey = getPagerdutyRoutingKeyForAlert(alert_.getCautionNotificationGroupId());
            else if ((alertLevel_ == Alert.CAUTION) && isPositiveAlert_) routingKey = getPagerdutyRoutingKeyForAlert(alert_.getCautionPositiveNotificationGroupId());
            else if ((alertLevel_ == Alert.DANGER) && !isPositiveAlert_) routingKey = getPagerdutyRoutingKeyForAlert(alert_.getDangerNotificationGroupId());
            else if ((alertLevel_ == Alert.DANGER) && isPositiveAlert_) routingKey = getPagerdutyRoutingKeyForAlert(alert_.getDangerPositiveNotificationGroupId());
            else routingKey = null;
            sendPagerDutyEvent(routingKey, pagerdutyPayload_);
        }
        
    }
    
    public void buildAlertEmail(Integer numMetricKeysPerEmail, MetricGroup metricGroup, List<MetricGroupTag> metricGroupTags) {
    
        if ((alert_ == null) || ((alertLevel_ != Alert.CAUTION) && (alertLevel_ != Alert.DANGER)) || (metricGroup == null) || (numMetricKeysPerEmail == null)) {
            logger.error("Failed to create email alert message.");
            return;
        }

        try {
            String warningLevelString = null;
            if (alertLevel_ == Alert.CAUTION) warningLevelString = "Caution";
            else if (alertLevel_ == Alert.DANGER) warningLevelString = "Danger";    

            if (isPositiveAlert_) emailSubject_ = "StatsAgg Positive Alert, " + warningLevelString + ", Name=\"" + alert_.getName() + "\"";
            else emailSubject_ = "StatsAgg Alert, " + warningLevelString + ", Name=\"" + alert_.getName() + "\"";

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

            emailBody_ = body.toString();

            String cleanSubject = StringUtilities.removeNewlinesFromString(emailSubject_, ' ');
            String cleanBody = StringUtilities.removeNewlinesFromString(emailBody_, ' ');
            logger.debug(cleanSubject + "\n" + cleanBody);
        }
        catch (Exception e) {
            logger.error("Failed to create email alert message.");
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
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
        
        int allowedSendAttempts = 5;
        
        for (int i = 0; i < allowedSendAttempts; i++) {
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
                
                return;
            }
            catch (Exception e) {
                String cleanSubject = StringUtilities.removeNewlinesFromString(emailSubject, ' ');
                logger.error("Message=\"Failed to send email alert. SMTP failure. \", " + "EmailSubject=\"" + cleanSubject + "\", " +
                        e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                Threads.sleepSeconds(5);
            }
        }

        String cleanSubject = StringUtilities.removeNewlinesFromString(emailSubject, ' ');
        logger.error("Message=\"Failed to send email alert " + allowedSendAttempts + " times. Email will not be sent\", " + "EmailSubject=\"" + cleanSubject + "\"");
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
        
        NotificationGroup notificationGroup = NotificationGroupsDao.getNotificationGroup(DatabaseConnections.getConnection(), true, notificationGroupId);

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
    
    public static String getPagerdutyRoutingKeyForAlert(Integer notificationGroupId) {
        
        if (notificationGroupId == null) {
            return null;
        }
        
        String routingKey = null;

        NotificationGroup notificationGroup = NotificationGroupsDao.getNotificationGroup(DatabaseConnections.getConnection(), true, notificationGroupId);
        
        if ((notificationGroup != null) && (notificationGroup.getPagerdutyServiceId() != null)) {
            PagerdutyService pagerdutyService = PagerdutyServicesDao.getPagerdutyService(DatabaseConnections.getConnection(), true, notificationGroup.getPagerdutyServiceId());
            if ((pagerdutyService != null) && (pagerdutyService.getRoutingKey() != null)) {
                routingKey = pagerdutyService.getRoutingKey();
            }
        }
        
        return routingKey;
    }
    
    public void buildPagerdutyEvent(Integer numMetricKeysPerPage, MetricGroup metricGroup, List<MetricGroupTag> metricGroupTags) {
        
        if ((alert_ == null) || ((alertLevel_ != Alert.CAUTION) && (alertLevel_ != Alert.DANGER)) || (metricGroup == null) || (numMetricKeysPerPage == null)) {
            logger.error("Failed to create PagerDuty event.");
            return;
        }
        
        try {
            String warningLevelString = null, pdSeverity = null;
            if (alertLevel_ == Alert.CAUTION) {
                warningLevelString = "Caution";
                pdSeverity = "warning";
            } 
            else if (alertLevel_ == Alert.DANGER) {
                warningLevelString = "Danger"; 
                pdSeverity = "critical";
            }

            String summary;
            if (isPositiveAlert_) summary = "StatsAgg Positive Alert, " + warningLevelString + ", Name=\"" + alert_.getName() + "\"";
            else summary = "StatsAgg Alert, " + warningLevelString + ", Name=\"" + alert_.getName() + "\"";

            JsonObject json = new JsonObject();
            json.addProperty("dedup_key", alert_.getId().toString());

            JsonArray links = new JsonArray();

            if ((statsAggLocation_ != null) && !statsAggLocation_.isEmpty()) {
                JsonObject statsaggLink = new JsonObject();
                statsaggLink.addProperty("text","StatsAgg");
                statsaggLink.addProperty("href",statsAggLocation_);
                links.add(statsaggLink);
            }

            if ((alertLevel_ == Alert.CAUTION) && (statsAggLocation_ != null) && !statsAggLocation_.isEmpty() && (alert_ != null) && (alert_.getName() != null)) {
                StringBuilder cautionMetricsURL = new StringBuilder();
                cautionMetricsURL.append(statsAggLocation_).append("/AlertAssociations?Name=").append(StatsAggHtmlFramework.urlEncode(alert_.getName())).append("&Level=" + "Caution");
                JsonObject triggeredCautionMetrics = new JsonObject();
                triggeredCautionMetrics.addProperty("text","Currently Triggered Caution Metrics");
                triggeredCautionMetrics.addProperty("href",cautionMetricsURL.toString());
                links.add(triggeredCautionMetrics);
            }

            if ((alertLevel_ == Alert.DANGER) && (statsAggLocation_ != null) && !statsAggLocation_.isEmpty() && (alert_ != null) && (alert_.getName() != null)) {
                StringBuilder dangerMetricsURL = new StringBuilder();
                dangerMetricsURL.append(statsAggLocation_).append("/AlertAssociations?Name=").append(StatsAggHtmlFramework.urlEncode(alert_.getName())).append("&Level=" + "Danger");
                JsonObject triggeredDangerMetrics = new JsonObject();
                triggeredDangerMetrics.addProperty("text","Currently Triggered Danger Metrics");
                triggeredDangerMetrics.addProperty("href",dangerMetricsURL.toString());
                links.add(triggeredDangerMetrics);
            }

            if (links.size()>0) json.add("links", links);

            JsonObject payload = new JsonObject();
            json.add("payload",payload);
            payload.addProperty("summary",summary);
            payload.addProperty("source", "See Custom Details");
            payload.addProperty("severity", pdSeverity);

            if (!isPositiveAlert_) {
                json.addProperty("event_action","trigger");

                JsonObject customDetails = new JsonObject();
                payload.add("custom_details",customDetails);

                Calendar currentDateAndTime = Calendar.getInstance();
                SimpleDateFormat dateAndTimeFormat = new SimpleDateFormat("yyyy-MM-dd  h:mm:ss a  z");
                String formattedDateAndTime = dateAndTimeFormat.format(currentDateAndTime.getTime());

                String alertTriggeredAt = null;
                if (isResend_) alertTriggeredAt = alert_.getHumanReadable_AmountOfTimeAlertIsTriggered(alertLevel_, currentDateAndTime);

                customDetails.addProperty("Current Time",formattedDateAndTime);
                if (alertTriggeredAt != null) customDetails.addProperty("Alert Triggered Time",alertTriggeredAt);
                customDetails.addProperty("Alert Name",alert_.getName());

                if (alert_.getDescription() != null) customDetails.addProperty("Alert Description",alert_.getDescription());
                if (metricGroup.getName() != null) customDetails.addProperty("Metric Group Name",metricGroup.getName());
                if (metricGroup.getDescription() != null) customDetails.addProperty("Metric Group Description",metricGroup.getDescription());
                if ((metricGroupTags != null) && !metricGroupTags.isEmpty()) {
                    StringBuilder sbTags = new StringBuilder();
                    String prefix = "";
                    for (MetricGroupTag metricGroupTag : metricGroupTags) {
                        if ((metricGroupTag != null) && (metricGroupTag.getTag() != null) && !metricGroupTag.getTag().trim().isEmpty()) {
                            sbTags.append(prefix).append(metricGroupTag.getTag().trim());
                            prefix = " ";
                        }
                    }
                    customDetails.addProperty("Tags",sbTags.toString());
                }

                JsonObject alertCriteriaJson = new JsonObject();
                String alertCriteria = "One or more metrics associated with the metric group " + metricGroup.getName() + " meet the following criteria:";
                customDetails.add(alertCriteria,alertCriteriaJson);

                if (alert_.getAlertType() == Alert.TYPE_AVAILABILITY) {
                    alertCriteriaJson.addProperty("1",alert_.getHumanReadable_AlertCriteria_AvailabilityCriteria(alertLevel_));
                }
                else if (alert_.getAlertType() == Alert.TYPE_THRESHOLD) {
                    alertCriteriaJson.addProperty("1",alert_.getHumanReadable_AlertCriteria_MinimumSampleCount(alertLevel_));
                    alertCriteriaJson.addProperty("2",alert_.getHumanReadable_AlertCriteria_ThresholdCriteria(alertLevel_));
                }

                JsonObject triggeredMetrics = new JsonObject();
                List<String> sortedMetricKeys = sortAndLimitMetricsForEmail(metricKeys_, numMetricKeysPerPage);
                int counter = 0;

                customDetails.add("The following metric(s) are in an alerted state (metric name/value):",triggeredMetrics);

                for (String metricKey : sortedMetricKeys) {
                    triggeredMetrics.addProperty(metricKey,"");

                    BigDecimal alertMetricValue = alertMetricValues_.get(metricKey + "-" + alert_.getId());
                    if (alertMetricValue != null) {
                        String metricValueString_WithLabel = Alert.getMetricValueString_WithLabel(alertLevel_, alert_, alertMetricValue);
                        if (metricValueString_WithLabel != null) triggeredMetrics.addProperty(metricKey,metricValueString_WithLabel);
                    }

                    counter++;
                }

                if (counter == numMetricKeysPerPage) { 
                    triggeredMetrics.addProperty("... "+(metricKeys_.size() - numMetricKeysPerPage)+" more","");
                }
            } 
            else {
                json.addProperty("event_action","resolve");
            }
            
            pagerdutyPayload_ = json;
            logger.debug(json.toString());
        }
        catch (Exception e) {
            logger.error("Failed to create PagerDuty event.");
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }

    }
    
    public void sendPagerDutyEvent(String routingKey, JsonObject event) {
        
        if (routingKey == null) {
            String cleanSubject = StringUtilities.removeNewlinesFromString(event.getAsJsonObject("payload").get("summary").toString(), ' ');
            logger.debug("Message=\"Failed to send PagerDuty event. No valid API keys.\", Subject=\"" + cleanSubject + "\"");
            return;
        }
        
        int allowedSendAttempts = 5;
        
        for (int j = 0; j < allowedSendAttempts; j++) {
            try {
                event.addProperty("routing_key",routingKey);

                HttpClient httpClient = HttpClientBuilder.create().build();
                StringEntity params = new StringEntity(event.toString());

                HttpPost request = new HttpPost("https://events.pagerduty.com/v2/enqueue");
                request.addHeader("content-type", "application/json");
                request.setEntity(params);

                HttpResponse response = httpClient.execute(request);

                String cleanSubject = StringUtilities.removeNewlinesFromString(event.getAsJsonObject("payload").get("summary").toString(), ' ');
                String cleanBody = StringUtilities.removeNewlinesFromString(event.toString(), ' ');

                if (response.getStatusLine().getStatusCode() < 400){
                    logger.info("Message=\"Sent PagerDuty event. Response was " + response.getStatusLine() + "\", Subject=\"" + cleanSubject + "\"" + ", Event=\"" + cleanBody + "\"");
                    return;
                }
                else {
                    logger.error("Message=\"Failed to send PagerDuty event. " + response.getStatusLine() + ".\", Subject=\"" + cleanSubject + "\"");
                    Threads.sleepSeconds(5);
                }

            }
            catch (Exception e) {
                String cleanSubject = StringUtilities.removeNewlinesFromString(event.getAsJsonObject("payload").get("summary").toString(), ' ');
                logger.error("Message=\"Failed to send PagerDuty event. \", " + "Subject=\"" + cleanSubject + "\", " +
                        e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
                Threads.sleepSeconds(5);
            }
        }
        
        String cleanSubject = StringUtilities.removeNewlinesFromString(event.getAsJsonObject("payload").get("summary").toString(), ' ');
        logger.error("Message=\"Failed to send PagerDuty alert " + allowedSendAttempts + " times. Alert will not be sent\", " + "Subject=\"" + cleanSubject + "\"");
    }
    
    public String getSubject() {
        return emailSubject_;
    }

    public String getEmailBody() {
        return emailBody_;
    }
    
    public JsonObject getPagerdutyPayload() {
        return pagerdutyPayload_;
    }
    
}
