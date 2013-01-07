package de.scoopgmbh.copper.gui.ui.worklowinstancedetail.result;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.Initializable;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.gui.form.filter.FilterResultController;
import de.scoopgmbh.copper.gui.ui.worklowinstancedetail.filter.WorkflowInstanceDetailFilterModel;

public class WorkflowInstanceDetailResultController implements Initializable, FilterResultController<WorkflowInstanceDetailFilterModel>, FxmlController {
	GuiCopperDataProvider copperDataProvider;

	public WorkflowInstanceDetailResultController(GuiCopperDataProvider copperDataProvider) {
		super();
		this.copperDataProvider = copperDataProvider;
	}


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        
    }

	@Override
	public void applyFilter(WorkflowInstanceDetailFilterModel filter) {
		copperDataProvider.getWorkflowDetails(filter);
		//TODO
	}
	
	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("WorkflowInstanceDetailResult.fxml");
	}

}
