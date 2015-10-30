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



-- 12 h
alter table COP_WORKFLOW_INSTANCE with GC_GRACE_SECONDS  = 43200;

-- 2 days
alter table COP_EARLY_RESPONSE with GC_GRACE_SECONDS  = 172800;