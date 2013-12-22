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
package org.copperengine.monitoring.client.screenshotgen.view.fixture;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.copperengine.monitoring.core.CopperMonitoringService;
import org.copperengine.monitoring.core.data.filter.MonitoringDataFilter;
import org.copperengine.monitoring.core.model.AuditTrailInfo;
import org.copperengine.monitoring.core.model.CopperInterfaceSettings;
import org.copperengine.monitoring.core.model.DependencyInjectorInfo;
import org.copperengine.monitoring.core.model.MeasurePointData;
import org.copperengine.monitoring.core.model.MessageInfo;
import org.copperengine.monitoring.core.model.MonitoringDataProviderInfo;
import org.copperengine.monitoring.core.model.MonitoringDataStorageInfo;
import org.copperengine.monitoring.core.model.ProcessingEngineInfo;
import org.copperengine.monitoring.core.model.ProcessingEngineInfo.EngineTyp;
import org.copperengine.monitoring.core.model.ProcessorPoolInfo;
import org.copperengine.monitoring.core.model.ProcessorPoolInfo.ProcessorPoolTyp;
import org.copperengine.monitoring.core.model.StorageInfo;
import org.copperengine.monitoring.core.model.WorkflowClassMetaData;
import org.copperengine.monitoring.core.model.WorkflowInstanceInfo;
import org.copperengine.monitoring.core.model.WorkflowInstanceMetaData;
import org.copperengine.monitoring.core.model.WorkflowInstanceState;
import org.copperengine.monitoring.core.model.WorkflowRepositoryInfo;
import org.copperengine.monitoring.core.model.WorkflowRepositoryInfo.WorkflowRepositorTyp;
import org.copperengine.monitoring.core.model.WorkflowStateSummary;
import org.copperengine.monitoring.core.model.WorkflowSummary;
import org.copperengine.monitoring.core.statistic.StatisticCreator;

public class TestDataProvider implements CopperMonitoringService {
    private static final long serialVersionUID = -509088135898037190L;

    public TestDataProvider() {
        super();
    }

    @Override
    public String getAuditTrailMessage(long id) throws RemoteException {
        String json = "{\r\n" +
                "		    \"firstName\": \"John\",\r\n" +
                "		    \"lastName\": \"Smith\",\r\n" +
                "		    \"age\": 25,\r\n" +
                "		    \"address\": {\r\n" +
                "		        \"streetAddress\": \"21 2nd Street\",\r\n" +
                "		        \"city\": \"New York\",\r\n" +
                "		        \"state\": \"NY\",\r\n" +
                "		        \"postalCode\": 10021\r\n" +
                "		    },\r\n" +
                "		    \"phoneNumber\": [\r\n" +
                "		        {\r\n" +
                "		            \"type\": \"home\",\r\n" +
                "		            \"number\": \"212 555-1234\"\r\n" +
                "		        },\r\n" +
                "		        {\r\n" +
                "		            \"type\": \"fax\",\r\n" +
                "		            \"number\": \"646 555-4567\"\r\n" +
                "		        }\r\n" +
                "		    ]\r\n" +
                "		}";

        String xml =
                "<?xml version=\"1.0\"?>\r\n" +
                        "<note>\r\n" +
                        "    <to>Tove</to>\r\n" +
                        "    <from>Jani</from>\r\n" +
                        "    <heading>Reminder</heading>\r\n" +
                        "    <body>Don't forget me this weekend!</body>\r\n" +
                        "</note>\r\n" +
                        "";

        String text = "blablablbalabl";

        int rnd = new Random().nextInt(3);
        if (rnd == 0) {
            return json;
        }
        if (rnd == 1) {
            return xml;
        }
        if (rnd == 2) {
            return text;
        }
        return "";
    }

    @Override
    public List<WorkflowSummary> getWorkflowSummary(String poolid, String classname) throws RemoteException {
        Map<WorkflowInstanceState, Integer> map = new HashMap<WorkflowInstanceState, Integer>();
        int counter = 0;
        for (WorkflowInstanceState workflowInstanceState : WorkflowInstanceState.values()) {
            map.put(workflowInstanceState, counter * 10 + ((int) (Math.random() * 10)));
            counter++;
        }

        ArrayList<WorkflowSummary> result = new ArrayList<WorkflowSummary>();
        WorkflowSummary workflowSummery = new WorkflowSummary("", 10,
                new WorkflowClassMetaData("WorkflowClass1", "alias", 0L, +(long) (Math.random() * 100), 0L, ""),
                new WorkflowStateSummary(map));
        result.add(workflowSummery);

        return result;
    }

    @Override
    public List<WorkflowInstanceInfo> getWorkflowInstanceList(String poolid, String classname,
            WorkflowInstanceState state, Integer priority, Date form, Date to, long resultRowLimit) throws RemoteException {
        ArrayList<WorkflowInstanceInfo> result = new ArrayList<WorkflowInstanceInfo>();
        WorkflowInstanceInfo workflowInfo = new WorkflowInstanceInfo();
        workflowInfo.setId("1");
        result.add(workflowInfo);
        workflowInfo = new WorkflowInstanceInfo();
        workflowInfo.setId("2");
        result.add(workflowInfo);
        workflowInfo = new WorkflowInstanceInfo();
        workflowInfo.setId("3");
        workflowInfo.setTimeout(new Date());
        result.add(workflowInfo);

        return result;
    }

    @Override
    public List<AuditTrailInfo> getAuditTrails(String workflowClass, String workflowInstanceId, String correlationId, Integer level, long resultRowLimit)
            throws RemoteException {
        ArrayList<AuditTrailInfo> result = new ArrayList<AuditTrailInfo>();
        AuditTrailInfo auditTrailInfo = new AuditTrailInfo();
        auditTrailInfo.setId(1);
        auditTrailInfo.setLoglevel(1);
        result.add(auditTrailInfo);
        auditTrailInfo = new AuditTrailInfo();
        auditTrailInfo.setId(2);
        auditTrailInfo.setLoglevel(2);
        result.add(auditTrailInfo);
        auditTrailInfo = new AuditTrailInfo();
        auditTrailInfo.setId(3);
        auditTrailInfo.setLoglevel(3);
        auditTrailInfo.setOccurrence(new Date());
        auditTrailInfo.setMessageType("json");
        result.add(auditTrailInfo);
        return result;
    }

