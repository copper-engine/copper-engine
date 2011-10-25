drop table COP_WORKFLOW_INSTANCE;
drop table COP_WAIT;
drop table COP_RESPONSE;
drop table COP_QUEUE;


--
-- BUSINESSPROCESS
--
create table COP_WORKFLOW_INSTANCE  (
   ID           		VARCHAR(128) not null,
   STATE                TINYINT not null,
   PRIORITY             TINYINT not null,
   LAST_MOD_TS          TIMESTAMP not null,
   PPOOL_ID      		VARCHAR(32) not null,
   DATA					MEDIUMTEXT not null,
   CS_WAITMODE			TINYINT,
   MIN_NUMB_OF_RESP		SMALLINT,
   NUMB_OF_WAITS		SMALLINT,
   TIMEOUT				TIMESTAMP,
   PRIMARY KEY (ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
 
--
-- RESPONSE
--
create table COP_RESPONSE  (
   CORRELATION_ID	VARCHAR(128) not null,
   RESPONSE_TS		TIMESTAMP not null,
   RESPONSE			MEDIUMTEXT,
   PRIMARY KEY (CORRELATION_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

 
--
-- WAIT
--
create table COP_WAIT (
   	CORRELATION_ID			VARCHAR(128) not null,
   	WORKFLOW_INSTANCE_ID  	VARCHAR(128) not null,
	MIN_NUMB_OF_RESP		SMALLINT not null,
	TIMEOUT_TS				TIMESTAMP,
   	STATE					TINYINT not null,
    PRIORITY            	TINYINT not null,
    PPOOL_ID      			VARCHAR(32) not null,
    PRIMARY KEY (CORRELATION_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


create index IDX_COP_WAIT_WFI_ID on COP_WAIT (
   WORKFLOW_INSTANCE_ID
);

--
-- QUEUE
--
create table COP_QUEUE (
   PPOOL_ID      		VARCHAR(32)					not null,
   PRIORITY             TINYINT                         not null,
   LAST_MOD_TS          TIMESTAMP                       not null,
   WORKFLOW_INSTANCE_ID	VARCHAR(128) 					not null,
   PRIMARY KEY (WORKFLOW_INSTANCE_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


