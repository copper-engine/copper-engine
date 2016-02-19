/*
 * Copyright 2002-2015 SCOOP Software GmbH
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
package org.copperengine.monitoring.server.logging;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.xml.DOMConfigurator;
import org.copperengine.monitoring.server.provider.MonitoringLog4jDataProvider;
import org.copperengine.monitoring.server.util.FileUtil;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class Log4jConfigManager implements LogConfigManager {

    private final MonitoringLog4jDataProvider monitoringLog4jDataProvider;

    public Log4jConfigManager(MonitoringLog4jDataProvider monitoringLog4jDataProvider) {
        this.monitoringLog4jDataProvider = monitoringLog4jDataProvider;
    }

    @Override
    public void updateLogConfig(String config) {
        Properties props = new Properties();
        StringReader reader = null;
        try {
            reader = new StringReader(config);
            try {
                props.load(reader);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        monitoringLog4jDataProvider.removeFromRootLogger();

        org.apache.log4j.LogManager.resetConfiguration();
        logProperty = config;
        if (isXml(config)) {
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(new InputSource(new StringReader(config)));
                DOMConfigurator.configure(doc.getDocumentElement());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            PropertyConfigurator.configure(props);
        }
        monitoringLog4jDataProvider.addToRootLogger();
    }

    String logProperty;

    public boolean isXml(String text) {
        try {
            XMLReader parser = XMLReaderFactory.createXMLReader();
            parser.setContentHandler(new DefaultHandler());
            InputSource source = new InputSource(new StringReader(text));
            parser.parse(source);
            return true;
        } catch (Exception ioe) {
            return false;
        }
    }

    @Override
    public String getLogConfig() {
        String propertylocation = System.getProperty("log4j.configuration");
        if (propertylocation == null) {
            propertylocation = "log4j.properties";
        }
        InputStream input = null;

        String config = "";
        if (logProperty == null) {
            try {
                final URL resource = Loader.getResource(propertylocation);
                if (resource != null) {
                    try {
                        input = resource.openStream();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (input == null) {
                    try {
                        input = new FileInputStream(propertylocation);
                    } catch (FileNotFoundException e) {
                        // ignore
                    }
                }
                if (input != null) {
                    logProperty = FileUtil.convertStreamToString(input);
                }
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        config = logProperty;

        return config;
    }

}
