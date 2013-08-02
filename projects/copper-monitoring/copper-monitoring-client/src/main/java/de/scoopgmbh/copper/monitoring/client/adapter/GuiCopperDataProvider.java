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
package de.scoopgmbh.copper.monitoring.client.adapter;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import de.scoopgmbh.copper.monitoring.client.form.filter.enginefilter.EnginePoolFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.adaptermonitoring.result.AdapterCallRowModel;
import de.scoopgmbh.copper.monitoring.client.ui.adaptermonitoring.result.AdapterLaunchRowModel;
import de.scoopgmbh.copper.monitoring.client.ui.adaptermonitoring.result.AdapterNotifyRowModel;
import de.scoopgmbh.copper.monitoring.client.ui.audittrail.result.AuditTrailResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.custommeasurepoint.filter.CustomMeasurePointFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.custommeasurepoint.result.CustomMeasurePointResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.logs.result.LogsResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.message.filter.MessageFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.message.result.MessageResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.provider.result.ProviderResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.sql.filter.SqlFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.sql.result.SqlResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.workflowinstance.filter.WorkflowInstanceFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.workflowinstance.result.WorkflowInstanceResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.workflowsummary.filter.WorkflowSummaryFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.workflowsummary.result.WorkflowSummaryResultModel;
import de.scoopgmbh.copper.monitoring.client.ui.worklowinstancedetail.filter.WorkflowInstanceDetailFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.worklowinstancedetail.result.WorkflowInstanceDetailResultModel;
import de.scoopgmbh.copper.monitoring.client.util.WorkflowVersion;
import de.scoopgmbh.copper.monitoring.core.CopperMonitoringService;
import de.scoopgmbh.copper.monitoring.core.data.filter.DistinctAndTypeFilter;
import de.scoopgmbh.copper.monitoring.core.data.filter.MeasurePointComperator;
import de.scoopgmbh.copper.monitoring.core.data.filter.MeasurePointFilter;
import de.scoopgmbh.copper.monitoring.core.data.filter.ProviderDataFilter;
import de.scoopgmbh.copper.monitoring.core.data.filter.TypeFilter;
import de.scoopgmbh.copper.monitoring.core.model.AdapterCallInfo;
import de.scoopgmbh.copper.monitoring.core.model.AdapterWfLaunchInfo;
import de.scoopgmbh.copper.monitoring.core.model.AdapterWfNotifyInfo;
import de.scoopgmbh.copper.monitoring.core.model.AuditTrailInfo;
import de.scoopgmbh.copper.monitoring.core.model.CopperInterfaceSettings;
import de.scoopgmbh.copper.monitoring.core.model.GenericMonitoringData;
import de.scoopgmbh.copper.monitoring.core.model.LogEvent;
import de.scoopgmbh.copper.monitoring.core.model.MeasurePointData;
import de.scoopgmbh.copper.monitoring.core.model.MessageInfo;
import de.scoopgmbh.copper.monitoring.core.model.MonitoringDataProviderInfo;
import de.scoopgmbh.copper.monitoring.core.model.MonitoringDataStorageInfo;
import de.scoopgmbh.copper.monitoring.core.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitoring.core.model.SystemResourcesInfo;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowClassMetaData;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowStateSummary;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowSummary;
import de.scoopgmbh.copper.monitoring.core.statistic.AggregateFunction;
import de.scoopgmbh.copper.monitoring.core.statistic.AggregateSystemRessourceAvg;
import de.scoopgmbh.copper.monitoring.core.statistic.AvgAggregateFunction;
import de.scoopgmbh.copper.monitoring.core.statistic.CountAggregateFunction;
import de.scoopgmbh.copper.monitoring.core.statistic.QuantilAggregateFunction;
import de.scoopgmbh.copper.monitoring.core.statistic.StatisticCreator;
import de.scoopgmbh.copper.monitoring.core.statistic.TimeValuePair;
import de.scoopgmbh.copper.monitoring.core.statistic.TimeframeGroup;
import de.scoopgmbh.copper.monitoring.core.statistic.converter.MeasurePointDataDateConverter;
import de.scoopgmbh.copper.monitoring.core.statistic.converter.MeasurePointDataDoubleConverter;
import de.scoopgmbh.copper.monitoring.core.statistic.converter.SystemResMeasurePointDataDoubleConverter;
import de.scoopgmbh.copper.monitoring.core.statistic.converter.SystemResourcesInfoDateConverter;
import de.scoopgmbh.copper.monitoring.core.statistic.converter.TimeConverter;

