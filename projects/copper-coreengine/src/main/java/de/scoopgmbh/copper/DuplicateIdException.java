package de.scoopgmbh.copper;

/**
 * An object (workflow instance of reponse) with the same id already exists
 *  
 * @author austermann
 *
 */
public class DuplicateIdException extends CopperRuntimeException {

	private static final long serialVersionUID = 1L;

	public DuplicateIdException() {
		super();
	}

	public DuplicateIdException(String message, Throwable cause) {
		super(message, cause);
	}

	public DuplicateIdException(String message) {
		super(message);
	}

	public DuplicateIdException(Throwable cause) {
		super(cause);
	}

}
