truncate table copper2.COP_WAIT;
truncate table copper2.COP_RESPONSE;
truncate table copper2.COP_WORKFLOW_INSTANCE;
truncate table copper2.COP_QUEUE;
truncate table copper2.COP_WORKFLOW_INSTANCE_ERROR;

alter table copper2.COP_WAIT move;
alter table copper2.COP_RESPONSE move;
alter table copper2.COP_WORKFLOW_INSTANCE move;
alter table copper2.COP_QUEUE move;
alter table copper2.COP_WORKFLOW_INSTANCE_ERROR move;



