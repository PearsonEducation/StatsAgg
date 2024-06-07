CREATE TABLE SUSPENSIONS ( 
	ID INTEGER AUTO_INCREMENT PRIMARY KEY NOT NULL,  
	NAME VARCHAR(500) NOT NULL,  
	UPPERCASE_NAME VARCHAR(500) NOT NULL,  
	DESCRIPTION MEDIUMTEXT NOT NULL,  
	IS_ENABLED BOOLEAN NOT NULL,  
	SUSPEND_BY INTEGER NOT NULL,  
	ALERT_ID INTEGER, 
	METRIC_GROUP_TAGS_INCLUSIVE MEDIUMTEXT,  
	METRIC_GROUP_TAGS_EXCLUSIVE MEDIUMTEXT,  
	METRIC_SUSPENSION_REGEXES MEDIUMTEXT, 
	IS_ONE_TIME BOOLEAN NOT NULL,  
	IS_SUSPEND_NOTIFICATION_ONLY BOOLEAN NOT NULL,  
	IS_RECUR_SUNDAY BOOLEAN NOT NULL,  
	IS_RECUR_MONDAY BOOLEAN NOT NULL,  
	IS_RECUR_TUESDAY BOOLEAN NOT NULL,  
	IS_RECUR_WEDNESDAY BOOLEAN NOT NULL,  
	IS_RECUR_THURSDAY BOOLEAN NOT NULL,  
	IS_RECUR_FRIDAY BOOLEAN NOT NULL,  
	IS_RECUR_SATURDAY BOOLEAN NOT NULL, 
	START_DATE TIMESTAMP NULL DEFAULT NULL,  
	START_TIME TIMESTAMP NULL DEFAULT NULL, 
	DURATION BIGINT NOT NULL,  
	DURATION_TIME_UNIT INTEGER NOT NULL,  
	DELETE_AT_TIMESTAMP TIMESTAMP NULL DEFAULT NULL 
);

ALTER TABLE SUSPENSIONS 
ADD CONSTRAINT AS_U_NAME UNIQUE (NAME);

ALTER TABLE SUSPENSIONS 
ADD CONSTRAINT AS_U_UPPERCASE_NAME UNIQUE (UPPERCASE_NAME);

CREATE INDEX AS_SUSPEND_BY ON SUSPENSIONS(SUSPEND_BY);

CREATE INDEX AS_DELETE_AT_TIMESTAMP ON SUSPENSIONS(DELETE_AT_TIMESTAMP);

ALTER TABLE SUSPENSIONS 
ADD CONSTRAINT AS_AID_FK FOREIGN KEY (ALERT_ID) REFERENCES ALERTS(ID);