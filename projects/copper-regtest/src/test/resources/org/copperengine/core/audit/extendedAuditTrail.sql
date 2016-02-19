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

create table COP_AUDIT_TRAIL_EVENT_EXTENDED (
	SEQ_ID 					BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	OCCURRENCE				TIMESTAMP NOT NULL,
	CONVERSATION_ID 		VARCHAR(64) NOT NULL,
	LOGLEVEL				SMALLINT NOT NULL,
	CONTEXT					VARCHAR(128) NOT NULL,
	INSTANCE_ID				VARCHAR(128),
	CORRELATION_ID 			VARCHAR(128),
	TRANSACTION_ID 			VARCHAR(128),
	LONG_MESSAGE 			CLOB,
	MESSAGE_TYPE			VARCHAR(256),
	CUSTOM_VARCHAR			VARCHAR(256),
	CUSTOM_INT				SMALLINT,
	CUSTOM_TIMESTAMP		TIMESTAMP,
    PRIMARY KEY (SEQ_ID)
)