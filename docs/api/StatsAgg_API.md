**Show Alerts**
----
  Returns json data containing list of alerts.

* **URL**

    `/StatsAgg/api/alerts-list`



* **Method:**

    `GET`


  
*  **URL Params**

     **Required:**
 
    `page_size=[integer] The maximum number of alerts to return per page/request.` 
 
    `page_number=[integer] The page number containing the list of alerts.`
 



* **Example Request:**

    ` /StatsAgg/api/alerts-list?page_size=2&page_number=1`

   

* **Example Result:**

    ```{
	"alerts":
		[{"name":"too high",
		  "id":"21"},
		 {"name":"too low",
		  "id":"23"}],"count":2}```




**Show Metric Groups**
----
  Returns json data containing list of metric groups.

* **URL**

    `/StatsAgg/api/metric-groups`



* **Method:**

    `GET`
  


*  **URL Params**

     **Required:**
 
     `page_size=[integer] The maximum number of metric groups to return per page/request.` 
 
    `page_number=[integer] The page number containing the list of metric groups.` 



* **Example Request:**

    ` /StatsAgg/api/metric-groups?page_size=2&page_number=1`


   
* **Example Result:**

    ```{
	"metricgroups":
		[{"name":"http_busy_workers",
		  "id":"37"},
		 {"name":"app_db_busy_connections",
		  "id":"39"}],"count":2}```



**Show Notification Groups**
----
  Returns json data containing list of notification groups.

* **URL**

    `/StatsAgg/api/notification-groups-list`



* **Method:**

    `GET`


  
*  **URL Params**

     **Required:**
 
    `page_size=[integer] The maximum number of notification groups to return per page/request.` 
 
    `page_number=[integer] The page number containing the list of notification groups.` 



* **Example Request:**

    ` /StatsAgg/api//StatsAgg/api/notification-groups-list?page_size=2&page_number=1`


   
* **Example Result:**

    ```{
	"notificationgroups":
		[{"name":"DevOps Team",
		  "id":"10"},
		 {"name":"AppSupport Team",
		  "id":"11"}],"count":2}```




**Show Alert Suspensions** 
----
  Returns json data containing list of alert suspensions.

* **URL**

    `/StatsAgg/api/AlertsSuspension-list`



* **Method:**

    `GET`
  


*  **URL Params**

     **Required:**
 
    `page_size=[integer] The maximum number of Alert Suspensions to return per page/request.` 
 
    `page_number=[integer] The page number containing the list of Alert Suspensions.` 



* **Example Request:**

    ` /StatsAgg/api/AlertsSuspension-list?page_size=2&page_number=1`
   


* **Example Result:**

    ```{
	"alerts":
		[{"name":"business-hours",
		  "id":"2464"},
		 {"name":"hung core",
		  "id":"2477"}],"count":2}```



**Show Alert Details**
----
  Returns json data containing alert details.

* **URL**

    `/StatsAgg/api/alert-details`



* **Method:**

    `GET`
  


*  **URL Params**

     **Required:**
 
    `id=[integer] Alert id.` 



* **Example Request:**

    ` /StatsAgg/api/alert-details?id=2464`
   


* **Example Result:**

    ```{"metricgroup_id":36,"danger_enabled":true,"name":"prd_actaspire_app_db_busy_connections~~too high","description":"This alert is triggered when a prd server's JVM is actively using too many JDBC connections.","danger_alert_active":false,"id":21,"danger_notificationgroup_id":1,"caution_enabled":true,"alert_type":1002,"enabled":true,"caution_notificationgroup_id":1,"caution_alert_active":false} ```



**Show Metric Group Details** 
----
  Returns json data containing metric group details.

* **URL**

    `/StatsAgg/api/metric-group-details`



* **Method:**

    `GET`


  
*  **URL Params**

     **Required:**
 
    `id=[integer] Metric Group id.` 



* **Example Request:**

    ` /StatsAgg/api/metric-group-details?id=2464`


   
* **Example Result:**

    ```{"name":"db_busy_connections","description":"Watches the jdbc connection usage pool.","id":36}```



**Show Notification Group Details** 
----
  Returns json data containing notification group details.

* **URL**

    `/StatsAgg/api/notification-group-details`



