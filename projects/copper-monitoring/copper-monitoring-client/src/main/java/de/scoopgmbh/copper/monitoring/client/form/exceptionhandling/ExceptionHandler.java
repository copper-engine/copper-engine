/*
 * Copyright 2002-2013 SCOOP Software GmbH
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
package de.scoopgmbh.copper.monitoring.client.form.exceptionhandling;

/**
 * workaround for a bug in javafx 2.2 
 * http://stackoverflow.com/questions/12318861/javafx-2-catching-all-runtime-exceptions
 * https://javafx-jira.kenai.com/browse/RT-15332
 */
public interface ExceptionHandler {
	public void handleException(Throwable e);
	public void handleException(String message, Throwable e);
	
	public void handleWarning(Throwable e);
	public void handleWarning(String message, Throwable e);
}