    @Override
    public List<WorkflowClassMetaData> getWorkflowClassesList(final String engineId) throws RemoteException {
        ArrayList<WorkflowClassMetaData> result = new ArrayList<WorkflowClassMetaData>();
        result.add(new WorkflowClassMetaData("WorkflowClass1", "alias", 0L, +(long) (Math.random() * 100), 0L, ""));
        result.add(new WorkflowClassMetaData("WorkflowClass2", "alias", 1L, +(long) (Math.random() * 100), 0L, ""));
        result.add(new WorkflowClassMetaData("WorkflowClass2", "alias", 1L, +(long) (Math.random() * 100), 0L, ""));
        result.add(new WorkflowClassMetaData("WorkflowClass2", "alias", 1L, +(long) (Math.random() * 100), 0L, ""));
        result.add(new WorkflowClassMetaData("WorkflowClass2", "alias", 2L, +(long) (Math.random() * 100), 0L, ""));
        result.add(new WorkflowClassMetaData("WorkflowClass2", "alias", 2L, +(long) (Math.random() * 100), 0L, ""));
        result.add(new WorkflowClassMetaData("WorkflowClass2", "alias", 2L, +(long) (Math.random() * 100), 0L, ""));
        result.add(new WorkflowClassMetaData("WorkflowClass3", "alias", 3L, +(long) (Math.random() * 100), 0L, ""));
        return result;
    }

    @Override
    public WorkflowInstanceMetaData getWorkflowInstanceDetails(String workflowInstanceId, String engineid) {
        return new WorkflowInstanceMetaData(new WorkflowClassMetaData("WorkflowClass1", "alias", 0L, +(long) (Math.random() * 100), 0L,
                "package org.copperengine.monitoring.example.workflow;\r\n" +
                        "\r\n" +
                        "import java.math.BigDecimal;\r\n" +
                        "import java.util.ArrayList;\r\n" +
                        "import java.util.Date;\r\n" +
                        "\r\n" +
                        "import org.copperengine.AutoWire;\r\n" +
                        "import org.copperengine.Interrupt;\r\n" +
                        "import org.copperengine.Response;\r\n" +
                        "import org.copperengine.WaitMode;\r\n" +
                        "import org.copperengine.Workflow;\r\n" +
                        "import org.copperengine.WorkflowDescription;\r\n" +
                        "import org.copperengine.audit.AuditTrail;\r\n" +
                        "import org.copperengine.monitoring.example.adapter.Bill;\r\n" +
                        "import org.copperengine.monitoring.example.adapter.BillAdapter;\r\n" +
                        "import org.copperengine.monitoring.example.adapter.BillableService;\r\n" +
                        "import org.copperengine.persistent.PersistentWorkflow;\r\n" +
                        "\r\n" +
                        "@WorkflowDescription(alias=\"BillWorkflow\", majorVersion=1, minorVersion=0, patchLevelVersion=0)\r\n" +
                        "public class WorkflowClass1 extends PersistentWorkflow<String> {\r\n" +
                        "	private static final long serialVersionUID = 1L;\r\n" +
                        "\r\n" +
                        "	private transient BillAdapter billAdapter;\r\n" +
                        "	private transient AuditTrail auditTrail;\r\n" +
                        "	\r\n" +
                        "	private ArrayList<BillableService> billableServices= new ArrayList<BillableService>();\r\n" +
                        "\r\n" +
                        "	@AutoWire\r\n" +
                        "	public void setBillAdapter(BillAdapter billAdapter) {\r\n" +
                        "		this.billAdapter = billAdapter;\r\n" +
                        "	}\r\n" +
                        "	\r\n" +
                        "	@AutoWire\r\n" +
                        "	public void setAuditTrail(AuditTrail auditTrail) {\r\n" +
                        "		this.auditTrail = auditTrail;\r\n" +
                        "	}\r\n" +
                        "\r\n" +
                        "	@Override\r\n" +
                        "	public void main() throws Interrupt {\r\n" +
                        "		while (true){\r\n" +
                        "			auditTrail.asynchLog(2, new Date(), \"\", \"\", \"\", \"\", \"\", \"wait for Data\", \"Text\");\r\n" +
                        "			wait(WaitMode.ALL,Workflow.NO_TIMEOUT, BillAdapter.BILL_TIME,BillAdapter.BILLABLE_SERVICE);\r\n" +
                        "			auditTrail.asynchLog(1, new Date(), \"\", \"\", \"\", \"\", \"\", \"data found\", \"Text\");\r\n" +
                        "			\r\n" +
                        "			ArrayList<Response<?>> all = new ArrayList<Response<?>>(getAndRemoveResponses(BillAdapter.BILL_TIME));\r\n" +
                        "			all.addAll(getAndRemoveResponses(BillAdapter.BILLABLE_SERVICE));\r\n" +
                        "			\r\n" +
                        "			Response<String> rsponse = new Response<String>(\"cor\",\"message\",null);\r\n" +
                        "			rsponse.getResponse();\r\n" +
                        "			\r\n" +
                        "			\r\n" +
                        "			for(Response<?> response: all){\r\n" +
                        "				if (response.getResponse() instanceof BillableService){\r\n" +
                        "					billableServices.add(((BillableService)response.getResponse()));\r\n" +
                        "				}\r\n" +
                        "			}\r\n" +
                        "			for(Response<?> response: all){\r\n" +
                        "				if (response.getResponse() instanceof Bill){\r\n" +
                        "					Bill bill = ((Bill)response.getResponse());\r\n" +
                        "					BigDecimal sum = new BigDecimal(0);\r\n" +
                        "					for (BillableService billableService: billableServices){\r\n" +
                        "						sum = sum.add(billableService.getAmount());\r\n" +
                        "					}\r\n" +
                        "					bill.setTotalAmount(sum);\r\n" +
                        "					billAdapter.publishBill(bill);\r\n" +
                        "					billableServices.clear();\r\n" +
                        "				}\r\n" +
                        "			}\r\n" +
                        "			\r\n" +
                        "			\r\n" +
                        "		}\r\n" +
                        "		\r\n" +
                        "	}\r\n" +
                        "\r\n" +
                        "}"), null);

    }

    @Override
    public CopperInterfaceSettings getSettings() throws RemoteException {
        return new CopperInterfaceSettings(true);
    }

    @Override
    public List<String[]> executeSqlQuery(String query, long resultRowLimit) {
        List<String[]> result = new ArrayList<String[]>();
        result.add(new String[] { "column1", "column2", "colum3", query });
        result.add(new String[] { "content1", "content2", "conten3", query });
        return result;
    }

    @Override
    public WorkflowStateSummary getAggregatedWorkflowStateSummary(String engineid) throws RemoteException {
        Map<WorkflowInstanceState, Integer> map = new HashMap<WorkflowInstanceState, Integer>();
        int counter = 0;
        for (WorkflowInstanceState workflowInstanceState : WorkflowInstanceState.values()) {
            map.put(workflowInstanceState, counter * 10 + ((int) (Math.random() * 10)));
            counter++;
        }
        return new WorkflowStateSummary(map);
    }

