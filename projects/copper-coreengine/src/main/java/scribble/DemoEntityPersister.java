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
package scribble;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;

import org.springframework.jdbc.support.nativejdbc.C3P0NativeJdbcExtractor;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;

import de.scoopgmbh.copper.persistent.DefaultEntityPersister;
import de.scoopgmbh.copper.persistent.DefaultPersistenceWorker;

public class DemoEntityPersister extends DefaultEntityPersister<DemoEntity> {
	
	static final NativeJdbcExtractor extractor = new C3P0NativeJdbcExtractor();

	public static final SelectionWorker SELECTION_WORKER = new SelectionWorker();
	public static final InsertionWorker INSERTION_WORKER = new InsertionWorker();
	public static final UpdateWorker    UPDATE_WORKER    = new UpdateWorker();
	public static final DeletionWorker  DELETION_WORKER  = new DeletionWorker();

	public DemoEntityPersister(String workflowId) {
		super(workflowId);
	}

	public boolean acceptsWorker(@SuppressWarnings("rawtypes") DefaultPersistenceWorker worker) {
		if (worker == SELECTION_WORKER) {
			return !selects().isEmpty();
		}
		if (worker == INSERTION_WORKER) {
			return !inserts().isEmpty();
		}
		if (worker == UPDATE_WORKER) {
			return !updates().isEmpty();
		}
		if (worker == DELETION_WORKER) {
			return !deletes().isEmpty();
		}
		return false;
	}

	static final class SelectionWorker implements DefaultPersistenceWorker<DemoEntity, DemoEntityPersister> {
		
		static final String sql = "SELECT \"WORKFLOWID\", \"ENTITYID\", \"NAME\", \"INTVALUE\" "+
										"FROM  \"DEMOENTITY\", TABLE(:1) IDS "+
										"WHERE \"DEMOENTITY\".\"WORKFLOWID\" = IDS.COLUMN_VALUE";
		
		public void doExec(
				List<DemoEntityPersister> persisters,
				Connection connection) throws SQLException {
			
			Map<String, DemoEntityPersister> persisterMap = new HashMap<String, DemoEntityPersister>();
			for (DemoEntityPersister persister : persisters) {
				if (persister.selects() != null) {
					persisterMap.put(persister.workflowId(), persister);
				}
			}
			
			if (persisterMap.isEmpty())
				return;

			PreparedStatement stmt = connection.prepareStatement(sql);
			try {
				Connection nativeConnection = extractor.getNativeConnection(connection);
				ArrayDescriptor tableType =
			            ArrayDescriptor.createDescriptor("VARCHAR_TABLE", nativeConnection);
				String[] parameters  = persisterMap.keySet().toArray(new String[persisterMap.size()]);
				stmt.setArray(1, new ARRAY(tableType, nativeConnection, parameters));
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					DemoEntityPersister persister = persisterMap.get(rs.getString(1));
					if (persister == null)
						continue;
					List<EntityAndCallback<DemoEntity>> listOfEntityAndCallback = persister.selects().get(rs.getInt(2));
					if (listOfEntityAndCallback == null)
						continue;
					for (EntityAndCallback<DemoEntity> entityAndCallback : listOfEntityAndCallback) {
						DemoEntity entity = entityAndCallback.entity;
						entity.setName(rs.getString(3));
						entity.setIntValue(rs.getInt(4));
						entityAndCallback.callback.entitySelected(entity);
					}
					persister.selects().remove(rs.getInt(2));
				}
				rs.close();
				for (DemoEntityPersister persister : persisters) {
					Map<Integer, List<EntityAndCallback<DemoEntity>>> entities = persister.selects();
					for (List<EntityAndCallback<DemoEntity>> listOfEntityAndCallback : entities.values()) {
						for (EntityAndCallback<DemoEntity> entityAndCallback : listOfEntityAndCallback) {
							entityAndCallback.callback.entityNotFound(entityAndCallback.entity);
						}
					}
					persister.selects().clear();
				}
			} finally {
				stmt.close();
			}
		}
	}

	static final class InsertionWorker implements DefaultPersistenceWorker<DemoEntity, DemoEntityPersister> {
		
		static final String sql = "INSERT INTO \"DEMOENTITY\" (\"WORKFLOWID\", \"ENTITYID\", \"NAME\", \"INTVALUE\") VALUES (:1, :2, :3, :4)";

		public void doExec(
				List<DemoEntityPersister> persisters,
				Connection connection) throws SQLException {
			
			final PreparedStatement stmt = connection.prepareStatement(sql);
			try {
				for (DemoEntityPersister persister : persisters) {
					for (DemoEntity entity : persister.inserts()) {
						stmt.setString(1, persister.workflowId());
						stmt.setInt(2, entity.getEntityId());
						stmt.setString(3, entity.getName());
						stmt.setInt(4, entity.getIntValue());
						stmt.addBatch();
					}
				}
				stmt.executeBatch();
			} finally {
				stmt.close();
			}
		}
	}

	static final class UpdateWorker implements DefaultPersistenceWorker<DemoEntity, DemoEntityPersister> {
		
		static final String sql = "UPDATE \"DEMOENTITY\" SET \"NAME\" = :1, \"INTVALUE\" = :2 WHERE \"WORKFLOWID\" = :3 AND \"ENTITYID\" = :4";

		public void doExec(
				List<DemoEntityPersister> persisters,
				Connection connection) throws SQLException {
			
			final PreparedStatement stmt = connection.prepareStatement(sql);
			try {
				for (DemoEntityPersister persister : persisters) {
					for (DemoEntity entity : persister.updates()) {
						stmt.setString(1, persister.workflowId());
						stmt.setInt(2, entity.getEntityId());
						stmt.setString(3, entity.getName());
						stmt.setInt(4, entity.getIntValue());
						stmt.addBatch();
					}
				}
				stmt.executeBatch();
			} finally {
				if (stmt != null)
					stmt.close();
			}
		}
	}

	static final class DeletionWorker implements DefaultPersistenceWorker<DemoEntity, DemoEntityPersister> {
		
		static final String sql = "DELETE FROM  \"DEMOENTITY\" WHERE \"WORKFLOWID\" = :1 AND \"ENTITYID\" = :2";

		public void doExec(
				List<DemoEntityPersister> persisters,
				Connection connection) throws SQLException {
			
			final PreparedStatement stmt = connection.prepareStatement(sql);
			try {
				for (DemoEntityPersister persister : persisters) {
					for (DemoEntity entity : persister.deletes()) {
						stmt.setString(1, persister.workflowId());
						stmt.setInt(2, entity.getEntityId());
						stmt.addBatch();
					}
				}
				stmt.executeBatch();
			} finally {
				stmt.close();
			}
		}
	}


}
