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
package org.copperengine.monitoring.client.form.issuereporting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum LogIssueReporter implements IssueReporter {
    INSTANCE;
    
    final Logger logger = LoggerFactory.getLogger(LogIssueReporter.class);

    @Override
    public void reportError(Throwable e) {
        logger.error("", e);
    }

    @Override
    public void reportError(String message, Throwable e) {
        logger.error(message, e);
    }

    @Override
    public void reportWarning(Throwable e) {
        logger.warn("", e);
    }

    @Override
    public void reportWarning(String message, Throwable e) {
        logger.warn(message, e);
    }

    @Override
    public void reportError(String message, Throwable e, Runnable finishAction) {
        logger.error(message, e);
    }

    @Override
    public void reportWarning(String message, Throwable e, Runnable finishAction) {
        logger.warn(message, e);
    }
}
