drop table COP_WORKFLOW_INSTANCE_ERROR;
drop table COP_WORKFLOW_INSTANCE;
drop table COP_WAIT;
drop table COP_RESPONSE;
drop table COP_QUEUE;
drop table COP_AUDIT_TRAIL_EVENT;

--
-- BUSINESSPROCESS
--
create table COP_WORKFLOW_INSTANCE  (
   ID           		VARCHAR(128) not null,
   STATE                SMALLINT not null,
   PRIORITY             SMALLINT not null,
   LAST_MOD_TS          TIMESTAMP not null,
   PPOOL_ID      		VARCHAR(32) not null,
   DATA					TEXT null,
   OBJECT_STATE			TEXT null,
   CS_WAITMODE			SMALLINT,
   MIN_NUMB_OF_RESP		SMALLINT,
   NUMB_OF_WAITS		SMALLINT,
   TIMEOUT				TIMESTAMP,
   CREATION_TS			TIMESTAMP not null,
   CLASSNAME			VARCHAR(512) not null,
   PRIMARY KEY (ID)
);
 

create table COP_WORKFLOW_INSTANCE_ERROR (
   WORKFLOW_INSTANCE_ID		VARCHAR(128)	not null,
   EXCEPTION				TEXT			not null,
   ERROR_TS     	   		TIMESTAMP       not null
);

create index IDX_COP_WFID_WFID on COP_WORKFLOW_INSTANCE_ERROR (
   WORKFLOW_INSTANCE_ID
);

--
-- RESPONSE
--
create table COP_RESPONSE  (
   RESPONSE_ID		VARCHAR(128) not null,
   CORRELATION_ID	VARCHAR(128) not null,
   RESPONSE_TS		TIMESTAMP not null,
   RESPONSE			TEXT,
   RESPONSE_TIMEOUT	 TIMESTAMP,
   RESPONSE_META_DATA VARCHAR(4000),
   PRIMARY KEY (RESPONSE_ID)
);

create index IDX_COP_RESP_CID on COP_RESPONSE (
   CORRELATION_ID
);

 
--
-- WAIT
--
create table COP_WAIT (
   	CORRELATION_ID			VARCHAR(128) not null,
   	WORKFLOW_INSTANCE_ID  	VARCHAR(128) not null,
	MIN_NUMB_OF_RESP		SMALLINT not null,
	TIMEOUT_TS				TIMESTAMP,
   	STATE					SMALLINT not null,
    PRIORITY            	SMALLINT not null,
    PPOOL_ID      			VARCHAR(32) not null,
    PRIMARY KEY (CORRELATION_ID)
);


create index IDX_COP_WAIT_WFI_ID on COP_WAIT (
   WORKFLOW_INSTANCE_ID
);

--
-- QUEUE
--
create table COP_QUEUE (
   PPOOL_ID      		VARCHAR(32)	    				not null,
   PRIORITY             SMALLINT                        not null,
   LAST_MOD_TS          TIMESTAMP                       not null,
   WORKFLOW_INSTANCE_ID	VARCHAR(128) 					not null,
   PRIMARY KEY (WORKFLOW_INSTANCE_ID)
);


create table COP_AUDIT_TRAIL_EVENT (
	SEQ_ID 					SERIAL,
	OCCURRENCE				TIMESTAMP NOT NULL,
	CONVERSATION_ID 		VARCHAR(64) NOT NULL,
	LOGLEVEL				SMALLINT NOT NULL,
	CONTEXT					VARCHAR(128) NOT NULL,
	INSTANCE_ID				VARCHAR(128) NULL,
	CORRELATION_ID 			VARCHAR(128) NULL,
	TRANSACTION_ID 			VARCHAR(128) NULL,
	LONG_MESSAGE 			TEXT NULL,
	MESSAGE_TYPE			VARCHAR(256) NULL,
    PRIMARY KEY (SEQ_ID)
);



CREATE TABLE COP_ADAPTERCALL ("WORKFLOWID"  VARCHAR(128) NOT NULL,
                          "ENTITYID"    VARCHAR(128) NOT NULL,
                          "ADAPTERID"   VARCHAR(256) NOT NULL,
                          "PRIORITY"    BIGINT NOT NULL,
                          "DEFUNCT"     CHAR(1) DEFAULT '0' NOT NULL ,
                          "DEQUEUE_TS"  TIMESTAMP , 
                          "METHODDECLARINGCLASS" VARCHAR(1024)  NOT NULL,
                          "METHODNAME" VARCHAR(1024)  NOT NULL,
                          "METHODSIGNATURE" VARCHAR(2048)  NOT NULL,
                          "ARGS" CLOB,
                          PRIMARY KEY (ADAPTERID, WORKFLOWID, ENTITYID));

CREATE INDEX COP_IDX_ADAPTERCALL ON ADAPTERCALL(ADAPTERID, PRIORITY);
