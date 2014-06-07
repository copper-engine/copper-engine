create or replace view COP_WFI_MONITOR
AS
select 
wfi.id, 
decode(wfi.state,3,'FINISHED',4,'INVALID',5,'ERROR',decode(q.wfi_rowid,NULL,'WAITING',decode(q.engine_id,NULL,'ENQUEUED','DEQUEUED_PROCESSING'))) STATE,
wfi.state db_state, 
wfi.priority, 
wfi.last_mod_ts, 
wfi.ppool_id, 
wfi.cs_waitmode, 
wfi.min_numb_of_resp, 
wfi.numb_of_waits, 
wfi.timeout 
from COP_WORKFLOW_INSTANCE wfi, COP_QUEUE q
where wfi.rowid = q.wfi_rowid(+);


create or replace view COP_QUEUE_MONITOR
AS
select ppool_id, count(*) count from COP_QUEUE group by ppool_id;

