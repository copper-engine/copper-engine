package de.scoopgmbh.copper.gui.ui.load.result;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.gui.form.filter.FilterResultController;
import de.scoopgmbh.copper.gui.ui.load.filter.EngineLoadFilterModel;
import de.scoopgmbh.copper.monitor.adapter.model.CopperLoadInfo;

public class EngineLoadResultController implements Initializable, FilterResultController<EngineLoadFilterModel,CopperLoadInfo>, FxmlController {
	private final GuiCopperDataProvider copperDataProvider;
	
	public EngineLoadResultController(GuiCopperDataProvider copperDataProvider) {
		super();
		this.copperDataProvider = copperDataProvider;
	}

    @FXML //  fx:id="areaChart"
    private AreaChart<String, Number> areaChart; // Value injected by FXMLLoader

    @FXML //  fx:id="categoryAxis"
    private CategoryAxis categoryAxis; // Value injected by FXMLLoader

    @FXML //  fx:id="numberAxis"
    private NumberAxis numberAxis; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert areaChart != null : "fx:id=\"areaChart\" was not injected: check your FXML file 'EngineLoadResult.fxml'.";
        assert categoryAxis != null : "fx:id=\"categoryAxis\" was not injected: check your FXML file 'EngineLoadResult.fxml'.";
        assert numberAxis != null : "fx:id=\"numberAxis\" was not injected: check your FXML file 'EngineLoadResult.fxml'.";
        
        XYChart.Series<String, Number> running= new XYChart.Series<>();
        running.setName("running");

        XYChart.Series<String, Number> waiting= new XYChart.Series<>();
        waiting.setName("waiting");
        
        areaChart.getData().add(running);
        areaChart.getData().add(waiting);
    }
	
	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("EngineLoadResult.fxml");
	}

	@Override
	public void showFilteredResult(List<CopperLoadInfo> filteredlist, EngineLoadFilterModel usedFilter) {
		CopperLoadInfo copperLoadInfo = filteredlist.get(0);	
		String date = new SimpleDateFormat("HH:mm:ss").format(new Date());
		if (usedFilter.showRunning.getValue() ){
			areaChart.getData().get(0).getData().add(new XYChart.Data<String, Number>(date, copperLoadInfo.numberOfRunningWorkflowInstances));
		}
		if (usedFilter.showWaiting.getValue() ){
			areaChart.getData().get(1).getData().add(new XYChart.Data<String, Number>(date, copperLoadInfo.numberOfWaitingWorkflowInstances));
		}
	}

	@Override
	public List<CopperLoadInfo> applyFilterInBackgroundThread(EngineLoadFilterModel filter) {
		return Arrays.asList(copperDataProvider.getCopperLoadInfo());
	}

}
