/*
 * Copyright 2002-2014 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.copperengine.monitoring.client.ui.settings;

import java.text.MessageFormat;
import java.util.Arrays;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;

public class CssConfig {
    public static final int COLOR_COUNT = 3;

    public static final String[] DEFAULT_LIGHT_COLORS_HEX = { "#e0e0ff", "#ffffc0", "#a0ffc0" };
    public static final String[] DEFAULT_DARK_COLORS_HEX = { "#0f1720", "#1f1f00", "#0f2f1f" };

    public static final Color[] DEFAULT_LIGHT_COLORS = new Color[COLOR_COUNT];
    public static final Color[] DEFAULT_DARK_COLORS = new Color[COLOR_COUNT];

    static {
        for (int i = 0; i < COLOR_COUNT; i++) {
            DEFAULT_LIGHT_COLORS[i] = Color.valueOf(DEFAULT_LIGHT_COLORS_HEX[i]);
            DEFAULT_DARK_COLORS[i] = Color.valueOf(DEFAULT_DARK_COLORS_HEX[i]);
        }
    }

    private static final String colorsCssTemplate =
            ".root'{'\n" +
                    "  -fx-const-color1: {0};\n" +
                    "  -fx-const-color2: {1};\n" +
                    "  -fx-const-color3: {2};\n" +
                    "'}'\n";
    private static final MessageFormat colorsCssFormatter = new MessageFormat(colorsCssTemplate);

    public enum Type {
        CSS, LIGHT, DARK
    };

    private final CssConfig.Type type;
    @SuppressWarnings("unchecked")
    private final ObjectProperty<Color>[] colorProperties = new ObjectProperty[COLOR_COUNT];
    private StringProperty cssUrlProperty = new SimpleStringProperty("");

    public static CssConfig createLight() {
        return new CssConfig("LIGHT");
    }

    public static CssConfig createDark() {
        return new CssConfig("DARK");
    }

    public static CssConfig createUrl() {
        return new CssConfig();
    }

    private CssConfig() {
        type = Type.CSS;
        for (int i = 0; i < COLOR_COUNT; i++) {
            colorProperties[i] = new SimpleObjectProperty<>();
        }
    }

    public CssConfig(String cssUri) {
        for (int i = 0; i < COLOR_COUNT; i++) {
            colorProperties[i] = new SimpleObjectProperty<>();
        }
        cssUri = get(cssUri, "LIGHT");
        String[] tokens = Arrays.copyOf(cssUri.split(":"), COLOR_COUNT + 1);
        String typeToken = get(tokens[0], "LIGHT");
        type = typeToken.equals("LIGHT") ? Type.LIGHT : (typeToken.equals("DARK") ? Type.DARK : Type.CSS);
        if (type == Type.CSS) {
            setCssUrl(cssUri);
        } else {
            String[] defaultColors = (type == Type.LIGHT) ? DEFAULT_LIGHT_COLORS_HEX : DEFAULT_DARK_COLORS_HEX;

            for (int i = 0; i < COLOR_COUNT; i++) {
                String color = get(tokens[i + 1], defaultColors[i]);
                colorProperties[i].set(Color.valueOf(color));
            }
        }
    }

    private static String get(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty())
            return defaultValue;
        return value.trim();
    }

    public CssConfig.Type getType() {
        return type;
    }

    public ObjectProperty<Color>[] getColorProperties() {
        return colorProperties;
    }

    public Color getColor(int i) {
        return colorProperties[i].get();
    }

    public StringProperty getCssUrlProperty() {
        return cssUrlProperty;
    }

    public String getCssUrl() {
        return cssUrlProperty.getValue();
    }

    public void setCssUrl(String cssUrl) {
        this.cssUrlProperty.setValue(cssUrl);
    }

    public String getColorsCssContent() {
        String[] colors = new String[COLOR_COUNT];
        for (int i = 0; i < COLOR_COUNT; i++) {
            colors[i] = toHex(colorProperties[i].get());
        }
        return colorsCssFormatter.format(colors);
    }

    public static String toHex(Color color) {
        int red = (int) (Math.round(color.getRed() * 255));
        int green = (int) (Math.round(color.getGreen() * 255));
        int blue = (int) (Math.round(color.getBlue() * 255));
        return String.format("#%02X%02X%02X", red, green, blue);
    }

    @Override
    public String toString() {
        if (type == Type.CSS)
            return getCssUrl();
        StringBuilder sb = new StringBuilder();
        sb.append(type.name());
        for (int i = 0; i < COLOR_COUNT; i++) {
            sb.append(':').append(toHex(getColor(i)));
        }
        return sb.toString();
    }
}