* **Method:**

    `GET`


  
*  **URL Params**

     **Required:**
 
    `id=[integer] Notification Group id.` 



* **Example Request:**

    ` /StatsAgg/api/notification-group-details?id=2464`


   
* **Example Result:**

    ```{"name":"qlmq Performance","id":1, "email_addresses":"performance@xyz.com"} ```



**Show Alert Suspension Details** 
----
  Returns json data containing alert suspension details.

* **URL**

    `/StatsAgg/api/alertsuspension-details`



* **Method:**

    `GET`
  


*  **URL Params**

     **Required:**
 
    `id=[integer] Alert Suspensions id.` 



* **Example Request:**

    ` /StatsAgg/api/alertsuspension-details?id=2464`
   


* **Example Result:**

    ```{"StartDate":2015-04-01 00:00:00.0,"MetricGroupTagsExclusive":"","DurationTimeUnit":73,"Description":"","SuspendBy":2,"MetricGroupTagsInclusive":"irnprd","StartTime":1978-03-01 18:01:00.0,"Duration":54045600,"Id":24,"Name":"nonprd-non-business-hours"}```



**Create Metric Group** 
----
  Creates a new Metric Group.

* **URL**

    `/StatsAgg/api/create-metric-group`



* **Method:**

    `POST/JSON`
  


*  **JSON Fields**

	**Required:**
 
    `Name=[String] A unique name for this metric group. Other than uniqueness, the only limitation is that it must be under 500 characters long.` 
	
	`MatchRegexes=[String] Regular expressions used to tie individual metrics to the metric group.`

	**Optional:**

    `Description=[String] A description of the metric group. Avoid descriptions longer than 1000000 characters.`

	`BlacklistRegexes=[String] Regular expressions used to blacklist metrics from the metric group.`

	`Tags=[String] Metric groups can be ‘tagged’. Tags allows for convenient filtering in the various tables on the StatsAgg web user-interface.`	



* **Example Request:**

    ` /StatsAgg/api/create-metric-group`
   


    ```{
		"Name":"alertsuspension-hours",
		"Description":"2464",
		"MatchRegexes": ".*",
        "BlacklistRegexes": "",
        "Tags": "group_1"
       } ```



**Create Notification Group** 
----
  Creates a new Notification Group.

* **URL**

    `/StatsAgg/api/create-notification-group`



* **Method:**

    `POST/JSON`


  
*  **JSON Fields**

	  **Required:**
 
    `Name=[String] A unique name for this ‘notification group’. Other than uniqueness, the only limitation is that it must be under 500 characters long.`

	**Optional:** 

    `EmailAddresses=[String] A comma separated list of email addresses.`



* **Example Request:**

    ```{
		"Name":"notification-group",
		"EmailAddresses":"example@test.com"
       } ```



**Create Alert**  
----
  Creates a new Alert.

* **URL**

    `/StatsAgg/api/create-alert`



* **Method:**

    `POST`


  
