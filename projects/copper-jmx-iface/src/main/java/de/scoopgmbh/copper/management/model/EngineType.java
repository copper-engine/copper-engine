package de.scoopgmbh.copper.management.model;

public enum EngineType {
	persistent, 
	tranzient /** named tran_z_ient instead of transient, because transient is a java keyword */, 
	hyprid, 
	other 
}
