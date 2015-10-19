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
