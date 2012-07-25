package de.scoopgmbh.copper.test;

import java.io.Serializable;

import org.apache.log4j.Logger;

import de.scoopgmbh.copper.InterruptException;
import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.WaitMode;
import de.scoopgmbh.copper.persistent.PersistentWorkflow;

public class IssueClassCastExceptionWorkflow3<Data extends Serializable> extends PersistentWorkflow<Serializable> {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(IssueClassCastExceptionWorkflow3.class);

	private int retriesLeft = 5;

	@Override
	public void main() throws InterruptException {
		this.callPartner(20);

	}

	protected void exceptionSimulation() throws InterruptException {
		wait(WaitMode.FIRST, 20, "x");
		throw new RuntimeException("Simulate exception.");
	}

	protected void callPartner(int theWaitInterval) throws InterruptException {
		logger.warn("Start " + this.getClass().getName());
		boolean retryInterrupted = false;
		while (!retryInterrupted && retriesLeft > 0) {
			retryInterrupted = withCatch(theWaitInterval);
		}
	}

	private boolean withCatch(int theWaitInterval) throws InterruptException {
		try {
			exceptionSimulation();
			return false;
		} catch (Exception e) {
			logger.warn("Handle exception: " + e);
			return waitForNetRetry(theWaitInterval);
		}
	}

	private boolean waitForNetRetry(int theWaitInterval) throws InterruptException {
		boolean interupted = false;
		if (retriesLeft > 0) {
			retriesLeft--;
			String correlationID = "RETRY-" + this.getEngine().createUUID();
			wait(WaitMode.FIRST, theWaitInterval, correlationID);
			Response<String> r = getAndRemoveResponse(correlationID);
			if (logger.isInfoEnabled())
				logger.info("Response for " + correlationID + ": " + r);
			if (!r.isTimeout()) {
				if (logger.isInfoEnabled())
					logger.info("Receiver no TIMEOUT while retring, so must be INTERRUPT_RETRY.");
				interupted = true;
			}
		}
		return interupted;
	}
}