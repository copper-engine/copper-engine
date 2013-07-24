package de.scoopgmbh.copper.monitoring.client.form.filter;

import java.util.List;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import de.scoopgmbh.copper.monitoring.client.form.Form;
import de.scoopgmbh.copper.monitoring.client.form.exceptionhandling.ExceptionHandler;

public class BackgroundFilterService<F,R>  extends Service<FilterAbleForm.ResultFilterPair<F,R>> {
    private final FilterResultController<F,R> filterResultController;
    private final Form<FilterController<F>> filterForm;
    private final ExceptionHandler exceptionHandler;
    
    public BackgroundFilterService(FilterResultController<F,R> filterResultControllerParam, Form<FilterController<F>> filterForm, ExceptionHandler exceptionHandlerParam) {
		super();
		this.filterResultController = filterResultControllerParam;
		this.filterForm = filterForm;
		this.exceptionHandler = exceptionHandlerParam;
		
		setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				try {
	            	@SuppressWarnings("unchecked")
					FilterAbleForm.ResultFilterPair<F, R> result = (FilterAbleForm.ResultFilterPair<F,R>)event.getSource().getValue();
	            	filterResultController.showFilteredResult(result.result, result.usedFilter);
				} catch (Exception e){
					exceptionHandler.handleException(e);
				}
			}
		});
	}

	@Override
	protected Task<FilterAbleForm.ResultFilterPair<F,R>> createTask() {
		return new Task<FilterAbleForm.ResultFilterPair<F,R>>() {
            @Override
			protected FilterAbleForm.ResultFilterPair<F,R> call() throws Exception {
				try {
					final List<R> result = filterResultController.applyFilterInBackgroundThread(filterForm.getController().getFilter());
					return new FilterAbleForm.ResultFilterPair<F, R>(result, filterForm.getController().getFilter());
				} catch (Exception e) {
					exceptionHandler.handleException(e);
					throw new RuntimeException(e);
				}
			}
        };
    }
	
}