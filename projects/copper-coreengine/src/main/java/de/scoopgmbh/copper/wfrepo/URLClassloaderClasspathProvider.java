package de.scoopgmbh.copper.wfrepo;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class URLClassloaderClasspathProvider implements CompilerOptionsProvider {

	@Override
	public Collection<String> getOptions() {
		StringBuilder buf = new StringBuilder();
		URLClassLoader loader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
		for ( URL url : loader.getURLs() ) {
			buf.append(url.getFile()).append(File.pathSeparator);
		}
		List<String> options = new ArrayList<String>();
		options.add("-classpath");
		options.add(buf.toString());
		return options;
	}

}
