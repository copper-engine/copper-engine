package de.scoopgmbh.copper.persistent.alpha.generator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;

import org.springframework.jdbc.support.nativejdbc.C3P0NativeJdbcExtractor;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;

import de.scoopgmbh.copper.persistent.DefaultEntityPersister;
import de.scoopgmbh.copper.persistent.DefaultEntityPersisterFactory;
import de.scoopgmbh.copper.persistent.DefaultPersistenceWorker;
import de.scoopgmbh.copper.persistent.DefaultPersisterSharedRessources;
import de.scoopgmbh.copper.persistent.DefaultPersisterSimpleCRUDSharedRessources;
import de.scoopgmbh.copper.persistent.PersistentWorkflow;

/*ADDITIONAL_IMPORTS*/

public abstract class TEMPLATEPersisterFactory implements
		DefaultEntityPersisterFactory<TEMPLATE, DefaultEntityPersister<TEMPLATE>> {

	static final NativeJdbcExtractor extractor = new C3P0NativeJdbcExtractor();
	

	@Override
	public Class<TEMPLATE> getEntityClass() {
		return TEMPLATE.class;
	}

	@Override
	public Class<?> getPersisterClass() {
		return DefaultEntityPersister.class;
	}

	@Override
	public DefaultEntityPersister<TEMPLATE> createPersister(PersistentWorkflow<?> workflow,
			DefaultPersisterSharedRessources<TEMPLATE, DefaultEntityPersister<TEMPLATE>> sharedRessources) {
		return new DefaultEntityPersister<TEMPLATE>(workflow, (DefaultPersisterSimpleCRUDSharedRessources<TEMPLATE, DefaultEntityPersister<TEMPLATE>>)sharedRessources);
	}
	

	static class EntityIdentity implements Comparable<EntityIdentity> {
		DefaultPersistenceWorker.WorkflowAndEntity<TEMPLATE> entry;
		String workflowId;
		String entityId;
		public EntityIdentity (DefaultPersistenceWorker.WorkflowAndEntity<TEMPLATE> entry) {
			this.entry = entry;
			this.workflowId = entry.workflow.getId();
			this.entityId = entry.entity.getEntityId();
		}
		public EntityIdentity (String workflowId,String entityId) {
			this.workflowId = workflowId;
			this.entityId = entityId;
		}
		@Override
		public int compareTo(EntityIdentity o) {
			return compare(this,o);
		}
		static int compare(
				EntityIdentity o1,
				EntityIdentity o2) {
			int cmp = o1.workflowId.compareTo(o2.workflowId);
			if (cmp != 0)
				return cmp;
			String entityId1 = o1.entityId;
			String entityId2 = o2.entityId;
			if (entityId1 == null) {
				if (entityId2 != null)
					return -1;
				return 1;
			}
			return entityId1.compareTo(entityId2);
		}
		public int findLeftmostOccurrence(EntityIdentity[] sortedEntities) {
			int idx = Arrays.binarySearch(sortedEntities, this);
			if (idx < 0)
				return idx;
			while (idx > 0 && compare(sortedEntities[idx-1],this) == 0) {
				--idx;
			}
			return idx;
		}

	}
	
	
	static final class OracleSelectionWorker extends DefaultPersistenceWorker<TEMPLATE, DefaultEntityPersister<TEMPLATE>> {
		
		public OracleSelectionWorker() {
			super(OperationType.SELECT);
		}

		
		static final String sql = 
				/*BEGINSQL_SELECTORACLE*/"SELECT \"WORKFLOWID\", \"ENTITYID\", \"NAME\", \"INTVALUE\" "+
										"FROM  \"TEMPLATE\", TABLE(:1) IDS "+
										"WHERE \"TEMPLATE\".\"WORKFLOWID\" = IDS.COLUMN_VALUE"/*ENDSQL_SELECTORACLE*/
				;
		
		@Override
		public void doExec(Connection connection, List<WorkflowAndEntity<TEMPLATE>> theWork) throws SQLException {
			
			if (theWork.isEmpty())
				return;
			
			Set<String> workflowIds = new HashSet<String>();
			EntityIdentity[] sortedEntities = new EntityIdentity[theWork.size()];
			int i = 0;
			for (WorkflowAndEntity<TEMPLATE> en : theWork) {
				workflowIds.add(en.workflow.getId());
				sortedEntities[i++] = new EntityIdentity(en);
			}
			Arrays.sort(sortedEntities);

			PreparedStatement stmt = connection.prepareStatement(sql);
			try {
				Connection nativeConnection = extractor.getNativeConnection(connection);
				ArrayDescriptor tableType =
			            ArrayDescriptor.createDescriptor("COP_VARCHAR128_ARRAY", nativeConnection);
				String[] parameters  = workflowIds.toArray(new String[workflowIds.size()]);
				stmt.setArray(1, new ARRAY(tableType, nativeConnection, parameters));
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					EntityIdentity identity = new EntityIdentity(rs.getString(1), rs.getString(2));
					int idx = identity.findLeftmostOccurrence(sortedEntities);
					if (idx < 0)
						continue;
					EntityIdentity match = sortedEntities[idx];
					entityLoop: for (;;) {
						if (match.entry != null) {
							TEMPLATE entity = match.entry.entity;
							/*BEGINSQL_SELECT_GETMEMBERS*/
							entity.setName(rs.getString(3));
							entity.setIntValue(rs.getInt(4));
							/*ENDSQL_SELECT_GETMEMBERS*/
							if (match.entry.callback != null) {
								match.entry.callback.entitySelected(entity);
								sortedEntities[idx] = identity;
							}
						}
						if (++idx < sortedEntities.length) {
							match = sortedEntities[idx];
							if (identity.compareTo(match) != 0)
								break entityLoop;
						} else {
							break entityLoop;
						}
					}
				}
				for (EntityIdentity match : sortedEntities) {
					if (match != null && match.entry != null) {
						match.entry.callback.entityNotFound(match.entry.entity);
					}
				}
				rs.close();
			} finally {
				stmt.close();
			}
		}
	}
	

	static final class CommonSelectionWorker extends DefaultPersistenceWorker<TEMPLATE, DefaultEntityPersister<TEMPLATE>> {
		
		public CommonSelectionWorker() {
			super(OperationType.SELECT);
		}

		
		@Override
		public void doExec(Connection connection, List<WorkflowAndEntity<TEMPLATE>> theWork) throws SQLException {
			
			if (theWork.isEmpty())
				return;
			
			final StringBuilder sql = new StringBuilder( 
					/*BEGINSQL_SELECTCOMMON*/"SELECT \"WORKFLOWID\", \"ENTITYID\", \"NAME\", \"INTVALUE\" "+
											"FROM  \"TEMPLATE\" "+
											"WHERE \"TEMPLATE\".\"WORKFLOWID\" IN (");/*ENDSQL_SELECTCOMMON*/

			Set<String> workflowIds = new HashSet<String>();
			EntityIdentity[] sortedEntities = new EntityIdentity[theWork.size()];
			int i = 0;
			for (WorkflowAndEntity<TEMPLATE> en : theWork) {
				workflowIds.add(en.workflow.getId());
				sortedEntities[i++] = new EntityIdentity(en);
			}
			Arrays.sort(sortedEntities);

			for (@SuppressWarnings("unused") String en : workflowIds) {
				sql.append("?,");
			}
			sql.setLength(sql.length()-1);
			sql.append(")");

			PreparedStatement stmt = connection.prepareStatement(sql.toString());
			try {
				int pIdx = 1;
				for (String parameter : workflowIds.toArray(new String[workflowIds.size()])) {
					stmt.setString(pIdx++, parameter);
				}
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					EntityIdentity identity = new EntityIdentity(rs.getString(1), rs.getString(2));
					int idx = identity.findLeftmostOccurrence(sortedEntities);
					if (idx < 0)
						continue;
					EntityIdentity match = sortedEntities[idx];
					entityLoop: for (;;) {
						if (match.entry != null) {
							TEMPLATE entity = match.entry.entity;
							/*BEGINSQL_SELECT_GETMEMBERS*/
							entity.setName(rs.getString(3));
							entity.setIntValue(rs.getInt(4));
							/*ENDSQL_SELECT_GETMEMBERS*/
							if (match.entry.callback != null) {
								match.entry.callback.entitySelected(entity);
								sortedEntities[idx] = identity;
							}
						}
						if (++idx < sortedEntities.length) {
							match = sortedEntities[idx];
							if (identity.compareTo(match) != 0)
								break entityLoop;
						} else {
							break entityLoop;
						}
					}
				}
				for (EntityIdentity match : sortedEntities) {
					if (match != null && match.entry != null) {
						match.entry.callback.entityNotFound(match.entry.entity);
					}
				}
				rs.close();
			} finally {
				stmt.close();
			}
		}
	}

	static final class InsertionWorker  extends DefaultPersistenceWorker<TEMPLATE, DefaultEntityPersister<TEMPLATE>> {
		
		public InsertionWorker() {
			super(OperationType.INSERT);
		}

		
		static final String sql = 
				/*BEGINSQL_INSERT*/"INSERT INTO \"TEMPLATE\" (\"WORKFLOWID\", \"ENTITYID\", \"NAME\", \"INTVALUE\") VALUES (:1, :2, :3, :4)"/*ENDSQL_INSERT*/
				;

		@Override
		public void doExec(Connection connection, List<WorkflowAndEntity<TEMPLATE>> theWork) throws SQLException {
			
			if (theWork.isEmpty())
				return;
			
			final PreparedStatement stmt = connection.prepareStatement(sql);
			try {
				for (WorkflowAndEntity<TEMPLATE> en : theWork) {
					TEMPLATE entity = en.entity;
					stmt.setString(1, en.workflow.getId());
					stmt.setString(2, entity.getEntityId());
					/*BEGINSQL_INSERT_SETMEMBERS*/
					stmt.setString(3, entity.getName());
					stmt.setInt(4, entity.getIntValue());
					/*ENDSQL_INSERT_SETMEMBERS*/
					stmt.addBatch();
				}
				stmt.executeBatch();
			} finally {
				stmt.close();
			}
		}
	}

	static final class UpdateWorker extends DefaultPersistenceWorker<TEMPLATE, DefaultEntityPersister<TEMPLATE>> {
		
		public UpdateWorker() {
			super(OperationType.UPDATE);
		}

		
		
		static final String sql = 
				/*BEGINSQL_UPDATE*/ "UPDATE \"TEMPLATE\" SET \"NAME\" = :1, \"INTVALUE\" = :2 WHERE \"WORKFLOWID\" = :3 AND \"ENTITYID\" = :4"/*ENDSQL_UPDATE*/
				;

		public void doExec(Connection connection, List<WorkflowAndEntity<TEMPLATE>> theWork) throws SQLException {
			
			if (theWork.isEmpty())
				return;
			
			final PreparedStatement stmt = connection.prepareStatement(sql);
			try {
				for (WorkflowAndEntity<TEMPLATE> en : theWork) {
					TEMPLATE entity = en.entity;
					/*BEGINSQL_UPDATE_SETMEMBERS*/
					stmt.setString(1, entity.getName());
					stmt.setInt(2, entity.getIntValue());
					stmt.setString(3, en.workflow.getId());
					stmt.setString(4, entity.getEntityId());
					/*ENDSQL_UPDATE_SETMEMBERS*/
					stmt.addBatch();
				}
				stmt.executeBatch();
			} finally {
				if (stmt != null)
					stmt.close();
			}
		}
	}

	static final class DeletionWorker extends DefaultPersistenceWorker<TEMPLATE, DefaultEntityPersister<TEMPLATE>> {
		
		public DeletionWorker() {
			super(OperationType.DELETE);
		}
		
		
		static final String sql = 
				/*BEGINSQL_DELETE*/"DELETE FROM  \"TEMPLATE\" WHERE \"WORKFLOWID\" = :1 AND \"ENTITYID\" = :2"/*ENDSQL_DELETE*/
				;

		public void doExec(Connection connection, List<WorkflowAndEntity<TEMPLATE>> theWork) throws SQLException {
			
			if (theWork.isEmpty())
				return;
			
			final PreparedStatement stmt = connection.prepareStatement(sql);
			try {
				for (WorkflowAndEntity<TEMPLATE> en : theWork) {
					stmt.setString(1, en.workflow.getId());
					stmt.setString(2, en.entity.getEntityId());
					stmt.addBatch();
				}
				stmt.executeBatch();
			} finally {
				stmt.close();
			}
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	public Collection<Class<?>> getEntityClassesDependingOn() {
		return Collections.EMPTY_LIST;
	}
	
	public static class Oracle extends TEMPLATEPersisterFactory {

		@Override
		public DefaultPersisterSharedRessources<TEMPLATE, DefaultEntityPersister<TEMPLATE>> createSharedRessources() {
			return new DefaultPersisterSimpleCRUDSharedRessources<TEMPLATE, DefaultEntityPersister<TEMPLATE>>(
					new OracleSelectionWorker(),
					new InsertionWorker(),
					new UpdateWorker(),
					new DeletionWorker()
					);
		}

	}
	
	public static class Common extends TEMPLATEPersisterFactory {

		@Override
		public DefaultPersisterSharedRessources<TEMPLATE, DefaultEntityPersister<TEMPLATE>> createSharedRessources() {
			return new DefaultPersisterSimpleCRUDSharedRessources<TEMPLATE, DefaultEntityPersister<TEMPLATE>>(
					new CommonSelectionWorker(),
					new InsertionWorker(),
					new UpdateWorker(),
					new DeletionWorker()
					);
		}

	}




}

