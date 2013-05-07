package de.scoopgmbh.copper.monitoring.server.wrapper;

import java.util.List;

import de.scoopgmbh.copper.CopperException;
import de.scoopgmbh.copper.CopperRuntimeException;
import de.scoopgmbh.copper.EngineState;
import de.scoopgmbh.copper.ProcessingEngine;
import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.WaitHook;
import de.scoopgmbh.copper.WaitMode;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.WorkflowInstanceDescr;
import de.scoopgmbh.copper.monitoring.server.monitoring.MonitoringDataCollector;

/**
 * Add Monitoring for ProcessingEngine for a specific adapter
 * @author hbrackmann
 *
 */
public class MonitoringAdapterProcessingEngine implements ProcessingEngine{
	
	private final ProcessingEngine processingEngine;
	private final MonitoringDataCollector monitoringDataCollector;
	private final Object adapter;
	
	public MonitoringAdapterProcessingEngine(Object adapter,ProcessingEngine processingEngine, MonitoringDataCollector monitoringDataCollector) {
		super();
		this.adapter = adapter;
		this.processingEngine = processingEngine;
		this.monitoringDataCollector = monitoringDataCollector;
	}

	@Override
	public void startup() throws CopperRuntimeException {
		processingEngine.startup();
	}

	@Override
	public void shutdown() throws CopperRuntimeException {
		processingEngine.shutdown();
	}

	@Override
	public void addShutdownObserver(Runnable observer) {
		processingEngine.addShutdownObserver(observer);
	}

	@Override
	public void registerCallbacks(Workflow<?> w, WaitMode mode, long timeoutMsec, String... correlationIds) throws CopperRuntimeException {
		processingEngine.registerCallbacks(w, mode, timeoutMsec, correlationIds);
	}

	@Override
	public void notify(Response<?> response) throws CopperRuntimeException {
		monitoringDataCollector.submitAdapterWfNotify(response.getCorrelationId(), response.getResponse(),adapter);
		processingEngine.notify(response);
	}

	@Override
	public String createUUID() {
		return processingEngine.createUUID();
	}

	@Override
	public void run(String wfname, Object data) throws CopperException {
		monitoringDataCollector.submitAdapterWfLaunch(wfname,adapter);
		processingEngine.run(wfname,data);
	}

	@Override
	public void run(WorkflowInstanceDescr<?> wfInstanceDescr) throws CopperException {
		monitoringDataCollector.submitAdapterWfLaunch(wfInstanceDescr.getWfName(),adapter);
		processingEngine.run(wfInstanceDescr);
	}

	@Override
	public void runBatch(List<WorkflowInstanceDescr<?>> wfInstanceDescr) throws CopperException {
		for (WorkflowInstanceDescr<?> wfInstanceDesc: wfInstanceDescr){
			monitoringDataCollector.submitAdapterWfLaunch(wfInstanceDesc.getWfName(),adapter);
		}
		processingEngine.runBatch(wfInstanceDescr);
	}

	@Override
	public EngineState getEngineState() {
		return processingEngine.getEngineState();
	}

	@Override
	public String getEngineId() {
		return processingEngine.getEngineId();
	}

	@Override
	public void addWaitHook(String wfInstanceId, WaitHook waitHook) {
		processingEngine.addWaitHook(wfInstanceId, waitHook);
	}

	

}
