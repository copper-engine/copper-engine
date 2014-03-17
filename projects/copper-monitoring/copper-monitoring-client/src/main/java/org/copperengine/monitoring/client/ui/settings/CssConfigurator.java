package org.copperengine.monitoring.client.ui.settings;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import javafx.collections.ObservableList;

public class CssConfigurator {
    private static final String CSS_DIR = "/org/copperengine/gui/css/";
    
    private final CssConfig cssConfig;
    
    public CssConfigurator(CssConfig cssConfig) {
        this.cssConfig = cssConfig;
    }

    public CssConfigurator(String cssUri) {
        this.cssConfig = new CssConfig(cssUri);
    }

    public void configure(ObservableList<String> stylesheets) throws IOException {
        stylesheets.clear();
        String baseColorsCssUrl = this.getClass().getResource(CSS_DIR + "base-colors.css").toExternalForm();
        stylesheets.add(baseColorsCssUrl);

        // theme custom colors
        if(cssConfig.getType() != CssConfig.Type.CSS) {
            File themeColorsFile = File.createTempFile("copper-colors-", ".css");
            themeColorsFile.deleteOnExit();

            String colorsCssContent = cssConfig.getColorsCssContent();
            try(Writer cssWriter = new FileWriter(themeColorsFile)) {
                cssWriter.write(colorsCssContent);
            }
            stylesheets.add(themeColorsFile.toURI().toURL().toExternalForm());
        }
        
        String baseCssUrl = this.getClass().getResource(CSS_DIR + "base.css").toExternalForm();
        stylesheets.add(baseCssUrl);

        if(cssConfig.getType() == CssConfig.Type.CSS) {
            // custom css
            stylesheets.add(cssConfig.getCssUrl());
        } else {
            // theme css
            String themeFile = CSS_DIR + cssConfig.getType().name().toLowerCase() + ".css";
            String themeUrl = this.getClass().getResource(themeFile).toExternalForm();
            stylesheets.add(themeUrl);
        }
    }
}
