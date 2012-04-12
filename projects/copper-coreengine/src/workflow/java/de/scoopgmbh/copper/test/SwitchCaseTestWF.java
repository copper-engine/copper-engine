package de.scoopgmbh.copper.test;

import junit.framework.Assert;
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
