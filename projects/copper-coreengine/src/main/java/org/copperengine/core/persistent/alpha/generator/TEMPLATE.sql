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

CREATE TABLE "TEMPLATE"
(
   WORKFLOWID varchar2(128) NOT NULL,
   ENTITYID   varchar2(128) NOT NULL,
   /*BEGINCOLUMNS*/
   NAME  varchar2(200),
   INTVALUE decimal(9)/*ENDCOLUMNS*/
   , CONSTRAINT PK_TEMPLATE PRIMARY KEY ( WORKFLOWID, ENTTIYID )
);