*  **JSON Fields**

	**Required:**
 
    `Name=[String] A unique name for this alert. Other than uniqueness, the only limitation is that it must be under 500 characters long.` 

    `Description=[String] (optional) A description of the alert. Avoid descriptions longer than 1000000 characters.`

	`MetricGroupName=[String] The ‘metric group’ that this alert is associated with.`

	`Enabled=[Boolean] If you want the alert to be enabled right away after creation or alteration, then check this checkbox. When an alert is disabled, caution & danger alerts will not fire (they are not even evaluated).`

	`CautionEnabled=[Boolean] When caution alerting is disabled, caution alerts will not trigger (they are not even evaluated).`	

	`DangerEnabled=[Boolean] When danger alerting is disabled, danger alerts will not trigger (they are not even evaluated).`	

	`Type=[String] "Availability" or "Threshold"`

	`AlertOnPositive=[String] If this is checked, alerts that change states from ‘triggered’ to ‘not triggered’ will send an email notification to the notification group’s recipients.`	

	`AllowResendAlert=[String] If this is checked, alerts that are in a ‘triggered’ state will send a new email notification to the notification group’s recipients after the specified amount of time.`	

	`ResendAlertEvery=[Integer] If ‘resend alert?’ is enabled, then alerts in a ‘triggered’ state will send a new email notification to the notification group’s recipients after the amount of time specified by this option.`	

	`ResendAlertEveryTimeUnit=[String] "Seconds", "Minutes" "Hours", or "Days"'

	`CautionNotificationGroupName=[String] (optional) The ‘notification group’ that is associated with the alert. Alerts that are triggered will be sent to the members of this ‘notification group’.`	

	`CautionPositiveNotificationGroupName=[String] `	

	`CautionWindowDurationTimeUnit=[String] "Seconds", "Minutes" "Hours", or "Days"'`	

	`CautionWindowDuration=[String] The ‘window duration’ is an amount of time (between ‘now’ and ‘x seconds ago’) that metrics values are allowed to be considered for alerts.`	

	`CautionStopTrackingAfterTimeUnit=[String] "Seconds", "Minutes" "Hours", or "Days"'`	

	`CautionStopTrackingAfter=[String] For availability alerts, StatsAgg requires that you eventually ‘give up’ on tracking a metric that hasn’t had any new data points.`	

	`CautionMinimumSampleCount=[String] `	

	`CautionOperator=[String] The values of a metric-key are considered for threshold-based alerting when they are above/below/equal-to a certain threshold. This value controls the above/below/equal-to aspect of the alert.`	

	`CautionCombination=[String] For a threshold-alert, we must decide how the values will be used for alert consideration.`	

	`CautionCombinationCount=[String] When one chooses a ‘combination’ of ‘at most X values’ or ‘at least X values’, a count is required. The ‘combination count’ specifies how many values are required.`	

	`CautionThreshold=[String] The ‘threshold’ is the value that is compared against when deciding whether an alert is active or not.`	
	
	`DangerNotificationGroupName=[String] `	

	`DangerPositiveNotificationGroupName=[String] `	

	`DangerWindowDurationTimeUnit=[String] "Seconds", "Minutes" "Hours", or "Days"'`	

	`DangerWindowDuration=[String] `

	`DangerStopTrackingAfterTimeUnit=[String] `	

	`DangerStopTrackingAfter=[String] `	

	`DangerMinimumSampleCount=[String] `	

	`DangerOperator=[String] `

	`DangerCombination=[String] `	

	`DangerCombinationCount=[String] `	

	`DangerThreshold=[String] `

* **Example Request:**

    ```   
	{"Name": "xyz-alert",
	"Description": "A very in-depth description about xyz-alert",
	"MetricGroupName":"metric grp1",
	"Enabled": "true",
	"CautionEnabled": "true",
	"DangerEnabled": "true",
	"Type": "Threshold",
	"AlertOnPositive": "true",
	"AllowResendAlert": "true",
    "SendAlertEvery": 60,
    "SendAlertEveryTimeUnit": "Minutes",
	"CautionNotificationGroupName": "notification grp 1",
	"CautionPositiveNotificationGroupName": "notification grp 2",
	"CautionWindowDurationTimeUnit": "Hours",
    "CautionWindowDuration": 2,
	"CautionStopTrackingAfterTimeUnit": 10,
	"CautionStopTrackingAfter": 5,
	"CautionMinimumSampleCount": 25,
	"CautionOperator": "true",
	"CautionCombination": "true",
	"CautionCombinationCount": 20,
	"CautionThreshold": 30,
	"DangerNotificationGroupName": "notification grp 1",
	"DangerPositiveNotificationGroupName": "notification grp 2",
	"DangerWindowDurationTimeUnit": "Seconds",
	"DangerWindowDuration": 240,
	"DangerStopTrackingAfterTimeUnit": 20,
    "DangerStopTrackingAfter": 34,
	"DangerMinimumSampleCount": 23,
	"DangerOperator": "true",
	"DangerCombination": "true",
	"DangerCombinationCount": 43,
	"DangerThreshold": "true"}```



**Create Alert Suspension** 
----
  Creates a new Alert Suspension.

* **URL**

    `/StatsAgg/api/create-alertsuspension`



* **Method:**

    `POST/JSON`


  

