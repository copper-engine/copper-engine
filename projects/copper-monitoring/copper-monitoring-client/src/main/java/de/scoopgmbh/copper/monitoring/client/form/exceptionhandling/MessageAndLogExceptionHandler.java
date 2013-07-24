package de.scoopgmbh.copper.monitoring.client.form.exceptionhandling;

import javafx.scene.layout.StackPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.scoopgmbh.copper.monitoring.client.util.ComponentUtil;

public class MessageAndLogExceptionHandler implements ExceptionHandler {
	
	Logger logger = LoggerFactory.getLogger(MessageAndLogExceptionHandler.class);
	private final StackPane stackPane;

	
	
	public MessageAndLogExceptionHandler(StackPane stackPane) {
		super();
		this.stackPane = stackPane;
	}

	@Override
	public void handleException(Throwable e) {
		logger.error("",e);
		ComponentUtil.showErrorMessage(stackPane,e.getMessage(), e);
	}

}
