drop table if exists COP_WORKFLOW_INSTANCE_ERROR;
drop table if exists COP_WORKFLOW_INSTANCE;
drop table if exists COP_WAIT;
drop table if exists COP_RESPONSE;
drop table if exists COP_QUEUE;
drop table if exists COP_AUDIT_TRAIL_EVENT;
drop table if exists COP_ADAPTERCALL;


--
-- WORKFLOW_INSTANCE
--
create table COP_WORKFLOW_INSTANCE  (
  ID                   VARCHAR(128)    not null,
  STATE                SMALLINT        not null,
  PRIORITY             SMALLINT        not null,
  LAST_MOD_TS          TIMESTAMP       not null,
  PPOOL_ID             VARCHAR(32)     not null,
  DATA                 TEXT null,
  OBJECT_STATE         TEXT null,
  CS_WAITMODE          SMALLINT,
  MIN_NUMB_OF_RESP     SMALLINT,
  NUMB_OF_WAITS        SMALLINT,
  TIMEOUT              TIMESTAMP,
  CREATION_TS          TIMESTAMP       not null,
  CLASSNAME            VARCHAR(512)    not null,
  CONSTRAINT PK_COP_WORKFLOW_INSTANCE PRIMARY KEY (ID)
);


create table COP_WORKFLOW_INSTANCE_ERROR (
  WORKFLOW_INSTANCE_ID VARCHAR(128)    not null,
  EXCEPTION            TEXT            not null,
  ERROR_TS             TIMESTAMP       not null
);

create index IDX_COP_WFID_WFID on COP_WORKFLOW_INSTANCE_ERROR (
  WORKFLOW_INSTANCE_ID
);


--
-- RESPONSE
--
create table COP_RESPONSE  (
  RESPONSE_ID          VARCHAR(128)    not null,
  CORRELATION_ID       VARCHAR(128)    not null,
  RESPONSE_TS          TIMESTAMP       not null,
  RESPONSE             TEXT,
  RESPONSE_TIMEOUT     TIMESTAMP,
  RESPONSE_META_DATA   VARCHAR(4000),
  CONSTRAINT PK_COP_RESPONSE PRIMARY KEY (RESPONSE_ID)
);

create index IDX_COP_RESP_CID on COP_RESPONSE (
  CORRELATION_ID
);


--
-- WAIT
--
create table COP_WAIT (
  CORRELATION_ID       VARCHAR(128)    not null,
  WORKFLOW_INSTANCE_ID VARCHAR(128)    not null,
  MIN_NUMB_OF_RESP     SMALLINT        not null,
  TIMEOUT_TS           TIMESTAMP,
  STATE                SMALLINT        not null,
  PRIORITY             SMALLINT        not null,
  PPOOL_ID             VARCHAR(32)     not null,
  CONSTRAINT PK_COP_WAIT PRIMARY KEY (CORRELATION_ID)
);


create index IDX_COP_WAIT_WFI_ID on COP_WAIT (
  WORKFLOW_INSTANCE_ID
);


--
-- QUEUE
--
create table COP_QUEUE (
  PPOOL_ID             VARCHAR(32)     not null,
  PRIORITY             SMALLINT        not null,
  LAST_MOD_TS          TIMESTAMP       not null,
  WORKFLOW_INSTANCE_ID VARCHAR(128)    not null,
  CONSTRAINT PK_COP_QUEUE PRIMARY KEY (WORKFLOW_INSTANCE_ID)
);


--
-- AUDIT_TRAIL_EVENT
--
create table COP_AUDIT_TRAIL_EVENT (
  SEQ_ID               BIGINT          not null auto_increment,
  OCCURRENCE           TIMESTAMP       not null,
  CONVERSATION_ID      VARCHAR(64)     not null,
  LOGLEVEL             SMALLINT        not null,
  CONTEXT              VARCHAR(128)    not null,
  INSTANCE_ID          VARCHAR(128),
  CORRELATION_ID       VARCHAR(128),
  TRANSACTION_ID       VARCHAR(128),
  LONG_MESSAGE         TEXT,
  MESSAGE_TYPE         VARCHAR(256),
  CONSTRAINT PK_COP_AUDIT_TRAIL_EVENT PRIMARY KEY (SEQ_ID)
);


--
-- ADAPTERCALL
--
CREATE TABLE COP_ADAPTERCALL (
  WORKFLOWID           VARCHAR(128)    not null,
  ENTITYID             VARCHAR(128)    not null,
  ADAPTERID            VARCHAR(256)    not null,
  PRIORITY             BIGINT          not null,
  DEFUNCT              CHAR(1)         not null default '0',
  DEQUEUE_TS           TIMESTAMP,
  METHODDECLARINGCLASS VARCHAR(1024)   not null,
  METHODNAME           VARCHAR(1024)   not null,
  METHODSIGNATURE      VARCHAR(2048)   not null,
  ARGS                 TEXT,
  CONSTRAINT PK_COP_ADAPTERCALL PRIMARY KEY (ADAPTERID, WORKFLOWID, ENTITYID)
);

CREATE INDEX COP_IDX_ADAPTERCALL ON COP_ADAPTERCALL(ADAPTERID, PRIORITY);

