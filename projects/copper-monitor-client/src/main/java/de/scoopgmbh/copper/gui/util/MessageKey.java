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
	measurePoint_title("measurePoint.title");
			
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
