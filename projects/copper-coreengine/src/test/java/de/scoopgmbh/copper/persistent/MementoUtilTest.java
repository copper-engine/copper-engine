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
package de.scoopgmbh.copper.persistent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import de.scoopgmbh.copper.InterruptException;
import de.scoopgmbh.copper.instrument.Transformed;

public class MementoUtilTest {
	
	static class SomeMemberClass extends PersistentEntity {
		private static final long serialVersionUID = 1L;
		
		public SomeMemberClass(String data, String id) {
			this.data = data;
			setEntityId(id);
		}
		
		String data;
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj.getClass() != SomeMemberClass.class)
				return false;
			SomeMemberClass other = (SomeMemberClass)obj;
			if (getEntityId() == null) {
				return other.getEntityId() == null; 
			}
			if (!getEntityId().equals(other.getEntityId()))
				return false;
			if (data == null) {
				return other.data == null; 
			}
			if (!data.equals(other.data))
				return false;
			return true;
		}
		
		public String toString() {
			return data;
		}
	}


	@Transformed
	static class TestWorkflow extends PersistentWorkflow<Serializable> {
		private static final long serialVersionUID = 1L;
		@PersistentMember
		transient SomeMemberClass   simpleDeletedMember;
		@PersistentMember
		transient SomeMemberClass   simpleUnchangedMember;
		@PersistentMember
		transient SomeMemberClass   simpleChangedMember;
		@PersistentMember
		transient SomeMemberClass   simpleInsertedMember;
		@PersistentMember
		transient SomeMemberClass[] arrayMember;
		@PersistentMember
		List<SomeMemberClass> listMember;
		@PersistentMember
		Map<String,SomeMemberClass> mapMember;
		@Override

		public void main() throws InterruptException {
		}
		
		
	}
	
	@Test
	public void testMementoUtil() {
		TestWorkflow wf = new TestWorkflow();
		int id = 0;
		wf.simpleDeletedMember = new SomeMemberClass("deleted",""+(++id));
		wf.simpleUnchangedMember = new SomeMemberClass("unchanged",""+(++id));
		wf.simpleChangedMember = new SomeMemberClass("changed",""+(++id));
		wf.arrayMember = new SomeMemberClass[4];
		wf.arrayMember[0] = new SomeMemberClass("deletedArray",""+(++id));
		wf.arrayMember[1] = new SomeMemberClass("unchangedArray",""+(++id));
		wf.arrayMember[2] = new SomeMemberClass("changedArray",""+(++id));
		wf.listMember = new ArrayList<SomeMemberClass>();
		wf.listMember.add(new SomeMemberClass("deletedList",""+(++id)));
		wf.listMember.add(new SomeMemberClass("unchangedList",""+(++id)));
		wf.listMember.add(new SomeMemberClass("changedList",""+(++id)));
		wf.mapMember = new HashMap<String,SomeMemberClass>();
		wf.mapMember.put("0",new SomeMemberClass("deletedMap",""+(++id)));
		wf.mapMember.put("1",new SomeMemberClass("unchangedMap",""+(++id)));
		wf.mapMember.put("2",new SomeMemberClass("changedMap",""+(++id)));
		MementoUtil util = new MementoUtil();
		final int[] selected = new int[]{0};
		PersistenceContext loadContext = new PersistenceContext() {
			@Override
			public <T> EntityPersister<T> getPersister(
					Class<? extends T> entityClass) {
				return new EntityPersister<T>(){
					@Override
					public void select(
							T e,
							de.scoopgmbh.copper.persistent.EntityPersister.PostSelectedCallback<T> callback) {
						callback.entitySelected(e);
						selected[0]++;
					}
					@Override
					public void insert(T e) {
						Assert.fail("Only selects allowed");
					}
					@Override
					public void update(T e) {
						Assert.fail("Only selects allowed");
					}
					@Override
					public void delete(T e) {
						Assert.fail("Only selects allowed");
					}};
			}
			@Override
			public <T> T getMapper(Class<T> mapperInterface) {
				return null;
			}
			
		};
		util.autoLoad(wf, loadContext);
		Assert.assertEquals(12, selected[0]);
		wf.simpleChangedMember.data = "changed#";
		wf.simpleDeletedMember = null;
		wf.simpleInsertedMember = new SomeMemberClass("inserted#",null);
		wf.arrayMember[0] = null;
		wf.arrayMember[2].data = "changed#";
		wf.arrayMember[3] = new SomeMemberClass("insertedArray#",null);
		wf.listMember.set(0, null);
		wf.listMember.get(2).data = "changedList#";
		wf.listMember.add(new SomeMemberClass("insertedList#",null));
		wf.mapMember.remove("0");
		wf.mapMember.get("2").data = "changedMap#";
		wf.mapMember.put("3", new SomeMemberClass("insertedMap#",null));
		final int[] inserted = new int[]{0};
		final int[] changed = new int[]{0};
		final int[] deleted = new int[]{0};
		PersistenceContext saveContext = new PersistenceContext() {
			@Override
			public <T> EntityPersister<T> getPersister(
					Class<? extends T> entityClass) {
				return new EntityPersister<T>(){
					@Override
					public void select(
							T e,
							de.scoopgmbh.copper.persistent.EntityPersister.PostSelectedCallback<T> callback) {
						Assert.fail("no selects allowed");
					}
					@Override
					public void insert(T e) {
						Assert.assertTrue(((SomeMemberClass)e).data.startsWith("inserted"));
						Assert.assertNotNull(((SomeMemberClass)e).getEntityId());
						inserted[0]++;
					}
					@Override
					public void update(T e) {
						Assert.assertTrue(((SomeMemberClass)e).data.startsWith("changed"));
						changed[0]++;
					}
					@Override
					public void delete(T e) {
						Assert.assertTrue(((SomeMemberClass)e).data.startsWith("deleted"));
						deleted[0]++;
					}};
			}
			@Override
			public <T> T getMapper(Class<T> mapperInterface) {
				return null;
			}
			
		};
		util.autoStore(wf, saveContext);
		Assert.assertEquals(4, inserted[0]);
		Assert.assertEquals(4, changed[0]);
		Assert.assertEquals(4, deleted[0]);
		//Must be idempotent
		inserted[0] = 0;
		changed[0] = 0;
		deleted[0] = 0;
		util.autoStore(wf, saveContext);
		Assert.assertEquals(4, inserted[0]);
		Assert.assertEquals(4, changed[0]);
		Assert.assertEquals(4, deleted[0]);
	}

}
