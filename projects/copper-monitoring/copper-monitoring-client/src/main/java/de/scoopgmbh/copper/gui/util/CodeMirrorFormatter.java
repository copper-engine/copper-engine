/*
 * Copyright 2002-2013 SCOOP Software GmbH
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
package de.scoopgmbh.copper.gui.util;

import java.io.IOException;
import java.io.InputStream;

public class CodeMirrorFormatter{
	private final String codemirrorcss;
	private final String codemirrorjs;
	private final String xmlmodejs;
	private final String javascriptjs;
	private final String sqljs;
	
	public CodeMirrorFormatter() {
		codemirrorcss = convertStreamToString("/codemirror/lib/codemirror.css");
		codemirrorjs = convertStreamToString("/codemirror/lib/codemirror.js");
		javascriptjs = convertStreamToString("/codemirror/mode/json/javascript.js");
		xmlmodejs = convertStreamToString("/codemirror/mode/xml/xml.js");
		sqljs = convertStreamToString("/codemirror/mode/sql/plsql.js");
	}
	
    private String convertStreamToString(String ressourceclasspath) {
    	InputStream input = null; 
    	try {
    		input = getClass().getResourceAsStream(ressourceclasspath);
    		@SuppressWarnings("resource")
    		java.util.Scanner s = new java.util.Scanner(input).useDelimiter("\\A");
    		return s.hasNext() ? s.next() : "";
    	} finally {
    		try {
				input.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
    	}
    }
	
	public String format(String code, CodeFormatLanguage language){
		String mode = xmlmodejs;
		String modeScript= "<script>\n" + 
				"      var editor = CodeMirror.fromTextArea(document.getElementById(\"code\"), {\n" + 
				"        mode: {name: \"xml\", alignCDATA: true},\n" + 
				"        lineNumbers: true\n" + 
				"      });\n" + 
				"    </script>";
		if (language==CodeFormatLanguage.JAVASCRIPT){
			mode=javascriptjs;
			modeScript=
					"    <script>\r\n" + 
					"      var editor = CodeMirror.fromTextArea(document.getElementById(\"code\"), {\r\n" + 
					"        lineNumbers: true,\r\n" + 
					"        matchBrackets: true,\r\n" + 
					"        extraKeys: {\"Enter\": \"newlineAndIndentContinueComment\"}\r\n" + 
					"      });\r\n" + 
					"    </script>";
		}
		if (language==CodeFormatLanguage.SQL){
			mode=sqljs;
			modeScript=
					"    <script>\r\n" + 
					"      var editor = CodeMirror.fromTextArea(document.getElementById(\"code\"), {\r\n" + 
					"        lineNumbers: true,\r\n" + 
					"        indentUnit: 4,\r\n" + 
					"        mode: \"text/x-plsql\"\r\n" + 
					"      });\r\n" + 
					"    </script>";
		}
				
		String formatedMessage = "<!doctype html>" +
				"<html><head>" +
				"<style type=\"text/css\">\n" + 
				codemirrorcss+"\n"+
				"</style>"+
				" <script>"+codemirrorjs+"</script>" +
				" <script>"+mode+"</script>" +
				"</head>" +
				"<body>" +
				"<form><textarea id=\"code\" name=\"code\" style=\"width: 100%; height: 100%;\">\n" +
				code+
				"</textarea></form>" +
				modeScript+
				"</body>" +
				"</html>";
		return formatedMessage;
	}
	
	public static enum CodeFormatLanguage{
		JAVASCRIPT,XML,SQL;
	}
	
}