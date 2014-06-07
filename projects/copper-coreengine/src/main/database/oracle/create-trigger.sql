create or replace trigger COP_BP_INSERT_TRIGGER
after insert on COP_WORKFLOW_INSTANCE
for each row
begin
	insert into COP_QUEUE (ppool_id, priority, last_mod_ts, WFI_ROWID) values (:new.ppool_id, :new.priority, :new.last_mod_ts, :new.rowid);
end;
/
