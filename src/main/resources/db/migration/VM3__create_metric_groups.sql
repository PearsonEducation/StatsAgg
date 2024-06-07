CREATE TABLE METRIC_GROUPS ( 
	ID INTEGER AUTO_INCREMENT PRIMARY KEY NOT NULL,  
	NAME VARCHAR(500) NOT NULL,  
	UPPERCASE_NAME VARCHAR(500) NOT NULL,  
	DESCRIPTION MEDIUMTEXT NOT NULL 
);

ALTER TABLE METRIC_GROUPS 
ADD CONSTRAINT MG_U_NAME UNIQUE (NAME);

ALTER TABLE METRIC_GROUPS 
ADD CONSTRAINT MG_U_UPPERCASE_NAME UNIQUE (UPPERCASE_NAME);