/**
 * Copyright 2002-2017 SCOOP Software GmbH
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
package org.copperengine.performancetest.main;

import java.io.PrintStream;
import java.util.Properties;

import org.slf4j.Logger;

public class ConfigurationManager {

    private final Properties props;

    public ConfigurationManager(Properties props) {
        this.props = props;
    }

    public int getConfigInt(ConfigParameter p) {
        String v = props.getProperty(p.getKey());
        if (v == null || v.trim().isEmpty())
            return ((Integer) p.getDefaultValue()).intValue();
        return Integer.parseInt(v);
    }

    public boolean getConfigBoolean(ConfigParameter p) {
        String v = props.getProperty(p.getKey());
        if (v == null || v.trim().isEmpty())
            return ((Boolean) p.getDefaultValue()).booleanValue();
        return Boolean.parseBoolean(v);
    }

    public Integer getConfigInteger(ConfigParameter p) {
        String v = props.getProperty(p.getKey());
        if (v == null || v.trim().isEmpty())
            return (Integer) p.getDefaultValue();
        return Integer.parseInt(v);
    }

    public String getConfigString(ConfigParameter p) {
        String v = props.getProperty(p.getKey());
        if (v == null || v.trim().isEmpty())
            return (String) p.getDefaultValue();
        return v;
    }

    private Object getConfig(ConfigParameter p) {
        Object v = props.getProperty(p.getKey());
        return v == null ? p.getDefaultValue() : v;
    }

    public void print(PrintStream ps) {
        for (ConfigParameterGroup grp : ConfigParameterGroup.values()) {
            for (ConfigParameter p : ConfigParameter.all4group(grp)) {
                System.out.println(p.getKey() + "=" + getConfig(p));
            }
        }
    }

    public void log(Logger logger, ConfigParameterGroup... grps) {
        logger.info("Configuration parameters:");
        for (ConfigParameterGroup grp : grps) {
            for (ConfigParameter p : ConfigParameter.all4group(grp)) {
                logger.info("{}={}", p.getKey(), getConfig(p));
            }
        }
    }
}
