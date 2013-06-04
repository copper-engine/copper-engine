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
package de.scoopgmbh.copper.monitoring.client.ui.workflowinstance.result;

import javafx.scene.layout.BorderPane;
import de.scoopgmbh.copper.monitoring.client.context.FormContext;
import de.scoopgmbh.copper.monitoring.client.form.FxmlForm;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterAbleForm;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterResultController;
import de.scoopgmbh.copper.monitoring.client.ui.audittrail.filter.AuditTrailFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.audittrail.result.AuditTrailResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.worklowinstancedetail.filter.WorkflowInstanceDetailFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.worklowinstancedetail.result.WorkflowInstanceDetailResultModel;
import de.scoopgmbh.copper.monitoring.core.model.ProcessingEngineInfo;

public class BaseWorkflowInstanceListNavigation implements WorkflowInstanceListNavigation {

	FormContext formContext;
	public BaseWorkflowInstanceListNavigation(FormContext formContext) {
		super();
		this.formContext = formContext;
	}

	@Override
	public void navigateToAudittrail(String workflowInstanceId) {
		FilterAbleForm<AuditTrailFilterModel,AuditTrailResultModel> audittrailForm = formContext.createAudittrailForm();
		audittrailForm.getFilter().workflowInstanceId.set(workflowInstanceId);
		audittrailForm.show();
	}

	@Override
	public void navigateToIntsanceDetail(String workflowInstanceId, ProcessingEngineInfo engine) {
		formContext.createWorkflowInstanceDetailForm(workflowInstanceId,engine).show();
	}

	@Override
	public FxmlForm<FilterResultController<WorkflowInstanceDetailFilterModel, WorkflowInstanceDetailResultModel>> createWorkflowinstanceDetailResultForm(
			BorderPane detailPane) {
		return formContext.createWorkflowinstanceDetailResultForm(detailPane);
	}
	
}
