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
package org.copperengine.core.wfrepo;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class URLClassloaderClasspathProvider implements CompilerOptionsProvider {

    @Override
    public Collection<String> getOptions() {
        StringBuilder buf = new StringBuilder();
        ClassLoader loader = (ClassLoader) Thread.currentThread().getContextClassLoader();
        while (loader != null) {
            if (loader instanceof URLClassLoader) {
                StringBuilder buf2 = new StringBuilder();
                for (URL url : ((URLClassLoader) loader).getURLs()) {
                    File f = null;
                    try {
                        // convert the URL to a URI to remove the HTML encoding, if it exists.
                        f = new File(url.toURI().getPath());
                    } catch (URISyntaxException e) {
                        throw new RuntimeException("failed to convert the classpath URL '" + url + "' to a URI", e);
                    }
                    buf2.append(f.getAbsolutePath()).append(File.pathSeparator);
                }
                buf.insert(0, buf2.toString());
            }
            loader = loader.getParent();
        }
        List<String> options = new ArrayList<String>();
        options.add("-classpath");
        options.add(buf.toString());
        return options;
    }

}
