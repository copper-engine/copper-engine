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
package de.scoopgmbh.copper.monitoring.client.form.issuereporting;

import javafx.scene.layout.StackPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.scoopgmbh.copper.monitoring.client.util.ComponentUtil;

public class MessageAndLogIssueReporter implements IssueReporter {
	
	Logger logger = LoggerFactory.getLogger(MessageAndLogIssueReporter.class);
	private final StackPane stackPane;

	
	
	public MessageAndLogIssueReporter(StackPane stackPane) {
		super();
		this.stackPane = stackPane;
	}

	@Override
	public void reportError(Throwable e) {
		logger.error("",e);
		ComponentUtil.showErrorMessage(stackPane,e.getMessage(), e);
	}

	@Override
	public void reportError(String message, Throwable e) {
		logger.error("",e);
		ComponentUtil.showErrorMessage(stackPane,message, e);
	}

	@Override
	public void reportWarning(Throwable e) {
		logger.warn("",e);
		ComponentUtil.showWarningMessage(stackPane,e.getMessage(), e);
	}

	@Override
	public void reportWarning(String message, Throwable e) {
		logger.warn("",e);
		ComponentUtil.showWarningMessage(stackPane,message, e);
	}

	@Override
	public void reportError(String message, Throwable e, Runnable finishAction) {
		logger.error("",e);
		ComponentUtil.showErrorMessage(stackPane,message, e,finishAction);
	}

	@Override
	public void reportWarning(String message, Throwable e, Runnable finishAction) {
		logger.warn("",e);
		ComponentUtil.showWarningMessage(stackPane,message, e,finishAction);
	}


}
