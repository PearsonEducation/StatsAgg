CREATE TABLE PAGERDUTY_SERVICES ( 
	ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), 
	NAME VARCHAR(500) NOT NULL, 
	UPPERCASE_NAME VARCHAR(500) NOT NULL, 
	DESCRIPTION CLOB(1048576) NOT NULL, 
	ROUTING_KEY VARCHAR(500) NOT NULL 
);

ALTER TABLE PAGERDUTY_SERVICES 
ADD CONSTRAINT PDS_PK PRIMARY KEY (ID);

ALTER TABLE PAGERDUTY_SERVICES 
ADD CONSTRAINT PDS_U_NAME UNIQUE (NAME);

ALTER TABLE PAGERDUTY_SERVICES 
ADD CONSTRAINT PDS_U_UPPERCASE_NAME UNIQUE (UPPERCASE_NAME);

CREATE INDEX PDS_ROUTINGKEY ON PAGERDUTY_SERVICES(ROUTING_KEY);