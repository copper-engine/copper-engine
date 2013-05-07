package de.scoopgmbh.copper.monitoring.core.model;

import java.io.Serializable;
import java.util.List;

public class AdapterHistoryInfo implements Serializable{
	private static final long serialVersionUID = 5463189504944441358L;
	
	List<AdapterCallInfo> adapterCalls;
	List<AdapterWfLaunchInfo> adapterWfLaunches;
	List<AdapterWfNotifyInfo> adapterWfNotifies;
	
	public List<AdapterCallInfo> getAdapterCalls() {
		return adapterCalls;
	}

	public void setAdapterCalls(List<AdapterCallInfo> adapterCalls) {
		this.adapterCalls = adapterCalls;
	}

	public AdapterHistoryInfo() {
		super();
	}

	public AdapterHistoryInfo(List<AdapterCallInfo> adapterCalls, List<AdapterWfLaunchInfo> adapterWfLaunches,
			List<AdapterWfNotifyInfo> adapterWfNotifies) {
		super();
		this.adapterCalls = adapterCalls;
		this.adapterWfLaunches = adapterWfLaunches;
		this.adapterWfNotifies = adapterWfNotifies;
	}

	public List<AdapterWfLaunchInfo> getAdapterWfLaunches() {
		return adapterWfLaunches;
	}

	public void setAdapterWfLaunches(List<AdapterWfLaunchInfo> adapterWfLaunches) {
		this.adapterWfLaunches = adapterWfLaunches;
	}

	public List<AdapterWfNotifyInfo> getAdapterWfNotifies() {
		return adapterWfNotifies;
	}

	public void setAdapterWfNotifies(List<AdapterWfNotifyInfo> adapterWfNotifies) {
		this.adapterWfNotifies = adapterWfNotifies;
	}
	
	
	
	

}
