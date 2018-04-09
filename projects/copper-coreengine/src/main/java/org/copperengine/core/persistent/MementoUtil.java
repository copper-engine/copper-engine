/*
 * Copyright 2002-2015 SCOOP Software GmbH
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
package org.copperengine.core.persistent;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.copperengine.core.CopperRuntimeException;
import org.copperengine.core.Workflow;
import org.copperengine.core.persistent.EntityPersister.PostSelectedCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to help find the delta to persist upon saving of a persistent workflow.
 * This class will work out-of-the-box for instances assignable to PersistenEntity. If user classes shall be handled,
 * override the methods {@link #equals(Object, Object)}, {@link #identifier(Object)} and {@link #clone(Object)}.
 *
 * @author Roland Scheel
 */
public class MementoUtil {

    private static final Logger logger = LoggerFactory.getLogger(MementoUtil.class);

    ArrayList<Object> inserted = new ArrayList<Object>();
    HashMap<Object, Object[]> memento = new HashMap<Object, Object[]>();
    HashMap<Object, Object> potentiallyChanged = new HashMap<Object, Object>();

    /**
     * Remember this entity after loading
     *
     * @param entity
     *        The entity to be remembered
     */
    public void addMementoEntity(Object entity) {
        memento.put(identifier(entity), new Object[] { entity });
    }

    /**
     * Use this entity as the new
     *
     * @param entity
     *        the entity to be used as the new
     */
    public void addCurrentEntity(Object entity) {
        Object identifier = identifier(entity);
        Object o = memento.get(identifier);
        if (o == null) {
            inserted.add(entity);
        } else {
            potentiallyChanged.put(identifier, entity);
        }
    }

    public void autoLoad(Workflow<?> wf, PersistenceContext pc) {
        autoIteratePersistentMembers(wf, new IteratorCallback() {
            @Override
            public void operateOn(String memberName, Object o) {
                addMementoEntity(o);
            }
        });
        load(pc);
    }

    public void autoStore(Workflow<?> wf, PersistenceContext pc) {
        autoIteratePersistentMembers(wf, new IteratorCallback() {
            @Override
            public void operateOn(String memberName, Object o) {
                ensureId(memberName, o);
                addCurrentEntity(o);
            }
        });
        store(pc);
    }

    public void load(PersistenceContext pc) {
        final Iterator<Entry<Object, Object[]>> i = memento.entrySet().iterator();
        while (i.hasNext()) {
            Entry<Object, Object[]> en = i.next();
            final Object[] mementoSlot = en.getValue();
            Object mementoObject = mementoSlot[0];
            ((EntityPersister)pc.getPersister(mementoObject.getClass())).select(mementoObject, new PostSelectedCallback<Object>() {
                @Override
                public void entitySelected(Object e) {
                    mementoSlot[0] = MementoUtil.this.clone(e);
                }

                @Override
                public void entityNotFound(Object e) {
                    logger.warn("Entity could not be loaded: Not found: " + identifier(e));
                    i.remove();
                }
            });
        }
    }

    public void store(PersistenceContext pc) {
        try {
            for (Map.Entry<Object, Object[]> mementoEntry : memento.entrySet()) {
                Object currentObject = potentiallyChanged.get(mementoEntry.getKey());
                Object mementoObject = mementoEntry.getValue()[0];
                if (currentObject == null) {
                    ((EntityPersister)pc.getPersister(mementoObject.getClass())).delete(mementoObject);
                } else {
                    if (!equals(currentObject, mementoObject)) {
                        ((EntityPersister)pc.getPersister(mementoObject.getClass())).update(currentObject);
                    }
                }
            }
            for (Object inserted : this.inserted) {
                ensureId(null, inserted);
                ((EntityPersister)pc.getPersister(inserted.getClass())).insert(inserted);
            }
        } finally {
            inserted.clear();
            potentiallyChanged.clear();
        }
    }

    static interface IteratorCallback {
        void operateOn(String memberName, Object o);
    }

