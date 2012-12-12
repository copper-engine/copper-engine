package de.scoopgmbh.copper.gui.adapter;

import java.util.List;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import de.scoopgmbh.copper.management.AuditTrailInfo;
import de.scoopgmbh.copper.management.ProcessingEngineMXBean;
import de.scoopgmbh.copper.management.WorkflowInfo;

public class JMXCopperDataProvider implements CopperDataProvider {
	
	public static void main(String[] args) {
		try {
			JMXServiceURL target = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9191/jmxrmi");
			JMXConnector connector = JMXConnectorFactory.connect(target);
			MBeanServerConnection remote = connector.getMBeanServerConnection();

			ObjectName bean = new ObjectName("copper.engine" + ":type="
					+ ProcessingEngineMXBean.class.getSimpleName());

			MBeanInfo info = remote.getMBeanInfo(bean);
			MBeanAttributeInfo[] attributes = info.getAttributes();
			for (MBeanAttributeInfo attr : attributes) {
				System.out.println(attr.getDescription() + " " + remote.getAttribute(bean, attr.getName()));
			}
			connector.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(0);
		}
	}
	
	public JMXCopperDataProvider(String serverAdress, String port) {
		try {
			JMXServiceURL target = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + serverAdress + ":" + port + "/jmxrmi");
			JMXConnector connector = JMXConnectorFactory.connect(target);
			MBeanServerConnection remote = connector.getMBeanServerConnection();

			ObjectName bean = new ObjectName(ProcessingEngineMXBean.class.getPackage().getName() + ":type="
					+ ProcessingEngineMXBean.class.getSimpleName());

			MBeanInfo info = remote.getMBeanInfo(bean);
			MBeanAttributeInfo[] attributes = info.getAttributes();
			for (MBeanAttributeInfo attr : attributes) {
				System.out.println(attr.getDescription() + " " + remote.getAttribute(bean, attr.getName()));
			}
			connector.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(0);
		}
	}
	
	
	@Override
	public int getWorkflowInstancesInfosCount() {
		return 0;
	}

	@Override
	public List<WorkflowInfo> getWorkflowInstancesInfos(int fromCount, int toCount) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getWorkflowInstancesInfosCount(String worklfowId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<WorkflowInfo> getWorkflowInstancesInfos(String worklfowId, int fromCount, int toCount) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowClassesInfo> getWorklowClassesInfos() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AuditTrailInfo> getAuditTrails(String transactionId, String conversationId, String correlationId, Integer level,
			int maxResult) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAuditTrailMessage(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorkflowInstanceMetaData getWorkflowInstanceMetaData(String worklfowId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getProcessorPoolNumberOfThreads() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getProcessorPoolThreadPriority() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getProcessorPoolMemoryQueueSize() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	
}