    @Override
    public void restartWorkflowInstance(String workflowInstanceId, String engineid) throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void restartAllErroneousInstances(String engineid) throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public List<ProcessingEngineInfo> getProccessingEngineList() throws RemoteException {
        WorkflowRepositoryInfo repositoryInfo = new WorkflowRepositoryInfo();
        repositoryInfo.setWorkflowRepositorTyp(WorkflowRepositorTyp.FILE);
        repositoryInfo.setSrcPaths(new ArrayList<String>());
        return Arrays.asList(
                new ProcessingEngineInfo(EngineTyp.PERSISTENT, "peId1", repositoryInfo, new DependencyInjectorInfo("POJO"), "None", new StorageInfo(), new ProcessorPoolInfo("poId1", ProcessorPoolTyp.PERSISTENT)),
                new ProcessingEngineInfo(EngineTyp.TRANSIENT, "peId2", repositoryInfo, new DependencyInjectorInfo("POJO"), "None", new StorageInfo(), new ProcessorPoolInfo("poId2", ProcessorPoolTyp.TRANSIENT), new ProcessorPoolInfo("poId3", ProcessorPoolTyp.TRANSIENT))
        );
    }

    @Override
    public List<MeasurePointData> getMeasurePoints(String engineid) {
        ArrayList<MeasurePointData> result = new ArrayList<MeasurePointData>();
        for (int i = 0; i < 20; i++) {
            result.add(new MeasurePointData(getClass().getName() + "#" + i, 10, i + (int) (10000 * Math.random()), 10));
        }
        return result;
    }

    public int numberOfThreads;

