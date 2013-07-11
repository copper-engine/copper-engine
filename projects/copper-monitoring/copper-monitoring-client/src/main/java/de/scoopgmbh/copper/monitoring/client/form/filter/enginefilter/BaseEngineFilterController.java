package de.scoopgmbh.copper.monitoring.client.form.filter.enginefilter;

import java.util.List;

import javafx.scene.Node;
import de.scoopgmbh.copper.monitoring.client.form.filter.BaseFilterController;
import de.scoopgmbh.copper.monitoring.client.form.filter.defaultfilter.DefaultFilterFactory;
import de.scoopgmbh.copper.monitoring.core.model.ProcessingEngineInfo;

public abstract class BaseEngineFilterController<T extends EnginePoolFilterModel> extends BaseFilterController<T> {
	
	protected final T model;
	protected final List<ProcessingEngineInfo> availableEngines;
	
	public BaseEngineFilterController(List<ProcessingEngineInfo> availableEngines, T model) {
		super();
		this.model = model;
		this.availableEngines = availableEngines;
		
		getFilter().selectedEngine.set(availableEngines.get(0));
		getFilter().selectedPool.set(availableEngines.get(0).getPools().get(0));
	}

	@Override
	public Node createDefaultFilter() {
		DefaultFilterFactory defaultFilterFactory = new DefaultFilterFactory();
		final Node createAdditionalFilter = createAdditionalFilter();
		final Node engineFilterUI = defaultFilterFactory.createEngineFilterUI(getFilter(), availableEngines);
		
		
		if (createAdditionalFilter!=null){
			return defaultFilterFactory.createVerticalMultiFilter(createAdditionalFilter,engineFilterUI);
		} else {
			return defaultFilterFactory.createVerticalMultiFilter(engineFilterUI);
		}
	}
	
	public abstract Node createAdditionalFilter();
	
	@Override
	public T getFilter(){
		return model;
	}
	
}

