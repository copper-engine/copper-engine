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
package de.scoopgmbh.copper.monitoring.client.ui.repository.result;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import de.scoopgmbh.copper.monitoring.client.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterResultControllerBase;
import de.scoopgmbh.copper.monitoring.client.ui.repository.filter.WorkflowRepositoryFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.workflowclasssesctree.WorkflowClassesTreeController;
import de.scoopgmbh.copper.monitoring.client.ui.workflowclasssesctree.WorkflowClassesTreeController.DisplayWorkflowClassesModel;
import de.scoopgmbh.copper.monitoring.client.util.CodeMirrorFormatter;
import de.scoopgmbh.copper.monitoring.client.util.CodeMirrorFormatter.CodeFormatLanguage;
import de.scoopgmbh.copper.monitoring.client.util.ComponentUtil;
import de.scoopgmbh.copper.monitoring.client.util.WorkflowVersion;

public class WorkflowRepositoryResultController extends FilterResultControllerBase<WorkflowRepositoryFilterModel,WorkflowVersion> implements Initializable {
	
	private final GuiCopperDataProvider copperDataProvider;
	private final WorkflowRepositoryDependencyFactory workflowRepositoryDependencyFactory;
	private final CodeMirrorFormatter codeMirrorFormatter;
	private WorkflowClassesTreeController workflowClassesTreeController;
	
	public WorkflowRepositoryResultController(GuiCopperDataProvider copperDataProvider, WorkflowRepositoryDependencyFactory workflowRepositoryDependencyFactory, CodeMirrorFormatter codeMirrorFormatter) {
		super();
		this.copperDataProvider = copperDataProvider;
		this.workflowRepositoryDependencyFactory = workflowRepositoryDependencyFactory;
		this.codeMirrorFormatter = codeMirrorFormatter;
	}

    @FXML //  fx:id="detailStackPane"
    private StackPane detailStackPane; // Value injected by FXMLLoader

    @FXML //  fx:id="search"
    private TextField search; // Value injected by FXMLLoader

    @FXML //  fx:id="workflowView"
    private TreeView<DisplayWorkflowClassesModel> workflowView; // Value injected by FXMLLoader

    @FXML //  fx:id="sourceView"
    private WebView sourceView; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
    	assert detailStackPane != null : "fx:id=\"detailStackPane\" was not injected: check your FXML file 'WorkflowRepositoryResult.fxml'.";
    	assert search != null : "fx:id=\"search\" was not injected: check your FXML file 'WorkflowRepositoryResult.fxml'.";
        assert sourceView != null : "fx:id=\"sourceView\" was not injected: check your FXML file 'WorkflowRepositoryResult.fxml'.";
        assert workflowView != null : "fx:id=\"workflowView\" was not injected: check your FXML file 'WorkflowRepositoryResult.fxml'.";

        workflowClassesTreeController = workflowRepositoryDependencyFactory.createWorkflowClassesTreeController(workflowView);
        workflowClassesTreeController.selectedItem.addListener(new ChangeListener<WorkflowVersion>() {
			@Override
			public void changed(ObservableValue<? extends WorkflowVersion> observable, WorkflowVersion oldValue, WorkflowVersion newValue) {
				sourceView.getEngine().loadContent(codeMirrorFormatter.format(newValue.source.get(), CodeFormatLanguage.JAVA,true));
				ComponentUtil.startValueSetAnimation(detailStackPane);
			}
		});
        
        search.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (newValue!=null){
					TreeItem<DisplayWorkflowClassesModel> item = search(workflowView.getRoot(),newValue);
					if (item!=null){
						workflowView.getSelectionModel().select(item);	
					} else {
						workflowView.getSelectionModel().clearSelection();
					}
				}
				if (newValue==null){
					workflowView.getSelectionModel().clearSelection();
				}
			}
		});
    }
    
    private TreeItem<DisplayWorkflowClassesModel> search(TreeItem<DisplayWorkflowClassesModel> item, String regex){
    	Pattern p = Pattern.compile(regex, Pattern.DOTALL | Pattern.MULTILINE);
    	for (TreeItem<DisplayWorkflowClassesModel> child: item.getChildren()){
    		if (child.getValue().displayname!=null && p.matcher(child.getValue().displayname).matches()){
    			return child;
    		} else {
    			return search(child,regex);
    		}
    	}
		return null;
    }
    
	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("WorkflowRepositoryResult.fxml");
	}

	@Override
	public void showFilteredResult(List<WorkflowVersion> filteredResult, WorkflowRepositoryFilterModel usedFilter) {
		workflowClassesTreeController.refresh(filteredResult);
	}

	@Override
	public List<WorkflowVersion> applyFilterInBackgroundThread(WorkflowRepositoryFilterModel filter) {
		return copperDataProvider.getWorkflowClassesList(filter.enginePoolModel.selectedEngine.get().getId());  
	}
	
	@Override
	public void clear() {
		if (workflowView.getRoot()!=null){
			workflowView.getRoot().getChildren().clear();
		}
	}

}
