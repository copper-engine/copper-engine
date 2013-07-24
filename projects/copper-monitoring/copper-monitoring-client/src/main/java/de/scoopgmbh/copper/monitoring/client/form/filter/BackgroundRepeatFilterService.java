package de.scoopgmbh.copper.monitoring.client.form.filter;

import java.util.List;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import de.scoopgmbh.copper.monitoring.client.form.Form;
import de.scoopgmbh.copper.monitoring.client.form.exceptionhandling.ExceptionHandler;

public class BackgroundRepeatFilterService<F,R>  extends Service<Void> {
	private long refreshRate=1000;
	long lasttime = System.currentTimeMillis();
    private final FilterResultController<F,R> filterResultController;
    private final Form<FilterController<F>> filterForm;
    private final ExceptionHandler exceptionHandler;
    
    public BackgroundRepeatFilterService(FilterResultController<F,R> filterResultController, Form<FilterController<F>> filterForm, ExceptionHandler exceptionHandler) {
		super();
		this.filterResultController = filterResultController;
		this.filterForm = filterForm;
		this.exceptionHandler = exceptionHandler;
	}
    
	public void setRefreshIntervall(long refreshRate) {
		this.refreshRate = refreshRate;
	}

	@Override
	public void start() {
		lasttime = System.currentTimeMillis();
		super.start();
	}
    
	@Override
	protected Task<Void> createTask() {
		return new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				while (!isCancelled()) {
					if (lasttime + refreshRate < System.currentTimeMillis()) {
						updateProgress(-1, 1);
						final List<R> result;
						try {
							result = filterResultController.applyFilterInBackgroundThread(filterForm.getController().getFilter());
						} catch (Exception e1) {
							exceptionHandler.handleException(e1);
							cancel();
							throw new RuntimeException(e1);
						}
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								try {
									filterResultController.showFilteredResult(result, filterForm.getController().getFilter());
								} catch (Exception e) {
									exceptionHandler.handleException(e);
									cancel();
								}
							}
						});
						lasttime = System.currentTimeMillis();
					}
					Thread.sleep(Math.min(50,refreshRate/10));
					long progress = System.currentTimeMillis() - lasttime;
					progress = progress <= refreshRate ? progress : refreshRate;
					if (refreshRate<=500){
						progress=-1;
					}
					updateProgress(progress, refreshRate);
				}
				return null;
			}
		};
	}
}