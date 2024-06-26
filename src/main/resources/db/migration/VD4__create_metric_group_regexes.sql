CREATE TABLE METRIC_GROUP_REGEXES ( 
	ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),  
	METRIC_GROUP_ID INTEGER NOT NULL, 
	IS_BLACKLIST_REGEX BOOLEAN NOT NULL, 
	PATTERN CLOB(65535) NOT NULL 
);

ALTER TABLE METRIC_GROUP_REGEXES 
ADD CONSTRAINT MGR_PK PRIMARY KEY (ID);

ALTER TABLE METRIC_GROUP_REGEXES 
ADD CONSTRAINT MGR_MGID_FK FOREIGN KEY (METRIC_GROUP_ID) REFERENCES METRIC_GROUPS(ID);