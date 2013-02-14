package de.scoopgmbh.copper.instrument;

import java.io.Serializable;
import java.util.List;

public class ClassInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	final List<MethodInfo> methodInfos;
	
	public ClassInfo(List<MethodInfo> methodInfos) {
		this.methodInfos = methodInfos;
	}
	
	public List<MethodInfo> getMethodInfos() {
		return methodInfos;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(2000);
		sb.append("methods: \n");
		for (MethodInfo info : methodInfos) {
			sb.append("\t").append(info).append('\n');
		}
		return sb.toString();
	}
	
}