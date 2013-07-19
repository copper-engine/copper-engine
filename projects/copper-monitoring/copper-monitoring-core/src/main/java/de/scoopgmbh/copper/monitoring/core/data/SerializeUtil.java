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
package de.scoopgmbh.copper.monitoring.core.data;

import java.util.ArrayList;
import java.util.Date;

import com.esotericsoftware.kryo.Kryo;

import de.scoopgmbh.copper.monitoring.core.model.AdapterCallInfo;
import de.scoopgmbh.copper.monitoring.core.model.AdapterWfLaunchInfo;
import de.scoopgmbh.copper.monitoring.core.model.AdapterWfNotifyInfo;
import de.scoopgmbh.copper.monitoring.core.model.LogEvent;
import de.scoopgmbh.copper.monitoring.core.model.MeasurePointData;
import de.scoopgmbh.copper.monitoring.core.model.SystemResourcesInfo;

public class SerializeUtil{
	
	private static Kryo kryo;

	/**
	 * We use Kryo instead of java serialization to create small serialized output
	 * 
	 * reading and write need the same order
	 * its not necessary to register all used classes it just saves same space
	 * @return
	 */
	public static Kryo getKryo(){
		if (kryo==null){
			kryo = new Kryo();
			kryo.register(Date.class);
			kryo.register(ArrayList.class);
			kryo.register(AdapterCallInfo.class);
			kryo.register(AdapterWfNotifyInfo.class);
			kryo.register(AdapterWfLaunchInfo.class);
			kryo.register(MeasurePointData.class);
			kryo.register(LogEvent.class);
			kryo.register(SystemResourcesInfo.class);
		}
		return kryo;
	}

}
