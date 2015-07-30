**Show Alerts**
----
  Returns json data containing list of alerts.

* **URL**

    `/StatsAgg/api/alerts-list`

* **Method:**

    `GET`
  
*  **URL Params**

     **Required:**
 
    `page_size=[integer] The maximum number of alerts to return per page.` 
 
    `page_number=[integer] The page number containing the list of alerts.` 


* **Example Request:**

    ` /StatsAgg/api/alerts-list?page_size=2&page_number=1`
   
* **Example Result:**

    ```{
	"alerts":
		[{"name":"too high",
		  "id":"21"},
		 {"name":"too low",
		  "id":"23"}],"count":2}
  ```


**Show Metric Groups**
----
  Returns json data containing list of metric groups.

* **URL**

    `/StatsAgg/api/metric-groups`

* **Method:**

    `GET`
  
*  **URL Params**

     **Required:**
 
    `page_size=[integer]`
 
    `page_number=[integer]`


* **Example Request:**

    ` /StatsAgg/api/metric-groups?page_size=2&page_number=1`
   
* **Example Result:**

    ```{
	"metricgroups":
		[{"name":"http_busy_workers",
		  "id":"37"},
		 {"name":"app_db_busy_connections",
		  "id":"39"}],"count":2}
  ```


**Show Notification Groups**
----
  Returns json data containing list of notification groups.

* **URL**

    `/StatsAgg/api/notification-groups-list`

* **Method:**

    `GET`
  
*  **URL Params**

     **Required:**
 
    `page_size=[integer]`
 
    `page_number=[integer]`


* **Example Request:**

    ` /StatsAgg/api//StatsAgg/api/notification-groups-list?page_size=2&page_number=1`
   
* **Example Result:**

    ```{
	"notificationgroups":
		[{"name":"DevOps Team",
		  "id":"10"},
		 {"name":"AppSupport Team",
		  "id":"11"}],"count":2}
  ```



Show Alert Suspensions 
----
  Returns json data containing list of alert suspensions.

* **URL**

    `/StatsAgg/api/AlertsSuspension-list`

* **Method:**

    `GET`
  
*  **URL Params**

     **Required:**
 
    `page_size=[integer]`
 
    `page_number=[integer]`


* **Example Request:**

    ` /StatsAgg/api/AlertsSuspension-list?page_size=2&page_number=1`
   
* **Example Result:**

    ```{
	"alerts":
		[{"name":"business-hours",
		  "id":"2464"},
		 {"name":"hung core",
		  "id":"2477"}],"count":2}
  ```


Show Alert Details 
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

    ```{"metricgroup_id":36,"danger_enabled":true,"name":"prd_actaspire_app_db_busy_connections~~too high","description":"This alert is triggered when a prd server's JVM is actively using too many JDBC connections. ","danger_alert_active":false,"id":21,"danger_notificationgroup_id":1,"caution_enabled":true,"alert_type":1002,"enabled":true,"caution_notificationgroup_id":1,"caution_alert_active":false}
  ```


Show Metric Group Details 
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

    ```{"name":"db_busy_connections","description":"Watches the jdbc connection usage pool.","id":36}
  ```

Show Notification Group Details 
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

    ```{"email_addresses":"performance@xyz.com","name":"qlmq Performance","id":1}
  ```


Show Alert Suspension Details 
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

    ```{"StartDate":2015-04-01 00:00:00.0,"MetricGroupTagsExclusive":"","DurationTimeUnit":73,"Description":"","SuspendBy":2,"MetricGroupTagsInclusive":"irnprd","StartTime":1978-03-01 18:01:00.0,"Duration":54045600,"Id":24,"Name":"nonprd-non-business-hours"}
  ```

Create Metric Group 
----
  Creates a new Metric Group.

* **URL**

    `/StatsAgg/api/create-metric-group`

* **Method:**

    `POST`
  
*  **JSON Fields**
 
    `Name=[String] ` 

    `Description=[String] `

	`MatchRegexes=[String] `

	`BlacklistRegexes=[String] `

	`Tags=[String] `	

* **Example Request:**

    ` /StatsAgg/api/create-metric-group`
   


    ```{
		"Name":"alertsuspension-hours",
		"Description":"2464",
		"MatchRegexes": ".*",
        "BlacklistRegexes": "",
        "Tags": "group_1"
       }
  ```


Create Notification Group 
----
  Creates a new Notification Group.

* **URL**

    `/StatsAgg/api/create-notification-group`

* **Method:**

    `POST`
  
*  **JSON Fields**
 
    `Name=[String] ` 

    `EmailAddresses=[String] `


* **Example Request:**

    ```{
		"Name":"notification-group",
		"EmailAddresses":"example@test.com"
       }
  ```



Create Alert  
----
  Creates a new Alert.

* **URL**

    `/StatsAgg/api/create-alert`

* **Method:**

    `POST`
  
