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
package org.copperengine.core.util;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VarProperties extends Properties {
    private static final long serialVersionUID = 1L;

    private static final Pattern p = Pattern.compile("\\$\\{([^{}]+)\\}");

    // private static final Pattern p = Pattern.compile("\\$\\{.+\\}" );

    public String getProperty(String key) {
        String vorher = super.getProperty(key);
        String nachher = null;
        while (vorher != null && !vorher.equals(nachher = doReplacements(vorher))) {
            vorher = nachher;
        }
        return nachher;
    }

    private String doReplacements(String value) {
        if (value == null)
            return null;
        Matcher m = p.matcher(value);
        StringBuffer sb = new StringBuffer();
        String replacement = null;
        while (m.find()) {
            if (replacement == null) {
                replacement = System.getProperty(m.group(1));
                if (replacement != null)
                    replacement = replacement.replaceAll("\\$", "\\\\\\$");
            }
            if (replacement != null && replacement.length() > 0) {
                m = m.appendReplacement(sb, replacement);
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