public class GuiCopperDataProvider {


	private final CopperMonitoringService copperMonitoringService;
	
	public GuiCopperDataProvider(final CopperMonitoringService copperDataProvider) {
		super();
		this.copperMonitoringService=copperDataProvider;
	}

	public List<WorkflowInstanceResultModel> getWorkflowInstanceList(WorkflowInstanceFilterModel filter, int maxResultCount){
		List<WorkflowInstanceInfo> list;
		try {
			list = copperMonitoringService.getWorkflowInstanceList(getPoolId(filter),filter.version.classname.get(),
					filter.state.get(), getFilterReadyInteger(filter.priority.get()),filter.from.get(),filter.to.get(), maxResultCount);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		ArrayList<WorkflowInstanceResultModel> result = new ArrayList<WorkflowInstanceResultModel>();
		for (WorkflowInstanceInfo workflowInstanceInfo: list){
			result.add(new WorkflowInstanceResultModel(workflowInstanceInfo));
		}
		return result;
	}
	
	private Integer getFilterReadyInteger(String string){
		try {
			return string==null?null:Integer.valueOf(string);
		} catch (NumberFormatException e){
			return null;
		}
	}

	public String getPoolId(EnginePoolFilterModel enginePoolModel) {
		if (enginePoolModel.selectedPool.get()==null){
			return null;
		} else {
			return enginePoolModel.selectedPool.get().getId();
		}
	}
	
	public List<AuditTrailResultModel> getAuditTrails(de.scoopgmbh.copper.monitoring.client.ui.audittrail.filter.AuditTrailFilterModel  filter, int maxResultCount){
		
		List<AuditTrailInfo> list;
		try {
			list = copperMonitoringService.getAuditTrails(filter.workflowClass.getValue(), filter.workflowInstanceId.getValue(), filter.correlationId.getValue(), getFilterReadyInteger(filter.level.getValue()), maxResultCount);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		ArrayList<AuditTrailResultModel> result = new ArrayList<AuditTrailResultModel>();
		for (AuditTrailInfo auditTrailInfo: list){
			result.add(new AuditTrailResultModel(auditTrailInfo));
		}
		return result;
	}

	public List<WorkflowSummaryResultModel> getWorkflowSummery(WorkflowSummaryFilterModel filter) {
		List<WorkflowSummary> summeries;
		try {
			summeries = copperMonitoringService.getWorkflowSummary(getPoolId(filter), filter.version.classname.get());
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		ArrayList<WorkflowSummaryResultModel> result = new ArrayList<WorkflowSummaryResultModel>();
		for (WorkflowSummary workflowSummery: summeries){
			result.add(new WorkflowSummaryResultModel(workflowSummery));
		}
		return result;
	}
	
	public List<WorkflowVersion> getWorkflowClassesList(final String engineId){
		List<WorkflowClassMetaData> list;
		try {
			list = copperMonitoringService.getWorkflowClassesList(engineId);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		ArrayList<WorkflowVersion> result = new ArrayList<WorkflowVersion>();
		for (WorkflowClassMetaData workflowClassesInfo: list){
			result.add(new WorkflowVersion(workflowClassesInfo));
		}
		return result;
	}

	public WorkflowInstanceDetailResultModel getWorkflowDetails(WorkflowInstanceDetailFilterModel filter ) {
		try {
			return new WorkflowInstanceDetailResultModel(copperMonitoringService.getWorkflowInstanceDetails(filter.workflowInstanceId.getValue(),filter.selectedEngine.get().getId()));
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public WorkflowStateSummary getCopperLoadInfo(ProcessingEngineInfo engine) {
		try {
			return  copperMonitoringService.getAggregatedWorkflowStateSummary(engine.getId());
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public String getAuditTrailMessage(SimpleLongProperty id) {
		try {
			return  copperMonitoringService.getAuditTrailMessage(id.getValue());
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<SqlResultModel> executeSqlQuery(SqlFilterModel filter, int maxResultCount){
		List<String[]> list;
		try {
			list = copperMonitoringService.executeSqlQuery(filter.sqlQuery.getValue(), maxResultCount);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		ArrayList<SqlResultModel> result = new ArrayList<SqlResultModel>();
		for (String[] row: list){
			result.add(new SqlResultModel(row));
		}
		return result;
	}

	public List<SystemResourcesInfo> getSystemRessources(Date from, Date to, int groupCount) {
		try {
			if (from==null){
				from = copperMonitoringService.getMonitoringDataMinDate();
			}
	
			if (to==null){
				to = copperMonitoringService.getMonitoringDataMaxDate();
			}
			
			final AggregateFunction<SystemResourcesInfo, SystemResourcesInfo> aggregateFunction =new AggregateSystemRessourceAvg();
			final TimeConverter<SystemResourcesInfo> dateConverter = new SystemResourcesInfoDateConverter();
		
			final StatisticCreator<SystemResourcesInfo, SystemResourcesInfo> statisticCreator = new StatisticCreator<SystemResourcesInfo,SystemResourcesInfo>(TimeframeGroup.<SystemResourcesInfo, SystemResourcesInfo>createGroups(
					groupCount,from,to,aggregateFunction, dateConverter));
			List<List<SystemResourcesInfo>> statisticCreators = copperMonitoringService.<SystemResourcesInfo,SystemResourcesInfo>createStatistic(
					new TypeFilter<SystemResourcesInfo>(SystemResourcesInfo.class), Arrays.<StatisticCreator<SystemResourcesInfo, SystemResourcesInfo>>asList(statisticCreator),from,to);
			return (List<SystemResourcesInfo>) statisticCreators.get(0);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<ProcessingEngineInfo> getEngineList() {
		try {
			return copperMonitoringService.getProccessingEngineList();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<MeasurePointData> getMeasurePoints(EnginePoolFilterModel engineFilter) {
		try {
			return copperMonitoringService.getMeasurePoints(engineFilter.selectedEngine.get().getId());
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void setNumberOfThreads(String engineid, String processorPoolId, int numberOfThreads){
		try {
			copperMonitoringService.setNumberOfThreads(engineid, processorPoolId, numberOfThreads);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}		
	}
	
	public void setThreadPriority(String engineid, String processorPoolId, int threadPriority){
		try {
			copperMonitoringService.setThreadPriority(engineid, processorPoolId, threadPriority);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}		
	}

	public void resetMeasurePoints() {
		try {
			copperMonitoringService.resetMeasurePoints();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}	
	}

	public void setBatcherNumThreads(String id, int numThread) {
		try {
			copperMonitoringService.setBatcherNumThreads(numThread, id);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}	
	}

	public List<MessageResultModel> getMessageList(MessageFilterModel filter, final int maxResultCount) {
		List<MessageInfo> list;
		try {
			list = copperMonitoringService.getMessageList(filter.ignoreProcessed.get(), maxResultCount);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		ArrayList<MessageResultModel> result = new ArrayList<MessageResultModel>();
		for (MessageInfo message: list){
			result.add(new MessageResultModel(message)); 
		}
		return result;
	}

	public void restartAllError(String engineid) {
		try {
			copperMonitoringService.restartAllErroneousInstances(engineid);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public CopperInterfaceSettings getInterfaceSettings() {
		try {
			return copperMonitoringService.getSettings();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public ObservableList<AdapterCallRowModel> getAdapterCalls(Date from, Date to, int maxCount){
		try {
			ObservableList<AdapterCallRowModel> result = FXCollections.observableArrayList();
			List<AdapterCallInfo> list = copperMonitoringService.getList(new TypeFilter<AdapterCallInfo>(AdapterCallInfo.class), from, to, maxCount);
			for (AdapterCallInfo adapterCallInfo: list){
				result.add(new AdapterCallRowModel(adapterCallInfo));
			}
			return result;
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}
	
	public ObservableList<AdapterNotifyRowModel> getAdapterNotifies(Date from, Date to, int maxCount){
		try {
			ObservableList<AdapterNotifyRowModel> result = FXCollections.observableArrayList();
			List<AdapterWfNotifyInfo> list = copperMonitoringService.getList(new TypeFilter<AdapterWfNotifyInfo>(AdapterWfNotifyInfo.class), from, to, maxCount);
			for (AdapterWfNotifyInfo adapterWfNotifyInfo: list){
				result.add(new AdapterNotifyRowModel(adapterWfNotifyInfo));
			}
			return result;
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}	
	
	public ObservableList<AdapterLaunchRowModel> getAdapterLaunches(Date from, Date to, int maxCount){
		try {
			ObservableList<AdapterLaunchRowModel> result = FXCollections.observableArrayList();
			List<AdapterWfLaunchInfo> list = copperMonitoringService.getList(new TypeFilter<AdapterWfLaunchInfo>(AdapterWfLaunchInfo.class), from, to, maxCount);
			for (AdapterWfLaunchInfo dapterLaunchInfo: list){
				result.add(new AdapterLaunchRowModel(dapterLaunchInfo));
			}
			return result;
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void restartInstance(final String workflowInstanceId, String engineid) {
		try {
			copperMonitoringService.restartWorkflowInstance(workflowInstanceId, engineid);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public CustomMeasurePointResultModel getMonitoringMeasurePoints(CustomMeasurePointFilterModel filter, Date from, Date to, int count) {
		try {
			if (from==null){
				from=copperMonitoringService.getMonitoringDataMinDate();
			}	
			if (to==null){
				to=copperMonitoringService.getMonitoringDataMaxDate();
			}	
			
			final AvgAggregateFunction<MeasurePointData> avgFunction =new AvgAggregateFunction<MeasurePointData>(new MeasurePointDataDoubleConverter());
			final StatisticCreator<MeasurePointData, TimeValuePair<Double>> avgCreator = new StatisticCreator<MeasurePointData,TimeValuePair<Double>>(TimeframeGroup.<MeasurePointData, TimeValuePair<Double>>createGroups(
					count,from,to,avgFunction, new MeasurePointDataDateConverter()));
			
			final CountAggregateFunction<MeasurePointData> countFunction =new CountAggregateFunction<MeasurePointData>();
			final StatisticCreator<MeasurePointData, TimeValuePair<Double>> countCreator = new StatisticCreator<MeasurePointData,TimeValuePair<Double>>(TimeframeGroup.<MeasurePointData, TimeValuePair<Double>>createGroups(
					count,from,to,countFunction, new MeasurePointDataDateConverter()));
			
			final QuantilAggregateFunction<MeasurePointData> quantil50Function =new QuantilAggregateFunction<MeasurePointData>(0.5,new MeasurePointDataDoubleConverter());
			final StatisticCreator<MeasurePointData, TimeValuePair<Double>> quantil50Creator = new StatisticCreator<MeasurePointData,TimeValuePair<Double>>(TimeframeGroup.<MeasurePointData, TimeValuePair<Double>>createGroups(
					count,from,to,quantil50Function, new MeasurePointDataDateConverter()));
			
			final QuantilAggregateFunction<MeasurePointData> quantil90Function =new QuantilAggregateFunction<MeasurePointData>(0.9,new MeasurePointDataDoubleConverter());
			final StatisticCreator<MeasurePointData, TimeValuePair<Double>> quantil90Creator = new StatisticCreator<MeasurePointData,TimeValuePair<Double>>(TimeframeGroup.<MeasurePointData, TimeValuePair<Double>>createGroups(
					count,from,to,quantil90Function, new MeasurePointDataDateConverter()));
			
			final QuantilAggregateFunction<MeasurePointData> quantil99Function =new QuantilAggregateFunction<MeasurePointData>(0.99,new MeasurePointDataDoubleConverter());
			final StatisticCreator<MeasurePointData, TimeValuePair<Double>> quantil99Creator = new StatisticCreator<MeasurePointData,TimeValuePair<Double>>(TimeframeGroup.<MeasurePointData, TimeValuePair<Double>>createGroups(
					count,from,to,quantil99Function, new MeasurePointDataDateConverter()));
			
			final AvgAggregateFunction<MeasurePointData> avgCpuFunction =new AvgAggregateFunction<MeasurePointData>(new SystemResMeasurePointDataDoubleConverter());
			final StatisticCreator<MeasurePointData, TimeValuePair<Double>> avgCpuCreator = new StatisticCreator<MeasurePointData,TimeValuePair<Double>>(TimeframeGroup.<MeasurePointData, TimeValuePair<Double>>createGroups(
					count,from,to,avgCpuFunction, new MeasurePointDataDateConverter()));
			
			final List<StatisticCreator<MeasurePointData, TimeValuePair<Double>>> statistics = Arrays.<StatisticCreator<MeasurePointData, TimeValuePair<Double>>>asList(
					avgCreator,
					countCreator,
					quantil50Creator,
					quantil90Creator,
					quantil99Creator,
					avgCpuCreator
					);
			
			
			List<List<TimeValuePair<Double>>> result = copperMonitoringService.<MeasurePointData,TimeValuePair<Double>>createStatistic(
					new MeasurePointFilter(filter.measurePointId.get()), 
					statistics, from, to);
			
			return new CustomMeasurePointResultModel(
					result.get(0),
					result.get(1),
					result.get(2),
					result.get(3),
					result.get(4),
					result.get(5)
					);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<String> getMonitoringMeasurePointIds(Date from, Date to) {
		try {
			List<MeasurePointData> list = copperMonitoringService.getList(new DistinctAndTypeFilter<MeasurePointData>(MeasurePointData.class,new MeasurePointComperator()), from , to, 100);
			ArrayList<String> result = new ArrayList<String>();
			for (MeasurePointData measurePointData: list){
				result.add(measurePointData.getMeasurePointId());
			}
			return result;
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}
	

	public LogsResultModel getLogData(Date from, Date to, int maxCount) {
		try {
			List<LogEvent> list = copperMonitoringService.getList(new TypeFilter<LogEvent>(LogEvent.class), from, to, maxCount);
			return new LogsResultModel(copperMonitoringService.getLogConfig(), list);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void updateLogConfig(String config){
		try {
			copperMonitoringService.updateLogConfig(config);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getDatabaseMonitoringHtmlReport() {
		try {
			return copperMonitoringService.getDatabaseMonitoringHtmlReport();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public String getDatabaseMonitoringHtmlDetailReport(String sqlid)  {
		try {
			return copperMonitoringService.getDatabaseMonitoringHtmlDetailReport(sqlid);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getDatabaseMonitoringRecommendationsReport(String sqlid)  {
		try {
			return copperMonitoringService.getDatabaseMonitoringRecommendationsReport(sqlid);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public List<MonitoringDataProviderInfo> getMonitoringDataProvider() {
		try {
			return copperMonitoringService.getMonitoringDataProviderInfos();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public void startMonitoringDataProvider(String name) {
		try {
			copperMonitoringService.startMonitoringDataProvider(name);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public void stopMonitoringDataProvider(String name) {
		try {
			copperMonitoringService.stopMonitoringDataProvider(name);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public MonitoringDataStorageInfo getMonitoringStorageInfo() {
		try {
			return copperMonitoringService.getMonitroingDataStorageInfo();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public List<ProviderResultModel> getGenericMonitoringData(String id, Date from,
			Date to, int maxCount) {
		try {
			ArrayList<ProviderResultModel> result = new ArrayList<ProviderResultModel>();
			List<GenericMonitoringData> list = copperMonitoringService.getList(new ProviderDataFilter(id), from, to, maxCount);
			for (GenericMonitoringData genericMonitoringData: list){
				result.add(new ProviderResultModel(genericMonitoringData));
			}
			return result;
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}
	
}
