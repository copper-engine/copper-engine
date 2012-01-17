SELECT y.WORKFLOW_INSTANCE_ID, y.ppool_id, y.priority FROM (
  SELECT x.WORKFLOW_INSTANCE_ID, max(x.is_timed_out) is_timed_out, min(x.min_numb_of_resp) min_numb_of_resp, sum(x.rcid) c, min(x.ppool_id) ppool_id, min(x.priority) priority from (
    SELECT w.correlation_id, w.WORKFLOW_INSTANCE_ID, w.min_numb_of_resp, w.priority, w.ppool_id, case when w.timeout_ts <= ? then 1 else 0 end is_timed_out, case when r.correlation_id is not null then 1 else 0 end rcid FROM 
    (SELECT * FROM cop_wait WHERE state=0) w
    LEFT OUTER JOIN
    cop_response r
    ON w.correlation_id = r.correlation_id
    WHERE w.timeout_ts <= ? or r.correlation_id is not null
  ) x 
  GROUP BY x.WORKFLOW_INSTANCE_ID
) y 
WHERE y.is_timed_out = 1 OR y.min_numb_of_resp <= y.c