package de.scoopgmbh.copper.gui.util;

import java.io.IOException;
import java.io.InputStream;

public class CodeMirrorFormatter{
	private final String codemirrorcss;
	private final String codemirrorjs;
	private final String xmlmodejs;
	private final String javascriptjs;
	private final String sqljs;
	
	public CodeMirrorFormatter(){
		try {
			try (InputStream input = getClass().getResourceAsStream("/codemirror/lib/codemirror.css")) {
				codemirrorcss = convertStreamToString(input);
			}
			try (InputStream input = getClass().getResourceAsStream("/codemirror/lib/codemirror.js")) {
				codemirrorjs = convertStreamToString(input);
			}
			try (InputStream input = getClass().getResourceAsStream("/codemirror/mode/json/javascript.js")) {
				javascriptjs = convertStreamToString(input);
			}
			try (InputStream input = getClass().getResourceAsStream("/codemirror/mode/xml/xml.js")) {
				xmlmodejs = convertStreamToString(input);
			}
			try (InputStream input = getClass().getResourceAsStream("/codemirror/mode/sql/plsql.js")) {
				sqljs = convertStreamToString(input);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
    private static String convertStreamToString(java.io.InputStream is) {
        @SuppressWarnings("resource")
		java.util.Scanner s = new java.util.Scanner(is,"UTF-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
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