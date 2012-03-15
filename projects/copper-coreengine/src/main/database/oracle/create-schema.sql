--
-- BUSINESSPROCESS
--
create table COP_WORKFLOW_INSTANCE  (
   ID           		VARCHAR2(128)                   not null,
   STATE                NUMBER(1)                       not null,
   PRIORITY             NUMBER(2)                       not null,
   LAST_MOD_TS          TIMESTAMP                       not null,
   PPOOL_ID      		VARCHAR2(32)					not null,
   DATA					VARCHAR2(4000) /* CLOB */		null,
   LONG_DATA			CLOB							null,
   CS_WAITMODE			NUMBER(1),
   MIN_NUMB_OF_RESP		NUMBER(10),
   NUMB_OF_WAITS		NUMBER(10),
   TIMEOUT				TIMESTAMP,
   CREATION_TS			TIMESTAMP						not null,
   constraint PK_COP_WORKFLOW_INSTANCE primary key (ID)
)
LOB(LONG_DATA) STORE AS SECUREFILE
INITRANS 5;

create table COP_WORKFLOW_INSTANCE_ERROR (
   WORKFLOW_INSTANCE_ID		VARCHAR2(128)	not null,
   EXCEPTION				CLOB			not null,
   ERROR_TS     	   		TIMESTAMP(3)    not null
);
 
create index IDX_COP_WFID_WFID on COP_WORKFLOW_INSTANCE_ERROR (
   WORKFLOW_INSTANCE_ID
);

--
-- RESPONSE
--
create table COP_RESPONSE  (
   CORRELATION_ID	VARCHAR2(128) not null,
   RESPONSE_TS		TIMESTAMP not null,
   RESPONSE			VARCHAR2(4000),
   LONG_RESPONSE	CLOB,
   constraint PK_COP_RESPONSE primary key (CORRELATION_ID) using index storage (buffer_pool keep)
)
LOB(LONG_RESPONSE) STORE AS SECUREFILE
;

 
--
-- WAIT
--
create table COP_WAIT (
   	CORRELATION_ID			VARCHAR2(128) not null,
   	WORKFLOW_INSTANCE_ID  	VARCHAR2(128) not null,
	MIN_NUMB_OF_RESP		NUMBER(10) not null,
	TIMEOUT_TS				TIMESTAMP(2),
   	WFI_ROWID				ROWID not null,
   	STATE					NUMBER(1) not null,
    PRIORITY            	NUMBER(2) not null,
    PPOOL_ID      			VARCHAR2(32) not null,
    constraint PK_COP_WAIT primary key (CORRELATION_ID)
--   constraint FK_COP_WAIT_REFERENCE_BP foreign key (WORKFLOW_INSTANCE_ID) references COP_WORKFLOW_INSTANCE (ID)
)
organization index storage (buffer_pool keep);


create index IDX_COP_WAIT_WFI_ID on COP_WAIT (
   WORKFLOW_INSTANCE_ID
)
storage (buffer_pool keep);

--
-- QUEUE
--
create table COP_QUEUE (
   PPOOL_ID      		VARCHAR2(32)					not null,
   PRIORITY             NUMBER(2)                       not null,
   LAST_MOD_TS          TIMESTAMP                       not null,
   WFI_ROWID			ROWID							not null,
   ENGINE_ID			VARCHAR2(16)					null,
   constraint PK_COP_QUEUE primary key (PPOOL_ID,PRIORITY,WFI_ROWID)
)
organization index 
storage (buffer_pool keep)
compress 2;

create table COP_AUDIT_TRAIL_EVENT (
	SEQ_ID 					NUMBER(19) NOT NULL,
	OCCURRENCE				TIMESTAMP NOT NULL,
	CONVERSATION_ID 		VARCHAR2(64) NOT NULL,
	LOGLEVEL				NUMBER(2) NOT NULL,
	CONTEXT					VARCHAR2(128) NOT NULL,
	INSTANCE_ID				VARCHAR2(128) NULL,
	CORRELATION_ID 			VARCHAR2(128) NULL,
	TRANSACTION_ID 			VARCHAR2(128) NULL,
	MESSAGE					VARCHAR2(4000) NOT NULL,
	LONG_MESSAGE 			CLOB NULL,
	MESSAGE_TYPE			VARCHAR2(256) NULL
);

CREATE SEQUENCE COP_SEQ_AUDIT_TRAIL CACHE 1000;

/*
create table COP_ENGINE (
	ENGINE_ID			VARCHAR2(16) NOT NULL,
	LAST_HEARTBEAT_TS	TIMESTAMP NOT NULL,
	NEXT_HEARTBEAT_TS	TIMESTAMP NOT NULL,
	FAILURE_DETECTED_TS TIMESTAMP NULL, 
   constraint PK_COP_ENGINE primary key (ENGINE_ID)
);
*/

