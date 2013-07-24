package de.scoopgmbh.copper.monitoring.client.form.exceptionhandling;

/**
 * workaround for a bug in javafx 2.2 
 * http://stackoverflow.com/questions/12318861/javafx-2-catching-all-runtime-exceptions
 * https://javafx-jira.kenai.com/browse/RT-15332
 */
public interface ExceptionHandler {
	public void handleException(Throwable e);
}
