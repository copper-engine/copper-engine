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
