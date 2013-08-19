package de.scoopgmbh.copper.persistent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Array;
import java.sql.Connection;

/**
 * Some utils to use oracle driver without link dependencies. This is due to the fact that oracle drivers are not admissible in public repositories.
 * @author rscheel
 *
 */
public class OracleUtil {
	
	static Constructor<?>     arrayDescriptorCtor; 
	static Constructor<? extends Array> arrayCtor;
	
	static 
	{
		try {
			arrayDescriptorCtor = Class.forName("oracle.sql.ArrayDescriptor").getConstructor(String.class, Connection.class);
			@SuppressWarnings("unchecked")
			Class<? extends Array> arrayClass = (Class<? extends Array>)Class.forName("oracle.sql.ARRAY");
			arrayCtor = arrayClass.getConstructor(Class.forName("oracle.sql.ArrayDescriptor"), Connection.class, Object.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Object createArrayDescriptor(String arrayTypeName, Connection nativeConnection) {
		try {
			return
			        arrayDescriptorCtor.newInstance(arrayTypeName, nativeConnection);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public static Array createArray(Object arrayDescriptor, Connection nativeConnection, Object args) {
		try {
			return arrayCtor.newInstance(arrayDescriptor, nativeConnection, args);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public static Array createArray(String arrayTypeName, Connection nativeConnection, Object args) {
		return createArray(createArrayDescriptor(arrayTypeName, nativeConnection), nativeConnection, args);
	}

}
