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

/**
 * workaround for a bug in javafx 2.2 (not possible to define uncougtexception handler)
 * http://stackoverflow.com/questions/12318861/javafx-2-catching-all-runtime-exceptions
 * https://javafx-jira.kenai.com/browse/RT-15332
 */
public interface IssueReporter {
    public void reportError(Throwable e);

    public void reportError(String message, Throwable e);

    /**
     * @param message
     * @param e
     * @param finishAction
     *            action executed when ok button is clicked/ message is procceded
     */
    public void reportError(String message, Throwable e, Runnable finishAction);

    public void reportWarning(Throwable e);

    public void reportWarning(String message, Throwable e);

    /**
     * @param message
     * @param e
     * @param finishAction
     *            action executed when ok button is clicked/ message is procceded
     */
    public void reportWarning(String message, Throwable e, Runnable finishAction);
}
