package de.scoopgmbh.copper.gui.util;

import javafx.scene.paint.Color;

public class CSSHelper {

	public static String toCssColor(Color color){
		return "rgba("+
				Math.round(255*color.getRed())+","+
				Math.round(255*color.getGreen())+","+
				Math.round(255*color.getBlue())+","+
				color.getOpacity()+
				")";
	}
}

