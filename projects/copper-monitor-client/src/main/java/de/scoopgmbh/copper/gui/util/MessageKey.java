/*
 * Copyright 2002-2012 SCOOP Software GmbH
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
package de.scoopgmbh.copper.gui.util;

public enum MessageKey {
	dynamicworkflow_title("dynamicworkflow.title"),
	filterAbleForm_button_refresh("filterAbleForm.button.refresh"),
	filterAbleForm_button_clear("filterAbleForm.button.clear"),
	workflowoverview_title("workflowoverview.title"),
	workflowInstance_title("workflowInstance.title"),
	audittrail_title("audittrail.title"),
	workflowClassesTreeForm_title("workflowClassesTreeForm.title"),
	workflowInstanceDetail_title("workflowInstanceDetail.title"),
	engineLoad_title("engineLoad.title"),
	settings_title("settings.title"),
	sql_title("sql.title"),
	resource_title("resource.title"),
	loadGroup_title("loadGroup.title"),
	dashboard_title("dashboard.title"),
	measurePoint_title("measurePoint.title"),
	workflowHistory_title("workflowHistory.title"),
	workflowGroup_title("workflowGroup.title"),
	workflowRepository_title("workflowRepository.title"),
	hotfix_title("hotfix.title");
			 
    private final String value;
    MessageKey(String v) {
        value = v;
    }
    
    public String value() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }


}
