--
-- Copyright 2002-2015 SCOOP Software GmbH
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--      http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

SELECT y.WORKFLOW_INSTANCE_ID, y.ppool_id, y.priority FROM (
  SELECT x.WORKFLOW_INSTANCE_ID, max(x.is_timed_out) is_timed_out, min(x.min_numb_of_resp) min_numb_of_resp, sum(x.rcid) c, min(x.ppool_id) ppool_id, min(x.priority) priority from (
    SELECT w.correlation_id, w.WORKFLOW_INSTANCE_ID, w.min_numb_of_resp, w.priority, w.ppool_id, case when w.timeout_ts <= ? then 1 else 0 end is_timed_out, case when r.correlation_id is not null then 1 else 0 end rcid FROM 
    (SELECT * FROM COP_WAIT WHERE state=0) w
    LEFT OUTER JOIN
    (SELECT DISTINCT correlation_id FROM COP_RESPONSE) r
    ON w.correlation_id = r.correlation_id
    WHERE w.timeout_ts <= ? or r.correlation_id is not null
  ) x 
  GROUP BY x.WORKFLOW_INSTANCE_ID
) y 
WHERE y.is_timed_out = 1 OR y.min_numb_of_resp <= y.c