*  **JSON Fields**
 
    `Name=[String] A unique name for this ‘alert suspension’. Other than uniqueness, the only limitation is that it must be under 500 characters long.` 

    `Description=[String] `

	`Enabled=[String] If you want the alert suspension to be enabled after creation or alteration, then check this checkbox. When an ‘alert suspension’ is disabled, the alert suspension won’t be able to suspend any caution or danger alerts.`

	`SuspendNotificationOnly=[String] When checked, suspended alerts still evaluate their alert criteria (and display the caution/danger triggered status on the StatsAgg WebUI); they just don’t send out emails alerts. When unchecked, suspended alerts will not be evaluated at all. The most common approach is to have this field checked.`

	`CreateAlertSuspension_SuspendBy=[String] `	

	`AlertName=[String] (optional) If you want to suspend a single alert, then suspend it by alert name.`	

	`MetricGroupTagsInclusive=[String] `	

	`MetricGroupTagsExclusive=[String] `	

	`CreateAlertSuspension_Type=[String] `	

	`StartDate=[String] `	

	`RecurSunday=[String] `

	`RecurMonday=[String] `

	`RecurTuesday=[String] `	

	`RecurWednesday=[String] `	

	`RecurThursday=[String] `	

	`RecurFriday=[String] `	

	`RecurSaturday=[String] `	

	`DurationTimeUnit=[String] `	

	`Duration=[String] `


	
* **Example Request:**

    ```   {"Name": "alert suspension","Description": "alert new created field","Enabled":"true",
	"SuspendNotificationOnly": "true",
	"CreateAlertSuspension_SuspendBy":"true",
	"MetricGroupTagsInclusive":"metric group tags inclusive",
	"MetricGroupTagsExclusive": "metric grp tags exclusive",
	"CreateAlertSuspension_Type":"alert suspension type",
    "StartDate": 5000,
	"RecurSunday":"notification grp",
	"RecurMonday": "positive notification grp1",
	"RecurTuesday": 100,
    "RecurWednesday": 10,
	"RecurThursday": 10,
	"RecurFriday": 5,
	"RecurSaturday": 25,
	"DurationTimeUnit": "true",
	"Duration": "true"}```



**Enable Alert** 
----
  Enable Alert.

* **URL**

    `/StatsAgg/api/alert-enable`



* **Method:**

    `POST`


  
*  **URL Params**
	
	**Required:**
 
    `Name=[String] Alert Name.` 

    `Enabled=[Boolean] To enable the alert.`



* **Example Request:**

    ` /StatsAgg/api/alert-enable?name=http_busy_threads&Enabled=true`
   


**Remove Alert** 
----
  Deletes an Alert from Database.

* **URL**

    `/StatsAgg/api/alert-remove`



* **Method:**

    `POST`


  
*  **URL Params**
 
    `Name=[String] Name of the Alert.` 



* **Example Request:**

    ` /StatsAgg/api/alert-remove?name=alert_name`


   
* **Example Result:**


    ```{"response": "Delete alert success. AlertName="alert_name"."}```



**Remove Metric Group** 
----
  Deletes a Metric Group.

* **URL**

    `/StatsAgg/api/metric-remove`



* **Method:**

    `POST`


  
*  **URL Params**
 
    `Name=[String] Metric Group Name.` 


* **Example Request:**

    ` /StatsAgg/api/metric-remove?name=metric_grp_name`


   
* **Example Result:**

    ```{"response": "Delete metric group success. MetricGroupName="metric_grp_name"."}```



**Remove Notification Group** 
----
  Deletes a Notification Group.

* **URL**

    `/StatsAgg/api/notification-remove`

* **Method:**

    `POST`
  
*  **URL Params**
 
    `Name=[String] Notification Group Name.` 


* **Example Request:**

    ` /StatsAgg/api/notification-remove?name=notification_grp_name`
   
* **Example Result:**

        {"response": "Delete notification group success. NotificationGroupName="notification_grp_name"."}

**Remove Alert Suspension**
----
  Deletes an Alert Suspension from Database.

* **URL**

    `/StatsAgg/api/alertsuspension-remove`

* **Method:**

    `POST`
  
*  **URL Params**
 
    `Name=[String] Name of the Alert Suspension.` 


* **Example Request:**

    ` /StatsAgg/api/alertsuspension-remove?name=alert_suspension_name`
   
* **Example Result:**


    ```{"response":"Delete alert suspension success. AlertSuspensionName=\"alert_suspension_name\"."}```