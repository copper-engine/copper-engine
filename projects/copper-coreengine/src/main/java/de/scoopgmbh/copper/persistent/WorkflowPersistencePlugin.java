package de.scoopgmbh.copper.persistent;

import java.sql.Connection;
import java.sql.SQLException;


public interface WorkflowPersistencePlugin {

	void onWorkflowsLoaded(Connection con, Iterable<PersistentWorkflow<?>> workflows) throws SQLException;

	void onWorkflowsSaved(Connection con, Iterable<PersistentWorkflow<?>> workflows) throws SQLException;

	void onWorkflowsDeleted(Connection con, Iterable<PersistentWorkflow<?>> workflows) throws SQLException;
	
	final WorkflowPersistencePlugin NULL_PLUGIN = new WorkflowPersistencePlugin() {
		@Override
		public void onWorkflowsSaved(Connection con,
				Iterable<PersistentWorkflow<?>> workflows) throws SQLException {
			for (PersistentWorkflow<?> wf : workflows) {
				wf.doOnSave(PersistenceContext.NULL_CONTEXT);
			}
		}
		
		@Override
		public void onWorkflowsLoaded(Connection con,
				Iterable<PersistentWorkflow<?>> workflows) throws SQLException {
			for (PersistentWorkflow<?> wf : workflows) {
				wf.doOnLoad(PersistenceContext.NULL_CONTEXT);
			}
		}
		
		@Override
		public void onWorkflowsDeleted(Connection con,
				Iterable<PersistentWorkflow<?>> workflows) throws SQLException {
			for (PersistentWorkflow<?> wf : workflows) {
				wf.doOnDelete(PersistenceContext.NULL_CONTEXT);
			}
		}
	};

}
