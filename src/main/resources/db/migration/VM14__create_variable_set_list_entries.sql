CREATE TABLE VARIABLE_SET_LIST_ENTRIES ( 
	ID INTEGER AUTO_INCREMENT PRIMARY KEY NOT NULL,  
	VARIABLE_SET_LIST_ID INTEGER NOT NULL, 
	VARIABLE_SET_ID INTEGER NOT NULL
);

ALTER TABLE VARIABLE_SET_LIST_ENTRIES 
ADD CONSTRAINT VSLE_VSL_FK FOREIGN KEY (VARIABLE_SET_LIST_ID) REFERENCES VARIABLE_SET_LISTS(ID);
	
ALTER TABLE VARIABLE_SET_LIST_ENTRIES 
ADD CONSTRAINT VSLE_VS_FK FOREIGN KEY (VARIABLE_SET_ID) REFERENCES VARIABLE_SETS(ID);