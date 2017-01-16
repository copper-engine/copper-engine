--
-- Copyright 2002-2017 SCOOP Software GmbH
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
    LAST_MOD_TS timestamp,
    CLASSNAME varchar,
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