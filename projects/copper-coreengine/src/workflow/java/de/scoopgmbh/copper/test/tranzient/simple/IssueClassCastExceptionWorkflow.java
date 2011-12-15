package de.scoopgmbh.copper.test.tranzient.simple;

import java.io.Serializable;

import org.apache.log4j.Logger;

import de.scoopgmbh.copper.InterruptException;

public class IssueClassCastExceptionWorkflow extends AbstractIssueClassCastExceptionWorkflow {
	
	private static final Logger logger = Logger.getLogger(IssueClassCastExceptionWorkflow.class);
	
	@Override
	protected void callAbstractExceptionSimulation0(String partnerLink) {
		throw new RuntimeException("Simulate exception.");
	}

	@Override
	protected void callAbstractExceptionSimulation1() throws InterruptException {
		throw new RuntimeException("Simulate exception.");
	}

	@Override
	protected void callAbstractExceptionSimulation2(String partnerLink) {
		throw new RuntimeException("Simulate exception.");
	}

	@Override
	public void main() throws InterruptException {
		this.callPartner(100);
		getData().error = false;
		getData().done = true;
	}
}