    @Override
    public void setNumberOfThreads(String engineid, String processorPoolId, int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    public int threadPriority;

    @Override
    public void setThreadPriority(String engineid, String processorPoolId, int threadPriority) {
        this.threadPriority = threadPriority;
    }

    @Override
    public void resetMeasurePoints() {
        // TODO Auto-generated method stub
    }

    public int numerOfThreadsBatcher;

    @Override
    public void setBatcherNumThreads(int numThread, String engineid) {
        this.numerOfThreadsBatcher = numThread;
    }

    @Override
    public List<MessageInfo> getMessageList(boolean ignoreproceeded, long resultRowLimit) {
        return Arrays.asList(new MessageInfo(new Date(), "message", "correlationid"));
    }

    // @Override
    // public AdapterHistoryInfo getAdapterHistoryInfos(String adapterId) throws RemoteException {
    // final AdapterHistoryInfo adapterHistoryInfo = new AdapterHistoryInfo();
    // adapterHistoryInfo.setAdapterCalls(new ArrayList<AdapterCallInfo>());
    // adapterHistoryInfo.setAdapterWfLaunches(new ArrayList<AdapterWfLaunchInfo>());
    // adapterHistoryInfo.setAdapterWfNotifies(new ArrayList<AdapterWfNotifyInfo>());
    // return adapterHistoryInfo;
    // }

    // @Override
    // public List<MeasurePointData> getMonitoringMeasurePoints(String measurePoint,long limit) throws RemoteException {
    // ArrayList<MeasurePointData> list = new ArrayList<MeasurePointData>();
    // for (int i=0;i<20;i++){
    // final MeasurePointData measurepoint = new MeasurePointData("Measurepoint");
    // measurepoint.setElapsedTimeMicros((long) (Math.random()*50));
    // measurepoint.setSystemResourcesInfo(new PerformanceMonitor().createRessourcenInfo());
    // measurepoint.setTime(new Date(i));
    // list.add(measurepoint);
    // }
    // return list;
    // }

    @Override
    public String getLogConfig() throws RemoteException {
        return "# Set root logger level to DEBUG and its only appender to A1.\r\n" +
                "log4j.rootLogger=DEBUG, A1\r\n" +
                "\r\n" +
                "# A1 is set to be a ConsoleAppender.\r\n" +
                "log4j.appender.A1=org.apache.log4j.ConsoleAppender";
    }

    @Override
    public void updateLogConfig(String config) throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public String getDatabaseMonitoringHtmlReport() throws RemoteException {
        return "<html>\r\n" +
                "  <head>\r\n" +
                "    <title> SQL Monitor List </title>\r\n" +
                "    <style type=\"text/css\"> \r\n" +
                "         body, table, input, select, textarea\r\n" +
                "         {font:normal normal 8pt Verdana,Arial;text-decoration:none;\r\n" +
                "          color:#000000;}\r\n" +
                "         .s8 {font-size:8pt;color:#006699}\r\n" +
                "         .s9 {font-size:10pt;color:#006699}\r\n" +
                "         .s10 {font-size:14pt;color:#006699;}\r\n" +
                "         .s16 {border-width : 1px; border-color : #CCCC99;\r\n" +
                "              border-style: solid; color:#006699;font-size:8pt; \r\n" +
                "              background-color:#CCCC99; }\r\n" +
                "        .s17 {border-width : 1px; border-color : #CCCC99;\r\n" +
                "              border-style: solid; font-size:8pt; \r\n" +
                "              background-color:#E8E8E6; empty-cells: show }\r\n" +
                "        .s17a {border-width : 1px; border-color : #BDCCC3;\r\n" +
                "              border-style: solid; font-size:8pt; \r\n" +
                "              background-color:#F5F5F5; empty-cells: show}\r\n" +
                "        .s17b {border-width : 1px; border-color : #BDCCC3;\r\n" +
                "               border-style: solid; font-size:8pt; \r\n" +
                "               background-color:#F1F5DD; empty-cells: show}\r\n" +
                "        .s27 {border-width : 1px; border-color : #CCCC99; \r\n" +
                "              border-style: solid;}\r\n" +
                "\r\n" +
                "        .graph {\r\n" +
                "          border: solid 0px ; empty-cells: show\r\n" +
                "        }\r\n" +
                "\r\n" +
                "        .bar {\r\n" +
                "          border-left: solid 0px;\r\n" +
                "          padding-right: 0.5em;\r\n" +
                "          font-size:1pt;\r\n" +
                "        }\r\n" +
                "\r\n" +
                "        .sql {border-width : 1px; border-color : #CCCC99;\r\n" +
                "              border-style: solid; font-size:8pt; \r\n" +
                "              background-color:#E8E8E6;\r\n" +
                "              empty-cells: show; text-align: left }\r\n" +
                "\r\n" +
                "        .nodisp { font-size: 1% ; height: 10px; color:#F2F2F2 }\r\n" +
                "        \r\n" +
                "        .bar div { \r\n" +
                "          text-align: center;\r\n" +
                "          float: left;\r\n" +
                "        }\r\n" +
                "\r\n" +
                "        .executing { \r\n" +
                "          background-color: #7CFF44;\r\n" +
                "        }      \r\n" +
                "\r\n" +
                "        .error { \r\n" +
                "          background-color: red;\r\n" +
                "        }      \r\n" +
                "\r\n" +
                "        .div_Cpu { \r\n" +
                "          border-top: solid 3px #1EC162;\r\n" +
                "          background-color: #19A352;\r\n" +
                "          color: #19A352;\r\n" +
                "          border-bottom: solid 3px #0E5D2F;\r\n" +
                "        }      \r\n" +
                "\r\n" +
                "        .div_cpu_time { \r\n" +
                "          border-top: solid 3px #1EC162;\r\n" +
                "          background-color: #19A352;\r\n" +
                "          color: #19A352;\r\n" +
                "          border-bottom: solid 3px #0E5D2F;\r\n" +
                "        }      \r\n" +
                "\r\n" +
                "        .div_duration { \r\n" +
                "          border-top: solid 3px #A2C8E4;\r\n" +
                "          background-color: #8CADC5;\r\n" +
                "          color: #8CADC5;\r\n" +
                "          border-bottom: solid 3px #617888;\r\n" +
                "          cursor: default;\r\n" +
                "        }      \r\n" +
                "\r\n" +
                "        .div_executing_time { \r\n" +
                "          border-top: solid 3px #A2C8E4;\r\n" +
                "          background-color: #8CADC5;\r\n" +
                "          color: #8CADC5;\r\n" +
                "          border-bottom: solid 3px #617888;\r\n" +
                "          cursor: default;\r\n" +
                "        }\r\n" +
                "\r\n" +
                "        .div_queuing_time {\r\n" +
                "          border-top: solid 3px #C5FFB7;\r\n" +
                "          background-color: #86FF86;\r\n" +
                "          color: #86FF86;\r\n" +
                "          border-bottom: solid 3px #74DD74;\r\n" +
                "          cursor: default;\r\n" +
                "        } \r\n" +
                "\r\n" +
                "        .div_read_reqs { \r\n" +
                "          border-top: solid 3px #FFC846;\r\n" +
                "          background-color: #DDAD3D;\r\n" +
                "          color: #DDAD3D;\r\n" +
                "          border-bottom: solid 3px #B99133;\r\n" +
                "          cursor: default;\r\n" +
                "        }\r\n" +
                "\r\n" +
                "        .div_write_reqs { \r\n" +
                "          border-top: solid 3px #F76838;\r\n" +
                "          background-color: #BC4F2B;\r\n" +
                "          color: #BC4F2B;\r\n" +
                "          border-bottom: solid 3px #9C4223;\r\n" +
                "          cursor: default;\r\n" +
                "        }\r\n" +
                "\r\n" +
                "        .div_read_bytes { \r\n" +
                "          border-top: solid 3px #FFC846;\r\n" +
                "          background-color: #DDAD3D;\r\n" +
                "          color: #DDAD3D;\r\n" +
                "          border-bottom: solid 3px #B99133;\r\n" +
                "          cursor: default;\r\n" +
                "        }\r\n" +
                "\r\n" +
                "        .div_write_bytes { \r\n" +
                "          border-top: solid 3px #F76838;\r\n" +
                "          background-color: #BC4F2B;\r\n" +
                "          color: #BC4F2B;\r\n" +
                "          border-bottom: solid 3px #9C4223;\r\n" +
                "          cursor: default;\r\n" +
                "        }\r\n" +
                "\r\n" +
                "        .div_buffer_gets { \r\n" +
                "          border-top: solid 3px #FFAB9A;\r\n" +
                "          background-color: #D89182;\r\n" +
                "          color: #D89182;\r\n" +
                "          border-bottom: solid 3px #BC7E72;\r\n" +
                "          cursor: default;\r\n" +
                "        }      \r\n" +
                "\r\n" +
                "        .div_sql_cpu_time { \r\n" +
                "          border-top: solid 3px #1EC162;\r\n" +
                "          background-color: #19A352;\r\n" +
                "          color: #19A352;\r\n" +
                "          border-bottom: solid 3px #0E5D2F;\r\n" +
                "          cursor: default;\r\n" +
                "        }      \r\n" +
                "\r\n" +
                "       .div_UserIO { \r\n" +
                "          border-top: solid 3px #49A1FF;\r\n" +
                "          background-color: #3C85D2;\r\n" +
                "          color: #3C85D2;\r\n" +
                "          border-bottom: solid 3px #28588B;\r\n" +
                "          cursor: default;\r\n" +
                "        }      \r\n" +
                "\r\n" +
                "        .div_application_wait_time { \r\n" +
                "          border-top: solid 3px #C8C6FF;\r\n" +
                "          background-color: #B5B3E7;\r\n" +
                "          color: #B5B3E7;\r\n" +
                "          border-bottom: solid 3px #7C7B9E;\r\n" +
                "          cursor: default;\r\n" +
                "        }      \r\n" +
                "\r\n" +
                "       .div_user_io_wait_time { \r\n" +
                "          border-top: solid 3px #FF8C6C;\r\n" +
                "          background-color: #E27C60;\r\n" +
                "          color: #E27C60;\r\n" +
                "          border-bottom: solid 3px #B1614B;\r\n" +
                "          cursor: default;\r\n" +
                "        }\r\n" +
                "\r\n" +
                "       .div_SystemIO { \r\n" +
                "          border-top: solid 3px #49A1FF;\r\n" +
                "          background-color: #3C85D2;\r\n" +
                "          color: #3C85D2;\r\n" +
                "          border-bottom: solid 3px #28588B;\r\n" +
                "          cursor: default;\r\n" +
                "        }      \r\n" +
                "\r\n" +
                "        .div_Concurrency { \r\n" +
                "          border-top: solid 3px #E81B1F;\r\n" +
                "          background-color: #BE1619;\r\n" +
                "          color: #BE1619;\r\n" +
                "          border-bottom: solid 3px #760E10;\r\n" +
                "          cursor: default;\r\n" +
                "        }      \r\n" +
                "\r\n" +
                "        .div_concurrency_wait_time { \r\n" +
                "          border-top: solid 3px #E81B1F;\r\n" +
                "          background-color: #BE1619;\r\n" +
                "          color: #BE1619;\r\n" +
                "          border-bottom: solid 3px #760E10;\r\n" +
                "          cursor: default;\r\n" +
                "        }      \r\n" +
                "\r\n" +
                "        .div_cluster_wait_time { \r\n" +
                "          border-top: solid 3px #D43B61;\r\n" +
                "          background-color: #8B2740;\r\n" +
                "          color: #8B2740;\r\n" +
                "          border-bottom: solid 3px #5A1929;\r\n" +
                "          cursor: default;\r\n" +
                "        }      \r\n" +
                "\r\n" +
                "        .div_plsql_exec_time { \r\n" +
                "          border-top: solid 3px #EDD957;\r\n" +
                "          background-color: #B1A241;\r\n" +
                "          color: #B1A241;\r\n" +
                "          border-bottom: solid 3px #8B7F33;\r\n" +
                "          cursor: default;\r\n" +
                "        }      \r\n" +
                "\r\n" +
                "        .div_java_exec_time { \r\n" +
                "          border-top: solid 3px #B40FEB;\r\n" +
                "          background-color: #8E0CB9;\r\n" +
                "          color: #8E0CB9;\r\n" +
                "          border-bottom: solid 3px #6A098B;\r\n" +
                "          cursor: default;\r\n" +
                "        }\r\n" +
                "\r\n" +
                "        .div_other_wait_time { \r\n" +
                "          border-top: solid 3px #4AFDEB;\r\n" +
                "          background-color: #41DECD;\r\n" +
                "          color: #41DECD;\r\n" +
                "          border-bottom: solid 3px #2B9389;\r\n" +
                "          cursor: default;\r\n" +
                "        }\r\n" +
                "\r\n" +
                "        .div_disk_reads { \r\n" +
                "          border-top: solid 3px #FFC846;\r\n" +
                "          background-color: #DDAD3D;\r\n" +
                "          color: #DDAD3D;\r\n" +
                "          border-bottom: solid 3px #B99133;\r\n" +
                "          cursor: default;\r\n" +
                "        }\r\n" +
                "\r\n" +
                "        .div_direct_writes { \r\n" +
                "          border-top: solid 3px #F76838;\r\n" +
                "          background-color: #BC4F2B;\r\n" +
                "          color: #BC4F2B;\r\n" +
                "          border-bottom: solid 3px #9C4223;\r\n" +
                "          cursor: default;\r\n" +
                "        }\r\n" +
                "\r\n" +
                "        .div_user_fetch_count { \r\n" +
                "          border-top: solid 3px #DFFF6C;\r\n" +
                "          background-color: #CAE762;\r\n" +
                "          color: #CAE762;\r\n" +
                "          border-bottom: solid 3px #9EB44C;\r\n" +
                "          cursor: default;\r\n" +
                "        }\r\n" +
                "\r\n" +
                "        .div_Other { \r\n" +
                "          border-top: solid 3px #4AFDEB;\r\n" +
                "          background-color: #41DECD;\r\n" +
                "          color: #41DECD;\r\n" +
                "          border-bottom: solid 3px #2B9389;\r\n" +
                "          cursor: default;\r\n" +
                "        }      \r\n" +
                "\r\n" +
                "        .progress_Disp { \r\n" +
                "          border-top: solid 3px #F59582;\r\n" +
                "          background-color: #E08877;\r\n" +
                "          text-align: center;\r\n" +
                "          border-bottom: solid 3px #B46D5F;\r\n" +
                "          cursor: default;\r\n" +
                "          height: 13px;\r\n" +
                "          float:left;\r\n" +
                "        }      \r\n" +
                "\r\n" +
                "        .progress_Nodisp { \r\n" +
                "          border-top: solid 3px #F59582;\r\n" +
                "          background-color: #E08877;\r\n" +
                "          color: #E08877;\r\n" +
                "          border-bottom: solid 3px #B46D5F;\r\n" +
                "          cursor: default;\r\n" +
                "          font-size: 1% ; color:#F2F2F2\r\n" +
                "          height: 13px; \r\n" +
                "          float:left;\r\n" +
                "        }\r\n" +
                "\r\n" +
                "        .pxtype_QC { \r\n" +
                "          border-top: solid 2px #FAA589;\r\n" +
                "          background-color: #E2967C;\r\n" +
                "          border-bottom: solid 2px #BC7C67;\r\n" +
                "          cursor: default;\r\n" +
                "        }      \r\n" +
                "\r\n" +
                "        .pxtype_S1 { \r\n" +
                "          border-top: solid 2px #88F3B8;\r\n" +
                "          background-color: #79D8A4;\r\n" +
                "          border-bottom: solid 2px #60AC82;\r\n" +
                "          cursor: default;\r\n" +
                "        }      \r\n" +
                "\r\n" +
                "        .pxtype_S2 { \r\n" +
                "          border-top: solid 2px #9DF5FB;\r\n" +
                "          background-color: #87D2D7;\r\n" +
                "          border-bottom: solid 2px #70AEB2;\r\n" +
                "          cursor: default;\r\n" +
                "        }      \r\n" +
                "\r\n" +
                "        .pxtype_Instance { \r\n" +
                "          border: solid 1px #FFFFFF;\r\n" +
                "          cursor: default;\r\n" +
                "        }      \r\n" +
                "\r\n" +
                "        .active_period { \r\n" +
                "          border-top: solid 3px #A2C8E4;\r\n" +
                "          background-color: #8CADC5;\r\n" +
                "          color: #8CADC5;\r\n" +
                "          border-bottom: solid 3px #617888;\r\n" +
                "          cursor: default;\r\n" +
                "        }      \r\n" +
                "\r\n" +
                "        a.info span {\r\n" +
                "          display: none;\r\n" +
                "        }\r\n" +
                "\r\n" +
                "        a.info:hover {\r\n" +
                "          position: relative;\r\n" +
                "        }\r\n" +
                "\r\n" +
                "        a.info:hover span {\r\n" +
                "          display: block;\r\n" +
                "          position: absolute;\r\n" +
                "          border: thin solid black;\r\n" +
                "          background-color: #FFFF99;\r\n" +
                "        }\r\n" +
                "\r\n" +
                "       </style>\r\n" +
                "  </head>\r\n" +
                "  <body bgcolor=\"#FFFFFF\">\r\n" +
                "    <h1 align=\"center\">SQL Monitoring List</h1>\r\n" +
                "    <br/>\r\n" +
                "    <br/>\r\n" +
                "    <table class=\"s17\" border=\"1\" ora_borderstyle=\"headeronly\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\r\n" +
                "      <tr class=\"s16\">\r\n" +
                "        <th>\r\n" +
                "           Status\r\n" +
                "         </th>\r\n" +
                "        <th width=\"120\">\r\n" +
                "           Duration\r\n" +
                "         </th>\r\n" +
                "        <th>\r\n" +
                "           SQL Id\r\n" +
                "         </th>\r\n" +
                "        <th>\r\n" +
                "           User\r\n" +
                "         </th>\r\n" +
                "        <th>\r\n" +
                "           Dop\r\n" +
                "         </th>\r\n" +
                "        <th width=\"120\">\r\n" +
                "           DB Time\r\n" +
                "         </th>\r\n" +
                "        <th width=\"120\">\r\n" +
                "           IOs\r\n" +
                "         </th>\r\n" +
                "        <th>\r\n" +
                "           Start\r\n" +
                "         </th>\r\n" +
                "        <th>\r\n" +
                "           End\r\n" +
                "         </th>\r\n" +
                "        <th width=\"30%\">\r\n" +
                "           SQL Text\r\n" +
                "         </th>\r\n" +
                "      </tr>\r\n" +
                "      <tr width=\"100%\">\r\n" +
                "        <td>\r\n" +
                "          <a>DONE (ALL ROWS)</a>\r\n" +
                "        </td>\r\n" +
                "        <td>\r\n" +
                "          <table cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\r\n" +
                "            <tbody>\r\n" +
                "              <td align=\"left\">0.00s</td>\r\n" +
                "            </tbody>\r\n" +
                "          </table>\r\n" +
                "        </td>\r\n" +
                "        <td align=\"center\">\r\n" +
                "          <a class=\"info\">\r\n" +
                "                        7gujagv9a2a34\r\n" +
                "                        <span>Exec_id:16777219</span>\r\n" +
                "          </a>\r\n" +
                "        </td>\r\n" +
                "        <td align=\"center\">\r\n" +
                "          <a class=\"info\">\r\n" +
                "                        COPPER2\r\n" +
                "                        <span>\r\n" +
                "                            UId:77\r\n" +
                "                            <br/>\r\n" +
                "                            Sid:7:7562\r\n" +
                "                            <br/>\r\n" +
                "                            Mod/Act:JDBC Thin Client/-\r\n" +
                "                            <br/>\r\n" +
                "                            Prg:JDBC Thin Client\r\n" +
                "                            <br/>\r\n" +
                "                            Svc:SYS$USERS\r\n" +
                "                            <br/>\r\n" +
                "            </span>\r\n" +
                "          </a>\r\n" +
                "        </td>\r\n" +
                "        <td align=\"center\">\r\n" +
                "          <a class=\"info\"/>\r\n" +
                "        </td>\r\n" +
                "        <td>\r\n" +
                "          <table cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\r\n" +
                "            <tbody>\r\n" +
                "              <td align=\"left\">0s</td>\r\n" +
                "            </tbody>\r\n" +
                "          </table>\r\n" +
                "        </td>\r\n" +
                "        <td>\r\n" +
                "          <table cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\r\n" +
                "            <tbody>\r\n" +
                "              <td align=\"left\"/>\r\n" +
                "            </tbody>\r\n" +
                "          </table>\r\n" +
                "        </td>\r\n" +
                "        <td align=\"center\">06/24/2013 15:10:16</td>\r\n" +
                "        <td align=\"center\">06/24/2013 15:10:16</td>\r\n" +
                "        <td>\r\n" +
                "          <a class=\"info\">\r\n" +
                "                        SELECT /*+ MONITOR */  * FROM DUAL\r\n" +
                "                        <span>SELECT /*+ MONITOR */  * FROM DUAL</span>\r\n" +
                "          </a>\r\n" +
                "        </td>\r\n" +
                "      </tr>\r\n" +
                "      <tr width=\"100%\">\r\n" +
                "        <td>\r\n" +
                "          <a>DONE (ALL ROWS)</a>\r\n" +
                "        </td>\r\n" +
                "        <td>\r\n" +
                "          <table cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\r\n" +
                "            <tbody>\r\n" +
                "              <td align=\"left\">0.00s</td>\r\n" +
                "            </tbody>\r\n" +
                "          </table>\r\n" +
                "        </td>\r\n" +
                "        <td align=\"center\">\r\n" +
                "          <a class=\"info\">\r\n" +
                "                        7gujagv9a2a34\r\n" +
                "                        <span>Exec_id:16777218</span>\r\n" +
                "          </a>\r\n" +
                "        </td>\r\n" +
                "        <td align=\"center\">\r\n" +
                "          <a class=\"info\">\r\n" +
                "                        COPPER2\r\n" +
                "                        <span>\r\n" +
                "                            UId:77\r\n" +
                "                            <br/>\r\n" +
                "                            Sid:4:177\r\n" +
                "                            <br/>\r\n" +
                "                            Mod/Act:JDBC Thin Client/-\r\n" +
                "                            <br/>\r\n" +
                "                            Prg:JDBC Thin Client\r\n" +
                "                            <br/>\r\n" +
                "                            Svc:SYS$USERS\r\n" +
                "                            <br/>\r\n" +
                "            </span>\r\n" +
                "          </a>\r\n" +
                "        </td>\r\n" +
                "        <td align=\"center\">\r\n" +
                "          <a class=\"info\"/>\r\n" +
                "        </td>\r\n" +
                "        <td>\r\n" +
                "          <table cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\r\n" +
                "            <tbody>\r\n" +
                "              <td align=\"left\">0s</td>\r\n" +
                "            </tbody>\r\n" +
                "          </table>\r\n" +
                "        </td>\r\n" +
                "        <td>\r\n" +
                "          <table cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\r\n" +
                "            <tbody>\r\n" +
                "              <td align=\"left\"/>\r\n" +
                "            </tbody>\r\n" +
                "          </table>\r\n" +
                "        </td>\r\n" +
                "        <td align=\"center\">06/24/2013 15:09:56</td>\r\n" +
                "        <td align=\"center\">06/24/2013 15:09:56</td>\r\n" +
                "        <td>\r\n" +
                "          <a class=\"info\">\r\n" +
                "                        SELECT /*+ MONITOR */  * FROM DUAL\r\n" +
                "                        <span>SELECT /*+ MONITOR */  * FROM DUAL</span>\r\n" +
                "          </a>\r\n" +
                "        </td>\r\n" +
                "      </tr>\r\n" +
                "      <tr width=\"100%\">\r\n" +
                "        <td>\r\n" +
                "          <a>DONE (ALL ROWS)</a>\r\n" +
                "        </td>\r\n" +
                "        <td>\r\n" +
                "          <table cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\r\n" +
                "            <tbody>\r\n" +
                "              <td align=\"left\">0.00s</td>\r\n" +
                "            </tbody>\r\n" +
                "          </table>\r\n" +
                "        </td>\r\n" +
                "        <td align=\"center\">\r\n" +
                "          <a class=\"info\">\r\n" +
                "                        7gujagv9a2a34\r\n" +
                "                        <span>Exec_id:16777217</span>\r\n" +
                "          </a>\r\n" +
                "        </td>\r\n" +
                "        <td align=\"center\">\r\n" +
                "          <a class=\"info\">\r\n" +
                "                        COPPER2\r\n" +
                "                        <span>\r\n" +
                "                            UId:77\r\n" +
                "                            <br/>\r\n" +
                "                            Sid:40:110\r\n" +
                "                            <br/>\r\n" +
                "                            Mod/Act:JDBC Thin Client/-\r\n" +
                "                            <br/>\r\n" +
                "                            Prg:JDBC Thin Client\r\n" +
                "                            <br/>\r\n" +
                "                            Svc:SYS$USERS\r\n" +
                "                            <br/>\r\n" +
                "            </span>\r\n" +
                "          </a>\r\n" +
                "        </td>\r\n" +
                "        <td align=\"center\">\r\n" +
                "          <a class=\"info\"/>\r\n" +
                "        </td>\r\n" +
                "        <td>\r\n" +
                "          <table cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\r\n" +
                "            <tbody>\r\n" +
                "              <td align=\"left\">0s</td>\r\n" +
                "            </tbody>\r\n" +
                "          </table>\r\n" +
                "        </td>\r\n" +
                "        <td>\r\n" +
                "          <table cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\r\n" +
                "            <tbody>\r\n" +
                "              <td align=\"left\"/>\r\n" +
                "            </tbody>\r\n" +
                "          </table>\r\n" +
                "        </td>\r\n" +
                "        <td align=\"center\">06/24/2013 15:09:38</td>\r\n" +
                "        <td align=\"center\">06/24/2013 15:09:38</td>\r\n" +
                "        <td>\r\n" +
                "          <a class=\"info\">\r\n" +
                "                        SELECT /*+ MONITOR */  * FROM DUAL\r\n" +
                "                        <span>SELECT /*+ MONITOR */  * FROM DUAL</span>\r\n" +
                "          </a>\r\n" +
                "        </td>\r\n" +
                "      </tr>\r\n" +
                "      <tr width=\"100%\">\r\n" +
                "        <td>\r\n" +
                "          <a>DONE (ALL ROWS)</a>\r\n" +
                "        </td>\r\n" +
                "        <td>\r\n" +
                "          <table cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\r\n" +
                "            <tbody>\r\n" +
                "              <td align=\"left\">0.00s</td>\r\n" +
                "            </tbody>\r\n" +
                "          </table>\r\n" +
                "        </td>\r\n" +
                "        <td align=\"center\">\r\n" +
                "          <a class=\"info\">\r\n" +
                "                        7gujagv9a2a34\r\n" +
                "                        <span>Exec_id:16777216</span>\r\n" +
                "          </a>\r\n" +
                "        </td>\r\n" +
                "        <td align=\"center\">\r\n" +
                "          <a class=\"info\">\r\n" +
                "                        COPPER2\r\n" +
                "                        <span>\r\n" +
                "                            UId:77\r\n" +
                "                            <br/>\r\n" +
                "                            Sid:223:9163\r\n" +
                "                            <br/>\r\n" +
                "                            Mod/Act:JDBC Thin Client/-\r\n" +
                "                            <br/>\r\n" +
                "                            Prg:JDBC Thin Client\r\n" +
                "                            <br/>\r\n" +
                "                            Svc:SYS$USERS\r\n" +
                "                            <br/>\r\n" +
                "            </span>\r\n" +
                "          </a>\r\n" +
                "        </td>\r\n" +
                "        <td align=\"center\">\r\n" +
                "          <a class=\"info\"/>\r\n" +
                "        </td>\r\n" +
                "        <td>\r\n" +
                "          <table cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\r\n" +
                "            <tbody>\r\n" +
                "              <td align=\"left\">0s</td>\r\n" +
                "            </tbody>\r\n" +
                "          </table>\r\n" +
                "        </td>\r\n" +
                "        <td>\r\n" +
                "          <table cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\r\n" +
                "            <tbody>\r\n" +
                "              <td align=\"left\"/>\r\n" +
                "            </tbody>\r\n" +
                "          </table>\r\n" +
                "        </td>\r\n" +
                "        <td align=\"center\">06/24/2013 15:09:26</td>\r\n" +
                "        <td align=\"center\">06/24/2013 15:09:26</td>\r\n" +
                "        <td>\r\n" +
                "          <a class=\"info\">\r\n" +
                "                        SELECT /*+ MONITOR */  * FROM DUAL\r\n" +
                "                        <span>SELECT /*+ MONITOR */  * FROM DUAL</span>\r\n" +
                "          </a>\r\n" +
                "        </td>\r\n" +
                "      </tr>\r\n" +
                "      <tr width=\"100%\">\r\n" +
                "        <td>\r\n" +
                "          <a>DONE (ALL ROWS)</a>\r\n" +
                "        </td>\r\n" +
                "        <td>\r\n" +
                "          <table cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\r\n" +
                "            <tbody>\r\n" +
                "              <td style=\"          width:40%        \">\r\n" +
                "                <table class=\"graph\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\r\n" +
                "                  <tbody>\r\n" +
                "                    <tr>\r\n" +
                "                      <td class=\"bar\">\r\n" +
                "                        <div class=\"div_duration\" style=\"                     width:100%                   \" title=\"duration - 10s\">\r\n" +
                "                          <P class=\"nodisp\">.</P>\r\n" +
                "                        </div>\r\n" +
                "                      </td>\r\n" +
                "                    </tr>\r\n" +
                "                  </tbody>\r\n" +
                "                </table>\r\n" +
                "              </td>\r\n" +
                "              <td align=\"left\">10s</td>\r\n" +
                "            </tbody>\r\n" +
                "          </table>\r\n" +
                "        </td>\r\n" +
                "        <td align=\"center\">\r\n" +
                "          <a class=\"info\">\r\n" +
                "                        1rtwsxvw83x7s\r\n" +
                "                        <span>Exec_id:16777216</span>\r\n" +
                "          </a>\r\n" +
                "        </td>\r\n" +
                "        <td align=\"center\">\r\n" +
                "          <a class=\"info\">\r\n" +
                "                        HUBMANAGER\r\n" +
                "                        <span>\r\n" +
                "                            UId:72\r\n" +
                "                            <br/>\r\n" +
                "                            Sid:99:1\r\n" +
                "                            <br/>\r\n" +
                "                            Prg:ORACLE.EXE (J007)\r\n" +
                "                            <br/>\r\n" +
                "                            Svc:SYS$USERS\r\n" +
                "                            <br/>\r\n" +
                "            </span>\r\n" +
                "          </a>\r\n" +
                "        </td>\r\n" +
                "        <td align=\"center\">\r\n" +
                "          <a class=\"info\"/>\r\n" +
                "        </td>\r\n" +
                "        <td>\r\n" +
                "          <table cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\r\n" +
                "            <tbody>\r\n" +
                "              <td style=\"          width:37%        \">\r\n" +
                "                <table class=\"graph\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\r\n" +
                "                  <tbody>\r\n" +
                "                    <tr>\r\n" +
                "                      <td class=\"bar\">\r\n" +
                "                        <div class=\"div_cpu_time\" style=\"                     width:99%                   \" title=\"Cpu - 9.48s (99%)\">\r\n" +
                "                          <P class=\"nodisp\">.</P>\r\n" +
                "                        </div>\r\n" +
                "                      </td>\r\n" +
                "                    </tr>\r\n" +
                "                  </tbody>\r\n" +
                "                </table>\r\n" +
                "              </td>\r\n" +
                "              <td align=\"left\">10s</td>\r\n" +
                "            </tbody>\r\n" +
                "          </table>\r\n" +
                "        </td>\r\n" +
                "        <td>\r\n" +
                "          <table cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\r\n" +
                "            <tbody>\r\n" +
                "              <td style=\"          width:22%        \">\r\n" +
                "                <table class=\"graph\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\r\n" +
                "                  <tbody>\r\n" +
                "                    <tr>\r\n" +
                "                      <td class=\"bar\">\r\n" +
                "                        <div class=\"div_read_reqs\" style=\"                     width:100%                   \" title=\"Read Requests: 109 = 3MB\">\r\n" +
                "                          <P class=\"nodisp\">.</P>\r\n" +
                "                        </div>\r\n" +
                "                      </td>\r\n" +
                "                    </tr>\r\n" +
                "                  </tbody>\r\n" +
                "                </table>\r\n" +
                "              </td>\r\n" +
                "              <td align=\"left\">109</td>\r\n" +
                "            </tbody>\r\n" +
                "          </table>\r\n" +
                "        </td>\r\n" +
                "        <td align=\"center\">06/24/2013 09:44:10</td>\r\n" +
                "        <td align=\"center\">06/24/2013 09:44:20</td>\r\n" +
                "        <td>\r\n" +
                "          <a class=\"info\">\r\n" +
                "                        SELECT I.COLUMN_NAME, HISTTAB.TABLE_NAME FROM USER_IND_COLUMNS I, USER_TAB_COLUM...\r\n" +
                "                        <span>SELECT I.COLUMN_NAME, HISTTAB.TABLE_NAME FROM USER_IND_COLUMNS I, USER_TAB_COLUMNS T, USER_TABLES HISTTAB WHERE INDEX_NAME IN ( SELECT INDEX_NAME FROM USER_IND_COLUMNS WHERE INDEX_NAME IN ( SELECT INDEX_NAME FROM USER_CONSTRAINTS WHERE CONSTRAINT_TYPE = &apos;P&apos;) GROUP BY INDEX_NAME HAVING COUNT(1) = 1 ) AND T.COLUMN_NAME = I.COLUMN_NAME AND T.TABLE_NAME = I.TABLE_NAME AND T.DATA_TYPE = &apos;NUMBER&apos; AND T.DATA_PRECISION &lt;= 18 AND T.DATA_SCALE = 0 AND HISTTAB.TABLE_NAME = &apos;H_&apos;||T.TABLE_NAME</span>\r\n" +
                "          </a>\r\n" +
                "        </td>\r\n" +
                "      </tr>\r\n" +
                "      <tr width=\"100%\">\r\n" +
                "        <td>\r\n" +
                "          <a>DONE</a>\r\n" +
                "        </td>\r\n" +
                "        <td>\r\n" +
                "          <table cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\r\n" +
                "            <tbody>\r\n" +
                "              <td style=\"          width:60%        \">\r\n" +
                "                <table class=\"graph\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\r\n" +
                "                  <tbody>\r\n" +
                "                    <tr>\r\n" +
                "                      <td class=\"bar\">\r\n" +
                "                        <div class=\"div_duration\" style=\"                     width:100%                   \" title=\"duration - 15s\">\r\n" +
                "                          <P class=\"nodisp\">.</P>\r\n" +
                "                        </div>\r\n" +
                "                      </td>\r\n" +
                "                    </tr>\r\n" +
                "                  </tbody>\r\n" +
                "                </table>\r\n" +
                "              </td>\r\n" +
                "              <td align=\"left\">15s</td>\r\n" +
                "            </tbody>\r\n" +
                "          </table>\r\n" +
                "        </td>\r\n" +
                "        <td align=\"center\">\r\n" +
                "          <a class=\"info\">\r\n" +
                "                        csp5671rfba0g\r\n" +
                "                        <span>Exec_id:16777216</span>\r\n" +
                "          </a>\r\n" +
                "        </td>\r\n" +
                "        <td align=\"center\">\r\n" +
                "          <a class=\"info\">\r\n" +
                "                        HUBMANAGER\r\n" +
                "                        <span>\r\n" +
                "                            UId:72\r\n" +
                "                            <br/>\r\n" +
                "                            Sid:99:1\r\n" +
                "                            <br/>\r\n" +
                "                            Prg:ORACLE.EXE (J007)\r\n" +
                "                            <br/>\r\n" +
                "                            Svc:SYS$USERS\r\n" +
                "                            <br/>\r\n" +
                "            </span>\r\n" +
                "          </a>\r\n" +
                "        </td>\r\n" +
                "        <td align=\"center\">\r\n" +
                "          <a class=\"info\"/>\r\n" +
                "        </td>\r\n" +
                "        <td>\r\n" +
                "          <table cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\r\n" +
                "            <tbody>\r\n" +
                "              <td style=\"          width:60%        \">\r\n" +
                "                <table class=\"graph\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\r\n" +
                "                  <tbody>\r\n" +
                "                    <tr>\r\n" +
                "                      <td class=\"bar\">\r\n" +
                "                        <div class=\"div_cpu_time\" style=\"                     width:98%                   \" title=\"Sql Cpu Time - 15.5s (98%)\">\r\n" +
                "                          <P class=\"nodisp\">.</P>\r\n" +
                "                        </div>\r\n" +
                "                      </td>\r\n" +
                "                    </tr>\r\n" +
                "                  </tbody>\r\n" +
                "                </table>\r\n" +
                "              </td>\r\n" +
                "              <td align=\"left\">16s</td>\r\n" +
                "            </tbody>\r\n" +
                "          </table>\r\n" +
                "        </td>\r\n" +
                "        <td>\r\n" +
                "          <table cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\r\n" +
                "            <tbody>\r\n" +
                "              <td style=\"          width:60%        \">\r\n" +
                "                <table class=\"graph\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\r\n" +
                "                  <tbody>\r\n" +
                "                    <tr>\r\n" +
                "                      <td class=\"bar\">\r\n" +
                "                        <div class=\"div_read_reqs\" style=\"                     width:100%                   \" title=\"Read Requests: 310 = 5MB\">\r\n" +
                "                          <P class=\"nodisp\">.</P>\r\n" +
                "                        </div>\r\n" +
                "                      </td>\r\n" +
                "                    </tr>\r\n" +
                "                  </tbody>\r\n" +
                "                </table>\r\n" +
                "              </td>\r\n" +
                "              <td align=\"left\">310</td>\r\n" +
                "            </tbody>\r\n" +
                "          </table>\r\n" +
                "        </td>\r\n" +
                "        <td align=\"center\">06/24/2013 09:44:05</td>\r\n" +
                "        <td align=\"center\">06/24/2013 09:44:20</td>\r\n" +
                "        <td>\r\n" +
                "          <a class=\"info\">\r\n" +
                "                        DECLARE job BINARY_INTEGER := :job; next_date DATE := :mydate;  broken BOOLEAN :...\r\n" +
                "                        <span>DECLARE job BINARY_INTEGER := :job; next_date DATE := :mydate;  broken BOOLEAN := FALSE; BEGIN begin HOUSEKEEPING; end; :mydate := next_date; IF broken THEN :b := 1; ELSE :b := 0; END IF; END; </span>\r\n" +
                "          </a>\r\n" +
                "        </td>\r\n" +
                "      </tr>\r\n" +
                "    </table>\r\n" +
                "  </body>\r\n" +
                "</html>\r\n" +
                "";
    }

    @Override
    public String getDatabaseMonitoringHtmlDetailReport(String sqlid) throws RemoteException {
        return "";
    }

    @Override
    public String getDatabaseMonitoringRecommendationsReport(String sqlid) throws RemoteException {
        return "";
    }

    @Override
    public Date getMonitoringDataMinDate() throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getMonitoringDataMaxDate() throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T, R extends Serializable> List<List<R>> createStatistic(MonitoringDataFilter<T> filter,
            List<StatisticCreator<T, R>> statisticCreator, Date from, Date to) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> List<T> getList(MonitoringDataFilter<T> filter, Date from, Date to, long maxCount) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MonitoringDataProviderInfo> getMonitoringDataProviderInfos() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void startMonitoringDataProvider(String name) throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void stopMonitoringDataProvider(String name) throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public MonitoringDataStorageInfo getMonitroingDataStorageInfo() {
        // TODO Auto-generated method stub
        return null;
    }

}
