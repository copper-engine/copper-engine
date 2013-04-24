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
package de.scoopgmbh.copper.monitoring.client.form.filter;

import java.net.MalformedURLException;
import java.net.URL;

public class GenericFilterController<T> implements FilterController<T>{
	
	public static final URL EMPTY_DUMMY_URL;
	static{
		try {
			EMPTY_DUMMY_URL = new URL("http://a");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private final T filter;
	public GenericFilterController(T filter) {
		super();
		this.filter = filter;
	}
	
	public GenericFilterController() {
		super();
		this.filter = null;
	}

	@Override
	public URL getFxmlRessource() {
		return EMPTY_DUMMY_URL;
	}

	@Override
	public T getFilter() {
		return filter;
	}

	@Override
	public boolean supportsFiltering() {
		return filter!=null;
	}

}
