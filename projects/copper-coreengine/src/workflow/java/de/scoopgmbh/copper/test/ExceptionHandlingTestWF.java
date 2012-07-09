package de.scoopgmbh.copper.test;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.scoopgmbh.copper.InterruptException;
import de.scoopgmbh.copper.persistent.PersistentWorkflow;

public class ExceptionHandlingTestWF extends PersistentWorkflow<Serializable> {
	
	
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlingTestWF.class);

	@Override
	public void main() throws InterruptException {
		//System.err.println("main "+this.__stack);
		main_1();
	}
	
	
	public void main_1() throws InterruptException {
		//System.err.println("main_1 "+this.__stack);
		try{
			red("");
		}
		catch(RuntimeException e){
			logger.debug(e.toString());
			//throw e;
		}
	}
	
	
	public void red(String prefix) throws InterruptException{
		//System.err.println("red "+this.__stack);
		String red = new String(prefix+":red");
		blue(red);
	}
	
	public void blue(String prefix) throws InterruptException{
		//System.err.println("blue "+this.__stack);
		String blue = new String(prefix+":blue");
		green(blue);
	}

	public void green(String prefix) throws InterruptException{
		//System.err.println("gree "+this.__stack);
		throw new RuntimeException("out of colour");
	}
	
}
