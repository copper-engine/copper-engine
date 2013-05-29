package de.scoopgmbh.copper.monitoring.client.context;

import de.scoopgmbh.copper.monitoring.client.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.monitoring.client.form.FxmlForm;
import de.scoopgmbh.copper.monitoring.client.form.ShowFormStrategy;
import de.scoopgmbh.copper.monitoring.client.form.TabPaneShowFormStrategie;
import de.scoopgmbh.copper.monitoring.client.form.enginefilter.EngineFilterAbleForm;
import de.scoopgmbh.copper.monitoring.client.form.enginefilter.EngineFilterModel;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterAbleForm;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterController;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterResultController;
import de.scoopgmbh.copper.monitoring.client.util.MessageProvider;

/**
 * @return
 * FM = filtermodel
 * RM resultmodel
 * R Resultcontroller
 * F Filtercontroller
 */
public class FormBuilder<FM,RM, F extends FilterController<FM> ,R extends FilterResultController<FM,RM>>{
	protected final F filterController;
	protected final R resultController;
	protected final MessageProvider messageProvider;
	protected final GuiCopperDataProvider guiCopperDataProvider;
	protected final ShowFormStrategy<?> showFormStrategy;
	
	public FormBuilder(F filterController, R resultController, MessageProvider messageProvider,
			GuiCopperDataProvider guiCopperDataProvider, ShowFormStrategy<?> showFormStrategy){
		this.filterController =filterController;
		this.resultController = resultController;
		this.messageProvider = messageProvider;
		this.guiCopperDataProvider = guiCopperDataProvider;
		this.showFormStrategy = showFormStrategy;
	}
	
	public FormBuilder(F filterController, R resultController, FormContext formContext){
		this(filterController,resultController,formContext.messageProvider,formContext.guiCopperDataProvider,new TabPaneShowFormStrategie(formContext.mainTabPane));
	}
	
	public FilterAbleForm<FM,RM> build(){
		FilterController<FM> fCtrl = filterController; 
		FxmlForm<FilterController<FM>> filterForm = new FxmlForm<FilterController<FM>>(fCtrl, messageProvider);
		
		FilterResultController<FM,RM> resCtrl = resultController;
		FxmlForm<FilterResultController<FM,RM>> resultForm = 
				new FxmlForm<FilterResultController<FM,RM>>(resCtrl, messageProvider);
		
		return new FilterAbleForm<FM,RM>(messageProvider, showFormStrategy, filterForm, resultForm,guiCopperDataProvider);
	}
	
	
	
	public static class EngineFormBuilder<FM extends EngineFilterModel,RM, F extends FilterController<FM> ,R extends FilterResultController<FM,RM>> extends FormBuilder<FM,RM, F,R>{

		public EngineFormBuilder(F filterController, R resultController, FormContext formContext) {
			super(filterController, resultController, formContext);
		}

		public EngineFormBuilder(F filterController, R resultController, MessageProvider messageProvider,
				GuiCopperDataProvider guiCopperDataProvider, ShowFormStrategy<?> showFormStrategy, String title) {
			super(filterController, resultController, messageProvider, guiCopperDataProvider, showFormStrategy);
		}
		
		@Override
		public EngineFilterAbleForm<FM,RM> build(){
			FilterController<FM> fCtrl = filterController; 
			FxmlForm<FilterController<FM>> filterForm = new FxmlForm<FilterController<FM>>(fCtrl, messageProvider);
			
			FilterResultController<FM,RM> resCtrl = resultController;
			FxmlForm<FilterResultController<FM,RM>> resultForm = 
					new FxmlForm<FilterResultController<FM,RM>>(resCtrl, messageProvider);
			
			return new EngineFilterAbleForm<FM,RM>(messageProvider, showFormStrategy, filterForm, resultForm,guiCopperDataProvider);
		}
	}
}



//public class FormBuilder<FM extends EngineFilterModel,RM, F extends FilterController<FM> ,R extends FilterResultController<FM,RM>>{
//	private final F filterController;
//	private final R resultController;
//	private final MessageProvider messageProvider;
//	private final GuiCopperDataProvider guiCopperDataProvider;
//	private final ShowFormStrategy<?> showFormStrategy;
//	private final String title;
//	
//	public FormBuilder(F filterController, R resultController, MessageProvider messageProvider,
//			GuiCopperDataProvider guiCopperDataProvider, ShowFormStrategy<?> showFormStrategy, String title){
//		this.filterController =filterController;
//		this.resultController = resultController;
//		this.messageProvider = messageProvider;
//		this.guiCopperDataProvider = guiCopperDataProvider;
//		this.showFormStrategy = showFormStrategy;
//		this.title = title;
//	}
//	
//	public FormBuilder(F filterController, R resultController, FormContext formContext, MessageKey messageKey){
//		this(filterController,resultController,formContext.messageProvider,formContext.guiCopperDataProvider,new TabPaneShowFormStrategie(formContext.mainTabPane),formContext.messageProvider.getText(MessageKey.workflowInstance_title));
//	}
//	
//	public EngineFilterAbleForm<FM,RM> build(){
//		FilterController<FM> fCtrl = filterController; 
//		FxmlForm<FilterController<FM>> filterForm = new FxmlForm<FilterController<FM>>(fCtrl, messageProvider);
//		
//		FilterResultController<FM,RM> resCtrl = resultController;
//		FxmlForm<FilterResultController<FM,RM>> resultForm = 
//				new FxmlForm<FilterResultController<FM,RM>>(resCtrl, messageProvider);
//		
//		return new EngineFilterAbleForm<FM,RM>(title,messageProvider,
//				showFormStrategy, filterForm, resultForm,guiCopperDataProvider);
//	}
//}