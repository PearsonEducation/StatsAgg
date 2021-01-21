ALTER TABLE ALERTS ADD COLUMN ALERT_TEMPLATE_ID INTEGER;
ALTER TABLE ALERTS ADD COLUMN VARIABLE_SET_ID INTEGER;

ALTER TABLE ALERTS 
ADD CONSTRAINT A_ATID_FK FOREIGN KEY (ALERT_TEMPLATE_ID) REFERENCES ALERT_TEMPLATES(ID);

ALTER TABLE ALERTS 
ADD CONSTRAINT A_VSID_FK FOREIGN KEY (VARIABLE_SET_ID) REFERENCES VARIABLE_SETS(ID);