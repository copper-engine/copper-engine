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
package de.scoopgmbh.copper.test;

import org.junit.Assert;

import de.scoopgmbh.copper.InterruptException;
import de.scoopgmbh.copper.Workflow;

public class SwitchCaseTestWF extends Workflow<SwitchCaseTestData> {

	private static final long serialVersionUID = 1L;

	@Override
	public void main() throws InterruptException {
		try {
			doSwitch();
			getData().asyncResponseReceiver.setResponse(0);
		}
		catch(Exception e) {
			getData().asyncResponseReceiver.setException(e);
		}
		catch(Error e) {
			getData().asyncResponseReceiver.setException(new RuntimeException(e));
		}
	}

	private void doSwitch() {
		switch (getData().testEnumValue) {
		case A:
			Assert.fail();
			break;
		case B:
			Assert.fail();
			break;
		case C:
			break;
		case D:
			Assert.fail();
			break;
		default:
			Assert.fail();
			break;
		}
	}

}
