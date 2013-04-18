package de.scoopgmbh.copper.gui.form.enginefilter;

import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import de.scoopgmbh.copper.gui.form.Widget;
import de.scoopgmbh.copper.monitor.core.adapter.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitor.core.adapter.model.ProcessorPoolInfo;
import de.scoopgmbh.copper.monitor.core.adapter.model.ProcessingEngineInfo.EngineTyp;

public class EngineSelectionWidget implements Widget{
	private final EnginePoolModel model;
	private final List<ProcessingEngineInfo> engineList;
	
	public EngineSelectionWidget(EnginePoolModel model, List<ProcessingEngineInfo> engineList){
		this.model=model;
		this.engineList = engineList;
	}

	@Override
	public Node createContent() {
		HBox pane = new HBox();
		pane.setAlignment(Pos.CENTER_LEFT);

		final ChoiceBox<ProcessingEngineInfo> engineChoicebox = createEngineChoicebox();

		final ChoiceBox<ProcessorPoolInfo> poolChoicebox = createPoolChoicebox();
		
		
		pane.getChildren().add(engineChoicebox);
		pane.getChildren().add(poolChoicebox);
		HBox.setMargin(engineChoicebox, new Insets(3));
		HBox.setMargin(poolChoicebox, new Insets(3));
		
		engineChoicebox.getSelectionModel().selectFirst();
		return pane;
	}

	public ChoiceBox<ProcessingEngineInfo> createEngineChoicebox() {
		final ChoiceBox<ProcessingEngineInfo> engineChoicebox = new ChoiceBox<ProcessingEngineInfo>();
		engineChoicebox.setTooltip(new Tooltip("ProcessingEngine"));
		for (ProcessingEngineInfo engineFilter: engineList){
			engineChoicebox.getItems().add(engineFilter);
		}
		engineChoicebox.setConverter(new StringConverter<ProcessingEngineInfo>() {
			@Override
			public String toString(ProcessingEngineInfo object) {
				return object.getId()+"("+(object.getTyp()==EngineTyp.PERSISTENT?"P":"T")+")";
			}
			
			@Override
			public ProcessingEngineInfo fromString(String string) {
				return null;
			}
		});
		engineChoicebox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ProcessingEngineInfo>() {
			@Override
			public void changed(ObservableValue<? extends ProcessingEngineInfo> observable, ProcessingEngineInfo oldValue,
					ProcessingEngineInfo newValue) {
				model.selectedEngine.setValue(newValue);
			}
		});
		model.selectedEngine.addListener(new ChangeListener<ProcessingEngineInfo>() {
			@Override
			public void changed(ObservableValue<? extends ProcessingEngineInfo> observable, ProcessingEngineInfo oldValue,
					ProcessingEngineInfo newValue) {
				for (ProcessingEngineInfo processingEngineInfo: engineChoicebox.getItems()){
					if (processingEngineInfo.getId()!=null && processingEngineInfo.getId().equals(newValue.getId())){
						engineChoicebox.getSelectionModel().select(processingEngineInfo);
					}
				}
			}
		});
		return engineChoicebox;
	}

	public ChoiceBox<ProcessorPoolInfo> createPoolChoicebox() {
		final ChoiceBox<ProcessorPoolInfo> poolChoicebox = new ChoiceBox<ProcessorPoolInfo>();
		poolChoicebox.setTooltip(new Tooltip("ProcessorPool"));
		
		model.selectedEngine.addListener(new ChangeListener<ProcessingEngineInfo>() {
			@Override
			public void changed(ObservableValue<? extends ProcessingEngineInfo> observable, ProcessingEngineInfo oldValue, ProcessingEngineInfo newValue) {
				if (newValue!=null){
					poolChoicebox.getItems().clear();
					for (ProcessorPoolInfo processorPoolInfo: newValue.getPools()){
						poolChoicebox.getItems().add(processorPoolInfo);
					}
					model.selectedPool.set(null);
					if (!newValue.getPools().isEmpty()){
						model.selectedPool.set(newValue.getPools().get(0));
					}
				}
			}
		});
		
		poolChoicebox.setConverter(new StringConverter<ProcessorPoolInfo>() {
			@Override
			public String toString(ProcessorPoolInfo object) {
				return object.getId();
			}
			
			@Override
			public ProcessorPoolInfo fromString(String string) {
				return null;
			}
		});
		
		poolChoicebox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ProcessorPoolInfo>() {
			@Override
			public void changed(ObservableValue<? extends ProcessorPoolInfo> observable, ProcessorPoolInfo oldValue,
					ProcessorPoolInfo newValue) {
				model.selectedPool.setValue(newValue);
			}
		});
		
		model.selectedPool.addListener(new ChangeListener<ProcessorPoolInfo>() {
			@Override
			public void changed(ObservableValue<? extends ProcessorPoolInfo> observable, ProcessorPoolInfo oldValue,
					ProcessorPoolInfo newValue) {
				for (ProcessorPoolInfo pool: poolChoicebox.getItems()){
					if (pool.getId()!=null && pool.getId().equals(newValue.getId())){
						poolChoicebox.getSelectionModel().select(pool);
					}
				}
			}
		});
		return poolChoicebox;
	}
}
