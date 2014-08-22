/*
 * Copyright 2002-2014 SCOOP Software GmbH
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
package org.copperengine.monitoring.client.util;

public enum MessageKey {
    dynamicworkflow_title("dynamicworkflow.title"),
    filterAbleForm_button_refresh("filterAbleForm.button.refresh"),
    filterAbleForm_button_clear("filterAbleForm.button.clear"),
    filterAbleForm_button_expandall("filterAbleForm.button.expandall"),
    filterAbleForm_button_collapseall("filterAbleForm.button.collapseall"),
    workflowOverview_title("workflowoverview.title"),
    workflowInstance_title("workflowInstance.title"),
    audittrail_title("audittrail.title"),
    workflowClassesTreeForm_title("workflowClassesTreeForm.title"),
    workflowInstanceDetail_title("workflowInstanceDetail.title"),
    engineLoad_title("engineLoad.title"),
    settings_title("settings.title"),
    sql_title("sql.title"),
    resource_title("resource.title"),
    loadGroup_title("loadGroup.title"),
    configuration_title("configuration.title"),
    measurePoint_title("measurePoint.title"),
    message_title("message.title"),
    workflowGroup_title("workflowGroup.title"),
    workflowRepository_title("workflowRepository.title"),
    hotfix_title("hotfix.title"),
    adapterMonitoring_title("adapterMonitoring.title"),
    customMeasurePoint_title("customMeasurePoint.title"),
    logsGroup_title("logsGroup.title"),
    logs_title("logs.title"),
    databaseMonitoring_title("databaseMonitoring.title"),
    provider_title("provider.title");

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
