package de.scoopgmbh.copper.test;

import java.io.Serializable;

import de.scoopgmbh.copper.InterruptException;
import de.scoopgmbh.copper.persistent.PersistentWorkflow;

public class ExceptionHandlingTestWF extends PersistentWorkflow<Serializable> {

	private static final long serialVersionUID = 1L;

	@Override
	public void main() throws InterruptException {
		try{
			red("");
		}
		catch(RuntimeException e){
			e.printStackTrace();
		}
	}
	
	
	public void red(String prefix) throws InterruptException{
		String red = new String(prefix+":red");
		blue(red);
	}
	
	public void blue(String prefix) throws InterruptException{
		String blue = new String(prefix+":blue");
		green(blue);
	}

	public void green(String prefix) throws InterruptException{
		throw new RuntimeException("out of colour");
	}
	
}
