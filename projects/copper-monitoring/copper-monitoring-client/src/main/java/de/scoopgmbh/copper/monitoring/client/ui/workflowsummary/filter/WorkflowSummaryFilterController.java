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
package de.scoopgmbh.copper.monitoring.client.ui.workflowsummary.filter;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import de.scoopgmbh.copper.monitoring.client.context.FormContext;
import de.scoopgmbh.copper.monitoring.client.form.FxmlController;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterController;
import de.scoopgmbh.copper.monitoring.client.form.filter.defaultfilter.DefaultFilterFactory;
import de.scoopgmbh.copper.monitoring.client.form.filter.enginefilter.BaseEngineFilterController;
import de.scoopgmbh.copper.monitoring.client.util.ComponentUtil;
import de.scoopgmbh.copper.monitoring.client.util.WorkflowVersion;
import de.scoopgmbh.copper.monitoring.core.model.ProcessingEngineInfo;

public class WorkflowSummaryFilterController extends BaseEngineFilterController<WorkflowSummaryFilterModel> implements Initializable, FxmlController {
	private final FormContext formContext;

	public WorkflowSummaryFilterController(FormContext formContext, List<ProcessingEngineInfo> availableEngines) {
		super(availableEngines,new WorkflowSummaryFilterModel());
		this.formContext = formContext;
	}
	
	public void setFilter(WorkflowVersion workflowVersion){
		model.version.setAllFrom(workflowVersion);
	}
	
    @FXML //  fx:id="searchMenueItem"
    private CustomMenuItem searchMenueItem; // Value injected by FXMLLoader

    @FXML //  fx:id="serachbutton"
    private MenuButton serachbutton; // Value injected by FXMLLoader

    @FXML //  fx:id="workflowClass"
    private TextField workflowClass; // Value injected by FXMLLoader
    
    @FXML
    private StackPane stackPane;
    
    @FXML
    private Pane filterPane;

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert searchMenueItem != null : "fx:id=\"searchMenueItem\" was not injected: check your FXML file 'WorkflowSummeryFilter.fxml'.";
        assert serachbutton != null : "fx:id=\"serachbutton\" was not injected: check your FXML file 'WorkflowSummeryFilter.fxml'.";
        assert workflowClass != null : "fx:id=\"workflowClass\" was not injected: check your FXML file 'WorkflowSummeryFilter.fxml'.";
        assert stackPane != null : "fx:id=\"stackPane\" was not injected: check your FXML file 'WorkflowSummeryFilter.fxml'.";
        assert filterPane !=null;
        
        workflowClass.textProperty().bindBidirectional(model.version.classname );
        
        searchMenueItem.setContent(formContext.createWorkflowClassesTreeForm(this).createContent());
        serachbutton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/de/scoopgmbh/copper/gui/icon/search.png"))));
        
        searchMenueItem.getStyleClass().setAll("noSelectAnimationMenueItem","menu-item");
	}

	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("WorkflowSummaryFilter.fxml");
	}

	@Override
	public boolean supportsFiltering() {
		return true;
	}
	
	public void startValueSetAnimation() {
		ComponentUtil.startValueSetAnimation(stackPane);
	}
	
	@Override
	public long getDefaultRefreshIntervall() {
		return FilterController.DEFAULT_REFRESH_INTERVALL;
	}

	@Override
	public Node createAdditionalFilter() {
		return new DefaultFilterFactory().createMaxCount(model.maxCountFilterModel);
	}
	
	
}
