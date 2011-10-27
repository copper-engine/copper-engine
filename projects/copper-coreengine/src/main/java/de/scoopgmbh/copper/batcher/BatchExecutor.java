/*
 * Copyright 2002-2011 SCOOP Software GmbH
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
package de.scoopgmbh.copper.batcher;

import java.util.Collection;

/**
 * Abstract base implementation of the {@link BatchExecutorBase} interface.
 * 
 * @author austermann
 *
 * @param <E>
 * @param <T>
 */
public abstract class BatchExecutor<E extends BatchExecutor<E,T>, T extends BatchCommand<E,T>> implements BatchExecutorBase<T> {

	private final String id = this.getClass().getName();

	protected abstract void executeCommands(Collection<T> commands);
	@SuppressWarnings("unchecked")
	public final void execute(Collection<?> commands) {
		try {
			executeCommands((Collection<T>)commands);
		} catch (Exception e) {
			for (T cmd : (Collection<T>) commands) {
				cmd.callback().unhandledException(e);
			}
		}
	}
	public boolean prioritize() {
		return false;
	}
	
	@Override
	public String id() {
		return id;
	}

}
