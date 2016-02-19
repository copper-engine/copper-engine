/*
 * Copyright 2002-2015 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.copperengine.monitoring.server.persistent;

import org.copperengine.core.audit.BatchingAuditTrail;
import org.copperengine.core.audit.MessagePostProcessor;
import org.copperengine.core.persistent.Serializer;

/**
 * MySQL implementation of the {@link DatabaseMonitoringDialect} interface.
 *
 */
public class MySqlMonitoringDbDialect extends BaseDatabaseMonitoringDialect {

    public MySqlMonitoringDbDialect(Serializer serializer, MessagePostProcessor messagePostProcessor, BatchingAuditTrail auditTrail) {
        super(serializer, messagePostProcessor, auditTrail);
    }

}