*  **JSON Fields**
 
    `Name=[String] ` 

    `Description=[String] `

	`MetricGroupName=[String] `

	`Enabled=[Boolean] `

	`CautionEnabled=[Boolean] `	

	`DangerEnabled=[Boolean] `	

	`CreateAlert_Type=[String] `	

	`AlertOnPositive=[String] `	

	`AllowResendAlert=[String] `	

	`SendAlertEveryNumMilliseconds=[Integer] `	

	`CautionNotificationGroupName=[String] `	

	`CautionPositiveNotificationGroupName=[String] `	

	`CautionWindowDurationTimeUnit=[String] `	

	`CautionWindowDuration=[String] `	

	`CautionStopTrackingAfterTimeUnit=[String] `	

	`CautionStopTrackingAfter=[String] `	

	`CautionMinimumSampleCount=[String] `	

	`CautionOperator=[String] `	

	`CautionCombination=[String] `	

	`CautionCombinationCount=[String] `	

	`CautionThreshold=[String] `	
	
	`DangerNotificationGroupName=[String] `	

	`DangerPositiveNotificationGroupName=[String] `	

	`DangerWindowDurationTimeUnit=[String] `	

	`DangerWindowDuration=[String] `

	`DangerStopTrackingAfterTimeUnit=[String] `	

	`DangerStopTrackingAfter=[String] `	

	`DangerMinimumSampleCount=[String] `	

	`DangerOperator=[String] `

	`DangerCombination=[String] `	

	`DangerCombinationCount=[String] `	

	`DangerThreshold=[String] `

* **Example Request:**

    ```   {"Name": "xyz-alert","Description": "alert new created field","MetricGroupName":"netric grp1","Enabled":"true",
	"CautionEnabled": "true",
	"DangerEnabled":"true",
	"CreateAlert_Type":"true",
	"AlertOnPositive": "true",
	"AllowResendAlert":"true",
    "SendAlertEveryNumMilliseconds": 5000,
	"CautionNotificationGroupName":"notification grp",
	"CautionPositiveNotificationGroupName": "positive notification grp1",
	"CautionWindowDurationTimeUnit": 100,
    "CautionWindowDuration": 10,
	"CautionStopTrackingAfterTimeUnit": 10,
	"CautionStopTrackingAfter": 5,
	"CautionMinimumSampleCount": 25,
	"CautionOperator": "true",
	"CautionCombination": "true",
	"CautionCombinationCount": 20,
	"CautionThreshold": 30,
	"DangerNotificationGroupName": danger notification grp 1,
	"DangerPositiveNotificationGroupName": danger positive notification grp name,
	"DangerWindowDurationTimeUnit": 50,
	"DangerWindowDuration": 21,
	"DangerStopTrackingAfterTimeUnit": 20,
    "DangerStopTrackingAfter": 34,
	"DangerMinimumSampleCount": 23,
	"DangerOperator": "true",
	"DangerCombination": "true",
	"DangerCombinationCount": 43,
	"DangerThreshold": "true"}
  ```


Create Alert Suspension 
----
  Creates a new Alert Suspension.

* **URL**

    `/StatsAgg/api/create-alertsuspension`

* **Method:**

    `POST`
  
*  **JSON Fields**
 
    `Name=[String] ` 

    `Description=[String] `

	`Enabled=[String] `

	`SuspendNotificationOnly=[String] `

	`CreateAlertSuspension_SuspendBy=[String] `	

	`AlertName=[String] `	

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
	"Duration": "true"}
  ```

Enable Alert 
----
  Enable Alert.

* **URL**

    `/StatsAgg/api/alert-enable`

* **Method:**

    `POST`
  
*  **URL Params**
 
    `Name=[String] ` 

    `Enabled=[Boolean] `


* **Example Request:**

    ` /StatsAgg/api/alert-enable?name=http_busy_threads&Enabled=true`
   

Remove Alert 
----
  Deletes an Alert from Database.

* **URL**

    `/StatsAgg/api/alert-remove`

* **Method:**

    `POST`
  
*  **URL Params**
 
    `Name=[String] ` 


* **Example Request:**

    ` /StatsAgg/api/alert-remove?name=alert_name`
   
* **Example Result:**


    ```{"response": "Delete alert success. AlertName="alert_name"."}
  ```


Remove Metric Group 
----
  Deletes a Metric Group.

* **URL**

    `/StatsAgg/api/metric-remove`

* **Method:**

    `POST`
  
*  **URL Params**
 
    `Name=[String] ` 


* **Example Request:**

    ` /StatsAgg/api/metric-remove?name=metric_grp_name`
   
* **Example Result:**

    ```{"response": "Delete metric group success. MetricGroupName="metric_grp_name"."}
  ```



Remove notification Group 
----
  Deletes a notification Group.

* **URL**

    `/StatsAgg/api/notification-remove`

* **Method:**

    `POST`
  
*  **URL Params**
 
    `Name=[String] ` 


* **Example Request:**

    ` /StatsAgg/api/notification-remove?name=notification_grp_name`
   
* **Example Result:**

        {"response": "Delete notification group success. NotificationGroupName="notification_grp_name"."}