    protected void autoIteratePersistentMembers(Workflow<?> wf, IteratorCallback callback) {
        for (PersistentMemberAccessor accessor : genericCreateAccessors(wf.getClass())) {
            Object o = accessor.get(wf);
            if (o == null)
                continue;
            Class<?> clazz = o.getClass();
            Iterator<?> it = null;
            if (clazz.isArray()) {
                it = Arrays.asList((Object[]) o).iterator();
            } else if (Iterable.class.isAssignableFrom(clazz)) {
                it = ((Iterable<?>) o).iterator();
            } else if (Map.class.isAssignableFrom(clazz)) {
                it = ((Map<?, ?>) o).values().iterator();
            } else {
                it = Arrays.asList(o).iterator();
            }
            while (it.hasNext()) {
                Object next = it.next();
                if (next == null)
                    continue;
                callback.operateOn(accessor.memberName(), next);
            }
        }
    }

    static class PersistentEntityId {
        final PersistentEntity pe;

        public PersistentEntityId(PersistentEntity pe) {
            this.pe = pe;
        }

        public int hashCode() {
            return (pe.getEntityId() == null ? 0 : pe.getEntityId().hashCode()) ^ pe.getClass().hashCode();
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof PersistentEntityId))
                return false;
            PersistentEntity other = ((PersistentEntityId) obj).pe;
            if (pe == other)
                return true;
            if (other == null || other.getClass() != pe.getClass())
                return false;
            if (pe.getEntityId() == null) {
                return false;
            }
            return pe.getEntityId().equals(other.getEntityId());
        }

        @Override
        public String toString() {
            return pe.getClass().getCanonicalName() + "#" + pe.getEntityId();
        }
    }

    protected Object identifier(final Object entity) {
        if (entity instanceof PersistentEntity) {
            final PersistentEntity pe = (PersistentEntity) entity;
            return new PersistentEntityId(pe);
        }
        return new Object() {
            public int hashCode() {
                return System.identityHashCode(entity);
            }

            public boolean equals(Object obj) {
                return entity == obj;
            }
        };
    }

    @SuppressWarnings("unchecked")
    protected <T> T clone(T obj) {
        if (obj instanceof PersistentEntity)
            return (T) ((PersistentEntity) obj).clone();
        throw new CopperRuntimeException("No clone method override for " + obj.getClass());
    }

    protected boolean equals(Object currentObject, Object mementoObject) {
        return currentObject.equals(mementoObject);
    }

    protected boolean ensureId(String hint, Object entity) {
        if (entity instanceof PersistentEntity) {
            PersistentEntity pe = (PersistentEntity) entity;
            if (pe.getEntityId() == null) {
                pe.setEntityId(hint != null ? hint + '#' + UUID.randomUUID().toString() : UUID.randomUUID().toString());
                return true;
            }
        }
        return false;
    }

    interface PersistentMemberAccessor {
        String memberName();

        Object get(Object obj);
    }

    static Map<Class<?>, Collection<PersistentMemberAccessor>> accessorCache = Collections.synchronizedMap(new HashMap<Class<?>, Collection<PersistentMemberAccessor>>());

    protected Collection<PersistentMemberAccessor> genericCreateAccessors(Class<?> clazz) {
        Collection<PersistentMemberAccessor> ret = accessorCache.get(clazz);
        if (ret != null)
            return ret;

        ret = new ArrayList<PersistentMemberAccessor>();
        Class<?> classIter = clazz;

        while (classIter != Object.class) {
            for (Field f : classIter.getDeclaredFields()) {
                if (!isPersistableMember(f))
                    continue;
                final Field pf = f;
                pf.setAccessible(true);
                ret.add(new PersistentMemberAccessor() {
                    @Override
                    public String memberName() {
                        return pf.getName();
                    }

                    @Override
                    public Object get(Object obj) {
                        try {
                            return pf.get(obj);
                        } catch (Exception e) {
                            throw new CopperRuntimeException(e);
                        }
                    }

                });
            }
            classIter = classIter.getSuperclass();
        }
        accessorCache.put(clazz, ret);
        return ret;
    }

    protected boolean isPersistableMember(Field f) {
        if (Modifier.isStatic(f.getModifiers()))
            return false;
        return f.getAnnotation(PersistentMember.class) != null;
    }

}
