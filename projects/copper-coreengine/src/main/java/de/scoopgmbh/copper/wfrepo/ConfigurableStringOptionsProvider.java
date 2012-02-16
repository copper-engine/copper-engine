package de.scoopgmbh.copper.wfrepo;

import java.util.Collections;
import java.util.List;

public class ConfigurableStringOptionsProvider implements CompilerOptionsProvider {
	
	private volatile List<String> options = Collections.emptyList();

	public void setOptions(List<String> options) {
		this.options = options;
	}

	@Override
	public List<String> getOptions() {
		return options;
	}

}
