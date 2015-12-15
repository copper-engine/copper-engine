CREATE TABLE COP_WFI_ID  (
    ID varchar,
    primary key (ID)
);

alter table COP_WFI_ID with GC_GRACE_SECONDS  = 1;


CREATE TABLE COP_WORKFLOW_INSTANCE  (
    ID varchar,
    STATE varchar,
    PPOOL_ID varchar,
    PRIO int,
    CREATION_TS timestamp,
    WAIT_MODE varchar,
    TIMEOUT timestamp,
    RESPONSE_MAP_JSON varchar,
    DATA varchar,
    OBJECT_STATE varchar,
    primary key (ID)
);

CREATE TABLE COP_EARLY_RESPONSE (
    CORRELATION_ID varchar,
    RESPONSE varchar,
    primary key (CORRELATION_ID)
);



-- 5 days
alter table COP_WORKFLOW_INSTANCE with GC_GRACE_SECONDS  = 432000;

-- 2 days
alter table COP_EARLY_RESPONSE with GC_GRACE_SECONDS  = 172800;