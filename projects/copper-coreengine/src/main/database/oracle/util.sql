exec dbms_stats.UNLOCK_SCHEMA_STATS('COPPER2');
exec dbms_stats.GATHER_SCHEMA_STATS('COPPER2');
exec dbms_stats.LOCK_SCHEMA_STATS('COPPER2');

truncate table copper2.COP_WAIT;
truncate table copper2.COP_RESPONSE;
truncate table copper2.COP_WORKFLOW_INSTANCE;
truncate table copper2.COP_QUEUE;
truncate table copper2.COP_WORKFLOW_INSTANCE_ERROR;


--
-- Most COPPER tables are volatile, i.e. they may be empty now and contain a million rows a moment later.
-- For that reason, the CBO statistics are mostly bad. To prevent the CBO from creating bad execution plans
-- the statistics for most COPPER tables are deleted and locked.
-- The CBO will use some default values. 
--
exec DBMS_STATS.DELETE_TABLE_STATS (sys_context('USERENV', 'CURRENT_SCHEMA'), 'COP_WORKFLOW_INSTANCE');
exec DBMS_STATS.DELETE_TABLE_STATS (sys_context('USERENV', 'CURRENT_SCHEMA'), 'COP_WORKFLOW_INSTANCE_ERROR');
exec DBMS_STATS.DELETE_TABLE_STATS (sys_context('USERENV', 'CURRENT_SCHEMA'), 'COP_RESPONSE');
exec DBMS_STATS.DELETE_TABLE_STATS (sys_context('USERENV', 'CURRENT_SCHEMA'), 'COP_WAIT');
exec DBMS_STATS.DELETE_TABLE_STATS (sys_context('USERENV', 'CURRENT_SCHEMA'), 'COP_QUEUE');

exec DBMS_STATS.LOCK_TABLE_STATS (sys_context('USERENV', 'CURRENT_SCHEMA'), 'COP_WORKFLOW_INSTANCE');
exec DBMS_STATS.LOCK_TABLE_STATS (sys_context('USERENV', 'CURRENT_SCHEMA'), 'COP_WORKFLOW_INSTANCE_ERROR');
exec DBMS_STATS.LOCK_TABLE_STATS (sys_context('USERENV', 'CURRENT_SCHEMA'), 'COP_RESPONSE');
exec DBMS_STATS.LOCK_TABLE_STATS (sys_context('USERENV', 'CURRENT_SCHEMA'), 'COP_WAIT');
exec DBMS_STATS.LOCK_TABLE_STATS (sys_context('USERENV', 'CURRENT_SCHEMA'), 'COP_QUEUE');
